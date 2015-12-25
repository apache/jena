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

package org.seaborne.tdb2.lib;

import java.util.function.Supplier ;

import org.apache.jena.query.Dataset ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.seaborne.dboe.transaction.ThreadTxn ;
import org.seaborne.dboe.transaction.Transactional ;
import org.seaborne.dboe.transaction.Txn ;
import org.seaborne.tdb2.store.DatasetGraphTxn ;

public class TDBTxn {
    // Long term - replace with Txn when Dataset and DatasetGraph have same Transactional  
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

    // Adapters for "Transactional". So as to use Txn operations, we need to get from
    // Dataset (and standard Jena) to Mantis/DBOE Transactional. 
    // DatasetGraphTxn combines standard Jena and Mantis/DBOE Transactional. 
    
    private static Transactional transactional(Dataset ds) {
        // DatasetGraphTxn combines standard Jenas and Mantis/DBOE Transactional. 
        DatasetGraphTxn dsgtxn = (DatasetGraphTxn)ds.asDatasetGraph() ;
        return dsgtxn ;
    }

    private static Transactional transactional(DatasetGraph dsg) {
        // adapter across "Transactional"
        // DatasetGraphTxn combines standard Jenas and Mantis/DBOE Transactional. 
        DatasetGraphTxn dsgtxn = (DatasetGraphTxn)dsg ;
        return dsgtxn ;
    }
}

