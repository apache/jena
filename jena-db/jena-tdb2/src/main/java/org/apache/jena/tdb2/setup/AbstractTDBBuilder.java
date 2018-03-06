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

package org.apache.jena.tdb2.setup;

import java.io.File;
import java.io.FileFilter;

import org.apache.jena.atlas.lib.tuple.TupleMap;
import org.apache.jena.dboe.DBOpEnvException;
import org.apache.jena.dboe.base.file.*;
import org.apache.jena.dboe.base.record.RecordFactory;
import org.apache.jena.dboe.index.RangeIndex;
import org.apache.jena.dboe.sys.Names;
import org.apache.jena.dboe.trans.data.TransBinaryDataFile;
import org.apache.jena.dboe.transaction.txn.ComponentId;
import org.apache.jena.dboe.transaction.txn.TransactionCoordinator;
import org.apache.jena.dboe.transaction.txn.TransactionalBase;
import org.apache.jena.dboe.transaction.txn.TransactionalSystem;
import org.apache.jena.dboe.transaction.txn.journal.Journal;
import org.apache.jena.sparql.engine.main.QC;
import org.apache.jena.sparql.engine.optimizer.reorder.ReorderLib;
import org.apache.jena.tdb2.TDBException;
import org.apache.jena.tdb2.solver.OpExecutorTDB2;
import org.apache.jena.tdb2.store.*;
import org.apache.jena.tdb2.store.nodetable.NodeTable;
import org.apache.jena.tdb2.store.nodetable.NodeTableCache;
import org.apache.jena.tdb2.store.nodetable.NodeTableInline;
import org.apache.jena.tdb2.store.nodetupletable.NodeTupleTable;
import org.apache.jena.tdb2.store.nodetupletable.NodeTupleTableConcrete;
import org.apache.jena.tdb2.store.tupletable.TupleIndex;
import org.apache.jena.tdb2.store.tupletable.TupleIndexRecord;
import org.apache.jena.tdb2.sys.SystemTDB;
import org.slf4j.Logger;

/** Build TDB2-technology databases.
 * <p>
 * <b>Do not call these operations directly - use StoreConnection.</b>
 */
public abstract class AbstractTDBBuilder {
    protected abstract Logger log();
    
    // ---- Helpers.
    public static TransactionCoordinator buildTransactionCoordinator(Location location) {
        Journal journal = Journal.create(location);
        TransactionCoordinator txnCoord = new TransactionCoordinator(journal);
        return txnCoord;
    }

    public static String choosePrimaryForIndex(StoreParams params, String index) {
        String primary3 = params.getPrimaryIndexTriples();
        String primary4 = params.getPrimaryIndexQuads();
        
        if ( index.length() == primary3.length() )
            return primary3;
        if ( index.length() == primary4.length() )
            return primary4;
        throw new DBOpEnvException("Can't find primary for '"+index+"'");
    }

    // ---- Object starts
    final Location location;
    final StoreParams params;
    final ComponentIdMgr componentIdMgr;
    final TransactionCoordinator txnCoord;

    protected AbstractTDBBuilder(TransactionCoordinator txnCoord, Location location, StoreParams params, ComponentIdMgr componentIdMgr) {
        this.txnCoord = txnCoord;
        this.location = location;
        this.params = params;
        this.componentIdMgr = componentIdMgr;
    }

    // Supply these two operations at least.
    public abstract NodeTable buildBaseNodeTable(String name);
    public abstract RangeIndex buildRangeIndex(RecordFactory recordFactory, String name);
    
    protected DatasetGraphTxn build$() {
        NodeTable nodeTable = buildNodeTable(params.getNodeTableBaseName());
        
        TripleTable tripleTable = buildTripleTable(nodeTable);
        QuadTable quadTable = buildQuadTable(nodeTable);
        
        NodeTable nodeTablePrefixes = buildNodeTable(params.getPrefixTableBaseName());
        DatasetPrefixesTDB prefixes = buildPrefixTable(nodeTablePrefixes);
        
        TransactionalSystem trans = new TransactionalBase(txnCoord);
        DatasetGraphTxn dsg = new DatasetGraphTDB(trans, 
                                                  tripleTable, quadTable, prefixes, 
                                                  ReorderLib.fixed(), location, params);
        QC.setFactory(dsg.getContext(), OpExecutorTDB2.OpExecFactoryTDB);
        txnCoord.start();
        return dsg;
    }

    public Location getLocation()               { return location; }
    public StoreParams getParams()              { return params; }
    public TransactionCoordinator getTxnCoord() { return txnCoord; }
    
    /** Look at a directory and see if it is a new area */
    protected static boolean isNewDatabaseArea(Location location) {
        if ( location.isMem() )
            return true;
        File d = new File(location.getDirectoryPath());
        if ( !d.exists() )
            return true;
        FileFilter ff = fileFilterNewDB;
        File[] entries = d.listFiles(ff);
        return entries.length == 0;
    }
    
