/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.tdb.store.bulkloader3;

import static com.hp.hpl.jena.tdb.lib.NodeLib.setHash;
import static com.hp.hpl.jena.tdb.sys.SystemTDB.LenNodeHash;
import static com.hp.hpl.jena.tdb.sys.SystemTDB.SizeOfNodeId;
import static tdb.tdbloader3.spill_size;
import static tdb.tdbloader3.spill_size_auto;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;

import org.apache.commons.codec.binary.Hex;
import org.openjena.atlas.AtlasException;
import org.openjena.atlas.data.DataBag;
import org.openjena.atlas.data.SerializationFactory;
import org.openjena.atlas.data.ThresholdPolicy;
import org.openjena.atlas.data.ThresholdPolicyCount;
import org.openjena.atlas.data.ThresholdPolicyMemory;
import org.openjena.atlas.iterator.Iter;
import org.openjena.atlas.iterator.Transform;
import org.openjena.atlas.lib.Bytes;
import org.openjena.atlas.lib.Pair;
import org.openjena.atlas.lib.Sink;
import org.openjena.atlas.lib.Tuple;
import org.slf4j.Logger;

import tdb.tdbloader3;


import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.tdb.base.block.BlockMgr;
import com.hp.hpl.jena.tdb.base.block.BlockMgrFactory;
import com.hp.hpl.jena.tdb.base.file.FileFactory;
import com.hp.hpl.jena.tdb.base.file.FileSet;
import com.hp.hpl.jena.tdb.base.objectfile.ObjectFile;
import com.hp.hpl.jena.tdb.base.record.Record;
import com.hp.hpl.jena.tdb.base.record.RecordFactory;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTree;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTreeParams;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTreeRewriter;
import com.hp.hpl.jena.tdb.lib.NodeLib;
import com.hp.hpl.jena.tdb.solver.stats.StatsCollectorNodeId;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB;
import com.hp.hpl.jena.tdb.store.Hash;
import com.hp.hpl.jena.tdb.store.NodeId;
import com.hp.hpl.jena.tdb.store.bulkloader.BulkLoader;
import com.hp.hpl.jena.tdb.sys.Names;
import com.hp.hpl.jena.tdb.sys.SystemTDB;

public class NodeTableBuilder2 implements Sink<Quad>
{
    private DatasetGraphTDB dsg ;
    private ObjectFile objects ;
    private DataBag<Tuple<Long>> outputTriples ;
    private DataBag<Tuple<Long>> outputQuads ;
    private ProgressLogger monitor ;
    private StatsCollectorNodeId stats ;
    
    private DataBag<Pair<byte[], byte[]>> sdb01 ;
    private DataBag<Pair<byte[], byte[]>> sdb02 ;
    private DataBag<Pair<byte[], byte[]>> sdb03 ;
    private SerializationFactory<Pair<byte[], byte[]>> serializationFactory = new PairSerializationFactory() ;
    
    private MessageDigest digest ;
    
    private final Logger log ;

    public NodeTableBuilder2(DatasetGraphTDB dsg, ProgressLogger monitor, DataBag<Tuple<Long>> outputTriples, DataBag<Tuple<Long>> outputQuads)
    {
//        dsg.getTripleTable().getNodeTupleTable().close() ; 

        this.dsg = dsg ;
        this.monitor = monitor ;
        this.log = monitor.getLogger() ;

        String filename = new FileSet(dsg.getLocation(), Names.indexId2Node).filename(Names.extNodeData) ;
        this.objects = FileFactory.createObjectFileDisk(filename) ; 
        
        this.outputTriples = outputTriples ; 
        this.outputQuads = outputQuads ; 
        this.stats = new StatsCollectorNodeId() ;
        
        this.sdb01 = new MultiThreadedSortedDataBag<Pair<byte[], byte[]>>(getThresholdPolicy(), serializationFactory, new PairComparator());
        
        try {
            this.digest = MessageDigest.getInstance("MD5") ;               
        } catch (NoSuchAlgorithmException e) {
            throw new AtlasException(e) ;
        }
    }
    
    public StatsCollectorNodeId getCollector() { return stats ; }

    private ThresholdPolicy<Pair<byte[], byte[]>> getThresholdPolicy() {
        if ( spill_size_auto == true ) {
            long memory = Math.round( Runtime.getRuntime().maxMemory() * 0.065 ) ; // in bytes
            log.info("Threshold spill is: " + memory) ;
            return new ThresholdPolicyMemory<Pair<byte[], byte[]>>(memory, serializationFactory);
        } else {
            return new ThresholdPolicyCount<Pair<byte[], byte[]>>(spill_size);            
        }
    }
    
