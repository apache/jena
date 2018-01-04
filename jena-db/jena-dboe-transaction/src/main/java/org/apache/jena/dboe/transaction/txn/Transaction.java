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

import static org.apache.jena.dboe.transaction.txn.TxnState.ABORTED;
import static org.apache.jena.dboe.transaction.txn.TxnState.ACTIVE;
import static org.apache.jena.dboe.transaction.txn.TxnState.COMMIT;
import static org.apache.jena.dboe.transaction.txn.TxnState.COMMITTED;
import static org.apache.jena.dboe.transaction.txn.TxnState.DETACHED;
import static org.apache.jena.dboe.transaction.txn.TxnState.END_ABORTED;
import static org.apache.jena.dboe.transaction.txn.TxnState.END_COMMITTED;
import static org.apache.jena.dboe.transaction.txn.TxnState.INACTIVE;
import static org.apache.jena.dboe.transaction.txn.TxnState.PREPARE;

import java.util.List ;
import java.util.Objects ;
import java.util.concurrent.atomic.AtomicReference ;

import org.apache.jena.query.ReadWrite ;
import org.apache.jena.query.TxnType;

/** 
 * A transaction as the composition of actions on components. 
 * Works in conjunction with the TransactionCoordinator 
 * to provide the transaction lifecycle.
 * @see TransactionCoordinator
 * @see TransactionalComponent
 */
final
public class Transaction implements TransactionInfo {
    // Using an AtomicReference<TxnState> requires that 
    // TransactionalComponentLifecycle.internalComplete
    // frees the thread local for the threadTxn, otherwise memory
    // usage grows. If a plain member variable is used slow growth
    // is still seen. 
    // Nulling txnMgr and clearing components stops that slow growth.

    private TransactionCoordinator txnMgr ;
    private final TxnId txnId ;
    private final List<SysTrans> components ;
    
    // Using an AtomicReference makes this observable from the outside.
    // It also allow for multithreaded transactions (later). 
    private final AtomicReference<TxnState> state = new AtomicReference<>() ;
    //private TxnState state ;
    private final long dataVersion ;
    private final TxnType txnType ;
    private ReadWrite mode ;
    
    public Transaction(TransactionCoordinator txnMgr, TxnType txnType, ReadWrite readWrite, TxnId txnId, long dataVersion, List<SysTrans> components) {
        Objects.requireNonNull(txnMgr) ;
        Objects.requireNonNull(txnId) ;
        Objects.requireNonNull(readWrite) ;
        Objects.requireNonNull(components) ;
        this.txnMgr = txnMgr ;
        this.txnId = txnId ;
        this.txnType = txnType ;
        this.mode = readWrite ;
        this.dataVersion = dataVersion ;
        this.components = components ;
        setState(INACTIVE) ;
    }
    
//    /*package*/ void resetDataVersion(long dataVersion) {
//        this.dataVersion = dataVersion;
//    }

    /*package*/ void setState(TxnState newState) {
        state.set(newState) ;
    }

    @Override
    public TxnState getState() {
        return state.get() ;
    }

    /**
     * Each transaction is allocated a serialization point by the transaction
     * coordinator. Normally, this is related to this number and it increases
     * over time as the data changes. Two readers can have the same
     * serialization point - they are working with the same view of the data.
     */
    @Override
    public long getDataVersion() {
        return dataVersion ;
    }

    public void begin() {
        checkState(INACTIVE);
        components.forEach((c) -> c.begin()) ;
        setState(ACTIVE) ;
    }
    
    public boolean promote() {
        checkState(ACTIVE);
        boolean b = txnMgr.promoteTxn(this) ;
        if ( !b )
            return false ;
        mode = ReadWrite.WRITE;
        return true ;
    }
    
    /*package*/ void promoteComponents() {
        // Call back from the Transaction coordinator during promote.
        components.forEach((c) -> {
            if ( ! c.promote() )
                throw new TransactionException("Failed to promote") ;
        }) ;
        mode = ReadWrite.WRITE ;
    }
    
