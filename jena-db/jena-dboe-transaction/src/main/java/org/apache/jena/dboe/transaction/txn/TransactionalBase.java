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

package org.apache.jena.dboe.transaction.txn;

import java.util.Objects ;

import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.query.ReadWrite ;
import org.apache.jena.query.TxnType;

/**
 * Framework for implementing a Transactional.
 */

public class TransactionalBase implements TransactionalSystem {
    // Help debugging by generating names for Transactionals.
    private final String label ; 
    protected boolean isShutdown = false ; 
    protected final TransactionCoordinator txnMgr ;
    
    // Per thread transaction.
    private final ThreadLocal<Transaction> theTxn = new ThreadLocal<>() ;
    
    public TransactionalBase(String label, TransactionCoordinator txnMgr) {
        this.label = label ;
        this.txnMgr = txnMgr ;
    }
    
    public TransactionalBase(TransactionCoordinator txnMgr) {
        this(null, txnMgr) ;
    }

    @Override
    public TransactionCoordinator getTxnMgr() {
        return txnMgr ;
    }

    // Development
    private static final boolean trackAttachDetach = false ;
    
    @Override
    public TransactionCoordinatorState detach() {
        if ( trackAttachDetach )
            Log.info(this,  ">> detach");
        checkRunning() ;
        // Not if it just commited but before end.
        //checkActive() ;
        Transaction txn = theTxn.get() ;
        TransactionCoordinatorState coordinatorState = null ;
        if ( txn != null )
            // We are not ending.
            coordinatorState = txnMgr.detach(txn) ;
        if ( trackAttachDetach )
            Log.info(this,  "  theTxn = "+txn) ;
        theTxn.remove() ; ///??????
        if ( trackAttachDetach )
            Log.info(this,  "<< detach");
        if ( coordinatorState == null )
            throw new TransactionException("Not attached") ;
        return coordinatorState ;
    }
    
    @Override
    public void attach(TransactionCoordinatorState coordinatorState) {
        if ( trackAttachDetach )
            Log.info(this,  ">> attach");
        Objects.nonNull(coordinatorState) ;
        checkRunning() ;
        checkNotActive() ;
        TxnState txnState = coordinatorState.transaction.getState() ;
        if ( txnState != TxnState.DETACHED )
            throw new TransactionException("Not a detached transaction") ;
        txnMgr.attach(coordinatorState) ;
        if ( trackAttachDetach )
            Log.info(this,  "  theTxn = "+coordinatorState.transaction) ;
        theTxn.set(coordinatorState.transaction);
        if ( trackAttachDetach )
            Log.info(this,  "<< attach");
    } 

    @Override
    public final void begin(ReadWrite readWrite) { 
        begin(TxnType.convert(readWrite));
    }

    @Override
    public final void begin(TxnType txnType) {
        Objects.nonNull(txnType) ;
        checkRunning() ;
        checkNotActive() ;
        Transaction transaction = txnMgr.begin(txnType) ;
        theTxn.set(transaction) ;
    }

    @Override
    public final boolean promote() {
        checkActive();
        return TransactionalSystem.super.promote();
    }

    
    @Override
    public final boolean promote(Promote promoteMode) {
        checkActive() ;
        boolean readCommitted = (promoteMode == Promote.READ_COMMITTED);
        Transaction txn = getValidTransaction() ;
        return txn.promote(readCommitted) ;
    }

    @Override
    public final void commit() {
        checkRunning() ;
        TransactionalSystem.super.commit() ;
    }

    @Override
    public void commitPrepare() {
        Transaction txn = getValidTransaction() ;
        txn.prepare() ;
    }

    @Override
    public void commitExec() {
        Transaction txn = getValidTransaction() ;
        try { txn.commit(); }
        finally { _end(); }
    }

//    /** Signal end of commit phase */
//    @Override
//    public void commitEnd() {
//        _end() ;
//    }
    
    @Override
    public final void abort() {
        checkRunning() ;
        checkActive() ;
        Transaction txn = getValidTransaction() ;
        try { txn.abort() ; }
        finally { _end() ; }
    }

    @Override
    public final void end() {
        checkRunning() ;
        // Don't check if active or if any thread locals exist
        // because this may have already been called.
        // txn.get() ; -- may be null -- test repeat calls.
        _end() ;
    }

    @Override
    public ReadWrite transactionMode() {
        checkRunning() ;
        Transaction txn = Lib.readThreadLocal(theTxn) ;
        if ( txn != null )
            return txn.getMode() ;
        return null ; 
    }

    @Override
    public TxnType transactionType() {
        checkRunning() ;
        Transaction txn = Lib.readThreadLocal(theTxn) ;
        if ( txn != null )
            return txn.getTxnType() ;
        return null ;
    }

    @Override
    public boolean isInTransaction() {
        return Lib.readThreadLocal(theTxn) != null;
    }

    @Override
    final
    public TransactionInfo getTransactionInfo() {
        return getThreadTransaction() ;
    }
    
    @Override
    final
    public Transaction getThreadTransaction() {
        return  Lib.readThreadLocal(theTxn);
    }

    /** Get the transaction, checking there is one */  
    private Transaction getValidTransaction() {
        Transaction txn = theTxn.get() ;
        if ( txn == null )
            throw new TransactionException("Not in a transaction") ;
        return txn ;
    }

    private void checkRunning() {
//        if ( ! hasStarted )
//            throw new TransactionException("Not started") ;
        
        if ( isShutdown )
            throw new TransactionException("Shutdown") ;
    }
    
    /**
     * Shutdown component, aborting any in-progress transactions. This operation
     * is not guaranteed to be called.
     */
    public void shutdown() {
        txnMgr.shutdown() ;
        isShutdown = true ;
    }

    protected String label(String msg) {
        if ( label == null )
            return msg ;
        return label+": "+msg ;
    }
    
    final
    protected void checkActive() {
        checkNotShutdown() ;
        if ( ! isInTransaction() )
            throw new TransactionException(label("Not in an active transaction")) ;
    }

    final
    protected void checkNotActive() {
        checkNotShutdown() ;
        if ( isInTransaction() )
            throw new TransactionException(label("Currently in an active transaction")) ;
    }

    final
    protected void checkNotShutdown() {
        if ( isShutdown )
            throw new TransactionException(label("Already shutdown")) ;
    }

    private final void _end() {
        Transaction txn = theTxn.get() ;
        if ( txn != null ) {
            try {
                // Can throw an exception on begin(W)...end().
                txn.end() ;
            } finally {
                theTxn.set(null) ;
                theTxn.remove() ;
            }
        }
    }
}

