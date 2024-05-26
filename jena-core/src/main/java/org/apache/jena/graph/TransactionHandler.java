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

package org.apache.jena.graph;

import java.util.function.Supplier ;

/**
    Preliminary interface for graphs supporting transactions.

 */
public interface TransactionHandler
{
    /**
        Does this handler support transactions at all?

        @return true iff begin/abort/commit are implemented and make sense.
     */
    boolean transactionsSupported();

    /**
        If transactions are supported, begin a new transaction. If transactions are
        not supported, or they are but this transaction is nested and nested transactions
        are not supported, throw an UnsupportedOperationException.
     */
    void begin();

    /**
        If transactions are supported and there is a transaction in progress, abort
        it. If transactions are not supported, or there is no transaction in progress,
        throw an UnsupportedOperationException.
     */
    void abort();

    /**
        If transactions are supported and there is a transaction in progress, commit
        it. If transactions are not supported, , or there is no transaction in progress,
        throw an UnsupportedOperationException.
     */
    void commit();

    /**
     * Execute the runnable <code>action</code> within a transaction. If it completes normally,
     * commit the transaction, otherwise abort the transaction.
     */
    void execute( Runnable action );

    /**
     * Execute inside a transaction if transactions supported - execute anyway if transactions not supported.
     */
    void executeAlways( Runnable action );


    /**
     * Execute the supplier <code>action</code> within a transaction. If it completes normally,
     * commit the transaction and return the result, otherwise abort the transaction.
     */
    <T> T calculate( Supplier<T> action ) ;

    /**
     * Calculate inside a transaction if transactions supported - calculate anyway if transactions not supported.
     */
    <T> T calculateAlways( Supplier<T> action );
}
