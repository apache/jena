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

package com.hp.hpl.jena.graph.impl;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.shared.*;

/**
     
    A base for transaction handlers - all it does is provide the canonical
    implementation of executeInTransaction.
*/
public abstract class TransactionHandlerBase implements TransactionHandler
    {
    public TransactionHandlerBase()
        { super(); }

    /**
        Execute the command <code>c</code> within a transaction. If it
        completes normally, commit the transaction and return the result.
        Otherwise abort the transaction and throw a wrapped exception.
    */
    @Override
    public Object executeInTransaction( Command c )
        {
        begin();
        try { Object result = c.execute(); commit(); return result; }
        catch (JenaException e) { abort(); throw e ; }
        catch (Throwable e) { abort(); throw new JenaException( e ); }
        }
    }
