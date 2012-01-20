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

package com.hp.hpl.jena.tdb.sys ;

import static com.hp.hpl.jena.tdb.sys.SystemTDB.LenNodeHash ;
import static com.hp.hpl.jena.tdb.sys.SystemTDB.SizeOfNodeId ;

import org.openjena.atlas.lib.ColumnMap ;
import org.openjena.atlas.lib.StrUtils ;
import org.slf4j.Logger ;

import com.hp.hpl.jena.tdb.TDB ;
import com.hp.hpl.jena.tdb.TDBException ;
import com.hp.hpl.jena.tdb.base.block.BlockMgr ;
import com.hp.hpl.jena.tdb.base.block.BlockMgrFactory ;
import com.hp.hpl.jena.tdb.base.file.FileFactory ;
import com.hp.hpl.jena.tdb.base.file.FileSet ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.base.objectfile.ObjectFile ;
import com.hp.hpl.jena.tdb.base.record.RecordFactory ;
import com.hp.hpl.jena.tdb.index.* ;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTree ;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTreeParams ;
import com.hp.hpl.jena.tdb.mgt.TDBSystemInfoMBean ;
import com.hp.hpl.jena.tdb.nodetable.* ;
import com.hp.hpl.jena.tdb.setup.DatasetBuilderStd ;
import com.hp.hpl.jena.tdb.setup.SystemParams ;
import com.hp.hpl.jena.tdb.store.* ;

/** Makes things: datasets from locations, indexes */

// Future - this become a collection of statics making things in standard ways. Does not build a dataset. 

public class SetupTDB_Y
{
    //private static final Logger log = LoggerFactory.getLogger(NewSetup.class) ;
    static final Logger log = TDB.logInfo ;
    
    /* Logical information goes in the location metafile. This includes
     * dataset type, NodeTable type and indexes expected.  But it does
     * not include how the particular files are realised.
     * 
     * A NodeTable is a pair of id->Node and Node->id mappings. 
     * 
     * An index file has it's own .meta file saying that it is a B+tree and
     * the record size - everything needed to access it to build a RangeIndex.
     * The individual node table files are the same.  This means we can
     * open a single index or object file (e.g to dump) and it allows
     * for changes both in implementation technology and in overall design. 
     */
    
    // Naming of statics: Maker at a place: X makeX(FileSet, MetaFile?, defaultBlockSize, defaultRecordFactory,
    
    // IndexBuilder for metadata files. 
    
    // Old code:
    // IndexBuilders.  Or add a new IndexBuilder that can make from meta files.
    
    static public final String NodeTableType   = "dat" ; 
    static public final String NodeTableLayout = "1" ;
    
    
    /**  The JVM-wide parameters (these can change without a change to on-disk structure) */ 
//    public final static Properties globalConfig = new Properties() ;
//
//    static {
//        globalConfig.setProperty(Names.pNode2NodeIdCacheSize,  Integer.toString(Node2NodeIdCacheSize)) ;
//        globalConfig.setProperty(Names.pNodeId2NodeCacheSize,  Integer.toString(NodeId2NodeCacheSize)) ;
//        globalConfig.setProperty(Names.pNodeMissesCacheSize,   Integer.toString(NodeMissCacheSize)) ;
//        globalConfig.setProperty(Names.pBlockWriteCacheSize,   Integer.toString(BlockWriteCacheSize)) ;
//        globalConfig.setProperty(Names.pBlockReadCacheSize,    Integer.toString(BlockReadCacheSize)) ;
////        globalConfig.setProperty(Names.pSyncTick,              Integer.toString(SyncTick)) ;
//    }
    
    public static void error(Logger log, String msg)
    {
        if ( log != null )
            log.error(msg) ;
        throw new TDBException(msg) ;
    }

