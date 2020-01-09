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

import static org.apache.jena.dboe.transaction.txn.journal.JournalEntryType.UNDO;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.dboe.base.file.FileException;
import org.apache.jena.dboe.base.file.Location;
import org.apache.jena.dboe.sys.Sys;
import org.apache.jena.dboe.transaction.txn.journal.Journal;
import org.apache.jena.dboe.transaction.txn.journal.JournalEntry;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.slf4j.Logger;

/**
 * One {@code TransactionCoordinator} per group of {@link TransactionalComponent}s.
 * {@link TransactionalComponent}s can not be shared across TransactionCoordinators.
 * <p>
 * This is a general engine although tested and most used for multiple-reader
 * and single-writer (MR+SW). {@link TransactionalComponentLifecycle} provides the
 * per-thread style.
 * <p>
 * Contrast to MRSW: multiple-reader or single-writer.
 * <h3>Block writers</h3>
 * Block until no writers are active.
 * When this returns, this guarantees that the database is not changing
 * and the journal is flushed to disk.
 * <p>
 * See {@link #blockWriters()}, {@link #enableWriters()}, {@link #execAsWriter(Runnable)}
 * <h3>Exclusive mode</h3>
 * Exclusive mode is when the current thread is the only active code : no readers, no writers.
 * <p>
 * See {@link #startExclusiveMode()}/{@link #tryExclusiveMode()} {@link #finishExclusiveMode()}, {@link #execExclusive(Runnable)}
 *
 * @see Transaction
 * @see TransactionalComponent
 * @see TransactionalSystem
 */
final
public class TransactionCoordinator {
    private static Logger SysLog = Sys.syslog;
    private static Logger SysErr = Sys.errlog;

    private final Journal journal;
    // Lock on configuration changes.
    private boolean configurable = true;

    private final ComponentGroup components = new ComponentGroup();
    private final List<TransactionListener> listeners = new ArrayList<>();
    
    // Components
    private List<ShutdownHook> shutdownHooks;
    private TxnIdGenerator txnIdGenerator = TxnIdFactory.txnIdGenSimple;

    private QuorumGenerator quorumGenerator = null;
    //private QuorumGenerator quorumGenerator = (m) -> components;

    // Semaphore to implement "Single Active Writer" - independent of readers
    // This is not reentrant.
    private Semaphore writersWaiting = new Semaphore(1, true);

    // All transaction need a "read" lock through out their lifetime.
    // Do not confuse with read/write transactions.  We need a
    // "one exclusive, or many other" lock which happens to be called ReadWriteLock
    // See also {@code lock} which protects the datastructures during transaction management.
    private ReadWriteLock exclusivitylock = new ReentrantReadWriteLock();

    // The version is the serialization point for a transaction.
    // All transactions on the same view of the data get the same serialization point.

    // A read transaction can be promoted if writer does not start
    // This TransactionCoordinator provides Serializable, Read-lock-free
    // execution. With no item locking, a read can only be promoted
    // if no writer started since the reader started or if it is "read committed",
    // seeing changes made since it started and comitted at the poiont of promotion.

    /* The version of the data - incremented when transaction commits.
     * This is the version with repest to the last commited transaction.
     * Aborts do not cause the data version to advance.
     * This counter never goes backwards.
     */
    private final AtomicLong dataVersion = new AtomicLong(0);

    // Coordinator wide lock object.
    private Object coordinatorLock = new Object();

    @FunctionalInterface
    public interface ShutdownHook { void shutdown(); }

    /** Create a TransactionCoordinator, initially with no associated {@link TransactionalComponent}s */
    public TransactionCoordinator(Location location) {
        this(Journal.create(location));
    }

    /** Create a TransactionCoordinator, initially with no associated {@link TransactionalComponent}s */
    public TransactionCoordinator(Journal journal) {
        this(journal, null , new ArrayList<>());
    }

    /** Create a TransactionCoordinator, initially with {@link TransactionalComponent} in the ComponentGroup */
    public TransactionCoordinator(Journal journal, List<TransactionalComponent> components) {
        this(journal, components , new ArrayList<>());
    }

