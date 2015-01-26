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

package org.seaborne.dboe.transaction;

import java.nio.ByteBuffer ;

import org.seaborne.dboe.transaction.txn.ComponentId ;
import org.seaborne.dboe.transaction.txn.TransactionCoordinator ;
import org.seaborne.dboe.transaction.txn.TransactionalComponentLifecycle ;
import org.seaborne.dboe.transaction.txn.TxnId ;

import com.hp.hpl.jena.query.ReadWrite ;

/** This class is stateless in the transaction.
 * It simply add the {@link TransactionalComponentLifecycle} machinary.
 * Useful if there is no other TransactionalComponentLifecycle
 * in the {@link TransactionCoordinator}.
 */
public class TransLifecycleMonitor extends TransactionalComponentLifecycle<Object> {

    @Override
    public ComponentId getComponentId() {
        return null ;
    }

    @Override
    public void startRecovery() {}

    @Override
    public void recover(ByteBuffer ref) {}

    @Override
    public void finishRecovery() {}

    @Override
    protected Object _begin(ReadWrite readWrite, TxnId txnId) {
        return new Object() ;
    }

    @Override
    protected ByteBuffer _commitPrepare(TxnId txnId, Object state) {
        return null ;
    }

    @Override
    protected void _commit(TxnId txnId, Object state) {}

    @Override
    protected void _commitEnd(TxnId txnId, Object state) {}

    @Override
    protected void _abort(TxnId txnId, Object state) {}

    @Override
    protected void _complete(TxnId txnId, Object state) {}

    @Override
    protected void _shutdown() {}
}
