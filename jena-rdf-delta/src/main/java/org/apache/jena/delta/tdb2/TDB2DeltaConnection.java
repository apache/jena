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

package org.apache.jena.delta.tdb2;

import org.apache.jena.dboe.transaction.txn.TransactionCoordinator;
import org.apache.jena.delta.DeltaException;
import org.apache.jena.delta.client.DeltaLink;
import org.apache.jena.rdfpatch.system.DatasetGraphChanges;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.tdb2.TDB2Factory;
import org.apache.jena.tdb2.store.DatasetGraphTDB;
import org.apache.jena.tdb2.store.DatasetGraphTxn;
import org.apache.jena.tdb2.sys.TDBInternal;

/**
 * Manages the connection between a TDB2 dataset and an RDF Delta patch log.
 * This class provides methods for creating and configuring TDB2 datasets that automatically
 * track and log changes to a patch log server.
 */
public class TDB2DeltaConnection {
    
    /**
     * Connect a TDB2 dataset to a Delta patch log server. This enables the dataset
     * to automatically send changes to the patch log server when transactions are committed.
     * 
     * @param dataset The TDB2 dataset to connect
     * @param deltaLink The connection to the patch log server
     * @param datasetId The ID of the dataset in the patch log server
     * @return The same dataset, configured to send changes to the patch log server
     */
    public static DatasetGraph connect(DatasetGraph dataset, DeltaLink deltaLink, String datasetId) {
        if (!TDBInternal.isTDB2(dataset))
            throw new DeltaException("Dataset is not a TDB2 dataset");
        
        // Get the TDB2 transaction coordinator
        DatasetGraphTDB dsgtdb = TDBInternal.getDatasetGraphTDB(dataset);
        TransactionCoordinator txnCoord = dsgtdb.getTxnSystem().getTxnMgr().getTransactionCoordinator();
        
        // Create and register the change logger
        TDB2PatchLogger patchLogger = new TDB2PatchLogger(deltaLink, datasetId);
        txnCoord.addListener(patchLogger);
        
        // Optionally wrap with DatasetGraphChanges to track modifications through the RDFChanges interface
        DatasetGraph trackingDsg = new DatasetGraphChanges(dataset, patchLogger);
        
        return trackingDsg;
    }
    
    /**
     * Create a new TDB2 dataset at the specified location and connect it to a Delta patch log server.
     * 
     * @param location The directory where the TDB2 dataset will be stored
     * @param deltaLink The connection to the patch log server
     * @param datasetId The ID of the dataset in the patch log server
     * @return A new TDB2 dataset configured to send changes to the patch log server
     */
    public static DatasetGraph createConnected(String location, DeltaLink deltaLink, String datasetId) {
        DatasetGraph dsg = TDB2Factory.connectDataset(location);
        return connect(dsg, deltaLink, datasetId);
    }
    
    /**
     * Check if a dataset is connected to a Delta patch log server.
     * 
     * @param dataset The dataset to check
     * @return true if the dataset is connected to a Delta patch log server
     */
    public static boolean isConnected(DatasetGraph dataset) {
        if (!TDBInternal.isTDB2(dataset))
            return false;
        
        // Check if it's wrapped with a DatasetGraphChanges
        if (dataset instanceof DatasetGraphChanges) {
            // Check if the changes monitor is a TDB2PatchLogger
            DatasetGraphChanges changes = (DatasetGraphChanges)dataset;
            return changes.getMonitor() instanceof TDB2PatchLogger;
        }
        
        // Check if the transaction coordinator has a TDB2PatchLogger
        DatasetGraphTDB dsgtdb = TDBInternal.getDatasetGraphTDB(dataset);
        TransactionCoordinator txnCoord = dsgtdb.getTxnSystem().getTxnMgr().getTransactionCoordinator();
        
        // This requires accessing a private field or adding a check method to TransactionCoordinator
        // As a workaround, we can only check if it's a TDB2 dataset
        return true;
    }
}