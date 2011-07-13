/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
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
    private Journal journal = null ;
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
        activedsg = null ;      // Don't know yet.
        this.iterators = new ArrayList<Iterator<?>>() ;
        state = TxnState.ACTIVE ;
    }

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
            JournalEntry entry = new JournalEntry(JournalEntryType.Commit, FileRef.Journal, null) ;
            journal.writeJournal(entry) ;
            journal.sync() ;        // Commit point.
        }

        state = TxnState.COMMITED ;
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

    /** For testing - do things but do not write the commit record */
    public void semiCommit()
    {
        prepare() ;
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
        // [TxTDB:TODO] : No - truncates a pending transaction.
        journal.truncate(0) ;

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
    TransactionManager getTxnMgr()                  { return txnMgr ; }
    
    public DatasetGraphTxn getActiveDataset()
    {
        return activedsg ;
    }

    public void setActiveDataset(DatasetGraphTxn activedsg)
    {
        this.activedsg = activedsg ;
    }

    public void addIterator(Iterator<?> iter)       { iterators.add(iter) ; }
    public void removeIterator(Iterator<?> iter)    { iterators.remove(iter) ; }
    public List<Iterator<?>> iterators()            { return Collections.unmodifiableList(iterators) ; }
    
    public Iterator<Transactional> components()
    {
        // FIX NEEDED
        List<Transactional> x = new ArrayList<Transactional>() ;
        x.addAll(nodeTableTrans) ;
        x.addAll(blkMgrs) ;
        return x.iterator() ;
    }
    
    public void setJournal(Journal journal)
    {
        this.journal = journal ;
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

/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */