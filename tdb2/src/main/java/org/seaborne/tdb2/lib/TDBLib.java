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

package org.seaborne.tdb2.lib;

import java.util.function.Supplier ;

import org.apache.jena.query.Dataset ;
import org.apache.jena.query.ReadWrite ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.seaborne.dboe.transaction.ThreadTxn ;
import org.seaborne.dboe.transaction.Transactional ;
import org.seaborne.dboe.transaction.Txn ;
import org.seaborne.tdb2.store.DatasetGraphTxn ;

public class TDBLib {
    // Adapters from old world to new world.
    
    /** Execute the Runnable in a read transaction. 
     * @see Txn#executeRead
     */
    public static void executeRead(Dataset ds, Runnable r) {
        Txn.executeRead(transactional(ds), r); 
    }

    /** Execute the Runnable in a write transaction. 
     * @see Txn#executeWrite
     */
    public static void executeWrite(Dataset ds, Runnable r) {
        Txn.executeWrite(transactional(ds), r); 
    }

    /** Execute the Runnable in a read transaction. 
     * @see Txn#executeReadReturn
     */
    public static <X> X executeReadReturn(Dataset ds, Supplier<X> r) {
        return Txn.executeReadReturn(transactional(ds), r); 
    }

    /** Execute the Runnable in a write transaction 
     * @see Txn#executeWriteReturn
     */
    public static <X> X executeWriteReturn(Dataset ds, Supplier<X> r) {
        return Txn.executeWriteReturn(transactional(ds), r) ; 
    }
    
    /** Create a thread-backed delayed READ transaction action. */
    public static ThreadTxn threadTxnRead(Dataset ds, Runnable action) {
        return Txn.threadTxnRead(transactional(ds), action) ;
    }
    
    /** Create a thread-backed delayed WRITE  action.
     * If called from inside a write transaction on the {@code trans},
     * this will deadlock.
     */
    public static ThreadTxn threadTxnWrite(Dataset ds, Runnable action) {
        return Txn.threadTxnWrite(transactional(ds), action) ;
    }
    
    /** Create a thread-backed delayed WRITE-abort action (testing). */
    public static ThreadTxn threadTxnWriteAbort(Dataset ds, Runnable action) {
        return Txn.threadTxnWriteAbort(transactional(ds), action) ;
    }

    /** Execute the Runnable in a read transaction. 
     * @see Txn#executeRead
     */
    public static void executeRead(DatasetGraph ds, Runnable r) {
        Txn.executeRead(transactional(ds), r); 
    }

    /** Execute the Runnable in a write transaction. 
     * @see Txn#executeWrite
     */
    public static void executeWrite(DatasetGraph ds, Runnable r) {
        Txn.executeWrite(transactional(ds), r); 
    }

    /** Execute the Runnable in a read transaction. 
     * @see Txn#executeReadReturn
     */
    public static <X> X executeReadReturn(DatasetGraph ds, Supplier<X> r) {
        return Txn.executeReadReturn(transactional(ds), r); 
    }

    /** Execute the Runnable in a write transaction 
     * @see Txn#executeWriteReturn
     */
    public static <X> X executeWriteReturn(DatasetGraph ds, Supplier<X> r) {
        return Txn.executeWriteReturn(transactional(ds), r) ; 
    }
    
    /** Create a thread-backed delayed READ transaction action. */
    public static ThreadTxn threadTxnRead(DatasetGraph ds, Runnable action) {
        return Txn.threadTxnRead(transactional(ds), action) ;
    }
    
    /** Create a thread-backed delayed WRITE  action.
     * If called from inside a write transaction on the {@code trans},
     * this will deadlock.
     */
    public static ThreadTxn threadTxnWrite(DatasetGraph ds, Runnable action) {
        return Txn.threadTxnWrite(transactional(ds), action) ;
    }
    
    /** Create a thread-backed delayed WRITE-abort action (testing). */
    public static ThreadTxn threadTxnWriteAbort(DatasetGraph ds, Runnable action) {
        return Txn.threadTxnWriteAbort(transactional(ds), action) ;
    }

    private static Transactional transactional(Dataset ds) {
        // adapter across "Transactional"
        DatasetGraphTxn dsgtxn = (DatasetGraphTxn)ds.asDatasetGraph() ;
        return dsgtxn.getTransactional() ;
        
        //return new AdapterTransactionalJenaToDBOE(ds) ;
    }

    private static Transactional transactional(DatasetGraph dsg) {
        // adapter across "Transactional"
        DatasetGraphTxn dsgtxn = (DatasetGraphTxn)dsg ;
        return dsgtxn.getTransactional() ;
        
        //return new AdapterTransactionalJenaToDBOE(ds) ;
    }

    private static class AdpaterTransactionalDBOEToJena implements org.apache.jena.sparql.core.Transactional {
        private final Transactional transactional ;
        AdpaterTransactionalDBOEToJena(Transactional transactional) {
            this.transactional = transactional ;
        }
        @Override
        public void begin(ReadWrite readWrite) { transactional.begin(readWrite); }
    
        @Override
        public void commit() { transactional.commit(); }
    
        @Override
        public void abort() { transactional.abort(); }
    
        @Override
        public void end() {}
        @Override
        public boolean isInTransaction() {
            throw new UnsupportedOperationException("isInTransaction") ;
        }
        
    }
}

