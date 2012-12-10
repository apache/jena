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

package com.hp.hpl.jena.sparql.graph;

import com.hp.hpl.jena.graph.BulkUpdateHandler ;
import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.TransactionHandler ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.graph.impl.SimpleBulkUpdateHandler ;
import com.hp.hpl.jena.graph.impl.SimpleTransactionHandler ;
import com.hp.hpl.jena.graph.impl.WrappedGraph ;
import com.hp.hpl.jena.shared.AddDeniedException ;
import com.hp.hpl.jena.shared.DeleteDeniedException ;

public class GraphReadOnly extends WrappedGraph
{
    public GraphReadOnly(Graph graph) { super(graph) ; }
    
    @Override
    public void add(Triple t) throws AddDeniedException
    { throw new AddDeniedException("read-only graph") ; }

    @Override
    public void performAdd(Triple t) throws AddDeniedException
    { throw new AddDeniedException("read-only graph") ; }

    @Override
    public void delete(Triple t) throws DeleteDeniedException
    { throw new DeleteDeniedException("read-only graph") ; }
    
    @Override
    public void performDelete(Triple t) throws DeleteDeniedException
    { throw new DeleteDeniedException("read-only graph") ; }
    
    @Override
    public TransactionHandler getTransactionHandler()
    {
        // AKA "no".  
        return new SimpleTransactionHandler() ;
    }

    @Deprecated
    @Override
    public BulkUpdateHandler getBulkUpdateHandler()
    {
        //This turns all operations into calls to add/remove.
        return new SimpleBulkUpdateHandler(this) ;
    }
}
