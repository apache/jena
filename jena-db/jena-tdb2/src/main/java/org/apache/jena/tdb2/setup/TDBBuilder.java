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

import java.util.UUID;

import org.apache.jena.dboe.base.file.FileSet;
import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.dboe.base.record.RecordFactory;
import org.apache.jena.dboe.index.Index;
import org.apache.jena.dboe.index.RangeIndex;
import org.apache.jena.dboe.trans.bplustree.BPlusTree;
import org.apache.jena.dboe.trans.bplustree.BPlusTreeFactory;
import org.apache.jena.dboe.trans.data.TransBinaryDataFile;
import org.apache.jena.dboe.transaction.txn.ComponentId;
import org.apache.jena.dboe.transaction.txn.TransactionCoordinator;
import org.apache.jena.tdb2.store.DatasetGraphTxn;
import org.apache.jena.tdb2.store.nodetable.NodeTable;
import org.apache.jena.tdb2.store.nodetable.NodeTableTRDF;
import org.apache.jena.tdb2.sys.SystemTDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Takes from TDB2Builder
// Converted to statics.

/** Build TDB2 databases.
 * <p>
 * <b>Do not call these operations directly - use StoreConnection.</b>
 */
public class TDBBuilder extends AbstractTDBBuilder {
    private Logger log = LoggerFactory.getLogger(TDBBuilder.class);
    
    @Override
    protected Logger log() { return log; }
    
    public static DatasetGraphTxn build(Location location) {
        return build(location, StoreParams.getDftStoreParams());
    }

    public static DatasetGraphTxn build(Location location, StoreParams appParams) {
        StoreParams locParams = StoreParamsCodec.read(location);
        StoreParams dftParams = StoreParams.getDftStoreParams();
        boolean newArea = isNewDatabaseArea(location);
        if ( newArea ) {
        }
        // This can write the chosen parameters if necessary (new database, appParams != null, locParams == null)
        StoreParams params = StoreParamsFactory.decideStoreParams(location, newArea, appParams, locParams, dftParams);
        return create(location, params).build$(); 
    }

    public static TDBBuilder create(Location location) {
        return create(location, StoreParams.getDftStoreParams()); 
    }

    public static TDBBuilder create(Location location, StoreParams params) {
        TransactionCoordinator txnCoord = buildTransactionCoordinator(location);
        return new TDBBuilder(txnCoord, location, params, new ComponentIdMgr(UUID.randomUUID()));
    }

    public static TDBBuilder create(TransactionCoordinator txnCoord, Location location, StoreParams params) {
        return new TDBBuilder(txnCoord, location, params, new ComponentIdMgr(UUID.randomUUID()));
    }

    protected TDBBuilder(TransactionCoordinator txnCoord, Location location, StoreParams params, ComponentIdMgr componentIdMgr) {
        super(txnCoord, location, params, componentIdMgr);
    }

    @Override
    public RangeIndex buildRangeIndex(RecordFactory recordFactory, String name) {
        ComponentId cid = componentIdMgr.getComponentId(name);
        FileSet fs = new FileSet(location, name);
        BPlusTree bpt = BPlusTreeFactory.createBPTree(cid, fs, recordFactory);
        txnCoord.add(bpt);
        return bpt;
    }
    
    @Override
    public NodeTable buildBaseNodeTable(String name) {
        RecordFactory recordFactory = new RecordFactory(SystemTDB.LenNodeHash, SystemTDB.SizeOfNodeId);
        Index index = buildRangeIndex(recordFactory, name);
        
        String dataname = name+"-data"; 
        TransBinaryDataFile transBinFile = buildBinaryDataFile(dataname);
        txnCoord.add(transBinFile);
        return new NodeTableTRDF(index, transBinFile);
    }
}
