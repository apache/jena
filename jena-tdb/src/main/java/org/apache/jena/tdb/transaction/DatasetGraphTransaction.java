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

package org.apache.jena.tdb.transaction ;

import org.apache.jena.atlas.lib.Sync ;
import org.apache.jena.query.ReadWrite ;
import org.apache.jena.sparql.JenaTransactionException ;
import org.apache.jena.sparql.core.DatasetGraphTrackActive ;
import org.apache.jena.sparql.util.Context ;
import org.apache.jena.tdb.StoreConnection ;
import org.apache.jena.tdb.TDB ;
import org.apache.jena.tdb.base.file.Location ;
import org.apache.jena.tdb.store.DatasetGraphTDB ;
import static java.lang.ThreadLocal.withInitial ;

/**
 * A transactional {@code DatasetGraph} that allows one active transaction per thread.
 * 
 * {@link DatasetGraphTxn} holds the {~link Trasnaction} object.
 *
 * This is analogous to a "connection" in JDBC. 
 * It is a holder of a {@link StoreConnection} combined with the machinary from
 * {@link DatasetGraphTrackActive}.  
 * 
 * Not considered to be in the public API.
 */

 public class DatasetGraphTransaction extends DatasetGraphTrackActive implements Sync {
    /*
     * Initially, the app can use this DatasetGraph non-transactionally. But as
     * soon as it starts a transaction, the dataset can only be used inside
     * transactions.
     * 
     * There are two per-thread state variables: txn: ThreadLocalTxn -- the
     * transactional , one time use dataset isInTransactionB: ThreadLocalBoolean
     * -- flags true between begin and commit/abort, and end for read
     * transactions.
     */

    // Transaction per thread per DatasetGraphTransaction object.
    private ThreadLocal<DatasetGraphTxn> txn           = withInitial(() -> null);
    private ThreadLocal<Boolean>         inTransaction = withInitial(() -> false);

    private final StoreConnection        sConn;
    private boolean                      isClosed      = false;

    public DatasetGraphTransaction(Location location) {
        sConn = StoreConnection.make(location) ;
    }

    public Location getLocation() {
        return sConn.getLocation() ;
    }

    public DatasetGraphTDB getDatasetGraphToQuery() {
        checkNotClosed() ;
        return get() ;
    }

    /** Access the base storage - use with care */
    public DatasetGraphTDB getBaseDatasetGraph() {
        checkNotClosed() ;
        return sConn.getBaseDataset() ;
    }

    /** Get the current DatasetGraphTDB */
    @Override
    public DatasetGraphTDB get() {
        if ( isInTransaction() ) {
            DatasetGraphTxn dsgTxn = txn.get() ;
            if ( dsgTxn == null )
                throw new TDBTransactionException("In a transaction but no transactional DatasetGraph") ;
            return dsgTxn.getView() ;
        }

        if ( sConn.haveUsedInTransaction() )
            throw new TDBTransactionException("Not in a transaction") ;

        // Never used in a transaction - return underlying database for old
        // style (non-transactional) usage.
        return sConn.getBaseDataset() ;
    }

    @Override
    protected void checkActive() {
        checkNotClosed() ;
        if ( !isInTransaction() )
            throw new JenaTransactionException("Not in a transaction (" + getLocation() + ")") ;
    }

    @Override
    protected void checkNotActive() {
        checkNotClosed() ;
        if ( sConn.haveUsedInTransaction() && isInTransaction() )
            throw new JenaTransactionException("Currently in a transaction (" + getLocation() + ")") ;
    }

    protected void checkNotClosed() {
        if ( isClosed )
            throw new JenaTransactionException("Already closed") ;
    }

    @Override
    public boolean isInTransaction() {
        checkNotClosed() ;
        return inTransaction.get() ;
    }

    public boolean isClosed() {
        return isClosed ;
    }

    public void syncIfNotTransactional() {
        if ( !sConn.haveUsedInTransaction() )
            sConn.getBaseDataset().sync() ;
    }

    @Override
    protected void _begin(ReadWrite readWrite) {
        checkNotClosed() ;
        DatasetGraphTxn dsgTxn = sConn.begin(readWrite) ;
        txn.set(dsgTxn) ;
        inTransaction.set(true) ;
    }

    @Override
    protected void _commit() {
        checkNotClosed() ;
        txn.get().commit() ;
        inTransaction.set(false) ;
    }

    @Override
    protected void _abort() {
        checkNotClosed() ;
        txn.get().abort() ;
        inTransaction.set(false) ;
    }

    @Override
    protected void _end() {
        checkNotClosed() ;
        DatasetGraphTxn dsg = txn.get() ;
        // It's null if end() already called.
        if ( dsg == null ) {
            TDB.logInfo.warn("Transaction already ended") ;
            return ;
        }
        txn.get().end() ;
        // May already be false due to .commit/.abort.
        inTransaction.set(false) ;
        txn.set(null) ;
    }

    @Override
    public String toString() {
        try {
            if ( isInTransaction() )
                // Risky ...
                return get().toString() ;
            // Hence ...
            return getBaseDatasetGraph().toString() ;
        }
        catch (Throwable th) {
            return "DatasetGraphTransaction" ;
        }
    }

    @Override
    protected void _close() {
        if ( isClosed )
            return ;

        if ( !sConn.haveUsedInTransaction() && get() != null ) {
            // Non-transactional behaviour.
            DatasetGraphTDB dsg = get() ;
            dsg.sync() ;
            dsg.close() ;
            StoreConnection.release(dsg.getLocation()) ;
            isClosed = true ;
            return ;
        }

        if ( isInTransaction() ) {
            TDB.logInfo.warn("Attempt to close a DatasetGraphTransaction while a transaction is active - ignored close (" + getLocation()
                             + ")") ;
            return ;
        }
        isClosed = true ;
        txn.remove() ;
        inTransaction.remove() ;
    }

    @Override
    public Context getContext() {
        // Not the transactional dataset.
        return getBaseDatasetGraph().getContext() ;
    }

    /**
     * Return some information about the store connection and transaction
     * manager for this dataset.
     */
    public SysTxnState getTransMgrState() {
        return sConn.getTransMgrState() ;
    }

    @Override
    public void sync() {
        if ( !sConn.haveUsedInTransaction() && get() != null )
            get().sync() ;
    }
}
