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

package org.apache.jena.tdb1.setup;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.engine.main.QC;
import org.apache.jena.sparql.engine.optimizer.reorder.ReorderLib;
import org.apache.jena.sparql.engine.optimizer.reorder.ReorderTransformation;
import org.apache.jena.sparql.sse.SSE_ParseException;
import org.apache.jena.tdb1.TDB1;
import org.apache.jena.tdb1.TDB1Exception;
import org.apache.jena.tdb1.base.block.BlockMgr;
import org.apache.jena.tdb1.base.file.BufferChannel;
import org.apache.jena.tdb1.base.file.FileSet;
import org.apache.jena.tdb1.base.file.Location;
import org.apache.jena.tdb1.base.objectfile.ObjectFile;
import org.apache.jena.tdb1.base.record.RecordFactory;
import org.apache.jena.tdb1.index.BuilderStdIndex;
import org.apache.jena.tdb1.index.Index;
import org.apache.jena.tdb1.index.IndexParams;
import org.apache.jena.tdb1.index.RangeIndex;
import org.apache.jena.tdb1.index.bplustree.BPlusTree;
import org.apache.jena.tdb1.index.bplustree.BPlusTreeParams;
import org.apache.jena.tdb1.lib.ColumnMap;
import org.apache.jena.tdb1.solver.OpExecutorTDB1;
import org.apache.jena.tdb1.store.*;
import org.apache.jena.tdb1.store.nodetable.NodeTable;
import org.apache.jena.tdb1.store.nodetable.NodeTableCache;
import org.apache.jena.tdb1.store.nodetable.NodeTableInline;
import org.apache.jena.tdb1.store.nodetable.NodeTableNative;
import org.apache.jena.tdb1.store.nodetupletable.NodeTupleTable;
import org.apache.jena.tdb1.store.nodetupletable.NodeTupleTableConcrete;
import org.apache.jena.tdb1.store.tupletable.TupleIndex;
import org.apache.jena.tdb1.store.tupletable.TupleIndexRecord;
import org.apache.jena.tdb1.sys.*;
import org.slf4j.Logger;

/**
 * This class is the process of building a dataset. Records
 * BlockMgr/BufferChannel/NodeTable for use by the transaction builder.
 */

public class DatasetBuilderStd {
    private static final Logger log = TDB1.logInfo;

    private BlockMgrBuilder blockMgrBuilder = new BuilderStdIndex.BlockMgrBuilderStd();
    private ObjectFileBuilder objectFileBuilder = new BuilderStdDB.ObjectFileBuilderStd();

    private void setupRecord() {
        if ( this.blockMgrBuilder instanceof BlockMgrBuilderRecorder)
            throw new TDB1Exception("Already recording (BlockMgrBuilder)");
        if ( this.objectFileBuilder instanceof ObjectFileBuilderRecorder)
            throw new TDB1Exception("Already recording (ObjectFileBuilder)");

        this.blockMgrBuilder = new BlockMgrBuilderRecorder(blockMgrBuilder, recorder);
        this.objectFileBuilder= new ObjectFileBuilderRecorder(objectFileBuilder, recorder);
    }

    private RangeIndex buildRangeIndex(FileSet fileSet, RecordFactory recordFactory, IndexParams indexParams) {
        int blkSize = indexParams.getBlockSize();
        int order = BPlusTreeParams.calcOrder(blkSize, recordFactory.recordLength());
        RangeIndex rIndex = createBPTree(fileSet, order, blockMgrBuilder, blockMgrBuilder, recordFactory, indexParams);
        return rIndex;
    }

    private Index buildIndex(FileSet fileSet, RecordFactory recordFactory, IndexParams indexParams) {
        // Cheap.
        return buildRangeIndex(fileSet, recordFactory, indexParams);
    }

