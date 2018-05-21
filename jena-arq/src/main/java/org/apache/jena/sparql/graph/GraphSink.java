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

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.TransactionHandler;
import org.apache.jena.graph.Triple ;
import org.apache.jena.graph.impl.GraphBase ;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.util.iterator.ExtendedIterator ;
import org.apache.jena.util.iterator.NullIterator ;

/** 
 * Black hole graph - adds and deletes are silently ignored.
 * @see GraphZero
 */
public class GraphSink extends GraphBase
{
    private static Graph graph = new GraphSink();
    public static Graph instance() {
        return graph;
    }
    
    @Override
    protected ExtendedIterator<Triple> graphBaseFind(Triple triple)
    { return NullIterator.instance() ; }
    
    @Override
    public void performAdd( Triple t ) {}
    
    @Override
    public void performDelete( Triple t ) {}
    
    @Override
    protected PrefixMapping createPrefixMapping() { 
        return new PrefixMappingSink() ;
    }
    
    @Override 
    public TransactionHandler getTransactionHandler() {
        return new TransactionHandlerNull(); 
    }
}