    //    /** Create a TransactionCoordinator, initially with no associated {@link TransactionalComponent}s */
//    public TransactionCoordinator(Location journalLocation) {
//        this(Journal.create(journalLocation), new ArrayList<>() , new ArrayList<>());
//    }

    private TransactionCoordinator(Journal journal, List<TransactionalComponent> txnComp, List<ShutdownHook> shutdownHooks) {
        this.journal = journal;
        this.shutdownHooks = new ArrayList<>(shutdownHooks);
        if ( txnComp != null )
            components.addAll(txnComp);
    }

    /** Add a {@link TransactionalComponent}.
     * Safe to call at any time but it is good practice is to add all the
     * components before any transactions start.
     * Internally, the coordinator ensures the add will safely happen but it
     * does not add the component to existing transactions.
     * This must be setup before recovery is attempted.
     */
    public TransactionCoordinator add(TransactionalComponent elt) {
        checklAllowModification();
        components.add(elt);
        return this;
    }

    /**
     * Remove a {@link TransactionalComponent}.
     * @see #add
     */
    public TransactionCoordinator remove(TransactionalComponent elt) {
        checklAllowModification();
        components.remove(elt.getComponentId());
        return this;
    }

    public TransactionCoordinator addListener(TransactionListener listener) {
        checklAllowModification();
        listeners.add(listener);
        return this;
    }

    public TransactionCoordinator removeListener(TransactionListener listener) {
        checklAllowModification();
        listeners.remove(listener);
        return this;
    }
    
    /**
     * Perform modification of this {@code TransactionCoordiator} after it has been
     * started.
     * <p>
     * This operation enters {@linkplain #startExclusiveMode() exclusive mode}, releases the
     * configuration lock, then calls the {@code action}. On exit from the action,
     * it resets the configuration lock, and exits exclusive mode.
     * <p>
     * Do not call inside a transaction, it may cause a deadlock.
     * <p>
     * Use with care!
     */
    public void modifyConfig(Runnable action) {
        try {
            startExclusiveMode();
            configurable = true;
            action.run();
        } finally {
            configurable = false;
            finishExclusiveMode();
        }
    }

    /** Call the action for each listener */ 
    private void listeners(Consumer<TransactionListener> action) {
        listeners.forEach(x->action.accept(x));
    }
    
    /**
     * Add a shutdown hook. Shutdown is not guaranteed to be called
     * and hence hooks may not get called.
     */
    public void add(TransactionCoordinator.ShutdownHook hook) {
        checklAllowModification();
        shutdownHooks.add(hook);
    }

    /** Remove a shutdown hook */
    public void remove(TransactionCoordinator.ShutdownHook hook) {
        checklAllowModification();
        shutdownHooks.remove(hook);
    }

    public void setQuorumGenerator(QuorumGenerator qGen) {
        checklAllowModification();
        this.quorumGenerator = qGen;
    }

    public void start() {
        checklAllowModification();
        recovery();
        configurable = false;
    }

    private /*public*/ void recovery() {

        Iterator<JournalEntry> iter = journal.entries();
        if ( ! iter.hasNext() ) {
            components.forEachComponent(c -> c.cleanStart());
            return;
        }

        SysLog.info("Journal recovery start");
        components.forEachComponent(c -> c.startRecovery());

        // Group to commit

        List<JournalEntry> entries = new ArrayList<>();

        iter.forEachRemaining( entry -> {
            switch(entry.getType()) {
                case ABORT :
                    entries.clear();
                    break;
                case COMMIT :
                    recover(entries);
                    entries.clear();
                    break;
                case REDO : case UNDO :
                    entries.add(entry);
                    break;
            }
        });

        components.forEachComponent(c -> c.finishRecovery());
        journal.reset();
        SysLog.info("Journal recovery end");
    }