    /** Knowing all the parameters, create a B+Tree */
    private RangeIndex createBPTree(FileSet fileset, int order,
                                    BlockMgrBuilder blockMgrBuilderNodes,
                                    BlockMgrBuilder blockMgrBuilderRecords,
                                    RecordFactory factory, IndexParams indexParams)
    {
        // ---- Checking
        {
            int blockSize = indexParams.getBlockSize();
            if (blockSize < 0 )
                throw new IllegalArgumentException("Negative blocksize: "+blockSize);
            if (blockSize < 0 && order < 0) throw new IllegalArgumentException("Neither blocksize nor order specified");
            if (blockSize >= 0 && order < 0) order = BPlusTreeParams.calcOrder(blockSize, factory.recordLength());
            if (blockSize >= 0 && order >= 0)
            {
                int order2 = BPlusTreeParams.calcOrder(blockSize, factory.recordLength());
                if (order != order2)
                    throw new IllegalArgumentException("Wrong order (" + order + "), calculated = " + order2);
            }
        }

        BPlusTreeParams params = new BPlusTreeParams(order, factory);

        BlockMgr blkMgrNodes = blockMgrBuilderNodes.buildBlockMgr(fileset, Names.bptExtTree, indexParams);
        BlockMgr blkMgrRecords = blockMgrBuilderRecords.buildBlockMgr(fileset, Names.bptExtRecords, indexParams);
        return BPlusTree.create(params, blkMgrNodes, blkMgrRecords);
    }

    private Recorder            recorder = new Recorder();

    /**
     * @param location
     * @return DatasetGraphTDB
     */
    public static DatasetGraphTDB create(Location location) {
        return create(location, null);
    }

    /**
     * Create a {@link DatasetGraphTDB} with a set of {@link StoreParams}.
     * The parameters for a store have 3 inputs: the parameters provided,
     * any parameters
     *
     * @param location    Where to create the database.
     * @param appParams   Store parameters to use (null means use default). {See {@link StoreParams}).
     * @return DatasetGraphTDB
     */
    public static DatasetGraphTDB create(Location location, StoreParams appParams) {
        StoreParams locParams = StoreParamsCodec.read(location);
        StoreParams dftParams = StoreParams.getDftStoreParams();
        // This can write the chosen parameters if necessary (new database, appParams != null, locParams == null)
        boolean newArea = TDBInternal.isNewDatabaseArea(location);
        StoreParams params = Build.decideStoreParams(location, newArea, appParams, locParams, dftParams);
        DatasetBuilderStd x = new DatasetBuilderStd();
        DatasetGraphTDB dsg = x.build(location, params);
        return dsg;
    }

    public static DatasetGraphTDB create(StoreParams params) {
        // Memory version?
        return create(Location.mem(), params);
    }

    public static DatasetBuilderStd stdBuilder() {
        return new DatasetBuilderStd();
    }

    protected DatasetBuilderStd() {
        this(new BuilderStdIndex.BlockMgrBuilderStd(),
             new BuilderStdDB.ObjectFileBuilderStd());
    }

    public DatasetBuilderStd(BlockMgrBuilder blockMgrBuilder, ObjectFileBuilder objectFileBuilder) {
        this.blockMgrBuilder = blockMgrBuilder;
        this.objectFileBuilder = objectFileBuilder;
        this.recorder = new Recorder();
        // XXX YUK
        setupRecord();
    }

//    private void standardSetup() {
//        ObjectFileBuilder objectFileBuilder = new BuilderStdDB.ObjectFileBuilderStd();
//        BlockMgrBuilder blockMgrBuilder = new BuilderStdIndex.BlockMgrBuilderStd();
//        IndexBuilder indexBuilderNT = new BuilderStdIndex.IndexBuilderStd(blockMgrBuilder, blockMgrBuilder);
//        NodeTableBuilder nodeTableBuilder = new BuilderStdDB.NodeTableBuilderStd(indexBuilderNT, objectFileBuilder);
//        setupRecord(blockMgrBuilder, nodeTableBuilder);
//    }

    private static void checkLocation(Location location) {
        if ( location.isMem() )
            return;
        String dirname = location.getDirectoryPath();
        File dir = new File(dirname);
        // File location.
        if ( ! dir.exists() )
            error(log, "Does not exist: "+dirname);
        if ( ! dir.isDirectory() )
            error(log, "Not a directory: "+dirname);
        if ( ! dir.canRead() )
            error(log, "Directory not readable: "+dirname);
        if ( ! dir.canWrite() )
            error(log, "Directory not writeable: "+dirname);
    }

    public DatasetGraphTDB build(Location location, StoreParams params) {
        // Ensure that there is global synchronization
        synchronized (DatasetBuilderStd.class) {
            log.debug("Build database: "+location.getDirectoryPath());
            checkLocation(location);
            return _build(location, params, true, null);
        }
    }

    private static String DB_CONFIG_FILE = "tdb.cfg";

