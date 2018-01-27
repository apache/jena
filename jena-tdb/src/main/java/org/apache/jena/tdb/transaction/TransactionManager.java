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

package org.apache.jena.tdb.transaction;

import static java.lang.String.format ;
import static org.apache.jena.tdb.sys.SystemTDB.syslog ;
import static org.apache.jena.tdb.transaction.TransactionManager.TxnPoint.BEGIN ;
import static org.apache.jena.tdb.transaction.TransactionManager.TxnPoint.CLOSE ;

import java.io.File ;
import java.util.*;
import java.util.concurrent.BlockingQueue ;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque ;
import java.util.concurrent.Semaphore ;
import java.util.concurrent.atomic.AtomicLong ;
import java.util.concurrent.atomic.AtomicReference ;
import java.util.concurrent.locks.ReadWriteLock ;
import java.util.concurrent.locks.ReentrantReadWriteLock ;

import org.apache.jena.atlas.lib.Pair ;
import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.query.ReadWrite ;
import org.apache.jena.query.TxnType;
import org.apache.jena.shared.Lock ;
import org.apache.jena.tdb.store.DatasetGraphTDB ;
import org.apache.jena.tdb.sys.SystemTDB ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

public class TransactionManager
{
    private static boolean checking = true ;
    
    private static Logger log = LoggerFactory.getLogger(TransactionManager.class) ;
    private Set<Transaction> activeTransactions = ConcurrentHashMap.newKeySet();
    synchronized public boolean activeTransactions() { return !activeTransactions.isEmpty() ; }
    
    // Setting this true cause the TransactionManager to keep lists of transactions
    // and what has happened.  Nothing is thrown away, but eventually it will
    // consume too much memory.
    
    // Record happenings.
    private boolean recordHistory = false ;
    
    /** This controls how many write transactions we batch up before 
     *  deciding to try to flush the journal to the main database.  
     */
    public static /*final*/ int QueueBatchSize = setQueueBatchSize() ; 
    
    // JENA-1222
    /** This controls how large to let the journal get in size 
     * before deciding to flush even if below the queue batch size.
     * -1 means "off".   
     */
    public static /*final*/ int JournalThresholdSize = -1 ;  
    
    // JENA-1224
    /** This controls how large to let the journal in comitted transactions
     * before deciding to pause and flush as soon as possible.
     * -1 means "off".   
     */
    public static /*final*/ int MaxQueueThreshold = 100 ;
    
    private static int setQueueBatchSize() {
        if ( SystemTDB.is64bitSystem )
            return 10 ;
        // On 32bit systems are memory constrained. The Java address space is
        // limited to about 1.5G - the heap can not be bigger.
        // So we don't do batching (change if batching is less memory hungry).
        return 0 ;
    }
    
    // Records the states that a transaction goes though.
    enum TxnPoint { BEGIN, COMMIT, ABORT, CLOSE, QUEUE, UNQUEUE }
    private List<Pair<Transaction, TxnPoint>> transactionStateTransition ;
    
    private void record(Transaction txn, TxnPoint state) {
        if ( !recordHistory )
            return ;
        initRecordingState() ;
        transactionStateTransition.add(new Pair<>(txn, state)) ;
    }
    
    // Statistic variables to record the maximum length of the flush queue.
    
    int maxQueue = 0 ;
    List<Transaction> commitedAwaitingFlush = new ArrayList<>() ;
    
    static AtomicLong transactionId = new AtomicLong(1) ;
    // The version of the data starting at 1. The contract is that this is
    // never the same for two version (it may not be monotonic increasing).
    // Currently, it is sequentially increasing.
    // Set on each commit.
    private AtomicLong version = new AtomicLong(0) ;
    
    // Accessed by SysTxnState
    // These must be AtomicLong
    /*package*/ AtomicLong activeReaders = new AtomicLong(0) ; 
    /*package*/ AtomicLong activeWriters = new AtomicLong(0) ; // 0 or 1
    
    public long getCountActiveReaders()     { return activeReaders.get() ; }
    public long getCountActiveWriters()     { return activeWriters.get() ; }
    
    // Misc stats (should be LongAdder / Java8?)
    /*package*/ AtomicLong finishedReaders = new AtomicLong(0) ;
    /*package*/ AtomicLong committedWriters = new AtomicLong(0) ;
    /*package*/ AtomicLong abortedWriters = new AtomicLong(0) ;
    
    // This is the DatasetGraphTDB for the first read-transaction created for
    // a particular view.  The read DatasetGraphTDB can be used by all the readers
    // seeing the same view.
    // A write transaction clears this when it commits; the first reader of a 
    // particular state creates the view datasetgraph and sets the  lastreader.
    private AtomicReference<DatasetGraphTDB> currentReaderView = new AtomicReference<>(null) ;
    
