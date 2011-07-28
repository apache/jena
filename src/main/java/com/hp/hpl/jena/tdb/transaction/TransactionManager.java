/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.transaction;

import static com.hp.hpl.jena.tdb.ReadWrite.READ ;
import static com.hp.hpl.jena.tdb.sys.SystemTDB.syslog ;
import static java.lang.String.format ;

import java.util.ArrayList ;
import java.util.HashSet ;
import java.util.Iterator ;
import java.util.List ;
import java.util.Set ;
import java.util.concurrent.BlockingQueue ;
import java.util.concurrent.LinkedBlockingDeque ;
import java.util.concurrent.Semaphore ;

import org.openjena.atlas.logging.Log ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.tdb.DatasetGraphTxn ;
import com.hp.hpl.jena.tdb.ReadWrite ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

public class TransactionManager
{
    // TODO Don't keep counter, keep lists.
    // TODO Useful logging.
    
    private static Logger log = LoggerFactory.getLogger(TransactionManager.class) ;
    
    private Set<Transaction> activeTransactions = new HashSet<Transaction>() ;
    
    // Transactions that have commited (and the journal is written) but haven't
    // writted back to the main database. 
    
    List<Transaction> commitedAwaitingFlush = new ArrayList<Transaction>() ;    
    
    static long transactionId = 1 ;
    
    int activeReaders = 0 ; 
    int activeWriters = 0 ;  // 0 or 1
    
    // Misc stats
    int finishedReads = 0 ;
    int committedWrite = 0 ;
    int abortedWrite = 0 ;
    
    // Ensure single writer.
    private Semaphore writersWaiting = new Semaphore(1, true) ;
    private BlockingQueue<Transaction> queue = new LinkedBlockingDeque<Transaction>() ;

    private Thread committerThread ;

    private DatasetGraphTDB baseDataset ;
    private Journal journal ;
    
    // it would be better to associate with the DatasetGraph itself. 
    
    //static Map<DatasetGraphTDB, Journal> journals = new HashMap<DatasetGraphTDB, Journal>() ;
    
    public TransactionManager(DatasetGraphTDB dsg)
    {
        this.baseDataset = dsg ; 
        this.journal = Journal.create(dsg.getLocation()) ;
        // LATER
//        Committer c = new Committer() ;
//        this.committerThread = new Thread(c) ;
//        committerThread.setDaemon(true) ;
//        committerThread.start() ;
    }
    
    private Transaction createTransaction(DatasetGraphTDB dsg, ReadWrite mode, String label)
    {
        Transaction txn = new Transaction(dsg, mode, transactionId++, label, this) ;
        return txn ;
    }

    public DatasetGraphTxn begin(ReadWrite mode)
    {
        return begin(mode, null) ;
    }
    
    public DatasetGraphTxn begin(ReadWrite mode, String label)
    {
        // Not synchronized (else blocking on semaphore wil never wake up
        // because Semaphore.release is inside synchronized.
        // Allow only one active writer. 
        if ( mode == ReadWrite.WRITE )
        {
            
            try { writersWaiting.acquire() ; }
            catch (InterruptedException e)
            { 
                log.error(label, e) ;
                throw new TDBTransactionException(e) ;
            }
        }
        return begin$(mode, label) ;
    }
        
    synchronized
    private DatasetGraphTxn begin$(ReadWrite mode, String label)
    {
//        // Subs transactions are a new view - commit is only commit to parent transaction.  
//        if ( dsg instanceof DatasetGraphTxn )
//        {
//            throw new TDBException("Already in transactional DatasetGraph") ;
//            // Either:
//            //   error -> implies nested
//            //   create new transaction 
//        }
        switch (mode)
        {
            case READ : activeReaders++ ; break ;
            case WRITE :
                if ( activeWriters > 0 )    // Guard
                    throw new TDBTransactionException("Existing active write transaction") ;
                activeWriters++ ;
                break ;
        }
        
        DatasetGraphTDB dsg = baseDataset ;
        // *** But, if there are pending, committed transactions, use one.
        if ( ! commitedAwaitingFlush.isEmpty() )
            dsg = commitedAwaitingFlush.get(commitedAwaitingFlush.size()-1).getActiveDataset() ;
        
        Transaction txn = createTransaction(dsg, mode, label) ;
        DatasetGraphTxn dsgTxn = (DatasetGraphTxn)new DatasetBuilderTxn(this).build(txn, mode, dsg) ;
        txn.setActiveDataset(dsgTxn) ;
        Iterator<Transactional> iter = dsgTxn.getTransaction().components() ;
        
        // Notify everyone we're starting.
        for ( ; iter.hasNext() ; )
            iter.next().begin(dsgTxn.getTransaction()) ;

        activeTransactions.add(txn) ;
        log("begin",txn) ;
        return dsgTxn ;
    }