    // Main engine for building.
    // Called by DatasetBuilderTxn
    // XXX Rework - provide a cloning constructor (copies maps).
    // Or "reset"
    public DatasetGraphTDB _build(Location location, StoreParams params, boolean writeable, ReorderTransformation _transform) {
        return buildWorker(location, writeable, _transform, params);
    }

    private synchronized DatasetGraphTDB buildWorker(Location location, boolean writeable, ReorderTransformation _transform, StoreParams params) {
        recorder.start();
        DatasetControl policy = createConcurrencyPolicy();
        NodeTable nodeTable = makeNodeTable(location, params);
        TripleTable tripleTable = makeTripleTable(location, nodeTable, policy, params);
        QuadTable quadTable = makeQuadTable(location, nodeTable, policy, params);
        DatasetPrefixesTDB prefixes = makePrefixTable(location, policy, params);

        ReorderTransformation transform = (_transform == null) ? chooseReorderTransformation(location) : _transform;

        StorageConfig storageConfig = new StorageConfig(location, params, writeable,
                                                        recorder.blockMgrs, recorder.objectFiles, recorder.bufferChannels);

        recorder.finish();

        DatasetGraphTDB dsg = new DatasetGraphTDB(tripleTable, quadTable, prefixes, transform, storageConfig);
        // TDB does filter placement on BGPs itself.
        dsg.getContext().set(ARQ.optFilterPlacementBGP, false);
        QC.setFactory(dsg.getContext(), OpExecutorTDB1.OpExecFactoryTDB);
        return dsg;
    }

    private static <X,Y> Map<X,Y> freeze(Map<X,Y> map) {
        return Map.copyOf(map);
    }

    protected DatasetControl createConcurrencyPolicy() {
        return new DatasetControlMRSW();
    }

    protected TripleTable makeTripleTable(Location location, NodeTable nodeTable, DatasetControl policy, StoreParams params) {
        String primary = params.getPrimaryIndexTriples();
        String[] indexes = params.getTripleIndexes();

        // Allow experimentation of other index layouts.
        // if ( indexes.length != 3 )
        // error(log,
        // "Wrong number of triple table indexes: "+StrUtils.strjoin(",",
        // indexes));
        TupleIndex tripleIndexes[] = makeTupleIndexes(location, primary, indexes, params);

        if ( tripleIndexes.length != indexes.length )
            error(log, "Wrong number of triple table tuples indexes: " + tripleIndexes.length);
        TripleTable tripleTable = new TripleTable(tripleIndexes, nodeTable, policy);
        return tripleTable;
    }

    protected QuadTable makeQuadTable(Location location, NodeTable nodeTable, DatasetControl policy, StoreParams params) {
        String primary = params.getPrimaryIndexQuads();
        String[] indexes = params.getQuadIndexes();

        // Allow experimentation of other index layouts.
        // if ( indexes.length != 6 )
        // error(log,
        // "Wrong number of quad table indexes: "+StrUtils.strjoin(",",
        // indexes));

        TupleIndex quadIndexes[] = makeTupleIndexes(location, primary, indexes, params);
        if ( quadIndexes.length != indexes.length )
            error(log, "Wrong number of quad table tuples indexes: " + quadIndexes.length);
        QuadTable quadTable = new QuadTable(quadIndexes, nodeTable, policy);
        return quadTable;
    }

    protected DatasetPrefixesTDB makePrefixTable(Location location, DatasetControl policy, StoreParams params) {
        String primary = params.getPrimaryIndexPrefix();
        String[] indexes = params.getPrefixIndexes();

        TupleIndex prefixIndexes[] = makeTupleIndexes(location, primary, indexes, new String[]{params.getIndexPrefix()}, params);
        if ( prefixIndexes.length != 1 )
            error(log, "Wrong number of prefix table tuples indexes: " + prefixIndexes.length);

        String pnNode2Id = params.getPrefixNode2Id();
        String pnId2Node = params.getPrefixId2Node();

        // No cache - the prefix mapping is a cache
        NodeTable prefixNodes = makeNodeTableNoCache(location, pnNode2Id, pnId2Node, params);
        NodeTupleTable prefixTable = new NodeTupleTableConcrete(primary.length(),
                                                                prefixIndexes,
                                                                prefixNodes, policy);
        DatasetPrefixesTDB prefixes = new DatasetPrefixesTDB(prefixTable);
        return prefixes;
    }

