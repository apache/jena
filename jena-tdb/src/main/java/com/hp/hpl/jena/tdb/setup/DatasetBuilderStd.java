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

package com.hp.hpl.jena.tdb.setup ;

import java.io.File ;
import java.io.IOException ;
import java.util.HashMap ;
import java.util.Map ;
import java.util.Properties ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.lib.ColumnMap ;
import org.apache.jena.atlas.lib.FileOps ;
import org.apache.jena.atlas.lib.PropertyUtils ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.slf4j.Logger ;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.sparql.engine.main.QC ;
import com.hp.hpl.jena.sparql.engine.optimizer.reorder.ReorderLib ;
import com.hp.hpl.jena.sparql.engine.optimizer.reorder.ReorderTransformation ;
import com.hp.hpl.jena.sparql.sse.SSEParseException ;
import com.hp.hpl.jena.tdb.TDB ;
import com.hp.hpl.jena.tdb.TDBException ;
import com.hp.hpl.jena.tdb.base.block.BlockMgr ;
import com.hp.hpl.jena.tdb.base.file.BufferChannel ;
import com.hp.hpl.jena.tdb.base.file.FileSet ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.index.IndexParams ;
import com.hp.hpl.jena.tdb.solver.OpExecutorTDB1 ;
import com.hp.hpl.jena.tdb.store.* ;
import com.hp.hpl.jena.tdb.store.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.store.nodetupletable.NodeTupleTable ;
import com.hp.hpl.jena.tdb.store.nodetupletable.NodeTupleTableConcrete ;
import com.hp.hpl.jena.tdb.store.tupletable.TupleIndex ;
import com.hp.hpl.jena.tdb.sys.* ;

/**
 * This class is the process of building a dataset. Records
 * BlockMgr/BufferChannel/NodeTable for use by the transaction builder.
 */

public class DatasetBuilderStd implements DatasetBuilder {
    private static final Logger         log            = TDB.logInfo ;

    private NodeTableBuilder            nodeTableBuilder ;
    private TupleIndexBuilder           tupleIndexBuilder ;

    // XXX Should not be an object field. 
    //private SystemParams                params ;

    private Map<FileRef, BlockMgr>      blockMgrs      = new HashMap<>() ;
    private Map<FileRef, BufferChannel> bufferChannels = new HashMap<>() ;
    private Map<FileRef, NodeTable>     nodeTables     = new HashMap<>() ;

    public static DatasetGraphTDB create(Location location) {
        SystemParams params = paramsForLocation(location) ;
        DatasetBuilderStd x = new DatasetBuilderStd() ;
        x.standardSetup() ;
        return x.build(location, params) ;
    }

    public static DatasetGraphTDB create() {
        return create(Location.mem()) ;
    }

    public static DatasetBuilderStd stdBuilder() {
        DatasetBuilderStd x = new DatasetBuilderStd() ;
        x.standardSetup() ;
        return x ;
    }

    protected DatasetBuilderStd() { }

    // Used by DatasetBuilderTxn
    public DatasetBuilderStd(BlockMgrBuilder blockMgrBuilder, NodeTableBuilder nodeTableBuilder) {
        set(blockMgrBuilder, nodeTableBuilder) ;
    }

    protected void set(NodeTableBuilder nodeTableBuilder, TupleIndexBuilder tupleIndexBuilder) {
        this.nodeTableBuilder = nodeTableBuilder ;
        this.tupleIndexBuilder = tupleIndexBuilder ;
    }

    protected void set(BlockMgrBuilder blockMgrBuilder, NodeTableBuilder nodeTableBuilder) {
        Recorder recorder = new Recorder(this) ;
        BlockMgrBuilder blockMgrBuilderRec = new BlockMgrBuilderRecorder(blockMgrBuilder, recorder) ;

        IndexBuilder indexBuilder = new BuilderIndex.IndexBuilderStd(blockMgrBuilderRec, blockMgrBuilderRec) ;
        RangeIndexBuilder rangeIndexBuilder = new BuilderIndex.RangeIndexBuilderStd(blockMgrBuilderRec, blockMgrBuilderRec) ;

        this.nodeTableBuilder = nodeTableBuilder ;
        nodeTableBuilder = new NodeTableBuilderRecorder(nodeTableBuilder, recorder) ;

        TupleIndexBuilder tupleIndexBuilder = new BuilderDB.TupleIndexBuilderStd(rangeIndexBuilder) ;
        set(nodeTableBuilder, tupleIndexBuilder) ;
    }

