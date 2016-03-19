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

/** Implementation for "un-Transactional" interface.
 * 
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

    // As an included component. 
    /*
    private final Transactional txn                     = new TransactionalNotSupported() ;
    @Override public void begin(ReadWrite mode)         { txn.begin(mode) ; }
    @Override public void commit()                      { txn.commit() ; }
    @Override public void abort()                       { txn.abort() ; }
    @Override public boolean isInTransaction()          { return txn.isInTransaction() ; }
    @Override public void end()                         { txn.end(); }
    @Override public boolean supportsTransactions()     { return true ; }
    @Override public boolean supportsTransactionAbort() { return false ; }
    */
    
    @Override
    public void begin(ReadWrite readWrite)
    { throw new UnsupportedOperationException("Transactional.begin") ; }

    @Override
    public void commit()
    { throw new UnsupportedOperationException("Transactional.commit") ; }

    @Override
    public void abort()
    { throw new UnsupportedOperationException("Transactional.abort") ; }

    @Override
    public boolean isInTransaction()
    { return false ; }

    @Override
    public void end()
    { throw new UnsupportedOperationException("Transactional.end") ; }
    
    public boolean supportsTransactions() {
        return false ;
    }
    
    /** Declare whether {@link #abort} is supported.
     *  This goes along with clearing up after exceptions inside application transaction code.
     */
    public boolean supportsTransactionAbort() {
        return false;
    }
}