    /* Signal a transaction has commited.  The journal has a commit record
     * and a sync to disk. The code here manages the inter-transaction stage
     *  of deciding how to play the changes back to the base data. 
     */ 
    synchronized
    public void notifyCommit(Transaction transaction)
    {
        log("commit", transaction) ;

        // Transaction has done the commitPrepare - can we enact it?
        
        if ( ! activeTransactions.contains(transaction) )
            SystemTDB.errlog.warn("Transaction not active: "+transaction.getTxnId()) ;
        
        endTransaction(transaction) ;
        
        switch ( transaction.getMode() )
        {
            case READ: 
                endOfRead(transaction) ;
                break ;
            case WRITE:
                if ( activeReaders == 0 )
                    // Can commit imemdiately.
                    enactTransaction(transaction) ;
                else
                {
                    // Can't make permanent at the moment.
                    commitedAwaitingFlush.add(transaction) ;
                    log("Queue commit flush", transaction) ; 
                    //if ( log.isDebugEnabled() )
                    //    log.debug("Commit blocked at the moment") ;
                    queue.add(transaction) ;
                }
                committedWrite ++ ;
                // Allow another writer.
                writersWaiting.release() ;
        }
    }

    /** The stage in a commit after commiting - make the changes permanent in the base data */ 
    private void enactTransaction(Transaction transaction)
    {
        // Really, really do it!
        Iterator<Transactional> iter = transaction.components() ;
        for ( ; iter.hasNext() ; )
        {
            Transactional x = iter.next() ;
            x.commitEnact(transaction) ;
            x.commitClearup(transaction) ;
        }
        // This cleans up the journal as well.
        JournalControl.replay(transaction) ;
    }
    
    synchronized
    public void notifyAbort(Transaction transaction)
    {
        log("abort", transaction) ;
        // Transaction has done the abort on all the transactional elements.
        if ( ! activeTransactions.contains(transaction) )
            SystemTDB.errlog.warn("Transaction not active: "+transaction.getTxnId()) ;
        
        endTransaction(transaction) ;
        
        switch ( transaction.getMode() )
        {
            case READ:
                endOfRead(transaction) ;
                break ;
            case WRITE:
                // Journal cleaned in Transaction.abort.
                abortedWrite ++ ;
        }
    }
    
    /** READ specific final actions. */
    private void endOfRead(Transaction transaction)
    {
        processDelayedReplayQueue(transaction) ;
        finishedReads ++ ;
    }
    
    private void processDelayedReplayQueue(Transaction txn)
    {
        // Sync'ed by notifyCommit.
        // If we knew which version of the DB each was looking at, we could reduce more often here.
        // [TxTDB:TODO]
        if ( activeReaders != 0 || activeWriters != 0 )
        {
            if ( queue.size() > 0 )
                if ( log() ) log(format("Pending transactions: R=%d / W=%d", activeReaders, activeWriters), txn) ;
            return ;
        }
        while ( queue.size() > 0 )
        {
            try {
                Transaction txn2 = queue.take() ;
               
                if ( txn2.getMode() == READ )
                    continue ;
                log("Flush delayed commit", txn2) ;
                // This takes a Write lock on the  DSG - this is where it blocks.
                // **** REPLAYS WHOLE JOURNAL
                // **** Related NodeFileTrans: writes at "prepare" 
                enactTransaction(txn2) ;
                commitedAwaitingFlush.remove(txn2) ;
            } catch (InterruptedException ex)
            { Log.fatal(this, "Interruped!", ex) ; }
        }
    }

    synchronized
    public void notifyClose(Transaction txn)
    {
        log("close", txn) ;
        
        if ( txn.getState() == TxnState.ACTIVE )
        {
            String x = txn.getBaseDataset().getLocation().getDirectoryPath() ;
            syslog.warn("close: Transaction not commited or aborted: Transaction: "+txn.getTxnId()+" @ "+x) ;
            txn.abort() ;
            return ;
        }
    }
        
    private void endTransaction(Transaction transaction)
    {
        switch (transaction.getMode())
        {
            case READ : activeReaders-- ; break ;
            case WRITE : activeWriters-- ; break ;
        }
        activeTransactions.remove(transaction) ;
    }
    
    public Journal getJournal()
    {
        return journal ;
    }

    private boolean log()
    {
        return syslog.isDebugEnabled() || log.isDebugEnabled() ;
    }
    
    private void log(String msg, Transaction txn)
    {
        if ( ! log() )
            return ;
        if ( syslog.isDebugEnabled() )
            syslog.debug(txn.getLabel()+": "+msg) ;
        else
            log.debug(txn.getLabel()+": "+msg) ;
    }

    synchronized
    public SysTxnState state()
    { return new SysTxnState(this) ; }
    
    // LATER.
    class Committer implements Runnable
    {
        @Override
        public void run()
        {
            for(;;)
            {
                // Wait until the reader count goes to zero.
                
                // This wakes up for every transation but maybe 
                // able to play several transactions at once (later).
                try {
                    Transaction txn = queue.take() ;
                    // This takes a Write lock on the  DSG - this is where it blocks.
                    JournalControl.replay(txn) ;
                    synchronized(TransactionManager.this)
                    {
                        commitedAwaitingFlush.remove(txn) ;
                    }
                } catch (InterruptedException ex)
                { Log.fatal(this, "Interruped!", ex) ; }
            }
        }
        
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