    public static int parseInt(String str, String messageBase)
    {
        try { return Integer.parseInt(str) ; }
        catch (NumberFormatException ex) { error(log, messageBase+": "+str) ; return -1 ; }
    }
    
    
    private static SystemParams params = SystemParams.getStdSystemParams() ;

    
    public final static TDBSystemInfoMBean systemInfo = new TDBSystemInfoMBean() {
		@Override
        public int getSegmentSize()             { return SystemTDB.SegmentSize; }
		@Override
        public int getNodeId2NodeCacheSize()    { return params.NodeId2NodeCacheSize ; }
		@Override
        public int getNode2NodeIdCacheSize()    { return params.Node2NodeIdCacheSize ; }
        @Override
        public int getNodeMissCacheSize()       { return params.NodeMissCacheSize ; }
		@Override
        public int getBlockSize()               { return params.blockSize ; }
		@Override
        public int getBlockReadCacheSize()      { return params.readCacheSize ; }
        @Override
        public int getBlockWriteCacheSize()     { return params.writeCacheSize ; }
	};
	
    // And here we make datasets ... 
    public static DatasetGraphTDB buildDataset(Location location)
    {
        return DatasetBuilderStd.build(location) ;
    }

    //protected static DatasetControl createConcurrencyPolicy() { return new DatasetControlMRSW() ; }
    
    public static TripleTable makeTripleTable(Location location, NodeTable nodeTable, String dftPrimary, String[] dftIndexes, DatasetControl policy)
    {
        String primary = Names.primaryIndexTriples ;
        String indexes[] = Names.tripleIndexes ;
        
        if ( indexes.length != 3 )
            error(log, "Wrong number of triple table indexes: "+StrUtils.strjoin(",", indexes)) ;
        log.debug("Triple table: "+primary+" :: "+StrUtils.strjoin(",", indexes)) ;
        
        TupleIndex tripleIndexes[] = makeTupleIndexes(location, primary, indexes, indexes) ;
        if ( tripleIndexes.length != indexes.length )
            error(log, "Wrong number of triple table tuples indexes: "+tripleIndexes.length) ;
        TripleTable tripleTable = new TripleTable(tripleIndexes, nodeTable, policy) ;
        return tripleTable ;
    }
    
    public static QuadTable makeQuadTable(Location location, NodeTable nodeTable, String dftPrimary, String[] dftIndexes, DatasetControl policy)
    {
        String primary = Names.primaryIndexQuads ;
        String indexes[] = Names.quadIndexes ;

        if ( indexes.length != 6 )
            error(log, "Wrong number of quad table indexes: "+StrUtils.strjoin(",", indexes)) ;
        log.debug("Quad table: "+primary+" :: "+StrUtils.strjoin(",", indexes)) ;
        
        TupleIndex quadIndexes[] = makeTupleIndexes(location, primary, indexes, indexes) ;
        if ( quadIndexes.length != indexes.length )
            error(log, "Wrong number of quad table tuples indexes: "+quadIndexes.length) ;
        QuadTable quadTable = new QuadTable(quadIndexes, nodeTable, policy) ;
        return quadTable ;
    }


    public static DatasetPrefixesTDB makePrefixes(Location location, DatasetControl policy)
    {
        // The index using for Graph+Prefix => URI
        String indexPrefixes = params.indexPrefix ;
        String primary = params.primaryIndexPrefix ;
        String indexes[] = params.prefixIndexes ;
        
        TupleIndex prefixIndexes[] = makeTupleIndexes(location, primary, indexes, new String[]{indexPrefixes}) ;
        if ( prefixIndexes.length != indexes.length )
            error(log, "Wrong number of triple table tuples indexes: "+prefixIndexes.length) ;
        
        // The nodetable.
        String pnNode2Id = params.prefixNode2Id ;
        String pnId2Node = params.prefixId2Node ;
        
        // No cache - the prefix mapping is a cache
        NodeTable prefixNodes = makeNodeTable(location, pnNode2Id, -1, pnId2Node, -1, -1)  ;
        
        DatasetPrefixesTDB prefixes = new DatasetPrefixesTDB(prefixIndexes, prefixNodes, policy) ; 
        
        log.debug("Prefixes: "+StrUtils.strjoin(", ", indexes)) ;
        
        return prefixes ;
    }

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
        int readCacheSize = params.readCacheSize ;
        int writeCacheSize = params.writeCacheSize ;
        
