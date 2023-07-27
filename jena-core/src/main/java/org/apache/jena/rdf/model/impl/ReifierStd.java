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

package org.apache.jena.rdf.model.impl;


import java.util.* ;
import java.util.function.Predicate;

import org.apache.jena.graph.* ;
import org.apache.jena.shared.AlreadyReifiedException ;
import org.apache.jena.shared.CannotReifyException ;
import org.apache.jena.util.iterator.ExtendedIterator ;
import org.apache.jena.util.iterator.NullIterator ;
import org.apache.jena.util.iterator.WrappedIterator ;
import org.apache.jena.vocabulary.RDF ;

/**
 * Library support for reification.
 */

public class ReifierStd
{
    private ReifierStd() {}

    private final static Node rdfType      = RDF.Nodes.type ;
    private final static Node statement    = RDF.Nodes.Statement ;
    private final static Node subject      = RDF.Nodes.subject ;
    private final static Node predicate    = RDF.Nodes.predicate ;
    private final static Node object       = RDF.Nodes.object ;

    /**
     * Answer an iterator over the reification triples of the graph, or an empty
     * iterator. If showHidden is false, only the exposed triples, otherwise only the
     * concealed ones.
     */
    public static ExtendedIterator<Triple> findEither(Graph graph, Triple match, boolean showHidden) {
        if ( showHidden )
            return NullIterator.instance() ;
        else
            return graph.find(match) ;
    }

    static Predicate<Triple> filterReif = triple -> triple.getPredicate().equals(subject) ||
                   triple.getPredicate().equals(predicate) ||
                   triple.getPredicate().equals(object) ||
                   ( triple.getPredicate().equals(rdfType) && triple.getObject().equals(statement) ) ;

    /**
     * Answer an iterator over all the reification triples that match.
     */
    public static ExtendedIterator<Triple> findExposed(Graph graph, Triple match) {
        ExtendedIterator<Triple> it = graph.find(match);
        it = it.filterKeep(filterReif);
        return WrappedIterator.create(it);
    }

    /**
     * Answer the triple associated with the node <code>n</code>.
     *
     * @param n
     *            the node to use as the key
     * @return the associated triple, or <code>null</code> if none
     */

    public static Triple getTriple(Graph graph, Node n) {
        // Must have rdf:type rdf:Statement
        if ( ! graph.contains(n, rdfType, statement) )
            return null ;
        Node s = getObject(graph, n, subject) ;
        if ( s == null ) return null ;
        Node p = getObject(graph, n, predicate) ;
        if ( p == null ) return null ;
        Node o = getObject(graph, n, object) ;
        if ( o == null ) return null ;
        return Triple.create(s,p,o) ;
    }

    // Get one and only one object
    private static Node getObject(Graph graph, Node n, Node predicate) {
        ExtendedIterator<Triple> iter = graph.find(n, predicate, Node.ANY);
        try {
            if ( !iter.hasNext() )
                // None.
                return null;
            Triple t = iter.next();
            if ( iter.hasNext() )
                // Too many.
                return null;
            return t.getObject();
        }
        finally {
            iter.close();
        }
    }

    /**
     * @return true iff there are {@literal >} 0 reification quads for the triple.
     */
    public static boolean hasTriple(Graph graph, Triple t) {
        ExtendedIterator<Node> iter = findNodesForTriple(graph, t, false);
        try {
            return iter.hasNext();
        }
        finally {
            iter.close();
        }
    }

    /**
     * true iff _n_ is associated with some triple rfication
     */
    public static boolean hasTriple(Graph graph, Node node) {
        return getTriple(graph, node) != null;
    }

    /**
     * return an iterator over all the nodes that are reifiying something in the
     * graph
     */
    public static ExtendedIterator<Node> allNodes(Graph graph) {
        return allNodes(graph, null);
    }

    /**
     * return an iterator over all the nodes that are reifiying t in the graph
     */
    public static ExtendedIterator<Node> allNodes(Graph graph, Triple t) {
        return findNodesForTriple(graph, t, false);
    }

    private static ExtendedIterator<Node> findNodesForTriple(Graph graph, Triple t, boolean oneWillDo) {
        ExtendedIterator<Triple> iter = graph.find(Node.ANY, rdfType, statement) ;
        List<Node> nodes = new ArrayList<>() ;
        try {
            while (iter.hasNext()) {
                Triple typeTriple = iter.next();
                Node n = typeTriple.getSubject();

                // Check.
                if ( t != null ) {
                    if ( !exactlyOne(graph, n, subject, t.getSubject()) )
                        continue;
                    if ( !exactlyOne(graph, n, predicate, t.getPredicate()) )
                        continue;
                    if ( !exactlyOne(graph, n, object, t.getObject()) )
                        continue;
                }
                nodes.add(n);
                if ( oneWillDo )
                    break;
            }
        } finally { iter.close() ; }
        return WrappedIterator.createNoRemove(nodes.iterator()) ;
    }

    // check whether there is exactly the triple expected, and no others with same S and P but different O.
    private static boolean exactlyOne(Graph graph, Node n, Node predicate, Node object)
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

    /**
     * note the triple _t_ as reified using _n_ as its representing node. If _n_
     * is already reifying something, a AlreadyReifiedException is thrown.
     */
    public static Node reifyAs(Graph graph, Node node, Triple triple) {
        if ( node == null )
            node = NodeFactory.createBlankNode() ;
        else {
            Triple t = getTriple(graph, node) ;

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

        graph.add(Triple.create(node, rdfType, statement)) ;
        graph.add(Triple.create(node, subject, triple.getSubject())) ;
        graph.add(Triple.create(node, predicate, triple.getPredicate())) ;
        graph.add(Triple.create(node, object, triple.getObject())) ;

        return node ;
    }

    /**
     * remove all refications which map to this triple.
     */
    public static void remove(Graph graph, Triple triple) {
        // Materialize the nodes to delete - avoid ConcurrentModificationException.
        for ( Node n : allNodes(graph, triple).toList() )
            remove(graph, n, triple);
    }

    /**
     * remove any existing binding for _n_; hasNode(n) will return false and
     * getTriple(n) will return null. This only removes *unique, single* bindings.
     */
    public static void remove(Graph graph, Node node, Triple triple) {
        Set<Triple> triples = new HashSet<>();
        triplesToZap(graph, triples, node, rdfType, statement);
        triplesToZap(graph, triples, node, subject, triple.getSubject());
        triplesToZap(graph, triples, node, predicate, triple.getPredicate());
        triplesToZap(graph, triples, node, object, triple.getObject());
        for ( Triple t : triples )
            graph.delete(t);
    }

    private static void triplesToZap(Graph graph, Collection<Triple> acc, Node s, Node p, Node o) {
        ExtendedIterator<Triple> iter = graph.find(s, p, o);
        while (iter.hasNext())
            acc.add(iter.next());
    }
}