    // Ensure single writer. A writer calling begin(WRITE) blocks.  
    private Semaphore writerPermits = new Semaphore(1, true) ;
    
    // All transactions need a "read" lock throughout their lifetime. 
    // Do not confuse with read/write transactions.  We need a 
    // "one exclusive, or many other" lock which happens to be called a ReadWriteLock.
    // Fair lock - approximately arrival order. 
    // Stops "readers" (normal transactions, READ or WRITE) from locking
    // out a "writer" (exclusive mode).  
    private ReadWriteLock exclusivitylock = new ReentrantReadWriteLock(true) ;
    
    // Delays enacting transactions.
    private BlockingQueue<Transaction> queue = new LinkedBlockingDeque<>() ;
    public long getQueueLength() { return queue.size() ; }

    private DatasetGraphTDB baseDataset ;
    private Journal journal ;
    
    /*
     * The order of calls is: 
     * 1/ transactionStarts
     * 2/ readerStarts or writerStarts
     * 3/ readerFinishes or writerCommits or writerAborts
     * 4/ transactionFinishes
     * 5/ transactionCloses
     */
    
    private interface TSM {
        void transactionStarts(Transaction txn) ;
        void transactionFinishes(Transaction txn) ;
        void transactionCloses(Transaction txn) ;
        void readerStarts(Transaction txn) ;
        void readerFinishes(Transaction txn) ;
        void transactionPromotes(Transaction txnOld, Transaction txnNew) ;
        void writerStarts(Transaction txn) ;
        void writerCommits(Transaction txn) ;
        void writerAborts(Transaction txn) ;
    }
    
    class TSM_Base implements TSM {
        @Override public void transactionStarts(Transaction txn)    {}
        @Override public void transactionFinishes(Transaction txn)  {}
        @Override public void transactionCloses(Transaction txn)    {}
        @Override public void readerStarts(Transaction txn)         {}
        @Override public void readerFinishes(Transaction txn)       {}
        @Override public void transactionPromotes(Transaction txnOld, Transaction txnNew) {}
        @Override public void writerStarts(Transaction txn)         {}
        @Override public void writerCommits(Transaction txn)        {}
        @Override public void writerAborts(Transaction txn)         {}
    }
    
    class TSM_Logger extends TSM_Base {
        TSM_Logger() {}
        @Override public void readerStarts(Transaction txn)         { log("start", txn) ; }
        @Override public void readerFinishes(Transaction txn)       { log("finish", txn) ; }
        @Override public void transactionPromotes(Transaction txnOld, Transaction txnNew)
        { log("promote(old)", txnOld) ; log("promote(new)", txnNew) ; }  
        @Override public void writerStarts(Transaction txn)         { log("begin", txn) ; }
        @Override public void writerCommits(Transaction txn)        { log("commit", txn) ; }
        @Override public void writerAborts(Transaction txn)         { log("abort", txn) ; }
    }

    /** More detailed */
    class TSM_LoggerDebug extends TSM_Base {
        TSM_LoggerDebug() {}
        @Override public void readerStarts(Transaction txn)         { logInternal("start",  txn) ; }
        @Override public void readerFinishes(Transaction txn)       { logInternal("finish", txn) ; }
        @Override public void transactionPromotes(Transaction txnOld, Transaction txnNew)
        { logInternal("promote(old)", txnOld) ; logInternal("promote(new)", txnNew) ; }  
        @Override public void writerStarts(Transaction txn)         { logInternal("begin",  txn) ; }
        @Override public void writerCommits(Transaction txn)        { logInternal("commit", txn) ; }
        @Override public void writerAborts(Transaction txn)         { logInternal("abort",  txn) ; }
    }
    
    class TSM_Counters implements TSM {
        TSM_Counters() {}
        @Override public void transactionStarts(Transaction txn)    { activeTransactions.add(txn) ; }
        @Override public void transactionFinishes(Transaction txn)  { activeTransactions.remove(txn) ; }
        @Override public void transactionCloses(Transaction txn)    { }
        @Override public void readerStarts(Transaction txn)         { inc(activeReaders) ; }
        @Override public void readerFinishes(Transaction txn)       { dec(activeReaders) ; inc(finishedReaders); }
        
        @Override public void transactionPromotes(Transaction txnOld, Transaction txnNew)
        { dec(activeReaders) ; inc(finishedReaders); inc(activeWriters); }
        
        @Override public void writerStarts(Transaction txn)         { inc(activeWriters) ; }
        @Override public void writerCommits(Transaction txn)        { dec(activeWriters) ; inc(committedWriters) ; }
        @Override public void writerAborts(Transaction txn)         { dec(activeWriters) ; inc(abortedWriters) ; }
    }
    
    // Short name: x++
    static long inc(AtomicLong x)   { return x.getAndIncrement() ; }
    // Short name: --x
    static long dec(AtomicLong x)   { return x.decrementAndGet() ; }
    
