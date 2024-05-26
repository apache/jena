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

import java.util.function.Supplier;

import org.apache.jena.query.ReadWrite ;
import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.JenaTransactionException;
import org.apache.jena.system.Txn;

/** Interface that encapsulates the  begin/abort|commit/end operations.
 * <p>The read lifecycle is:
 * <pre> begin(READ) ... end()</pre>
 * <p>{@code commit} and {@code abort} are allowed.
 * <p>The write lifecycle is:
 * <pre> begin(WRITE) ... abort() or commit() end()</pre>
 * <p>{@code end()} is optional for "write" but is preferred.
 * </p>
 * <h4>Application use</h4>
 * Applications can conveniently execute the lifecycle with methods to read or write:
 * <pre>dataset.executeRead({@literal ()->} { ... sparql query ... });</pre>
 * <pre>dataset.executeWrite({@literal ()->} { ... sparql update ... });</pre>
 * <p>
 * Use one of {@code calculateRead} or {@code calculateWrite}
 * to return a value for the transaction block.
 * </p>
 * <h4>Core Functionality</h4>
 * Directly called, code might look like:
 * <pre>
 *     Transactional object = ...
 *     object.begin(TxnMode.READ) ;
 *     try {
 *       ... actions inside a read transaction ...
 *     } finally { object.end() ; }
 * </pre>
 *
 * <p>or</p>
 *
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
 *
 * @see Txn
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
    public default void begin(ReadWrite readWrite) {
        begin(TxnType.convert(readWrite));
    }

    /**
     * Attempt to promote a transaction from "read" to "write" when the transaction
     * started with a "promote" mode ({@code READ_PROMOTE} or
     * {@code READ_COMMITTED_PROMOTE}).
     * <p>
     * Returns "true" if the transaction is in write mode after the call. The method
     * always succeeds of the transaction is already "write".
     * <p>
     * A {@code READ_COMMITTED_PROMOTE} can always be promoted, but the call may need to
     * wait.
     * <p>
     * This method returns true if a {@code READ_PROMOTE} or
     * {@code READ_COMMITTED_PROMOTE} is promoted.
     * <p>
     * This method returns false if a {@code READ_PROMOTE} can't be promoted - the
     * transaction is still valid and in "read" mode. Any further calls to
     * {@code promote()} will also return false.
     * <p>
     * This method returns false if there is an attempt to promote a "READ" transaction.
     */
    public default boolean promote() {
        if ( transactionMode() == ReadWrite.WRITE )
            return true;
        TxnType txnType = transactionType();
        if ( txnType == null )
            throw new JenaTransactionException("txnType");
        switch(txnType) {
            case WRITE :                  return true;
            case READ :                   return false;
            case READ_PROMOTE :           return promote(Promote.ISOLATED);
            case READ_COMMITTED_PROMOTE : return promote(Promote.READ_COMMITTED);
        }
        throw new JenaTransactionException("Can't determine promote '"+txnType+"'transaction");
    }

    public enum Promote { ISOLATED, READ_COMMITTED } ;

    /**
     * Attempt to promote a transaction from "read" mode to "write" and the transaction. This
     * method allows the form of promotion to be specified. The transaction must not have been started
     * with {@code READ}, which is read-only.
     * <p>
     * An argument of {@code READ_PROMOTE} treats the promotion as if the transaction was started
     * with {@code READ_PROMOTE} (any other writer commiting since the transaction started
     * blocks promotion) and {@code READ_COMMITTED_PROMOTE} treats the promotion as if the transaction was started
     * with {@code READ_COMMITTED_PROMOTE} (intemediate writer commits become visible).
     * <p>
     * Returns "true" if the transaction is in write mode after the call. The method
     * always succeeds of the transaction is already "write".
     * <p>
     * This method returns true if a {@code READ_PROMOTE} or
     * {@code READ_COMMITTED_PROMOTE} is promoted.
     * <p>
     * This method returns false if a {@code READ_PROMOTE} can't be promoted - the
     * transaction is still valid and in "read" mode.
     * <p>
     * This method throws an exception if there is an attempt to promote a {@code READ}
     * transaction.
     */
    public boolean promote(Promote mode);

    /** Commit a transaction - finish the transaction and make any changes permanent (if a "write" transaction) */
    public void commit() ;

    /** Abort a transaction - finish the transaction and undo any changes (if a "write" transaction) */
    public void abort() ;

    /** Finish the transaction - if a write transaction and commit() has not been called, then abort */
    public void end() ;

    /** Return the current mode of the transaction - "read" or "write".
     * If the caller is not in a transaction, this method returns null.
     */
    public ReadWrite transactionMode();

    /** Return the transaction type used in {@code begin(TxnType)}.
     * If the caller is not in a transaction, this method returns null.
     */
    public TxnType transactionType();

    /** Say whether inside a transaction. */
    public boolean isInTransaction() ;

    /**
     * Execute application code in a transaction with the given {@link TxnType
     * transaction type}. See {@link Txn#exec}.
     */
    public default void exec(TxnType txnType, Runnable action) { Txn.exec(this, txnType, action); }

    /**
     * Execute and return a value in a transaction with the given {@link TxnType
     * transaction type}. See {@link Txn#calc}.
     */
    public default <T> T calc(TxnType txnType, Supplier<T> action) { return Txn.calc(this, txnType, action); }

    /**
     * Execute in a "read" transaction that can promote to "write".
     * <p>
     * Such a transaction may abort if an update is executed
     * by another thread before this one is promoted to "write" mode.
     * If so, the data protected by {@code txn} is unchanged.
     * <p>
     * If the application knows updates will be needed, consider using {@link #executeWrite}
     * which starts in "write" mode.
     * <p>
     * The application code can call {@link Transactional#promote} to attempt to
     * change from "read" to "write"; the {@link Transactional#promote promote} method
     * returns a boolean indicating whether the promotion was possible or not.
     */
    public default void execute(Runnable r) { Txn.execute(this, r); }

    /**
     * Execute in a "read" transaction that can promote to "write" and return some calculated value.
     * <p>
     * Such a transaction may abort if an update is executed
     * by another thread before this one is promoted to "write" mode.
     * If so, the data protected by {@code txn} is unchanged.
     * <p>
     * If the application knows updates will be needed, consider using {@link #executeWrite}
     * which starts in "write" mode.
     * <p>
     * The application code can call {@link Transactional#promote} to attempt to
     * change from "read" to "write"; the {@link Transactional#promote promote} method
     * returns a boolean indicating whether the promotion was possible or not.
     */
    public default <X> X calculate(Supplier<X> r) { return Txn.calculate(this, r); }

    /** Execute in a read transaction */
    public default <T extends Transactional> void executeRead(Runnable r) { Txn.executeRead(this, r); }

    /** Execute and return a value in a read transaction */
    public default <X> X calculateRead(Supplier<X> r) { return Txn.calculateRead(this, r); }

    /** Execute the Runnable in a write transaction */
    public default void executeWrite(Runnable r) { Txn.executeWrite(this, r); }

    /** Execute and return a value in a write transaction. */
    public default <X> X calculateWrite(Supplier<X> r) { return Txn.calculateWrite(this, r); }
}
