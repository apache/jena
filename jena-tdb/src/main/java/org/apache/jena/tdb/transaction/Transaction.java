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
import java.io.IOException ;
import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.query.ReadWrite ;
import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.JenaTransactionException;
import org.apache.jena.tdb.store.DatasetGraphTDB ;
import org.apache.jena.tdb.sys.FileRef ;
import org.apache.jena.tdb.sys.SystemTDB ;

/** A transaction.  Much of the work is done in the transaction manager */
public class Transaction
{
    private final long id ;
    private final String label ;
    private final TransactionManager txnMgr ;
    private final Journal journal ;
    private final TxnType txnType ;
    private final TxnType originalTxnType ;
    private final ReadWrite mode ;
    
    private final List<ObjectFileTrans> objectFileTrans = new ArrayList<>() ;
    private final List<BlockMgrJournal> blkMgrs = new ArrayList<>() ;
    // The dataset this is a transaction over - may be a commited, pending dataset.
    private final DatasetGraphTDB   basedsg ;
    private final long version ;

    private final List<Iterator<?>> iterators ;     // Tracking iterators 
    private DatasetGraphTxn         activedsg ;
    private TxnState state ;
    
    // How this transaction ended.
    enum TxnOutcome { UNFINISHED, W_ABORTED, W_COMMITED, R_CLOSED, R_ABORTED, R_COMMITED }
    private TxnOutcome outcome ;
    
    private boolean changesPending ;
    
    public Transaction(DatasetGraphTDB dsg, long version, TxnType txnType, ReadWrite mode, long id,  TxnType originalTxnType, String label, TransactionManager txnMgr) {
        this.id = id ;
        if (label == null )
            label = "Txn" ;
        label = label+"["+id+"]" ;
        switch(mode) {
            case READ : label = label+"/R" ; break ;
            case WRITE : label = label+"/W" ; break ;
        }
        
        this.label = label ;
        this.txnMgr = txnMgr ;
        this.basedsg = dsg ;
        this.version = version ;
        this.txnType = txnType ;
        this.originalTxnType = originalTxnType ; 
        this.mode = mode ;
        this.journal = ( txnMgr == null ) ? null : txnMgr.getJournal() ;
        activedsg = null ;      // Don't know yet.
        this.iterators = null ; //new ArrayList<>() ;   // Debugging aid.
        state = TxnState.ACTIVE ;
        outcome = TxnOutcome.UNFINISHED ;
        changesPending = (mode == ReadWrite.WRITE) ;
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
                    outcome = TxnOutcome.R_COMMITED ;
                    break ;
                case WRITE:
                    if ( state != TxnState.ACTIVE )
                        throw new TDBTransactionException("Transaction has already committed or aborted") ;
                    // ---- Prepare
                    try {
                        prepare() ;
                    } catch (RuntimeException ex)
                    {
                        if ( isIOException(ex) )
                            SystemTDB.errlog.warn("IOException during 'prepare' : attempting transaction abort: "+ex.getMessage()) ;
                        else
                            SystemTDB.errlog.warn("Exception during 'prepare' : attempting transaction abort", ex) ;
                        state = TxnState.ACTIVE ;
                        try {
                            abort() ;
                        } catch (RuntimeException ex2) 
                        {
                            // It's a mess.
                            SystemTDB.errlog.warn("Exception during 'abort' after 'prepare'", ex2) ;
                        }
                        throw new TDBTransactionException("Abort during prepare - transaction did not commit", ex) ;
                    }
                    // ---- end prepare
                    
                    try {
                        journal.write(JournalEntryType.Commit, FileRef.Journal, null) ;
                        // **** COMMIT POINT
                        journal.sync() ;
                        // **** COMMIT POINT
                    } catch (RuntimeException ex) {
                        // It either did all commit or didn't but we don't know which.
                        // Some low level system error - probably a sign of something
                        // serious like disk error. 
                        if ( isIOException(ex) )
                            SystemTDB.errlog.warn("IOException during 'commit' : transaction status not known (but not a partial commit): "+ex.getMessage()) ;
                        else
                            SystemTDB.errlog.warn("Exception during 'commit' : transaction status not known (but not a partial commit): ",ex) ;
                        throw new TDBTransactionException("Exception at commit point", ex) ;
                    }
                    outcome = TxnOutcome.W_COMMITED ;
                    break ;
            }

