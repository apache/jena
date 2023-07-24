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

package org.apache.jena.dboe.storage.system;

import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.jena.dboe.transaction.txn.TransactionException;
import org.apache.jena.dboe.transaction.txn.TransactionalSystemControl;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphWrapper;
import org.apache.jena.sparql.util.Context;

/**
 * DatasetGraph wrapper controls entry and exit of transactions.
 * <ul>
 * <li>Exclusive access (no transactions active)
 * <li>Read only database - No possible writers (write and promote-able transactions)
 * </ul>
 */

public class DatasetGraphTxnCtl extends DatasetGraphWrapper implements TransactionalSystemControl {

    // ---- Controls

    // Multiple state X or single state Y.
    // Do not confuse with read/write transactions. We need a "one exclusive, or many
    // other" lock which happens to be called {@code ReadWriteLock}.

    // All transactions need "read" state X through out their lifetime.
    // The "write" state Y is used for exclusive mode.
    private ReadWriteLock exclusivitylock = new ReentrantReadWriteLock();

    // Lock to guarantee only readers are present.
    // Writers and promote transaction need to take a lock on entry.
    // This is not reentrant.
    private ReadWriteLock writeableDatabase = new ReentrantReadWriteLock();

    // Better Lock naming?
    //  MultipleOrSingle (MOS) == MRSW
    //  MultipleAndSingle (MAS) == MR+SW

    public DatasetGraphTxnCtl(DatasetGraph dsg) {
        super(dsg);
    }

    public DatasetGraphTxnCtl(DatasetGraph dsg, Context context) {
        super(dsg, context);
    }

    @Override
    public void begin(TxnType txnType) {
        Objects.nonNull(txnType);
        checkNotActive();
        enterTransaction(txnType, true);
        super.begin(txnType);
    }

    final
    protected void checkNotActive() {
        if ( isInTransaction() )
            throw new TransactionException(label("Currently in an active transaction"));
    }

    private String label(String msg) {
        // Optional labelling
        return msg;
    }

    final
    protected void checkActive() {
        // Check no transaction on this thread.
        if ( ! isInTransaction() )
            throw new TransactionException(label("Not in an active transaction"));
    }

    @Override
    public void begin(ReadWrite readWrite) {
        Objects.nonNull(readWrite);
        begin(TxnType.convert(readWrite));
    }

    /*
     * Admission policy.
     * - exclusive mode (no transactions running)
     * - possibleWriters
     */
    private void enterTransaction(TxnType txnType, boolean canBlock) {
        boolean canBlockWriters = canBlock; // false => "bounce writers mode"

        boolean bExclusive = tryNonExclusiveMode(canBlock);
        if ( !bExclusive )
            throw new TransactionException("Can't start transaction at the moment.");

        // Readers never block.
        // Writers admitted one at a time (provided by TransactionCoordinator presently)
        // Promotes are readers until they promote.
        switch(txnType) {
            case READ :
                break;
            case READ_COMMITTED_PROMOTE :
            case READ_PROMOTE :
            case WRITE : {
                startPossibleWriter();
                break;
            }
        }
    }

    private void exitTransaction(TxnType txnType) {
        switch(txnType) {
            case READ :
                break;
            case READ_COMMITTED_PROMOTE :
            case READ_PROMOTE :
            case WRITE : {
                finishPossibleWriter();
                break;
            }
        }
        finishNonExclusiveMode();
    }

    @Override
    public void commit() {
        TxnType txnType = transactionType();
        super.commit();
        exitTransaction(txnType);
    }

    @Override
    public void abort() {
        TxnType txnType = transactionType();
        super.abort();
        exitTransaction(txnType);
    }

    @Override
    public void end() {
        TxnType txnType = transactionType();
        super.end();
        if ( isInTransaction() )
            exitTransaction(txnType);
    }

    /**
     * Block until no writers or promote transactions present are present.
     */
    @Override
    public void startReadOnlyDatabase()  {
        startReadOnly();
    }

    /**
     * Release any waiting potential writers.
     */
    @Override
    public void finishReadOnlyDatabase() {
        finishReadOnly();
    }

    // Any writers - WRITE, READ_PROMOTE, READ_COMMITTED_PROMOTE
    // Writer take this lock (slightly confusingly as a "read" lock)
    // READ-ONLY operation do no tneed to take this block.

    protected void startReadOnly() {
        startReadOnly(true);
    }

    /**
     * Indicate the database is to be readonly.
     * Any writer, or potential writer, takes this lock in "multi" mode.
     * To block, call this method.
     */
    protected void startReadOnly(boolean canBlock) {
        beginSingleMode(writeableDatabase, canBlock);
    }

    protected void finishReadOnly() {
        endSingleMode(writeableDatabase);
    }

