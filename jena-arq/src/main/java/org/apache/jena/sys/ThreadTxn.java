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

import org.apache.jena.query.TxnType;
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

    /** Create a thread-backed delayed transaction action. 
     * Call {@link ThreadAction#run} to perform the read transaction.
     */
    public static ThreadAction threadTxn(Transactional trans, TxnType txnType, Runnable action) {
        return create(trans, txnType, action, true, true) ;
    }


    /** Create a thread-backed delayed READ transaction action. 
     * Call {@link ThreadAction#run} to perform the read transaction.
     */
    public static ThreadAction threadTxnRead(Transactional trans, Runnable action) {
        return threadTxn(trans, TxnType.READ, action) ;
    }

    /** Create a thread-backed delayed WRITE action.
     * Call {@link ThreadAction#run} to perform the write transaction.
     * (If called from inside a write transaction on the {@code trans},
     * this will deadlock.)
     */
    public static ThreadAction threadTxnWrite(Transactional trans, Runnable action) {
        return threadTxn(trans, TxnType.WRITE, action) ;
    }
   
    /** Create a thread-backed delayed WRITE-abort action (mainly for testing). */
    public static ThreadAction threadTxnWriteAbort(Transactional trans, Runnable action) {
        return create(trans, TxnType.WRITE, action, true, false) ;
    }

    private static ThreadAction create(Transactional trans, TxnType txnType, Runnable action, boolean isCommitBefore,  boolean isCommitAfter) {
        return ThreadAction.create
            ( beforeAction(trans, txnType, isCommitBefore)
            , action
            , afterAction(trans, txnType, isCommitAfter) ) ;
    }
    
    private static Runnable beforeAction(Transactional trans, TxnType txnType, boolean isCommit) {
        return ()-> trans.begin(txnType) ;
    }
    
    private static Runnable afterAction(Transactional trans, TxnType txnType, boolean isCommit) {
        return () -> {
            // Finish transaction (if no throwable)
            switch (txnType) {
                case WRITE :
                case READ_COMMITTED_PROMOTE :
                case READ_PROMOTE :
                {
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
