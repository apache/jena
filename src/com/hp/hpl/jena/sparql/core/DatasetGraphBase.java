/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.core;


import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.shared.LockMRSW;
import com.hp.hpl.jena.sparql.lib.CacheFactory;
import com.hp.hpl.jena.sparql.lib.Cache;
import com.hp.hpl.jena.sparql.util.Context ;

/** 
 * DatasetGraph that caches graphs created.
 */
abstract public class DatasetGraphBase implements DatasetGraph
{
    private final Lock lock = new LockMRSW() ;
    private Context context = new Context() ;

    
    private final boolean caching = true ;
    // read synchronised in this class, not need for a sync wrapper.
    protected Graph defaultGraph = null ;
    protected Cache<Node, Graph> namedGraphs = CacheFactory.createCache(100) ;
    
    abstract protected void _close() ;
    abstract protected Graph _createNamedGraph(Node graphNode) ;
    abstract protected Graph _createDefaultGraph() ;
    abstract protected boolean _containsGraph(Node graphNode) ;
    
    protected DatasetGraphBase() {}
    
    //@Override
    public boolean containsGraph(Node graphNode)
    {
        if ( namedGraphs.containsKey(graphNode) )
            // Empty graph may or may not count.
            // If they don't, need to override ths method.
            return true ;
        return _containsGraph(graphNode) ;
    }
    
    //@Override
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

    //@Override
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

    //@Override
    public Lock getLock()
    {
        return lock ;
    }
    
    public Context getContext()
    {
        return context ;
    }
    
    //@Override
    public final void close()
    {
        defaultGraph = null ;
        namedGraphs.clear() ;
        _close() ;
    }

//    @Override
//    public Iterator<Node> listGraphNodes()
//    {
//        return null ;
//    }

//    @Override
//    public int size()
//    {
//        return 0 ;
//    }

    
    
}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
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