    /** FileFilter
     * Skips "..", ".", "tdb.lock" and "tdb.cfg"
     * 
     */
    protected static  FileFilter fileFilterNewDB  = (pathname)->{
        String fn = pathname.getName();
        if ( fn.equals(".") || fn.equals("..") )
            return false;
        if ( pathname.isDirectory() )
            return true;
        if ( fn.equals(Names.TDB_LOCK_FILE) )
            return false;
        if ( fn.equals(Names.TDB_CONFIG_FILE) )
            return false;
        
        return true;
    };
    
    public TripleTable buildTripleTable(NodeTable nodeTable) {    
        String primary = params.getPrimaryIndexTriples();
        String[] indexes = params.getTripleIndexes();

        // Validation checks - common index forms.  
        if ( indexes.length != 3 && indexes.length != 2 )
            error(log(), "Wrong number of triple table indexes: "+String.join(",", indexes));
        log().debug("Triple table: "+primary+" :: "+String.join(",", indexes));

        TupleIndex tripleIndexes[] = makeTupleIndexes(primary, indexes);

        if ( tripleIndexes.length != indexes.length )
            error(log(), "Wrong number of triple table tuples indexes: "+tripleIndexes.length);
        TripleTable tripleTable = new TripleTable(tripleIndexes, nodeTable);
        return tripleTable;
    }

    public QuadTable buildQuadTable(NodeTable nodeTable) {    
        String primary = params.getPrimaryIndexQuads();
        String[] indexes = params.getQuadIndexes();

        // Validation checks - common index forms.  
        if ( indexes.length != 6 && indexes.length != 4 )
            error(log(), "Wrong number of quad table indexes: "+String.join(",", indexes));
        log().debug("Quad table: "+primary+" :: "+String.join(",", indexes));

        TupleIndex quadIndexes[] = makeTupleIndexes(primary, indexes);

        if ( quadIndexes.length != indexes.length )
            error(log(), "Wrong number of quad table tuples indexes: "+quadIndexes.length);
        QuadTable quadTable = new QuadTable(quadIndexes, nodeTable);
        return quadTable;
    }

    public DatasetPrefixesTDB buildPrefixTable(NodeTable prefixNodes) {
        String primary = params.getPrimaryIndexPrefix();
        String[] indexes = params.getPrefixIndexes();

        TupleIndex prefixIndexes[] = makeTupleIndexes(primary, indexes);
        if ( prefixIndexes.length != 1 )
            error(log(), "Wrong number of triple table tuples indexes: "+prefixIndexes.length);

        // No cache - the prefix mapping is a cache
        //NodeTable prefixNodes = makeNodeTable(location, pnNode2Id, pnId2Node, -1, -1, -1) ;
        NodeTupleTable prefixTable = new NodeTupleTableConcrete(primary.length(),
                                                                prefixIndexes,
                                                                prefixNodes);
        DatasetPrefixesTDB prefixes = new DatasetPrefixesTDB(prefixTable);
        log().debug("Prefixes: "+primary+" :: "+String.join(",", indexes));
        return prefixes;
    }

    // ---- Build structures

    public TupleIndex[] makeTupleIndexes(String primary, String[] indexNames) {
        int indexRecordLen = primary.length()*SystemTDB.SizeOfNodeId;
        TupleIndex indexes[] = new TupleIndex[indexNames.length];
        for (int i = 0; i < indexes.length; i++) {
            String indexName = indexNames[i];
            String indexLabel = indexNames[i];
            indexes[i] = buildTupleIndex(primary, indexName, indexLabel);
        }
        return indexes;
    }

    public TupleIndex buildTupleIndex(String primary, String index, String name) {
        TupleMap cmap = TupleMap.create(primary, index);
        RecordFactory rf = new RecordFactory(SystemTDB.SizeOfNodeId * cmap.length(), 0);
        RangeIndex rIdx = buildRangeIndex(rf, index);
        TupleIndex tIdx = new TupleIndexRecord(primary.length(), cmap, index, rf, rIdx);
        return tIdx;
    }
    
    public NodeTable buildNodeTable(String name) {
        NodeTable nodeTable = buildBaseNodeTable(name);
        nodeTable = NodeTableCache.create(nodeTable, params);
        nodeTable = NodeTableInline.create(nodeTable);
        return nodeTable;
    }

    public TransBinaryDataFile buildBinaryDataFile(String name) {
        ComponentId cid = componentIdMgr.getComponentId(name);
        FileSet fs = new FileSet(location, name); 
        BinaryDataFile binFile = FileFactory.createBinaryDataFile(fs, Names.extObjNodeData);
        BufferChannel pState = FileFactory.createBufferChannel(fs, Names.extBdfState);
        // ComponentId mgt.
        TransBinaryDataFile transBinFile = new TransBinaryDataFile(binFile, cid, pState);
        return transBinFile;
    }
    
    private void error(Logger log, String msg)
    {
        if ( log != null )
            log.error(msg);
        throw new TDBException(msg);
    }
}
