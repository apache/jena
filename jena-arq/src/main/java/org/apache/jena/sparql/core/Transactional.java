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

package org.apache.jena.sparql.core;

import org.apache.jena.query.ReadWrite ;
import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.JenaTransactionException;
import org.apache.jena.system.Txn;

/** Interface that encapsulates the  begin/abort|commit/end operations.
 * <p>The read lifecycle is:
 * <pre> begin(READ) ... end()</pre>
 * <p>{@code commit} and {@code abort} are allowed. 
 * <p>The write lifecycle is:
 * <pre> begin(WRITE) ... abort() or commit()</pre>
 * <p>{@code end()} is optional but preferred.
 * <p>
 * Helper code is available {@link Txn} so, for example:
 * <pre>Txn.executeRead(dataset, ()-> { ... sparql query ... });</pre> 
 * <pre>Txn.executeWrite(dataset, ()-> { ... sparql update ... });</pre>
 * or use one of <tt>Txn.calculateRead</tt> and <tt>Txn.executeWrite</tt>
 * to return a value for the transaction block.
 * <p>
 * Directly called, code might look like:
 * <pre>
 *     Transactional object = ...
 *     object.begin(TxnMode.READ) ;
 *     try {
 *       ... actions inside a read transaction ...
 *     } finally { object.end() ; }
 * </pre>
 * or
 * <pre>
 *     Transactional object = ...
 *     object.begin(TxnMode.WRITE) ;
 *     try {
 *        ... actions inside a write transaction ...
 *        object.commit() ;
 *     } finally {
 *        // This causes an abort if {@code commit} has not been called.
 *        object.end() ;
 *     }
 * </pre>
 */

public interface Transactional 
{
    /**
     * Start a transaction which is READ mode and which will switch to WRITE if an update
     * is attempted but only if no intermediate transaction has performed an update. 
     * <p>
     * See {@link #begin(TxnType)} for more details an options.
     * <p>
     * May not be implemented. See {@link #begin(ReadWrite)} is guaranteed to be provided.
     */
    public default void begin() { begin(TxnType.READ_PROMOTE); }
    
    /**
     * Start a transaction.<br/>
     * READ or WRITE transactions start in that state and do not change for the
     * lifetime of the transaction.
     * <ul>
     * 
     * <li>{@code WRITE}: this guarantees a WRITE will complete if {@code commit()} is
     * called. The same as {@code begin(ReadWrite.WRITE)}.
     * 
     * <li>{@code READ}: the transaction can not promote to WRITE,ensuring read-only
     * access to the data. The same as {@code begin(ReadWrite.READ)}.
     * 
     * <li>{@code READ_PROMOTE}: the transaction will go from "read" to "write" if an
     * update is attempted and if the dataset has not been changed by another write
     * transaction. See also {@link #promote}.
     * 
     * <li>{@code READ_COMMITTED_PROMOTE}: Use this with care. The promotion will
     * succeed but changes from other transactions become visible.
     * 
     * </ul>
     * 
     * Read committed: at the point transaction attempts promotion from "read" to
     * "write", the system checks if the dataset has change since the transaction started
     * (called {@code begin}). If {@code READ_PROMOTE}, the dataset must not have
     * changed; if {@code READ_COMMITTED_PROMOTE} any intermediate changes are
     * visible but the application can not assume any data it has read in the
     * transaction is the same as it was at the point the transaction started.
     * <p>
     * This operation is optional and some implementations may throw
     * a {@link JenaTransactionException} exception for some or all {@link TxnType} values.
     * <p> 
     * See {@link #begin(ReadWrite)} for a form that is required of implementations.
     */
    public void begin(TxnType type);
    
    /** Start either a READ or WRITE transaction. */ 
    public void begin(ReadWrite readWrite) ;
    
    /**
     * Attempt to promote a transaction from "read" to "write" and the transaction
     * start with a "promote" mode ({@code READ_PROMOTE} or {@code READ_COMMITTED_PROMOTE}).
     * <p>
     * Returns "true" if the transaction is in write mode after the call.
     * The method always succeeds of the transaction is already
     * "write".
     * <p>
     * This method returns true if a {@code READ_PROMOTE} or {@code READ_COMMITTED_PROMOTE} is promoted.
     * <p>
     * This method returns false if a {@code READ_PROMOTE} can't be promoted - the transaction is still valid and in "read" mode. 
     * <p>
     * This method throws an exception if there is an attempt to promote a "READ" transaction. 
     */
    public boolean promote();

    /** Commit a transaction - finish the transaction and make any changes permanent (if a "write" transaction) */  
    public void commit() ;
    
    /** Abort a transaction - finish the transaction and undo any changes (if a "write" transaction) */  
    public void abort() ;

    /** Finish the transaction - if a write transaction and commit() has not been called, then abort */  
    public void end() ;

    /** Return the current mode of the transaction - "read" or "write" */ 
    public ReadWrite transactionMode();

    /** Return the transaction type used in {@code begin(TxnType)}. */ 
    public TxnType transactionType();
    //public default TxnType transactionType() { throw new JenaTransactionException("Not implemented"); }

    /** Say whether inside a transaction. */ 
    public boolean isInTransaction() ;
}
