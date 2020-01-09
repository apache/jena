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

package org.apache.jena.dboe.transaction.txn;

/** Call backs for the transaction lifecycle. */ 
public interface TransactionListener {
    
    /** A transaction has started; begin has done the setup. */
    public default void notifyTxnStart(Transaction transaction) { }

    /** Start a call to promote */
    public default void notifyPromoteStart(Transaction transaction) { }
    /** Finish a call to promote */
    public default void notifyPromoteFinish(Transaction transaction) { }

    /** Start prepare during a commit */
    public default void notifyPrepareStart(Transaction transaction) { }
    /** Finish prepare during a commit */
    public default void notifyPrepareFinish(Transaction transaction) { }

    /** Start a commit (prepare has been done) */
    public default void notifyCommitStart(Transaction transaction) { }
    /** Finish a commit (prepare has been done) */
    public default void notifyCommitFinish(Transaction transaction) { }

    /** Start an abort */
    public default void notifyAbortStart(Transaction transaction) { }
    /** Start finish an abort */
    public default void notifyAbortFinish(Transaction transaction) { }

    /** Start an end() */
    public default void notifyEndStart(Transaction transaction) { }
    /** Finish an end() */
    public default void notifyEndFinish(Transaction transaction) { }

    /** Start the complete step. */
    public default void notifyCompleteStart(Transaction transaction) { }
    /** Finish the complete step. */
    public default void notifyCompleteFinish(Transaction transaction) { }
    
    /** Transaction has finished. This is called during "complete" */
    public default void notifyTxnFinish(Transaction transaction) { }
}
