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

import static org.apache.jena.query.ReadWrite.WRITE ;
import static org.seaborne.dboe.transaction.txn.journal.JournalEntryType.UNDO ;

import java.nio.ByteBuffer ;
import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;
import java.util.Objects ;
import java.util.concurrent.ConcurrentHashMap ;
import java.util.concurrent.Semaphore ;
import java.util.concurrent.atomic.AtomicLong ;
import java.util.concurrent.locks.ReadWriteLock ;
import java.util.concurrent.locks.ReentrantReadWriteLock ;

import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.query.ReadWrite ;
import org.seaborne.dboe.base.file.Location ;
import org.seaborne.dboe.sys.SystemBase ;
import org.seaborne.dboe.transaction.txn.journal.Journal ;
import org.seaborne.dboe.transaction.txn.journal.JournalEntry ;
import org.slf4j.Logger ;

/**
 * One TransactionCoordinator per group of TransactionalComponents.
 * TransactionalComponent can not be shared across TransactionCoordinators.
 * <p>
 * This is a general engine although tested and most used for multiple-reader
 * and single-writer (MR+SW). TransactionalComponentLifecycle provided the
 * per-threadstyle.
 * <p>
 * Contrast to MRSW: multiple-reader or single-writer
 * 
 * @see Transaction
 * @see TransactionalComponent
 * @see TransactionalSystem
 */
final
public class TransactionCoordinator {
    private static Logger log = SystemBase.syslog ;
    
    private final Journal journal ;
    private boolean coordinatorStarted = false ;

    private final ComponentGroup components = new ComponentGroup() ;
    // Components 
    private ComponentGroup txnComponents = null ;
    private List<ShutdownHook> shutdownHooks ;
    private TxnIdGenerator txnIdGenerator = TxnIdFactory.txnIdGenSimple ;
    
    private QuorumGenerator quorumGenerator = null ;
    //private QuorumGenerator quorumGenerator = (m) -> components ;

    // Semaphore to implement "Single Active Writer" - independent of readers
    // This is not reentrant.
    private Semaphore writersWaiting = new Semaphore(1, true) ;
    
    // All transaction need a "read" lock through out their lifetime. 
    // Do not confuse with read/write transactions.  We need a 
    // "one exclusive, or many other" lock which happens to be called ReadWriteLock
    // See also {@code lock} which protects the datastructures during transaction management.  
    private ReadWriteLock exclusivitylock = new ReentrantReadWriteLock() ;

    // Coordinator wide lock object.
    private Object lock = new Object() ;

    @FunctionalInterface
    public interface ShutdownHook { void shutdown() ; }

    /** Create a TransactionCoordinator, initially with no associated {@link TransactionalComponent}s */ 
    public TransactionCoordinator(Location location) {
        this(Journal.create(location)) ;
    }
    
    /** Create a TransactionCoordinator, initially with no associated {@link TransactionalComponent}s */ 
    public TransactionCoordinator(Journal journal) {
        this(journal, null , new ArrayList<>()) ;
    }

    /** Create a TransactionCoordinator, initially with {@link TransactionalComponent} in the ComponentGroup */
    public TransactionCoordinator(Journal journal, List<TransactionalComponent> components) {
        this(journal, components , new ArrayList<>()) ;
    }

    //    /** Create a TransactionCoordinator, initially with no associated {@link TransactionalComponent}s */ 
//    public TransactionCoordinator(Location journalLocation) {
//        this(Journal.create(journalLocation), new ArrayList<>() , new ArrayList<>()) ;
//    }

    private TransactionCoordinator(Journal journal, List<TransactionalComponent> txnComp, List<ShutdownHook> shutdownHooks) { 
        this.journal = journal ;
        this.shutdownHooks = new ArrayList<>(shutdownHooks) ;
        if ( txnComp != null ) {
            //txnComp.forEach(x-> System.out.println(x.getComponentId().label()+" :: "+Bytes.asHex(x.getComponentId().bytes()) ) ) ;
            txnComp.forEach(components::add);
        }
    }
    
    /** Add a {@link TransactionalComponent}.
     * Safe to call at any time but it is good practice is to add all the
     * compoents before any transactions start.
     * Internally, the coordinator ensures the add will safely happen but it
     * does not add the component to existing transactions.
     * This must be setup before recovery is attempted. 
     */
    public TransactionCoordinator add(TransactionalComponent elt) {
        checkSetup() ;
        synchronized(lock) {
            components.add(elt) ;
        }
        return this ;
    }

