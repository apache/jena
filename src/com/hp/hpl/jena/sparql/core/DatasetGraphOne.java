/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Systems Ltd.
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.core;

import java.util.Iterator ;

import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.iterator.NullIterator ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;

/** 
 * DatasetGraph of a single graph as default graph. 
 * Fixed as one graph (the default) - can not add named graphs.
 */
public class DatasetGraphOne extends DatasetGraphBaseFind
{
    private final Graph graph ;
    
    protected DatasetGraphOne(Graph graph) { this.graph = graph ; }
    
    @Override
    public boolean containsGraph(Node graphNode)
    {
        return false ;
    }
    
    @Override
    public Graph getDefaultGraph() { return graph ; }

    @Override
    public Graph getGraph(Node graphNode) { return null ; }

    public Iterator<Node> listGraphNodes()
    {
        return new NullIterator<Node>() ;
    }

    @Override
    public long size()
    {
        return 0 ;
    }

    @Override
    public void add(Node g , Node s, Node p, Node o)
    {
        if (  Quad.isDefaultGraph(g) )
            graph.add(new Triple(s, p, o)) ;
        else
            throw new UnsupportedOperationException("DatasetGraphOne.add/named graph") ;
    }
    
    @Override
    public void add(Quad quad)
    { 
        if (  isDefaultGraph(quad) )
            graph.add(quad.asTriple()) ;
        else
            throw new UnsupportedOperationException("DatasetGraphOne.add/named graph") ;
    }      
    
    @Override
    public void delete(Node g , Node s, Node p, Node o)
    {
        if (  Quad.isDefaultGraph(g) )
            graph.delete(new Triple(s, p, o)) ;
        else
            throw new UnsupportedOperationException("DatasetGraphOne.delete/named graph") ;
    }

    @Override
    public void delete(Quad quad)
    {
        if (  isDefaultGraph(quad) )
            graph.delete(quad.asTriple()) ;
        else
            throw new UnsupportedOperationException("DatasetGraphOne.delete/named graph") ;
    }

    @Override
    public void setDefaultGraph(Graph g)    
    { throw new UnsupportedOperationException("DatasetGraphOne.setDefaultGraph") ; }

    @Override
    public void addGraph(Node graphName, Graph graph)
    { throw new UnsupportedOperationException("DatasetGraphOne.addGraph") ; }

    @Override
    public void removeGraph(Node graphName)
    { throw new UnsupportedOperationException("DatasetGraphOne.removeGraph") ; }

    // -- Not needed -- implement find(g,s,p,o) directly.
    @Override
    protected Iterator<Quad> findInDftGraph(Node s, Node p, Node o)
    { 
        if ( true ) throw new UnsupportedOperationException() ;
        return triples2quadsDftGraph(graph.find(s, p ,o)) ; }

    // -- Not needed.
    @Override
    protected Iterator<Quad> findInSpecificNamedGraph(Node g, Node s, Node p, Node o)
    {
        if ( true ) throw new UnsupportedOperationException() ;
        // There are no named graphs
        return Iter.nullIterator() ;
    }

    // -- Not needed.
    @Override
    protected Iterator<Quad> findInAnyNamedGraphs(Node s, Node p, Node o)
    {
        if ( true ) throw new UnsupportedOperationException() ;
        // There are no named graphs
        return Iter.nullIterator() ;
    }

    protected static boolean isDefaultGraph(Quad quad)
    {
        return isDefaultGraph(quad.getGraph()) ;
    }

    protected static boolean isDefaultGraph(Node quadGraphNode)
    {
        return ( quadGraphNode == null || Quad.isDefaultGraph(quadGraphNode) ) ;
    }

    // It's just easier and more direct ...
    @Override
    public Iterator<Quad> find(Node g, Node s, Node p , Node o)
    {
        if ( isWildcard(g) || isDefaultGraph(g) )
            return triples2quadsDftGraph(graph.find(s, p, o)) ;
        else
            return new NullIterator<Quad>() ;
    }
    
    
    @Override
    public void close()
    { 
        graph.close();
        super.close() ;
    }
}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Systems Ltd.
 * (c) Copyright 2010 Epimorphics Ltd.
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