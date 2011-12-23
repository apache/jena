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

package com.hp.hpl.jena.tdb.transaction;
import java.util.ArrayList ;
import java.util.Collections ;
import java.util.Iterator ;
import java.util.List ;

import org.openjena.atlas.logging.Log ;

import com.hp.hpl.jena.query.ReadWrite ;
import com.hp.hpl.jena.tdb.DatasetGraphTxn ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.sys.FileRef ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

/** A transaction.  Much of the work is done in the transaction manager */
public class Transaction
{
    private final long id ;
    private final String label ;
    private final TransactionManager txnMgr ;
    private final Journal journal ;
    private final ReadWrite mode ;
    
    private final List<NodeTableTrans> nodeTableTrans = new ArrayList<NodeTableTrans>() ;
    private final List<BlockMgrJournal> blkMgrs = new ArrayList<BlockMgrJournal>() ;
    // The dataset this is a transaction over - may be a commited, pending dataset.
    private final DatasetGraphTDB   basedsg ;

    private final List<Iterator<?>> iterators ;     // Tracking iterators 
    private DatasetGraphTxn         activedsg ;
    private TxnState state ;
    
    // How this transaction ended.
    enum TxnOutcome { UNFINISHED, W_ABORTED, W_COMMITED, R_CLOSED, R_ABORTED, R_COMMITED }
    private TxnOutcome outcome ;
    
    private boolean changesPending ;
    
