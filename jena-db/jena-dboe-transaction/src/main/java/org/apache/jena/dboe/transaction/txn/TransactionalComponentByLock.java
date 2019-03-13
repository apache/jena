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

import java.nio.ByteBuffer;

import org.apache.jena.atlas.logging.Log;

import org.apache.jena.query.ReadWrite;
import org.apache.jena.shared.Lock;
import org.apache.jena.shared.LockMRSW;

//  ** Not used currently **
/** Implementation of the component interface for {@link TransactionalComponent}.
 *  Useful for in-memory transactions that do not provide durability or abort (undo).
 *  When retro fitting to other systems, that may be the best that can be done.
 */
public class TransactionalComponentByLock extends TransactionalComponentLifecycle<Object> {
    //See org.apache.jena.sparql.core.TransactionalLock
    private Lock lock = new LockMRSW();

    private TransactionalComponentByLock(ComponentId componentId) {
        super(componentId);
    }

    // ---- Recovery phase
    @Override
    public void startRecovery() {}

    @Override
    public void recover(ByteBuffer ref) {
        Log.warn(this, "Called to recover a transaction (ignored)");
    }

    @Override
    public void finishRecovery() { }

    @Override
    public void cleanStart() {}

    @Override
    protected Object _begin(ReadWrite readWrite, TxnId thisTxnId) {
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
        // We have a read lock - there are no other writers.

        // No lock promotion.
        // Best we can do is unlock and lock again:-(
        // This is "read committed"
        if ( isReadTxn() ) {
            finishReadTxn();
            startWriteTxn();
        }
        return createState();
    }

    protected void startReadTxn()   { lock.enterCriticalSection(Lock.READ); }
    protected void startWriteTxn()  { lock.enterCriticalSection(Lock.WRITE); }
    protected void finishReadTxn()  { lock.leaveCriticalSection(); }
    protected void finishWriteTxn() { lock.leaveCriticalSection(); }

    @Override
    protected ByteBuffer _commitPrepare(TxnId txnId, Object obj) {
        return null;
    }

    @Override
    protected void _commit(TxnId txnId, Object obj) {
        clearup();
    }

    @Override
    protected void _commitEnd(TxnId txnId, Object obj) {
        clearup();
    }

    @Override
    protected void _abort(TxnId txnId, Object obj) {
        clearup();
    }

    @Override
    protected void _complete(TxnId txnId, Object obj) {
    }

    @Override
    protected void _shutdown() {
        lock = null;
    }

    private void clearup() {
        if ( isWriteTxn() )
            finishWriteTxn();
        else
            finishReadTxn();
    }
}

