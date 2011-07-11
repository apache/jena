/*
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.transaction;


import com.hp.hpl.jena.tdb.TxnState ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

public class DatasetGraphTxnTDB extends DatasetGraphTDB
{
    private final Transaction transaction ;
    TxnState state = TxnState.BEGIN ;       // TODO Part of transaction -- but checked in close()

    public DatasetGraphTxnTDB(DatasetGraphTDB dsg, Transaction txn)
    {
        super(dsg) ;
        this.transaction = txn ;
    }

    public Transaction getTransaction() { return transaction ; }
    
    synchronized
    public void commit()
    {
        if ( state != TxnState.BEGIN )
        {
            SystemTDB.syslog.warn("Can't commit: Transaction is already "+state) ;
            throw new TDBTransactionException("commit: Illegal state: "+state) ;
        }
        state = TxnState.COMMITTED ;
        transaction.commit() ;
    }

    synchronized
    public void abort()
    {
        if ( state != TxnState.BEGIN )
        {
            SystemTDB.syslog.warn("Can't abort: Transaction is already "+state) ;
            throw new TDBTransactionException("abort: Illegal state: "+state) ;
        }
        state = TxnState.ABORTED ;
        transaction.abort() ;
    }
    
    @Override
    public String toString()
    { return "Txn:"+super.toString() ; }
    
    @Override
    synchronized
    public void close()
    {
        if ( state == TxnState.BEGIN )
        {
            SystemTDB.syslog.warn("close: Transaction not commited or aborted: Transaction: "+transaction.getTxnId()+" @ "+getLocation().getDirectoryPath()) ;
            abort() ;
        }
        //Don't really close.
        //super.close() ;
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