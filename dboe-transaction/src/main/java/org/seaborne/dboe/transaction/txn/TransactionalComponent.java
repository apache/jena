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

package org.seaborne.dboe.transaction.txn;

import java.nio.ByteBuffer ;

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
* Transctions to discard are not notified, only fully commited trasnaction are
* notified during recovery. The component may need to keepit's own record of
* undo actions needed across restarts.
* <p><br/>
* Lifecycle of startup:
* <ul>
* <li>{@link #startRecovery}
* <li>{@link #recover} for each commited/durable transaction (redo actions)
* <li>{@link #finishRecovery}, discarding any othe transactions (undo actions).
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
    public ComponentId getComponentId() ;

    // ---- Recovery phase
    public void startRecovery() ;
    
    /** Notification that {@code ref} was really committed.
     *  
     * @param ref Same bytes as were written during prepare originally.
     */
    public void recover(ByteBuffer ref) ;
    
    public void finishRecovery() ;

    // ---- Normal operation
    
    /** Start a transaction; return an identifier for this components use. */ 
    public void begin(Transaction transaction) ;
    
    /** Prepare for a commit.
     *  Returns some bytes that will be written to the journal.
     *  The journal remains valid until {@link #commitEnd} is called.
     */
    public ByteBuffer commitPrepare(Transaction transaction) ;

    /** Commit a transaction (make durable).
     * Other components not have been commited yet and recovery may occur still.
     * Permanet state should not be finalised until {@link #commitEnd}.
     */
    public void commit(Transaction transaction) ;
    
    /** Signal all commits on all components are done (the component can clearup now) */  
    public void commitEnd(Transaction transaction) ;

    /** Abort a transaction (undo the effect of a transaction) */   
    public void abort(Transaction transaction) ;

    /** Finalization - the coordinator will not mention the transaction again
     *  although recovery after a crash may do.
     */
    public void complete(Transaction transaction) ;
    
    // ---- End of operations
    
    /** Shutdown component, aborting any in-progress transactions.
     * This operation is not guaranteed to be called.
     */
    public void shutdown() ;
    
}
