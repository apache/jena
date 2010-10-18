/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.core;

import java.util.Iterator ;

import org.openjena.atlas.iterator.Iter ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;

/** Base class for implementations of a DatasetGraph as a set of graphs.
 */
public abstract class DatasetGraphCollection extends DatasetGraphBaseFind
{
    @Override
    public void add(Quad quad)
    {
        Graph g = fetchGraph(quad.getGraph()) ;
        if ( g == null )
            System.err.println("null graph") ;
        
        g.add(quad.asTriple()) ;
    }

    @Override
    public void delete(Quad quad)
    {
        Graph g = fetchGraph(quad.getGraph()) ;
        g.delete(quad.asTriple()) ;
    }
    
    @Override
    protected Iterator<Quad> findInDftGraph(Node s, Node p , Node o)
    {
        return triples2quadsDftGraph(getDefaultGraph().find(s, p, o)) ;
    }
    
    @Override
    protected Iter<Quad> findInSpecificNamedGraph(Node g, Node s, Node p , Node o)
    {
        Graph graph = fetchGraph(g) ;
        if ( g == null )
            return Iter.nullIter() ;
        return triples2quads(g, graph.find(s, p, o)) ;
    }

    @Override
    protected Iterator<Quad> findInAnyNamedGraphs(Node s, Node p, Node o)
    {
        Iterator<Node> gnames = listGraphNodes() ;
        Iterator<Quad> iter = null ;
        // Named graphs
        for ( ; gnames.hasNext() ; )  
        {
            Node gn = gnames.next();
            Iterator<Quad> qIter = findInSpecificNamedGraph(gn, s, p, o) ;
            if ( qIter != null )
                // copes with null for iter
                iter = Iter.append(iter, qIter) ;
        }
        return iter ;
    }

    
    //@Override
    public abstract Iterator<Node> listGraphNodes() ;

    protected Graph fetchGraph(Node gn)
    {
        if ( Quad.isDefaultGraph(gn) )
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