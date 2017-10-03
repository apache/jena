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

import java.nio.ByteBuffer ;

import org.apache.jena.query.ReadWrite ;

/**
 * A transaction component that does nothing - can be used as a helper for
 * management tasks hooked into the transaction component lifecycle but which
 * are not stateful across restarts.
 */
public class TransactionalComponentBase<X> extends TransactionalComponentLifecycle<X> {
    
    public TransactionalComponentBase(ComponentId id) {
        super(id) ;
    }
    
    @Override
    public void startRecovery() {}

    @Override
    public void recover(ByteBuffer ref) {}

    @Override
    public void finishRecovery() {}
    
    @Override 
    public void cleanStart() {}

    @Override
    protected X _begin(ReadWrite readWrite, TxnId txnId) {
        return null ;
    }

    @Override
    protected ByteBuffer _commitPrepare(TxnId txnId, X state) {
        return null ;
    }

    @Override
    protected void _commit(TxnId txnId, X state) {}

    @Override
    protected void _commitEnd(TxnId txnId, X state) {}

    @Override
    protected void _abort(TxnId txnId, X state) {}

    @Override
    protected void _complete(TxnId txnId, X state) {}

    @Override
    protected void _shutdown() {}

    @Override
    protected X _promote(TxnId txnId, X state) { return null; }

}

