/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package setup;

import static com.hp.hpl.jena.tdb.sys.SystemTDB.SizeOfNodeId ;

import java.util.Properties ;

import org.openjena.atlas.lib.ColumnMap ;
import org.openjena.atlas.lib.StrUtils ;
import org.slf4j.Logger ;

import com.hp.hpl.jena.sparql.core.DatasetPrefixStorage ;
import com.hp.hpl.jena.sparql.engine.optimizer.reorder.ReorderTransformation ;
import com.hp.hpl.jena.tdb.TDB ;
import com.hp.hpl.jena.tdb.TDBException ;
import com.hp.hpl.jena.tdb.base.block.BlockMgr ;
import com.hp.hpl.jena.tdb.base.block.BlockMgrFactory ;
import com.hp.hpl.jena.tdb.base.file.FileFactory ;
import com.hp.hpl.jena.tdb.base.file.FileSet ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.base.file.MetaFile ;
import com.hp.hpl.jena.tdb.base.objectfile.ObjectFile ;
import com.hp.hpl.jena.tdb.base.record.RecordFactory ;
import com.hp.hpl.jena.tdb.index.Index ;
import com.hp.hpl.jena.tdb.index.RangeIndex ;
import com.hp.hpl.jena.tdb.index.TupleIndex ;
import com.hp.hpl.jena.tdb.index.TupleIndexRecord ;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTree ;
import com.hp.hpl.jena.tdb.index.bplustree.BPlusTreeParams ;
import com.hp.hpl.jena.tdb.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.nodetable.NodeTableCache ;
import com.hp.hpl.jena.tdb.nodetable.NodeTableInline ;
import com.hp.hpl.jena.tdb.nodetable.NodeTableNative ;
import com.hp.hpl.jena.tdb.nodetable.NodeTupleTable ;
import com.hp.hpl.jena.tdb.nodetable.NodeTupleTableConcrete ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.store.DatasetPrefixesTDB ;
import com.hp.hpl.jena.tdb.store.NodeId ;
import com.hp.hpl.jena.tdb.store.QuadTable ;
import com.hp.hpl.jena.tdb.store.TripleTable ;
import com.hp.hpl.jena.tdb.sys.ConcurrencyPolicy ;
import com.hp.hpl.jena.tdb.sys.ConcurrencyPolicyMRSW ;
import com.hp.hpl.jena.tdb.sys.Names ;
import com.hp.hpl.jena.tdb.sys.SetupTDB ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

/** This class is the process of building a dataset. */ 

public class DatasetBuilderStd implements DatasetBuilder
{
    private static final Logger log = TDB.logInfo ;

    private BlockMgrBuilder blockMgrBuilder ;
    private ObjectFileBuilder objectFileBuilder ;

    private IndexBuilder indexBuilder ;
    private RangeIndexBuilder rangeIndexBuilder ;
    
    private NodeTableBuilder nodeTableBuilder ;
    
    private TupleIndexBuilder tupleIndexBuilder ;
    
    private Properties config ;
    
    protected DatasetBuilderStd()
    {
    }
    
    protected void set(NodeTableBuilder nodeTableBuilder,
                       //NodeTupleTableBuilder nodeTupleTableBuilder,
                       TupleIndexBuilder tupleIndexBuilder,
                       IndexBuilder indexBuilder,
                       RangeIndexBuilder rangeIndexBuilder,
                       BlockMgrBuilder blockMgrBuilder,
                       ObjectFileBuilder objectFileBuilder)
    {
        this.nodeTableBuilder = nodeTableBuilder ;
        this.tupleIndexBuilder = tupleIndexBuilder ;
        this.indexBuilder = indexBuilder ;
        this.rangeIndexBuilder = rangeIndexBuilder ;
        this.blockMgrBuilder = blockMgrBuilder ;
        this.objectFileBuilder = objectFileBuilder ;
    }
        
