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

import com.hp.hpl.jena.query.ReadWrite ;
import com.hp.hpl.jena.sparql.JenaTransactionException ;
import com.hp.hpl.jena.tdb.StoreConnection ;
import com.hp.hpl.jena.tdb.TDB ;
import com.hp.hpl.jena.tdb.base.file.Location ;
import com.hp.hpl.jena.tdb.migrate.DatasetGraphTrackActive ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;

/** Transactional DatasetGraph that allows one active transaction.
 * For multiple read transactions, create multiple DatasetGraphTransaction objects.
 * This is analogous to a "connection" in JDBC.
 */

public class DatasetGraphTransaction extends DatasetGraphTrackActive
{
    /* Initially, the app can use this DatasetGraph non-transactionally.
     * But as soon as it starts a transaction, the dataset can only be used
     * inside transactions. 
     */
    
    // Two per-thread state variables:
    //   txn: ThreadLocalTxn -- the transactional , one time use dataset
    //   isInTransactionB: ThreadLocalBoolean -- flags true between begin and commit/abort, and end for read transactions.

    static class ThreadLocalTxn extends ThreadLocal<DatasetGraphTxn>
    {
        // This is the default - but nice to give it a name and to set it clearly.
        @Override protected DatasetGraphTxn initialValue() {
            return null ;
        }
    }

    static class ThreadLocalBoolean extends ThreadLocal<Boolean>
    {
        @Override protected Boolean initialValue() {
            return false ;
        }
    }

    @Override
    protected void finalize() throws Throwable
    {
//        if ( txn.get() != null )
//            txn.get().abort() ;
        txn.remove() ;
    }

    // Transaction per thread.
    private ThreadLocalTxn txn = new ThreadLocalTxn() ;
    private ThreadLocalBoolean inTransaction = new ThreadLocalBoolean() ;
    
    private final StoreConnection sConn ;

    public DatasetGraphTransaction(Location location)
    {
        sConn = StoreConnection.make(location) ;
    }

    public Location getLocation()       { return sConn.getLocation() ; }
    
    public DatasetGraphTDB getDatasetGraphToQuery()
    {
        return get() ;
    }
    
    public DatasetGraphTDB getBaseDatasetGraph()
    {
        return sConn.getBaseDataset() ;
    }

    @Override
    protected DatasetGraphTDB get()
    {
        if ( isInTransaction() )
        {
            DatasetGraphTxn dsgTxn = txn.get() ;
            if ( dsgTxn == null )
                throw new TDBTransactionException("In a transaction but no transactional DatasetGraph") ;
            return dsgTxn.getView() ;
        }
        
        if ( sConn.haveUsedInTransaction() )
            throw new TDBTransactionException("Not in a transaction") ;

        // Never used in a transaction - return underlying database for old style (non-transactional) usage.  
        return sConn.getBaseDataset() ;
    }

    @Override
    protected void checkActive()
    {
        if ( sConn.haveUsedInTransaction() && ! isInTransaction() )
            throw new JenaTransactionException("Not in a transaction ("+getLocation()+")") ;
    }

    @Override
    protected void checkNotActive()
    {
        if ( sConn.haveUsedInTransaction() && isInTransaction() )
            throw new JenaTransactionException("Currently in a transaction ("+getLocation()+")") ;
    }
    
    @Override
    public boolean isInTransaction()    
    { 
        return inTransaction.get() ;
    }

    
    public void syncIfNotTransactional()
    {
        if ( ! sConn.haveUsedInTransaction() )
            sConn.getBaseDataset().sync() ;
    }

    
    @Override
    protected void _begin(ReadWrite readWrite)
    {
        DatasetGraphTxn dsgTxn = sConn.begin(readWrite) ;
        txn.set(dsgTxn) ;
        inTransaction.set(true) ;
    }

    @Override
    protected void _commit()
    {
        txn.get().commit() ;
        inTransaction.set(false) ;
    }

    @Override
    protected void _abort()
    {
        txn.get().abort() ;
        inTransaction.set(false) ;
    }

    @Override
    protected void _end()
    {
        DatasetGraphTxn dsg = txn.get() ;
        // It's null if end() already called.
        if ( dsg  == null )
        {
            TDB.logInfo.warn("Transaction already ended") ;
            return ;
            // throw new TDBTransactionException("Transaction already ended") ;
        }
        txn.get().end() ;
        // May already be false due to .commit/.abort.
        inTransaction.set(false) ;
        txn.set(null) ;
    }

    @Override
    public String toString()
    {
        try {
            // Risky ... 
            return get().toString() ;
            // Hence ...
        } catch (Throwable th) { return "DatasetGraphTransactional" ; }
    }
    
    @Override
    protected void _close()
    {
        if ( ! sConn.haveUsedInTransaction() && get() != null )
        {
            // Non-transactional behaviour.
            DatasetGraphTDB dsg = get() ;
            dsg.sync() ;
            dsg.close() ;
        }
    }
}
