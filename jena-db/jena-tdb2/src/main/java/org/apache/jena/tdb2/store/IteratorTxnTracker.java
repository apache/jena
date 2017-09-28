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

package org.apache.jena.tdb2.store;

import java.util.Iterator;
import java.util.Objects;

import org.apache.jena.atlas.iterator.IteratorWrapper;
import org.apache.jena.dboe.transaction.txn.Transaction;
import org.apache.jena.dboe.transaction.txn.TransactionException;
import org.apache.jena.dboe.transaction.txn.TransactionalSystem;
import org.apache.jena.dboe.transaction.txn.TxnId;

/** Wrapper to check that an iterator is used inside its originating transaction. */
public class IteratorTxnTracker<T> extends IteratorWrapper<T> {
    private final TransactionalSystem txnSystem;
    private final TxnId txnId;

    public IteratorTxnTracker(Iterator<T> iterator, TransactionalSystem txnSystem, TxnId txnId) {
        super(iterator);
        this.txnSystem = Objects.requireNonNull(txnSystem, "TransactionalSystem");
        this.txnId = Objects.requireNonNull(txnId, "TxnId") ;
    }

    @Override public boolean hasNext()  { check() ; return super.hasNext() ; }

    @Override public T next()           { check() ; return super.next() ; }
    
    @Override public void remove()      { check() ; super.remove() ; }

    private void check() {
        Transaction txn = txnSystem.getThreadTransaction();
        if ( txn == null )
            throw new TransactionException("Iterator used outside its original transaction");
        if ( txn != null && txnId.equals(txn.getTxnId()) )
            return ;
        throw new TransactionException("Iterator used inside a different transaction");
    }
}