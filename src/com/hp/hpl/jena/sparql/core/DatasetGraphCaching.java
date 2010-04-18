/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Information Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.core;


import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.lib.Cache ;
import com.hp.hpl.jena.sparql.lib.CacheFactory ;

/** 
 * DatasetGraph that <em>caches</em> calls to make graph implementations.  
 * Useful for storage layers that use triples+quads storage and the
 * graphs are wrappers to the actual storage layer.
 * 
 * The cache is finite and graphs will be dropped as needed. 
 *  
 * {@link DatasetGraphMap} provides an implementation which is an extensable collection of graphs.
 */
abstract public class DatasetGraphCaching extends DatasetGraphBase
{
    private final boolean caching = true ;
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
            // If they don't, need to override ths method.
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
    
//    @Override
//    public void addGraph(Node graphName, Graph graph)
//    { deleteAny(

    @Override
    public void removeGraph(Node graphName)
    { 
        deleteAny(graphName, Node.ANY, Node.ANY, Node.ANY) ;
    }

    @Override
    public void close()
    {
        defaultGraph = null ;
        namedGraphs.clear() ;
        _close() ;
        super.close() ;
    }
}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Information Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */