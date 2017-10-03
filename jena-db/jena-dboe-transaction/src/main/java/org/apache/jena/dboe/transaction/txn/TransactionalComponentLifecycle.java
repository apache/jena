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

import static org.apache.jena.dboe.transaction.txn.TxnState.*;

import java.nio.ByteBuffer ;
import java.util.Objects ;

import org.apache.jena.query.ReadWrite ;

import org.apache.jena.atlas.lib.InternalErrorException ;

/** 
 * Base implementation of the component interface for {@link TransactionalComponent}.
 */
public abstract class TransactionalComponentLifecycle<X> implements TransactionalComponent {
    
    // Pass down recovery operations.
    // This class has no transaction-recorded state.
    @Override public abstract void startRecovery();
    @Override public abstract void recover(ByteBuffer ref);
    @Override public abstract void finishRecovery();
    
    // ---- Normal operation
    
    private static final boolean CHECKING = false ;
    private ThreadLocal<TxnState> trackTxn = CHECKING ? ThreadLocal.withInitial(() -> INACTIVE) : null ; 
    
    // Access to these two must be via the getter/setters below only.
    // Allows stuff for thread switching.
    private ThreadLocal<Transaction> threadTxn = new ThreadLocal<>() ;
    private ThreadLocal<X> componentState = new ThreadLocal<>() ;
    private final ComponentId componentId ;
    
    protected TransactionalComponentLifecycle(ComponentId componentId) {
        this.componentId = componentId ;
    }
    
    @Override
    public ComponentId getComponentId() {
        return componentId ;
    }

//    // Very dangerous!
//    protected void setForThread(Transaction txn, X state) {
//        threadTxn.set(txn);
//        componentState.set(state);
//    }
    
    /** Start a transaction */
    @Override
    final
    public void begin(Transaction transaction) {
        Objects.requireNonNull(transaction) ;
        setTransaction(transaction); 
        checkState(INACTIVE, COMMITTED, ABORTED) ;
        setTrackTxn(ACTIVE);
        X x = _begin(transaction.getMode(), transaction.getTxnId()) ;
        setDataState(x);
    }
    
    /** Promote a component in a transaction */
    @Override
    final
    public boolean promote(Transaction transaction) {
        Objects.requireNonNull(transaction) ;
        checkState(ACTIVE) ;
        X newState = _promote(transaction.getTxnId(), getDataState());
        if ( newState == null )
            return false;
        setDataState(newState);
        return true;
    }
    
    /** Commit a transaction (make durable): prepare - commit - cleanup */  
    @Override
    final
    public ByteBuffer commitPrepare(Transaction transaction) {
        checkAligned(transaction) ;
        checkState(ACTIVE) ;
        try { return _commitPrepare(transaction.getTxnId(), getDataState()) ; }
        finally { setTrackTxn(PREPARE) ; }
    }

    @Override
    final
    public void commit(Transaction transaction) {
        checkAligned(transaction) ;
        checkState(PREPARE) ;
        _commit(transaction.getTxnId(), getDataState());
        setTrackTxn(COMMIT) ;
    }
    
    @Override
    final
    public void commitEnd(Transaction transaction) {
        checkAligned(transaction) ;
        checkState(COMMIT) ;
        _commitEnd(transaction.getTxnId(), getDataState());
        setTrackTxn(COMMITTED) ;
        internalComplete(transaction) ;
    }

    @Override 
    final
    public void abort(Transaction transaction) {
        checkAligned(transaction) ;
        checkState(ACTIVE, PREPARE, COMMIT) ;
        _abort(transaction.getTxnId(), getDataState()) ;
        setTrackTxn(ABORTED) ;
        internalComplete(transaction) ;
    }

    private void internalComplete(Transaction transaction) {
        _complete(transaction.getTxnId(), getDataState());
        setTrackTxn(INACTIVE) ;
        releaseThreadState() ;
    }

    @Override
    final
    public void complete(Transaction transaction) {
        if ( transaction.hasFinished() )
            return ;
        checkAligned(transaction) ;
        ReadWrite m = getReadWriteMode() ;
        switch(m) {
            case READ:
                checkState(ACTIVE, COMMITTED, ABORTED) ;
                break ;
            case WRITE:
                // If bad, force abort?
                checkState(COMMITTED, ABORTED) ; 
                break ;
        }
        _complete(transaction.getTxnId(), getDataState());
        switch(m) {
            case READ:
                internalComplete(transaction);
                break ;
            case WRITE:
                // complete happened in the commit or abort.
                break ;
        }
    }
    
    @Override 
    final
    public void shutdown() {
        _shutdown() ;
        clearInternal() ;
    }
    
