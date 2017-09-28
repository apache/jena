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

import java.util.Objects ;
import java.util.concurrent.Executor ;
import java.util.concurrent.Semaphore ;
import java.util.concurrent.atomic.AtomicReference ;

import org.apache.jena.query.ReadWrite ;

import org.seaborne.dboe.sys.Sys ;
import org.apache.jena.sparql.core.Transactional ;

/**
 * An action that will happen on a different thread later when {@link #run} is
 * called. A thread is created and the transaction started during a call to the
 * creation operations {@link #threadTxnRead} or {@link #threadTxnWrite}.
 * The associated Runnable is called and the transaction completed when
 * {@link #run} is called. Being on a thread, the state of the world the
 * forked transaction sees is outside the creating thread which may itself be in a
 * transaction. Warning: creating a write transaction inside a write transaction
 * will cause deadlock.
 */ 
public class ThreadTxn {
    
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
    
    private final Semaphore semaStart ;
    private final Semaphore semaFinish ;
    private final AtomicReference<RuntimeException> thrownRuntimeException = new AtomicReference<>(null) ; 
    private final AtomicReference<Error> thrownError = new AtomicReference<>(null) ;
    private final Runnable action ;
    
    private ThreadTxn(Runnable action) {
        this.action = action ;
        this.semaStart = new Semaphore(0, true) ;
        this.semaFinish = new Semaphore(0, true) ;
    }
    
    /**
     * Perform the Runnable, reporting any 
     * {@link java.lang.RuntimeException} or {@link java.lang.Error}
     */
    public void run() { 
        semaStart.release();
        semaFinish.acquireUninterruptibly() ;
        if ( thrownError.get() != null )
            throw thrownError.get() ;
        if ( thrownRuntimeException.get() != null )
            throw thrownRuntimeException.get() ;
    }
    
    // Called on the async thread.
    private void trigger() {
        try { action.run(); }
        catch (Error error) { thrownError.set(error) ; throw error  ;}
        catch (RuntimeException ex) { thrownRuntimeException.set(ex) ; throw ex ; }
    }
    
    // System-shared executor.
    private static Executor executor = Sys.executor ;
    
    /*package*/ static ThreadTxn create(Transactional trans, ReadWrite mode, Runnable action, boolean isCommit) {
        Objects.requireNonNull(trans) ;
        Objects.requireNonNull(mode) ;
        Objects.requireNonNull(action) ;
        
        ThreadTxn threadAction = new ThreadTxn(action) ;
        // Startup semaphore so that the thread has started by the
        // time we exit this setup function. 
        Semaphore semaStartup = new Semaphore(0, true) ;
        executor.execute( ()-> {
            // NB. trans.begin then semaStartup.release() ;
            // This ensures that the transaction has really started.
            trans.begin(mode) ;
            
            // Signal the creator (see below) that the transaction has started.
            semaStartup.release() ;
            
            // Wait for the signal to run the action.
            threadAction.semaStart.acquireUninterruptibly();
            
            try {
                // Performane the action, catch and record any RuntimeException or Error. 
                threadAction.trigger() ;
                
                // Finish transaction (if no throwable)
                if ( mode == ReadWrite.WRITE ) {
                    if ( isCommit )
                        trans.commit();
                    else
                        trans.abort() ;
                    trans.end() ;
                } else {
                    // Read
                    if ( isCommit )
                        trans.commit();
                    trans.end() ;
                }
            } 
            catch (Throwable ex) { 
                       // Suppress now it has trigger transaction mechanism in
                       // the presence of an unchecked exception.
                       // Passed to the main thread via ThreadTxn
            }
            finally { threadAction.semaFinish.release() ; }
        }) ;
        // Don't return until the transaction has started.
        semaStartup.acquireUninterruptibly();
        return threadAction ;
    }
}
