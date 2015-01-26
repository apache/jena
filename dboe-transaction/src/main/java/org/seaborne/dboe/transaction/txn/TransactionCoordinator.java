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

package org.seaborne.dboe.transaction.txn;

import static com.hp.hpl.jena.query.ReadWrite.READ ;
import static com.hp.hpl.jena.query.ReadWrite.WRITE ;

import java.util.ArrayList ;
import java.util.List ;
import java.util.Map ;
import java.util.Objects ;
import java.util.concurrent.ConcurrentHashMap ;
import java.util.concurrent.Semaphore ;
import java.util.concurrent.atomic.AtomicLong ;

import org.seaborne.dboe.transaction.txn.journal.Journal ;
import org.seaborne.dboe.transaction.txn.journal.JournalEntry ;

import com.hp.hpl.jena.query.ReadWrite ;

/**
 * One TransactionCoordinator per group of TransactionalComponents.
 * TransactionalComponent can no tbe shared across TransactionCoordinators.
 * <p>
 * This is a general engine although tested and most used for multiple-reader
 * and single-writer (MR+SW). TransactionalComponentLifecycle provide the
 * per-threadstyle.
 * <p>
 * Contrast to MRSW: multiple-reader or single-writer
 * 
 * @see Transaction
 * @see TransactionalComponent
 **/
final
public class TransactionCoordinator {
    
    private final Journal journal ;
    private final ComponentGroup components = new ComponentGroup() ;
    //List<TransactionalComponent> elements ;
    private List<ShutdownHook> shutdownHooks ;
    
    // Semaphore to implement "Single Active Writer" - independent of readers 
    private Semaphore writersWaiting = new Semaphore(1, true) ;

    @FunctionalInterface
    public interface ShutdownHook { void shutdown() ; }

    // Coordinator wide lock object.
    private Object lock = new Object() ; 
    
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
     * Safe to call at any time but good pratice is to add all the
     * compoents before any transactions start.
     * Internally, the coordinator ensures the add wil safely happen but it
     * does not add the compoent to existing transactions. 
     */
    public TransactionCoordinator add(TransactionalComponent elt) {
        checkNotShutdown() ;
        synchronized(lock) {
            components.add(elt) ;
        }
        return this ;
    }

    public void recovery() {
        journal.entries() ;
    }
    
    /** 
     * Remove a {@link TransactionalComponent}.
     * @see #add 
     */
    public TransactionCoordinator remove(TransactionalComponent elt) {
        checkNotShutdown() ;
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
        checkNotShutdown() ;
        synchronized(lock) {
            shutdownHooks.add(hook) ;
        }
    }

    /** Remove a shutdown hook */
    public void remove(TransactionCoordinator.ShutdownHook hook) {
        checkNotShutdown() ;
        synchronized(lock) {
            shutdownHooks.remove(hook) ;
        }
    }
    
    public Journal getJournal()     { return journal ; }
    
    // Coordinator state.
    private final AtomicLong countBegin         = new AtomicLong(0) ;
    private final AtomicLong countBeginRead     = new AtomicLong(0) ;
    private final AtomicLong countBeginWrite    = new AtomicLong(0) ;
    private final AtomicLong countFinished      = new AtomicLong(0) ;
    
    // Active transactions
    private final static Object dummy           = new Object() ;
    private Map<Transaction, Object> activeTransactions = new ConcurrentHashMap<>() ;
    
    // The epoch is the serialization point for a transaction.
    // All readers on the same view of the data get the same serialization point.
    // The serialization point (epoch for short) of the active writer
    // is the next
    // This becomes the new reader 
    
    // A read transaction can be promoted if writer does not start
    // This TransactionCoordinator provides Serializable, Read-lock-free
    // execution.  With not item locking, a read can only be promoted
    // if no writer started since the reader started.
    
    private final AtomicLong writerEpoch = new AtomicLong(0) ;      // The leading edge of the epochs
    private final AtomicLong readerEpoch = new AtomicLong(0) ;      // The trailing edge of epochs
    
    // Access counters
    public long countBegin()        { return countBegin.get() ; }
    public long countBeginRead()    { return countBeginRead.get() ; }
    public long countBeginWrite()   { return countBeginWrite.get() ; }
    public long countFinished()     { return countFinished.get() ; }
    public long countActive()       { return activeTransactions.size() ; }
    
    public void shutdown() {
        components.forEach((id, c) -> c.shutdown()) ;
        shutdownHooks.forEach((h)-> h.shutdown()) ;
        lock = null ;
    }

