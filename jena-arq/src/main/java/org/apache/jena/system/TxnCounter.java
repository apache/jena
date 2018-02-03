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

package org.apache.jena.system;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.JenaTransactionException;
import org.apache.jena.sparql.core.Transactional;

/** A MR+SW transactional Counter */ 
public class TxnCounter implements Transactional {
    
    // ---- TransactionCoordinator.
    
    // Semaphore to implement "Single Active Writer" - independent of readers
    // This is not reentrant.
    private Semaphore writersWaiting = new Semaphore(1, true);
    
    private void releaseWriterLock() {
        int x = writersWaiting.availablePermits();
        if ( x != 0 )
            throw new JenaTransactionException("TransactionCoordinator: Probably mismatch of enable/disableWriter calls");
        writersWaiting.release();
    }
    
    private boolean acquireWriterLock(boolean canBlock) {
        if ( ! canBlock )
            return writersWaiting.tryAcquire();
        try { 
            writersWaiting.acquire(); 
            return true;
        } catch (InterruptedException e) { throw new JenaTransactionException(e); }
    }
    // ---- TransactionCoordinator.
    
    // Transaction state.
    static class IntegerState {
        long txnValue;
        public IntegerState(long v) { this.txnValue = v; }
    }
    
    // Global state - the exterally visible value and the starting point for any
    // transaction. This is set to a new value when a write transaction commits.
    
    private final AtomicLong value = new AtomicLong(-1712); 
    private final AtomicLong epoch = new AtomicLong(1);

    // ---- Transaction state.
    
    // The per-transaction state (inside a transaction). Null outside a transaction
    // cleared by commit or abort in a write transaction.
    private ThreadLocal<IntegerState> transactionValue = ThreadLocal.withInitial(()->null);
    // The kind of transaction.
    private ThreadLocal<ReadWrite>    transactionMode  = ThreadLocal.withInitial(()->null);
    private ThreadLocal<TxnType>      transactionType  = ThreadLocal.withInitial(()->null);
    private ThreadLocal<Long>         transactionEpoch = ThreadLocal.withInitial(()->null);
    
    // Synchronization for making changes.  
    private Object txnLifecycleLock   = new Object(); 
    
    public TxnCounter(long x) {
        value.set(x);
    }
    
    @Override
    public void begin(ReadWrite readWrite) {
        begin(TxnType.convert(readWrite));
    }
    
    @Override
    public void begin(TxnType txnType) {
        begin(txnType, true);
    }
    
    public void begin(TxnType txnType, boolean canBlock) {
        // Ensure a single writer. 
        // (Readers never block at this point.)
        if ( txnType == TxnType.WRITE ) {
            // Writers take a WRITE permit from the semaphore to ensure there
            // is at most one active writer, else the attempt to start the
            // transaction blocks.
            // Released by in commit/abort.
            acquireWriterLock(canBlock);
        }
        // at this point, 
        // One writer or one of many readers. 
        
        synchronized(txnLifecycleLock) {
            if ( transactionMode.get() != null )
                throw new JenaTransactionException("Already in a transaction");
            // Set transaction to current epoch - writes advance this in commit().
            transactionEpoch.set(epoch.get()) ;
            IntegerState state = new IntegerState(value.get());
            transactionValue.set(state);
            transactionMode.set(TxnType.initial(txnType));
            transactionType.set(txnType);
        }
    }

    @Override
    public boolean promote() {
        return promote(transactionType.get());
    }
    
    @Override
    public boolean promote(TxnType txnType) {
        checkTxn();
        if ( transactionMode.get() == ReadWrite.WRITE )
            return true;
        if ( txnType == TxnType.READ )
            throw new JenaTransactionException("Attempt to promote a READ transsction");
        if ( txnType == TxnType.READ_COMMITTED_PROMOTE ) {
            // READ_COMMITTED_PROMOTE
            acquireWriterLock(true);
            transactionMode.set(ReadWrite.WRITE);
            IntegerState state = new IntegerState(value.get());
            transactionValue.set(state);
            return true;
        }
        // READ_PROMOTE
        acquireWriterLock(true);
        synchronized(txnLifecycleLock) {
            long nowEpoch = epoch.get();
            if ( transactionEpoch.get() != nowEpoch ) {
                // Can't.
                releaseWriterLock();
                return false;
            }
            // Can.
            transactionMode.set(ReadWrite.WRITE);
        }
        return true;
    }

