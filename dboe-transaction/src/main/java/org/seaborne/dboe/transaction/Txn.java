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

package org.seaborne.dboe.transaction;

import java.util.function.Supplier ;

import org.apache.jena.query.ReadWrite ;
import org.apache.jena.sparql.core.Transactional ;

/** Application utilities for transactions. */
public class Txn {
    
    /** Execute the Runnable in a read transaction.
     *  Nested transactions are not supported.
     */
    public static <T extends Transactional> void executeRead(T txn, Runnable r) {
        txn.begin(ReadWrite.READ) ;
        try {  
            r.run(); 
        } finally { txn.end() ; } 
    }

    /** Execute and return a value in a read transaction
     * Nested transactions are not supported.
     */

    public static <T extends Transactional, X> X executeReadReturn(T txn, Supplier<X> r) {
        try {  
            txn.begin(ReadWrite.READ) ;
            return r.get() ;
        } finally { txn.end() ; } 
    }

    /** Execute the Runnable in a write transaction 
     *  Nested transaction are not supported.
     */
    public static <T extends Transactional> void executeWrite(T txn, Runnable r) {
        txn.begin(ReadWrite.WRITE) ;
        try {
            r.run();
            txn.commit();
        } catch (Throwable th) {
            txn.abort();
            throw th ; 
        } finally { txn.end(); }
    }
    
    /** Execute the Runnable in a write transaction 
     *  Nested transaction are not supported.
     */
    public static <T extends Transactional, X> X executeWriteReturn(Transactional txn, Supplier<X> r) {
        txn.begin(ReadWrite.WRITE) ;
        try { 
            X x = r.get() ;
            txn.commit() ;
            return x ;
        } catch (Throwable th) {
            txn.abort();
            throw th ; 
        } finally { txn.end(); }
    }
    
    // ---- Thread
    
    /** Create a thread-backed delayed READ transaction action. */
    public static ThreadTxn threadTxnRead(Transactional trans, Runnable action) {
        return ThreadTxn.create(trans, ReadWrite.READ, action, false) ;
    }
    
    /** Create a thread-backed delayed WRITE  action.
     * If called from inside a write transaction on the {@code trans},
     * this will deadlock.
     */
    public static ThreadTxn threadTxnWrite(Transactional trans, Runnable action) {
        return ThreadTxn.create(trans, ReadWrite.WRITE, action, true) ;
    }
    
    /** Create a thread-backed delayed WRITE-abort action (testing). */
    public static ThreadTxn threadTxnWriteAbort(Transactional trans, Runnable action) {
        return ThreadTxn.create(trans, ReadWrite.WRITE, action, false) ;
    }
}

