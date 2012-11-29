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


import java.util.* ;

import org.apache.jena.atlas.iterator.Filter ;
import org.apache.jena.atlas.iterator.Iter ;

import com.hp.hpl.jena.graph.* ;
import com.hp.hpl.jena.shared.AlreadyReifiedException ;
import com.hp.hpl.jena.shared.CannotReifyException ;
import com.hp.hpl.jena.shared.ReificationStyle ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;
import com.hp.hpl.jena.util.iterator.NullIterator ;
import com.hp.hpl.jena.util.iterator.WrappedIterator ;
import com.hp.hpl.jena.vocabulary.RDF ;

/** A Reifier that only supports one style Standard (intercept, no conceal 
 *  -- and intercept is a no-op anyway because all triples 
 *  appear in the underlying graph for storing all triples). 
 */

public class Reifier2 implements Reifier
{
    private final static Node rdfType      = RDF.Nodes.type ;
    private final static Node statement    = RDF.Nodes.Statement ;
    private final static Node subject      = RDF.Nodes.subject ;
    private final static Node predicate    = RDF.Nodes.predicate ;
    private final static Node object       = RDF.Nodes.object ;

    private final Graph graph ;

    public Reifier2(Graph graph)
    {
        this.graph = graph ;
    }
    
    @Override
    public ExtendedIterator<Node> allNodes()
    {
        return allNodes(null) ;
    }

    @Override
    public void close()
    {}

    @Override
    public ExtendedIterator<Triple> find(TripleMatch match)
    {
        return graph.find(match) ; 
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
    public Triple getTriple(Node n)
    {
        Node s = getObject(n, subject) ;
        if ( s == null ) return null ;
        Node p = getObject(n, predicate) ;
        if ( p == null ) return null ;
        Node o = getObject(n, object) ;
        if ( o == null ) return null ;
        return new Triple(s,p,o) ;
    }
    
    private Node getObject(Node n, Node predicate)
    {
        ExtendedIterator<Triple> iter = graph.find(n, predicate, Node.ANY) ;
        try {
            if ( ! iter.hasNext() )
                // None.
                return null ;
            Triple t = iter.next() ;
            if ( iter.hasNext() )
                // Too many.
                return null ;
            return t.getObject() ;
        } finally { iter.close() ; }
    }

    @Override
    public boolean hasTriple(Triple t)
    {
        ExtendedIterator<Node> iter = findNodesForTriple(t, false) ;
        try {
            return iter.hasNext() ;
        } finally { iter.close() ; }
    }

    @Override
    public boolean hasTriple(Node node)
    {
        return getTriple(node) != null ;
    }

    @Override
    public ExtendedIterator<Node> allNodes(Triple t)
    {
        return findNodesForTriple(t, false) ;
    }
    
    private ExtendedIterator<Node> findNodesForTriple(Triple t, boolean oneWillDo)
    {
        ExtendedIterator<Triple> iter = graph.find(Node.ANY, rdfType, statement) ;
        List<Node> nodes = new ArrayList<Node>() ;
        try
        {
            while (iter.hasNext())
            {
                Triple typeTriple = iter.next() ;
                Node n = typeTriple.getSubject() ;
                
                // Check.
                if ( t != null )
                {
                    if ( ! exactlyOne(n, subject, t.getSubject()) )
                        continue ;
                    if ( ! exactlyOne(n, predicate, t.getPredicate()) )
                        continue ;
                    if ( ! exactlyOne(n, object, t.getObject()) )
                        continue ;
                }
                nodes.add(n) ;
                if ( oneWillDo )
                    break ;
            }
        } finally { iter.close() ; }
        return WrappedIterator.create(nodes.iterator()) ;
    }
    // ----

    // check whether there is exactly the triple expected, and no others with same S and P but different O. 
    private boolean exactlyOne(Node n, Node predicate, Node object)
    {
        ExtendedIterator<Triple> iter = graph.find(n, predicate, Node.ANY) ;
        try {
            if ( ! iter.hasNext() )
                return false ;
            
            while (iter.hasNext())
            {
                Node obj = iter.next().getObject() ;
                if  ( ! obj.equals(object) )
                    return false ;
            }
            return true ;
        } finally { iter.close() ; }
    }

    private static Iterator<Triple> generate(ExtendedIterator<Triple> iterSubj, 
                                             ExtendedIterator<Triple> iterPred,
                                             ExtendedIterator<Triple> iterObj)
    {
        try {
            return null ;
        } finally {
            iterSubj.close() ;
            iterPred.close() ;
            iterObj.close() ;
        }
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

            // Check it's a well-formed reification by Jena's uniqueness rules
            // No fragments (we checked for exact match by getTriple(node))
            if ( graph.contains(node, subject, Node.ANY) )
                throw new CannotReifyException(node) ;
            if ( graph.contains(node, predicate, Node.ANY) )
                throw new CannotReifyException(node) ;
            if ( graph.contains(node, object, Node.ANY) )
                throw new CannotReifyException(node) ;
        }
        
        graph.add(new Triple(node, rdfType, statement)) ;
        graph.add(new Triple(node, subject, triple.getSubject())) ;
        graph.add(new Triple(node, predicate, triple.getPredicate())) ;
        graph.add(new Triple(node, object, triple.getObject())) ;

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
}
