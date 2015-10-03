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

import org.apache.jena.graph.impl.GraphBase ;
import org.apache.jena.mem.GraphMem ;
import org.apache.jena.util.iterator.ExtendedIterator ;
import org.apache.jena.util.iterator.NullIterator ;

/**
    A factory class for creating Graphs.

*/

public class Factory
    {
    private Factory()
        { super(); }
    
    /**
        Answer a memory-based Graph.
    */
    public static Graph createDefaultGraph()
        { return Factory.createGraphMem( ); }
              
    public static Graph createGraphMem()
        { return new GraphMem(); }

    public static Graph createGraphMemWithTransactionHandler( final TransactionHandler th )
        {
        Graph g = new GraphMem() 
            {
            @Override
            public TransactionHandler getTransactionHandler() 
                {  return th; }
            };
        return g;
        }

    private static Graph emptyGraph = new GraphBase() {

        @Override
        protected ExtendedIterator<Triple> graphBaseFind(Triple triplePattern) {
            return NullIterator.instance() ;
        }
    } ;
 
    /** Immutable graph with no triples */ 
    public static Graph empty() { return emptyGraph ; }

    }