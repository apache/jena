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

package com.hp.hpl.jena.sparql.graph;

import com.hp.hpl.jena.graph.* ;
import com.hp.hpl.jena.shared.AddDeniedException ;
import com.hp.hpl.jena.shared.DeleteDeniedException ;
import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;

/** Graph wrapper */
public class GraphWrapper implements Graph
{
    // Like WrappedGraph but pure wrapper - use this?
    final private Graph graph ;

    public GraphWrapper(Graph graph) { this.graph = graph ; }
    
    public Graph get() { return graph ; }
    
    @Override
    public void add(Triple t) throws AddDeniedException
    { graph.add(t) ; }

    @Override
    public boolean dependsOn(Graph other)
    {
        return graph.dependsOn(other) ;
    }

    @Override
    public TransactionHandler getTransactionHandler()
    {
        return graph.getTransactionHandler() ;
    }

    @Deprecated
    @Override
    public BulkUpdateHandler getBulkUpdateHandler()
    {
        return graph.getBulkUpdateHandler() ;
    }

    @Override
    public Capabilities getCapabilities()
    {
        return graph.getCapabilities() ;
    }

    @Override
    public GraphEventManager getEventManager()
    {
        return graph.getEventManager() ;
    }

    @Override
    public GraphStatisticsHandler getStatisticsHandler()
    {
        return graph.getStatisticsHandler() ;
    }

    @Override
    public PrefixMapping getPrefixMapping()
    {
        return graph.getPrefixMapping() ;
    }

    @Override
    public void delete(Triple t) throws DeleteDeniedException
    {
        graph.delete(t) ;
    }

    @Override
    public ExtendedIterator<Triple> find(TripleMatch m)
    {
        return graph.find(m) ;
    }

    @Override
    public ExtendedIterator<Triple> find(Node s, Node p, Node o)
    {
        return graph.find(s, p, o) ;
    }

    @Override
    public boolean isIsomorphicWith(Graph g)
    {
        return graph.isIsomorphicWith(g) ;
    }

    @Override
    public boolean contains(Node s, Node p, Node o)
    {
        return graph.contains(s, p, o) ;
    }

    @Override
    public boolean contains(Triple t)
    {
        return graph.contains(t) ;
    }

    @Override
    public void close()
    {
        graph.close() ;
    }

    @Override
    public boolean isEmpty()
    {
        return graph.isEmpty() ;
    }

    @Override
    public int size()
    {
        return graph.size() ;
    }

    @Override
    public boolean isClosed()
    {
        return graph.isClosed() ;
    }

    @Override
    public void clear()
    { graph.clear() ; }

    @Override
    public void remove(Node s, Node p, Node o)
    { graph.remove(s, p, o) ; }

}