    // Transaction policy:
    // TSM + WriterEnters, WriterLeaves which may use the semaphore. (+ReaderEnters, ReaderLeaves ??)
    
    // Policy for writing back journal'ed data to the base datasetgraph
    // Writes if no reader at end of writer, else queues.
    // Queue cleared at end of any transaction finding itself the only transaction.
    class TSM_WriteBackEndTxn extends TSM_Base {
        // Safe mode.
        // Take a READ lock over the base dataset.
        // Write-back takes a WRITE lock.
        @Override public void readerStarts(Transaction txn)     { txn.getBaseDataset().getLock().enterCriticalSection(Lock.READ) ; }
        
        @Override public void writerStarts(Transaction txn)     { txn.getBaseDataset().getLock().enterCriticalSection(Lock.READ) ; }
        
        // Currently, the writer semaphore is managed explicitly in the main code.
        
        @Override public void transactionPromotes(Transaction txnOld, Transaction txnNew) {
            // Switch locks.
            txnOld.getBaseDataset().getLock().leaveCriticalSection() ;
            txnNew.getBaseDataset().getLock().enterCriticalSection(Lock.READ) ;
        }
        
        @Override
        public void readerFinishes(Transaction txn) {
            txn.getBaseDataset().getLock().leaveCriticalSection() ;
            readerFinishesWorker(txn) ;
        }

        @Override
        public void writerCommits(Transaction txn) {
            txn.getBaseDataset().getLock().leaveCriticalSection() ;
            writerCommitsWorker(txn) ;
        }

        @Override
        public void writerAborts(Transaction txn) {
            txn.getBaseDataset().getLock().leaveCriticalSection() ;
            writerAbortsWorker(txn) ;
        }
    }
    
    // Policy for writing back that always writes from the writer by using a
    // MRSW lock, with a write step at the end of the writer.
    // Always a read loc for any active transaction (reader or the writer)
    // Still use semaphore for writer entry control.
    class TSM_WriterWriteBack extends TSM_Base
    {
        // Not implemented
    }
    
    // Policy for writing where a transaction takes an  MRSW at the start.
    // Semaphore for writer entry unnecessary.
    class TSM_MRSW_Writer extends TSM_Base
    {
        // Not implemented
    }
    

    class TSM_Record extends TSM_Base {
        // Later - record on one list the state transition.
        @Override
        public void transactionStarts(Transaction txn)      { record(txn, BEGIN) ; }
        @Override
        public void transactionFinishes(Transaction txn)    { record(txn, CLOSE) ; }
    }
    
    private TSM[] actions = new TSM[] { 
        new TSM_Counters() ,           // Must be first.
        //new TSM_LoggerDebug() ,
        new TSM_Logger() ,
        (recordHistory ? new TSM_Record() : null ) ,
        new TSM_WriteBackEndTxn()        // Write back policy. Must be last.
    } ;

    public TransactionManager(DatasetGraphTDB dsg){
        this.baseDataset = dsg ; 
        this.journal = Journal.create(dsg.getLocation()) ;
        // LATER
//        Committer c = new Committer() ;
//        this.committerThread = new Thread(c) ;
//        committerThread.setDaemon(true) ;
//        committerThread.start() ;
    }

    public void closedown() {
        processDelayedReplayQueue(null) ;
        journal.close() ;
    }

    public /*for testing only*/ static final boolean DEBUG = false ;
    
    // TIM
//    @Override
//    public void begin(TxnMode txnMode) {
//        if (isInTransaction()) 
//            throw new JenaTransactionException("Transactions cannot be nested!");
//        transactionMode.set(txnMode);
//        ReadWrite initial = txnMode.equals(TxnMode.WRITE) ? WRITE : READ;
//        _begin(initial);
//    }
//    
//    @Override
//    public void begin(final ReadWrite readWrite) {
//        if (isInTransaction()) 
//            throw new JenaTransactionException("Transactions cannot be nested!");
//        transactionMode.set(TxnMode.convert(readWrite));
//        _begin(readWrite) ;
//    }
    
    public DatasetGraphTxn begin(TxnType txnType, String label) {
        return beginInternal(txnType, txnType, label);
    }
    
    private DatasetGraphTxn beginInternal(TxnType txnType, TxnType originalTxnType, String label) {   
        // The exclusivitylock surrounds the entire transaction cycle.
        // Paired with notifyCommit, notifyAbort.
        startNonExclusive();
        
        // Not synchronized (else blocking on semaphore will never wake up
        // because Semaphore.release is inside synchronized).
        // Allow only one active writer. 
        if ( txnType == TxnType.WRITE ) {
            // Writers take a WRITE permit from the semaphore to ensure there
            // is at most one active writer, else the attempt to start the
            // transaction blocks.
            acquireWriterLock(true) ;
        }
        // entry synchronized part
        DatasetGraphTxn dsgtxn = begin$(txnType, originalTxnType, label) ;
        noteTxnStart(dsgtxn.getTransaction()) ;
        return dsgtxn;
    }
    