    private static SystemParams paramsForLocation(Location location) {
        if ( location.exists(DB_CONFIG_FILE) ) {
            log.debug("Existing configuration file found") ;
            Properties properties = new Properties() ;
            try { 
                PropertyUtils.loadFromFile(properties, DB_CONFIG_FILE) ;
            } catch (IOException ex) { IO.exception(ex) ; throw new TDBException("Bad configuration file", ex) ; }
        }
        return SystemParams.getDftSystemParams() ;
    }

//    private void checkIfConfig(Location location) {
//    }

    private void checkIfNew(Location location) {
        if ( location.isMem() ) {
            return ;
        }
        
        if ( FileOps.existsAnyFiles(location.getDirectoryPath()) ) {
            
        }

        if ( location.exists(DB_CONFIG_FILE) ) {
            log.debug("Existing config file") ;
            return ;
        }
        
    }
    
    private void checkConfiguration() { } 
    
    private static void checkLocation(Location location) { 
        if ( location.isMem() )
            return ;
        String dirname = location.getDirectoryPath() ;
        File dir = new File(dirname) ;
        // File location.
        if ( ! dir.exists() )
            error(log, "Does not exist: "+dirname) ;
        if ( ! dir.isDirectory() )
            error(log, "Not a directory: "+dirname) ;
        if ( ! dir.canRead() )
            error(log, "Directory not readable: "+dirname) ;
        if ( ! dir.canWrite() )
            error(log, "Directory not writeable: "+dirname) ;
    }

    private void standardSetup() {
        ObjectFileBuilder objectFileBuilder = new BuilderIndex.ObjectFileBuilderStd() ;
        BlockMgrBuilder blockMgrBuilder = new BuilderIndex.BlockMgrBuilderStd() ;
        IndexBuilder indexBuilderNT = new BuilderIndex.IndexBuilderStd(blockMgrBuilder, blockMgrBuilder) ;
        NodeTableBuilder nodeTableBuilder = new BuilderDB.NodeTableBuilderStd(indexBuilderNT, objectFileBuilder) ;

        set(blockMgrBuilder, nodeTableBuilder) ;
    }

    @Override
    public DatasetGraphTDB build(Location location, SystemParams params) {
        // Ensure that there is global synchronization
        synchronized (DatasetBuilderStd.class) {
            log.debug("Build database: "+location.getDirectoryPath()) ;
            checkIfNew(location) ;
            checkLocation(location) ;
            return _build(location, params, true, null) ;
        }
    }

    private static String DB_CONFIG_FILE = "tdb.cfg" ; 
    
    // Main engine for building.
    // Called by DatasetBuilderTxn
    // XXX Rework - provide a cloning constructor (copies maps).
    // Or "reset"
    public DatasetGraphTDB _build(Location location, SystemParams params, boolean writeable, ReorderTransformation _transform) {
        init(location) ;
        return buildWorker(location, writeable, _transform, params) ;
    }
    
    private DatasetGraphTDB buildWorker(Location location, boolean writeable, ReorderTransformation _transform, SystemParams params) {
        DatasetControl policy = createConcurrencyPolicy() ;
        NodeTable nodeTable = makeNodeTable(location, params) ;
        TripleTable tripleTable = makeTripleTable(location, nodeTable, policy, params) ;
        QuadTable quadTable = makeQuadTable(location, nodeTable, policy, params) ;
        DatasetPrefixesTDB prefixes = makePrefixTable(location, policy, params) ;

        ReorderTransformation transform = (_transform == null) ? chooseReorderTransformation(location) : _transform ;

        StorageConfig storageConfig = new StorageConfig(location, params, writeable, blockMgrs, bufferChannels,
                                                        nodeTables) ;
        DatasetGraphTDB dsg = new DatasetGraphTDB(tripleTable, quadTable, prefixes, transform, storageConfig) ;
        // TDB does filter placement on BGPs itself.
        dsg.getContext().set(ARQ.optFilterPlacementBGP, false) ;
        QC.setFactory(dsg.getContext(), OpExecutorTDB1.OpExecFactoryTDB) ;
        return dsg ;
    }

    protected DatasetControl createConcurrencyPolicy() {
        return new DatasetControlMRSW() ;
    }

    protected void init(Location location) {
    }

    protected TripleTable makeTripleTable(Location location, NodeTable nodeTable, DatasetControl policy, SystemParams params) {
        String primary = params.getPrimaryIndexTriples() ;
        String[] indexes = params.getTripleIndexes() ;

        // Allow experimentation of other index layouts.
        // if ( indexes.length != 3 )
        // error(log,
        // "Wrong number of triple table indexes: "+StrUtils.strjoin(",",
        // indexes)) ;
        log.debug("Triple table: " + primary + " :: " + StrUtils.strjoin(",", indexes)) ;

        TupleIndex tripleIndexes[] = makeTupleIndexes(location, primary, indexes, params) ;

        if ( tripleIndexes.length != indexes.length )
            error(log, "Wrong number of triple table tuples indexes: " + tripleIndexes.length) ;
        TripleTable tripleTable = new TripleTable(tripleIndexes, nodeTable, policy) ;
        return tripleTable ;
    }