    protected void startPossibleWriter() {
        beginMultiMode(writeableDatabase, true);
    }

    protected void startPossibleWriter(boolean canBlock) {
        beginMultiMode(writeableDatabase, canBlock);
    }

    protected void finishPossibleWriter() {
        endMultiMode(writeableDatabase);
    }

    // Lock abstraction.
    /* Function for enter-leave for two lock types:
     *    Multiple active threads or exclusive access. (MRSW)
     *      beginMultiMode, beginSingleMode
     *    Multiple active threads and one distinguished thread (MR+SW).
     *      beginSingleGate
     *
     * == Multiple active threads or exclusive access. (MRSW)
     * This uses a ReadWriteLock where "read" is multiple outstanding threads
     * and "write" is exclusive access.
     * Used for:
     *   Exclusivity lock
     *   No potential writer lock (TxnType.WRITE or PROMOTE).
     *
     * The use of R and W for the exclusive access locks are slightly confusing.
     * It is not related to whether a transaction is R or W.
     * R means "multiple", W means "exclusive"
     *
     * == Multiple active threads and one distinguished thread (MR+SW).
     * This is a semaphore with one permit taken by the distinguished thread.
     * The other active threads are not affected.
     * Use for:
     *   Multiple ReadWrite==READ and single ReadWrite.WRITE
     */

    // MRSW - two modes: multiple OR a single thread.

    // -- Exclusivity

    @Override
    public void startNonExclusiveMode() {
        boolean b = beginMultiMode(exclusivitylock, true);
        if ( !b )
            throw new TransactionException("Can't start transaction at the moment.");
    }

    @Override
    public boolean tryNonExclusiveMode(boolean canBlock) {
        return beginMultiMode(exclusivitylock, canBlock);
    }

    @Override
    public void finishNonExclusiveMode() {
        endMultiMode(exclusivitylock);
    }

    /**
     * Enter exclusive mode; block if necessary. There are no active transactions on
     * return; new transactions will be held up in 'begin'. Return to normal (release
     * waiting transactions, allow new transactions) with
     * {@link #finishExclusiveMode}.
     * <p>
     * Do not call inside an existing transaction.
     */
    @Override
    public void startExclusiveMode() {
        startExclusiveMode(true);
    }

    /**
     * Enter exclusive mode; block if necessary. There are no active transactions on
     * return; new transactions will be held up in 'begin'. Return to normal (release
     * waiting transactions, allow new transactions) with
     * {@link #finishExclusiveMode}.
     * <p>
     * Do not call inside an existing transaction.
     */
    private boolean startExclusiveMode(boolean canBlock) {
        if ( canBlock ) {
            exclusivitylock.writeLock().lock();
            return true;
        }
        return exclusivitylock.writeLock().tryLock();
    }

    /**
     * Try to enter exclusive mode. If return is true, then there are no active
     * transactions on return and new transactions will be held up in 'begin'. If
     * false, there were in-progress transactions. Return to normal (release waiting
     * transactions, allow new transactions) with {@link #finishExclusiveMode}.
     * <p>
     * Do not call inside an existing transaction.
     */
    @Override
    public boolean tryExclusiveMode() {
        return tryExclusiveMode(false);
    }

    /**
     * Try to enter exclusive mode. If return is true, then there are no active
     * transactions on return and new transactions will be held up in 'begin'. If
     * false, there were in-progress transactions. Return to normal (release waiting
     * transactions, allow new transactions) with {@link #finishExclusiveMode}.
     * <p>
     * Do not call inside an existing transaction.
     *
     * @param canBlock Allow the operation block and wait for the exclusive mode
     *     lock.
     */
    @Override
    public boolean tryExclusiveMode(boolean canBlock) {
        return startExclusiveMode(canBlock);
    }

    /**
     * Return to normal (release waiting transactions, allow new transactions). Must
     * be paired with an earlier {@link #startExclusiveMode}.
     */
    @Override
    public void finishExclusiveMode() {
        exclusivitylock.writeLock().unlock();
    }

    // Lock abstraction.
    // An MRSW lock has two modes: multiple XOR a single thread.
    //   Hide names like "read" and "write" in favour of "multi" and "single"

    private final boolean beginMultiMode(ReadWriteLock lock, boolean canBlock) {
        if ( !canBlock )
            return lock.readLock().tryLock();
        lock.readLock().lock();
        return true;
    }

    private static void endMultiMode(ReadWriteLock lock) {
        lock.readLock().unlock();
    }

    private static boolean beginSingleMode(ReadWriteLock lock, boolean canBlock) {
        if ( !canBlock )
            return lock.writeLock().tryLock();
        lock.writeLock().lock();
        return true;
    }

    private static void endSingleMode(ReadWriteLock lock) {
        lock.writeLock().unlock();
    }
}
