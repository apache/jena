/**
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

package org.apache.jena.sparql.core;

import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.sparql.JenaTransactionException;

/**
 * A null action {@link Transactional}.
 * Does not protect anything but does track the transaction status.
 * It does provide "abort".   
 */
public class TransactionalNull implements Transactional {
    
    // Usage example:
    private static class Example implements Transactional {  
        private final Transactional txn                     = new TransactionalNotSupported() ;
        @Override public void begin()                       { txn.begin(); }
        @Override public void begin(TxnType txnType)        { txn.begin(txnType); }
        @Override public void begin(ReadWrite mode)         { txn.begin(mode); }
        @Override public void commit()                      { txn.commit(); }
        @Override public void abort()                       { txn.abort(); }
        @Override public boolean promote(Promote mode)      { return txn.promote(mode) ; }
        @Override public boolean isInTransaction()          { return txn.isInTransaction(); }
        @Override public void end()                         { txn.end(); }
        @Override public ReadWrite transactionMode()        { return txn.transactionMode(); }
        @Override public TxnType transactionType()          { return txn.transactionType(); }
     
//        For DatasetGraphs:
//        @Override public boolean supportsTransactions()     { return true; }
//        @Override public boolean supportsTransactionAbort() { return false; }
    }
    
    public static Transactional create() { return new TransactionalNull(); }
    
    private ThreadLocal<Boolean> inTransaction = ThreadLocal.withInitial(() -> Boolean.FALSE);
    private ThreadLocal<TxnType> txnType = ThreadLocal.withInitial(() -> null);
    private ThreadLocal<ReadWrite> txnMode = ThreadLocal.withInitial(() -> null);

    @Override
    public ReadWrite transactionMode() {
        return txnMode.get();
    }
    
    @Override 
    public TxnType transactionType() {
        return txnType.get();
    }

    @Override
    public void begin(ReadWrite readWrite) {
        begin(TxnType.convert(readWrite));
    }

    @Override
    public void begin(TxnType type) {
        if ( inTransaction.get() )
            throw new JenaTransactionException("Already in transaction"); 
        inTransaction.set(true);
        txnType.set(type);
        txnMode.set(TxnType.initial(type));
    }

    @Override
    public boolean promote(Promote txnType) {
        if ( ! inTransaction.get() )
            throw new JenaTransactionException("Not in transaction"); 
        txnMode.set(ReadWrite.WRITE);
        return true;
    }

    @Override
    public void commit() {
        if ( ! inTransaction.get() )
            throw new JenaTransactionException("Not in transaction"); 
        inTransaction.set(false);
    }

    @Override
    public void abort() {
        if ( ! inTransaction.get() )
            throw new JenaTransactionException("Not in transaction"); 
        inTransaction.set(false);
    }

    @Override
    public boolean isInTransaction() {
        return inTransaction.get();
    }

    @Override
    public void end() {
        clearup();
    }

    private void clearup() {
        inTransaction.set(false);
        inTransaction.remove();
        txnType.set(null);
        txnType.remove();
    }
    
    public void remove() {
        inTransaction.remove();
    }
}
