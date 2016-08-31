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

import org.apache.jena.query.ReadWrite ;
import org.apache.jena.sparql.core.Transactional ;

/**
 * An action that will happen on a different thread later when {@link ThreadAction#run} is
 * called. A thread is created and the transaction started during a call to the
 * creation operations {@link #threadTxnRead} or {@link #threadTxnWrite}.
 * The associated Runnable is called and the transaction completed when
 * {@link ThreadAction#run} is called. Being on a thread, the state of the world the
 * forked transaction sees is outside the creating thread which may itself be in a
 * transaction. Warning: creating a write transaction inside a write transaction
 * will cause deadlock.
 */ 
public class ThreadTxn {
    // ---- Thread

    /** Create a thread-backed delayed READ transaction action. 
     * Call {@link ThreadAction#run} to perform the read transaction.
     */
    public static ThreadAction threadTxnRead(Transactional trans, Runnable action) {
        return create(trans, ReadWrite.READ, action, false) ;
    }

    /** Create a thread-backed delayed WRITE action.
     * Call {@link ThreadAction#run} to perform the write transaction.
     * (If called from inside a write transaction on the {@code trans},
     * this will deadlock.)
     */
    public static ThreadAction threadTxnWrite(Transactional trans, Runnable action) {
        return create(trans, ReadWrite.WRITE, action, true) ;
    }
   
    /** Create a thread-backed delayed WRITE-abort action (mainly for testing). */
    public static ThreadAction threadTxnWriteAbort(Transactional trans, Runnable action) {
        return create(trans, ReadWrite.WRITE, action, false) ;
    }

    /*package*/ static ThreadAction create(Transactional trans, ReadWrite mode, Runnable action, boolean isCommit) {
        return ThreadAction.create
            ( beforeAction(trans, mode, isCommit)
            , action
            , afterAction(trans, mode, isCommit) ) ;
    }
    
    private static Runnable beforeAction(Transactional trans, ReadWrite mode, boolean isCommit) {
        return ()-> trans.begin(mode) ;
    }
    
    private static Runnable afterAction(Transactional trans, ReadWrite mode, boolean isCommit) {
        return () -> {
            // Finish transaction (if no throwable)
            switch (mode) {
                case WRITE : {
                    if ( isCommit )
                        trans.commit() ;
                    else
                        trans.abort() ;
                    trans.end() ;
                    break ;
                }
                case READ : {
                    if ( isCommit )
                        trans.commit() ;
                    trans.end() ;
                    break ;
                }
            }
        } ;
    }

}
