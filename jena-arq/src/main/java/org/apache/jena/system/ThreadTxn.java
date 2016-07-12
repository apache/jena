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

package org.apache.jena.system;

import java.util.Objects ;
import java.util.concurrent.Executor ;
import java.util.concurrent.Executors ;
import java.util.concurrent.Semaphore ;
import java.util.concurrent.atomic.AtomicReference ;

import org.apache.jena.query.ReadWrite ;
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
    // ---- Thread

    /** Create a thread-backed delayed READ transaction action. 
     * Call {@link ThreadTxn#run} to perform the read transaction.
     */
    public static ThreadTxn threadTxnRead(Transactional trans, Runnable action) {
        return ThreadTxn.create(trans, ReadWrite.READ, action, false) ;
    }

    /** Create a thread-backed delayed WRITE action.
     * Call {@link ThreadTxn#run} to perform the write transaction.
     * (If called from inside a write transaction on the {@code trans},
     * this will deadlock.)
     */
    public static ThreadTxn threadTxnWrite(Transactional trans, Runnable action) {
        return ThreadTxn.create(trans, ReadWrite.WRITE, action, true) ;
    }
   
    /** Create a thread-backed delayed WRITE-abort action (mainly for testing). */
    public static ThreadTxn threadTxnWriteAbort(Transactional trans, Runnable action) {
        return ThreadTxn.create(trans, ReadWrite.WRITE, action, false) ;
    }
    
    private final Semaphore semaStart   = new Semaphore(0, true) ;
    private final Semaphore semaFinish  = new Semaphore(0, true) ;
    
    // Catch the two kinds that do not need a "throws" clause. 
    private final AtomicReference<RuntimeException> thrownRuntimeException = new AtomicReference<>(null) ; 
    private final AtomicReference<Error> thrownError = new AtomicReference<>(null) ;
    private final Runnable action ;
    
    private ThreadTxn(Runnable action) {
        this.action = action ;
    }
    
    /**
     * Perform the Runnable, reporting any 
     * {@link java.lang.RuntimeException} or {@link java.lang.Error}
     */
    public void run() { 
        // Signal the thread, which is already running and inside
        // the transaction, can now call the action.
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
    
    // System-shared executor better.
    private static Executor executor = Executors.newCachedThreadPool() ;
    
    /*package*/ static ThreadTxn create(Transactional trans, ReadWrite mode, Runnable action, boolean isCommit) {
        Objects.requireNonNull(trans) ;
        Objects.requireNonNull(mode) ;
        Objects.requireNonNull(action) ;
        
        ThreadTxn threadAction = new ThreadTxn(action) ;
        // Startup semaphore so that the thread has started and entered the
        // transaction by the time we exit this setup function. 
        Semaphore semaCreateStart = new Semaphore(0, true) ;
        executor.execute( ()-> {
            // NB. trans.begin then semaCreateStartup.release() ;
            // This ensures that the transaction has really started.
            trans.begin(mode) ;
            
            // Signal the creator (see below) that the transaction has started.
            semaCreateStart.release() ;
            
            // Wait for the signal to run the action.
            threadAction.semaStart.acquireUninterruptibly();
            
            try {
                // Perform the action, catching and recording any RuntimeException or Error. 
                threadAction.trigger() ;
                
                // Finish transaction (if no throwable)
                switch (mode) {
                    case WRITE : {
                        if ( isCommit )
                            trans.commit() ;
                        else
                            trans.abort() ;
                        trans.end() ;
                    }
                    case READ : {
                        if ( isCommit )
                            trans.commit() ;
                        trans.end() ;
                    }
                }
            }
            catch (Throwable ex) {
                // Surpress. trigger() recorded it and it is passed
                // to the caller in run(). 
            }
            finally { threadAction.semaFinish.release() ; }
        }) ;
        // Don't return until the transaction has started.
        semaCreateStart.acquireUninterruptibly();
        return threadAction ;
    }
}