    protected ReorderTransformation chooseReorderTransformation(Location location) {
        return chooseOptimizer(location);
    }

    private TupleIndex[] makeTupleIndexes(Location location, String primary, String[] indexNames, StoreParams params) {
        return makeTupleIndexes(location, primary, indexNames, indexNames, params);
    }

    private TupleIndex[] makeTupleIndexes(Location location, String primary, String[] indexNames, String[] filenames, StoreParams params) {
        if ( primary.length() != 3 && primary.length() != 4 )
            error(log, "Bad primary key length: " + primary.length());

        int indexRecordLen = primary.length() * NodeId.SIZE;
        TupleIndex indexes[] = new TupleIndex[indexNames.length];
        for ( int i = 0; i < indexes.length; i++ )
            indexes[i] = makeTupleIndex(location, filenames[i], primary, indexNames[i], params);
        return indexes;
    }

    protected TupleIndex makeTupleIndex(Location location, String name, String primary, String indexOrder, StoreParams params) {
        // Commonly, name == indexOrder.
        FileSet fs = new FileSet(location, name);
        ColumnMap colMap = new ColumnMap(primary, indexOrder);
        return /*tupleIndexBuilder.*/buildTupleIndex(fs, colMap, indexOrder, params);
    }

    //-------------
    private TupleIndex buildTupleIndex(FileSet fileSet, ColumnMap colMap, String name, StoreParams params) {
        RecordFactory recordFactory = new RecordFactory(SystemTDB.SizeOfNodeId * colMap.length(), 0);
        RangeIndex rIdx = /*rangeIndexBuilder.*/buildRangeIndex(fileSet, recordFactory, params);
        TupleIndex tIdx = new TupleIndexRecord(colMap.length(), colMap, name, recordFactory, rIdx);
        return tIdx;
    }

    public NodeTable makeNodeTable(Location location, StoreParams params) {
        return makeNodeTable$(location, params.getIndexNode2Id(), params.getIndexId2Node(), params);
    }

    /** Make a node table overriding the node->id and id->node table names */
    private NodeTable makeNodeTable$(Location location, String indexNode2Id, String indexId2Node, StoreParams params) {
        FileSet fsNodeToId = new FileSet(location, indexNode2Id);
        FileSet fsId2Node = new FileSet(location, indexId2Node);
        NodeTable nt = /*nodeTableBuilder.*/buildNodeTable(fsNodeToId, fsId2Node, params);
        return nt;
    }

    //-------------

    private NodeTable buildNodeTable(FileSet fsIndex, FileSet fsObjectFile, StoreParams params) {
        RecordFactory recordFactory = new RecordFactory(SystemTDB.LenNodeHash, SystemTDB.SizeOfNodeId);
        Index idx = /*indexBuilder.*/buildIndex(fsIndex, recordFactory, params);
        ObjectFile objectFile = objectFileBuilder.buildObjectFile(fsObjectFile, Names.extNodeData);
        NodeTable nodeTable = new NodeTableNative(idx, objectFile);
        nodeTable = NodeTableCache.create(nodeTable,
                                          params.getNode2NodeIdCacheSize(),
                                          params.getNodeId2NodeCacheSize(),
                                          params.getNodeMissCacheSize());
        nodeTable = NodeTableInline.create(nodeTable);
        return nodeTable;
    }

    protected NodeTable makeNodeTableNoCache(Location location, String indexNode2Id, String indexId2Node, StoreParams params) {
        StoreParamsBuilder spb = StoreParams.builder(params)
            .node2NodeIdCacheSize(-1)
            .nodeId2NodeCacheSize(-1)
            .nodeMissCacheSize(-1);
        return makeNodeTable$(location, indexNode2Id, indexId2Node, spb.build());
    }

    private static void error(Logger log, String msg) {
        if ( log != null )
            log.error(msg);
        throw new TDB1Exception(msg);
    }

    private static int parseInt(String str, String messageBase) {
        try {
            return Integer.parseInt(str);
        }
        catch (NumberFormatException ex) {
            error(log, messageBase + ": " + str);
            return -1;
        }
    }

    /**
     * Set the global flag that control the "No BGP optimizer" warning. Set to
     * false to silence the warning
     */
    public static void setOptimizerWarningFlag(boolean b) {
        warnAboutOptimizer = b;
    }
    private static boolean warnAboutOptimizer = true;

