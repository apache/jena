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

package org.seaborne.dboe.transaction;

import java.util.concurrent.Semaphore ;
import java.util.concurrent.atomic.AtomicReference ;
import java.util.function.Supplier ;

import com.hp.hpl.jena.query.ReadWrite ;

/** Application utilities for transactions. */
public class Txn {
    
    /** Execute the Runnable in a read transaction.
     *  Nested transactions are not supported.
     */
    public static <T extends Transactional> void executeRead(T txn, Runnable r) {
        txn.begin(ReadWrite.READ) ;
        r.run(); 
        txn.end() ;
    }

    /** Execute and return a value in a read transaction
     * Nested transactions are not supported.
     */

    public static <T extends Transactional, X> X executeReadReturn(T txn, Supplier<X> r) {
        txn.begin(ReadWrite.READ) ;
        X x = r.get() ;
        txn.end() ;
        return x ;
    }

    /** Execute the Runnable in a write transaction 
     *  Nested transaction are not supported.
     */
    public static <T extends Transactional> void executeWrite(T txn, Runnable r) {
        txn.begin(ReadWrite.WRITE) ;
        try { r.run(); }
        catch (Throwable th) {
            txn.abort();
            txn.end();
            throw th ; 
        }
        txn.commit() ;
        txn.end() ;
    }
    
    /** Execute the Runnable in a write transaction 
     *  Nested transaction are not supported.
     */
    public static <T extends Transactional, X> X executeWriteReturn(Transactional txn, Supplier<X> r) {
        txn.begin(ReadWrite.WRITE) ;
        X x = r.get() ;
        txn.commit() ;
        txn.end() ;
        return x ;
    }
    
    // ---- Actions.
    
//    /** Trigger a thread action. */
//    private static void execThreadTxn(ThreadTxn action) {
//        action.semaStart.release();
//        action.semaFinish.acquireUninterruptibly();
//    }

    /** An action that wil happen on a different thread later (on .exec) 
     * where the transaction is created immediately.
     */ 
    public static class ThreadTxn {
        private final Semaphore semaStart ;
        private final Semaphore semaFinish ;
        private final AtomicReference<RuntimeException> thrownRuntimeException = new AtomicReference<>(null) ; 
        private final AtomicReference<Error> thrownError = new AtomicReference<>(null) ;
        private final Runnable action ;
        ThreadTxn(Runnable action) {
            this.action = action ;
            this.semaStart = new Semaphore(0, true) ;
            this.semaFinish = new Semaphore(0, true) ;
        }
        
        // Called on the thread.
        void trigger() {
            try { action.run(); }
            catch (Error error) { thrownError.set(error) ; throw error  ;}
            catch (RuntimeException ex) { thrownRuntimeException.set(ex) ; throw ex ; }
        }
        
        public void exec() { 
            semaStart.release();
            semaFinish.acquireUninterruptibly() ;
            if ( thrownError.get() != null )
                throw thrownError.get() ;
            if ( thrownRuntimeException.get() != null )
                throw thrownRuntimeException.get() ;
        }
    }
    
    /** Create a thread-backed delayed READ transaction action. */
    public static ThreadTxn threadTxnRead(Transactional trans, Runnable action) {
        // Startup semaphore so that the thread has started by the
        // time we exit this setup function. 
        ThreadTxn threadAction = new ThreadTxn(action) ;
        Semaphore semaStartup = new Semaphore(0, true) ; 
        new Thread( ()-> {
            // NB. trans.begin then semaStartup.release() ;
            // This ensures that the transaction has really started.
            // Maybe we can make a single Runnable to cover all 3 cases
            // if it takes a flag.
            trans.begin(ReadWrite.READ) ;
            // Signal the creator that the transaction has started.
            semaStartup.release() ;
            // Wait for the signal to run the action.
            threadAction.semaStart.acquireUninterruptibly() ;
            try {
                // Enact.
                threadAction.trigger() ;
                trans.end() ;
                // Signal action complete.
            } catch (Throwable ex) { 
                // Surpress now it has trigger transaction 
                // processing of exceptions.
                // Passed to the main thread via threadAction
            }
            finally { threadAction.semaFinish.release() ; }
            
        }).start() ;
        // Don't return until the transaction has started. 
        semaStartup.acquireUninterruptibly();
        return threadAction ;
    }
    
    /** Create a thread-backed delayed WRITE-commit action. */
    public static ThreadTxn threadTxnWriteCommit(Transactional trans, Runnable action) {
        ThreadTxn threadAction = new ThreadTxn(action) ;
        Semaphore semaStartup = new Semaphore(0, true) ; 
        new Thread( ()-> {
            trans.begin(ReadWrite.WRITE) ;
            semaStartup.release() ;
            threadAction.semaStart.acquireUninterruptibly() ;
            try {
                threadAction.trigger() ;
                trans.commit();
                trans.end() ;
            } 
            catch (Throwable ex) { }
            finally { threadAction.semaFinish.release() ; }
        }).start() ;
        semaStartup.acquireUninterruptibly();
        return threadAction ;
    }
    
    /** Create a thread-backed delayed WRITE-abort action. */
    public static ThreadTxn threadTxnWriteAbort(Transactional trans, Runnable action) {
        ThreadTxn threadAction = new ThreadTxn(action) ;
        Semaphore semaStartup = new Semaphore(0, true) ; 
        new Thread( ()-> {
            trans.begin(ReadWrite.WRITE) ;
            semaStartup.release() ;
            threadAction.semaStart.acquireUninterruptibly() ;
            try {
                threadAction.trigger() ;
                trans.abort();
                trans.end() ;
            } 
            catch (Throwable ex) { } 
            finally { threadAction.semaFinish.release() ; }
        }).start() ;
        semaStartup.acquireUninterruptibly();
        return threadAction ;
    }
    

}