    /** 
     * Remove a {@link TransactionalComponent}.
     * @see #add 
     */
    public TransactionCoordinator remove(TransactionalComponent elt) {
        checkSetup() ;
        synchronized(lock) {
            components.remove(elt.getComponentId()) ;
        }
        return this ;
    }

    /**
     * Add a shutdown hook. Shutdown is not guaranteed to be called
     * and hence hooks may not get called.
     */
    public void add(TransactionCoordinator.ShutdownHook hook) {
        checkSetup() ;
        synchronized(lock) {
            shutdownHooks.add(hook) ;
        }
    }

    /** Remove a shutdown hook */
    public void remove(TransactionCoordinator.ShutdownHook hook) {
        checkSetup() ;
        synchronized(lock) {
            shutdownHooks.remove(hook) ;
        }
    }
    
    public void setQuorumGenerator(QuorumGenerator qGen) {
        checkSetup() ;
        this.quorumGenerator = qGen ;
    }

    public void start() {
        checkSetup() ;
        recovery() ;
        coordinatorStarted = true ;
    }

    private /*public*/ void recovery() {
        
        Iterator<JournalEntry> iter = journal.entries() ;
        if ( ! iter.hasNext() ) {
            components.forEachComponent(c -> c.cleanStart()) ;
            return ;
        }
        
        log.info("Journal recovery start") ;
        components.forEachComponent(c -> c.startRecovery()) ;
        
        // Group to commit
        
        List<JournalEntry> entries = new ArrayList<>() ;
        
        iter.forEachRemaining( entry -> {
            switch(entry.getType()) {
                case ABORT :
                    entries.clear() ;
                    break ;
                case COMMIT :
                    recover(entries) ;
                    entries.clear() ;
                    break ;
                case REDO : case UNDO :
                    entries.add(entry) ;
                    break ;
            }
        }) ;
    
        components.forEachComponent(c -> c.finishRecovery()) ;
        journal.reset() ;
        log.info("Journal recovery end") ;
    }

    private void recover(List<JournalEntry> entries) {
        entries.forEach(e -> {
            if ( e.getType() == UNDO ) {
                Log.warn(TransactionCoordinator.this, "UNDO entry : not handled") ;  
                return ;
            }
            ComponentId cid = e.getComponentId() ;
            ByteBuffer bb = e.getByteBuffer() ;
            // find component.
            TransactionalComponent c = components.findComponent(cid) ;
            if ( c == null ) {
                Log.warn(TransactionCoordinator.this, "No component for "+cid) ;
                return ;
            }
            c.recover(bb); 
        }) ;
    }

    public void setTxnIdGenerator(TxnIdGenerator generator) {
        this.txnIdGenerator = generator ;
    }
    
    public Journal getJournal() {
        return journal ;
    }
    
    public TransactionCoordinatorState detach(Transaction txn) {
        txn.detach();
        TransactionCoordinatorState coordinatorState = new TransactionCoordinatorState(txn) ;
        components.forEach((id, c) -> {
            SysTransState s = c.detach() ;
            coordinatorState.componentStates.put(id, s) ;
        } ) ;
        // The txn still counts as "active" for tracking purposes below.
        return coordinatorState ;
    }

    public void attach(TransactionCoordinatorState coordinatorState) {
        Transaction txn = coordinatorState.transaction ;
        txn.attach() ;
        coordinatorState.componentStates.forEach((id, obj) -> {
            components.findComponent(id).attach(obj);
        });
    }

    public void shutdown() {
        if ( lock == null )
            return ;
        components.forEach((id, c) -> c.shutdown()) ;
        shutdownHooks.forEach((h)-> h.shutdown()) ;
        lock = null ;
        journal.close(); 
    }

    // Are we in the initialization phase?
    private void checkSetup() {
        if ( coordinatorStarted )
            throw new TransactionException("TransactionCoordinator has already been started") ;
    }

    // Are we up and ruuning?
    private void checkActive() {
        if ( ! coordinatorStarted )
            throw new TransactionException("TransactionCoordinator has not been started") ;
        checkNotShutdown();
    }

    // Check not wrapped up
    private void checkNotShutdown() {
        if ( lock == null )
            throw new TransactionException("TransactionCoordinator has been shutdown") ;
    }

    private void releaseWriterLock() {
        int x = writersWaiting.availablePermits() ;
        if ( x != 0 )
            throw new TransactionException("TransactionCoordinator: Probably mismatch of enable/disableWriter calls") ;
        writersWaiting.release() ;
    }
    