    private void checkNotShutdown() {
        if ( lock == null )
            throw new TransactionException("TransactionCoordinator has been shutdown") ;
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
        checkNotShutdown() ;
        
        // Readers never block.
        if ( readWrite == WRITE )
        {
            // Writers take a WRITE permit from the semaphore to ensure there
            // is at most one active writer, else the attempt to start the
            // transaction blocks.
            // Released by in notifyCommitEnd
            if ( ! canBlock ) {
                boolean b = writersWaiting.tryAcquire() ;
                if ( !b )
                    return null ;
            }
            else {
                try { writersWaiting.acquire() ; }
                catch (InterruptedException e) { throw new TransactionException(e) ; }
            }
        }

        Transaction transaction = begin$(readWrite) ;
        // Thread safe - we have not let the Transaction object out yet.
        countBegin.incrementAndGet() ;
        switch(readWrite) {
            case READ:  countBeginRead.incrementAndGet() ; break ;
            case WRITE: countBeginWrite.incrementAndGet() ; break ;
        }
        startActiveTransaction(transaction) ;
        transaction.begin();
        return transaction;
    }
    
    private Transaction begin$(ReadWrite readWrite) {
        synchronized(lock) {
            // Thread safe part of 'begin'
            // Allocate the transaction serialization point.
            long dataVersion =    
                ( readWrite == WRITE ) ?
                    writerEpoch.incrementAndGet() :
                    readerEpoch.get() ;
            TxnId txnId = TxnId.create() ;
            List<SysTrans> sysTransList = new ArrayList<>() ;
            Transaction transaction = new Transaction(this, txnId, readWrite, dataVersion, sysTransList) ;
            try {
                components.forEachComponent(elt -> {
                    SysTrans sysTrans = new SysTrans(elt, transaction, txnId) ;
                    sysTransList.add(sysTrans) ; }) ;
                // Calling each component must be inside the lock
                // so that a transaction does not commit overlapping with setup.
                // If it did, different components might end up starting from
                // different start states of the overall system.
                components.forEachComponent(elt -> elt.begin(transaction)) ;
            } catch(Throwable ex) {
                // Careful about incomplete.
                //abort() ;
                //complete() ;
                throw ex ;
            }
            return transaction ;
        }
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
    
    /*package*/ void promote(Transaction transaction) {
        if ( transaction.getMode() == READ) {
            throw new TransactionException("Attempt to promote READ transaction") ;
        }
    }

    // Called by Transaction once at the end of first
    // commit()/abort() or end(), if no commit()/abort()

    // Called by Transaction after the action of commit()/abort() or end() 
    /*package*/ void completed(Transaction transaction) {
        // The check that a writer has called commit()/abort() 
        // explicitly before end() is in Transaction where lifecycle
        // checks are done.
        // finishActiveTransaction is idempotent.
        finishActiveTransaction(transaction);
    }

    /*package*/ void executeCommit(Transaction transaction, List<PrepareState> prepareState, Runnable commit, Runnable finish) {
        
        // TODO Journal.
        // Write prepared state, no sync.
        // Jounral use is sync'ed 
        // (effectively no-op by the single writer assumption
        // but this machinery is more general)
        synchronized(lock) {
            // No transactions begin during this block.
            notifyCommitStart(transaction) ;

            prepareState.forEach((ps) -> {
                journal.write(ps); 
            }) ;
            journal.writeJournal(JournalEntry.COMMIT) ;
            // *** COMMIT POINT
            journal.sync() ;
            // *** COMMIT POINT
            
            // Now run the Transactions commit actions. 
            commit.run() ;
            // Can forget the journal entries now.
            
            //journal.truncate(0) ;
            
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
    
    private void startActiveTransaction(Transaction transaction) {
        activeTransactions.put(transaction, dummy) ;
    }
    
    private void finishActiveTransaction(Transaction transaction) {
        // Idempotent.
        Object x = activeTransactions.remove(transaction) ; 
        if ( x != null )
            countFinished.incrementAndGet() ;
    }
    
    // notify*Start/Finish called round each transaction lifecycle step
    // Called in cooperation between Transaction and TransactionCoordinator
    // depending on who is actually do the work of each step.

    /*package*/ void notifyPrepareStart(Transaction transaction) {}

    /*package*/ void notifyPrepareFinish(Transaction transaction) {}

    // Writers released here - can happen because of commit() or abort(). 

    private void notifyCommitStart(Transaction transaction) {}
    
    private void notifyCommitFinish(Transaction transaction) {
        if ( transaction.getMode() == WRITE ) {
            writersWaiting.release();
        }
    }
    
    private void notifyAbortStart(Transaction transaction) { }
    
    private void notifyAbortFinish(Transaction transaction) {
        if ( transaction.getMode() == WRITE ) {
            writersWaiting.release();
        }
    }

    /*package*/ void notifyEndStart(Transaction transaction) { }

    /*package*/ void notifyEndFinish(Transaction transaction) {}

    // Called by Transaction once at the end of first commit()/abort() or end()
    
    /*package*/ void notifyCompleteStart(Transaction transaction) { }

    /*package*/ void notifyCompleteFinish(Transaction transaction) { }
}

