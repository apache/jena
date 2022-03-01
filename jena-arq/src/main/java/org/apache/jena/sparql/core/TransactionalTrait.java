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

import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;

/**
 * Provide {@link Transactional} as an indirection to another {@link Transactional}.
 *
 * @apiNote Downside - the "getTxn" method is public. The alternative is
 *     copy-and-paste. See the {@code ExTransactions} in the jena-examples module.
 */
public interface TransactionalTrait extends Transactional {
    Transactional getTxn();
    @Override default public void begin(TxnType txnType)        { getTxn().begin(txnType); }
    @Override default public void begin(ReadWrite mode)         { getTxn().begin(mode); }
    @Override default public void commit()                      { getTxn().commit(); }
    @Override default public boolean promote(Promote txnType)   { return getTxn().promote(txnType); }
    @Override default public void abort()                       { getTxn().abort(); }
    @Override default public boolean isInTransaction()          { return getTxn().isInTransaction(); }
    @Override default public void end()                         { getTxn().end(); }
    @Override default public ReadWrite transactionMode()        { return getTxn().transactionMode(); }
    @Override default public TxnType transactionType()          { return getTxn().transactionType(); }
    default public boolean supportsTransactions()     { return true; }
    default public boolean supportsTransactionAbort() { return false; }
}
