/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.setup;

import java.util.HashMap ;
import java.util.Map ;
import java.util.Properties ;

import org.openjena.atlas.lib.ColumnMap ;
import org.openjena.atlas.lib.StrUtils ;
import org.slf4j.Logger ;

import com.hp.hpl.jena.sparql.core.DatasetPrefixStorage ;
import com.hp.hpl.jena.sparql.engine.optimizer.reorder.ReorderTransformation ;
import com.hp.hpl.jena.tdb.TDB ;
import com.hp.hpl.jena.tdb.TDBException ;
import com.hp.hpl.jena.tdb.base.block.BlockMgr ;
import com.hp.hpl.jena.tdb.base.file.FileSet ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.base.file.MetaFile ;
import com.hp.hpl.jena.tdb.base.objectfile.ObjectFile ;
import com.hp.hpl.jena.tdb.index.TupleIndex ;
import com.hp.hpl.jena.tdb.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.store.DatasetPrefixesTDB ;
import com.hp.hpl.jena.tdb.store.NodeId ;
import com.hp.hpl.jena.tdb.store.QuadTable ;
import com.hp.hpl.jena.tdb.store.StoreConfig ;
import com.hp.hpl.jena.tdb.store.TripleTable ;
import com.hp.hpl.jena.tdb.sys.ConcurrencyPolicy ;
import com.hp.hpl.jena.tdb.sys.ConcurrencyPolicyMRSW ;
import com.hp.hpl.jena.tdb.sys.FileRef ;
import com.hp.hpl.jena.tdb.sys.Names ;
import com.hp.hpl.jena.tdb.sys.SetupTDB ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

/** This class is the process of building a dataset. */ 

public class DatasetBuilderStd implements DatasetBuilder
{
    private static final Logger log = TDB.logInfo ;

    private BlockMgrBuilder blockMgrBuilder ;
    private ObjectFileBuilder objectFileBuilder ;

    //private IndexBuilder indexBuilder ;
    private RangeIndexBuilder rangeIndexBuilder ;
    
    private NodeTableBuilder nodeTableBuilder ;
    
    private TupleIndexBuilder tupleIndexBuilder ;
    
    private Properties config ;
    
    private Map<FileRef, BlockMgr> blockMgrs = new HashMap<FileRef, BlockMgr>() ;
    private Map<FileRef, NodeTable> nodeTables = new HashMap<FileRef, NodeTable>() ;

    public static DatasetGraphTDB build(String dir)
    {
        DatasetBuilderStd x = new DatasetBuilderStd() ;
        x.setStd() ;
        return x.build(new Location(dir), null) ;
    }
    
    public static DatasetGraphTDB build()
    {
        DatasetBuilderStd x = new DatasetBuilderStd() ;
        x.setStd() ;
        return x.build(Location.mem(), null) ;
    }
    
    
    protected DatasetBuilderStd()
    {
    }
    
    public DatasetBuilderStd(BlockMgrBuilder blockMgrBuilder,
                             NodeTableBuilder nodeTableBuilder)
    {
        set(blockMgrBuilder, nodeTableBuilder) ;
    }
    
    protected void setAll(NodeTableBuilder nodeTableBuilder,
                       TupleIndexBuilder tupleIndexBuilder,
                       IndexBuilder indexBuilder,
                       RangeIndexBuilder rangeIndexBuilder,
                       BlockMgrBuilder blockMgrBuilder,
                       ObjectFileBuilder objectFileBuilder)
    {
        this.nodeTableBuilder = nodeTableBuilder ;
        this.tupleIndexBuilder = tupleIndexBuilder ;
        //this.indexBuilder = indexBuilder ;
        this.rangeIndexBuilder = rangeIndexBuilder ;
        this.blockMgrBuilder = blockMgrBuilder ;
        this.objectFileBuilder = objectFileBuilder ;
    }
    
    protected void set(NodeTableBuilder nodeTableBuilder,
                       TupleIndexBuilder tupleIndexBuilder,
                       //IndexBuilder indexBuilder,
                       RangeIndexBuilder rangeIndexBuilder)
    {
        this.nodeTableBuilder = nodeTableBuilder ;
        this.tupleIndexBuilder = tupleIndexBuilder ;
        //this.indexBuilder = indexBuilder ;
        this.rangeIndexBuilder = rangeIndexBuilder ;
    }
    
