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

package org.apache.jena.sparql.core;


import java.util.concurrent.Callable ;

import org.apache.jena.atlas.lib.Cache ;
import org.apache.jena.atlas.lib.CacheFactory ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.GraphUtil ;
import org.apache.jena.graph.Node ;

/** 
 * DatasetGraph that <em>caches</em> calls to make graph implementations.  
 * Useful for storage layers that use triples+quads storage and the
 * graphs are wrappers to the actual storage layer.
 * 
 * The cache is finite and graphs will be dropped as needed. 
 *  
 * @deprecated This class will be removed.
 */
@Deprecated
abstract public class DatasetGraphCaching extends DatasetGraphTriplesQuads
{
    private final boolean caching = true ;
    private boolean closed = false ;

    // read synchronised in this class, not needed for a sync wrapper.
    private Graph defaultGraph = null ;
    private Cache<Node, Graph> namedGraphs = CacheFactory.createCache(100) ;
    
    @Deprecated abstract protected void _close() ;
    @Deprecated abstract protected Graph _createNamedGraph(Node graphNode) ;
    @Deprecated abstract protected Graph _createDefaultGraph() ;
    @Deprecated abstract protected boolean _containsGraph(Node graphNode) ;
    
    protected DatasetGraphCaching() { this(100) ; }
    
    protected DatasetGraphCaching(int cacheSize) {
        if ( cacheSize <= 0 )
            throw new IllegalArgumentException("Cache size is less that 1: " + cacheSize) ;
        namedGraphs = CacheFactory.createCache(cacheSize) ;
    }

    @Override
    public boolean containsGraph(Node graphNode) {
        if ( namedGraphs.containsKey(graphNode) )
            // Empty graph may or may not count.
            // If they don't, need to override this method.
            return true ;
        return _containsGraph(graphNode) ;
    }

    @Override
    public final Graph getDefaultGraph() {
        if ( !caching )
            return _createDefaultGraph() ;

        synchronized (this) {
            if ( defaultGraph == null )
                defaultGraph = _createDefaultGraph() ;
        }
        return defaultGraph ;
    }

    @Override
    public final Graph getGraph(final Node graphNode) {
        if ( !caching )
            return _createNamedGraph(graphNode) ;

        synchronized (this) { 
            Callable<Graph> filler = new Callable<Graph>() {
                @Override
                public Graph call() {
                    return _createNamedGraph(graphNode) ;
                }} ;
            // MRSW - need to create and update the cache atomically.
            Graph graph = namedGraphs.getOrFill(graphNode, filler) ;
            return graph ;
        }
    }

    @Override
    public void addGraph(Node graphName, Graph graph) {
        removeGraph(graphName) ;
        GraphUtil.addInto(getGraph(graphName), graph) ;
    }

    @Override
    public final void removeGraph(Node graphName) {
        super.removeGraph(graphName);
        synchronized (this) {
            namedGraphs.remove(graphName) ;
        }
    }

    @Override
    public void clear() {
        synchronized (this) {
            super.clear() ;
            namedGraphs.clear() ;
        }
    }

    @Override
    public void close() {
        synchronized (this) {
            if ( closed )
                return ;
            closed = true ;

            defaultGraph = null ;
            namedGraphs.clear() ;
            _close() ;
            super.close() ;
        }
    }
}
