/**
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

import com.hp.hpl.jena.tdb.DatasetGraphTxn ;
import com.hp.hpl.jena.tdb.ReadWrite ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.sys.FileRef ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

/** A transaction.  Much of the work is done in the transaction manager */
public class Transaction
{
    // TODO split internal and external features.
    private final long id ;
    private final String label ;
    private final TransactionManager txnMgr ;
    private final List<Iterator<?>> iterators ; 
    private final Journal journal ;
    private TxnState state ;
    private ReadWrite mode ;
    
    private final List<NodeTableTrans> nodeTableTrans = new ArrayList<NodeTableTrans>() ;
    private final List<BlockMgrJournal> blkMgrs = new ArrayList<BlockMgrJournal>() ;
    // The dataset this is a transaction over - may be a commited, pending dataset.
    private DatasetGraphTDB     basedsg ;
    private DatasetGraphTxn     activedsg ;

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
    }

    /* Commit is a 4 step process
     * 1/ commitPrepare - call all the components to tell them we are going to commit.
     * 2/ Actually commit - write the commit point to the journal
     * 3/ commitEnact -- make the changes to the original data
     * 4/ commitClearup -- release resources
     * The transaction manager is the place which knows all the components in a transaction. 
     */
    
    synchronized
    public void commit()
    {
        // Do prepare, write the COMMIT record.
        // Enacting is left to the TransactionManager.
        if ( mode == ReadWrite.WRITE )
        {
            if ( state != TxnState.ACTIVE )
                throw new TDBTransactionException("Transaction has already committed or aborted") ; 
            prepare() ;
            journal.write(JournalEntryType.Commit, FileRef.Journal, null) ;
            journal.sync() ;        // Commit point.
        }

        state = TxnState.COMMITED ;
        // The transaction manager does the enact and clearup calls 
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

    synchronized
    public void abort()
    { 
        if ( mode == ReadWrite.READ )
        {
            state = TxnState.ABORTED ;
            return ;
        }
        
        if ( state != TxnState.ACTIVE )
            throw new TDBTransactionException("Transaction has already committed or aborted") ; 
        
        // Clearup.
        for ( BlockMgrJournal x : blkMgrs )
            x.abort(this) ;
        
        for ( NodeTableTrans x : nodeTableTrans )
            x.abort(this) ;

        // [TxTDB:TODO]
        // journal.truncate to last commit 
        // No need currently as the jounral is only written in prepare. 
        

        state = TxnState.ABORTED ;
        txnMgr.notifyAbort(this) ;
    }

    /** transaction close happens after commit/abort 
     *  read transactions "auto commit" on close().
     *  write transactions must call abort or commit.
     */
    synchronized
    public void close()
    {
        switch(state)
        {
            case CLOSED:    return ;    // Can call close() repeatedly.
            case ACTIVE:
                if ( mode == ReadWrite.READ )
                    commit() ;
                else
                {
                    SystemTDB.errlog.warn("Transaction not commited or aborted: "+this) ;
                    abort() ;
                }
                break ;
                default:
        }
        
        state = TxnState.CLOSED ;
        txnMgr.notifyClose(this) ;
        
        // Imperfect : too many higher level iterators build on unclosables
        // (e.g. anoniterators in Iter) 
        // so close does not get passed to the base.   
//        for ( Iterator<?> iter : iterators )
//            Log.info(this, "Active iterator: "+iter) ;
        
        // Clear per-transaction temnporary state. 
        iterators.clear() ;
    }
    
    public ReadWrite getMode()                      { return mode ; }
    public TxnState getState()                      { return state ; }
    
    public long getTxnId()                          { return id ; }
    public TransactionManager getTxnMgr()                  { return txnMgr ; }
    
    public DatasetGraphTxn getActiveDataset()
    {
        return activedsg ;
    }

    public void setActiveDataset(DatasetGraphTxn activedsg)
    {
        this.activedsg = activedsg ;
    }

    public synchronized void addIterator(Iterator<?> iter)       { iterators.add(iter) ; }
    public synchronized void removeIterator(Iterator<?> iter)    { iterators.remove(iter) ; }
    public List<Iterator<?>> iterators()            { return Collections.unmodifiableList(iterators) ; }
    
    public List<TransactionLifecycle> components()
    {
        // FIX NEEDED
        List<TransactionLifecycle> x = new ArrayList<TransactionLifecycle>() ;
        x.addAll(nodeTableTrans) ;
        x.addAll(blkMgrs) ;
        return x ;
    }
    
    public Journal getJournal()    { return journal ; }

    public void add(NodeTableTrans ntt)
    {
        nodeTableTrans.add(ntt) ;
    }

    public void add(BlockMgrJournal blkMgr)
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
