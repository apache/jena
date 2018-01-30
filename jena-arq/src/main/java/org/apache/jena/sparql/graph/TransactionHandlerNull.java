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

package org.apache.jena.sparql.graph;

import org.apache.jena.graph.TransactionHandler;
import org.apache.jena.graph.impl.TransactionHandlerBase;
import org.apache.jena.sparql.JenaTransactionException;

/** Implementation of {@link TransactionHandler} that does nothing but track the transaction state. */
public class TransactionHandlerNull extends TransactionHandlerBase {
 
    private ThreadLocal<Boolean> inTransaction = ThreadLocal.withInitial(()->Boolean.FALSE);
    
    @Override
    public boolean transactionsSupported() {
        return true;
    }
    
    @Override
    public void begin() {
        if ( inTransaction.get() )
            throw new JenaTransactionException("Already in transaction"); 
        inTransaction.set(true);
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

    public void remove() {
        inTransaction.remove();
    }
}
