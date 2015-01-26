/**
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.seaborne.dboe.transaction.txn;

import static org.seaborne.dboe.transaction.txn.Transaction.TxnState.ABORTED ;
import static org.seaborne.dboe.transaction.txn.Transaction.TxnState.ACTIVE ;
import static org.seaborne.dboe.transaction.txn.Transaction.TxnState.COMMIT ;
import static org.seaborne.dboe.transaction.txn.Transaction.TxnState.COMMITTED ;
import static org.seaborne.dboe.transaction.txn.Transaction.TxnState.INACTIVE ;
import static org.seaborne.dboe.transaction.txn.Transaction.TxnState.PREPARE ;

import java.nio.ByteBuffer ;
import java.util.Objects ;

import org.apache.jena.atlas.lib.InternalErrorException ;
import org.seaborne.dboe.transaction.txn.Transaction.TxnState ;

import com.hp.hpl.jena.query.ReadWrite ;

/** Base implementation of the component interface for {@link TransactionalComponent}.
 */
public abstract class TransactionalComponentLifecycle<X> implements TransactionalComponent {
    
//    // ---- Recovery phase
//    @Override
//    public void startRecovery() {}
//    
//    /** Notification that {@code ref} was really committed.
//     * @param ref Same bytes as were written during prepare originally.
//     */
//    @Override
//    public void recover(ByteBuffer ref) {
//        Log.warn(this,  "Called to recover a transaction (ignored)") ; 
//    }
//    @Override
//    public void finishRecovery() { }
//    
//    @Override
//    public void shutdown() {
//    }
    
    // ---- Normal operation
    
    // XXX Move to TransactionCoordinator
    // Add field in Transaction to be checked.
    // See TransLifecycleMonitor.
    
    private static final boolean CHECKING = true ;
    private ThreadLocal<TxnState> state = CHECKING ? ThreadLocal.withInitial(() -> INACTIVE) : null ; 
    private ThreadLocal<Transaction> threadTxn = new ThreadLocal<>() ;
    private ThreadLocal<X> componentState = new ThreadLocal<>() ;
    
    protected TransactionalComponentLifecycle() { }
    
    /** Start a transaction */
    @Override
    final
    public void begin(Transaction transaction) {
        Objects.requireNonNull(transaction) ;
        checkState(INACTIVE, COMMITTED, ABORTED) ;
        setState(ACTIVE);
        threadTxn.set(transaction); 
        X x = _begin(transaction.getMode(), transaction.getTxnId()) ;
        componentState.set(x);
    }
    
    /** Commit a transaction (make durable): prepare - commit - cleanup */  
    @Override
    final
    public ByteBuffer commitPrepare(Transaction transaction) {
        checkAligned(transaction) ;
        checkState(ACTIVE) ;
        try { return _commitPrepare(transaction.getTxnId(), getState()) ; }
        finally { setState(PREPARE) ; }
    }

    @Override
    final
    public void commit(Transaction transaction) {
        checkAligned(transaction) ;
        checkState(PREPARE) ;
        _commit(transaction.getTxnId(), getState());
        setState(COMMIT) ;
    }
    
    @Override
    final
    public void commitEnd(Transaction transaction) {
        checkAligned(transaction) ;
        checkState(COMMIT) ;
        _commitEnd(transaction.getTxnId(), getState());
        setState(COMMITTED) ;
        internalComplete(transaction) ;
    }

    @Override 
    final
    public void abort(Transaction transaction) {
        checkAligned(transaction) ;
        checkState(ACTIVE, PREPARE, COMMIT) ;
        _abort(transaction.getTxnId(), getState()) ;
        setState(ABORTED) ;
        internalComplete(transaction) ;
    }

    private void internalComplete(Transaction transaction) {
        _complete(transaction.getTxnId(), getState());
        setState(INACTIVE) ;
        // Remove thread locals
        if ( state != null )
            state.remove() ;
        componentState.remove();
        threadTxn.remove() ;
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
        _complete(transaction.getTxnId(), getState());
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
    }
    
    protected abstract X           _begin(ReadWrite readWrite, TxnId txnId) ;
    protected abstract ByteBuffer  _commitPrepare(TxnId txnId, X  state) ;
    protected abstract void        _commit(TxnId txnId, X state) ;
    protected abstract void        _commitEnd(TxnId txnId, X state) ;
    protected abstract void        _abort(TxnId txnId, X state) ;
    protected abstract void        _complete(TxnId txnId, X state) ;
    protected abstract void        _shutdown() ;

    protected ReadWrite getReadWriteMode() {
        Transaction txn = threadTxn.get();
        return txn.getMode() ;
    }

    protected X getState() {
        return componentState.get() ;
    }
    
    protected TxnState getTxnState() {
        Transaction txn = threadTxn.get();
        if ( txn == null )
            return null ;
        return txn.getState() ;
    }

    protected boolean isActiveTxn() {
        TxnState txnState = getTxnState() ;
        if ( txnState == null )
            return false ; 
        switch(getTxnState()) {
            case INACTIVE: case END_ABORTED: case END_COMMITTED: return false ;
            case ACTIVE:   case PREPARE: case ABORTED: case COMMIT: case COMMITTED:  return true ;
            //null: default: return false ;
            // Get the comp[iler to check all states covered.
        }
        // Should not happen.
        throw new InternalErrorException("Unclear transaction state") ;
    }

    protected boolean isReadTxn() { return ! isWriteTxn() ; }
    
    protected boolean isWriteTxn() {
        Transaction txn = threadTxn.get();
        return txn.isWriteTxn() ;
    }
    
    protected void checkTxn() {
        if ( ! isActiveTxn() )
            throw new TransactionException("Not in a transaction") ;
    }

    protected void requireWriteTxn() {
        Transaction txn = threadTxn.get();
        if ( txn == null )
            System.err.println("Not a transaction");
        
        txn.requireWriteTxn() ;
    }
    
    protected void checkWriteTxn() {
        if ( ! isActiveTxn() || isReadTxn() )
            throw new TransactionException("Not in a write transaction") ;
    }

    // Make these all no-ops for "no checks"
    
    private void checkAligned(Transaction transaction) {
        if ( ! CHECKING ) return ;
        Transaction txn = threadTxn.get();
        if ( txn != transaction )
            throw new TransactionException("Transaction is not the transaction of the thread") ; 
    }
    
    private void checkState(TxnState expected) {
        if ( ! CHECKING ) return ;
        TxnState s = state.get();
        if ( s != expected )
            throw new TransactionException("Transaction is in state "+s+": expected state "+expected) ;
    }

    private void checkState(TxnState expected1, TxnState expected2) {
        if ( ! CHECKING ) return ;
        TxnState s = state.get();
        if ( s != expected1 && s != expected2 )
            throw new TransactionException("Transaction is in state "+s+": expected state "+expected1+" or "+expected2) ;
    }

    // Avoid varargs ... undue worry?
    private void checkState(TxnState expected1, TxnState expected2, TxnState expected3) {
        if ( ! CHECKING ) return ;
        TxnState s = state.get();
        if ( s != expected1 && s != expected2 && s != expected3 )
            throw new TransactionException("Transaction is in state "+s+": expected state "+expected1+", "+expected2+" or "+expected3) ;
    }

    //    private void checkStateNot(State unexpected) {
//        State s = state.get();
//        if ( s == unexpected )
//            throw new TransactionException("Transaction in unexpected state "+s) ;
//    }

    private void setState(TxnState newState) {
        if ( ! CHECKING ) return ;
        state.set(newState);
    }

}