    private void recover(List<JournalEntry> entries) {
        entries.forEach(e -> {
            if ( e.getType() == UNDO ) {
                Log.warn(TransactionCoordinator.this, "UNDO entry : not handled");
                return;
            }
            ComponentId cid = e.getComponentId();
            ByteBuffer bb = e.getByteBuffer();
            // find component.
            TransactionalComponent c = components.findComponent(cid);
            if ( c == null ) {
                Log.warn(TransactionCoordinator.this, "No component for "+cid);
                return;
            }
            c.recover(bb);
        });
    }

    public void setTxnIdGenerator(TxnIdGenerator generator) {
        this.txnIdGenerator = generator;
    }

    public Journal getJournal() {
        return journal;
    }

    public Location getLocation() {
        return getJournal().getLocation();
    }

    public TransactionCoordinatorState detach(Transaction txn) {
        txn.detach();
        TransactionCoordinatorState coordinatorState = new TransactionCoordinatorState(txn);
        components.forEach((id, c) -> {
            SysTransState s = c.detach();
            coordinatorState.componentStates.put(id, s);
        } );
        // The txn still counts as "active" for tracking purposes below.
        return coordinatorState;
    }

    public void attach(TransactionCoordinatorState coordinatorState) {
        Transaction txn = coordinatorState.transaction;
        txn.attach();
        coordinatorState.componentStates.forEach((id, obj) -> {
            components.findComponent(id).attach(obj);
        });
    }

    public void shutdown() {
        shutdown(false);
    }
    
    public void shutdown(boolean silent) {
        if ( coordinatorLock == null )
            return;
        if ( ! silent && countActive() > 0 )
            FmtLog.warn(SysErr, "Transactions active: W=%d, R=%d", countActiveWriter(), countActiveReaders());
        components.forEach((id, c) -> c.shutdown());
        shutdownHooks.forEach((h)-> h.shutdown());
        coordinatorLock = null;
        journal.close();
    }

    // Can modifications be made?
    private void checklAllowModification() {
        if ( ! configurable )
            throw new TransactionException("TransactionCoordinator configuration is locked");
    }

    // Is this TransactionCoordinator up and running?
    private void checkActive() {
        if ( configurable )
            throw new TransactionException("TransactionCoordinator has not been started");
        checkNotShutdown();
    }

    private void checkNotShutdown() {
        if ( coordinatorLock == null )
            throw new TransactionException("TransactionCoordinator has been shutdown");
    }

    private void releaseWriterLock() {
        int x = writersWaiting.availablePermits();
        if ( x != 0 )
            throw new TransactionException("TransactionCoordinator: Probably mismatch of enable/disableWriter calls");
        writersWaiting.release();
    }

    /** Acquire the writer lock - return true if succeeded */
    private boolean acquireWriterLock(boolean canBlock) {
        if ( ! canBlock )
            return writersWaiting.tryAcquire();
        try {
            writersWaiting.acquire();
            return true;
        } catch (InterruptedException e) { throw new TransactionException(e); }
    }

    /** Enter exclusive mode; block if necessary.
     * There are no active transactions on return; new transactions will be held up in 'begin'.
     * Return to normal (release waiting transactions, allow new transactions)
     * with {@link #finishExclusiveMode}.
     * <p>
     * Do not call inside an existing transaction.
     */
    public void startExclusiveMode() {
        startExclusiveMode(true);
    }

    /** Try to enter exclusive mode.
     *  If return is true, then there are no active transactions on return and new transactions will be held up in 'begin'.
     *  If false, there were in-progress transactions.
     *  Return to normal (release waiting transactions, allow new transactions)
     *  with {@link #finishExclusiveMode}.
     * <p>
     * Do not call inside an existing transaction.
     */
    public boolean tryExclusiveMode() {
        return tryExclusiveMode(false);
    }

    /** Try to enter exclusive mode.
     *  If return is true, then there are no active transactions on return and new transactions will be held up in 'begin'.
     *  If false, there were in-progress transactions.
     *  Return to normal (release waiting transactions, allow new transactions)
     *  with {@link #finishExclusiveMode}.
     * <p>
     * Do not call inside an existing transaction.
     * @param canBlock Allow the operation block and wait for the exclusive mode lock.
     */
    public boolean tryExclusiveMode(boolean canBlock) {
        return startExclusiveMode(canBlock);
    }