    protected void setStd()
    {
        ObjectFileBuilder objectFileBuilder     = new ObjectFileBuilderStd() ;
        // Depends on memory/file?
        BlockMgrBuilder blockMgrBuilder         = new BlockMgrBuilderStd() ;
        
        IndexBuilder indexBuilder               = new IndexBuilderStd(blockMgrBuilder, blockMgrBuilder) ;
        RangeIndexBuilder rangeIndexBuilder     = new RangeIndexBuilderStd(blockMgrBuilder, blockMgrBuilder) ;
        
        NodeTableBuilder nodeTableBuilder       = new NodeTableBuilderStd(indexBuilder, objectFileBuilder) ;
        TupleIndexBuilder tupleIndexBuilder     = new TupleIndexBuilderStd(rangeIndexBuilder) ;
        
        set(nodeTableBuilder, tupleIndexBuilder, 
            indexBuilder, rangeIndexBuilder, 
            blockMgrBuilder, objectFileBuilder) ;
        
    }

    protected DatasetBuilderStd(NodeTableBuilder nodeTableBuilder,
                                TupleIndexBuilder tupleIndexBuilder,
                                IndexBuilder indexBuilder,
                                RangeIndexBuilder rangeIndexBuilder,
                                BlockMgrBuilder blockMgrBuilder,
                                ObjectFileBuilder objectFileBuilder)
    {
        set(nodeTableBuilder, tupleIndexBuilder, 
            indexBuilder, rangeIndexBuilder, 
            blockMgrBuilder, objectFileBuilder) ;
    }
    
    @Override
    public DatasetGraphTDB build(Location location, Properties config)
    {
        this.config = config ;
        init(location) ;
        
        ConcurrencyPolicy policy = createConcurrencyPolicy() ;
        
        NodeTable nodeTable = makeNodeTable(location, 
                                            Names.indexNode2Id, Names.indexId2Node,
                                            SystemTDB.Node2NodeIdCacheSize, SystemTDB.NodeId2NodeCacheSize) ;
        
        TripleTable tripleTable = makeTripleTable(location, nodeTable, policy) ; 
        QuadTable quadTable = makeQuadTable(location, nodeTable, policy) ;
        DatasetPrefixStorage prefixes = makePrefixTable(location, policy) ;
        ReorderTransformation transform  = chooseReorderTransformation(location) ;
        DatasetGraphTDB dsg = new DatasetGraphTDB(tripleTable, quadTable, prefixes, transform, location, config) ;
        return dsg ;
    }
    
    protected ConcurrencyPolicy createConcurrencyPolicy() { return new ConcurrencyPolicyMRSW() ; }
    
    protected void init(Location location)
    {
        // TODO Check this.
    }
    