    public void notifyUpdate() {
        checkState(ACTIVE) ;
        if ( mode == ReadWrite.READ ) {
            System.err.println("notifyUpdate - promote needed") ;
            promote() ;
            mode = ReadWrite.WRITE ;
        }
    }
    
    public void prepare() {
        checkState(ACTIVE) ;
        if ( mode == ReadWrite.WRITE ) 
            txnMgr.executePrepare(this) ;
        setState(PREPARE);
    }
    
    public void commit() { 
        // XXX Split into READ and WRITE forms.
        TxnState s = getState();
        if ( s == ACTIVE ) 
            // Auto exec prepare().
            prepare() ;
        checkState(PREPARE) ;
        setState(COMMIT) ;
        switch(mode) {
            case WRITE:
                txnMgr.executeCommit(this,
                                     ()->{components.forEach((c) -> c.commit()) ; } ,
                                     ()->{components.forEach((c) -> c.commitEnd()) ; } ) ;
                break ;
            case READ:
                // Different lifecycle?
                txnMgr.executeCommit(this, 
                                     ()->{components.forEach((c) -> c.commit()) ; } ,
                                     ()->{components.forEach((c) -> c.commitEnd()) ; } ) ;
                break ;
        }
        setState(COMMITTED) ;
    }
    
    public void abort() {
        abort$();
        endInternal() ;
    }

    private void abort$() {
        // Split into READ and WRITE forms.
        checkState(ACTIVE, ABORTED) ;
        // Includes notification start/finish
        txnMgr.executeAbort(this, ()-> { components.forEach((c) -> c.abort()) ; }) ;
        setState(ABORTED) ;
    }

    public void end() {
        txnMgr.notifyEndStart(this) ;
        if ( isWriteTxn() && getState() == ACTIVE ) {
            //Log.warn(this, "Write transaction with no commit() or abort() before end()");
            // Just the abort process.
            abort$() ;
            endInternal() ;
            throw new TransactionException("Write transaction with no commit() or abort() before end() - forced abort") ;
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
    
    /*package*/ List<SysTrans> getComponents() {
        return components ;
    }
    
    /*package*/ void detach() {
        checkState(ACTIVE,PREPARE) ;
        setState(DETACHED) ;
    }
    
    /*package*/ void attach() {
        checkState(DETACHED) ;
        setState(ACTIVE) ;
    }
    
    public void requireWriteTxn() {
        checkState(ACTIVE) ;
        if ( mode != ReadWrite.WRITE )
            throw new TransactionException("Not a write transaction") ;
    }

    @Override
    public boolean hasStarted()   { 
        TxnState x = getState() ;
        return x == INACTIVE ;
    }
    
    @Override
    public boolean hasFinished() { 
        TxnState x = getState() ;
        return x == COMMITTED || x == ABORTED || x == END_COMMITTED || x == END_ABORTED ;
    }

    @Override
    public boolean hasFinalised() { 
        TxnState x = getState() ;
        return x == END_COMMITTED || x == END_ABORTED ;
    }

    @Override
    public TxnId getTxnId()         { return txnId ; } 

    @Override
    public TxnType   getTxnType()   { return txnType ; }

    @Override
    public ReadWrite getMode()      { return mode ; }
    
    /** Is this a READ transaction?
     * Convenience operation equivalent to {@code (getMode() == READ)}
     */
    @Override
    public boolean isReadTxn()  { return mode == ReadWrite.READ ; }

    /** Is this a WRITE transaction?
     * Convenience operation equivalent to {@code (getMode() == WRITE)}
     */
    @Override
    public boolean isWriteTxn()  { return mode == ReadWrite.WRITE ; }
    
    // hashCode/equality
    // These must be object equality.  No two transactions objects are .equals unless they are ==   
    
    private void checkWriteTxn() {
        if ( ! isActiveTxn() || ! isWriteTxn() )
            throw new TransactionException("Not in a write transaction") ;
    }

    // XXX Duplicate -- TransactionalComponentLifecycle
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
    
    @Override
    public boolean isActiveTxn() {
        return getState() != INACTIVE ;
    }
}