    protected QuadTable makeQuadTable(Location location, NodeTable nodeTable, DatasetControl policy, SystemParams params) {
        String primary = params.getPrimaryIndexQuads() ;
        String[] indexes = params.getQuadIndexes() ;

        // Allow experimentation of other index layouts.
        // if ( indexes.length != 6 )
        // error(log,
        // "Wrong number of quad table indexes: "+StrUtils.strjoin(",",
        // indexes)) ;

        log.debug("Quad table: " + primary + " :: " + StrUtils.strjoin(",", indexes)) ;

        TupleIndex quadIndexes[] = makeTupleIndexes(location, primary, indexes, params) ;
        if ( quadIndexes.length != indexes.length )
            error(log, "Wrong number of quad table tuples indexes: " + quadIndexes.length) ;
        QuadTable quadTable = new QuadTable(quadIndexes, nodeTable, policy) ;
        return quadTable ;
    }

    protected DatasetPrefixesTDB makePrefixTable(Location location, DatasetControl policy, SystemParams params) {
        String primary = params.getPrimaryIndexPrefix() ;
        String[] indexes = params.getPrefixIndexes() ;

        TupleIndex prefixIndexes[] = makeTupleIndexes(location, primary, indexes, new String[]{params.getIndexPrefix()}, params) ;
        if ( prefixIndexes.length != 1 )
            error(log, "Wrong number of triple table tuples indexes: " + prefixIndexes.length) ;

        String pnNode2Id = params.getPrefixNode2Id() ;
        String pnId2Node = params.getPrefixId2Node() ;

        
        
        // No cache - the prefix mapping is a cache
        NodeTable prefixNodes = makeNodeTableNoCache(location, pnNode2Id, pnId2Node, params) ;
        NodeTupleTable prefixTable = new NodeTupleTableConcrete(primary.length(),
                                                                prefixIndexes,
                                                                prefixNodes, policy) ;
        DatasetPrefixesTDB prefixes = new DatasetPrefixesTDB(prefixTable) ;

        log.debug("Prefixes: " + primary + " :: " + StrUtils.strjoin(",", indexes)) ;

        return prefixes ;
    }

    protected ReorderTransformation chooseReorderTransformation(Location location) {
        return chooseOptimizer(location) ;
    }

    private TupleIndex[] makeTupleIndexes(Location location, String primary, String[] indexNames, SystemParams params) {
        return makeTupleIndexes(location, primary, indexNames, indexNames, params) ;
    }
    
    private TupleIndex[] makeTupleIndexes(Location location, String primary, String[] indexNames, String[] filenames, SystemParams params) {
        if ( primary.length() != 3 && primary.length() != 4 )
            error(log, "Bad primary key length: " + primary.length()) ;

        int indexRecordLen = primary.length() * NodeId.SIZE ;
        TupleIndex indexes[] = new TupleIndex[indexNames.length] ;
        for ( int i = 0 ; i < indexes.length ; i++ )
            indexes[i] = makeTupleIndex(location, filenames[i], primary, indexNames[i], params) ;
        return indexes ;
    }

    protected TupleIndex makeTupleIndex(Location location, String name, String primary, String indexOrder, SystemParams params) {
        // Commonly, name == indexOrder.
        FileSet fs = new FileSet(location, name) ;
        ColumnMap colMap = new ColumnMap(primary, indexOrder) ;
        return tupleIndexBuilder.buildTupleIndex(fs, colMap, indexOrder, params) ;
    }

    public NodeTable makeNodeTable(Location location, SystemParams params) {
        FileSet fsNodeToId = new FileSet(location, params.getIndexNode2Id()) ;
        FileSet fsId2Node = new FileSet(location, params.getIndexId2Node()) ;
        NodeTable nt = nodeTableBuilder.buildNodeTable(fsNodeToId, fsId2Node, params) ;
        return nt ;
    }
    
    /** Make a node table overriding the node->id and id->node table names */ 
    private NodeTable makeNodeTable$(Location location, String indexNode2Id, String indexId2Node, SystemParams params) {
        FileSet fsNodeToId = new FileSet(location, indexNode2Id) ;
        FileSet fsId2Node = new FileSet(location, indexId2Node) ;
        NodeTable nt = nodeTableBuilder.buildNodeTable(fsNodeToId, fsId2Node, params) ;
        return nt ;
    }
    