    /** Ensure a DatasetGraphTxn is for a write transaction.
     * <p>
     * If the transaction is already a write transaction, this is an efficient no-op.
     * <p>
     * If the transaction is a read transaction then promotion can either respect the transactions current
     * view of the data where no changes from other writers that started after this transaction are visible
     * ("serialized" or "fully isolated") or the promotion can include changes by other such writers ("read committed").
     * <p>
     * However, "serialized" can fail, in which case an exception {@link TDBTransactionException}
     * is thrown. The transactions can continue as a read transaction.
     * There is no point retrying - later committed changes have been made and will remain.
     * <p>
     * "read committed" will always succeed but the app needs to be aware that data access before the promotion
     * is no longer valid. It may need to check it. 
     * <p>
     * Return null for "no promote" due to intermediate commits.  
     */
    /*package*/ DatasetGraphTxn promote(DatasetGraphTxn dsgtxn, TxnType txnType) throws TDBTransactionException {
        Transaction txn = dsgtxn.getTransaction() ;
        if ( txn.getState() != TxnState.ACTIVE )
            throw new TDBTransactionException("promote: transaction is not active") ;
        if ( txn.getTxnMode() == ReadWrite.WRITE )
            return dsgtxn ;
        if ( txn.getTxnType() == TxnType.READ ) {
            txn.abort();
            throw new TDBTransactionException("promote: transaction is a READ transaction") ;
        }
        
        // Read commit - pick up whatever is current at the point setup.
        // Can also promote - may need to wait for active writers. 
        // Go through begin for the writers lock. 
        if ( txnType == TxnType.READ_COMMITTED_PROMOTE ) {
            acquireWriterLock(true);
            // No need to sync - we just queue as a writer.
            return promoteExec$(dsgtxn, txnType);
        }
        
        // First check, without the writer lock. Fast fail.
        // Have any finished writers run and commited since this transaction began?
        // This is a "fast fail" test of whether we can promote. Passing it does not gauarantee 
        // we can promote but failing it does mean we can't.
        // There are active writers running at this moment, (or any time up to
        // acquireWriterLock returning. It catches many cases without needing
        // to acquire the writer lock.
        
        if ( txn.getVersion() != version.get() )
            return null;

        // Put ourselves in the serialization timeline of the dataset - that, is grab a writer
        // lock as a step toward promotion. We can then test properly because no other writer
        // can start; readers can between acquireWriterLock and the synchronized in promote2$
        // but they don't matter.
        // Potentially blocking - must be outside 'synchronized' so that any active writer
        // can commit/abort.  Otherwise, we have deadlock.
        acquireWriterLock(true) ;
        // Do the synchronized stuff.
        return promoteSync$(dsgtxn, txnType) ; 
    }
    
    synchronized
    private DatasetGraphTxn promoteSync$(DatasetGraphTxn dsgtxn, TxnType originalTxnType) {
        Transaction txn = dsgtxn.getTransaction() ;
        // Writers may have happened between the first check of the active writers may have committed.  
        if ( txn.getVersion() != version.get() ) {
            releaseWriterLock();
            return null;
        }
        return promoteExec$(dsgtxn, originalTxnType);
    }
    
    private DatasetGraphTxn promoteExec$(DatasetGraphTxn dsgtxn, TxnType originalTxnType) {
        // Use begin$ (not beginInternal)
        // We have the writers lock.
        // We keep the exclusivity lock.
        Transaction txn = dsgtxn.getTransaction() ;
        DatasetGraphTxn dsgtxn2 = begin$(TxnType.WRITE, originalTxnType, txn.getLabel()) ;
        noteTxnPromote(txn, dsgtxn2.getTransaction());
        return dsgtxn2 ;
    }
    
    // If DatasetGraphTransaction has a sync lock on sConn, this
    // does not need to be sync'ed. But it's possible to use some
    // of the low level objects directly so we'll play safe.  
    
