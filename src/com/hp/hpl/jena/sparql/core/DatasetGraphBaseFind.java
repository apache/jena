/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.core;


import java.util.Iterator ;

import org.openjena.atlas.iterator.Iter ;

import com.hp.hpl.jena.graph.Node ;

/** 
 * DatasetGraph framework.  
 * This class contains a convenience implementation of find that maps to a split between 
 * defaultGraph/named graphs.
 * @see DatasetGraphTriplesQuads
 * @see DatasetGraphCollection
 * @see DatasetGraphOne
 * 
 */
abstract public class DatasetGraphBaseFind extends DatasetGraphBase 
{
    protected DatasetGraphBaseFind() {}
    
    /** Implementation of find based on splitting into triples (default graph) and quads (named graph) */
    //@Override
    public Iterator<Quad> find(Node g, Node s, Node p , Node o)
    {
        if ( ! isWildcard(g) )
        {
            if ( Quad.isDefaultGraph(g))
                return findInDftGraph(s,p,o) ;
            Iterator<Quad> qIter = findInSpecificNamedGraph(g, s, p, o) ;
            if ( qIter == null )
                return Iter.nullIterator() ;
            return qIter ;
        }

        return findAny(s, p, o) ;
    }
    
    //@Override
    public Iterator<Quad> findNG(Node g, Node s, Node p , Node o)
    {
        Iterator<Quad> qIter ;
        if ( ! isWildcard(g) )
            qIter = findInSpecificNamedGraph(g, s, p, o) ;
        else
            qIter = findInAnyNamedGraphs(s, p, o) ;
        if ( qIter == null )
            return Iter.nullIterator() ;
        return qIter ;
    }

    protected Iterator<Quad> findAny(Node s, Node p , Node o) 
    {
        // Default graph
        Iterator<Quad> iter1 = findInDftGraph(s, p, o) ;
        Iterator<Quad> iter2 = findInAnyNamedGraphs(s, p, o) ;

        if ( iter1 == null && iter2 == null )
            return Iter.nullIterator() ;
        // Copes with null in either position.
        return Iter.append(iter1, iter2) ;
    }

    protected abstract Iterator<Quad> findInDftGraph(Node s, Node p , Node o) ;
    protected abstract Iterator<Quad> findInSpecificNamedGraph(Node g, Node s, Node p , Node o) ;
    protected abstract Iterator<Quad> findInAnyNamedGraphs(Node s, Node p , Node o) ;
}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
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