    private boolean acquireWriterLock(boolean canBlock) {
        if ( ! canBlock )
            return writersWaiting.tryAcquire() ;
        try { 
            writersWaiting.acquire() ; 
            return true;
        } catch (InterruptedException e) { throw new TransactionException(e) ; }
    }
    
    /** Enter exclusive mode. 
     * There are no active transactions on return; new transactions will be held up in 'begin'.
     * Return to normal (release waiting transactions, allow new transactions)
     * with {@link #finishExclusiveMode}.   
     */
    public void startExclusiveMode() {
        startExclusiveMode(true);
    }
    
    /** Try to enter exclusive mode. 
     *  If return is true, then are no active transactions on return and new transactions will be held up in 'begin'.
     *  If alse, there were in-porgress transactions.
     *  Return to normal (release waiting transactions, allow new transactions)
     *  with {@link #finishExclusiveMode}.   
     */
    public boolean tryExclusiveMode(boolean canBlock) {
        return startExclusiveMode(false);
    }
    
    private boolean startExclusiveMode(boolean canBlock) {
        if ( canBlock ) {
            exclusivitylock.writeLock().lock() ;
            return true ;
        }
        return exclusivitylock.writeLock().tryLock() ;
    }

    /** Return to normal (release waiting transactions, allow new transactions).
     * Must be paired with an earlier {@link #startExclusiveMode}. 
     */
    public void finishExclusiveMode() {
        exclusivitylock.writeLock().unlock() ;
    }

    /** Execute an action in exclusive mode.  This method can block.
     * Equivalent to:
     * <pre>
     *  startExclusiveMode() ;
     *  try { action.run(); }
     *  finally { finishExclusiveMode(); }
     * </pre>
     * 
     * @param action
     */
    public void execExclusive(Runnable action) {
        startExclusiveMode() ;
        try { action.run(); }
        finally { finishExclusiveMode(); }
    }
    
    /** Block until no writers are active.
     *  When this returns, yhis guarantees that the database is not changing
     *  and the jounral is flush to disk.
     * <p> 
     * The application must call {@link #enableWriters} later.
     * <p> 
     * This operation must not be nested (it will block).
     * 
     * @see #tryBlockWriters()
     * @see #enableWriters()
     * 
     */
    public void blockWriters() {
        acquireWriterLock(true) ;
    }