    @Override
    public void send(Quad quad)
    {
        try {
            byte[] s = tdbloader3.serialize(quad.getSubject()).getBytes("UTF-8") ;
            byte[] p = tdbloader3.serialize(quad.getPredicate()).getBytes("UTF-8") ;
            byte[] o = tdbloader3.serialize(quad.getObject()).getBytes("UTF-8") ;
            byte[] g = null ;
            // Union graph?!
            if ( ! quad.isTriple() && ! quad.isDefaultGraph() )
                g = tdbloader3.serialize(quad.getGraph()).getBytes("UTF-8") ;
            
            digest.reset() ;
            digest.update(s) ; // TODO: should we do something better here?
            digest.update(p) ;
            digest.update(o) ;
            if ( g != null )
                digest.update(g.toString().getBytes("UTF-8")) ;

            String md5 = new String(Hex.encodeHex(digest.digest())) ;
            sdb01.add(new Pair<byte[], byte[]>(s, (md5 + "|s").getBytes("UTF-8"))) ;
            sdb01.add(new Pair<byte[], byte[]>(p, (md5 + "|p").getBytes("UTF-8"))) ;
            sdb01.add(new Pair<byte[], byte[]>(o, (md5 + "|o").getBytes("UTF-8"))) ;
            if ( g != null )
                sdb01.add(new Pair<byte[], byte[]>(g, (md5 + "|g").getBytes("UTF-8"))) ;
        } catch (UnsupportedEncodingException e) {
            throw new AtlasException(e) ;
        }

        monitor.tick() ;
    }

    @Override
    public void flush()
    {
        // TODO
    }

    @Override
    public void close() { 
        flush() ;
        
        // nodes.dat
        buildNodesObjectFile() ;
        generateSortedHashNodeIdDataBag() ;
        // node2id.dat and node2id.idn
        buildNodeTableBPTreeIndex() ;

        outputTriples.flush() ;
        outputQuads.flush() ;
        objects.sync() ;
    }
    
    private void buildNodesObjectFile() {
        
        // spill(sdb01) ;
        
        this.sdb02 = new MultiThreadedSortedDataBag<Pair<byte[], byte[]>>(getThresholdPolicy(), serializationFactory, new PairComparator());
        this.sdb03 = new MultiThreadedSortedDataBag<Pair<byte[], byte[]>>(getThresholdPolicy(), serializationFactory, new PairComparator());
        
        try {
            log.info("Node Table (1/3): building nodes.dat and sorting hash|id ...") ;
            ProgressLogger monitor01 = new ProgressLogger(log, "records for node table (1/3) phase", BulkLoader.DataTickPoint,BulkLoader.superTick) ;
            monitor01.start() ;
            String curr = null ;
            long id = -1L;
            Iterator<Pair<byte[], byte[]>> iter01 = sdb01.iterator() ;
            while ( iter01.hasNext() ) {
                Pair<byte[], byte[]> pair01 = iter01.next() ;
                String leftIn = new String(pair01.getLeft(), "UTF-8") ;
                String rightIn = new String(pair01.getRight(), "UTF-8") ;
                if ( ! leftIn.equals(curr) ) {
                    curr = leftIn ;
                    // generate the node id
                    Node node = tdbloader3.parse(leftIn) ;
                    id = NodeLib.encodeStore(node, objects) ;
                    // add to hash|id
                    Hash hash = new Hash(SystemTDB.LenNodeHash);
                    setHash(hash, node);
                    sdb03.add (new Pair<byte[], byte[]>(hash.getBytes(), Bytes.packLong(id))) ;
                }
//                System.out.println ("< ( " + leftIn + ", " + rightIn + " )") ;
                String tokens[] = rightIn.split("\\|") ;
                String leftOut = tokens[0] ;
                String rightOut = id + "|" + tokens[1] ;
//                System.out.println ("> ( " + leftOut + ", " + rightOut + " )") ;
                Pair<byte[], byte[]> pair02 = new Pair<byte[], byte[]>(leftOut.getBytes("UTF-8"), rightOut.getBytes("UTF-8")) ;
                sdb02.add(pair02) ;
                monitor01.tick() ;
            }
            ProgressLogger.print ( log, monitor01 ) ;
        } catch (UnsupportedEncodingException e) {
            throw new AtlasException(e) ;
        } finally {
            sdb01.close() ;
            sdb01 = null ;
//            spill (sdb02) ;
//            spill (sdb03) ;
        }
    }
    
