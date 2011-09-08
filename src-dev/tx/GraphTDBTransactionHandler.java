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

package tx;

import com.hp.hpl.jena.graph.TransactionHandler ;
import com.hp.hpl.jena.shared.Command ;
import com.hp.hpl.jena.tdb.store.GraphTDB ;

public class GraphTDBTransactionHandler implements TransactionHandler 
{
    // Replaces: TransactionHandlerTDB
    // Don't know read or write :-(
    // So grab the real transaction when first update occurs.
    // Start read.
    // If update, start write, end read.
    // 
    
    
    private final GraphTDB graph ;

    public GraphTDBTransactionHandler(GraphTDB graph)
    { this.graph = graph ; }
    
    @Override
    public boolean transactionsSupported()
    {
        return false ;
    }

    @Override
    public void begin()
    {}

    @Override
    public void abort()
    {}

    @Override
    public void commit()
    {}

    @Override
    public Object executeInTransaction(Command c)
    {
        return null ;
    }

}

