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

package org.apache.jena.tdb.sys ;

import org.apache.jena.atlas.lib.ColumnMap ;
import org.apache.jena.tdb.TDB ;
import org.apache.jena.tdb.TDBException ;
import org.apache.jena.tdb.base.file.FileFactory ;
import org.apache.jena.tdb.base.file.FileSet ;
import org.apache.jena.tdb.base.file.Location ;
import org.apache.jena.tdb.base.objectfile.ObjectFile ;
import org.apache.jena.tdb.base.record.RecordFactory ;
import org.apache.jena.tdb.index.Index ;
import org.apache.jena.tdb.index.RangeIndex ;
import org.apache.jena.tdb.index.SetupIndex ;
import org.apache.jena.tdb.index.bplustree.BPlusTree ;
import org.apache.jena.tdb.setup.StoreParams ;
import org.apache.jena.tdb.store.NodeId ;
import org.apache.jena.tdb.store.tupletable.TupleIndex ;
import org.apache.jena.tdb.store.tupletable.TupleIndexRecord ;
import org.slf4j.Logger ;

/** Makes things : datasets from locations, indexes, etc etc. */

public class SetupTDB
{
    // Replaced/merge with DatasetBuilderStd mechanisms.
    
    //private static final Logger log = LoggerFactory.getLogger(NewSetup.class) ;
    static final Logger log = TDB.logInfo ;
    public static void error(Logger log, String msg)
    {
        if ( log != null )
            log.error(msg) ;
        throw new TDBException(msg) ;
    }

    private static StoreParams params = StoreParams.getDftStoreParams() ;

//    // And here we make datasets ... 
//    public static DatasetGraphTDB buildDataset(Location location)
//    {
//        return DatasetBuilderStd.build(location) ;
//    }
//
//    //protected static DatasetControl createConcurrencyPolicy() { return new DatasetControlMRSW() ; }
//    
//    public static TripleTable makeTripleTable(Location location, NodeTable nodeTable, String dftPrimary, String[] dftIndexes, DatasetControl policy)
//    {
//        String primary = Names.primaryIndexTriples ;
//        String indexes[] = Names.tripleIndexes ;
//        
//        if ( indexes.length != 3 )
//            error(log, "Wrong number of triple table indexes: "+StrUtils.strjoin(",", indexes)) ;
//        log.debug("Triple table: "+primary+" :: "+StrUtils.strjoin(",", indexes)) ;
//        
//        TupleIndex tripleIndexes[] = makeTupleIndexes(location, primary, indexes, indexes) ;
//        if ( tripleIndexes.length != indexes.length )
//            error(log, "Wrong number of triple table tuples indexes: "+tripleIndexes.length) ;
//        TripleTable tripleTable = new TripleTable(tripleIndexes, nodeTable, policy) ;
//        return tripleTable ;
//    }
//    
//    public static QuadTable makeQuadTable(Location location, NodeTable nodeTable, String dftPrimary, String[] dftIndexes, DatasetControl policy)
//    {
//        String primary = Names.primaryIndexQuads ;
//        String indexes[] = Names.quadIndexes ;
//
//        if ( indexes.length != 6 )
//            error(log, "Wrong number of quad table indexes: "+StrUtils.strjoin(",", indexes)) ;
//        log.debug("Quad table: "+primary+" :: "+StrUtils.strjoin(",", indexes)) ;
//        
//        TupleIndex quadIndexes[] = makeTupleIndexes(location, primary, indexes, indexes) ;
//        if ( quadIndexes.length != indexes.length )
//            error(log, "Wrong number of quad table tuples indexes: "+quadIndexes.length) ;
//        QuadTable quadTable = new QuadTable(quadIndexes, nodeTable, policy) ;
//        return quadTable ;
//    }
//
//
//    public static DatasetPrefixesTDB makePrefixes(Location location, DatasetControl policy)
//    {
//        // The index using for Graph+Prefix => URI
//        String indexPrefixes = params.getIndexPrefix() ;
//        String primary = params.getPrimaryIndexPrefix() ;
//        String indexes[] = params.getPrefixIndexes() ;
//        
//        TupleIndex prefixIndexes[] = makeTupleIndexes(location, primary, indexes, new String[]{indexPrefixes}) ;
//        if ( prefixIndexes.length != indexes.length )
//            error(log, "Wrong number of triple table tuples indexes: "+prefixIndexes.length) ;
//        
//        // The nodetable.
//        String pnNode2Id = params.getPrefixNode2Id() ;
//        String pnId2Node = params.getPrefixId2Node() ;
//        
//        // No cache - the prefix mapping is a cache
//        NodeTable prefixNodes = makeNodeTable(location, pnNode2Id, -1, pnId2Node, -1, -1)  ;
//        NodeTupleTable prefixTable = new NodeTupleTableConcrete(primary.length(),
//                                                                prefixIndexes,
//                                                                prefixNodes, policy) ;
//        DatasetPrefixesTDB prefixes = new DatasetPrefixesTDB(prefixTable) ; 
//        log.debug("Prefixes: "+StrUtils.strjoin(", ", indexes)) ;
//        return prefixes ;
//    }