    // ======== Dataset level
    protected TripleTable makeTripleTable(Location location, NodeTable nodeTable, ConcurrencyPolicy policy)
    {    
        MetaFile metafile = location.getMetaFile() ;
        String dftPrimary = Names.primaryIndexTriples ;
        String[] dftIndexes = Names.tripleIndexes ;
        
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
        String dftPrimary = Names.primaryIndexQuads ;
        String[] dftIndexes = Names.quadIndexes ;
        
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
        String dftPrimary = Names.primaryIndexPrefix ;
        String[] dftIndexes = Names.prefixIndexes ;
        
        // The index using for Graph+Prefix => URI
        String indexPrefixes = metafile.getOrSetDefault("tdb.prefixes.index.file", Names.indexPrefix) ;
        String primary = metafile.getOrSetDefault("tdb.prefixes.primary", dftPrimary) ;
        String x = metafile.getOrSetDefault("tdb.prefixes.indexes", StrUtils.strjoin(",",dftIndexes)) ;
        String indexes[] = x.split(",") ;
        
        TupleIndex prefixIndexes[] = makeTupleIndexes(location, primary, indexes, new String[]{indexPrefixes}) ;
        if ( prefixIndexes.length != indexes.length )
            error(log, "Wrong number of triple table tuples indexes: "+prefixIndexes.length) ;
        
        // The nodetable.
        String pnNode2Id = metafile.getOrSetDefault("tdb.prefixes.nodetable.mapping.node2id", Names.prefixNode2Id) ;
        String pnId2Node = metafile.getOrSetDefault("tdb.prefixes.nodetable.mapping.id2node", Names.prefixId2Node) ;
        
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
    
    // This is not actually used in main dataset builder because it's done inside TripleTable/QuadTable.
    protected NodeTupleTable makeNodeTupleTable(Location location, String primary, String[] indexes, NodeTable nodeTable, ConcurrencyPolicy policy)
    {    
        int N = indexes.length ;
        TupleIndex tripleIndexes[] = makeTupleIndexes(location, primary, indexes) ;
        if ( tripleIndexes.length != indexes.length )
            error(log, "Wrong number of node table tuples indexes: expected="+N+" : actual="+tripleIndexes.length) ;
        NodeTupleTable ntt = new NodeTupleTableConcrete(N, tripleIndexes, nodeTable, policy) ;
        return ntt ;
    }
    
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

    public static class TupleIndexBuilderStd implements TupleIndexBuilder
    {
        private final RangeIndexBuilder rangeIndexBuilder ;

        public TupleIndexBuilderStd(RangeIndexBuilder rangeIndexBuilder)
        {
            this.rangeIndexBuilder = rangeIndexBuilder ;
        }
        
        @Override
        public TupleIndex buildTupleIndex(FileSet fileSet, ColumnMap colMap)
        {
            RecordFactory recordFactory = new RecordFactory(SizeOfNodeId*colMap.length(),0) ;
            
            RangeIndex rIdx = rangeIndexBuilder.buildRangeIndex(fileSet, recordFactory) ;
            TupleIndex tIdx = new TupleIndexRecord(colMap.length(), colMap, recordFactory, rIdx) ;
            return tIdx ;
        }
    }
    
    // ----
    
    public static class NodeTableBuilderStd implements NodeTableBuilder
    {
        private final IndexBuilder indexBuilder ;
        private final ObjectFileBuilder objectFileBuilder ;
        
        public NodeTableBuilderStd(IndexBuilder indexBuilder, ObjectFileBuilder objectFileBuilder)
        { 
            this.indexBuilder = indexBuilder ;
            this.objectFileBuilder = objectFileBuilder ;
        }
        
        @Override
        public NodeTable buildNodeTable(FileSet fsIndex, FileSet fsObjectFile, int sizeNode2NodeIdCache, int sizeNodeId2NodeCache)
        {
            RecordFactory recordFactory = new RecordFactory(SystemTDB.LenNodeHash, SystemTDB.SizeOfNodeId) ;
            Index idx = indexBuilder.buildIndex(fsIndex, recordFactory) ;
            ObjectFile objectFile = objectFileBuilder.buildObjectFile(fsObjectFile, Names.extNodeData) ;
            NodeTable nodeTable = new NodeTableNative(idx, objectFile) ;
            nodeTable = NodeTableCache.create(nodeTable, sizeNode2NodeIdCache, sizeNodeId2NodeCache) ;
            nodeTable = NodeTableInline.create(nodeTable) ;
            return nodeTable ;
        }
    }
    // ----
    
    public static class IndexBuilderStd implements IndexBuilder
    {
        private BlockMgrBuilder bMgr1 ;
        private BlockMgrBuilder bMgr2 ;
        private RangeIndexBuilderStd other ;

        public IndexBuilderStd(BlockMgrBuilder bMgr1, BlockMgrBuilder bMgr2)
        {
            this.bMgr1 = bMgr1 ;
            this.bMgr2 = bMgr2 ;
            this.other = new RangeIndexBuilderStd(bMgr1, bMgr2) ;
        }
        
        @Override
        public Index buildIndex(FileSet fileSet, RecordFactory recordFactory)
        {
            // Cheap.
            return other.buildRangeIndex(fileSet, recordFactory) ;
        }
    }
    
    public static class RangeIndexBuilderStd implements RangeIndexBuilder
    {
        private BlockMgrBuilder bMgr1 ;
        private BlockMgrBuilder bMgr2 ;
        public RangeIndexBuilderStd(BlockMgrBuilder bMgr1, BlockMgrBuilder bMgr2)
        {
            this.bMgr1 = bMgr1 ;
            this.bMgr2 = bMgr2 ;
        }

        @Override
        public RangeIndex buildRangeIndex(FileSet fileSet, RecordFactory recordFactory)
        {
            // ---- BPlusTree based range index.
            // Get parameters.
            /*
             * tdb.bplustree.record=24,0
             * tdb.bplustree.blksize=
             * tdb.bplustree.order=
             */

            // TODO Respect tdb.bplustree.record=24,0 (and so don't need RecordFactory argument).
            // No - Node table resorc size if different.
            
            MetaFile metafile = fileSet.getMetaFile() ;
            //RecordFactory recordFactory = new RecordFactory(keyLength, valueLength) ; // makeRecordFactory(metafile, "tdb.bplustree.record", dftKeyLength, dftValueLength) ;

            String blkSizeStr = metafile.getOrSetDefault("tdb.bplustree.blksize", Integer.toString(SystemTDB.BlockSize)) ; 
            int blkSize = parseInt(blkSizeStr, "Bad block size") ;

            // IndexBuilder.getBPlusTree().newRangeIndex(fs, recordFactory) ;
            // Does not set order.

            int calcOrder = BPlusTreeParams.calcOrder(blkSize, recordFactory.recordLength()) ;
            String orderStr = metafile.getOrSetDefault("tdb.bplustree.order", Integer.toString(calcOrder)) ;
            int order = parseInt(orderStr, "Bad order for B+Tree") ;
            if ( order != calcOrder )
                error(log, "Wrong order (" + order + "), calculated = "+calcOrder) ;

            //        int readCacheSize = PropertyUtils.getPropertyAsInteger(config, Names.pBlockReadCacheSize) ;
            //        int writeCacheSize = PropertyUtils.getPropertyAsInteger(config, Names.pBlockWriteCacheSize) ;

            int readCacheSize = SystemTDB.BlockReadCacheSize ;
            int writeCacheSize = SystemTDB.BlockWriteCacheSize ;

            RangeIndex rIndex = createBPTree(fileSet, order, blkSize, readCacheSize, writeCacheSize, bMgr1, bMgr2, recordFactory) ;
            
            metafile.flush() ;
            return rIndex ;
        }
        
        /** Knowing all the parameters, create a B+Tree */
        private RangeIndex createBPTree(FileSet fileset, int order, 
                                        int blockSize,
                                        int readCacheSize, int writeCacheSize,
                                        BlockMgrBuilder blockMgrBuilder1,
                                        BlockMgrBuilder blockMgrBuilder2,
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
            
//            BlockMgr blkMgrNodes = BlockMgrFactory.create(fileset, Names.bptExt1, blockSize, readCacheSize, writeCacheSize) ;
//            BlockMgr blkMgrRecords = BlockMgrFactory.create(fileset, Names.bptExt2, blockSize, readCacheSize, writeCacheSize) ;
            BlockMgr blkMgrNodes = blockMgrBuilder1.buildBlockMgr(fileset, Names.bptExt1, blockSize) ;
            BlockMgr blkMgrRecords = blockMgrBuilder2.buildBlockMgr(fileset, Names.bptExt2, blockSize) ;
            
            return BPlusTree.attach(params, blkMgrNodes, blkMgrRecords) ;
        }

    }
    
    
    public static class ObjectFileBuilderStd implements ObjectFileBuilder
    {
        @Override
        public ObjectFile buildObjectFile(FileSet fileSet, String ext)
        {
            if ( fileSet.isMem() )
                return FileFactory.createObjectFileMem() ;
            String filename = fileSet.filename(ext) ;
            return FileFactory.createObjectFileDisk(filename) ;
        }
    }
    
    public static class BlockMgrBuilderStd implements BlockMgrBuilder
    {
        public BlockMgrBuilderStd() {}

        @Override
        public BlockMgr buildBlockMgr(FileSet fileset, String ext, int blockSize)
        {
            //int readCacheSize = PropertyUtils.getPropertyAsInteger(config, Names.pBlockReadCacheSize) ;
            //int writeCacheSize = PropertyUtils.getPropertyAsInteger(config, Names.pBlockWriteCacheSize) ;
            
            int readCacheSize = SystemTDB.BlockReadCacheSize ;
            int writeCacheSize = SystemTDB.BlockWriteCacheSize ;
            
            BlockMgr mgr = BlockMgrFactory.create(fileset, ext, blockSize, readCacheSize, writeCacheSize) ;
            return mgr ;
        }
        
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