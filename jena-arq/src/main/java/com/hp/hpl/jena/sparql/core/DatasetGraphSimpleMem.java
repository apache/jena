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
import java.util.Collection ;
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

/** Very simple, non-scalable DatasetGraph implementation 
 * of a triples+quads style for testing.
 */
public class DatasetGraphSimpleMem extends DatasetGraphCaching
{
    private MiniSet<Triple> triples = new MiniSet<>() ;
    private MiniSet<Quad> quads = new MiniSet<>() ;
    
    /** Simple abstraction of a Set */
    private static class MiniSet<T> implements Iterable<T>
    {
        final Collection<T> store ; 
        MiniSet(Collection<T> store) { this.store = store ; }
        
        MiniSet() { this.store = new ArrayList<>() ; }
        
        void add(T t)
        {
            if ( !store.contains(t) ) 
                store.add(t) ;
        }
        
        void remove(T t)
        {
            store.remove(t) ; 
        }

        @Override
        public Iterator<T> iterator()
        {
            return store.iterator() ;
        }
        
        boolean isEmpty() { return store.isEmpty() ; }
        
        int size() { return store.size() ; }
    }
    
    public DatasetGraphSimpleMem() {}


    @Override
    public Iterator<Quad> findInDftGraph(Node s, Node p , Node o) 
    {
        List<Quad> results = new ArrayList<>() ;
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
        List<Quad> results = new ArrayList<>() ;
        for ( Quad q : quads )
            if ( matches(q, g, s, p, o) )
                results.add(q) ;
        return results.iterator() ;
    }
    
    @Override
    public Iterator<Quad> findInAnyNamedGraphs(Node s, Node p , Node o)
    {
        List<Quad> results = new ArrayList<>() ;
        for ( Quad q : quads )
            if ( matches(q, Node.ANY, s, p, o) )
                results.add(q) ;
        return results.iterator() ;
    }

    /** Convert null to Node.ANY */
    public static Node nullAsAny(Node x) { return nullAsDft(x, Node.ANY) ; }
    
    /** Convert null to some default Node */
    public static Node nullAsDft(Node x, Node dft) { return x==null ? dft : x ; }

    private boolean matches(Triple t, Node s, Node p, Node o)
    {
        s = nullAsAny(s) ;
        p = nullAsAny(p) ;
        o = nullAsAny(o) ;
        return t.matches(s,p,o) ;
    }

    private boolean matches(Quad q, Node g, Node s, Node p, Node o)
    {
        g = nullAsAny(g) ;
        s = nullAsAny(s) ;
        p = nullAsAny(p) ;
        o = nullAsAny(o) ;
        return q.matches(g,s,p,o) ;
    }

    @Override
    protected void addToDftGraph(Node s, Node p, Node o)
    {
        Triple t = new Triple(s, p, o) ;
        triples.add(t) ;
    }

    @Override
    protected void addToNamedGraph(Node g, Node s, Node p, Node o)
    {
        Quad q = new Quad(g, s, p, o) ;
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
            triples.add(t) ;
        }

        @Override
        public void performDelete(Triple t) { triples.remove(t) ; }

        @Override
        protected ExtendedIterator<Triple> graphBaseFind(TripleMatch m)
        {
            List<Triple> results = new ArrayList<>() ;
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
            quads.add(q) ;
        }

        @Override
        public void performDelete(Triple t) { Quad q = new Quad(graphName, t) ; quads.remove(q) ; }

        @Override
        protected ExtendedIterator<Triple> graphBaseFind(TripleMatch m)
        {
            List<Triple> results = new ArrayList<>() ;
            
            Iterator<Quad> iter = findNG(graphName, m.getMatchSubject(), m.getMatchPredicate(), m.getMatchObject()) ;
            for ( ; iter.hasNext() ; )
                results.add(iter.next().asTriple()) ;
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
        Set<Node> x = new HashSet<>() ;
        for ( Quad q : quads )
            x.add(q.getGraph()) ;
        return x ;
    }

    @Override
    protected void _close()
    {}
}
