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

import org.apache.jena.dboe.transaction.Transactional;

/** Implementation side of a {@link Transactional}.
 *  {@link Transactional} presents the application facing view
 *  whereas this has all the possible steps of an implementation.
 *  Normally, the implementation of {@link #commit} is split up.
 */
public interface TransactionalSystem extends Transactional {

    @Override
    public default void commit() {
        commitPrepare();
        commitExec();
    }

    /** Do the 2-phase "prepare" step after which
     *  the transaction coordinator decides whether to commit
     *  or abort.  A TransactionalSystem must be prepared for
     *  both possibilities.
     */
    public void commitPrepare();

    /** Do the 2-phase "commit" step */
    public void commitExec();

    /** Suspend this transaction, detaching from the current thread.
     * A new transaction on this thread can performed but the detached
     * transaction still exists and if it is a write transaction
     * it can still block other write transactions.
     */
    public TransactionCoordinatorState detach();

    /**
     * Attach a transaction to this thread.
     * A transaction system implementation usually imposes a rule that
     * only one thread can have a transaction attached at a time.
     */
    public void attach(TransactionCoordinatorState coordinatorState);

    /** Get the associated {@link TransactionCoordinator} */
    public TransactionCoordinator getTxnMgr();

    /** Return an information view of the transaction for this thread, if any.
     *  Returns null when there is no active transaction for this tread.
     */
    public TransactionInfo getTransactionInfo();

    /** Return the transaction object for this thread.
     *  Low-level use only.
     *  To get information about the current transaction, call {@link #getTransactionInfo}.
     */
    public Transaction getThreadTransaction();
}