        // Value part is null (zero length)
        RangeIndex rIndex = makeRangeIndex(location, indexName, keyLength, 0, readCacheSize, writeCacheSize) ;
        TupleIndex tupleIndex = new TupleIndexRecord(primary.length(), new ColumnMap(primary, indexOrder), rIndex.getRecordFactory(), rIndex) ;
        return tupleIndex ;
    }
    
    public static Index makeIndex(Location location, String indexName, 
                                  int dftKeyLength, int dftValueLength, 
                                  int readCacheSize,int writeCacheSize)
    {
        return makeRangeIndex(location, indexName, dftKeyLength, dftValueLength, readCacheSize, writeCacheSize) ;
    }
    
    public static RangeIndex makeRangeIndex(Location location, String indexName, 
                                             int dftKeyLength, int dftValueLength,
                                             int readCacheSize,int writeCacheSize)
    {
         FileSet fs = new FileSet(location, indexName) ;
         RangeIndex rIndex =  makeBPlusTree(fs, readCacheSize, writeCacheSize, dftKeyLength, dftValueLength) ;
         return rIndex ;
    }
    
    public static RangeIndex makeBPlusTree(FileSet fs, 
                                           int readCacheSize, int writeCacheSize,
                                           int dftKeyLength, int dftValueLength)
    {
        RecordFactory recordFactory = makeRecordFactory(dftKeyLength, dftValueLength) ;
        int blkSize = params.blockSize ;
        
        // IndexBuilder.getBPlusTree().newRangeIndex(fs, recordFactory) ;
        // Does not set order.
        
        int order = BPlusTreeParams.calcOrder(blkSize, recordFactory.recordLength()) ;
        RangeIndex rIndex = createBPTree(fs, order, blkSize, readCacheSize, writeCacheSize, recordFactory) ;
        return rIndex ;
    }

    public static RecordFactory makeRecordFactory(int keyLen, int valueLen)
    {
        return new RecordFactory(keyLen, valueLen) ;
    }
    
    /** Make a NodeTable without cache and inline wrappers */ 
    public static NodeTable makeNodeTableBase(Location location, String indexNode2Id, String indexId2Node)
    {
        if (location.isMem()) 
            return NodeTableFactory.createMem(IndexBuilder.mem()) ;

        /* Logical:
         * # Node table.
         * tdb.nodetable.mapping.node2id=node2id
         * tdb.nodetable.mapping.id2node=id2node
         * 
         * Physical:
         * 1- Index file for node2id
         * 2- Cached direct lookup object file for id2node
         *    Encoding. 
         */   
        
        String nodeTableType = location.getMetaFile().getProperty(Names.kNodeTableType) ;

        if (nodeTableType != null)
        {
            if ( ! nodeTableType.equals(NodeTableType))
                log.debug("Explicit node table type: " + nodeTableType + " (ignored)") ;
        }
        else
        {
            location.getMetaFile().setProperty(Names.kNodeTableType, NodeTableType) ;
            location.getMetaFile().setProperty(Names.kNodeTableLayout, NodeTableLayout) ;
        }
        
        // -- make id to node mapping -- Names.indexId2Node
        FileSet fsIdToNode = new FileSet(location, indexId2Node) ;
        //checkMetadata(fsIdToNode.getMetaFile(), /*Names.kNodeTableType,*/ NodeTable.type) ; 
        
        ObjectFile stringFile = makeObjectFile(fsIdToNode) ;
        
        // -- make node to id mapping -- Names.indexNode2Id
        // Make index of id to node (data table)
        
        // No caching at the index level - we use the internal caches of the node table.
        Index nodeToId = makeIndex(location, indexNode2Id, LenNodeHash, SizeOfNodeId, -1 ,-1) ;
        
        // -- Make the node table using the components established above.
        NodeTable nodeTable = new NodeTableNative(nodeToId, stringFile) ;
        return nodeTable ;
    }

    /** Make a NodeTable with cache and inline wrappers */ 
    public static NodeTable makeNodeTable(Location location)
    {
        return makeNodeTable(location,
                             Names.indexNode2Id, SystemTDB.Node2NodeIdCacheSize,
                             Names.indexId2Node, SystemTDB.NodeId2NodeCacheSize,
                             SystemTDB.NodeMissCacheSize) ;
    }

    /** Make a NodeTable with cache and inline wrappers */ 
    public static NodeTable makeNodeTable(Location location,
                                          String indexNode2Id, int nodeToIdCacheSize,
                                          String indexId2Node, int idToNodeCacheSize,
                                          int nodeMissCacheSize)
    {
        NodeTable nodeTable = makeNodeTableBase(location, indexNode2Id, indexId2Node) ;
        nodeTable = NodeTableCache.create(nodeTable, nodeToIdCacheSize, idToNodeCacheSize, nodeMissCacheSize) ; 
        nodeTable = NodeTableInline.create(nodeTable) ;
        return nodeTable ;
    }

    public static ObjectFile makeObjectFile(FileSet fsIdToNode)
    {
        String filename = fsIdToNode.filename(Names.extNodeData) ;
        ObjectFile objFile = FileFactory.createObjectFileDisk(filename);
        return objFile ;
    }

    //    /** Check and set default for the dataset design */
//    public static MetaFile locationMetadata(Location location)
//    {
//        boolean newDataset = location.isMem() || ! FileOps.existsAnyFiles(location.getDirectoryPath()) ; 
//
//        MetaFile metafile = location.getMetaFile() ;
//        boolean isPreMetadata = false ;
//        
//        if (!newDataset && metafile.existsMetaData())
//        {
//            // Existing metadata
//            String verString = metafile.getProperty("tdb.create.version", "unknown") ;
//            TDB.logInfo.debug("Location: " + location.toString()) ;
//            TDB.logInfo.debug("Version:  " + verString) ;
//        }
//        else
//        {
//            // Not new ?, no metadata
//            // Either it's brand new (so set the defaults)
//            // or it's a pre-0.9 dataset (files exists)
//
//            if ( ! newDataset )
//            {
//                // Well-known name of the primary triples index.
//                isPreMetadata = FileOps.exists(location.getPath("SPO.idn")) ;
//                // PROBLEM.
////                boolean b = FileOps.exists(location.getPath("SPO.idn")) ;
////                if ( !b )
////                {
////                    log.error("Existing files but no metadata and not old-style fixed layout: "+location.getDirectoryPath()) ;
////                    File d = new File(location.getDirectoryPath()) ;
////                    File[] entries = d.listFiles() ;
////                    for ( File f : d.listFiles()  )
////                        log.error("File: "+f.getName()) ;
////                    throw new TDBException("Can't build dataset: "+location) ;
////                }
////                isPreMetadata = true ;
//            }
//        }
//            
//        // Ensure defaults.
//        
//        if ( newDataset )
//        {
//            metafile.ensurePropertySet("tdb.create.version", TDB.VERSION) ;
//            metafile.ensurePropertySet("tdb.created", Utils.nowAsXSDDateTimeString()) ;
//        }
//        
//        if ( isPreMetadata )
//        {
//            // Existing location (has some files in it) but no metadata.
//            // Fake it as TDB 0.8.1 (which did not have metafiles)
//            // If it's the wrong file format, things do badly wrong later.
//            metafile.ensurePropertySet("tdb.create.version", "0.8") ;
//            metafile.setProperty(Names.kCreatedDate, Utils.nowAsXSDDateTimeString()) ;
//        }
//            
//        metafile.ensurePropertySet("tdb.layout", "v1") ;
//        metafile.ensurePropertySet("tdb.type", "standalone") ;
//        
//        String layout = metafile.getProperty("tdb.layout") ;
//        
//        if ( layout.equals("v1") )
//        {
//            metafile.ensurePropertySet("tdb.indexes.triples.primary", Names.primaryIndexTriples) ;
//            metafile.ensurePropertySet("tdb.indexes.triples", StrUtils.strjoin(",", Names.tripleIndexes)) ;
//
//            metafile.ensurePropertySet("tdb.indexes.quads.primary", Names.primaryIndexQuads) ;
//            metafile.ensurePropertySet("tdb.indexes.quads", StrUtils.strjoin(",", Names.quadIndexes)) ;
//            
//            metafile.ensurePropertySet("tdb.nodetable.mapping.node2id", Names.indexNode2Id) ;
//            metafile.ensurePropertySet("tdb.nodetable.mapping.id2node", Names.indexId2Node) ;
//            
//            metafile.ensurePropertySet("tdb.prefixes.index.file", Names.indexPrefix) ;
//            metafile.ensurePropertySet("tdb.prefixes.nodetable.mapping.node2id", Names.prefixNode2Id) ;
//            metafile.ensurePropertySet("tdb.prefixes.nodetable.mapping.id2node", Names.prefixId2Node) ;
//            
//        }
//        else
//            SetupTDB_Y.error(log, "tdb.layout: expected v1") ;
//            
//        
//        metafile.flush() ;
//        return metafile ; 
//    }
//
////    public static Index createIndex(FileSet fileset, RecordFactory recordFactory)
////    {
////        return chooseIndexBuilder(fileset).newIndex(fileset, recordFactory) ;
////    }
////    
////    public static RangeIndex createRangeIndex(FileSet fileset, RecordFactory recordFactory)
////    {
////        // Block size control?
////        return chooseIndexBuilder(fileset).newRangeIndex(fileset, recordFactory) ;
////    }
//    
    /** Create a B+Tree using defaults */
    public static RangeIndex createBPTree(FileSet fileset,
                                          RecordFactory factory)
    {
        int readCacheSize = SystemTDB.BlockReadCacheSize ;
        int writeCacheSize = SystemTDB.BlockWriteCacheSize ;
        int blockSize = SystemTDB.BlockSize ;
        if ( fileset.isMem() )
        {
            readCacheSize = 0 ;
            writeCacheSize = 0 ;
            blockSize = SystemTDB.BlockSizeTest ;
        }
        
        return createBPTreeByBlockSize(fileset, blockSize, readCacheSize, writeCacheSize, factory) ; 
    }
    
    /** Create a B+Tree by BlockSize */
    public static RangeIndex createBPTreeByBlockSize(FileSet fileset,
                                                     int blockSize,
                                                     int readCacheSize, int writeCacheSize,
                                                     RecordFactory factory)
    {
        return createBPTree(fileset, -1, blockSize, readCacheSize, writeCacheSize, factory) ; 
    }
    
    /** Create a B+Tree by Order */
    public static RangeIndex createBPTreeByOrder(FileSet fileset,
                                                 int order,
                                                 int readCacheSize, int writeCacheSize,
                                                 RecordFactory factory)
    {
        return createBPTree(fileset, order, -1, readCacheSize, writeCacheSize, factory) ; 
    }
    

    /** Knowing all the parameters, create a B+Tree */
    public static BPlusTree createBPTree(FileSet fileset, int order, int blockSize,
                                          int readCacheSize, int writeCacheSize,
                                          RecordFactory factory)
    {
        // ---- Checking
        if (blockSize < 0 && order < 0) throw new IllegalArgumentException("Neither blocksize nor order specified") ;
        if (blockSize >= 0 && order < 0) order = BPlusTreeParams.calcOrder(blockSize, factory.recordLength()) ;
        if (blockSize >= 0 && order >= 0)
        {
            int order2 = BPlusTreeParams.calcOrder(blockSize, factory.recordLength()) ;
            if (order != order2) throw new IllegalArgumentException("Wrong order (" + order + "), calculated = "
                                                                    + order2) ;
        }
    
        // Iffy - does not allow for slop.
        if (blockSize < 0 && order >= 0)
        {
            // Only in-memory.
            blockSize = BPlusTreeParams.calcBlockSize(order, factory) ;
        }
    
        BPlusTreeParams params = new BPlusTreeParams(order, factory) ;
        BlockMgr blkMgrNodes = BlockMgrFactory.create(fileset, Names.bptExtTree, blockSize, readCacheSize, writeCacheSize) ;
        BlockMgr blkMgrRecords = BlockMgrFactory.create(fileset, Names.bptExtRecords, blockSize, readCacheSize, writeCacheSize) ;
        return BPlusTree.create(params, blkMgrNodes, blkMgrRecords) ;
    }
}