    @Override
    public void commit() {
        checkTxn(); 
        if ( isWriteTxn() ) {
            // Theer is only one writer - we are inside the writer lock. 
            // Advance the epoch.
            long thisEpoch = epoch.incrementAndGet();
            value.set(getDataState().txnValue);
            transactionValue.set(null);
            releaseWriterLock();
        }
        endOnce();
    }

    @Override
    public void abort() {
        checkTxn(); 
        if ( isWriteTxn() ) {
            transactionValue.set(null); 
            releaseWriterLock();
        }
        endOnce();
    }

    @Override
    public boolean isInTransaction() {
        return Lib.readThreadLocal(transactionMode) != null;
    }

    @Override
    public ReadWrite transactionMode() {
        return Lib.readThreadLocal(transactionMode);
    }
    
    @Override
    public TxnType transactionType() {
        return Lib.readThreadLocal(transactionType);
    }

    @Override
    public void end() {
        if ( ! isInTransaction() ) 
            return;
        if ( isWriteTxn() && transactionValue.get() != null )
            throw new JenaTransactionException("No commit or abort before end for a write transaction");
        endOnce();
    }

    private void endOnce() {
        if ( isActiveTxn() ) {
            synchronized(txnLifecycleLock) {
                transactionValue.remove();
                transactionType.remove();
                transactionMode.remove();
                transactionEpoch.remove();
            }
        }
    }

    /** Increment the value inside a write transaction */ 
    public void inc() {
        checkWriteTxn();
        IntegerState ts = getDataState();
        ts.txnValue++;
    }
    
    /** Decrement the value inside a write transaction */ 
    public void dec() {
        checkWriteTxn();
        IntegerState ts = getDataState();
        ts.txnValue--;
    }

    /** Set the value inside a write transaction, return the old value*/ 
    public long set(long x) {
        checkWriteTxn();
        IntegerState ts = getDataState();
        long v = ts.txnValue;
        ts.txnValue = x;
        return v;
    }
    

    /** Return the current value in a transaction. 
     * Must be inside a transaction. 
     * @see #get
     */
    public long read() {
        checkTxn();
        return getDataState().txnValue;
    }

    /** Return the current value.
     * If inside a transaction, return the transaction view of the value.
     * If not in a transaction return the state value (effectively
     * a read transaction, optimized by the fact that reading the
     * {@code TransInteger} state is atomic).
     */
    public long get() {
        if ( isActiveTxn() )
            return getDataState().txnValue;
        else
            return value.get();
    }

    /** Read the current global state (that is, the last committed value) outside a transaction. */
    public long value() {
        return value.get();
    }

    // These two operations not clear the thread local if we are not in a transaction.
    // This is a potential memory leak.
    // Use "isInTransaction" to read and clear.

    /** Is this a write transaction? Should be called inside a transaction. */
    private boolean isWriteTxn() {
        ReadWrite rw = transactionMode.get();
        if ( rw == null )
            throw new JenaTransactionException(Lib.classShortName(this.getClass())+".isWriteTxn called outside a transaction");
        return transactionMode.get() == ReadWrite.WRITE;
    }

    private boolean isActiveTxn() {
        ReadWrite rw = transactionMode.get();
        return rw != null;
    }

    private IntegerState getDataState() {
        return transactionValue.get();
    }

    private void checkWriteTxn() {
        if ( ! isWriteTxn() )
            throw new JenaTransactionException("Not in a write transaction");
    }

    private void checkTxn() {
        if ( ! isActiveTxn() )
            throw new JenaTransactionException("Not in a transaction");
    }
}