    private boolean startExclusiveMode(boolean canBlock) {
        if ( canBlock ) {
            exclusivitylock.writeLock().lock();
            return true;
        }
        return exclusivitylock.writeLock().tryLock();
    }

    /** Return to normal (release waiting transactions, allow new transactions).
     * Must be paired with an earlier {@link #startExclusiveMode}.
     */
    public void finishExclusiveMode() {
        exclusivitylock.writeLock().unlock();
    }

    /** Execute an action in exclusive mode.  This method can block.
     * Equivalent to:
     * <pre>
     *  startExclusiveMode();
     *  try { action.run(); }
     *  finally { finishExclusiveMode(); }
     * </pre>
     *
     * @param action
     */
    public void execExclusive(Runnable action) {
        startExclusiveMode();
        try { action.run(); }
        finally { finishExclusiveMode(); }
    }

    /** Block until no writers are active.
     *  When this returns, this guarantees that the database is not changing
     *  and the journal is flushed to disk.
     * <p>
     * The application must call {@link #enableWriters} later.
     * <p>
     * This operation must not be nested (it will block).
     *
     * @see #tryBlockWriters()
     * @see #enableWriters()
     */
    public void blockWriters() {
        acquireWriterLock(true);
    }

    /** Try to block all writers, or return if can't at the moment.
     * <p>
     * Unlike a write transction, there is no associated transaction.
     * <p>
     * If it returns true, the application must call {@link #enableWriters} later.
     *
     * @see #blockWriters()
     * @see #enableWriters()
     *
     * @return true if the operation succeeded and writers are blocked
     */
    public boolean tryBlockWriters() {
        return tryBlockWriters(false);
    }

    /**
     * Block until no writers are active, optionally blocking or returning if can't at the moment.
     * <p>
     * Unlike a write transction, there is no associated transaction.
     * <p>
     * If it returns true, the application must call {@link #enableWriters} later.
     * @param canBlock
     * @return true if the operation succeeded and writers are blocked
     */
    public boolean tryBlockWriters(boolean canBlock) {
        return acquireWriterLock(canBlock);
    }
    /** Allow writers.
     * This must be used in conjunction with {@link #blockWriters()} or {@link #tryBlockWriters()}
     *
     * @see #blockWriters()
     * @see #tryBlockWriters()
     */
    public void enableWriters() {
        releaseWriterLock();
    }

    /** Execute an action in as if a Write but no write transaction started.
     * This method can block.
     * <p>
     * Equivalent to:
     * <pre>
     *  blockWriters();
     *  try { action.run(); }
     *  finally { enableWriters(); }
     * </pre>
     *
     * @param action
     */
    public void execAsWriter(Runnable action) {
        blockWriters();
        try { action.run(); }
        finally { enableWriters(); }
    }

    /** Start a transaction. This may block. */
    public Transaction begin(TxnType txnType) {
        return begin(txnType, true);
    }

    /**
     * Start a transaction.
     * Returns null if this operation would block.
     * Readers can start at any time.
     * A single writer policy is currently imposed so a "begin(WRITE)" may block.
     */
    public Transaction begin(TxnType txnType, boolean canBlock) {
        Objects.nonNull(txnType);
        checkActive();

        if ( false /* bounceWritersAtTheMoment */) {
            // Is this stil needed?
            // Switching happens as copy, not in-place compaction (at the moment).
            // so we don't need a write-reject mode currently.
            if ( txnType == TxnType.WRITE ) {
                throw new TransactionException("Writers currently being rejected");
            }
        }

        if ( canBlock )
            exclusivitylock.readLock().lock();
        else {
            if ( ! exclusivitylock.readLock().tryLock() )
                return null;
        }

        // Readers never block.
        if ( txnType == TxnType.WRITE ) {
            // Writers take a WRITE permit from the semaphore to ensure there
            // is at most one active writer, else the attempt to start the
            // transaction blocks.
            // Released by in notifyCommitFinish/notifyAbortFinish
            boolean b = acquireWriterLock(canBlock);
            if ( !b ) {
                exclusivitylock.readLock().unlock();
                return null;
            }
        }
        Transaction transaction = begin$(txnType);
        startActiveTransaction(transaction);
        transaction.begin();
        notifyBegin(transaction);
        return transaction;
    }