            state = TxnState.COMMITED ;
            // The transaction manager does the enact and clearup calls
        }
        
        try { txnMgr.notifyCommit(this) ; }
        catch (RuntimeException ex) {
            if ( isIOException(ex) )
                SystemTDB.errlog.warn("IOException after commit point : transaction commited but internal status not recorded properly : "+ex.getMessage()) ;
            else
                SystemTDB.errlog.warn("Exception after commit point : transaction commited but internal status not recorded properly", ex) ;
            throw new TDBTransactionException("Exception after commit point - transaction did commit", ex) ;
        }
    }
    
    private boolean isIOException(Throwable ex) {
        while (ex != null) {
            if ( ex instanceof IOException )
                return true ;
            ex = ex.getCause() ;
        }
        return false ;
    }

    private void prepare() {
        state = TxnState.PREPARING ;
        for ( TransactionLifecycle x : objectFileTrans )
            x.commitPrepare(this) ;
        for ( TransactionLifecycle x : blkMgrs )
            x.commitPrepare(this) ;
    }

    public void abort() {
        synchronized (this) {
            switch (mode) {
                case READ :
                    state = TxnState.ABORTED ;
                    outcome = TxnOutcome.R_ABORTED ;
                    break ;
                case WRITE :
                    if ( state != TxnState.ACTIVE )
                        throw new TDBTransactionException("Transaction has already committed or aborted") ;
                    try {
                        // Clearup.
                        for ( TransactionLifecycle x : objectFileTrans )
                            x.abort(this) ;
                        for ( TransactionLifecycle x : blkMgrs )
                            x.abort(this) ;
                    }
                    catch (RuntimeException ex) {
                        if ( isIOException(ex) )
                            SystemTDB.errlog.warn("IOException during 'abort' : " + ex.getMessage()) ;
                        else
                            SystemTDB.errlog.warn("Exception during 'abort'", ex) ;
                        // It's a bit of a mess!
                        throw new TDBTransactionException("Exception during abort - transaction did abort", ex) ;
                    }
                    state = TxnState.ABORTED ;
                    outcome = TxnOutcome.W_ABORTED ;
                    // journal.truncate to last commit
                    // Not need currently as the journal is only written in
                    // prepare.
                    break ;
            }
        }
        try { txnMgr.notifyAbort(this) ; } 
        catch (RuntimeException ex) {
            if ( isIOException(ex) )
                SystemTDB.errlog.warn("IOException during post-abort (transaction did abort): "+ex.getMessage()) ;
            else
                SystemTDB.errlog.warn("Exception during post-abort (transaction did abort)", ex) ;
            // It's a bit of a mess!
            throw new TDBTransactionException("Exception after abort point - transaction did abort", ex) ;
        }
    }

    /** transaction close happens after commit/abort 
     *  read transactions "auto commit" on close().
     *  write transactions must call abort or commit.
     */
    public void close() {
        //Log.info(this, "Peek = "+peekCount+" ; count = "+count) ; 
        
        JenaTransactionException throwThis = null;
        
        synchronized (this) {
            switch (state) {
                case CLOSED :
                    return ; // Can call close() repeatedly.
                case ACTIVE :
                    if ( mode == ReadWrite.READ ) {
                        commit() ;
                        outcome = TxnOutcome.R_CLOSED ;
                    } else {
                        // Application error: begin(WRITE)...end() with no commit() or abort(). 
                        abort() ;
                        String msg = "end() called for WRITE transaction without commit or abort having been called. This causes a forced abort.";
                        throwThis = new JenaTransactionException(msg);
                        // Keep going to clear up.
                    }
                    break ;
                default :
            }
            state = TxnState.CLOSED ;
            // Clear per-transaction temporary state.
            if ( iterators != null )
                iterators.clear() ;
        }
        // Called once.
        txnMgr.notifyClose(this) ;
        if ( throwThis != null )
            throw throwThis;
    }
    
    /** A write transaction has been processed and all changes propagated back to the database */  
    /*package*/ void signalEnacted()
    {
        synchronized (this)
        {
            if ( ! changesPending )
                Log.warn(this, "Transaction was a read transaction or a write transaction that has already been flushed") ; 
            changesPending = false ;
        }
    }

    public TxnType   getTxnType()                   { return originalTxnType ; }
    public TxnType   getCurrentTxnType()            { return txnType ; }
    public ReadWrite getTxnMode()                   { return mode ; }
    public boolean   isRead()                       { return mode == ReadWrite.READ ; }
    public boolean   isWrite()                      { return mode == ReadWrite.WRITE ; }

    public TxnState  getState()                     { return state ; }
    
    public long getTxnId()                          { return id ; }
    public TransactionManager getTxnMgr()           { return txnMgr ; }
    
    public DatasetGraphTxn getActiveDataset()       { return activedsg ; }
    public long getVersion()                        { return version ; }

    /*package*/ void setActiveDataset(DatasetGraphTxn activedsg) { 
        this.activedsg = activedsg ;
        if ( activedsg.getTransaction() != this )
            Log.warn(this, "Active DSG does not point to this transaction; "+this) ;
    }

    /*package*/ Journal getJournal()                     { return journal ; }

    private int count = 0 ;
    private int peekCount = 0 ;

    public void addIterator(Iterator<? > iter) {
        count++ ;
        peekCount = Math.max(peekCount, count) ;
        if ( iterators != null )
            iterators.add(iter) ;
    }

    // The code does not perfectly record end of iterator.
    public void removeIterator(Iterator<? > iter) {
        count-- ;
        if ( iterators != null )
            iterators.remove(iter) ;
        if ( count == 0 ) {
            peekCount = 0 ;
        }
    }
    
    /** Return the list of items registered for the transaction lifecycle */ 
    public List<TransactionLifecycle> lifecycleComponents() {
        List<TransactionLifecycle> x = new ArrayList<>() ;
        x.addAll(objectFileTrans) ;
        x.addAll(blkMgrs) ;
        return x ;
    }
    
    /*package*/ void addComponent(ObjectFileTrans oft) {
        objectFileTrans.add(oft);
    }

    /*package*/ void addComponent(BlockMgrJournal blkMgr) {
        blkMgrs.add(blkMgr) ;
    }

    public DatasetGraphTDB getBaseDataset() {
        return basedsg ;
    }

    @Override
    public String toString() {
        return "Transaction: " + id + " : Mode=" + mode + " : State=" + state + " : " + basedsg.getLocation().getDirectoryPath() ;
    }

    public String getLabel() {
        return label ;
    }

}
