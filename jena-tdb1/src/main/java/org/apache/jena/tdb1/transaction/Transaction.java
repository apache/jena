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

package org.apache.jena.tdb1.transaction;
import java.io.IOException;
import java.nio.channels.ClosedByInterruptException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import org.apache.jena.atlas.logging.Log;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.JenaTransactionException;
import org.apache.jena.tdb1.base.file.FileException;
import org.apache.jena.tdb1.store.DatasetGraphTDB;
import org.apache.jena.tdb1.sys.FileRef;
import org.apache.jena.tdb1.sys.SystemTDB;

/** A transaction.  Much of the work is done in the transaction manager */
public class Transaction
{
    private final long id;
    private final String label;
    private final TransactionManager txnMgr;
    private final Journal journal;
    private final TxnType txnType;
    private final TxnType originalTxnType;
    private final ReadWrite mode;

    private final List<ObjectFileTrans> objectFileTrans = new ArrayList<>();
    private final List<BlockMgrJournal> blkMgrs = new ArrayList<>();
    private final List<TransactionLifecycle> others = new ArrayList<>();
    // The dataset this is a transaction over - may be a commited, pending dataset.
    private final DatasetGraphTDB   basedsg;
    private final long version;

    private final List<Iterator<?>> iterators;     // Tracking iterators
    private DatasetGraphTxn         activedsg;
    private TxnState state;

    // How this transaction ended.
    enum TxnOutcome { UNFINISHED, W_ABORTED, W_COMMITED, R_CLOSED, R_ABORTED, R_COMMITED }
    private TxnOutcome outcome;

    private boolean changesPending;

    public Transaction(DatasetGraphTDB dsg, long version, TxnType txnType, ReadWrite mode, long id,  TxnType originalTxnType, String label, TransactionManager txnMgr) {
        this.id = id;
        if (label == null )
            label = "Txn";
        label = label+"["+id+"]";
        switch(mode) {
            case READ : label = label+"/R"; break;
            case WRITE : label = label+"/W"; break;
        }

        this.label = label;
        this.txnMgr = txnMgr;
        this.basedsg = dsg;
        this.version = version;
        this.txnType = txnType;
        this.originalTxnType = originalTxnType;
        this.mode = mode;
        this.journal = ( txnMgr == null ) ? null : txnMgr.getJournal();
        activedsg = null;      // Don't know yet.
        this.iterators = null; //new ArrayList<>();   // Debugging aid.
        state = TxnState.ACTIVE;
        outcome = TxnOutcome.UNFINISHED;
        changesPending = (mode == ReadWrite.WRITE);
    }

    /*
     * Commit is a 4 step process:
     *
     * 1/ commitPrepare - call all the components to tell them we are going to
     * commit.
     *
     * 2/ Actually commit - write the commit point to the journal
     *
     * 3/ commitEnact -- make the changes to the original data
     *
     * 4/ commitClearup -- release resources The transaction manager is the
     * place which knows all the components in a transaction.
     *
     * Synchronization note: The transaction manager can call back into a
     * transaction so make sure that the lock for this object is released before
     * calling into the transaction manager
     */

    public void commit() {
        synchronized (this) {
            // Do prepare, write the COMMIT record.
            // Enacting is left to the TransactionManager.
            switch(mode) {
                case READ:
                    outcome = TxnOutcome.R_COMMITED;
                    break;
                case WRITE:
                    if ( state != TxnState.ACTIVE )
                        throw new TDBTransactionException("Transaction has already committed or aborted");
                    journal.startWrite();
                    try {
                        writerPrepareCommit();
                        journal.commitWrite();
                    } catch (TDBTransactionException ex) {
                        // Should have cleared up already but call anyway ...
                        journal.abortWrite();
                        throw ex;
                    } catch (Throwable th) {
                        // Unexpected.
                        SystemTDB.errlog.error("Unhandled throwable in commit()", th);
                        journal.abortWrite();
                        throw th;
                    }
                    finally {
                        journal.endWrite();
                    }
                    // No errors (the normal case!)
                    outcome = TxnOutcome.W_COMMITED;
                    break;
            }
            state = TxnState.COMMITED;
            // The transaction manager does the enact and clearup calls
        }

        try { txnMgr.notifyCommit(this); }
        catch (RuntimeException ex) {
            if ( isIOException(ex) )
                SystemTDB.errlog.warn("IOException after commit point : transaction commited but internal status not recorded properly : "+ex.getMessage());
            else
                SystemTDB.errlog.warn("Exception after commit point : transaction commited but internal status not recorded properly", ex);
            throw new TDBTransactionException("Exception after commit point - transaction did commit", ex);
        }
    }

