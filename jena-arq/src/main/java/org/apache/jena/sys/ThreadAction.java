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

package org.apache.jena.sys;

import java.util.Objects ;
import java.util.concurrent.Executor ;
import java.util.concurrent.Executors ;
import java.util.concurrent.Semaphore ;
import java.util.concurrent.atomic.AtomicReference ;

import org.apache.jena.atlas.logging.Log ;

/**
 * An action that will happen on a different thread later when {@link #run}
 * is called. A thread is created and started during a call to the
 * {#link create()}. The associated Runnable is called when {@link #run}
 * is called.
 */ 
public class ThreadAction {
    private final Semaphore semaStart   = new Semaphore(0, true) ;
    private final Semaphore semaFinish  = new Semaphore(0, true) ;
    
    // Catch the two kinds that do not need a "throws" clause. 
    private final AtomicReference<RuntimeException> thrownRuntimeException = new AtomicReference<>(null) ; 
    private final AtomicReference<Error> thrownError = new AtomicReference<>(null) ;
    private final Runnable action ;
    
    private ThreadAction(Runnable action) {
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
        // Wait for it to finish.
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

    /** Create a {@code ThreadAction}.
     * @param action The main action run when {@link #run()} called.
     * @return ThreadAction
     */
    public static ThreadAction create(Runnable action) {
        return create(null, action, null) ; 
    }
    
    /** Create a {@code ThreadAction}.  
     * 
     * @param before
     *      Action to call as the thread starts before {@link #run()}.
     *      Can be null.
     * @param action 
     *      The main action run when {@link #run()} called.
     *      Any exceptions are passed over to {@link #run()} 
     *      and propagated on the {@link #run()} thread. 
     * @param after  
     *      Action to run after the main action.
     *      Can be null.
     * @return ThreadAction
     */
    public static ThreadAction create(Runnable before, Runnable action, Runnable after) {
        Objects.requireNonNull(action) ;
        
        ThreadAction threadAction = new ThreadAction(action) ;
        // Startup semaphore so that the thread has started and entered the
        // transaction by the time we exit this setup function. 
        Semaphore semaCreateStart = new Semaphore(0, true) ;
        executor.execute( ()-> {
            try { 
                if ( before != null )
                    before.run();
            } catch (Throwable th) {
                Log.warn(ThreadAction.class, "Throwable in 'before' action: "+th.getMessage(), th);
                semaCreateStart.release() ;
                threadAction.semaFinish.release() ;
                return ;
            }
            // Signal the creator (see below) that the action has started.
            semaCreateStart.release() ;

            // Wait for the signal to run the action.
            threadAction.semaStart.acquireUninterruptibly();

            try {
                // Perform the action, catching and recording any RuntimeException or Error. 
                threadAction.trigger() ;
            }
            catch (Throwable ex) {
                // Suppress. trigger() recorded it and it is passed
                // to the caller in run(). 
            }
            try { 
                if ( after != null )
                    after.run() ;
            } catch (Throwable th) {
                Log.warn(ThreadAction.class, "Throwable in 'after' action: "+th.getMessage(), th);
                // Drop through.
            }
            threadAction.semaFinish.release() ;
        }) ;
        // Don't return until the thread has started.
        semaCreateStart.acquireUninterruptibly();
        return threadAction ;
    }
}
