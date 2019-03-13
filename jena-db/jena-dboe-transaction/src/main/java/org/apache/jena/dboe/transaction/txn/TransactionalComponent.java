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

import java.nio.ByteBuffer;

/** Interface that for components of a transaction system.
* <p><br/>
* The {@link TransactionCoordinator} manages a number of components
* which provide the {@link TransactionalComponent} interface.
* <p><br/>
* When a new coordinator starts, typically being when the in-process system starts,
* there is a recovery phase when work from a previous coordinator is recovered.
* Transactions were either were properly committed by the previous coordinator,
* and hence redo actions (finalization) should be done,
* or they were not, in which case undo actions may be needed.
* Transctions to discard are not notified, only fully committed transaction are
* notified during recovery. The component may need to keepit's own record of
* undo actions needed across restarts.
* <p><br/>
* Lifecycle of startup:
* <ul>
* <li>{@link #startRecovery}
* <li>{@link #recover} for each commited/durable transaction (redo actions)
* <li>{@link #finishRecovery}, discarding any other transactions (undo actions).
* </ul>
* <p><br/>
* Lifecycle of a read transaction:
* <ul>
* <li>{@link #begin}
* <li>{@link #complete}
* </ul>
* <br/>
* A read transaction may also include {@code commit} or {@code abort} lifecycles.
* {@link #commitPrepare} and {@link #commitEnd} are not called.
*<p><br/>
* Lifecycle of a write transaction:
* <li>{@link #begin}
* <li>{@link #commitPrepare}
* <li>{@link #commit} or {@link #abort}
* <li>{@link #commitEnd}
* <li>{@link #complete} including abort
* </ul>
* <br/>
* or if the application aborts the transaction:
* <ul>
* <li>{@link #begin}
* <li>{@link #abort}
* <li>{@link #complete}
* </ul>
* <p>
* {@link #complete} may be called out of sequence and it forces an abort if before
* {@link #commitPrepare}. Once {@link #commitPrepare} has been called, the component
* can not decide whether to commit finally or to cause a system abort; it must wait
* for the coordinator. After {@link #commitEnd}, the coordinator has definitely
* commited the overall transaction and local prepared state can be released, and changes
* made to the permanent state of the component.
*
* @see Transaction
* @see TransactionCoordinator
*/

public interface TransactionalComponent
{
    /**
     * Every component <i>instance</i> must supplied a unique number.
     * It is used to route journal entries to subsystems, including across restarts/recovery.
     * Uniqueness scope is within the same {@link TransactionCoordinator},
     * and the same across restarts.
     * <p>
     * If a component imposes the rule of one-per-{@link TransactionCoordinator},
     * the same number can be used (if different from all other component type instances).
     * <p>
     * If a component can have multiple instances per {@link TransactionCoordinator},
     * for example indexes, each must have a unique instance id.
     */
    public ComponentId getComponentId();

    // ---- Recovery phase
    public void startRecovery();

    /** Notification that {@code ref} was really committed and is being recovered.
     *
     * @param ref Same bytes as were written during prepare originally.
     */
    public void recover(ByteBuffer ref);

    /** End of the recovery phase */
    public void finishRecovery();

    /** Indicate that no recovery is being done (the journal thinks everything was completed last time) */
    public void cleanStart();

    // ---- Normal operation

    /** Start a transaction; return an identifier for this components use. */
    public void begin(Transaction transaction);

    /** Promote a component in a transaction.
     * <p>
     *  May return "false" for "can't do that" if the transaction can not be promoted.
     *  <p>
     *  May throw {@link UnsupportedOperationException} if promotion is not supported.
     */
    public boolean promote(Transaction transaction);

    /** Prepare for a commit.
     *  Returns some bytes that will be written to the journal.
     *  The journal remains valid until {@link #commitEnd} is called.
     */
    public ByteBuffer commitPrepare(Transaction transaction);

    /** Commit a transaction (make durable).
     * Other components not have been commited yet and recovery may occur still.
     * Permanent state should not be finalised until {@link #commitEnd}.
     */
    public void commit(Transaction transaction);

    /** Signal all commits on all components are done (the component can clearup now) */
    public void commitEnd(Transaction transaction);

    /** Abort a transaction (undo the effect of a transaction) */
    public void abort(Transaction transaction);

    /** Finalization - the coordinator will not mention the transaction again
     *  although recovery after a crash may do so.
     */
    public void complete(Transaction transaction);

    // ---- End of operations

    /** Detach this component from the transaction of the current thread
     * and return some internal state that can be used in a future call of
     * {@link #attach(SysTransState)}
     * <p>
     * After this call, the component is not in a transaction but the
     * existing transaction still exists. The thread may start a new
     * transaction; that transaction is completely independent of the
     * detached transaction.
     * <p>
     * Returns {@code null} if the current thread not in a transaction.
     * The component may return null to indicate it has no state.
     * The return system state should be used in a call to {@link #attach(SysTransState)}
     * and the transaction ended in the usual way.
     *
     */
    public SysTransState detach();

    /** Set the current thread to be in the transaction.  The {@code systemState}
     * must be obtained from a call of {@link #detach()}.
     * This method can only be called once per {@code systemState}.
     */
    public void attach(SysTransState systemState);

    /** Shutdown component, aborting any in-progress transactions.
     * This operation is not guaranteed to be called.
     */
    public void shutdown();

}