    protected NodeTable makeNodeTableNoCache(Location location, String indexNode2Id, String indexId2Node, SystemParams params) {
        SystemParamsBuilder spb = new SystemParamsBuilder(params) ;
        spb.node2NodeIdCacheSize(-1) ;
        spb.nodeId2NodeCacheSize(-1) ;
        spb.nodeMissCacheSize(-1) ;
        return makeNodeTable$(location, indexNode2Id, indexId2Node, spb.build()) ;
    }
    
    private static void error(Logger log, String msg) {
        if ( log != null )
            log.error(msg) ;
        throw new TDBException(msg) ;
    }

    private static int parseInt(String str, String messageBase) {
        try {
            return Integer.parseInt(str) ;
        }
        catch (NumberFormatException ex) {
            error(log, messageBase + ": " + str) ;
            return -1 ;
        }
    }

    /**
     * Set the global flag that control the "No BGP optimizer" warning. Set to
     * false to silence the warning
     */
    public static void setOptimizerWarningFlag(boolean b) {
        warnAboutOptimizer = b ;
    }
    private static boolean warnAboutOptimizer = true ;

    public static ReorderTransformation chooseOptimizer(Location location) {
        if ( location == null )
            return ReorderLib.identity() ;

        ReorderTransformation reorder = null ;
        if ( location.exists(Names.optStats) ) {
            try {
                reorder = ReorderLib.weighted(location.getPath(Names.optStats)) ;
                log.debug("Statistics-based BGP optimizer") ;
            }
            catch (SSEParseException ex) {
                log.warn("Error in stats file: " + ex.getMessage()) ;
                reorder = null ;
            }
        }

        if ( reorder == null && location.exists(Names.optFixed) ) {
            // Not as good but better than nothing.
            reorder = ReorderLib.fixed() ;
            log.debug("Fixed pattern BGP optimizer") ;
        }

        if ( location.exists(Names.optNone) ) {
            reorder = ReorderLib.identity() ;
            log.debug("Optimizer explicitly turned off") ;
        }

        if ( reorder == null )
            reorder = SystemTDB.defaultOptimizer ;

        if ( reorder == null && warnAboutOptimizer )
            ARQ.getExecLogger().warn("No BGP optimizer") ;

        return reorder ;
    }

    interface RecordBlockMgr {
        void record(FileRef fileRef, BlockMgr blockMgr) ;
    }

    interface RecordNodeTable {
        void record(FileRef fileRef, NodeTable nodeTable) ;
    }

    static class NodeTableBuilderRecorder implements NodeTableBuilder {
        private NodeTableBuilder builder ;
        private RecordNodeTable  recorder ;

        NodeTableBuilderRecorder(NodeTableBuilder ntb, RecordNodeTable recorder) {
            this.builder = ntb ;
            this.recorder = recorder ;
        }

        @Override
        public NodeTable buildNodeTable(FileSet fsIndex, FileSet fsObjectFile, SystemParams params) {
            NodeTable nt = builder.buildNodeTable(fsIndex, fsObjectFile, params) ;
            // It just knows, right?
            FileRef ref = FileRef.create(fsObjectFile.filename(Names.extNodeData)) ;
            recorder.record(ref, nt) ;
            return nt ;
        }

    }

    static class BlockMgrBuilderRecorder implements BlockMgrBuilder {
        private BlockMgrBuilder builder ;
        private RecordBlockMgr  recorder ;

        BlockMgrBuilderRecorder(BlockMgrBuilder blkMgrBuilder, RecordBlockMgr recorder) {
            this.builder = blkMgrBuilder ;
            this.recorder = recorder ;
        }

        @Override
        public BlockMgr buildBlockMgr(FileSet fileSet, String ext, IndexParams params) {
            BlockMgr blkMgr = builder.buildBlockMgr(fileSet, ext, params) ;
            FileRef ref = FileRef.create(fileSet, ext) ;
            recorder.record(ref, blkMgr) ;
            return blkMgr ;
        }
    }

    static class Recorder implements RecordBlockMgr, RecordNodeTable {

        private DatasetBuilderStd dsBuilder ;

        Recorder(DatasetBuilderStd dsBuilder) {
            this.dsBuilder = dsBuilder ;
        }

        @Override
        public void record(FileRef fileRef, BlockMgr blockMgr) {
            // log.info("BlockMgr: "+fileRef) ;
            dsBuilder.blockMgrs.put(fileRef, blockMgr) ;
        }

        @Override
        public void record(FileRef fileRef, NodeTable nodeTable) {
            // log.info("NodeTable: "+fileRef) ;
            dsBuilder.nodeTables.put(fileRef, nodeTable) ;
        }
    }
}
