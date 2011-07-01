/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.transaction;


import java.util.Iterator ;

import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.tdb.TDBException ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;



public class TransactionManager
{
    static long transactionId = 10 ;
    
    public TransactionManager()
    {
    }
    
    public Transaction createTransaction()
    {
        Transaction txn = new Transaction(transactionId++, this) ;
        return txn ;
    }
    
    public DatasetGraphTxnTDB begin(DatasetGraph dsg)
    {
        // If already a transaction ... 
        // Subs transactions are a new view - commit is only commit to parent transaction.  
        if ( dsg instanceof DatasetGraphTxnTDB )
        {
            throw new TDBException("Already in transactional DatasetGraph") ;
            // Either:
            //   error -> implies nested
            //   create new transaction 
        }
        
        if ( ! ( dsg instanceof DatasetGraphTDB ) )
            throw new TDBException("Not a TDB-backed dataset") ;

        DatasetGraphTDB dsgtdb = (DatasetGraphTDB)dsg ;
        // THIS IS NECESSARY BECAUSE THE DATASET MAY HAVE BEEN UPDATED AND CHANGES STILL IN CACHES.
        // MUST WRITE OUT - BUT ALSO REUSE CACHES.
        dsgtdb.sync() ; 
        
        DatasetGraphTxnTDB dsgTxn = (DatasetGraphTxnTDB)new DatasetBuilderTxn(new TransactionManager()).build(dsgtdb) ;
        
        Iterator<Transactional> iter = dsgTxn.getTransaction().components() ;
        for ( ; iter.hasNext() ; )
            iter.next().begin(dsgTxn.getTransaction()) ;
        return dsgTxn ;
    }

    public void commit(Transaction transaction)
    {
        transaction.commit() ;
    }

    public void abort(Transaction transaction)
    {    
        transaction.abort() ;
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