    public Transaction(DatasetGraphTDB dsg, ReadWrite mode, long id, String label, TransactionManager txnMgr)
    {
        this.id = id ;
        if (label == null )
            label = "Txn" ;
        label = label+"["+id+"]" ;
        switch(mode)
        {
            case READ : label = label+"/R" ; break ;
            case WRITE : label = label+"/W" ; break ;
        }
        
        this.label = label ;
        this.txnMgr = txnMgr ;
        this.basedsg = dsg ;
        this.mode = mode ;
        this.journal = ( txnMgr == null ) ? null : txnMgr.getJournal() ;
        activedsg = null ;      // Don't know yet.
        this.iterators = new ArrayList<Iterator<?>>() ;
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
    
    public void commit()
    {
        synchronized (this)
        {
            // Do prepare, write the COMMIT record.
            // Enacting is left to the TransactionManager.
            switch(mode)
            {
                case READ:
                    outcome = TxnOutcome.R_COMMITED ;
                    break ;
                case WRITE:
                    if ( state != TxnState.ACTIVE )
                        throw new TDBTransactionException("Transaction has already committed or aborted") ; 
                    prepare() ;
                    journal.write(JournalEntryType.Commit, FileRef.Journal, null) ;
                    journal.sync() ;        // Commit point.
                    outcome = TxnOutcome.W_COMMITED ;
                    break ;
            }

            state = TxnState.COMMITED ;
            // The transaction manager does the enact and clearup calls
        }
        txnMgr.notifyCommit(this) ;
    }

    private void prepare()
    {
        state = TxnState.PREPARING ;
        for ( BlockMgrJournal x : blkMgrs )
            x.commitPrepare(this) ;
        for ( NodeTableTrans x : nodeTableTrans )
            x.commitPrepare(this) ;
    }

    public void abort()
    { 
        synchronized (this)
        {
            switch(mode)
            {
                case READ:
                    state = TxnState.ABORTED ;
                    outcome = TxnOutcome.R_ABORTED ;
                    break ;
                case WRITE:
                    if ( state != TxnState.ACTIVE )
                        throw new TDBTransactionException("Transaction has already committed or aborted") ; 

                    // Clearup.
                    for ( BlockMgrJournal x : blkMgrs )
                        x.abort(this) ;

                    for ( NodeTableTrans x : nodeTableTrans )
                        x.abort(this) ;
                    state = TxnState.ABORTED ;
                    outcome = TxnOutcome.W_ABORTED ;
                    // [TxTDB:TODO]
                    // journal.truncate to last commit 
                    // Not need currently as the journal is only written in prepare. 
                    break ;
            }
        }
        txnMgr.notifyAbort(this) ;
    }

    /** transaction close happens after commit/abort 
     *  read transactions "auto commit" on close().
     *  write transactions must call abort or commit.
     */
    public void close()
    {
        synchronized (this)
        {
            switch(state)
            {
                case CLOSED:    return ;    // Can call close() repeatedly.
                case ACTIVE:
                    if ( mode == ReadWrite.READ )
                    {    
                        commit() ;
                        outcome = TxnOutcome.R_CLOSED ;
                    }
                    else
                    {
                        SystemTDB.errlog.warn("Transaction not commited or aborted: "+this) ;
                        abort() ;
                    }
                    break ;
                default:
            }
            state = TxnState.CLOSED ;
            // Imperfect : too many higher level iterators build on unclosables
            // (e.g. anon iterators in Iter) 
            // so close does not get passed to the base.   
//            for ( Iterator<?> iter : iterators )
//                Log.info(this, "Active iterator: "+iter) ;
            
            // Clear per-transaction temporary state. 
            iterators.clear() ;
        }
        // Called once.
        txnMgr.notifyClose(this) ;
    }
    
    /** A write transaction has been processed and all chanages propagated back to the database */  
    /*package*/ void signalEnacted()
    {
        synchronized (this)
        {
            if ( ! changesPending )
                Log.warn(this, "Transaction was a read transaction or a write transaction that has already been flushed") ; 
            changesPending = false ;
        }
    }

    public ReadWrite getMode()                      { return mode ; }
    public boolean   isRead()                       { return mode == ReadWrite.READ ; }
    public TxnState  getState()                     { return state ; }
    
    public long getTxnId()                          { return id ; }
    public TransactionManager getTxnMgr()           { return txnMgr ; }
    
    public DatasetGraphTxn getActiveDataset()       { return activedsg ; }

    public void setActiveDataset(DatasetGraphTxn activedsg)
    { this.activedsg = activedsg ; }

    public Journal getJournal()                     { return journal ; }

    public List<Iterator<?>> iterators()            { return Collections.unmodifiableList(iterators) ; }
    
    public void addIterator(Iterator<?> iter)       { iterators.add(iter) ; }
    public void removeIterator(Iterator<?> iter)    { iterators.remove(iter) ; }
    
    // Debugging versions - concurrency problems show up because concurrent access
    // to iterators.contains can miss entries when removed by abother thread.
    // See JENA-131.
    // After TDB 0.9 release, remove debug code.

//    private static final boolean DEBUG = false ;     // Don't check-in to SVN trunk with this set to true.
//
//    public void addIterator(Iterator<?> iter)
//    {
//        if ( ! DEBUG )
//            iterators.add(iter) ;
//        else
//        {
//            if ( iterators.contains(iter) )
//                System.err.println("Already added") ;
//            iterators.add(iter) ;
//        }
//    }
//
//    public void removeIterator(Iterator<? > iter)
//    {
//        if ( ! DEBUG )
//            iterators.remove(iter) ;
//        else
//        {
//            if ( ! iterators.contains(iter) )
//                System.err.println("Already closed or not tracked: "+iter) ;
//        }
//    }
    
    public List<TransactionLifecycle> components()
    {
        List<TransactionLifecycle> x = new ArrayList<TransactionLifecycle>() ;
        x.addAll(nodeTableTrans) ;
        x.addAll(blkMgrs) ;
        return x ;
    }
    
    public void addComponent(NodeTableTrans ntt)
    {
        nodeTableTrans.add(ntt) ;
    }

    public void addComponent(BlockMgrJournal blkMgr)
    {
        blkMgrs.add(blkMgr) ;
    }

    public DatasetGraphTDB getBaseDataset()
    {
        return basedsg ;
    }
    
    @Override
    public String toString()
    {
        return "Transaction: "+id+" : Mode="+mode+" : State="+state+" : "+basedsg.getLocation().getDirectoryPath() ;
    }
    
    public String getLabel()
    {
        return label ;
    }
}