    synchronized
    private DatasetGraphTxn begin$(TxnType txnType, TxnType originalTxnType, String label) {
        Objects.requireNonNull(txnType);
        if ( txnType == TxnType.WRITE && activeWriters.get() > 0 )    // Guard
            throw new TDBTransactionException("Existing active write transaction") ;

        if ( DEBUG ) 
            switch ( txnType )
            {
                case READ : System.out.print("r") ; break ;
                case WRITE : System.out.print("w") ; break ;
                case READ_COMMITTED_PROMOTE : System.out.print("r(cp)") ; break ;
                case READ_PROMOTE : System.out.print("rp") ; break ;
            }
        
        DatasetGraphTDB dsg = determineBaseDataset() ;
        Transaction txn = createTransaction(dsg, txnType, originalTxnType, label) ;
        
        log("begin$", txn) ;
        
        ReadWrite mode = initialMode(txnType);
        DatasetGraphTxn dsgTxn = createDSGTxn(dsg, txn, mode);
        txn.setActiveDataset(dsgTxn) ;

        // Empty for READ ; only WRITE transactions have components that need notifying.
        // Promote is a WRITE transaction starting.
        List<TransactionLifecycle> components = dsgTxn.getTransaction().lifecycleComponents() ;
        
        if ( mode == ReadWrite.READ ) {
            // ---- Consistency check. View caching does not reset components.
            if ( components.size() != 0 )
                log.warn("read transaction, non-empty lifecycleComponents list") ;
        }
        
        for ( TransactionLifecycle component : components )
            component.begin(dsgTxn.getTransaction()) ;
        return dsgTxn ;
    }
    
    private DatasetGraphTDB determineBaseDataset() {
    //      if ( DEBUG ) {
    //          if ( !commitedAwaitingFlush.isEmpty() )
    //              System.out.print(commitedAwaitingFlush.size()) ;
    //      } else {
    //          System.out.print('_') ;
    //      }
          DatasetGraphTDB dsg = baseDataset ;
          // But, if there are pending, committed transactions, use latest.
          if ( !commitedAwaitingFlush.isEmpty() )
              dsg = commitedAwaitingFlush.get(commitedAwaitingFlush.size() - 1).getActiveDataset().getView() ;
          return dsg ;
      }
    private Transaction createTransaction(DatasetGraphTDB dsg, TxnType txnType, TxnType originalTxnType, String label) {
        if ( originalTxnType == null )
            originalTxnType = txnType;
        Transaction txn = new Transaction(dsg, version.get(), txnType, initialMode(txnType), transactionId.getAndIncrement(), originalTxnType, label, this) ;
        return txn ;
    }

    // State.
    private static ReadWrite initialMode(TxnType txnType) {
        return TxnType.initial(txnType);
    }
    
    private DatasetGraphTxn createDSGTxn(DatasetGraphTDB dsg, Transaction txn, ReadWrite mode) {
        // A read transaction (if it has no lifecycle components) can be shared over all
        // read transactions at the same commit level. 
        //    lastreader
        
        if ( mode == ReadWrite.READ ) {
            // If a READ transaction, and a previously built one is cached, use it.
            DatasetGraphTDB dsgCached = currentReaderView.get() ;
            if ( dsgCached != null ) {
                // No components so we don't need to notify them.
                // We can just reuse the storage dataset.
                return new DatasetGraphTxn(dsgCached, txn) ;
            }
        }
        
        DatasetGraphTxn dsgTxn = new DatasetBuilderTxn(this, dsg).build(txn, mode);
        if ( mode == ReadWrite.READ ) {
            // If a READ transaction, cache the storage view.
            // This is cleared when a WRITE commits
            currentReaderView.set(dsgTxn.getView()) ;
        }
        return dsgTxn ;
    }

    /* Signal a transaction has commited.  The journal has a commit record
     * and a sync to disk. The code here manages the inter-transaction stage
     * of deciding how to play the changes back to the base data
     * together with general recording of transaction details and status. 
     */ 
    /*package*/ void notifyCommit(Transaction transaction) {
        boolean excessiveQueue = false ;
        synchronized(this) {
            if ( ! activeTransactions.contains(transaction) )
                SystemTDB.errlog.warn("Transaction not active: "+transaction.getTxnId()) ;

            noteTxnCommit(transaction) ;

            switch ( transaction.getTxnMode() ) {
                case READ: break ;
                case WRITE:
                    version.incrementAndGet() ;
                    currentReaderView.set(null) ;       // Clear the READ transaction cache.
                    // JENA-1224
                    excessiveQueue = ( MaxQueueThreshold >= 0 && queue.size() > MaxQueueThreshold ) ;
                    releaseWriterLock();
            }
            // Paired with begin()
            finishNonExclusive();
        }
        // Force the queue to flush if it is getting excessively long.
        // Note that writers may happen between releaseWriterLock above
        // and exclusiveFlushQueue and so several writers may try to flush
        // the queue; this is safe.        
        
        if ( excessiveQueue ) {
            // Check again. This is an imperfect filter for multiple attempts to flush the queue
            // and avoid going into exclusive mode.
//            excessiveQueue = ( MaxQueueThreshold >= 0 && queue.size() > MaxQueueThreshold ) ;
//            if ( excessiveQueue )
                // Must have released the exclusivity lock (a read-MRSW lock) otherwise
                // taking the exclusivity lock (a write-MRSW lock) in
                // exclusiveFlushQueue->startExclusiveMode will block.
                // Java MRSW locks do not allow "hold reader -> take writer".
                exclusiveFlushQueue();
        }
    }