    public static ReorderTransformation chooseOptimizer(Location location) {
        if ( location == null )
            return ReorderLib.identity();

        ReorderTransformation reorder = null;
        if ( location.exists(Names.optStats) ) {
            try {
                reorder = ReorderLib.weighted(location.getPath(Names.optStats));
            }
            catch (SSE_ParseException ex) {
                log.warn("Error in stats file: " + ex.getMessage());
                reorder = null;
            }
        }

        if ( reorder == null && location.exists(Names.optFixed) ) {
            // Not as good but better than nothing.
            reorder = ReorderLib.fixed();
            log.debug("Fixed pattern BGP optimizer");
        }

        if ( location.exists(Names.optNone) ) {
            reorder = ReorderLib.identity();
            log.debug("Optimizer explicitly turned off");
        }

        if ( reorder == null )
            reorder = SystemTDB.defaultReorderTransform;

        if ( reorder == null && warnAboutOptimizer )
            ARQ.getExecLogger().warn("No BGP optimizer");

        return reorder;
    }

    interface RecordBlockMgr {
        void record(FileRef fileRef, BlockMgr blockMgr);
    }

    interface RecordObjectFile {
        void record(FileRef fileRef, ObjectFile objFile);
    }

    interface RecordNodeTable {
        void record(FileRef fileRef, NodeTable nodeTable);
    }

    static class ObjectFileBuilderRecorder implements ObjectFileBuilder {
        private final ObjectFileBuilder builder;
        private final RecordObjectFile  recorder;

        ObjectFileBuilderRecorder(ObjectFileBuilder objFileBuilder, RecordObjectFile recorder) {
            this.builder = objFileBuilder;
            this.recorder = recorder;
        }

        @Override
        public ObjectFile buildObjectFile(FileSet fsObjectFile, String ext) {
            ObjectFile objectFile = builder.buildObjectFile(fsObjectFile, ext);
            FileRef ref = FileRef.create(fsObjectFile, ext);
            recorder.record(ref, objectFile);
            return objectFile;
        }
    }

    static class BlockMgrBuilderRecorder implements BlockMgrBuilder {
        private final BlockMgrBuilder builder;
        private final RecordBlockMgr  recorder;

        BlockMgrBuilderRecorder(BlockMgrBuilder blkMgrBuilder, RecordBlockMgr recorder) {
            this.builder = blkMgrBuilder;
            this.recorder = recorder;
        }

        @Override
        public BlockMgr buildBlockMgr(FileSet fileSet, String ext, IndexParams params) {
            BlockMgr blkMgr = builder.buildBlockMgr(fileSet, ext, params);
            FileRef ref = FileRef.create(fileSet, ext);
            recorder.record(ref, blkMgr);
            return blkMgr;
        }
    }

    static class Recorder implements RecordBlockMgr, RecordObjectFile, RecordNodeTable {

        Map<FileRef, BlockMgr>      blockMgrs      = null;
        Map<FileRef, ObjectFile>    objectFiles    = null;
        // Not used currently.
        Map<FileRef, BufferChannel> bufferChannels = null;
        Map<FileRef, NodeTable>     nodeTables = null;
        boolean recording = false;

        Recorder() { }

        void start() {
            if ( recording )
                throw new TDB1Exception("Recorder already recording");
            recording      = true;
            blockMgrs      = new HashMap<>();

            objectFiles    = new HashMap<>();
            bufferChannels = new HashMap<>();
            nodeTables     = new HashMap<>();
        }
        void finish() {
            if ( ! recording )
                throw new TDB1Exception("Recorder not recording");
            // null out, not .clear.
            blockMgrs      = null;
            objectFiles    = null;
            bufferChannels = null;
            recording      = false;
        }

        @Override
        public void record(FileRef fileRef, BlockMgr blockMgr) {
            if ( recording )
                // log.info("BlockMgr: "+fileRef);
                blockMgrs.put(fileRef, blockMgr);
        }

        @Override
        public void record(FileRef fileRef, ObjectFile objFile) {
            if ( recording )
                // log.info("ObjectTable: "+fileRef);
                objectFiles.put(fileRef, objFile);
        }

        @Override
        public void record(FileRef fileRef, NodeTable nodeTable) {
            if ( recording )
                // log.info("NodeTable: "+fileRef);
                nodeTables.put(fileRef, nodeTable);
        }
    }
}