    /*
     * Worker method that execute the prepare phase then writes the
     * JournalEntryType.Commit, then syncs the journal.
     * Called inside journal.startWrite/finishWrite from commit().
     * This is followed by (normal case) jounrnal.commitWrite();
     */
    private void writerPrepareCommit() {
        // ---- Prepare

        abandonIfInterruped("Thread interrupt before 'commit' - transaction did not commit");

        try {
            prepare();
        } catch (RuntimeException ex) {
            if ( isIOException(ex) )
                SystemTDB.errlog.warn("IOException during 'prepare' : attempting transaction abort: "+ex.getMessage());
            else
                SystemTDB.errlog.warn("Exception during 'prepare' : attempting transaction abort", ex);
            abandonTxn();
            SystemTDB.errlog.warn("Exception during 'prepare' : transaction aborted", ex);
            throw new TDBTransactionException("Abort during prepare - transaction did not commit", ex);
        }
        // ---- end prepare

        abandonIfInterruped("Thread interrupt during 'prepare' phase - transaction did not commit");

        // -- write the "commit" entry and ensure it is on disk.
        // During this phase, an error leaves the journal "uncertain".
        // Entries are protected by the checksum so the commit record is either
        // valid or it is ignored.

        try {
            // Manual debug : imitate Thread.interupt in the middle of I/O.
            // This causes a ClosedByInterruptException exception and the file is closed
            // possible with no flush which we can't mimic. However for Journal.reopen,
            // this should not matter.
//            if ( true ) {
//                throw new FileException(new ClosedByInterruptException());
//            }
            // **** COMMIT POINT
            journal.write(JournalEntryType.Commit, FileRef.Journal, null);
            journal.sync();
            // **** COMMIT POINT
        }
        // It either did all the commit record or didn't but we don't know which.
        // Some low level system error - probably a sign of something serious like disk error.
        catch(FileException ex)  {
            if ( ex.getCause() instanceof ClosedByInterruptException ) {
                // Undo prepare.

                // Thread interrupt during java I/O.
                // File was closed by java.nio.
                // Reopen - this truncates to the last write start position.
                journal.reopen();

                // This call should clearup the transaction manager.
                rollback();
                SystemTDB.errlog.warn("Thread interrupt during I/O in 'commit' : transaction rollback: "+ex.getMessage());
                throw new TDBTransactionException("Thread interrupt during I/O in 'commit' : transaction rollback.", ex);
            }
            if ( isIOException(ex) )
                SystemTDB.errlog.warn("IOException during 'commit' : transaction may have committed. Attempting rollback: "+ex.getMessage());
            else
                SystemTDB.errlog.warn("Exception during 'commit' : transaction may have committed. Attempting rollback. Details:",ex);
            if ( abandonTxn() ) {
                SystemTDB.errlog.warn("Transaction rollback");
                throw new TDBTransactionException("Exception during 'commit' - transaction rollback.", ex);
            }
            // Very bad. (This should not happen and have been dealt with already.)
            SystemTDB.errlog.error("Transaction rollback failed. System unstable."+
                "\nPlease contact users@jena.apache.org, giving details of the environment and this incident.");
            throw new Error("Exception during 'rollback' - System unstable.", ex);
        }
        catch (Throwable ex) {
            SystemTDB.errlog.warn("Unexpected Throwable during 'commit' : transaction may have committed. Attempting rollback: ",ex);
            if ( abandonTxn() ) {
                SystemTDB.errlog.warn("Transaction rollback");
                throw new TDBTransactionException("Exception during 'commit' - transaction rollback.", ex);
            }
            // Very bad. (This should not happen.)
            SystemTDB.errlog.error("Transaction rollback failed. System unstable.");
            throw new TDBTransactionException("Exception during 'rollback' - System unstable.", ex);
        }

        try {
           committed();
        } catch (RuntimeException ex) {
            if ( isIOException(ex) )
                SystemTDB.errlog.warn("IOException during 'committed'"+ex.getMessage());
            else
                SystemTDB.errlog.warn("Exception during 'committed': "+ex.getMessage(), ex);
            throw new TDBTransactionException("Exception during 'committed' - transaction did commit", ex);
        }
    }

    private void abandonIfInterruped(String msg) {
        // Clears interrupted status
        if (Thread.interrupted()) {
            abandonTxn();
            Thread.currentThread().interrupt();
            throw new TDBTransactionException(msg);
        }
    }

    /** Try to abort, including removing the journal entries (includign commit if written)
     *  Return true for succeeded and false for throwable, state unknown.
     */
    private boolean abandonTxn() {
        try {
            journal.abortWrite();
            // This call should clearup the transaction manager.
            rollback();
            return true;
        } catch (Throwable th) {
            SystemTDB.errlog.warn("Exception during system 'abort'", th);
            return false;
        }
    }

    private boolean isIOException(Throwable ex) {
        while (ex != null) {
            if ( ex instanceof IOException )
                return true;
            ex = ex.getCause();
        }
        return false;
    }

    /*package*/ void forAllComponents(Consumer<TransactionLifecycle> action) {
        objectFileTrans.forEach(action);
        blkMgrs.forEach(action);
        others.forEach(action);
    }

    private void prepare() {
        state = TxnState.PREPARING;
        forAllComponents(x->x.commitPrepare(this));
    }

    private void committed() {
        forAllComponents(x->x.committed(this));
    }