    public static TupleIndex[] makeTupleIndexes(Location location, String primary, String[] descs, String[] filenames)
    {
        if ( primary.length() != 3 && primary.length() != 4 )
            error(log, "Bad primary key length: "+primary.length()) ;

        int indexRecordLen = primary.length()*NodeId.SIZE ;
        TupleIndex indexes[] = new TupleIndex[descs.length] ;
        for (int i = 0 ; i < indexes.length ; i++)
            indexes[i] = makeTupleIndex(location, primary, descs[i], filenames[i], indexRecordLen) ;
        return indexes ;
    }
    
    public static TupleIndex makeTupleIndex(Location location,
                                            String primary, String indexOrder, String indexName,
                                            int keyLength)
    {
        FileSet fs = new FileSet(location, indexName) ;
        int readCacheSize = params.getBlockReadCacheSize() ;
        int writeCacheSize = params.getBlockWriteCacheSize() ;
        
        // Value part is null (zero length)
        RangeIndex rIndex = SetupIndex.makeRangeIndex(location, indexName, params.getBlockSize(), keyLength, 0, readCacheSize, writeCacheSize) ;
        TupleIndex tupleIndex = new TupleIndexRecord(primary.length(), new ColumnMap(primary, indexOrder), indexOrder, rIndex.getRecordFactory(), rIndex) ;
        return tupleIndex ;
    }
    
    
    public static Index makeIndex(Location location, String indexName,
                                  int blkSize,
                                  int dftKeyLength, int dftValueLength, 
                                  int readCacheSize,int writeCacheSize)
    {
        return SetupIndex.makeIndex(location, indexName, blkSize, dftKeyLength, dftValueLength, readCacheSize, writeCacheSize) ;
    }
    
    public static RangeIndex makeRangeIndex(Location location, String indexName, 
                                            int blkSize,
                                             int dftKeyLength, int dftValueLength,
                                             int readCacheSize,int writeCacheSize)
    {
        return SetupIndex.makeRangeIndex(location, indexName, blkSize, dftKeyLength, dftValueLength, readCacheSize, writeCacheSize) ;
    }
    
    public static RangeIndex makeBPlusTree(FileSet fs, int blkSize,
                                           int readCacheSize, int writeCacheSize,
                                           int dftKeyLength, int dftValueLength)
    {
        return SetupIndex.makeBPlusTree(fs, blkSize, readCacheSize, writeCacheSize, dftKeyLength, dftValueLength) ;
    }