    protected void set(BlockMgrBuilder blockMgrBuilder,
                       NodeTableBuilder nodeTableBuilder)
    {
        Recorder recorder = new Recorder(this) ;
        BlockMgrBuilder blockMgrBuilderRec = new BlockMgrBuilderRecorder(blockMgrBuilder, recorder) ;

        IndexBuilder indexBuilder               = new Builder.IndexBuilderStd(blockMgrBuilderRec, blockMgrBuilderRec) ;
        RangeIndexBuilder rangeIndexBuilder     = new Builder.RangeIndexBuilderStd(blockMgrBuilderRec, blockMgrBuilderRec) ;
        
        this.nodeTableBuilder = nodeTableBuilder ;
        nodeTableBuilder = new NodeTableBuilderRecorder(nodeTableBuilder, recorder) ;
        
        TupleIndexBuilder tupleIndexBuilder     = new Builder.TupleIndexBuilderStd(rangeIndexBuilder) ;
        set(nodeTableBuilder, tupleIndexBuilder, rangeIndexBuilder) ;
    }
        
    protected void setStd()
    {
          ObjectFileBuilder objectFileBuilder     = new Builder.ObjectFileBuilderStd() ;
          BlockMgrBuilder blockMgrBuilder         = new Builder.BlockMgrBuilderStd() ;
          IndexBuilder indexBuilderNT             = new Builder.IndexBuilderStd(blockMgrBuilder, blockMgrBuilder) ;
          NodeTableBuilder nodeTableBuilder       = new Builder.NodeTableBuilderStd(indexBuilderNT, objectFileBuilder) ;
          
          set(blockMgrBuilder, nodeTableBuilder) ;
//        ObjectFileBuilder objectFileBuilder     = new Builder.ObjectFileBuilderStd() ;
//        // Depends on memory/file?
//        BlockMgrBuilder blockMgrBuilder         = new Builder.BlockMgrBuilderStd() ;
//        
//        IndexBuilder indexBuilder               = new Builder.IndexBuilderStd(blockMgrBuilder, blockMgrBuilder) ;
//        RangeIndexBuilder rangeIndexBuilder     = new Builder.RangeIndexBuilderStd(blockMgrBuilder, blockMgrBuilder) ;
//        
//        NodeTableBuilder nodeTableBuilder       = new Builder.NodeTableBuilderStd(indexBuilder, objectFileBuilder) ;
//        TupleIndexBuilder tupleIndexBuilder     = new Builder.TupleIndexBuilderStd(rangeIndexBuilder) ;
//        
//        set(nodeTableBuilder, tupleIndexBuilder, 
//            indexBuilder, rangeIndexBuilder, 
//            blockMgrBuilder, objectFileBuilder) ;
        
    }

    @Override
    synchronized final
    public DatasetGraphTDB build(Location location, Properties config)
    {
        // Ensure that there is global synchronization
        synchronized(DatasetBuilderStd.class)
        {
            return _build(location, config) ;
        }
    }
    
    protected DatasetGraphTDB _build(Location location, Properties config)
    {
        this.config = config ;
        init(location) ;
        
        ConcurrencyPolicy policy = createConcurrencyPolicy() ;
        
        NodeTable nodeTable = makeNodeTable(location, 
                                            params.indexNode2Id, params.indexId2Node,
                                            SystemTDB.Node2NodeIdCacheSize, SystemTDB.NodeId2NodeCacheSize) ;
        
        TripleTable tripleTable = makeTripleTable(location, nodeTable, policy) ; 
        QuadTable quadTable = makeQuadTable(location, nodeTable, policy) ;
        DatasetPrefixStorage prefixes = makePrefixTable(location, policy) ;
        ReorderTransformation transform  = chooseReorderTransformation(location) ;
        
        StoreConfig storeConfig = new StoreConfig(location, config, blockMgrs, nodeTables) ;
        DatasetGraphTDB dsg = new DatasetGraphTDB(tripleTable, quadTable, prefixes, transform, storeConfig) ;
        return dsg ;
    }
    
    protected ConcurrencyPolicy createConcurrencyPolicy() { return new ConcurrencyPolicyMRSW() ; }
    
