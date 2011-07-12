/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.transaction;

import static com.hp.hpl.jena.tdb.ReadWrite.READ ;
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

import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.tdb.DatasetGraphTxn ;
import com.hp.hpl.jena.tdb.ReadWrite ;
import com.hp.hpl.jena.tdb.TDBException ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

public class TransactionManager
{
    // TODO Don't keep counter, keep lists.
    
    private static Logger log = SystemTDB.syslog ;
    
    private Set<Transaction> activeTransactions = new HashSet<Transaction>() ;
    
    // Transactions that have commit (and the journal is written) but haven't
    // writted back to the main database. 
    private List<Transaction> commitedAwaitingFlush = new ArrayList<Transaction>() ;    
    static long transactionId = 1 ;
    
    private int readers = 0 ; 
    private int writers = 0 ;       // 0 or 1 
    private int committed = 0 ;

    private BlockingQueue<Transaction> queue = new LinkedBlockingDeque<Transaction>() ;

    private Thread committerThread ;
    
    
    public TransactionManager()
    { 
        // LATER
//        Committer c = new Committer() ;
//        this.committerThread = new Thread(c) ;
//        committerThread.setDaemon(true) ;
//        committerThread.start() ;
    }
    
    private Transaction createTransaction(DatasetGraphTDB dsg, ReadWrite mode)
    {
        Transaction txn = new Transaction(dsg, mode, transactionId++, this) ;
        return txn ;
    }
    
    synchronized
    public DatasetGraphTxn begin(DatasetGraph dsg, ReadWrite mode)
    {
        if ( log.isDebugEnabled() )
            log.debug(format("begin: R={} / W={} / #={}", readers, writers,activeTransactions.size())) ;
        
        // If already a transaction ... 
        // Subs transactions are a new view - commit is only commit to parent transaction.  
        if ( dsg instanceof DatasetGraphTxn )
        {
            throw new TDBException("Already in transactional DatasetGraph") ;
            // Either:
            //   error -> implies nested
            //   create new transaction 
        }
        
        if ( ! ( dsg instanceof DatasetGraphTDB ) )
            throw new TDBException("Not a TDB-backed dataset") ;

        switch (mode)
        {
            case READ : readers++ ; break ;
            case WRITE :
                if ( writers > 0 )
                    throw new TDBTransactionException("Existing active transaction") ;
                writers ++ ;
                break ;
        }
        
        DatasetGraphTDB dsgtdb = (DatasetGraphTDB)dsg ;
        // THIS IS NECESSARY BECAUSE THE DATASET MAY HAVE BEEN UPDATED AND CHANGES STILL IN CACHES.
        // MUST WRITE OUT - BUT ALSO REUSE CACHES.
        dsgtdb.sync() ; 
        
        Transaction txn = createTransaction(dsgtdb, mode) ;
        
        DatasetGraphTxn dsgTxn = (DatasetGraphTxn)new DatasetBuilderTxn(this).build(txn, mode, dsgtdb) ;
        Iterator<Transactional> iter = dsgTxn.getTransaction().components() ;
        for ( ; iter.hasNext() ; )
            iter.next().begin(dsgTxn.getTransaction()) ;

        activeTransactions.add(txn) ;
        if ( log.isDebugEnabled() )
            log.debug("begin: "+txn) ;
        return dsgTxn ;
    }

    synchronized
    public void notifyCommit(Transaction transaction)
    {
        if ( log.isDebugEnabled() )
            log.debug("commit: "+transaction) ;

        if ( ! activeTransactions.contains(transaction) )
            SystemTDB.errlog.warn("Transaction not active: "+transaction.getTxnId()) ;
        endTransaction(transaction) ;
        
        if ( readers == 0 && transaction.getMode() == ReadWrite.WRITE )
            // New readers blocked from starting by the synchronized here and on begin. 
            JournalControl.replay(transaction.getJournal(), transaction.getBaseDataset()) ;
        else
        {
            commitedAwaitingFlush.add(transaction) ;
            if ( log.isDebugEnabled() )
                log.info("Commit blocked at the moment") ;
            queue.add(transaction) ;
        }
    }

    synchronized
    public void notifyAbort(Transaction transaction)
    {    
        if ( log.isDebugEnabled() )
            log.info("notifyAbort: "+transaction) ;
        if ( ! activeTransactions.contains(transaction) )
            SystemTDB.errlog.warn("Transaction not active: "+transaction.getTxnId()) ;
        endTransaction(transaction) ;
    }
    
    synchronized
    public void notifyClose(Transaction txn)
    {
        if ( log.isDebugEnabled() )
            log.debug("notifyClose: "+txn) ;
        
        if ( txn.getState() == TxnState.ACTIVE )
        {
            String x = txn.getBaseDataset().getLocation().getDirectoryPath() ;
            SystemTDB.syslog.warn("close: Transaction not commited or aborted: Transaction: "+txn.getTxnId()+" @ "+x) ;
            txn.abort() ;
        }

        if ( readers == 0 && writers == 0 ) 
        {
            // Given this is sync'ed to the TransactionManager, 
            // the query never blocks, nor does it need to be concurrent-safe.
            // later ...
            while ( queue.size() > 0 )
            {
                try {
                    Transaction txn2 = queue.take() ;
                    if ( txn2.getMode() == READ )
                        continue ;
                    log.info("Delayed commit") ;
                    // This takes a Write lock on the  DSG - this is where it blocks.
                    JournalControl.replay(txn2.getJournal(), txn2.getBaseDataset()) ;
                    log.info("Delayed commit succeeded") ;
                    commitedAwaitingFlush.remove(txn) ;
                } catch (InterruptedException ex)
                { Log.fatal(this, "Interruped!", ex) ; }
            }
        }
        else
        {
            if ( log.isDebugEnabled() )
                log.debug(format("Pending transactions: R=%d / W=%d", readers, writers)) ;
        }
    }
    
    private void endTransaction(Transaction transaction)
    {
        if ( log.isDebugEnabled() )
            log.debug("endTransaction: "+transaction) ;
        
        if ( transaction.getMode() == READ )
            readers-- ;
        else
            writers-- ;
        activeTransactions.remove(transaction) ;
        
        if ( log.isDebugEnabled() )
            log.debug(format("endTransaction: R=%d / W=%d / #=%d\n", readers, writers,activeTransactions.size())) ;
        
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
                    System.out.println("Async commit") ;
                    // This takes a Write lock on the  DSG - this is where it blocks.
                    JournalControl.replay(txn.getJournal(), txn.getBaseDataset()) ;
                    System.out.println("Async commit succeeded") ;
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