    @Override 
    final public SysTransState detach() {
        TxnState txnState = getTxnState() ;
        if ( txnState == null )
            return null ;
        checkState(ACTIVE) ;
        setTrackTxn(DETACHED) ;
        SysTransState transState = new SysTransState(this, getTransaction(), getDataState()) ;
        //****** Thread locals 
        releaseThreadState() ;
        return transState ;
    }
    
    @Override
    public void attach(SysTransState state) {
        @SuppressWarnings("unchecked")
        X x = (X)state.getState() ;
//        // reset to not thread not in 
//        if ( CHECKING )
//            trackTxn : ThreadLocal<TxnState>
//        
//      
        setTransaction(state.getTransaction());
        setDataState(x);
        setTrackTxn(ACTIVE) ;
    }
    
    // -- Access object members.

    public static class ComponentState<X> {
        final TxnState state;
        final Transaction txn ;
        final X componentState ;
        ComponentState(TxnState state, Transaction txn, X componentState) {
            super() ;
            this.state = state ;
            this.txn = txn ;
            this.componentState = componentState ;
        }
    }
    
    public ComponentState<X> getComponentState() {
        return new ComponentState<>(getTrackTxn(), getTransaction(), getDataState()) ; 
    }
    
    public void setComponentState(ComponentState<X> state) {
        setTrackTxn(state.state);
        setTransaction(state.txn);
        setDataState(state.componentState);
        
    }

    protected void releaseThreadState() {
        // Remove thread locals
        if ( trackTxn != null )
            trackTxn.remove() ;
        componentState.remove();
        
//        java version "1.8.0_31"
//        Java(TM) SE Runtime Environment (build 1.8.0_31-b13)
//        Java HotSpot(TM) 64-Bit Server VM (build 25.31-b07, mixed mode)
//        
//        openjdk version "1.8.0_40-internal"
//        OpenJDK Runtime Environment (build 1.8.0_40-internal-b09)
//        OpenJDK 64-Bit Server VM (build 25.40-b13, mixed mode)
        
        // This one is very important else the memory usage grows.  Not clear why.
        // A Transaction has an internal AtomicReference.  Replacing the AtomicReference
        // with a plain member variable slows the growth down greatly.
        threadTxn.remove() ;
    }
    
    protected void clearInternal() {
        trackTxn = null ;
        threadTxn = null ;
        componentState = null ;
    }
    
    //protected ComponentState<X> getState()
    
    protected X getDataState()                      { return componentState.get() ; }
    protected void setDataState(X data)             { componentState.set(data) ; }

    protected Transaction getTransaction()          { return threadTxn.get(); }
    protected void setTransaction(Transaction txn)  { threadTxn.set(txn); }
    
    private void setTrackTxn(TxnState newState) {
        if ( ! CHECKING ) return ;
        trackTxn.set(newState);
    }

    // This is our record of the state - it is not necessarily the transactions
    // view during changes.  This class tracks the expected incomign state and
    // the transaction
    private TxnState getTrackTxn() {
        if ( ! CHECKING ) return null ;
        return trackTxn.get();
    }
    // -- Access object members.
    
    // XXX Align to javadoc in TransactionalComponent.
    
    /* There are two lifecycles, one for write transaction, one
     * for read transactions. This affects how transaction end so
     * when/if promoted read->write transactions happen, a promoted
     * transaction will follow the write lifecycle. 
     * 
     * In both lifecyles, the implementer can assume that calls
     * happen at the right points and called only as needed.  Framework
     * takes care of checking.  
     * 
     * Read lifecycle:
     * A read transaction be be just begin(READ)-end() but may also
     * have commit or abort before end. The _commitRead and _abortRead
     * calls note if an explicit commit or abort occurs but may not be
     * called. _endRead is always called exactly once.
     *  
     * _commitRead
     * _abortRead
     * _endRead
     * _complete
     * 
     * Write lifecycle:
     * A write transaction must have a commit() or abort() before end().
     * The fraemwork will check this.
     * 
     * If the transaction commits:
     * _commitPrepareWrite
     * _commitWrite -- The transaction is 
     * _commitEndWrite
     * 
     * If the transaction aborts:
     * _abortWrite
     * 
     * After any lifecycle, a final call of
     * _complete()
     * 
     * indicates ths transaction has fully finished.
     * 
     * Typically, an implementation does not need to take action in every call. 
     */
    
//    /**
//     * 
//     * @param readWrite
//     * @param txnId
//     * @return
//     */
//    protected abstract X           _begin(ReadWrite readWrite, TxnId txnId) ;
//    
//    /**
//     * 
//     * @param txnId
//     * @param state
//     * @return
//     */
//    protected abstract ByteBuffer  _commitPrepareWrite(TxnId txnId, X  state) ;
//    
//    /**
//     * 
//     * @param txnId
//     * @param state
//     */
//    protected abstract void        _commitWrite(TxnId txnId, X state) ;
//    
//    /**
//     * 
//     * @param txnId
//     * @param state
//     */
//    protected abstract void        _commitEndWrite(TxnId txnId, X state) ;
//    
//    /**
//     * 
//     * @param txnId
//     * @param state
//     */
//    protected abstract void        _abortWrite(TxnId txnId, X state) ;
//    
//    /**
//     * 
//     * @param txnId
//     * @param state
//     * @return
//     */
//    protected abstract ByteBuffer  _commitRead(TxnId txnId, X  state) ;
//    
//    /**
//     * 
//     * @param txnId
//     * @param state
//     * @return
//     */
//    protected abstract ByteBuffer  _abortRead(TxnId txnId, X  state) ;
//
//    /**
//     * 
//     * @param txnId
//     * @param state
//     */
//    protected abstract void        _complete(TxnId txnId, X state) ;
//    
//    /**
//     * 
//     */
//    protected abstract void        _shutdown() ;