    protected void init(Location location)
    {
        // Build params.
    }
    
    // ======== Dataset level
    protected TripleTable makeTripleTable(Location location, NodeTable nodeTable, ConcurrencyPolicy policy)
    {    
        MetaFile metafile = location.getMetaFile() ;
        String dftPrimary = params.primaryIndexTriples ;
        String[] dftIndexes = params.tripleIndexes ;
        
        String primary = metafile.getOrSetDefault("tdb.indexes.triples.primary", dftPrimary) ;
        String x = metafile.getOrSetDefault("tdb.indexes.triples", StrUtils.strjoin(",",dftIndexes)) ;
        String indexes[] = x.split(",") ;
        
        if ( indexes.length != 3 )
            error(log, "Wrong number of triple table indexes: "+StrUtils.strjoin(",", indexes)) ;
        log.debug("Triple table: "+primary+" :: "+StrUtils.strjoin(",", indexes)) ;
        
        TupleIndex tripleIndexes[] = makeTupleIndexes(location, primary, indexes) ;
        
        if ( tripleIndexes.length != indexes.length )
            error(log, "Wrong number of triple table tuples indexes: "+tripleIndexes.length) ;
        TripleTable tripleTable = new TripleTable(tripleIndexes, nodeTable, policy) ;
        metafile.flush() ;
        return tripleTable ;
    }
    
    protected QuadTable makeQuadTable(Location location, NodeTable nodeTable, ConcurrencyPolicy policy)
    {    
        MetaFile metafile = location.getMetaFile() ;
        String dftPrimary = params.primaryIndexQuads ;
        String[] dftIndexes = params.quadIndexes ;
        
        String primary = metafile.getOrSetDefault("tdb.indexes.quads.primary", dftPrimary) ;
        String x = metafile.getOrSetDefault("tdb.indexes.quads", StrUtils.strjoin(",",dftIndexes)) ;
        String indexes[] = x.split(",") ;

        if ( indexes.length != 6 )
            error(log, "Wrong number of quad table indexes: "+StrUtils.strjoin(",", indexes)) ;
        
        log.debug("Quad table: "+primary+" :: "+StrUtils.strjoin(",", indexes)) ;
        
        TupleIndex quadIndexes[] = makeTupleIndexes(location, primary, indexes) ;
        if ( quadIndexes.length != indexes.length )
            error(log, "Wrong number of quad table tuples indexes: "+quadIndexes.length) ;
        QuadTable quadTable = new QuadTable(quadIndexes, nodeTable, policy) ;
        metafile.flush() ;
        return quadTable ;
    }

    protected DatasetPrefixStorage makePrefixTable(Location location, ConcurrencyPolicy policy)
    {    
        /*
         * tdb.prefixes.index.file=prefixIdx
         * tdb.prefixes.indexes=GPU
         * tdb.prefixes.primary=GPU
         * 
         * tdb.prefixes.nodetable.mapping.node2id=prefixes
         * tdb.prefixes.nodetable.mapping.id2node=id2prefix
    
         * 
         * Logical:
         * 
         * tdb.prefixes.index.file=prefixIdx
         * tdb.prefixes.index=GPU
         * tdb.prefixes.nodetable.mapping.node2id=prefixes
         * tdb.prefixes.nodetable.mapping.id2node=id2prefix
    
         * 
         * Physical:
         * 
         * It's a node table and an index (rangeindex)
         * 
         */

        // Some of this is also in locationMetadata.
        
        MetaFile metafile = location.getMetaFile() ;
        String dftPrimary = params.primaryIndexPrefix ;
        String[] dftIndexes = params.prefixIndexes ;
        
        // The index using for Graph+Prefix => URI
        String indexPrefixes = metafile.getOrSetDefault("tdb.prefixes.index.file", params.indexPrefix) ;
        String primary = metafile.getOrSetDefault("tdb.prefixes.primary", dftPrimary) ;
        String x = metafile.getOrSetDefault("tdb.prefixes.indexes", StrUtils.strjoin(",",dftIndexes)) ;
        String indexes[] = x.split(",") ;
        
        TupleIndex prefixIndexes[] = makeTupleIndexes(location, primary, indexes, new String[]{indexPrefixes}) ;
        if ( prefixIndexes.length != indexes.length )
            error(log, "Wrong number of triple table tuples indexes: "+prefixIndexes.length) ;
        
        // The nodetable.
        String pnNode2Id = metafile.getOrSetDefault("tdb.prefixes.nodetable.mapping.node2id", params.prefixNode2Id) ;
        String pnId2Node = metafile.getOrSetDefault("tdb.prefixes.nodetable.mapping.id2node", params.prefixId2Node) ;
        
        // No cache - the prefix mapping is a cache
        NodeTable prefixNodes = makeNodeTable(location, pnNode2Id, pnId2Node, -1, -1)  ;
        
        DatasetPrefixesTDB prefixes = new DatasetPrefixesTDB(prefixIndexes, prefixNodes, policy) ; 
        
        log.debug("Prefixes: "+x) ;
        
        return prefixes ;
    }

    
    protected ReorderTransformation chooseReorderTransformation(Location location)
    {    
        return SetupTDB.chooseOptimizer(location) ;
    }

