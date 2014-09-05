/*
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

package com.hp.hpl.jena.tdb.lib;

import java.io.PrintStream ;
import java.util.Arrays ;
import java.util.HashSet ;
import java.util.Iterator ;
import java.util.Set ;

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.lib.ByteBufferLib ;
import org.apache.jena.atlas.lib.Pair ;
import org.apache.jena.atlas.lib.Tuple ;
import org.apache.jena.riot.out.NodeFmtLib ;
import arq.cmd.CmdException ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.tdb.base.block.Block ;
import com.hp.hpl.jena.tdb.base.block.BlockMgr ;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTree ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.store.DatasetPrefixesTDB ;
import com.hp.hpl.jena.tdb.store.NodeId ;
import com.hp.hpl.jena.tdb.store.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.store.nodetupletable.NodeTupleTable ;
import com.hp.hpl.jena.tdb.store.tupletable.TupleIndex ;
import com.hp.hpl.jena.tdb.store.tupletable.TupleTable ;

public class DumpOps
{
    public static void dump(Dataset ds)
    {
        DatasetGraphTDB dsg = (DatasetGraphTDB)(ds.asDatasetGraph()) ;

        NodeTupleTable nodeTupleTableTriples = dsg.getTripleTable().getNodeTupleTable() ;
        NodeTupleTable nodeTupleTableQuads = dsg.getQuadTable().getNodeTupleTable() ;

        if ( nodeTupleTableTriples.getNodeTable() != nodeTupleTableQuads.getNodeTable() )
            throw new CmdException("Different node tables for triples and quads") ;

        NodeTable nodeTable = nodeTupleTableTriples.getNodeTable() ;
        // V special.
        Set<NodeTable> dumpedNodeTables = new HashSet<>() ;



        if ( true )
        {
            System.out.print("## Node Table\n") ;
            dumpNodeTable(nodeTupleTableTriples.getNodeTable(), dumpedNodeTables) ;
            dumpNodeTable(nodeTupleTableQuads.getNodeTable(), dumpedNodeTables) ;
        }

        if ( false )
        {
            System.out.print("## Triple Table\n") ;
            dumpNodeTupleTable(nodeTupleTableTriples.getTupleTable()) ;
            System.out.print("## Quad Table\n") ;
            dumpNodeTupleTable(nodeTupleTableQuads.getTupleTable()) ;
        }

        // Indexes.
        if ( true )
        {
            dumpTupleIndexes(nodeTupleTableTriples.getTupleTable().getIndexes()) ;
            dumpTupleIndexes(nodeTupleTableQuads.getTupleTable().getIndexes()) ;
        }

        // Prefixes
        if ( true )
        {
            System.out.print("## Prefix Table\n") ;  
            DatasetPrefixesTDB prefixes = dsg.getPrefixes() ;

            NodeTupleTable pntt = prefixes.getNodeTupleTable() ;
            if ( ! dumpedNodeTables.contains(pntt.getNodeTable()))
            {
                dumpNodeTable(pntt.getNodeTable(), dumpedNodeTables) ;
                dumpedNodeTables.add(pntt.getNodeTable()) ;
            }
            dumpTupleIndexes(prefixes.getNodeTupleTable().getTupleTable().getIndexes()) ;
        }
    }

    public static void dumpNodeTable(NodeTable nodeTable, Set<NodeTable> dumpedNodeTables)
    {
        if ( dumpedNodeTables.contains(nodeTable) )
            return ;

        Iterator<Pair<NodeId, Node>> iter = nodeTable.all() ;
        for ( ; iter.hasNext() ; )
        {
            Pair<NodeId, Node> pair = iter.next() ;
            NodeId nid = pair.car() ;
            Node n = pair.cdr();
            String x = NodeFmtLib.displayStr(n) ;
            System.out.printf("%016X %s\n", nid.getId(), x) ; 
        }
        dumpedNodeTables.add(nodeTable) ;
    }

    public static void dumpTupleIndexes(TupleIndex[] tupleIndexes)
    {
        for ( TupleIndex tIdx : tupleIndexes )
            dumpTupleIndex(tIdx) ;
    }

    public static void dumpTupleIndex(TupleIndex tIdx)
    {
        System.out.print("## "+tIdx.getMapping()+"\n") ;
        Iterator<Tuple<NodeId>> iter = tIdx.all() ; 
        for ( ; iter.hasNext() ; )
        {
            Tuple<NodeId> t = iter.next() ;
            System.out.print(t) ;
            System.out.print("\n") ;
        }
    }
    
    public static void dumpBlockMgr(PrintStream out, BlockMgr blkMgr)
    {
        try {
            for ( int id = 0 ; id < 9999999 ; id++)
            {
                if ( ! blkMgr.valid(id) ) break ;
                Block blk = blkMgr.getRead(id) ;
                out.print("id="+blk.getId()+"  ") ;
                ByteBufferLib.print(out, blk.getByteBuffer()) ;
            }
        } catch (Exception ex) { 
            ex.printStackTrace() ;
        }
    }
    
    public static void dumpBPlusTree(PrintStream out, BPlusTree bpt)
    {
        IndentedWriter iw = new IndentedWriter(out) ;
        bpt.dump(iw) ;
    }
    
    
    public static void dumpBPlusTreeBlocks(BPlusTree bpt)
    {
        System.out.println("Data blocks");
        DumpOps.dumpBlockMgr(System.out, bpt.getRecordsMgr().getBlockMgr()) ;
        System.out.println("Node blocks");
        DumpOps.dumpBlockMgr(System.out, bpt.getRecordsMgr().getBlockMgr()) ;
    }    


    public static void dumpNodeTupleTable(TupleTable tupleTable)
    {
        int N = tupleTable.getTupleLen() ;
        NodeId[] nodeIds = new NodeId[N] ;
        Arrays.fill(nodeIds, NodeId.NodeIdAny) ;
        
        Tuple<NodeId> t = Tuple.create(nodeIds) ;

        Iterator<Tuple<NodeId>> iter = tupleTable.find(t) ;
        for ( ; iter.hasNext() ; )
        {
            Tuple<NodeId> tuple = iter.next() ;
            System.out.print(tuple) ;
            System.out.print("\n") ;
        }
    }
}
