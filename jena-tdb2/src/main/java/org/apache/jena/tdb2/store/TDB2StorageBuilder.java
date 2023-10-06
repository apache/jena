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

package org.apache.jena.tdb2.store;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import org.apache.jena.atlas.lib.tuple.TupleMap;
import org.apache.jena.dboe.DBOpEnvException;
import org.apache.jena.dboe.base.file.*;
import org.apache.jena.dboe.base.record.RecordFactory;
import org.apache.jena.dboe.index.Index;
import org.apache.jena.dboe.index.RangeIndex;
import org.apache.jena.dboe.storage.DatabaseRDF;
import org.apache.jena.dboe.storage.StoragePrefixes;
import org.apache.jena.dboe.sys.Names;
import org.apache.jena.dboe.trans.bplustree.BPlusTree;
import org.apache.jena.dboe.trans.bplustree.BPlusTreeFactory;
import org.apache.jena.dboe.trans.data.TransBinaryDataFile;
import org.apache.jena.dboe.transaction.txn.*;
import org.apache.jena.dboe.transaction.txn.journal.Journal;
import org.apache.jena.sparql.engine.main.QC;
import org.apache.jena.sparql.engine.optimizer.reorder.ReorderTransformation;
import org.apache.jena.tdb2.TDBException;
import org.apache.jena.tdb2.params.StoreParams;
import org.apache.jena.tdb2.solver.OpExecutorTDB2;
import org.apache.jena.tdb2.store.nodetable.NodeTable;
import org.apache.jena.tdb2.store.nodetable.NodeTableCache;
import org.apache.jena.tdb2.store.nodetable.NodeTableInline;
import org.apache.jena.tdb2.store.nodetable.NodeTableTRDF;
import org.apache.jena.tdb2.store.nodetupletable.NodeTupleTable;
import org.apache.jena.tdb2.store.nodetupletable.NodeTupleTableConcrete;
import org.apache.jena.tdb2.store.tupletable.TupleIndex;
import org.apache.jena.tdb2.store.tupletable.TupleIndexRecord;
import org.apache.jena.tdb2.sys.ComponentIdMgr;
import org.apache.jena.tdb2.sys.DatabaseConnection;
import org.apache.jena.tdb2.sys.DatabaseOps;
import org.apache.jena.tdb2.sys.SystemTDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Build TDB2 databases based on {@linkplain DatabaseRDF}.
 * This builds the storage database, not the switchable.
 *
 * {@link DatabaseOps#createSwitchable} adds the switching layer
 * and is called by {@link DatabaseConnection#make}
 * with the machinery in {@link DatabaseOps}.
 */
public class TDB2StorageBuilder {
    private static Logger log = LoggerFactory.getLogger(TDB2StorageBuilder.class);

    /*
     * Build a storage database at the given location with default store parameters and reorder transformation.
     * Typically used only for databases without a switchable container (tests or in-memory TDB2).
     */
    public static DatasetGraphTDB build(Location location) {
        return build(location, null, null);
    }

    public static DatasetGraphTDB build(Location location, StoreParams params, ReorderTransformation reorderTransform) {
        // Decisions about the StoreParams params and ReorderTransformation are made in DatabaseOps.build.
        if (params == null ) {
            if ( location.isMem() )
                params = StoreParams.getDftMemStoreParams();
            else
                params = StoreParams.getDftStoreParams();
        }

        if ( reorderTransform == null )
            reorderTransform = SystemTDB.getDefaultReorderTransform();

        // Builder pattern for adding components.
        TransactionCoordinator txnCoord = buildTransactionCoordinator(location);
        TransactionalSystem txnSystem = new TransactionalBase(txnCoord);

        TDB2StorageBuilder builder = new TDB2StorageBuilder(txnSystem, location, params, new ComponentIdMgr(UUID.randomUUID()));
        StorageTDB storage = builder.buildStorage();
        StoragePrefixes prefixes = builder.buildPrefixes();

        // Finalize.
        builder.components.forEach(txnCoord::add);
        builder.listeners.forEach(txnCoord::addListener);
        // Freezes the TransactionCoordinator components
        txnCoord.start();
        DatasetGraphTDB dsg = new DatasetGraphTDB(location, params, reorderTransform,
                                                  storage, prefixes, txnSystem);

        // Enable query processing.
        QC.setFactory(dsg.getContext(), OpExecutorTDB2.OpExecFactoryTDB);
        return dsg;
    }

    private static TransactionCoordinator buildTransactionCoordinator(Location location) {
        Journal journal = Journal.create(location);
        TransactionCoordinator txnCoord = new TransactionCoordinator(journal);
        return txnCoord;
    }

    private static String choosePrimaryForIndex(StoreParams params, String index) {
        String primary3 = params.getPrimaryIndexTriples();
        String primary4 = params.getPrimaryIndexQuads();

        if ( index.length() == primary3.length() )
            return primary3;
        if ( index.length() == primary4.length() )
            return primary4;
        throw new DBOpEnvException("Can't find primary for '"+index+"'");
    }

    /** Look at a directory and see if it is a new area */
    private static boolean isNewDatabaseArea(Location location) {
        if ( location.isMem() )
            return true;
        File d = new File(location.getDirectoryPath());
        if ( !d.exists() )
            return true;
        FileFilter ff = fileFilterNewDB;
        File[] entries = d.listFiles(ff);
        return entries.length == 0;
    }

    /**
     * FileFilter
     * Skips "..", "." "tdb.lock", and "tdb.cfg"
     */
    private static  FileFilter fileFilterNewDB  = (pathname)->{
        String fn = pathname.getName();
        if ( fn.equals(".") || fn.equals("..") )
            return false;
        if ( pathname.isDirectory() )
            return true;
        if ( fn.equals(Names.TDB_CONFIG_FILE) )
            return false;
        if ( fn.equals(Names.TDB_LOCK_FILE) )
            return false;
        return true;
    };

    private static void error(Logger log, String msg) {
        if ( log != null )
            log.error(msg);
        throw new TDBException(msg);
    }

    // ---- Object starts
    private final Location location;
    private final StoreParams params;
    private final TransactionalSystem txnSystem;
    private final ComponentIdMgr componentIdMgr;
    // Accumulate TransactionalComponents as they are used to build the database.
    private final Collection<TransactionalComponent> components = new ArrayList<>();
    private final Collection<TransactionListener> listeners = new ArrayList<>();

    private TDB2StorageBuilder(TransactionalSystem txnSystem,
                               Location location, StoreParams params,
                               ComponentIdMgr componentIdMgr) {
        this.txnSystem = txnSystem;
        this.location = location;
        this.params = params;
        this.componentIdMgr = componentIdMgr;
    }

//    private Location getLocation()               { return location; }
//    private StoreParams getParams()              { return params; }
//    private TransactionCoordinator getTxnCoord() { return txnCoord; }
//    private Collection<TransactionalComponent> getComponents() { return components; }

    private StorageTDB buildStorage() {
        NodeTable nodeTable = buildNodeTable(params.getNodeTableBaseName(), true);
        TripleTable tripleTable = buildTripleTable(nodeTable);
        QuadTable quadTable = buildQuadTable(nodeTable);
        StorageTDB dsg = new StorageTDB(txnSystem, tripleTable, quadTable);
        return dsg;
    }

    private StoragePrefixes buildPrefixes() {
        NodeTable nodeTablePrefixes = buildNodeTable(params.getPrefixTableBaseName(), false);
        StoragePrefixesTDB prefixes = buildPrefixTable(nodeTablePrefixes);
        return prefixes;
    }

    private TripleTable buildTripleTable(NodeTable nodeTable) {
        String primary = params.getPrimaryIndexTriples();
        String[] indexes = params.getTripleIndexes();

        // Validation checks - common index forms.
        if ( indexes.length != 3 && indexes.length != 2 )
            error(log, "Wrong number of triple table indexes: "+String.join(",", indexes));
        log.debug("Triple table: "+primary+" :: "+String.join(",", indexes));

        TupleIndex tripleIndexes[] = makeTupleIndexes(primary, indexes);

        if ( tripleIndexes.length != indexes.length )
            error(log, "Wrong number of triple table tuples indexes: "+tripleIndexes.length);
        TripleTable tripleTable = new TripleTable(tripleIndexes, nodeTable);
        return tripleTable;
    }

    private QuadTable buildQuadTable(NodeTable nodeTable) {
        String primary = params.getPrimaryIndexQuads();
        String[] indexes = params.getQuadIndexes();

        // Validation checks - common index forms.
        if ( indexes.length != 6 && indexes.length != 4 )
            error(log, "Wrong number of quad table indexes: "+String.join(",", indexes));
        log.debug("Quad table: "+primary+" :: "+String.join(",", indexes));

        TupleIndex tripleIndexes[] = makeTupleIndexes(primary, indexes);

        if ( tripleIndexes.length != indexes.length )
            error(log, "Wrong number of triple table tuples indexes: "+tripleIndexes.length);
        QuadTable tripleTable = new QuadTable(tripleIndexes, nodeTable);
        return tripleTable;
    }

    private StoragePrefixesTDB buildPrefixTable(NodeTable prefixNodes) {
        String primary = params.getPrimaryIndexPrefix();
        String[] indexes = params.getPrefixIndexes();

        TupleIndex prefixIndexes[] = makeTupleIndexes(primary, indexes);
        if ( prefixIndexes.length != 1 )
            error(log, "Wrong number of triple table tuples indexes: "+prefixIndexes.length);

        // No cache - the prefix mapping is a cache
        //NodeTable prefixNodes = makeNodeTable(location, pnNode2Id, pnId2Node, -1, -1, -1);
        NodeTupleTable prefixTable = new NodeTupleTableConcrete(primary.length(),
                                                                prefixIndexes,
                                                                prefixNodes);
        StoragePrefixesTDB x = new StoragePrefixesTDB(txnSystem, prefixTable);
        //DatasetPrefixesTDB prefixes = new DatasetPrefixesTDB(prefixTable);
        log.debug("Prefixes: "+primary+" :: "+String.join(",", indexes));
        return x;
    }

    // ---- Build structures

    private TupleIndex[] makeTupleIndexes(String primary, String[] indexNames) {
        int indexRecordLen = primary.length()*SystemTDB.SizeOfNodeId;
        TupleIndex indexes[] = new TupleIndex[indexNames.length];
        for (int i = 0; i < indexes.length; i++) {
            String indexName = indexNames[i];
            String indexLabel = indexNames[i];
            indexes[i] = makeTupleIndex(primary, indexName, indexLabel);
        }
        return indexes;
    }

    private TupleIndex makeTupleIndex(String primary, String index, String name) {
        TupleMap cmap = TupleMap.create(primary, index);
        RecordFactory rf = new RecordFactory(SystemTDB.SizeOfNodeId * cmap.length(), 0);
        RangeIndex rIdx = makeRangeIndex(rf, index);
        TupleIndex tIdx = new TupleIndexRecord(primary.length(), cmap, index, rf, rIdx);
        return tIdx;
    }

    private RangeIndex makeRangeIndex(RecordFactory recordFactory, String name) {
        ComponentId cid = componentIdMgr.getComponentId(name);
        FileSet fs = new FileSet(location, name);
        BPlusTree bpt = BPlusTreeFactory.createBPTree(cid, fs, recordFactory);
        components.add(bpt);
        return bpt;
    }

    private NodeTable buildNodeTable(String name, boolean isData) {
        NodeTable nodeTable = buildBaseNodeTable(name);

        nodeTable = addNodeTableCache(nodeTable, params, isData);

        if ( nodeTable instanceof NodeTableCache nodeTableCache)
            listeners.add(nodeTableCache);

        nodeTable = NodeTableInline.create(nodeTable);
        return nodeTable;
    }

    private static NodeTable addNodeTableCache(NodeTable nodeTable, StoreParams params, boolean isData) {
        int nodeToIdCacheSize   = isData ? params.getNode2NodeIdCacheSize() : params.getPrefixNode2NodeIdCacheSize();
        int idToNodeCacheSize   = isData ? params.getNodeId2NodeCacheSize() : params.getPrefixNodeId2NodeCacheSize();
        int missCacheSize       = isData ? params.getNodeMissCacheSize()    : params.getPrefixNodeMissCacheSize();
        nodeTable = NodeTableCache.create(nodeTable, nodeToIdCacheSize, idToNodeCacheSize, missCacheSize);
        return nodeTable;
    }

    private NodeTable buildBaseNodeTable(String name) {
        RecordFactory recordFactory = new RecordFactory(SystemTDB.LenNodeHash, SystemTDB.SizeOfNodeId);
        Index index = makeRangeIndex(recordFactory, name);

        String dataname = name+"-data";
        TransBinaryDataFile transBinFile = makeBinaryDataFile(dataname);
        components.add(transBinFile);
        return new NodeTableTRDF(index, transBinFile);
    }

    private TransBinaryDataFile makeBinaryDataFile(String name) {
        ComponentId cid = componentIdMgr.getComponentId(name);
        FileSet fs = new FileSet(location, name);
        BinaryDataFile binFile = FileFactory.createBinaryDataFile(fs, Names.extObjNodeData);
        BufferChannel pState = FileFactory.createBufferChannel(fs, Names.extBdfState);
        // ComponentId mgt.
        TransBinaryDataFile transBinFile = new TransBinaryDataFile(binFile, cid, pState);
        return transBinFile;
    }
}
