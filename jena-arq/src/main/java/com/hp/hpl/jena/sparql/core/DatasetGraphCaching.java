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

package com.hp.hpl.jena.sparql.core;


import java.util.Iterator ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.lib.Cache ;
import org.apache.jena.atlas.lib.CacheFactory ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.GraphUtil ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;

/** 
 * DatasetGraph that <em>caches</em> calls to make graph implementations.  
 * Useful for storage layers that use triples+quads storage and the
 * graphs are wrappers to the actual storage layer.
 * 
 * The cache is finite and graphs will be dropped as needed. 
 *  
 * {@link DatasetGraphMap} provides an implementation which is an extensable collection of graphs.
 */
abstract public class DatasetGraphCaching extends DatasetGraphTriplesQuads
{
    private final boolean caching = true ;
    private boolean closed = false ;

    // read synchronised in this class, not needed for a sync wrapper.
    protected Graph defaultGraph = null ;
    protected Cache<Node, Graph> namedGraphs = CacheFactory.createCache(100) ;
    
    abstract protected void _close() ;
    abstract protected Graph _createNamedGraph(Node graphNode) ;
    abstract protected Graph _createDefaultGraph() ;
    abstract protected boolean _containsGraph(Node graphNode) ;
    
    protected DatasetGraphCaching() { this(100) ; }
    
    protected DatasetGraphCaching(int cacheSize)
    {
        if ( cacheSize <= 0 )
            throw new IllegalArgumentException("Cache size is less that 1: "+cacheSize) ;
        namedGraphs = CacheFactory.createCache(cacheSize) ;
    }
    
    @Override
    public boolean containsGraph(Node graphNode)
    {
        if ( namedGraphs.containsKey(graphNode) )
            // Empty graph may or may not count.
            // If they don't, need to override this method.
            return true ;
        return _containsGraph(graphNode) ;
    }
    
    @Override
    public final Graph getDefaultGraph()
    {
        if ( ! caching )
            return _createDefaultGraph() ;
        
        synchronized(this)
        {
            if ( defaultGraph == null )
                defaultGraph = _createDefaultGraph() ;
        }
        return defaultGraph ;
    }

    @Override
    public final Graph getGraph(Node graphNode)
    {
        if ( ! caching )
            return _createNamedGraph(graphNode) ;

        synchronized(this)
        {   // MRSW - need to create and update the cache atomically.
            Graph graph = namedGraphs.get(graphNode) ;
            if ( graph == null )
            {
                graph = _createNamedGraph(graphNode) ;
                namedGraphs.put(graphNode, graph) ;
            }
            return graph ;
        }
    }
    
    @Override
    public void addGraph(Node graphName, Graph graph)
    {
        removeGraph(graphName) ;
        GraphUtil.addInto(getGraph(graphName), graph) ;
    }
    
    @Override
    public final void removeGraph(Node graphName)
    { 
        deleteAny(graphName, Node.ANY, Node.ANY, Node.ANY) ;
        synchronized(this)
        {
            namedGraphs.remove(graphName) ;
        }
    }
    
    @Override
    public void clear() {
        synchronized(this) { 
            super.clear() ;
            namedGraphs.clear() ;
        }
    }

    @Override
    public void close()
    {
        synchronized(this)
        {
            if ( closed )
                return ;
            closed = true ;

            defaultGraph = null ;
            namedGraphs.clear() ;
            _close() ;
            super.close() ;
        }
    }
    
    // Helper implementations of operations.
    // Not necessarily efficient.
    protected static class Helper 
    {
        public static void addToDftGraph(DatasetGraphCaching dsg, Node s, Node p, Node o)
        {
            dsg.getDefaultGraph().add(new Triple(s,p,o)) ;
        }

        public static void addToNamedGraph(DatasetGraphCaching dsg, Node g, Node s, Node p, Node o)
        {
            dsg.getGraph(g).add(new Triple(s,p,o)) ;
        }


        public static void deleteFromDftGraph(DatasetGraphCaching dsg, Node s, Node p, Node o)
        {
            dsg.getDefaultGraph().delete(new Triple(s,p,o)) ;
        }

        public static void deleteFromNamedGraph(DatasetGraphCaching dsg, Node g, Node s, Node p, Node o)
        {
            dsg.getGraph(g).delete(new Triple(s,p,o)) ;
        }

        public static Iterator<Quad> findInAnyNamedGraphs(DatasetGraphCaching dsg, Node s, Node p, Node o)
        {
            Iterator<Node> iter = dsg.listGraphNodes() ;
            Iterator<Quad> quads = null ;
            for ( ; iter.hasNext() ; )
            {
                Node gn = iter.next() ;
                quads = Iter.append(quads, findInSpecificNamedGraph(dsg, gn, s, p, o)) ;
            }
            return quads ;
        }

        public static Iterator<Quad> findInDftGraph(DatasetGraphCaching dsg, Node s, Node p, Node o)
        {
            return triples2quadsDftGraph(dsg.getDefaultGraph().find(s, p, o)) ;
        }

        public static Iterator<Quad> findInSpecificNamedGraph(DatasetGraphCaching dsg, Node g, Node s, Node p, Node o)
        {
            return triples2quadsDftGraph(dsg.getGraph(g).find(s, p, o)) ;
        }
    }
}