    // ======== Components level
    
//    // This is not actually used in main dataset builder because it's done inside TripleTable/QuadTable.
//    protected NodeTupleTable makeNodeTupleTable(Location location, String primary, String[] indexes, NodeTable nodeTable, ConcurrencyPolicy policy)
//    {    
//        int N = indexes.length ;
//        TupleIndex tripleIndexes[] = makeTupleIndexes(location, primary, indexes) ;
//        if ( tripleIndexes.length != indexes.length )
//            error(log, "Wrong number of node table tuples indexes: expected="+N+" : actual="+tripleIndexes.length) ;
//        NodeTupleTable ntt = new NodeTupleTableConcrete(N, tripleIndexes, nodeTable, policy) ;
//        return ntt ;
//    }
    
    private TupleIndex[] makeTupleIndexes(Location location, String primary, String[] indexNames)
    {
        return makeTupleIndexes(location, primary, indexNames, indexNames) ;
    }
    
    private TupleIndex[] makeTupleIndexes(Location location, String primary, String[] indexNames, String[] filenames)
    {
        if ( primary.length() != 3 && primary.length() != 4 )
            SetupTDB.error(log, "Bad primary key length: "+primary.length()) ;
    
        int indexRecordLen = primary.length()*NodeId.SIZE ;
        TupleIndex indexes[] = new TupleIndex[indexNames.length] ;
        for (int i = 0 ; i < indexes.length ; i++)
            indexes[i] = makeTupleIndex(location, filenames[i], primary, indexNames[i]) ;
        return indexes ;
    }

    // ----
    protected TupleIndex makeTupleIndex(Location location, String name, String primary, String indexOrder)
    {
        // Commonly,  name == indexOrder.
        // FileSet
        FileSet fs = new FileSet(location, name) ;
        ColumnMap colMap = new ColumnMap(primary, indexOrder) ;
        return tupleIndexBuilder.buildTupleIndex(fs, colMap) ;
    }

    // ----
    
    protected NodeTable makeNodeTable(Location location, String indexNode2Id, String indexId2Node, 
                                      int sizeNode2NodeIdCache, int sizeNodeId2NodeCache)
    {
        /* Physical
         * ---- An object file
         * tdb.file.type=object
         * tdb.file.impl=dat
         * tdb.file.impl.version=dat-v1
         *
         * tdb.object.encoding=sse 
         */
        
        FileSet fsNodeToId = new FileSet(location, indexNode2Id) ;
        FileSet fsId2Node = new FileSet(location, indexId2Node) ;
    
        MetaFile metafile = fsId2Node.getMetaFile() ;
        metafile.checkOrSetMetadata("tdb.file.type", ObjectFile.type) ;
        metafile.checkOrSetMetadata("tdb.file.impl", "dat") ;
        metafile.checkOrSetMetadata("tdb.file.impl.version", "dat-v1") ;
        metafile.checkOrSetMetadata("tdb.object.encoding", "sse") ;
        
        NodeTable nt = nodeTableBuilder.buildNodeTable(fsNodeToId, fsId2Node, sizeNode2NodeIdCache, sizeNodeId2NodeCache) ;
        fsNodeToId.getMetaFile().flush() ;
        fsId2Node.getMetaFile().flush() ;
        return nt ;
    }