    private Transaction begin$(TxnType txnType) {
        synchronized(coordinatorLock) {
            // Inside the lock - check again.
            checkActive();
            // Thread safe part of 'begin'
            // Allocate the transaction serialization point.
            TxnId txnId = txnIdGenerator.generate();
            List<SysTrans> sysTransList = new ArrayList<>();
            Transaction transaction = new Transaction(this, txnType, initialMode(txnType), txnId, dataVersion.get(), sysTransList);

            ComponentGroup txnComponents = chooseComponents(this.components, txnType);

            try {
                txnComponents.forEachComponent(elt -> {
                    SysTrans sysTrans = new SysTrans(elt, transaction, txnId);
                    sysTransList.add(sysTrans); }) ;
                // Calling each component must be inside the lock
                // so that a transaction does not commit overlapping with setup.
                // If it did, different components might end up starting from
                // different start states of the overall system.
                txnComponents.forEachComponent(elt -> elt.begin(transaction));
            } catch(Throwable ex) {
                // Careful about incomplete.
                //abort();
                //complete();
                throw ex;
            }
            return transaction;
        }
    }

    // Detemine ReadWrite for the transaction start from initial TxnType.
    private static ReadWrite initialMode(TxnType txnType) {
        return TxnType.initial(txnType);
    }

    private ComponentGroup chooseComponents(ComponentGroup components, TxnType txnType) {
        if ( quorumGenerator == null )
            return components;
        ComponentGroup cg = quorumGenerator.genQuorum(txnType);
        if ( cg == null )
            return components;
        cg.forEach((id, c) -> {
            TransactionalComponent tcx = components.findComponent(id);
            if ( ! tcx.equals(c) )
                SysLog.warn("TransactionalComponent not in TransactionCoordinator's ComponentGroup");
        });
        if ( SysLog.isDebugEnabled() )
            SysLog.debug("Custom ComponentGroup for transaction "+txnType+": size="+cg.size()+" of "+components.size());
        return cg;
    }

    /** Attempt to promote a transaction from READ mode to WRITE mode based.
     *  Whether intervening commits are seen is determined by the boolean flag.
     *  Return true if the transaction is already a writer.
     */
    /*package*/ boolean executePromote(Transaction transaction, boolean readCommittedPromotion) {
        if ( transaction.getMode() == ReadWrite.WRITE )
            return true;
        // Even if promotion of TxnType.READ allowed, this ability is usually rejected
        // by the transaction system around it. e.g. TransactionalBase.
        if ( transaction.getTxnType() == TxnType.READ )
            throw new TransactionException("promote: can't promote a READ transaction");
        
        notifyPromoteStart(transaction);
        boolean b = promoteTxn$(transaction, readCommittedPromotion);
        notifyPromoteFinish(transaction);
        return b;
    }

