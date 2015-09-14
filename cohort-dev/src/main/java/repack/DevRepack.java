/*
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
 
package repack;

import org.apache.jena.atlas.logging.LogCtl ;
import org.apache.jena.query.ReadWrite ;
import org.seaborne.dboe.base.file.Location ;
import org.seaborne.dboe.transaction.txn.TransactionCoordinator ;
import org.seaborne.tdb2.TDBFactory ;
import org.seaborne.tdb2.store.DatasetGraphTDB ;

public class DevRepack {
    static { LogCtl.setLog4j(); }

    // Turn on transaction logging.
    // Copy at current generation
    // replay logs (still recording!)
    // Go single mode
    // replay later logs
    // switch over
    
    // 1 - How to capture several transactions.
    // 2 - 
    
    public static void main(String... argv) {
        DatasetGraphTDB dsg = (DatasetGraphTDB)TDBFactory.createDatasetGraph();
        repack(dsg);
    }

    // TestTransactionCoordinatorControl ++ start writer -- go no writers --
    // test.
    
    /** Safe repack - go into "no writer" mode, clone the  
     */
    private static void repackSafe(DatasetGraphTDB dsg, Location newLocation) {
        TransactionCoordinator txnMgr = dsg.getTxnSystem().getTxnMgr() ; 
        txnMgr.execAsWriter(()->{
            // No writers.
            dsg.begin(ReadWrite.READ);
            DatasetGraphTDB dsg2 = CloneTDB.cloneDatasetSimple(dsg, newLocation) ;
            txnMgr.execExclusive(null);
            switchDatasets(dsg, dsg2) ;
            // New transactions go to dsg2 ;
        }) ;
        // Drain all old R transactions.
        txnMgr.startExclusiveMode();
        txnMgr.finishExclusiveMode();
        // dsg is now free.
    }

    private static void switchDatasets(DatasetGraphTDB dsg, DatasetGraphTDB dsg2) {}

    private static void repack(DatasetGraphTDB dsg) {
        // Clonign requires a consistent 
        
        // transaction
        TransactionCoordinator txnMgr = dsg.getTxnSystem().getTxnMgr() ;
        
        txnMgr.execAsWriter(()->{
            // Start collecting replay logs.
            
            // Start read transaction for this timepoint for the clone.
            dsg.begin(ReadWrite.READ);
        }) ;
    
        // clone
        
        txnMgr.execAsWriter(()->{
            // replay logs.
            // ???
            // Set up switch
            // Switch (for new transactions).
            // End clone transaction.
            dsg.end() ;
        }) ;
    }


}

