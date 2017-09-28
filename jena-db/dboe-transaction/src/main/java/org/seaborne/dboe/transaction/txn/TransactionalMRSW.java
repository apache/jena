/*
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

import java.nio.ByteBuffer ;
import java.util.concurrent.locks.Lock ;
import java.util.concurrent.locks.ReadWriteLock ;
import java.util.concurrent.locks.ReentrantReadWriteLock ;

import org.apache.jena.atlas.logging.Log ;

import org.apache.jena.query.ReadWrite ;

/** Implementation of the component interface for {@link TransactionalComponent}.
 *  Useful for in-memory transactions that do not provide durability or abort (undo). 
 *  When retro fitting to other systems, that may be the best that can be done. 
 */
public class TransactionalMRSW extends TransactionalComponentLifecycle<Object> {
    // MRSW implementation of TransactionMVCC
    // XXX Update to Jena style TransactionalLock
    private ReadWriteLock lock = new ReentrantReadWriteLock() ;
    
    public TransactionalMRSW(ComponentId componentId) {
        super(componentId) ;
    }

    // ---- Recovery phase
    @Override
    public void startRecovery() {}
    
    @Override
    public void recover(ByteBuffer ref) {
        Log.warn(this, "Called to recover a transaction (ignored)") ; 
    }

    @Override
    public void finishRecovery() { }
    
    @Override 
    public void cleanStart() {}
    
    private Lock getLock() {
        return ( ReadWrite.WRITE.equals(getReadWriteMode()) ) ? lock.writeLock() : lock.readLock() ;
    }
    
    @Override
    protected Object _begin(ReadWrite readWrite, TxnId thisTxnId) {
        Lock lock = getLock() ;
        // This is the point that makes this MRSW (readers OR writer), not MR+SW (readers and a writer)
        lock.lock();
        if ( isWriteTxn() )
            startWriteTxn(); 
        else 
            startReadTxn(); 
        return createState();                    
    }

    private Object createState() {
        return new Object();
    }
    
    @Override
    protected Object _promote(TxnId txnId, Object state) {
        // We have a read lock, the transaction coordinator has said 
        // it's OK (from it's point-of-view) to promote so this should succeed.
        // We have a read lock - theer are no other writers.
        boolean b = lock.writeLock().tryLock();
        if ( ! b ) {
            Log.warn(this, "Failed to promote");  
            return false;
        }
        lock.readLock().unlock(); 
        return createState(); 
    }

    // Checks.
    
    protected void startReadTxn()   {}
    protected void startWriteTxn()  {}
    protected void finishReadTxn()  {}
    protected void finishWriteTxn() {}

    @Override
    protected ByteBuffer _commitPrepare(TxnId txnId, Object obj) {
        return null ;
    }

    @Override
    protected void _commit(TxnId txnId, Object obj) {
        clearup() ;
    }

    @Override
    protected void _commitEnd(TxnId txnId, Object obj) {
        clearup() ;
    }

    @Override
    protected void _abort(TxnId txnId, Object obj) {
        clearup() ;
    }

    @Override
    protected void _complete(TxnId txnId, Object obj) {
    }

    @Override
    protected void _shutdown() {
        lock = null ;
    }

    private void clearup() {
        Lock lock = getLock() ;
        if ( isWriteTxn() )
            finishWriteTxn(); 
        else 
            finishReadTxn(); 
        lock.unlock(); 
    }
}