    /** Block until no writers are active or, optionally, return if can't at the moment.
     * Return 'true' if the operation succeeded.
     * <p>
     * If it returns true, the application must call {@link #enableWriters} later.
     *  
     * @see #blockWriters()
     * @see #enableWriters()
     */
    public boolean tryBlockWriters() {
        return acquireWriterLock(false) ;
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
     *  blockWriters() ;
     *  try { action.run(); }
     *  finally { enableWriters(); }
     * </pre>
     * 
     * @param action
     */
    public void execAsWriter(Runnable action) {
        blockWriters() ;
        try { action.run(); }
        finally { enableWriters(); }
    }
    
    /** Start a transaction. This may block. */
    public Transaction begin(ReadWrite readWrite) {
        return begin(readWrite, true) ;
    }
    
    /** 
     * Start a transaction.  Returns null if this operation would block.
     * Readers can start at any time.
     * A single writer policy is currently imposed so a "begin(WRITE)"
     * may block.  
     */
    public Transaction begin(ReadWrite readWrite, boolean canBlock) {
        Objects.nonNull(readWrite) ;
        checkActive() ;
        
        if ( canBlock )
            exclusivitylock.readLock().lock() ;
        else {
            if ( ! exclusivitylock.readLock().tryLock() )
                return null ;
        }
        
        // Readers never block.
        if ( readWrite == WRITE ) {
            // Writers take a WRITE permit from the semaphore to ensure there
            // is at most one active writer, else the attempt to start the
            // transaction blocks.
            // Released by in notifyCommitFinish/notifyAbortFinish
            boolean b = acquireWriterLock(canBlock) ;
            if ( !b ) {
                exclusivitylock.readLock().unlock() ;
                return null ;
            }
        }
        Transaction transaction = begin$(readWrite) ;
        startActiveTransaction(transaction) ;
        transaction.begin();
        return transaction;
    }
    
    // The epoch is the serialization point for a transaction.
    // All readers on the same view of the data get the same serialization point.
    // The serialization point (epoch for short) of the active writer
    // is the next
    // This becomes the new reader 
    
    // A read transaction can be promoted if writer does not start
    // This TransactionCoordinator provides Serializable, Read-lock-free
    // execution.  With no item locking, a read can only be promoted
    // if no writer started since the reader started.
    
    private final AtomicLong writerEpoch = new AtomicLong(0) ;      // The leading edge of the epochs
    private final AtomicLong readerEpoch = new AtomicLong(0) ;      // The trailing edge of epochs
    
    private Transaction begin$(ReadWrite readWrite) {
        synchronized(lock) {
            // Thread safe part of 'begin'
            // Allocate the transaction serialization point.
            long dataVersion =    
                ( readWrite == WRITE ) ?
                    writerEpoch.incrementAndGet() :
                    readerEpoch.get() ;
            TxnId txnId = txnIdGenerator.generate() ;
            List<SysTrans> sysTransList = new ArrayList<>() ;
            Transaction transaction = new Transaction(this, txnId, readWrite, dataVersion, sysTransList) ;
            
            ComponentGroup txnComponents = chooseComponents(this.components, readWrite) ;
            
            try {
                txnComponents.forEachComponent(elt -> {
                    SysTrans sysTrans = new SysTrans(elt, transaction, txnId) ;
                    sysTransList.add(sysTrans) ; }) ;
                // Calling each component must be inside the lock
                // so that a transaction does not commit overlapping with setup.
                // If it did, different components might end up starting from
                // different start states of the overall system.
                txnComponents.forEachComponent(elt -> elt.begin(transaction)) ;
            } catch(Throwable ex) {
                // Careful about incomplete.
                //abort() ;
                //complete() ;
                throw ex ;
            }
            return transaction ;
        }
    }
    
    private ComponentGroup chooseComponents(ComponentGroup components, ReadWrite readWrite) {
        if ( quorumGenerator == null )
            return components ;
        ComponentGroup cg = quorumGenerator.genQuorum(readWrite) ;
        if ( cg == null )
            return components ;
        cg.forEach((id, c) -> {
            TransactionalComponent tcx = components.findComponent(id) ;
            if ( ! tcx.equals(c) )
                log.warn("TransactionalComponent not in TransactionCoordinator's ComponentGroup") ; 
        }) ;
        if ( log.isDebugEnabled() )
            log.debug("Custom ComponentGroup for transaction "+readWrite+": size="+cg.size()+" of "+components.size()) ;
        return cg ;
    }

    /** Attempt to promote a tranasaction from READ to WRITE.
     * No-op for a transaction already a writer.
     * Throws {@link TransactionException} if the promotion
     * can not be done.
     * Current policy is to not support promotion.
     */
    
     // Later ...
//    * Current policy if a READ transaction can be promoted if intervening
//    * writer has started or an existing one committed.  
    
    /*package*/ boolean promoteTxn(Transaction transaction) {
        if ( transaction.getMode() == WRITE )
            return true ;
        // We're a reader. Try to be a writer.
        synchronized(lock) {
            // Is there a writer active?
            // Was there one since we started?
            long dataEpoch = transaction.getDataEpoch() ;
            long currentEpoch = writerEpoch.get() ;  // Advanced as a writer starts 
            if ( dataEpoch != currentEpoch )
                return false ;
            // Should not block - we checked the read/write epochs
            // and they said "no writer" all inside 'lock' 
            boolean b = acquireWriterLock(false) ;
            if ( !b )
                throw new TransactionException("Promote: Inconistent: Failed to get the writer lock");
            try { transaction.promoteComponents() ; }
            catch (TransactionException ex) {
                transaction.abort();
                return false ;
            }
            writerEpoch.incrementAndGet() ;
        }
        return true ;
    }

    // Called by Transaction once at the end of first
    // commit()/abort() or end(), if no commit()/abort()

    // Called by Transaction after the action of commit()/abort() or end() 
    /*package*/ void completed(Transaction transaction) {
        finishActiveTransaction(transaction);
        journal.reset() ;
    }

    /*package*/ void executePrepare(Transaction transaction) {
        // Do here because it needs access to the journal.
        notifyPrepareStart(transaction);
        transaction.getComponents().forEach(sysTrans -> {
            TransactionalComponent c = sysTrans.getComponent() ;
            // XXX Pass journal to TransactionalComponent.commitPrepare?
            ByteBuffer data = c.commitPrepare(transaction) ;
            if ( data != null ) {
                PrepareState s = new PrepareState(c.getComponentId(), data) ;
                journal.write(s) ;
            }
        }) ;
        notifyPrepareFinish(transaction);
    }

    /*package*/ void executeCommit(Transaction transaction,  Runnable commit, Runnable finish) {
        // This is the commit point. 
        synchronized(lock) {
            // *** COMMIT POINT
            journal.sync() ;
            // *** COMMIT POINT
            // Now run the Transactions commit actions. 
            commit.run() ;
            journal.truncate(0) ;
            // and tell the Transaction it's finished. 
            finish.run() ;
            // Bump global serialization point if necessary.
            if ( transaction.getMode() == WRITE )
                advanceEpoch() ;
            notifyCommitFinish(transaction) ;
        }
    }

    
    // Inside the global transaction start/commit lock.
    private void advanceEpoch() {
        long wEpoch = writerEpoch.get() ;
        // The next reader will see the committed state. 
        readerEpoch.set(wEpoch) ;
    }
    
    /*package*/ void executeAbort(Transaction transaction, Runnable abort) {
        notifyAbortStart(transaction) ;
        abort.run();
        notifyAbortFinish(transaction) ;
    }
    
    // Active transactions: this is (the missing) ConcurrentHashSet
    private final static Object dummy                   = new Object() ;    
    private ConcurrentHashMap<Transaction, Object> activeTransactions = new ConcurrentHashMap<>() ;
    private AtomicLong activeTransactionCount = new AtomicLong(0) ;
    private AtomicLong activeReadersCount = new AtomicLong(0) ;
    private AtomicLong activeWritersCount = new AtomicLong(0) ;
    
    private void startActiveTransaction(Transaction transaction) {
        synchronized(lock) {
            // Use lock to ensure all the counters move together.
            // Thread safe - we have not let the Transaction object out yet.
            countBegin.incrementAndGet() ;
            switch(transaction.getMode()) {
                case READ:  countBeginRead.incrementAndGet() ;  activeReadersCount.incrementAndGet() ; break ;
                case WRITE: countBeginWrite.incrementAndGet() ; activeWritersCount.incrementAndGet() ; break ;
            }
            activeTransactionCount.incrementAndGet() ;
            activeTransactions.put(transaction, dummy) ;
        }
    }
    
    private void finishActiveTransaction(Transaction transaction) {
        synchronized(lock) {
            // Idempotent.
            Object x = activeTransactions.remove(transaction) ;
            if ( x == null )
                return ;
            countFinished.incrementAndGet() ;
            activeTransactionCount.decrementAndGet() ;
            switch(transaction.getMode()) {
                case READ:  activeReadersCount.decrementAndGet() ; break ;
                case WRITE: activeWritersCount.decrementAndGet() ; break ;
            }
        }
        exclusivitylock.readLock().unlock() ; 
    }
    
    public long countActiveReaders()    { return activeReadersCount.get() ; } 
    public long countActiveWriter()     { return activeWritersCount.get() ; } 
    public long countActive()           { return activeTransactionCount.get(); }
    
    // notify*Start/Finish called round each transaction lifecycle step
    // Called in cooperation between Transaction and TransactionCoordinator
    // depending on who is actually do the work of each step.

    /*package*/ void notifyPrepareStart(Transaction transaction) {}

    /*package*/ void notifyPrepareFinish(Transaction transaction) {}

    // Writers released here - can happen because of commit() or abort(). 

    private void notifyCommitStart(Transaction transaction) {}
    
    private void notifyCommitFinish(Transaction transaction) {
        if ( transaction.getMode() == WRITE )
            releaseWriterLock();
    }
    
    private void notifyAbortStart(Transaction transaction) { }
    
    private void notifyAbortFinish(Transaction transaction) {
        if ( transaction.getMode() == WRITE )
            releaseWriterLock();
    }

    /*package*/ void notifyEndStart(Transaction transaction) { }

    /*package*/ void notifyEndFinish(Transaction transaction) {}

    // Called by Transaction once at the end of first commit()/abort() or end()
    
    /*package*/ void notifyCompleteStart(Transaction transaction) { }

    /*package*/ void notifyCompleteFinish(Transaction transaction) { }

    // Coordinator state.
    private final AtomicLong countBegin         = new AtomicLong(0) ;

    private final AtomicLong countBeginRead     = new AtomicLong(0) ;

    private final AtomicLong countBeginWrite    = new AtomicLong(0) ;

    private final AtomicLong countFinished      = new AtomicLong(0) ;

    // Access counters
    public long countBegin()        { return countBegin.get() ; }

    public long countBeginRead()    { return countBeginRead.get() ; }

    public long countBeginWrite()   { return countBeginWrite.get() ; }

    public long countFinished()     { return countFinished.get() ; }
}

