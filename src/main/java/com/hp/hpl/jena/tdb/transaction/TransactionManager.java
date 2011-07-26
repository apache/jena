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
    
    // Transactions that have commit (and the journal is written) but haven't
    // writted back to the main database. 
    
    private List<Transaction> commitedAwaitingFlush = new ArrayList<Transaction>() ;    
    
    static long transactionId = 1 ;
    
    private int readers = 0 ; 
    private int writers = 0 ;  // 0 or 1
    
    // Misc stats
    private int finishedReads = 0 ;
    private int committedWrite = 0 ;
    private int abortedWrite = 0 ;

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
    
    synchronized
    public DatasetGraphTxn begin(ReadWrite mode, String label)
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
            case READ : readers++ ; break ;
            case WRITE :
                int x = writers++ ;
                if ( x > 0 )
                    throw new TDBTransactionException("Existing active write transaction") ;
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

    synchronized
    public void notifyCommit(Transaction transaction)
    {
        log("commit", transaction) ;

        // Transaction has done the commitPrepare - can we enact it?
        
        if ( ! activeTransactions.contains(transaction) )
            SystemTDB.errlog.warn("Transaction not active: "+transaction.getTxnId()) ;
        
        endTransaction(transaction) ;
        
        if ( transaction.getMode() == ReadWrite.WRITE )
        {
            if ( readers == 0 )
                // Can commit imemdiately.
                commitTransaction(transaction) ;
            else
            {
                // Can't make permentent at the moment.
                commitedAwaitingFlush.add(transaction) ;
                //log.debug("Commit pending: "+transaction.getLabel()); 

                //if ( log.isDebugEnabled() )
                //    log.debug("Commit blocked at the moment") ;
                queue.add(transaction) ;
            }
        }
    }

    private void commitTransaction(Transaction transaction)
    {
        // Really, really do it!
        Iterator<Transactional> iter = transaction.components() ;
        for ( ; iter.hasNext() ; )
        {
            Transactional x = iter.next() ;
            x.commitEnact(transaction) ;
            x.clearup(transaction) ;
        }
        // This cleans up as well.
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
        }

        // Process any pending commits held up due to a reader. 
        if ( readers == 0 && writers == 0 ) 
        {
            // Given this is sync'ed to this TransactionManager, 
            // the query never blocks, nor does it need to be concurrent-safe.
            // later ...
            while ( queue.size() > 0 )
            {
                try {
                    Transaction txn2 = queue.take() ;
                    if ( txn2.getMode() == READ )
                        continue ;
                    log.info("Delayed commit", txn2) ;
                    // This takes a Write lock on the  DSG - this is where it blocks.
                    JournalControl.replay(txn2) ;
                    commitedAwaitingFlush.remove(txn) ;
                } catch (InterruptedException ex)
                { Log.fatal(this, "Interruped!", ex) ; }
            }
        }
        else
        {
            if ( log() ) log(format("Pending transactions: R=%d / W=%d", readers, writers), txn) ;
        }
    }
    
    private void endTransaction(Transaction transaction)
    {
        if ( transaction.getMode() == READ )
            readers-- ;
        else
            writers-- ;
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