    private boolean promoteTxn$(Transaction transaction, boolean readCommittedPromotion) {
        // == Read committed path.
        if ( transaction.getTxnType() == TxnType.READ_COMMITTED_PROMOTE ) {
            if ( ! promotionWaitForWriters() )
                return false;
            // Now single writer.
            synchronized(coordinatorLock) {
                try {
                    transaction.promoteComponents();
                    // Because we want to see the new state of the data.
                    // transaction.resetDataVersion(dataVersion.get());
                } catch (TransactionException ex) {
                    try { transaction.abort(); } catch(RuntimeException ex2) {}
                    releaseWriterLock();
                    return false;
                }
                promoteActiveTransaction(transaction);
            }
            return true;
        }

        // == Read with no committed allowed
        // Check epoch is current - no "read committed".
        // Check now outside synchronized (will need to check again to confirm) for speed
        // and to allow for "no wait for writes".

        if ( ! checkNoInterveningCommits(transaction) )
            return false;

        // Take writer lock.
        if ( ! promotionWaitForWriters() )
            // Failed to become a writer.
            return false;

        // Now a proto-writer. We need to confirm when inside the synchronized.
        synchronized(coordinatorLock) {
            // Not read committed.
            // Need to check the data version once we are the writer and all previous
            // writers have committed or aborted.
            // Has there been an writer active since the transaction started?

            if ( ! checkNoInterveningCommits(transaction) ) {
                // Failed to promote.
                releaseWriterLock();
                return false;
            }

            // ... we have now got the writer lock ...
            try {
                transaction.promoteComponents();
                // No need to reset the data version because strict isolation.
            } catch (TransactionException ex) {
                try { transaction.abort(); } catch(RuntimeException ex2) {}
                releaseWriterLock();
                return false;
            }
            promoteActiveTransaction(transaction);
        }
        return true;
    }

    private boolean checkNoInterveningCommits(Transaction transaction) {
        long txnEpoch = transaction.getDataVersion();      // The transaction-start point.
        long currentEpoch = dataVersion.get();             // The current data serialization point.

        if ( txnEpoch < currentEpoch )
            // The data has changed and "read committed" not allowed.
            // We can reject now.
            return false;
        return true;
    }

    /** Whether to wait for writers when trying to promote */
    private static final boolean promotionWaitForWriters = true;

    private boolean promotionWaitForWriters() {
        if ( promotionWaitForWriters )
            return acquireWriterLock(true);
        else
            return acquireWriterLock(false);
    }

    // Called once by Transaction after the action of commit()/abort() or end()
    /** Signal that the transaction has finished. */
    /*package*/ void completed(Transaction transaction) {
        finishActiveTransaction(transaction);
        journal.reset();
        notifyEnd(transaction);
    }

    // Internally, an APi call "commit" is "prepare then commit". 
    
    /*package*/ void executePrepare(Transaction transaction) {
        // Do here because it needs access to the journal.
        notifyPrepareStart(transaction);
        transaction.getComponents().forEach(sysTrans -> {
            ByteBuffer data = sysTrans.commitPrepare();
            if ( data != null ) {
                PrepareState s = new PrepareState(sysTrans.getComponentId(), data);
                journal.write(s);
            }
        });
        notifyPrepareFinish(transaction);
    }

    /*package*/ void executeCommit(Transaction transaction, Runnable commit, Runnable finish, Runnable sysabort) {
        notifyCommitStart(transaction);
        if ( transaction.getMode() == ReadWrite.READ ) {

            //[1746]
            //executeCommitReader();
            // No commit on components, all "end".
            // Make abort the same?
            finish.run();
            notifyCommitFinish(transaction);
            return;
        }
        journal.startWrite();
        try {
            executeCommitWriter(transaction, commit, finish, sysabort);
            journal.commitWrite();
        } catch (TransactionException ex) {
            throw ex;
        } catch (Throwable th) {
            throw th;
        } finally { journal.endWrite(); }
        notifyCommitFinish(transaction);
    }

    private void executeCommitWriter(Transaction transaction, Runnable commit, Runnable finish, Runnable sysabort) {
        synchronized(coordinatorLock) {
            try {
                // Simulate a Thread.interrupt during I/O.
//                if ( true )
//                    throw new FileException(new ClosedByInterruptException());

                // *** COMMIT POINT
                journal.writeJournal(JournalEntry.COMMIT);
                journal.sync();
                // *** COMMIT POINT
            }
            // catch (ClosedByInterruptException ex) {}
            // Some low level system error - probably a sign of something serious like disk error.
            catch(FileException ex)  {
                if ( ex.getCause() instanceof ClosedByInterruptException ) {
                    // Thread interrupt during java I/O.
                    // File was closed by java.nio.
                    // Reopen - this truncates to the last write start position.
                    journal.reopen();
                    // This call should clear up the transaction state.
                    rollback(transaction, sysabort);
                    SysLog.warn("Thread interrupt during I/O in 'commit' : executed transaction rollback: "+ex.getMessage());
                    throw new TransactionException("Thread interrupt during I/O in 'commit' : transaction rollback.", ex);
                }
                if ( isIOException(ex) )
                    SysErr.warn("IOException during 'commit' : transaction may have committed. Attempting rollback: "+ex.getMessage());
                else
                    SysErr.warn("Exception during 'commit' : transaction may have committed. Attempting rollback. Details:",ex);
                if ( abandonTxn(transaction, sysabort) ) {
                    SysErr.warn("Transaction rollback");
                    throw new TransactionException("Exception during 'commit' - transaction rollback.", ex);
                }
                // Very bad. (This have been dealt with already and should get to here.)
                SysErr.error("Transaction rollback failed. System unstable."+
                    "\nPlease contact users@jena.apache.org, giving details of the environment and this incident.");
                throw new Error("Exception during 'rollback' - System unstable.", ex);
            }
            catch (Throwable ex) {
                SysErr.warn("Unexpected Throwable during 'commit' : transaction may have committed. Attempting rollback: ",ex);
                if ( abandonTxn(transaction, sysabort) ) {
                    SysErr.warn("Transaction rollback");
                    throw new TransactionException("Exception during 'commit' - transaction rollback.", ex);
                }
                // Very bad. (This should not happen.)
                SysErr.error("Transaction rollback failed. System unstable.");
                throw new TransactionException("Exception during 'rollback' - System unstable.", ex);
            }

            // Now run the Transactions commit actions.
            commit.run();
            journal.truncate(0);
            // and tell the Transaction it's finished.
            finish.run();
            // Bump global serialization point
            advanceDataVersion();
        }
    }

    // Inside the global transaction start/commit lock.
    private void advanceDataVersion() {
        dataVersion.incrementAndGet();
    }

    /** Test whether the thread is interrupted and if it is, abort the transaction. */
    private void abandonIfInterruped(Transaction txn, Runnable sysabort, String msg) {
        // Clears interrupted status
        if (Thread.interrupted()) {
            abandonTxn(txn, sysabort);
            Thread.currentThread().interrupt();
            throw new TransactionException(msg);
        }
    }

    /**
     * Try to abort, including removing the journal entries (including commit if written)
     * Return true for succeeded and false for throwable, state unknown.
     */
    private boolean abandonTxn(Transaction txn, Runnable sysabort ) {
        try {
            journal.abortWrite();
            rollback(txn, sysabort);
            return true;
        } catch (Throwable th) {
            SysErr.warn("Exception during system rollback", th);
            return false;
        }
    }

    private void rollback(Transaction txn, Runnable sysabort) {
        txn.setState(TxnState.ACTIVE);
        sysabort.run();
        txn.setState(TxnState.ABORTED);
    }

    private boolean isIOException(Throwable ex) {
        while (ex != null) {
            if ( ex instanceof IOException )
                return true;
            ex = ex.getCause();
        }
        return false;
    }

    /*package*/ void executeAbort(Transaction transaction, Runnable abort) {
        notifyAbortStart(transaction);
        abort.run();
        notifyAbortFinish(transaction);
    }

    // Active transactions.
    private Set<Transaction> activeTransactions = ConcurrentHashMap.newKeySet();
    private AtomicLong activeTransactionCount = new AtomicLong(0);
    private AtomicLong activeReadersCount = new AtomicLong(0);
    private AtomicLong activeWritersCount = new AtomicLong(0);

    private void startActiveTransaction(Transaction transaction) {
        synchronized(coordinatorLock) {
            // Use lock to ensure all the counters move together.
            // Thread safe - we have not let the Transaction object out yet.
            countBegin.incrementAndGet();
            switch(transaction.getMode()) {
                case READ:  countBeginRead.incrementAndGet();  activeReadersCount.incrementAndGet() ; break ;
                case WRITE: countBeginWrite.incrementAndGet(); activeWritersCount.incrementAndGet() ; break ;
            }
            activeTransactionCount.incrementAndGet();
            activeTransactions.add(transaction);
        }
    }

    private void promoteActiveTransaction(Transaction transaction) {
        // Called for a real promote as READ-> WRITE
        activeReadersCount.decrementAndGet();
        activeWritersCount.incrementAndGet();
    }

    private void finishActiveTransaction(Transaction transaction) {
        synchronized(coordinatorLock) {
            // Idempotent.
            boolean x = activeTransactions.remove(transaction);
            if ( ! x )
                return;
            countFinished.incrementAndGet();
            activeTransactionCount.decrementAndGet();
            switch(transaction.getMode()) {
                case READ:  activeReadersCount.decrementAndGet(); break ;
                case WRITE: activeWritersCount.decrementAndGet(); break ;
            }
        }
        exclusivitylock.readLock().unlock();
    }

    public long countActiveReaders()    { return activeReadersCount.get(); }
    public long countActiveWriter()     { return activeWritersCount.get(); }
    public long countActive()           { return activeTransactionCount.get(); }

    // notify*Start/Finish called round each transaction lifecycle step

    private void notifyBegin(Transaction transaction) {
        listeners(x -> x.notifyTxnStart(transaction));
    }

    private void notifyEnd(Transaction transaction) {
        listeners(x -> x.notifyTxnFinish(transaction));
    }

    private void notifyPromoteStart(Transaction transaction) {
        listeners(x -> x.notifyPromoteStart(transaction));
    }

    private void notifyPromoteFinish(Transaction transaction) {
        listeners(x -> x.notifyPromoteFinish(transaction));
    }

    private void notifyPrepareStart(Transaction transaction) {
        listeners(x -> x.notifyPrepareStart(transaction));
    }

    private void notifyPrepareFinish(Transaction transaction) {
        listeners(x -> x.notifyPrepareFinish(transaction));
    }

    // Writers released here - can happen because of commit() or abort().

    private void notifyCommitStart(Transaction transaction) {
        listeners(x -> x.notifyCommitStart(transaction));
    }

    private void notifyCommitFinish(Transaction transaction)    {
        listeners(x->x.notifyCommitFinish(transaction));
        if ( transaction.getMode() == ReadWrite.WRITE )
            releaseWriterLock();
    }

    private void notifyAbortStart(Transaction transaction) { 
        listeners(x->x.notifyAbortStart(transaction));
    }

    private void notifyAbortFinish(Transaction transaction) {
        listeners(x->x.notifyAbortFinish(transaction));
        if ( transaction.getMode() == ReadWrite.WRITE )
            releaseWriterLock();
    }

    /*package*/ void notifyEndStart(Transaction transaction) {
        listeners(x->x.notifyEndStart(transaction));
    }

    /*package*/ void notifyEndFinish(Transaction transaction) {
        listeners(x->x.notifyEndFinish(transaction));
    }

    // Called by Transaction once at the end of first commit()/abort() or end()

    /*package*/ void notifyCompleteStart(Transaction transaction) {
        listeners(x -> x.notifyCompleteStart(transaction));
    }

    /*package*/ void notifyCompleteFinish(Transaction transaction) {
        listeners(x -> x.notifyCompleteFinish(transaction));
    }

    // Coordinator state.
    private final AtomicLong countBegin         = new AtomicLong(0);

    private final AtomicLong countBeginRead     = new AtomicLong(0);

    private final AtomicLong countBeginWrite    = new AtomicLong(0);

    private final AtomicLong countFinished      = new AtomicLong(0);

    // Access counters
    public long countBegin()        { return countBegin.get(); }

    public long countBeginRead()    { return countBeginRead.get(); }

    public long countBeginWrite()   { return countBeginWrite.get(); }

    public long countFinished()     { return countFinished.get(); }
}
