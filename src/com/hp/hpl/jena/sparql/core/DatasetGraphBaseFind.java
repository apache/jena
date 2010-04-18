/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Information Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.core;


import java.util.Iterator ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.lib.iterator.Iter ;
import com.hp.hpl.jena.sparql.lib.iterator.Transform ;

/** 
 * DatasetGraph framework.  This class contains a convenience implementation of find that maps to defaultGraph/named graphs.
 */
abstract public class DatasetGraphBaseFind extends DatasetGraphBase 
{
    protected DatasetGraphBaseFind() {}
    
    @Override
    public Iterator<Quad> find(Node g, Node s, Node p , Node o)
    {
        if ( ! isWildcard(g) )
        {
            if ( Quad.isDefaultGraph(g))
                return findInDftGraph(s,p,o) ;
            Iterator<Quad> qIter = findInNamedGraphs(g, s, p, o) ;
            if ( qIter == null )
                return Iter.nullIterator() ;
            return qIter ;
        }
        
        // Wildcard for g
        // Default graph
        Iterator<Quad> iter = findInDftGraph(s, p, o) ;

        Iterator<Node> gnames = listGraphNodes() ;
        // Named graphs
        for ( ; gnames.hasNext() ; )  
        {
            Node gn = gnames.next();
            Iterator<Quad> qIter = findInNamedGraphs(gn, s, p, o) ;
            if ( qIter != null )
                iter = Iter.append(iter, qIter) ;
        }
        return iter ;
    }

    protected abstract Iterator<Quad> findInDftGraph(Node s, Node p , Node o) ;
    protected abstract Iterator<Quad> findInNamedGraphs(Node g, Node s, Node p , Node o) ;

    protected static Iterator<Quad> triples2quadsDftGraph(Iterator<Triple> iter)
    {
        return triples2quads(Quad.tripleInQuad, iter) ;
    }
    
    protected static Iter<Quad> triples2quads(final Node graphNode, Iterator<Triple> iter)
    {
        Transform<Triple, Quad> transformNamedGraph = new Transform<Triple, Quad> () {
            public Quad convert(Triple triple)
            {
                return new Quad(graphNode, triple) ;
            }
        } ;
            
        return Iter.iter(iter).map(transformNamedGraph) ;
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