    private void generateSortedHashNodeIdDataBag() {
        try {
            log.info("Node Table (2/3): generating input data using node ids...") ;
            final ProgressLogger monitor02 = new ProgressLogger(log, "records for node table (2/3) phase", BulkLoader.DataTickPoint,BulkLoader.superTick) ;
            monitor02.start() ;
            Iterator<Pair<byte[], byte[]>> iter02 = sdb02.iterator() ;
            String curr = null ;
            Long s = null ;
            Long p = null ;
            Long o = null ;
            Long g = null ;
            while ( iter02.hasNext() ) {
                Pair<byte[], byte[]> pair02 = iter02.next() ;
                String leftIn = new String(pair02.getLeft(), "UTF-8") ;
                String rightIn = new String(pair02.getRight(), "UTF-8") ;
//                System.out.println ("< ( " + leftIn + ", " + rightIn + " )") ;
                if ( curr == null ) curr = leftIn ;
                if ( ! leftIn.equals(curr) ) {
                    curr = leftIn ;
                    write (g, s, p, o) ;
                    s = null ;
                    p = null ;
                    o = null ;
                    g = null ;
                    monitor02.tick() ;
                }
                String tokens[] = rightIn.split("\\|") ;
                if ( "s".equals(tokens[1]) ) s = Long.parseLong(tokens[0]) ;
                else if ( "p".equals(tokens[1]) ) p = Long.parseLong(tokens[0]) ;
                else if ( "o".equals(tokens[1]) ) o = Long.parseLong(tokens[0]) ;
                else if ( "g".equals(tokens[1]) ) g = Long.parseLong(tokens[0]) ;
                
            }
            write (g, s, p, o) ; // ensure we write the last triple|quad
            ProgressLogger.print ( log, monitor02 ) ;
        } catch (UnsupportedEncodingException e) {
            throw new AtlasException(e) ;
        } finally {
            sdb02.close() ;
            sdb02 = null ;            
        }
    }

    private void buildNodeTableBPTreeIndex() {
        try {
            // Node table B+Tree index (i.e. node2id.dat/idn)
            log.info("Node Table (3/3): building node table B+Tree index (i.e. node2id.dat and node2id.idn files)...") ;
            final ProgressLogger monitor03 = new ProgressLogger(log, "records for node table (3/3) phase", BulkLoader.DataTickPoint,BulkLoader.superTick) ;
            monitor03.start() ;
            String path = dsg.getLocation().getDirectoryPath() ;
            new File(path, "node2id.dat").delete() ;
            new File(path, "node2id.idn").delete() ;
            
            final RecordFactory recordFactory = new RecordFactory(LenNodeHash, SizeOfNodeId) ;
            Transform<Pair<byte[], byte[]>, Record> transformPair2Record = new Transform<Pair<byte[], byte[]>, Record>() {
                @Override public Record convert(Pair<byte[], byte[]> pair) {
                    monitor03.tick() ;
                    return recordFactory.create(pair.getLeft(), pair.getRight()) ;
                }
            };

            int order = BPlusTreeParams.calcOrder(SystemTDB.BlockSize, recordFactory) ;
            BPlusTreeParams bptParams = new BPlusTreeParams(order, recordFactory) ;
            int readCacheSize = 10 ;
            int writeCacheSize = 100 ;
            FileSet destination = new FileSet(dsg.getLocation(), Names.indexNode2Id) ;
            BlockMgr blkMgrNodes = BlockMgrFactory.create(destination, Names.bptExtTree, SystemTDB.BlockSize, readCacheSize, writeCacheSize) ;
            BlockMgr blkMgrRecords = BlockMgrFactory.create(destination, Names.bptExtRecords, SystemTDB.BlockSize, readCacheSize, writeCacheSize) ;
            Iterator<Record> iter2 = Iter.iter(sdb03.iterator()).map(transformPair2Record) ;
            BPlusTree bpt2 = BPlusTreeRewriter.packIntoBPlusTree(iter2, bptParams, recordFactory, blkMgrNodes, blkMgrRecords) ;
            bpt2.sync() ;

            ProgressLogger.print ( log, monitor03 ) ;
        } finally {
            sdb03.close() ;
            sdb03 = null ;
        }
    }

    private void write (Long g, Long s, Long p, Long o) {
//        System.out.println ("> ( " + g + ", " + s + ", " + p + ", " + o + " )") ;
        if ( g != null ) {
            outputQuads.add(Tuple.create(g, s, p, o)) ;
            stats.record(new NodeId(g), new NodeId(s), new NodeId(p), new NodeId(o)) ;
        } else {
            outputTriples.add(Tuple.create(s, p, o)) ;
            stats.record(null, new NodeId(s), new NodeId(p), new NodeId(o)) ;
        }
    }
}


