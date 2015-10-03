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

package org.apache.jena.tdb.graph;

import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.impl.TransactionHandlerBase ;
import org.apache.jena.tdb.TDB ;
import org.apache.jena.tdb.store.GraphTDB ;

/** Support for when TDB is used non-transactionally.does not support ACID transactions.  
 *  Flushes if commit is called although it denies supporting transactions
 */

public class TransactionHandlerTDB extends TransactionHandlerBase //implements TransactionHandler 
{
    private final Graph graph ;

    public TransactionHandlerTDB(GraphTDB graph)
    {
        this.graph = graph ;
    }
    
    @Override
    public void abort()
    {
        // Not the Jena old-style transaction interface
        throw new UnsupportedOperationException("TDB: 'abort' of a transaction not supported") ;
        //log.warn("'Abort' of a transaction not supported - ignored") ;
    }

    @Override
    public void begin()
    {}

    @Override
    public void commit()
    {
        TDB.sync(graph) ;
    }

    @Override
    public boolean transactionsSupported()
    {
        return false ;
    }
}
