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

package com.hp.hpl.jena.sparql.graph;


import java.util.Collection ;
import java.util.HashSet ;
import java.util.Iterator ;
import java.util.Set ;

import org.apache.jena.atlas.iterator.Filter ;
import org.apache.jena.atlas.iterator.Iter ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Reifier ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.graph.TripleMatch ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.shared.AlreadyReifiedException ;
import com.hp.hpl.jena.shared.CannotReifyException ;
import com.hp.hpl.jena.shared.ReificationStyle ;
import com.hp.hpl.jena.sparql.algebra.Algebra ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.Plan ;
import com.hp.hpl.jena.sparql.engine.QueryEngineFactory ;
import com.hp.hpl.jena.sparql.engine.QueryEngineRegistry ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory ;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap ;
import com.hp.hpl.jena.sparql.engine.binding.BindingRoot ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;
import com.hp.hpl.jena.util.iterator.NiceIterator ;
import com.hp.hpl.jena.util.iterator.NullIterator ;
import com.hp.hpl.jena.util.iterator.WrappedIterator ;
import com.hp.hpl.jena.vocabulary.RDF ;

/** A Reifier that only supports one style Standard (intercept, no conceal 
 *  -- and intercept is a no-op anyway because all triples 
 *  appear in the underlying graph for storing all triples). 
 */

public class Reifier2 implements Reifier
{
    private final static String qs = "PREFIX rdf: <"+RDF.getURI()+">\n" +
    		"SELECT * \n" +
    		"{ ?x rdf:type rdf:Statement ; rdf:subject ?S ; rdf:predicate ?P ; rdf:object ?O }" ;
    private final static Query query = QueryFactory.create(qs) ;
    private final static Op op = Algebra.compile(query) ; 
    private final static Var reifNodeVar = Var.alloc("x") ; 
    private final static Var varS = Var.alloc("S") ; 
    private final static Var varP = Var.alloc("P") ; 
    private final static Var varO = Var.alloc("O") ; 
    
    private final static Node rdfType      = RDF.Nodes.type ;
    private final static Node statement    = RDF.Nodes.Statement ;
    private final static Node subject      = RDF.Nodes.subject ;
    private final static Node predicate    = RDF.Nodes.predicate ;
    private final static Node object       = RDF.Nodes.object ;

    private final Graph graph ;
    private final DatasetGraph ds  ;
    private final QueryEngineFactory factory ;

    public Reifier2(Graph graph)
    {
        this.graph = graph ;
        this.ds = DatasetGraphFactory.createOneGraph(graph) ;
        this.factory = QueryEngineRegistry.findFactory(op, ds, null) ;
    }
    
    @Override
    public ExtendedIterator<Node> allNodes()
    {
        return allNodes(null) ;
    }

    private static class MapperToNode extends NiceIterator<Node>
    {
        private final QueryIterator iter ;
        private final Var var ;
        MapperToNode(QueryIterator iter, Var var) { this.iter = iter ; this.var = var ; }
        @Override public boolean hasNext() { return iter.hasNext() ; } 
        @Override public Node next()
        { 
            Binding b = iter.nextBinding();
            Node n = b.get(var) ;
            return n ;
        }
        @Override public void close() { iter.close() ; } 
    }

    @Override
    public ExtendedIterator<Node> allNodes(Triple triple)
    {
        QueryIterator qIter = nodesReifTriple(null, triple) ;
        return new MapperToNode(qIter, reifNodeVar) ;
    }
    
    private QueryIterator nodesReifTriple(Node node, TripleMatch triple)
    {
        Binding b = BindingRoot.create() ;
        
        if ( node == Node.ANY )
            node = null ;
        
        if ( node != null || triple != null )
        {
            BindingMap b2 = BindingFactory.create(b) ;
            if ( node != null )
                bind(b2, reifNodeVar, node) ; 
            if ( triple != null )
            {
                bind(b2, varS, triple.getMatchSubject()) ;
                bind(b2, varP, triple.getMatchPredicate()) ;
                bind(b2, varO, triple.getMatchObject()) ;
            }
            b = b2 ;
        }
        
        Plan plan = factory.create(op, ds, b, null) ;
        QueryIterator qIter = plan.iterator() ;
        return qIter ;
    }
    
    private static void bind(BindingMap b, Var var, Node node)
    {
        if ( node == null || node == Node.ANY )
            return ;
        b.add(var, node) ;
    }

    @Override
    public void close()
    {}

    private static class MapperToTriple extends NiceIterator<Triple>
    {
        private final QueryIterator iter ;
        MapperToTriple(QueryIterator iter) { this.iter = iter  ; }
        @Override public boolean hasNext() { return iter.hasNext() ; } 
        @Override public Triple next()
        { 
            Binding b = iter.nextBinding();
            Node S = b.get(varS) ;
            Node P = b.get(varP) ;
            Node O = b.get(varO) ;
            return new Triple(S,P,O) ;
        }
        @Override public void close() { iter.close() ; } 
    }
    
