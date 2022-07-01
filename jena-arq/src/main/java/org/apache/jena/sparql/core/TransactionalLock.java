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

package org.apache.jena.sparql.core;

import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.query.ReadWrite ;
import org.apache.jena.query.TxnType;
import org.apache.jena.shared.Lock ;
import org.apache.jena.shared.LockMRPlusSW ;
import org.apache.jena.shared.LockMRSW ;
import org.apache.jena.shared.LockMutex ;
import org.apache.jena.sparql.JenaTransactionException ;

/** An implementation of Transactional that provides MRSW locking but no abort.
 *  This is often the best you can do for a system that does not itself provide transactions.
 *  @apiNote
 *  To use with implementation inheritance, for when you don't inherit:
 *  <pre>
 *      private final Transactional txn                     = TransactionalLock.createMRSW() ;
 *      private final Transactional txn()                   { return txn; }
 *      {@literal @}Override public void begin(TxnType txnType)        { txn().begin(txnType) ; }
 *      {@literal @}Override public void begin()                       { txn().begin(); }
 *      {@literal @}Override public void begin(TxnType txnType)        { txn().begin(txnType); }
 *      {@literal @}Override public boolean promote()                  { return txn().promote(); }
 *      {@literal @}Override public void commit()                      { txn().commit(); }
 *      {@literal @}Override public void abort()                       { txn().abort(); }
 *      {@literal @}Override public boolean isInTransaction()          { return txn().isInTransaction(); }
 *      {@literal @}Override public void end()                         { txn().end(); }
 *      {@literal @}Override public ReadWrite transactionMode()        { return txn().transactionMode(); }
 *      {@literal @}Override public TxnType transactionType()          { return txn().transactionType(); }
 *      {@literal @}Override public boolean supportsTransactions()     { return true; }
 *      {@literal @}Override public boolean supportsTransactionAbort() { return false; }
 *   </pre>
 */
public class TransactionalLock implements Transactional {
/*
    private final Transactional txn                     = TransactionalLock.createMRSW() ;
    private final Transactional txn()                   { return txn; }
    @Override public void begin()                       { txn().begin(); }
    @Override public void begin(TxnType txnType)        { txn().begin(txnType); }
    @Override public void commit()                      { txn().commit(); }
    @Override public void abort()                       { txn().abort(); }
    @Override public boolean isInTransaction()          { return txn().isInTransaction(); }
    @Override public void end()                         { txn().end(); }
    @Override public ReadWrite transactionMode()        { return txn().transactionMode(); }
    @Override public TxnType transactionType()          { return txn().transactionType(); }
    @Override public boolean supportsTransactions()     { return true; }
    @Override public boolean supportsTransactionAbort() { return false; }
 */

    private ThreadLocal<Boolean>   inTransaction = ThreadLocal.withInitial(() -> Boolean.FALSE);
    private ThreadLocal<TxnType>   txnType = ThreadLocal.withInitial(() -> null);
    private ThreadLocal<ReadWrite> txnMode = ThreadLocal.withInitial(() -> null);
    private final Lock lock ;

    /** Create a Transactional using the given lock */
    public static TransactionalLock create(Lock lock) {
        return new TransactionalLock(lock) ;
    }

    /** Create a Transactional using a MR+SW (Multiple Reader AND a Single Writer) lock */
    public static TransactionalLock createMRPlusSW() {
        return create(new LockMRPlusSW()) ;
    }

    /** Create a Transactional using a MRSW (Multiple Reader OR a Single Writer) lock */
    public static TransactionalLock createMRSW() {
        return create(new LockMRSW()) ;
    }

    /** Create a Transactional using a mutex (exclusive - one at a time) lock */
    public static TransactionalLock createMutex() {
        return create(new LockMutex()) ;
    }

    protected TransactionalLock(Lock lock) {
        this.lock = lock ;
    }

    @Override
    public void begin(ReadWrite readWrite) {
        begin(TxnType.convert(readWrite));
    }

    @Override
    public void begin(TxnType txnType) {
        if ( isInTransaction() )
            error("Already in a transaction") ;
        switch(txnType) {
            case READ_PROMOTE:
            case READ_COMMITTED_PROMOTE:
                throw new UnsupportedOperationException("begin(TxnType."+txnType+")");
            default:
        }
        ReadWrite readWrite = TxnType.convert(txnType);
        boolean isRead = readWrite.equals(ReadWrite.READ) ;
        lock.enterCriticalSection(isRead);
        this.inTransaction.set(true);
        this.txnMode.set(readWrite);
        this.txnType.set(txnType);
    }

    @Override public ReadWrite transactionMode() {
        return Lib.readThreadLocal(txnMode) ;
    }

    @Override public TxnType transactionType() {
        return Lib.readThreadLocal(txnType) ;
    }

    // Lock promotion required (Ok for mutex)

    @Override
    public boolean promote(Promote txnType) {
        return false;
    }

    @Override
    public void commit() {
        endOnce() ;
    }

    @Override
    public void abort() {
        endOnce() ;
    }

    @Override
    public boolean isInTransaction() {
        return inTransaction.get();
    }

    public boolean isTransactionMode(ReadWrite mode) {
        if ( ! isInTransaction() )
            return false;
        return Lib.readThreadLocal(txnMode) == mode;
    }

    @Override
    public void end() {
        if ( isTransactionMode(ReadWrite.WRITE) )
            error("Write transaction - no commit or abort before end()") ;
        endOnce() ;
    }

    protected void endOnce() {
        if ( isInTransaction() ) {
            lock.leaveCriticalSection() ;
            txnMode.set(null);
            txnType.set(null);
            inTransaction.set(false);
            txnMode.remove();
            txnType.remove();
            inTransaction.remove();
        }
    }

    protected void error(String msg) {
        throw new JenaTransactionException(msg) ;
    }
}
