/**
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.seaborne.tdb2.setup;

import java.io.File ;
import java.io.FileFilter ;

import org.apache.jena.atlas.lib.ColumnMap ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.engine.main.QC ;
import org.apache.jena.sparql.engine.optimizer.reorder.ReorderLib ;
import org.seaborne.dboe.base.file.BinaryDataFile ;
import org.seaborne.dboe.base.file.FileFactory ;
import org.seaborne.dboe.base.file.FileSet ;
import org.seaborne.dboe.base.file.Location ;
import org.seaborne.dboe.base.record.RecordFactory ;
import org.seaborne.dboe.index.Index ;
import org.seaborne.dboe.index.RangeIndex ;
import org.seaborne.dboe.migrate.L ;
import org.seaborne.dboe.trans.bplustree.BPlusTree ;
import org.seaborne.dboe.trans.bplustree.BPlusTreeFactory ;
import org.seaborne.dboe.transaction.Transactional ;
import org.seaborne.dboe.transaction.txn.ComponentId ;
import org.seaborne.dboe.transaction.txn.TransactionCoordinator ;
import org.seaborne.dboe.transaction.txn.TransactionalBase ;
import org.seaborne.dboe.transaction.txn.journal.Journal ;
import org.seaborne.tdb2.TDBException ;
import org.seaborne.tdb2.solver.OpExecutorTDB1 ;
import org.seaborne.tdb2.store.* ;
import org.seaborne.tdb2.store.nodetable.NodeTable ;
import org.seaborne.tdb2.store.nodetable.NodeTableCache ;
import org.seaborne.tdb2.store.nodetable.NodeTableInline ;
import org.seaborne.tdb2.store.nodetable.NodeTableTRDF ;
import org.seaborne.tdb2.store.nodetupletable.NodeTupleTable ;
import org.seaborne.tdb2.store.nodetupletable.NodeTupleTableConcrete ;
import org.seaborne.tdb2.store.tupletable.TupleIndex ;
import org.seaborne.tdb2.store.tupletable.TupleIndexRecord ;
import org.seaborne.tdb2.sys.DatasetControl ;
import org.seaborne.tdb2.sys.DatasetControlMRSW ;
import org.seaborne.tdb2.sys.SystemTDB ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

public class TDB2Builder {
    private static Logger log = LoggerFactory.getLogger(TDB2Builder.class) ;
    
    public static DatasetGraph build(Location location) {
        return build(location, StoreParams.getDftStoreParams()) ;
    }

    // Recover from existing.
    // Align component ids from existing.
    
    private final ComponentId tdbComponentId ;
    private int componentCounter = 1 ;
    private final Location location ;
    private final StoreParams storeParams ;
    private static DatasetControl createPolicy() { return new DatasetControlMRSW() ; }
    
    public static DatasetGraphTxn build(Location location, StoreParams appParams) {
        StoreParams locParams = StoreParamsCodec.read(location) ;
        StoreParams dftParams = StoreParams.getDftStoreParams() ;
        // This can write the chosen parameters if necessary (new database, appParams != null, locParams == null)
        boolean newArea = isNewDatabaseArea(location) ;
        if ( newArea ) {
        }
        StoreParams params = StoreParamsFactory.decideStoreParams(location, newArea, appParams, locParams, dftParams) ;
        return new TDB2Builder(location, params).build() ;
    }
    
    /** Look at a directory and see if it is a new area */
    private static boolean isNewDatabaseArea(Location location) {
        if ( location.isMem() )
            return true ;
        File d = new File(location.getDirectoryPath()) ;
        if ( !d.exists() )
            return true ;
        FileFilter ff = fileFilterNewDB ;
        File[] entries = d.listFiles(ff) ;
        return entries.length == 0 ;
    }
    
    /** FileFilter
     * Skips .., . and "tdb.cfg"
     * 
     */
    static FileFilter fileFilterNewDB  = (pathname)->{
        String fn = pathname.getName() ;
        if ( fn.equals(".") || fn.equals("..") )
            return false ;
        if ( pathname.isDirectory() )
            return true ;
        if ( fn.equals(StoreParamsConst.TDB_CONFIG_FILE) )
            return false ;
        return true ;
    } ;

    private DatasetGraphTxn build() {
        // Migrate to StoreConnection.
        Journal journal = Journal.create(location) ;
        TransactionCoordinator txnCoord = new TransactionCoordinator(journal) ;
        // Reuse existing component ids.
        
        NodeTable nodeTable = buildNodeTable(txnCoord, nextComponentId(storeParams.getNodeTableBaseName()), storeParams.getNodeTableBaseName()) ;
        
        TripleTable tripleTable = buildTripleTable(txnCoord, nodeTable, storeParams) ;
        QuadTable quadTable = buildQuadTable(txnCoord, nodeTable, storeParams) ;
        
        NodeTable nodeTablePrefixes = buildNodeTable(txnCoord, nextComponentId(storeParams.getPrefixTableBaseName()), storeParams.getPrefixTableBaseName()) ;
        DatasetPrefixesTDB prefixes = buildPrefixTable(txnCoord, nodeTablePrefixes, storeParams) ;
        
        DatasetGraphTDB dsg = new DatasetGraphTDB(tripleTable, quadTable, prefixes, ReorderLib.fixed(), location, storeParams) ;
        Transactional trans = new TransactionalBase(txnCoord) ;
        DatasetGraphTxn dsgtxn = new DatasetGraphTxn(dsg, trans, txnCoord) ;
        QC.setFactory(dsgtxn.getContext(), OpExecutorTDB1.OpExecFactoryTDB) ;
        return dsgtxn ;
    }

    public TDB2Builder(Location location, StoreParams storeParams) {
        this.tdbComponentId = ComponentId.create("TDB", L.uuidAsBytes("6096e8da-f654-11e4-89bd-3417eb9beefa")) ;
        this.componentCounter = 1 ;
        this.location = location ;
        this.storeParams = storeParams ;
    }
    
    private ComponentId nextComponentId(String label) {
        return ComponentId.alloc(tdbComponentId, label, componentCounter++) ;
    }

    private TripleTable buildTripleTable(TransactionCoordinator txnCoord, NodeTable nodeTable, StoreParams params)
    {    
        String primary = params.getPrimaryIndexTriples() ;
        String[] indexes = params.getTripleIndexes() ;

        if ( indexes.length != 3 )
            error(log, "Wrong number of triple table indexes: "+StrUtils.strjoin(",", indexes)) ;
        log.debug("Triple table: "+primary+" :: "+StrUtils.strjoin(",", indexes)) ;

        TupleIndex tripleIndexes[] = makeTupleIndexes(txnCoord, primary, indexes, params) ;

        if ( tripleIndexes.length != indexes.length )
            error(log, "Wrong number of triple table tuples indexes: "+tripleIndexes.length) ;
        TripleTable tripleTable = new TripleTable(tripleIndexes, nodeTable, createPolicy()) ;
        return tripleTable ;
    }

    private QuadTable buildQuadTable(TransactionCoordinator txnCoord, NodeTable nodeTable, StoreParams params)
    {    
        String primary = params.getPrimaryIndexQuads() ;
        String[] indexes = params.getQuadIndexes() ;

        if ( indexes.length != 6 )
            error(log, "Wrong number of quad table indexes: "+StrUtils.strjoin(",", indexes)) ;
        log.debug("Quad table: "+primary+" :: "+StrUtils.strjoin(",", indexes)) ;

        TupleIndex tripleIndexes[] = makeTupleIndexes(txnCoord, primary, indexes, params) ;

        if ( tripleIndexes.length != indexes.length )
            error(log, "Wrong number of triple table tuples indexes: "+tripleIndexes.length) ;
        QuadTable tripleTable = new QuadTable(tripleIndexes, nodeTable, createPolicy()) ;
        return tripleTable ;
    }


    private DatasetPrefixesTDB buildPrefixTable(TransactionCoordinator txnCoord,
                                                NodeTable prefixNodes, StoreParams params) {
        String primary = params.getPrimaryIndexPrefix() ;
        String[] indexes = params.getPrefixIndexes() ;

        TupleIndex prefixIndexes[] = makeTupleIndexes(txnCoord, 
                                                      primary, indexes, params) ;
        if ( prefixIndexes.length != 1 )
            error(log, "Wrong number of triple table tuples indexes: "+prefixIndexes.length) ;

        // No cache - the prefix mapping is a cache
        //NodeTable prefixNodes = makeNodeTable(location, pnNode2Id, pnId2Node, -1, -1, -1)  ;
        NodeTupleTable prefixTable = new NodeTupleTableConcrete(primary.length(),
                                                                prefixIndexes,
                                                                prefixNodes, createPolicy()) ;
        DatasetPrefixesTDB prefixes = new DatasetPrefixesTDB(prefixTable) ; 
        log.debug("Prefixes: "+primary+" :: "+StrUtils.strjoin(",", indexes)) ;
        return prefixes ;
    }


    private TupleIndex[] makeTupleIndexes(TransactionCoordinator txnCoord,
                                                 String primary, String[] indexNames, StoreParams params) {
        int indexRecordLen = primary.length()*SystemTDB.SizeOfNodeId ;
        TupleIndex indexes[] = new TupleIndex[indexNames.length] ;
        for (int i = 0 ; i < indexes.length ; i++) {
            String indexName = indexNames[i] ;
            ComponentId cid = nextComponentId(indexName) ;
            indexes[i] = buildTupleIndex(txnCoord, cid, primary, indexNames[i]) ;
        }
        return indexes ;
    }

    public TupleIndex buildTupleIndex(TransactionCoordinator txnMgr, ComponentId cid, String primary, String index) {
        //Library-ize.
        ColumnMap cmap = new ColumnMap(primary, index) ;
        RecordFactory rf = new RecordFactory(SystemTDB.SizeOfNodeId * cmap.length(), 0) ;
        RangeIndex rIdx = buildRangeIndex(txnMgr, cid, rf, index) ;
        TupleIndex tIdx = new TupleIndexRecord(primary.length(), cmap, index, rf, rIdx) ;
        return tIdx ;
    }
    
    public RangeIndex buildRangeIndex(TransactionCoordinator coord, ComponentId cid,
                                      RecordFactory recordFactory,
                                      String name) {
        FileSet fs = new FileSet(location, name) ;
        BPlusTree bpt = BPlusTreeFactory.createBPTree(cid, fs, recordFactory) ;
        coord.add(bpt) ;
        return bpt ;
    }
    
    public NodeTable buildNodeTable(TransactionCoordinator coord, ComponentId cid, String name) {
        NodeTable nodeTable = buildBaseNodeTable(coord, cid, name) ;
        nodeTable = NodeTableCache.create(nodeTable, 
                                          storeParams.getNode2NodeIdCacheSize(),
                                          storeParams.getNodeId2NodeCacheSize(),
                                          storeParams.getNodeMissCacheSize()) ;
        nodeTable = NodeTableInline.create(nodeTable) ;
        return nodeTable ; 
    }

    private NodeTable buildBaseNodeTable(TransactionCoordinator coord, ComponentId cid, String name) {
        RecordFactory recordFactory = new RecordFactory(SystemTDB.LenNodeHash, SystemTDB.SizeOfNodeId) ;
        Index index = buildRangeIndex(coord, cid, recordFactory, name) ;
        FileSet fs = new FileSet(location, name+"-data") ; 
        BinaryDataFile binFile = FileFactory.createBinaryDataFile(fs, "obj") ;
        return new NodeTableTRDF(index, binFile) ;

        // Old SSE encoding for comparison. 
        // Slightly slower to write (5%, SSD), probably slower to read. 
        //NodeTable nodeTable = new NodeTableSSE(index, filename) ;
        
        // Old No write caching smarts. Slower to write (~10%, SSD).
        //NodeTable nodeTable = new NodeTableTRDF_Direct(index, filename) ;
    }
    
    private static void error(Logger log, String msg)
    {
        if ( log != null )
            log.error(msg) ;
        throw new TDBException(msg) ;
    }
}