    @Override
    public ExtendedIterator<Triple> find(TripleMatch match)
    {
        return graph.find(match) ; 
//        QueryIterator qIter = nodesReifTriple(null, match) ; 
//        // To ExtendedIterator.
//        return new MapperToTriple(qIter) ;
    }

    @Override
    public ExtendedIterator<Triple> findEither(TripleMatch match, boolean showHidden)
    {
        if ( showHidden )
            return NullIterator.instance() ;
        else
            return graph.find(match) ;
    }

    
    static Filter<Triple> filterReif = new Filter<Triple>() {
        @Override
        public boolean accept(Triple triple)
        {
            return triple.getPredicate().equals(subject) ||
                   triple.getPredicate().equals(predicate) ||
                   triple.getPredicate().equals(object) ||
                   ( triple.getPredicate().equals(rdfType) && triple.getObject().equals(statement) ) ;
        }} ; 

    @Override
    public ExtendedIterator<Triple> findExposed(TripleMatch match)
    {
        Iterator<Triple> it = graph.find(match) ;
        it = Iter.filter(it, filterReif) ;
        return WrappedIterator.create(it) ;
    }

    @Override
    public Graph getParentGraph()
    {
        return graph ;
    }

    @Override
    public ReificationStyle getStyle()
    {
        return ReificationStyle.Standard ;
    }

    @Override
    public boolean handledAdd(Triple triple)
    {
        graph.add(triple) ;
        return true ;
    }

    @Override
    public boolean handledRemove(Triple triple)
    {
        graph.delete(triple) ;
        return true ;
    }

    @Override
    public boolean hasTriple(Node node)
    {
        return getTriple(node) != null ;
    }

    @Override
    public boolean hasTriple(Triple triple)
    {
        QueryIterator qIter = nodesReifTriple(null, triple) ;
        try {
            if ( ! qIter.hasNext() )
                return false ;
            Binding b = qIter.nextBinding() ;
            Node x = b.get(reifNodeVar) ;
            if ( qIter.hasNext() )
                // Over specified
                return false ;
            // This checks there are no fragments
            return getTriple(x) != null ;
        } finally { qIter.close(); }
    }

    @Override
    public Node reifyAs(Node node, Triple triple)
    {
        if ( node == null )
            node = Node.createAnon() ;
        else
        {
            Triple t = getTriple(node) ; 
            
            if ( t != null && ! t.equals(triple) )
                throw new AlreadyReifiedException(node) ;
            if ( t != null )
                // Already there
                return node ;
        }
        
        graph.add(new Triple(node, rdfType, statement)) ;
        graph.add(new Triple(node, subject, triple.getSubject())) ;
        graph.add(new Triple(node, predicate, triple.getPredicate())) ;
        graph.add(new Triple(node, object, triple.getObject())) ;

        // Check it's a well-formed reification by Jena's uniqueness rules 
        Triple t = getTriple(node) ;
        if ( t == null )
            throw new CannotReifyException(node) ;
        return node ;
    }

    @Override
    public void remove(Triple triple)
    {
        // Materialize the nodes to delete - avoid ConcurrentModificationException.
        for ( Node n : Iter.toList(allNodes(triple)) )
            remove(n, triple) ;
    }

    @Override
    public void remove(Node node, Triple triple)
    {
        //QueryIterator qIter = nodesReifTriple(node, triple) ;
        Set<Triple> triples = new HashSet<Triple>();
        triplesToZap(triples, node, rdfType, statement) ;
        triplesToZap(triples, node, subject, triple.getSubject()) ;
        triplesToZap(triples, node, predicate, triple.getPredicate()) ;
        triplesToZap(triples, node, object, triple.getObject()) ;
        for ( Triple t : triples )
            graph.delete(t) ;
    }

    private void triplesToZap(Collection<Triple> acc, Node s, Node p , Node o)
    {
        ExtendedIterator<Triple> iter = graph.find(s,p,o) ;
        while(iter.hasNext())
            acc.add(iter.next()) ;
    }
    
    @Override
    public int size()
    {
        return 0 ;
    }

    @Override
    public Triple getTriple(Node node)
    {
        
        QueryIterator qIter = nodesReifTriple(node, null) ;
        try {
            if ( ! qIter.hasNext() )
                return null ;
            Binding b = qIter.nextBinding() ;
            if ( qIter.hasNext() )
                // Over specificied
                return null ;
            // Just right
            Node S = b.get(varS) ;
            Node P = b.get(varP) ;
            Node O = b.get(varO) ;
            return new Triple(S,P,O) ;
        } finally { qIter.close() ; }
    }

    private Node getNode(Node S, Node P)
    {
        ExtendedIterator<Triple> it = graph.find(S,P, Node.ANY) ;
        if ( ! it.hasNext() ) return null ;
        Triple t = it.next() ;
        it.close() ;
        return t.getObject() ;
    }
}
