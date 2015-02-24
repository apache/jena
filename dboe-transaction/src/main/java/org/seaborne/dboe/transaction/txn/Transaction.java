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
import static org.seaborne.dboe.transaction.txn.Transaction.TxnState.END_ABORTED ;
import static org.seaborne.dboe.transaction.txn.Transaction.TxnState.END_COMMITTED ;
import static org.seaborne.dboe.transaction.txn.Transaction.TxnState.INACTIVE ;
import static org.seaborne.dboe.transaction.txn.Transaction.TxnState.PREPARE ;

import java.nio.ByteBuffer ;
import java.util.ArrayList ;
import java.util.List ;
import java.util.Objects ;
import java.util.concurrent.atomic.AtomicReference ;

import com.hp.hpl.jena.query.ReadWrite ;

/** 
 * A transaction as the composition of actions on components. 
 * Works in conjunction with the TransactionCoordinator 
 * to provide the transaction lifecycle.
 * @see TransactionCoordinator
 * @see TransactionalComponent
 */
final
public class Transaction {
    // Using an AtomicReference<TxnState> requires that 
    // TransactionalComponentLifecycle.internalComplete
    // frees the thread local for the threadTxn.  
    // If a plain member variable is used slow growth is still seen.
    // Nulling txnMgr and clearing components stops that slow growth.

    private TransactionCoordinator txnMgr ;
    private final TxnId txnId ;
    private final List<SysTrans> components ;
    
    // Using an AtomicReference makes this observable from the outside. 
    private final AtomicReference<TxnState> state = new AtomicReference<TxnState>() ;
    //private TxnState state ;
    private final long dataEpoch ;
    private ReadWrite mode ;
    
    public enum TxnState { INACTIVE, ACTIVE, PREPARE, COMMIT, COMMITTED, ABORTED, END_COMMITTED, END_ABORTED }
    
    public Transaction(TransactionCoordinator txnMgr, TxnId txnId, ReadWrite readWrite, long dataEpoch, List<SysTrans> components) {
        Objects.requireNonNull(txnMgr) ;
        Objects.requireNonNull(txnId) ;
        Objects.requireNonNull(readWrite) ;
        this.txnMgr = txnMgr ;
        this.txnId = txnId ;
        this.mode = readWrite ;
        this.dataEpoch = dataEpoch ;
        this.components = components ;
        setState(INACTIVE) ;
    }
    
    public void add(SysTrans x) {
        components.add(x) ;
    }

    private void setState(TxnState newState) {
        state.set(newState) ;
        //state = newState ;
    }

    public TxnState getState() {
        return state.get() ;
        //return state ; 
    }

    /**
     * Each transaction is allocated a serialization point by the transaction
     * coordinator. Normally, this is related to the This number inceases over
     * time as the data changes. Two readers can have the same serialization
     * point - they are working with the same view of the data.
     */
    public long getSerilizationId() {
        return dataEpoch ;
    }

    public void begin() {
        checkState(INACTIVE);
        components.forEach((c) -> c.begin()) ;
        setState(ACTIVE) ;
    }
    
    public void requireWriteTxn() {
        checkState(ACTIVE) ;
        if ( mode != ReadWrite.WRITE )
            throw new TransactionException("Not a write transaction") ;
    }

    public void notifyUpdate() {
        checkState(ACTIVE) ;
        if ( mode == ReadWrite.READ ) {
            setState(ACTIVE) ;
            txnMgr.promote(this) ;
            mode = ReadWrite.WRITE ;
        }
    }
    
    public void commit() { 
        checkState(ACTIVE) ;
        setState(PREPARE) ;
        List<PrepareState> txnPrepared = prepare() ;
        setState(COMMIT) ;
        
        // Includes notification start/finish
        txnMgr.executeCommit(this, txnPrepared,
              ()->{components.forEach((c) -> c.commit()) ; } ,
              ()->{components.forEach((c) -> c.commitEnd()) ; } ) ;
        
        setState(COMMITTED) ;
    }
    
    public void abort() {
        checkState(ACTIVE, ABORTED) ;
        // Includes notification start/finish
        txnMgr.executeAbort(this, ()-> { components.forEach((c) -> c.abort()) ; }) ;
        setState(ABORTED) ;
        endInternal() ;
    }

    private List<PrepareState> prepare() {
        txnMgr.notifyPrepareStart(this);
        List<PrepareState> x = new ArrayList<>(components.size()) ;
        components.forEach((c) -> {
            ByteBuffer data = c.commitPrepare() ;
            if ( data != null ) {
                PrepareState s = new PrepareState(c.getComponentId(), data) ;
                x.add(s) ;
            }
        }) ;
        txnMgr.notifyPrepareFinish(this);
        return x ; 
    }

    public void end() {
        txnMgr.notifyEndStart(this) ;
        if ( isWriteTxn() && getState() == ACTIVE ) {
            throw new TransactionException("Write transaction with no commit or abort") ; 
            //abort() ;
        }
        endInternal() ;
        txnMgr.notifyEndFinish(this) ;
        txnMgr = null ;
        //components.clear() ;
    }
    
    private void endInternal() {  
        if ( hasFinalised() )
            return ;
        // Called once, at the first abort/commit/end.
        txnMgr.notifyCompleteStart(this);
        components.forEach((c) -> c.complete()) ;
        txnMgr.completed(this) ;
        if ( getState() == COMMITTED )
            setState(END_COMMITTED);
        else
            setState(END_ABORTED);
        txnMgr.notifyCompleteFinish(this);
    }
    
    public boolean hasStarted()   { 
        TxnState x = getState() ;
        return x == INACTIVE ;
    }
    
    public boolean hasFinished() { 
        TxnState x = getState() ;
        return x == COMMITTED || x == ABORTED || x == END_COMMITTED || x == END_ABORTED ;
    }

    public boolean hasFinalised() { 
        TxnState x = getState() ;
        return x == END_COMMITTED || x == END_ABORTED ;
    }

    public TxnId getTxnId()     { return txnId ; } 
    public ReadWrite getMode()  { return mode ; }
    
    // hashCode/equality
    // These must be object equality.  No two transactions objects are .equals unless they are ==   
    
    private void checkWriteTxn() {
        if ( ! isActiveTxn() || ! isWriteTxn() )
            throw new TransactionException("Not in a write transaction") ;
    }

    private void checkState(TxnState expected) {
        TxnState s = getState();
        if ( s != expected )
            throw new TransactionException("Transaction is in state "+s+": expected state "+expected) ;
    }

    private void checkState(TxnState expected1, TxnState expected2) {
        TxnState s = getState();
        if ( s != expected1 && s != expected2 )
            throw new TransactionException("Transaction is in state "+s+": expected state "+expected1+" or "+expected2) ;
    }

    // Avoid varargs ... undue worry?
    private void checkState(TxnState expected1, TxnState expected2, TxnState expected3) {
        TxnState s = getState();
        if ( s != expected1 && s != expected2 && s != expected3 )
            throw new TransactionException("Transaction is in state "+s+": expected state "+expected1+", "+expected2+" or "+expected3) ;
    }
    
    public boolean isActiveTxn() {
        return getState() != INACTIVE ;
    }
    
    public boolean isWriteTxn() {
        return mode == ReadWrite.WRITE ;
    }
}