    synchronized
    /*package*/ void notifyAbort(Transaction transaction) {
        // Transaction has done the abort on all the transactional elements.
        if ( ! activeTransactions.contains(transaction) )
            SystemTDB.errlog.warn("Transaction not active: "+transaction.getTxnId()) ;
        
        noteTxnAbort(transaction) ;
        
        switch ( transaction.getTxnMode() )
        {
            case READ: break ;
            case WRITE: releaseWriterLock();
        }
        // Paired with begin()
        finishNonExclusive();
    }
    
    synchronized
    /*package*/ void notifyClose(Transaction txn) {
        // Caution - not called if "Transactional.end() is not called."
        if ( txn.getState() == TxnState.ACTIVE )
        {
            // The application error case for begin(WRITE)...end() is handled in Trasnaction.close().
            // This is internal checking.
            String x = txn.getBaseDataset().getLocation().getDirectoryPath() ;
            syslog.warn("close: Transaction not commited or aborted: Transaction: "+txn.getTxnId()+" @ "+x) ;
            // Force abort then close
            txn.abort() ;
            txn.close() ;
            return ;
        }
        noteTxnClose(txn) ;
    }
        
    private void releaseWriterLock() {
        int x = writerPermits.availablePermits() ;
        if ( x != 0 )
            throw new TDBTransactionException("TransactionCoordinator: Probably mismatch of enableWriters/blockWriters calls") ;
        writerPermits.release() ;
    }
    
    private boolean acquireWriterLock(boolean canBlock) {
        if ( ! canBlock )
            return writerPermits.tryAcquire() ;
        try { 
            writerPermits.acquire() ; 
            return true;
        } catch (InterruptedException e) { throw new TDBTransactionException(e) ; }
    }
    
    /** Block until no writers are active.
     *  When this returns, it guarantees that the database is not changing
     *  and the journal is flush to disk.
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
    
    /** Force flushing the queue by entering exclusive mode.*/
    private void exclusiveFlushQueue() {
        startExclusiveMode(true) ;
        finishExclusiveMode(); 
    }

    /** Start non-exclusive mode - this is the normal transaction mode.
     * This does not relate to reader/writer. 
     */
    private void startNonExclusive() {
        exclusivitylock.readLock().lock();
    }
    
    /** Finish non-exclusive mode - this is the normal transaction mode.
     * This does not relate to reader/writer. 
     */
    private void finishNonExclusive() {
        exclusivitylock.readLock().unlock();
    }

    /** Enter exclusive mode. 
     * <p>
     * There are no active transactions on return; new transactions will be held up in 'begin'.
     * Return to normal (release waiting transactions, allow new transactions)
     * with {@link #finishExclusiveMode}.
     * <p>
     * The caller must not be inside a transaction associated with this TransactionManager.
     * (The call will block waiting for that transaction to finish.)
     */
    public void startExclusiveMode() {
        startExclusiveMode(true);
    }
    
    /** Try to enter exclusive mode. 
     *  If return is true, then are no active transactions on return and new transactions will be held up in 'begin'.
     *  If false, there is an in-progress transactions.
     *  Return to normal (release waiting transactions, allow new transactions)
     *  with {@link #finishExclusiveMode}.
     *  <p>
     *  The call must not itself be in a transaction (this call will return false).    
     */
    public boolean tryExclusiveMode() {
        return startExclusiveMode(false);
    }
    
    private boolean startExclusiveMode(boolean canBlock) {
        if ( canBlock ) {
            exclusivitylock.writeLock().lock() ;
            processDelayedReplayQueue(null);
            return true ;
        }
        boolean b = exclusivitylock.writeLock().tryLock() ;
        if ( ! b ) return false ;
        processDelayedReplayQueue(null);
        return true ;
    }

    /** Return the exclusivity lock. Testing and internal use only. */
    public ReadWriteLock getExclusivityLock$() { return exclusivitylock ; } 
    
    /** Return to normal (release waiting transactions, allow new transactions).
     * Must be paired with an earlier {@link #startExclusiveMode}. 
     */
    public void finishExclusiveMode() {
        exclusivitylock.writeLock().unlock() ;
    }
    
    /** The stage in a commit after committing - make the changes permanent in the base data */ 
    private void enactTransaction(Transaction transaction)
    {
        // Really, really do it!
        for ( TransactionLifecycle x : transaction.lifecycleComponents() )
        {
            x.commitEnact(transaction) ;
            x.commitClearup(transaction) ;
        }
        transaction.signalEnacted() ;
    }

    /** Try to flush the delayed write queue - only happens if there are no active transactions */ 
    synchronized
    public void flush() {
        processDelayedReplayQueue(null) ;
    }
    
