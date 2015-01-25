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

package org.seaborne.transaction.txn;

import java.nio.ByteBuffer ;


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
     * @param ref Same bytes as were written during prepare originally.
     */
    public void recover(ByteBuffer ref) ;
    
    public void finishRecovery() ;

    // ---- Normal operation
    
    /** Start a transaction; return an identifier for this components use. */ 
    public void begin(Transaction transaction) ;
    
    /** Prepare for a commit.
     *  Returns some bytes that will be written to the journal.
     */
    public ByteBuffer commitPrepare(Transaction transaction) ;

    /** Commit a transaction (make durable) */  
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