    private static void error(Logger log, String msg)
    {
        if ( log != null )
            log.error(msg) ;
        throw new TDBException(msg) ;
    }
    
    private static int parseInt(String str, String messageBase)
    {
        try { return Integer.parseInt(str) ; }
        catch (NumberFormatException ex) { error(log, messageBase+": "+str) ; return -1 ; }
    }
    
    private static Params params = new Params() ;
    
    // The standard setting
    private static class Params
    {
        final int      blockSize           = SystemTDB.BlockSize ;
        final int      memBlockSize        = SystemTDB.BlockSizeTestMem ;
        final int      readCacheSize       = SystemTDB.BlockReadCacheSize ;
        final int      writeCacheSize      = SystemTDB.BlockWriteCacheSize ;

        final String   indexNode2Id        = Names.indexNode2Id ;
        final String   indexId2Node        = Names.indexId2Node ;
        final String   primaryIndexTriples = Names.primaryIndexTriples ;
        final String[] tripleIndexes       = Names.tripleIndexes ;
        final String   primaryIndexQuads   = Names.primaryIndexQuads ;
        final String[] quadIndexes         = Names.quadIndexes ;
        final String   primaryIndexPrefix  = Names.primaryIndexPrefix ;
        final String[] prefixIndexes       = Names.prefixIndexes ;
        final String   indexPrefix         = Names.indexPrefix ;

        final String   prefixNode2Id       = Names.prefixNode2Id ;
        final String   prefixId2Node       = Names.prefixId2Node ;
         
    }
    
    interface RecordBlockMgr 
    {
        void record(FileRef fileRef, BlockMgr blockMgr) ;
    }
    
    interface RecordNodeTable 
    {
        void record(FileRef fileRef, NodeTable nodeTable) ;
    }

    static class NodeTableBuilderRecorder implements NodeTableBuilder
    {
        private NodeTableBuilder builder ;
        private RecordNodeTable recorder ;

        NodeTableBuilderRecorder(NodeTableBuilder ntb, RecordNodeTable recorder)
        {
            this.builder = ntb ;
            this.recorder = recorder ;
        }
        
        @Override
        public NodeTable buildNodeTable(FileSet fsIndex, FileSet fsObjectFile, int sizeNode2NodeIdCache,
                                        int sizeNodeId2NodeCache)
        {
            NodeTable nt = builder.buildNodeTable(fsIndex, fsObjectFile, sizeNode2NodeIdCache, sizeNodeId2NodeCache) ;
            // It just knows, right?
            FileRef ref = FileRef.create(fsObjectFile.filename(Names.extNodeData)) ;
            recorder.record(ref, nt) ;
            return nt ;
        }
        
    }
    
    static class BlockMgrBuilderRecorder implements BlockMgrBuilder
    {
        private BlockMgrBuilder builder ;
        private RecordBlockMgr recorder ;

        BlockMgrBuilderRecorder(BlockMgrBuilder blkMgrBuilder, RecordBlockMgr recorder)
        {
            this.builder = blkMgrBuilder ;
            this.recorder = recorder ;
        }
        
        @Override
        public BlockMgr buildBlockMgr(FileSet fileSet, String ext, int blockSize)
        {
            BlockMgr blkMgr = builder.buildBlockMgr(fileSet, ext, blockSize) ;
            FileRef ref = FileRef.create(fileSet, ext) ;
            recorder.record(ref, blkMgr) ;
            return blkMgr ;
        }
    }
    
    static class Recorder implements RecordBlockMgr, RecordNodeTable
    {

        private DatasetBuilderStd dsBuilder ;

        Recorder(DatasetBuilderStd dsBuilder)
        {
            this.dsBuilder = dsBuilder ;
        }
        
        @Override
        public void record(FileRef fileRef, BlockMgr blockMgr)
        {
            //log.info("BlockMgr: "+fileRef) ;
            dsBuilder.blockMgrs.put(fileRef, blockMgr) ;
        }

        @Override
        public void record(FileRef fileRef, NodeTable nodeTable)
        {
            //log.info("NodeTable: "+fileRef) ;
            dsBuilder.nodeTables.put(fileRef, nodeTable) ;
        }
    }
}


/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */