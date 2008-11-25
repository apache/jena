/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.store;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Reifier;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.shared.AlreadyReifiedException;
import com.hp.hpl.jena.shared.CannotReifyException;
import com.hp.hpl.jena.shared.ReificationStyle;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.core.DataSourceGraphImpl;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.Plan;
import com.hp.hpl.jena.sparql.engine.QueryEngineFactory;
import com.hp.hpl.jena.sparql.engine.QueryEngineRegistry;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;
import com.hp.hpl.jena.sparql.engine.binding.BindingRoot;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.NiceIterator;
import com.hp.hpl.jena.vocabulary.RDF;

/** A Reifier that only support one style Standard (intercept, no conceal 
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
    
    private final Node rdfType      = RDF.Nodes.type ;
    private final Node statement    = RDF.Nodes.Statement ;
    private final Node subject      = RDF.Nodes.subject ;
    private final Node predicate    = RDF.Nodes.predicate ;
    private final Node object       = RDF.Nodes.object ;
    
    

    //private static ReificationStyle style = new ReificationStyle(false, false) ;
    private final Graph graph ;
    private final DatasetGraph ds  ;
    private final QueryEngineFactory factory ;

    public Reifier2(Graph graph)
    {
        this.graph = graph ;
        this.ds = new DataSourceGraphImpl(graph) ;
        this.factory = QueryEngineRegistry.findFactory(op, ds, null) ;
    }
    
    @Override
    public ExtendedIterator allNodes()
    {
        // Or use graph.find( Node.ANY, rdfType, statement ) -> project subject
        return allNodes(null) ;
    }

    private static class MapperToNode extends NiceIterator
    {
        private final QueryIterator iter ;
        private Var var ;
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
    public ExtendedIterator allNodes(Triple triple)
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
            b = new BindingMap(b) ;
            if ( node != null )
                bind(b, reifNodeVar, node) ; 
            if ( triple != null )
            {
                bind(b, varS, triple.getMatchSubject()) ;
                bind(b, varP, triple.getMatchPredicate()) ;
                bind(b, varO, triple.getMatchObject()) ;
            }
        }
        
        Plan plan = factory.create(op, ds, b, null) ;
        QueryIterator qIter = plan.iterator() ;
        return qIter ;
    }
    
    private static void bind(Binding b, Var var, Node node)
    {
        if ( node == null || node == Node.ANY )
            return ;
        b.add(var, node) ;
    }

    @Override
    public void close()
    {}

    private static class MapperToTriple extends NiceIterator
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
    public ExtendedIterator find(TripleMatch match)
    {
        // Filter non-reficiations?
        return graph.find(match) ; 
//        QueryIterator qIter = nodesReifTriple(null, match) ; 
//        // To ExtendedIterator.
//        return new MapperToTriple(qIter) ;
    }

    @Override
    public ExtendedIterator findEither(TripleMatch match, boolean showHidden)
    {
        return graph.find(match) ;
    }

    @Override
    public ExtendedIterator findExposed(TripleMatch match)
    {
        return graph.find(match) ;
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
//        if ( ! graph.contains(Node.ANY, subject, triple.getSubject()) )
//            return false ;
//        if ( ! graph.contains(Node.ANY, predicate, triple.getPredicate()) )
//            return false ;
//        if ( ! graph.contains(Node.ANY, object, triple.getObject()) )
//            return false ;
//        return true ;
        QueryIterator qIter = nodesReifTriple(null, triple) ;
        try {
            if ( ! qIter.hasNext() )
                return false ;
            Binding b = qIter.nextBinding() ;
            Node x = b.get(reifNodeVar) ;
            if ( qIter.hasNext() )
                // Over specified
                return false ;
            // This check there are no fragments
            return getTriple(x) != null ;
        } finally { qIter.close(); }
    }

    @Override
    public Node reifyAs(Node node, Triple triple)
    {
        // If there already was a node, it is replaced.  YUK.
        
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
        
        // CannotReifyException
        // addWithCheck that node/predicate does not already exist
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
        remove(null, triple) ;
    }

    @Override
    public void remove(Node node, Triple triple)
    {
        if ( node == null )
            node = Node.ANY ;
        
        QueryIterator qIter = nodesReifTriple(node, triple) ;
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
        ExtendedIterator iter = graph.find(s,p,o) ;
        while(iter.hasNext())
            acc.add((Triple)iter.next()) ;
    }
    
    @Override
    public int size()
    {
        // Will be counted by the graph.size() directly.
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
        ExtendedIterator it = graph.find(S,P, Node.ANY) ;
        if ( ! it.hasNext() ) return null ;
        Triple t = (Triple)it.next() ;
        it.close() ;
        return t.getObject() ;
    }
}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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