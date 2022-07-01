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

package org.apache.jena.graph.impl;

import java.util.function.Supplier ;

import org.apache.jena.graph.TransactionHandler ;
import org.apache.jena.shared.JenaException ;

/**
 * A base for transaction handlers; provide implementations of execute* operations
 * using the fundamental begin-commit-abort. 
 * (This class predates java8 default methods.) 
 */
public abstract class TransactionHandlerBase implements TransactionHandler {
    public TransactionHandlerBase() {
        super() ;
    }

    /* Abort but don't let problems with the transaction system itself cause loss of the exception */ 
    private void abort(Throwable e) {
        try { abort() ; }
        catch (Throwable th) { e.addSuppressed(th); }
    }
    
    /**
     * Execute the runnable <code>action</code> within a transaction. If it completes normally,
     * commit the transaction, otherwise abort the transaction.
     */
    @Override
    public void execute( Runnable action ) {
        begin() ;
        try {
            action.run();
            commit() ;
        }
        catch (JenaException e) { abort(e) ; throw e ; }
        catch (Throwable e)     { abort(e) ; throw new JenaException(e) ; }
    }
    
    /**
     * Execute the supplier <code>action</code> within a transaction. If it completes normally,
     * commit the transaction and return the result, otherwise abort the transaction.
     */
    @Override
    public <T> T calculate( Supplier<T> action ) {
        begin() ;
        try {
            T result = action.get() ;
            commit() ;
            return result ;
        }
        catch (JenaException e) { abort(e) ; throw e ; }
        catch (Throwable e)     { abort(e) ; throw new JenaException(e) ; }
    }
}

