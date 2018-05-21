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

import org.apache.jena.query.ReadWrite ;
import org.apache.jena.query.TxnType;

/** Implementation for "un-Transactional" interface.
 * 
 * @see TransactionalNull for a do nothing implementation of Transactions.
 * @see TransactionalNotSupportedMixin
 */ 
public class TransactionalNotSupported implements Transactional
{
    /* Using as an interface mixin (trait) and "implements TransactionalNotSupportedMixin" 
     * does not always work. This may be a Eclipse limitation.
     * The problem arises with hierarchies involving Transactional
     * where transaction methods are also in the hierarchy. 
     */

    // Sometimes implementations will have to include this code
    // directly to override super class versions.

    // As an included component: 
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
//        // For Datasets: add:
//        @Override public boolean supportsTransactions()     { return false; }
//        @Override public boolean supportsTransactionAbort() { return false; }
    }
    
    public static Transactional create() { return new TransactionalNotSupported(); }
    
    @Override
    public void begin()
    { throw new UnsupportedOperationException("Transactional.begin()") ; }

    @Override
    public void begin(TxnType txnType)
    { throw new UnsupportedOperationException("Transactional.begin(TxnType") ; }

    @Override
    public void begin(ReadWrite readWrite)
    { throw new UnsupportedOperationException("Transactional.begin(ReadWrite)") ; }

    @Override public boolean promote(Promote txnType)
    { throw new UnsupportedOperationException("Transactional.promote") ; }

    @Override
    public void commit()
    { throw new UnsupportedOperationException("Transactional.commit") ; }

    @Override
    public void abort()
    { throw new UnsupportedOperationException("Transactional.abort") ; }

    @Override
    public boolean isInTransaction()
    { return false ; }
    
    @Override public ReadWrite transactionMode()
    { throw new UnsupportedOperationException("Transactional.transactionMode") ; }
    
    @Override public TxnType transactionType()
    { throw new UnsupportedOperationException("Transactional.transactionType") ; }

    @Override
    public void end()
    { throw new UnsupportedOperationException("Transactional.end") ; }
    
    public boolean supportsTransactions() {
        return false ;
    }
    
    public boolean supportsTransactionAbort() {
        return false;
    }
}

