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

import java.util.ArrayList ;
import java.util.HashSet ;
import java.util.Iterator ;
import java.util.List ;
import java.util.Set ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.graph.TripleMatch ;
import com.hp.hpl.jena.graph.impl.GraphBase ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;
import com.hp.hpl.jena.util.iterator.WrappedIterator ;

/** Very simple, non-scalable DatasetGraph implementation of a triples+quads
 * style for testing the upper levels of the class hierarchy.
 */
public class DSG_Mem extends DatasetGraphCaching
{
    List<Triple> triples = new ArrayList<Triple>() ;
    List<Quad> quads = new ArrayList<Quad>() ;

    private int indexTriple(Triple triple)
    {
        for ( int i = 0 ; i < triples.size() ; i++ )
        {
            Triple t = triples.get(i) ;
            if ( t.equals(triple) )
                return i ;
        }
        return -1 ;
    }

    private int indexQuad(Quad quad)
    {
        for ( int i = 0 ; i < triples.size() ; i++ )
        {
            Quad q = quads.get(i) ;
            if ( q.equals(quad) )
                return i ;
        }
        return -1 ;
    }
    
    @Override
    public Iterator<Quad> findInDftGraph(Node s, Node p , Node o) 
    {
        List<Quad> results = new ArrayList<Quad>() ;
        for ( Triple t : triples )
            if ( matches(t, s, p, o) )
                // ?? Quad.defaultGraphNodeGenerated
                //Quad.defaultGraphIRI
                results.add(new Quad(Quad.defaultGraphIRI, t)) ;
        return results.iterator() ;
    }
    
    @Override
    public Iterator<Quad> findInSpecificNamedGraph(Node g, Node s, Node p , Node o)
    {
        List<Quad> results = new ArrayList<Quad>() ;
        for ( Quad q : quads )
            if ( matches(q, g, s, p, o) )
                results.add(q) ;
        return results.iterator() ;
    }
    
    @Override
    public Iterator<Quad> findInAnyNamedGraphs(Node s, Node p , Node o)
    {
        List<Quad> results = new ArrayList<Quad>() ;
        for ( Quad q : quads )
            if ( matches(q, Node.ANY, s, p, o) )
                results.add(q) ;
        return results.iterator() ;
    }

    private boolean matches(Triple t, Node s, Node p, Node o)
    {
        if ( s == null ) s = Node.ANY ;
        if ( p == null ) p = Node.ANY ;
        if ( o == null ) o = Node.ANY ;
        return t.matches(s,p,o) ;
    }

    private boolean matches(Quad q, Node g, Node s, Node p, Node o)
    {
        if ( g == null ) g = Node.ANY ;
        if ( s == null ) s = Node.ANY ;
        if ( p == null ) p = Node.ANY ;
        if ( o == null ) o = Node.ANY ;
        return q.matches(g,s,p,o) ;
    }

    @Override
    protected void addToDftGraph(Node s, Node p, Node o)
    {
        Triple t = new Triple(s, p, o) ;
        if ( ! triples.contains(t) )
            triples.add(t) ;
    }

    @Override
    protected void addToNamedGraph(Node g, Node s, Node p, Node o)
    {
        Quad q = new Quad(g, s, p, o) ;
        if ( ! quads.contains(q) )
            quads.add(q) ;
    }

    @Override
    protected void deleteFromDftGraph(Node s, Node p, Node o)
    {
        triples.remove(new Triple(s, p, o)) ;
    }

    @Override
    protected void deleteFromNamedGraph(Node g, Node s, Node p, Node o)
    {
        quads.remove(new Quad(g, s, p, o)) ;
    }

    class GraphDft extends GraphBase
    {
        @Override
        public void performAdd(Triple t)
        {
            if ( ! triples.contains(t) )
                triples.add(t) ;
        }

        @Override
        public void performDelete(Triple t) { triples.remove(t) ; }

        @Override
        protected ExtendedIterator<Triple> graphBaseFind(TripleMatch m)
        {
            List<Triple> results = new ArrayList<Triple>() ;
            for ( Triple t : triples )
                if ( t.matches(m.getMatchSubject(), m.getMatchPredicate(), m.getMatchObject()) )
                    results.add(t) ;
            return WrappedIterator.create(results.iterator()) ;
        }
    }
    
    class GraphNamed extends GraphBase
    {
        private final Node graphName ;

        GraphNamed(Node gname) { this.graphName = gname ; }
        
        @Override
        public void performAdd(Triple t)
        {
            Quad q = new Quad(graphName, t) ;
            if ( ! quads.contains(q) )
                quads.add(q) ;
        }

        @Override
        public void performDelete(Triple t) { Quad q = new Quad(graphName, t) ; quads.remove(q) ; }

        @Override
        protected ExtendedIterator<Triple> graphBaseFind(TripleMatch m)
        {
            List<Triple> results = new ArrayList<Triple>() ;
            for ( Quad q : quads )
            {
                if ( matches(q, graphName, m.getMatchSubject(), m.getMatchPredicate(), m.getMatchObject()) )
                    results.add(q.asTriple()) ;
            }
            return WrappedIterator.create(results.iterator()) ;
        }
    }
    
    @Override
    protected Graph _createDefaultGraph()
    {
        return new GraphDft() ;
    }

    @Override
    protected Graph _createNamedGraph(Node graphNode)
    {
        return new GraphNamed(graphNode) ;
    }

    @Override
    protected boolean _containsGraph(Node graphNode)
    {
        return graphNodes().contains(graphNode) ;
    }

    @Override
    public Iterator<Node> listGraphNodes()
    {
        return graphNodes().iterator() ;
    }
    
    private Set<Node> graphNodes()
    {
        Set<Node> x = new HashSet<Node>() ;
        for ( Quad q : quads )
            x.add(q.getGraph()) ;
        return x ;
    }

    @Override
    protected void _close()
    {}
}
