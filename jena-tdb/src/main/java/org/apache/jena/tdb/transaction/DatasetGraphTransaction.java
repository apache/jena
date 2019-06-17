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

import static java.lang.ThreadLocal.withInitial ;

import org.apache.jena.atlas.lib.Sync ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.JenaTransactionException ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.DatasetGraphWrapper;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.util.Context ;
import org.apache.jena.tdb.StoreConnection ;
import org.apache.jena.tdb.TDB ;
import org.apache.jena.tdb.TDBException;
import org.apache.jena.tdb.base.file.Location ;
import org.apache.jena.tdb.store.DatasetGraphTDB ;
import org.apache.jena.tdb.store.GraphTxnTDB ;

/**
 * A transactional {@code DatasetGraph} that allows one active transaction per thread.
 *
 * {@link DatasetGraphTxn} holds the {@link Transaction} object.
 *
 * This is analogous to a "connection" in JDBC.
 *
 * Not considered to be in the public API.
 */
 public class DatasetGraphTransaction extends DatasetGraphWrapper implements Sync {
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
    private ThreadLocal<DatasetGraphTxn> dsgtxn        = withInitial(() -> null);
    private ThreadLocal<Boolean>         inTransaction = withInitial(() -> false);

    private final StoreConnection        sConn;
    private boolean                      isClosed      = false;

    public DatasetGraphTransaction(Location location) {
        this(StoreConnection.make(location)) ;
    }

    public DatasetGraphTransaction(StoreConnection sConn) {
        super(null);
        this.sConn = sConn;
    }

    public Location getLocation() {
        return sConn.getLocation() ;
    }

    // getCurrentTxnDSG
    public DatasetGraphTDB getDatasetGraphToQuery() {
        checkNotClosed() ;
        return get() ;
    }

    /** Access the base storage - use with care */
    public DatasetGraphTDB getBaseDatasetGraph() {
        checkNotClosed() ;
        return sConn.getBaseDataset() ;
    }

    @Override public DatasetGraph getW() {
        if ( isInTransaction() ) {
            DatasetGraphTxn dsgTxn = dsgtxn.get() ;
            if ( dsgTxn.getTransaction().isRead() ) {
                TxnType txnType = dsgTxn.getTransaction().getTxnType();
                Promote mode;
                switch(txnType) {
                    case READ :
                        throw new JenaTransactionException("Attempt to update in a read transaction");
                    case WRITE :
                        // Impossible. We're in read-mode.
                        throw new TDBException("Internal inconsistency: read-mode write transaction");
                    case READ_PROMOTE :
                        mode = Promote.ISOLATED;
                        break;
                    case READ_COMMITTED_PROMOTE :
                        mode = Promote.READ_COMMITTED;
                        break;
                    default:
                        throw new TDBException("Internal inconsistency: null transaction type");
                }
                // Promotion.
                TransactionManager txnMgr = dsgTxn.getTransaction().getTxnMgr() ;
                DatasetGraphTxn dsgTxn2 = txnMgr.promote(dsgTxn, txnType, mode) ;
                if ( dsgTxn2 == null )
                    // We were asked for a write operation and can't promote.
                    // Returning false makes no sense.
                    throw new JenaTransactionException("Can't promote "+txnType+"- dataset has been written to");
                dsgtxn.set(dsgTxn2);
            }
        }
        return super.getW() ;
    }

    /** Get the current DatasetGraphTDB */
    @Override
    public DatasetGraphTDB get() {
        if ( isInTransaction() ) {
            DatasetGraphTxn dsgTxn = dsgtxn.get() ;
            if ( dsgTxn == null )
                throw new TDBTransactionException("In a transaction but no transactional DatasetGraph") ;
            return dsgTxn.getView() ;
        }

        if ( sConn.haveUsedInTransaction() )
            throw new TDBTransactionException("Not in a transaction") ;

        // Never been used in a transaction - return underlying database for old
        // style (non-transactional) usage.
        return sConn.getBaseDataset() ;
    }

    @Override
    public Graph getDefaultGraph() {
        return new GraphTxnTDB(this, null);
    }

    @Override
    public Graph getUnionGraph() {
        return getGraph(Quad.unionGraph);
    }

    @Override
    public Graph getGraph(Node graphNode) {
        return new GraphTxnTDB(this, graphNode);
    }

    @Override
    public void begin(ReadWrite txnType) {
        begin(TxnType.convert(txnType));
    }
    
    @Override
    public void begin(TxnType txnType) {
        checkNotClosed() ;
        checkNotActive();
        DatasetGraphTxn dsgTxn = sConn.begin(txnType) ;
        dsgtxn.set(dsgTxn) ;
        inTransaction.set(true) ;
    }

    @Override
    public boolean promote() {
          Promote promoteMode = Promote.ISOLATED;
          TxnType txnType = transactionType();
          if ( txnType == TxnType.READ_COMMITTED_PROMOTE )
              promoteMode = Promote.READ_COMMITTED;
          return promote(promoteMode);
      }
    
    @Override
    public boolean promote(Promote promoteMode) {
        // Promotion (TDB1) is a reset of the DatasetGraphTxn.
        checkNotClosed() ;
        DatasetGraphTxn dsgTxn = dsgtxn.get();
        Transaction transaction = dsgTxn.getTransaction();
        DatasetGraphTxn dsgTxn2 = transaction.getTxnMgr().promote(dsgTxn, transaction.getTxnType(), promoteMode);
        if ( dsgTxn2 == null )
            return false;
        dsgtxn.set(dsgTxn2) ;
        return true;
    }

    @Override
    public void commit() {
        checkNotClosed() ;
        checkActive();
        dsgtxn.get().commit() ;
        inTransaction.set(false) ;
    }

    @Override
    public void abort() {
        checkNotClosed() ;
        checkActive();
        dsgtxn.get().abort() ;
        inTransaction.set(false) ;
    }

    @Override
    public void end() {
        checkNotClosed() ;
        DatasetGraphTxn dsg = dsgtxn.get() ;
        // It's null if end() already called.
        if ( dsg == null ) {
            TDB.logInfo.warn("Transaction already ended") ;
            return ;
        }
        try {
            // begin(W)..end() throws an exception.
            dsgtxn.get().end() ;
        } finally {
            // May already be false due to .commit/.abort.
            inTransaction.set(false) ;
            dsgtxn.set(null) ;
        }
    }

    @Override
    public boolean isInTransaction() {
        checkNotClosed() ;
        return inTransaction.get() ;
    }

    @Override
    public ReadWrite transactionMode() {
        checkNotClosed() ;
        if ( ! isInTransaction() )
            return null;
        return dsgtxn.get().getTransaction().getTxnMode();
    }

    @Override
    public TxnType transactionType() {
        checkNotClosed() ;
        if ( ! isInTransaction() )
            return null;
        return dsgtxn.get().getTransaction().getTxnType();
    }

    @Override
    public boolean supportsTransactions()       { return true ; }

    @Override
    public boolean supportsTransactionAbort()   { return true ; }

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

    public boolean isClosed() {
        return isClosed ;
    }

    @Override
    public synchronized void close() {
        if ( isClosed )
            return ;
        if ( sConn.haveUsedInTransaction() ) {
            if ( isInTransaction() ) {
                TDB.logInfo.warn("Attempt to close a DatasetGraphTransaction while a transaction is active - ignored close (" + getLocation() + ")") ;
                return ;
            }
            // Otherwise ignore - close() while transactional is meaningless.
            return ;
        }
        if ( ! sConn.isValid() ) {
            // There may be another DatasetGraphTransaction using this location
            // and that DatasetGraphTransaction has been closed, invalidating
            // the StoreConnection.
            return ;
        }
        DatasetGraphTDB dsg = sConn.getBaseDataset() ;
        dsg.sync() ;
        dsg.close() ;
        StoreConnection.release(getLocation()) ;
        dsgtxn.remove() ;
        inTransaction.remove() ;
        isClosed = true ;
    }

    @Override
    public Context getContext() {
        // Not the transactional dataset.
        return getBaseDatasetGraph().getContext() ;
    }

    public StoreConnection getStoreConnection() {
        return sConn ;
    }

    public void syncIfNotTransactional() {
        if ( !sConn.haveUsedInTransaction() )
            sConn.getBaseDataset().sync() ;
    }

    @Override
    public void sync() {
        if ( !sConn.haveUsedInTransaction() && get() != null )
            get().sync() ;
    }

    private void checkActive() {
        if ( !isInTransaction() )
            throw new JenaTransactionException("Not in a transaction (" + getLocation() + ")") ;
    }

    private void checkNotActive() {
        if ( sConn.haveUsedInTransaction() && isInTransaction() )
            throw new JenaTransactionException("Currently in a transaction (" + getLocation() + ")") ;
    }

    private void checkNotClosed() {
        if ( isClosed )
            throw new JenaTransactionException("Already closed") ;
    }
}