    // -- The main operations to undertake when a transaction finishes.
    // Called from TSM_WriteBackEndTxn but the worker code is here so all
    // related code, including queue flushing is close together.
    
    private void readerFinishesWorker(Transaction txn) {
        if ( checkForJournalFlush() )
            processDelayedReplayQueue(txn) ;
    }
    
    private void writerAbortsWorker(Transaction txn) {
        if ( checkForJournalFlush() )
            processDelayedReplayQueue(txn) ;
    }
    
    // Whether to try to flush the journal. We may still find that we are blocked
    // from doing so by another transaction (checked in processDelayedReplayQueue).
    // MaxQueueThreshold is handled in notifyCommit (writer exit).
    private boolean checkForJournalFlush() {
        if ( queue.size() >= QueueBatchSize )
            return true ;
        boolean journalSizeFlush = (JournalThresholdSize > 0 && journal.size() > JournalThresholdSize ) ;
        if ( journalSizeFlush )
            // JENA-1222
            // Based on Journal file growing large in terms of bytes
            return true ;
        // No test here for excessive queue length (MaxQueueThreshold).
        return false ;
    }
    
    private void writerCommitsWorker(Transaction txn) {
        if ( activeReaders.get() == 0 && checkForJournalFlush() ) {
            // Can commit immediately.
            // Ensure the queue is empty though.
            // Could simply add txn to the commit queue and do it that way.
            if ( log() ) log("Commit immediately", txn) ;

            // Currently, all we need is
            //    JournalControl.replay(txn) ;
            // because that plays queued transactions.
            // But for long term generallity, at the cost of one check of the journal size
            // we do this sequence.
            processDelayedReplayQueue(txn) ;
            enactTransaction(txn) ;
            JournalControl.replay(txn) ;
        } else {
            // Can't write back to the base database at the moment.
            commitedAwaitingFlush.add(txn) ;
            maxQueue = Math.max(commitedAwaitingFlush.size(), maxQueue) ;
            if ( log() ) log("Add to pending queue", txn) ;
            queue.add(txn) ;
        }
    }
    
    private void processDelayedReplayQueue(Transaction txn) {
        // JENA-1224: Are there too many wrapper layers?
        // This is handled in notifyCommit.
        
        // Can we do work?
        if ( activeReaders.get() != 0 || activeWriters.get() != 0 ) {
            if ( queue.size() > 0 && log() )
                log(format("Pending transactions: R=%s / W=%s", activeReaders, activeWriters), txn) ;
            return ;
        }
        if ( DEBUG ) {
            if ( queue.size() > 0 ) 
                System.out.print("!"+queue.size()+"!") ;
        }
        
        if ( DEBUG ) checkNodesDatJrnl("1", txn) ;
        
        if ( queue.size() == 0 && txn != null )
            // Nothing to do - journal should be empty. 
            return ;
        
        if ( log() )
            log("Start flush delayed commits", txn) ;
        
        // Drop the cached reader view so that next time it is recreated
        // against the updated database.
        currentReaderView.set(null) ;
        
        while (queue.size() > 0) {
            // Currently, replay is replay everything
            // so looping on a per-transaction basis is
            // pointless but harmless.

            try {
                Transaction txn2 = queue.take() ;
                if ( txn2.getTxnMode() == ReadWrite.READ )
                    continue ;
                if ( log() )
                    log("  Flush delayed commit of "+txn2.getLabel(), txn) ;
                if ( DEBUG ) checkNodesDatJrnl("2", txn) ;
                checkReplaySafe() ;
                enactTransaction(txn2) ;
                commitedAwaitingFlush.remove(txn2) ;
            } catch (InterruptedException ex)
            { Log.error(this, "Interruped!", ex) ; }
        }

        checkReplaySafe() ;
        if ( DEBUG ) checkNodesDatJrnl("3", txn) ;

        // Whole journal to base database
        JournalControl.replay(journal, baseDataset) ;

        if ( DEBUG ) checkNodesDatJrnl("4", txn) ;
        
        checkReplaySafe() ;
        if ( log() )
            log("End flush delayed commits", txn) ;
    }

    private void checkNodesDatJrnl(String label, Transaction txn) {
        if ( txn != null ) {
            String x = txn.getBaseDataset().getLocation().getPath(label + ": nodes.dat-jrnl") ;
            long len = new File(x).length() ;
            if ( len != 0 )
                log("nodes.dat-jrnl: not empty", txn) ;
        }
    }
    
    private void checkReplaySafe() {
        if ( ! checking ) return ;
        if ( activeReaders.get() != 0 || activeWriters.get() != 0 )
            log.error("There are now active transactions") ;
    }
    
    private void noteTxnStart(Transaction transaction) {
        switch (transaction.getTxnMode())
        {
            case READ : readerStarts(transaction) ; break ;
            case WRITE : writerStarts(transaction) ; break ;
        }
        transactionStarts(transaction) ;
    }