    public void abort() {
        synchronized (this) {
            switch (mode) {
                case READ :
                    state = TxnState.ABORTED;
                    outcome = TxnOutcome.R_ABORTED;
                    break;
                case WRITE :
                    if ( state != TxnState.ACTIVE )
                        throw new TDBTransactionException("Transaction has already committed or aborted");
                    // Application abort. Didn't start writing to the journal.
                    rollback();
                    break;
            }
        }
        try { txnMgr.notifyAbort(this); }
        catch (RuntimeException ex) {
            if ( isIOException(ex) )
                SystemTDB.errlog.warn("IOException during post-abort (transaction did abort): "+ex.getMessage());
            else
                SystemTDB.errlog.warn("Exception during post-abort (transaction did abort)", ex);
            // It's a bit of a mess!
            throw new TDBTransactionException("Exception after abort point - transaction did abort", ex);
        }
    }

    private void rollback() {
        // Ignore state and try to do it anyway.
        //state = TxnState.ABORTED;
        try {
            forAllComponents(x->x.abort(this));
        } catch (RuntimeException ex) {
            if ( isIOException(ex) )
                SystemTDB.errlog.warn("IOException during 'abort' : " + ex.getMessage());
            else
                SystemTDB.errlog.warn("Exception during 'abort'", ex);
            // It's a bit of a mess!
            throw new TDBTransactionException("Exception during rollback abort - transaction did abort", ex);
        }
        state = TxnState.ABORTED;
        outcome = TxnOutcome.W_ABORTED;
        // journal.truncate to last commit
        // Not need currently as the journal is only written in
        // prepare.
    }

    /** transaction close happens after commit/abort
     *  read transactions "auto commit" on close().
     *  write transactions must call abort or commit.
     */
    public void close() {
        //Log.info(this, "Peek = "+peekCount+"; count = "+count);

        JenaTransactionException throwThis = null;

        synchronized (this) {
            switch (state) {
                case CLOSED :
                    return; // Can call close() repeatedly.
                case ACTIVE :
                    if ( mode == ReadWrite.READ ) {
                        commit();
                        outcome = TxnOutcome.R_CLOSED;
                    } else {
                        // Application error: begin(WRITE)...end() with no commit() or abort().
                        abort();
                        String msg = "end() called for WRITE transaction without commit or abort having been called. This causes a forced abort.";
                        throwThis = new JenaTransactionException(msg);
                        // Keep going to clear up.
                    }
                    break;
                default :
            }
            state = TxnState.CLOSED;
            // Clear per-transaction temporary state.
            if ( iterators != null )
                iterators.clear();
        }
        // Called once.
        txnMgr.notifyClose(this);
        if ( throwThis != null )
            throw throwThis;
    }

    /** A write transaction has been processed and all changes propagated back to the database */
    /*package*/ void signalEnacted() {
        synchronized (this)
        {
            if ( ! changesPending )
                Log.warn(this, "Transaction was a read transaction or a write transaction that has already been flushed");
            changesPending = false;
        }
    }

    public TxnType   getTxnType()                   { return originalTxnType; }
    public TxnType   getCurrentTxnType()            { return txnType; }
    public ReadWrite getTxnMode()                   { return mode; }
    public boolean   isRead()                       { return mode == ReadWrite.READ; }
    public boolean   isWrite()                      { return mode == ReadWrite.WRITE; }

    public TxnState  getState()                     { return state; }

    public long getTxnId()                          { return id; }
    public TransactionManager getTxnMgr()           { return txnMgr; }

    public DatasetGraphTxn getActiveDataset()       { return activedsg; }
    public long getVersion()                        { return version; }

    /*package*/ void setActiveDataset(DatasetGraphTxn activedsg) {
        this.activedsg = activedsg;
        if ( activedsg.getTransaction() != this )
            Log.warn(this, "Active DSG does not point to this transaction; "+this);
    }

    /*package*/ Journal getJournal()                     { return journal; }

    private int count = 0;
    private int peekCount = 0;

    public void addIterator(Iterator<? > iter) {
        count++;
        peekCount = Math.max(peekCount, count);
        if ( iterators != null )
            iterators.add(iter);
    }

    // The code does not perfectly record end of iterator.
    public void removeIterator(Iterator<? > iter) {
        count--;
        if ( iterators != null )
            iterators.remove(iter);
        if ( count == 0 ) {
            peekCount = 0;
        }
    }

    // For development and tracking, keep these as separate lists.

    /*package*/ void addComponent(ObjectFileTrans oft) {
        objectFileTrans.add(oft);
    }

    /*package*/ void addComponent(BlockMgrJournal blkMgr) {
        blkMgrs.add(blkMgr);
    }

    /*package*/ void addAdditionaComponent(TransactionLifecycle tlc) {
        others.add(tlc);
    }

    public DatasetGraphTDB getBaseDataset() {
        return basedsg;
    }

    @Override
    public String toString() {
        return "Transaction: " + id + " : Mode=" + mode + " : State=" + state + " : " + basedsg.getLocation().getDirectoryPath();
    }

    public String getLabel() {
        return label;
    }
}