    protected abstract X           _begin(ReadWrite readWrite, TxnId txnId) ;
    protected abstract X           _promote(TxnId txnId, X oldState) ;
    protected abstract ByteBuffer  _commitPrepare(TxnId txnId, X  state) ;
    protected abstract void        _commit(TxnId txnId, X state) ;
    protected abstract void        _commitEnd(TxnId txnId, X state) ;
    protected abstract void        _abort(TxnId txnId, X state) ;
    protected abstract void        _complete(TxnId txnId, X state) ;
    protected abstract void        _shutdown() ;
    
    protected ReadWrite getReadWriteMode() {
        Transaction txn = getTransaction() ;
        return txn.getMode() ;
    }
    
    protected boolean isActiveTxn() {
        TxnState txnState = getTxnState() ;
        if ( txnState == null )
            return false ; 
        switch(getTxnState()) {
            case INACTIVE: case END_ABORTED: case END_COMMITTED: 
                return false ;
            case ACTIVE: case DETACHED: case PREPARE: case ABORTED: case COMMIT: case COMMITTED:  
                return true ;
            //null: default: return false ;
            // Get the compiler to check all states covered.
        }
        // Should not happen.
        throw new InternalErrorException("Unclear transaction state") ;
    }

    protected boolean isReadTxn() { return ! isWriteTxn() ; }
    
    protected boolean isWriteTxn() {
        Transaction txn = getTransaction();
        return txn.isWriteTxn() ;
    }
    
    protected void checkTxn() {
        if ( ! isActiveTxn() )
            throw new TransactionException("Not in a transaction") ;
    }

//    protected void requireWriteTxn() {
//        Transaction txn = getTransaction();
//        if ( txn == null )
//            throw new TransactionException("Not a transaction");
//        else 
//            txn.requireWriteTxn() ;
//    }
    
    protected void checkWriteTxn() {
        Transaction txn = getTransaction();
        if ( txn == null )
            throw new TransactionException("Not a transaction");
        else 
            txn.requireWriteTxn() ;
    }

    // -- Access object members.
    
    private TxnState getTxnState() { 
        Transaction txn = getTransaction() ;
        if ( txn == null )
            return null ;
        return txn.getState() ;
    }

    private void checkAligned(Transaction transaction) {
        if ( ! CHECKING ) return ;
        Transaction txn = getTransaction();
        if ( txn != transaction )
            throw new TransactionException("Transaction is not the transaction of the thread") ; 
    }
    
    private void checkState(TxnState expected) {
        if ( ! CHECKING ) return ;
        TxnState s = getTrackTxn() ;
        if ( s != expected )
            throw new TransactionException("Transaction is in state "+s+": expected state "+expected) ;
    }

    private void checkState(TxnState expected1, TxnState expected2) {
        if ( ! CHECKING ) return ;
        TxnState s = getTrackTxn() ;
        if ( s != expected1 && s != expected2 )
            throw new TransactionException("Transaction is in state "+s+": expected state "+expected1+" or "+expected2) ;
    }

    // Avoid varargs ... undue worry?
    private void checkState(TxnState expected1, TxnState expected2, TxnState expected3) {
        if ( ! CHECKING ) return ;
        TxnState s = getTrackTxn() ;
        if ( s != expected1 && s != expected2 && s != expected3 )
            throw new TransactionException("Transaction is in state "+s+": expected state "+expected1+", "+expected2+" or "+expected3) ;
    }

    //    private void checkStateNot(State unexpected) {
//        State s = state.get();
//        if ( s == unexpected )
//            throw new TransactionException("Transaction in unexpected state "+s) ;
//    }

}