    public static RecordFactory makeRecordFactory(int keyLen, int valueLen)
        {
            return SetupIndex.makeRecordFactory(keyLen, valueLen) ;
        }
    //    
    //    /** Make a NodeTable without cache and inline wrappers */ 
    //    public static NodeTable makeNodeTableBase(Location location, String indexNode2Id, String indexId2Node)
    //    {
    //        if (location.isMem()) 
    //            return NodeTableFactory.createMem() ;
    //
    //        // -- make id to node mapping -- Names.indexId2Node
    //        FileSet fsIdToNode = new FileSet(location, indexId2Node) ;
    //        
    //        ObjectFile stringFile = makeObjectFile(fsIdToNode) ;
    //        
    //        // -- make node to id mapping -- Names.indexNode2Id
    //        // Make index of id to node (data table)
    //        
    //        // No caching at the index level - we use the internal caches of the node table.
    //        Index nodeToId = makeIndex(location, indexNode2Id, LenNodeHash, SizeOfNodeId, -1 ,-1) ;
    //        
    //        // -- Make the node table using the components established above.
    //        NodeTable nodeTable = new NodeTableNative(nodeToId, stringFile) ;
    //        return nodeTable ;
    //    }
    //
    //    /** Make a NodeTable with cache and inline wrappers */ 
    //    public static NodeTable makeNodeTable(Location location)
    //    {
    //        return makeNodeTable(location,
    //                             Names.indexNode2Id, SystemTDB.Node2NodeIdCacheSize,
    //                             Names.indexId2Node, SystemTDB.NodeId2NodeCacheSize,
    //                             SystemTDB.NodeMissCacheSize) ;
    //    }
    //
    //    /** Make a NodeTable with cache and inline wrappers */ 
    //    public static NodeTable makeNodeTable(Location location,
    //                                          String indexNode2Id, int nodeToIdCacheSize,
    //                                          String indexId2Node, int idToNodeCacheSize,
    //                                          int nodeMissCacheSize)
    //    {
    //        NodeTable nodeTable = makeNodeTableBase(location, indexNode2Id, indexId2Node) ;
    //        nodeTable = NodeTableCache.create(nodeTable, nodeToIdCacheSize, idToNodeCacheSize, nodeMissCacheSize) ; 
    //        nodeTable = NodeTableInline.create(nodeTable) ;
    //        return nodeTable ;
    //    }
    //
    
    // XXX Move to FileFactory
    public static ObjectFile makeObjectFile(FileSet fsIdToNode)
    {
        String filename = fsIdToNode.filename(Names.extNodeData) ;
        ObjectFile objFile = FileFactory.createObjectFileDisk(filename);
        return objFile ;
    }

    /** Create a B+Tree using defaults */
    public static RangeIndex createBPTree(FileSet fileset, RecordFactory factory) {
        return SetupIndex.createBPTree(fileset, factory) ;
    }
    
    /** Create a B+Tree by BlockSize */
    public static RangeIndex createBPTreeByBlockSize(FileSet fileset,
                                                     int blockSize,
                                                     int readCacheSize, int writeCacheSize,
                                                     RecordFactory factory) {
        return SetupIndex.createBPTreeByBlockSize(fileset, blockSize, readCacheSize, writeCacheSize, factory) ;
    }
    
    /** Create a B+Tree by Order */
    public static RangeIndex createBPTreeByOrder(FileSet fileset,
                                                 int order,
                                                 int readCacheSize, int writeCacheSize,
                                                 RecordFactory factory) {
        return SetupIndex.createBPTreeByOrder(fileset, order, readCacheSize, writeCacheSize, factory) ;
    }
    

    /** Knowing all the parameters, create a B+Tree */
    public static BPlusTree createBPTree(FileSet fileset, int order, int blockSize,
                                          int readCacheSize, int writeCacheSize,
                                          RecordFactory factory) {
        return SetupIndex.createBPTree(fileset, order, blockSize, readCacheSize, writeCacheSize, factory) ;
    }
}