    private void noteTxnPromote(Transaction transaction, Transaction transaction2) {
        activeTransactions.remove(transaction);
        activeTransactions.add(transaction2);
        transactionPromotes(transaction, transaction2) ;
    }

    private void noteTxnCommit(Transaction transaction) {
        switch (transaction.getTxnMode())
        {
            case READ : readerFinishes(transaction) ; break ;
            case WRITE : writerCommits(transaction) ; break ;
        }
        transactionFinishes(transaction) ;
    }
    
    private void noteTxnAbort(Transaction transaction) {
        switch (transaction.getTxnMode())
        {
            case READ : readerFinishes(transaction) ; break ;
            case WRITE : writerAborts(transaction) ; break ;
        }
        transactionFinishes(transaction) ;
    }
    
    private void noteTxnClose(Transaction transaction) {
        transactionCloses(transaction) ;
    }
    
    // ---- Recording
    
    /** Get recording state */
    public boolean recording()              { return recordHistory ; }
    /** Set recording on or off */
    public void recording(boolean flag) {
        recordHistory = flag ;
        if ( recordHistory )
            initRecordingState() ;
    }
    /** Clear all recording state - does not clear stats */ 
    public void clearRecordingState() {
        initRecordingState() ;
        transactionStateTransition.clear() ;
    }
    
    private void initRecordingState() {
        if ( transactionStateTransition == null )
            transactionStateTransition = new ArrayList<>() ;
    }

    public Journal getJournal() {
        return journal ;
    }

    // ---- Logging
    // Choose log output once when this object is created.
    
    private final boolean logstate = (syslog.isDebugEnabled() || log.isDebugEnabled()) ;
    
    private boolean log() {
        return logstate ;
    }
    
    private void log(String msg, Transaction txn) {
        if ( ! log() )
            return ;
        if ( txn == null )
            logger().debug("<No txn>: "+msg) ;
        else
            logger().debug(txn.getLabel()+": "+msg) ;
    }
    
    private void logInternal(String action, Transaction txn) {
        if ( ! log() )
            return ;
        String txnStr = ( txn == null ) ? "<null>" : txn.getLabel() ;
        //System.err.printf(format("%6s %s -- %s", action, txnStr, state())) ;
        logger().debug(format("%6s %s -- %s", action, txnStr, state())) ;
    }

    private static Logger logger() {
        if ( syslog.isDebugEnabled() )
            return syslog ;
        else
            return log ;
    }
    
    synchronized
    public SysTxnState state() {
        return new SysTxnState(this) ;
    }
    
    // LATER.
    class Committer implements Runnable {
        @Override
        public void run() {
            for ( ;; ) {
                // Wait until the reader count goes to zero.
                
                // This wakes up for every transation but maybe 
                // able to play several transactions at once (later).
                try {
                    Transaction txn = queue.take() ;
                    // This takes a Write lock on the  DSG - this is where it blocks.
                    JournalControl.replay(txn) ;
                    synchronized (TransactionManager.this) {
                        commitedAwaitingFlush.remove(txn) ;
                    }
                } catch (InterruptedException ex)
                { Log.error(this, "Interruped!", ex) ; }
            }
        }
    }
    
    private void transactionStarts(Transaction txn) {
        for ( TSM tsm : actions )
            if ( tsm != null )
                tsm.transactionStarts(txn) ;
    }

    private void transactionFinishes(Transaction txn) {
        for ( TSM tsm : actions )
            if ( tsm != null )
                tsm.transactionFinishes(txn) ;
    }
    
    private void transactionCloses(Transaction txn) {
        for ( TSM tsm : actions )
            if ( tsm != null )
                tsm.transactionCloses(txn) ;
    }

    private void readerStarts(Transaction txn) {
        for ( TSM tsm : actions )
            if ( tsm != null )
                tsm.readerStarts(txn) ;
    }

    private void readerFinishes(Transaction txn) {
        for ( TSM tsm : actions )
            if ( tsm != null )
                tsm.readerFinishes(txn) ;
    }
    
    private void transactionPromotes(Transaction txnOld, Transaction txnNew) {
        for ( TSM tsm : actions )
            if ( tsm != null )
                tsm.transactionPromotes(txnOld, txnNew);
    }

    private void writerStarts(Transaction txn) {
        for ( TSM tsm : actions )
            if ( tsm != null )
                tsm.writerStarts(txn) ;
    }

    private void writerCommits(Transaction txn) {
        for ( TSM tsm : actions )
            if ( tsm != null )
                tsm.writerCommits(txn) ;
    }

    private void writerAborts(Transaction txn) {
        for ( TSM tsm : actions )
            if ( tsm != null )
                tsm.writerAborts(txn) ;
    }
}
