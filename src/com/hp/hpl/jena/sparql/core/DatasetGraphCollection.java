/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.core;

import java.util.Iterator ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.lib.iterator.Iter ;
import com.hp.hpl.jena.util.iterator.NullIterator ;

/** Base class for implementations of a DatasetGraph as a set of graphs.
 *  Translates quad calls to graph calls.
 * @see DatasetGraphQuad Base class for a quad view, converting graph calls to quads. 
 */
public abstract class DatasetGraphCollection extends DatasetGraphBase
{
    @Override
    public void add(Quad quad)
    {
        Graph g = fetchGraph(quad.getGraph()) ;
        g.add(quad.asTriple()) ;
    }

    @Override
    public void delete(Quad quad)
    {
        Graph g = fetchGraph(quad.getGraph()) ;
        g.delete(quad.asTriple()) ;
    }

    @Override
    public Iterator<Quad> find(final Node g, Node s, Node p , Node o)
    {
        if ( ! isWildcard(g) )
        {
            Graph graph = fetchGraph(g) ;
            if ( graph == null )
                return new NullIterator<Quad>() ;
            return triples2quadsNamedGraph(g, graph.find(s, p, o)) ;
        }
        
        // Wildcard
        // Default graph
        Iter<Quad> iter = triples2quadsDftGraph(getDefaultGraph().find(s, p, o)) ;

        Iterator<Node> gnames = listGraphNodes() ;
        // Named graphs
        for ( ; gnames.hasNext() ; )  
        {
            Node gn = gnames.next();
            Graph graph = getGraph(gn) ;
            Iter<Quad> qIter = triples2quadsNamedGraph(gn, graph.find(s, p, o)) ;
            iter = iter.append(qIter) ;
        }
        return iter ;
    }
    
    //@Override
    public abstract Iterator<Node> listGraphNodes() ;

    protected Graph fetchGraph(Node gn)
    {
        if ( Quad.isDefaultGraph(gn))
            return getDefaultGraph() ;
        else
            return getGraph(gn) ;
    }
}

/*
 * (c) Copyright 2010 Talis Systems Ltd.
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