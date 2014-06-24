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

// Package
///////////////
package com.hp.hpl.jena.util;

import java.util.*;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.reasoner.InfGraph;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * <p>
 * General utility methods that operate on RDF resources, but which are not specific
 * to a given model.
 * </p>
 */
public class ResourceUtils {

    /**
     * The size of the temporary list that hold triples to be added or deleted in bulk.
     */
    private static final int WINDOW_SIZE = 1000 ;
    
    /**
     * <p>
     * Answer the maximal lower elements of the given collection, given the partial
     * ordering <code>rel</code>. See {@link #maximalLowerElements( Iterator, Property, boolean )}
     * for details.
     * </p>
     *
     * @param resources A collection of resources
     * @param rel A property defining a partial-ordering on <code>resources</code>
     * @param inverse If true, we invert the given property (by reversing the order
     * of the arguments), which allows us to use eg subClassOf as a partial order
     * operator for both sub-class and super-class relationships
     * @return The collection that contains only those <code>resources</code> are not
     * greater than another resource under the partial order.
     */
    public static <T extends Resource> List<T> maximalLowerElements( Collection<T> resources, Property rel, boolean inverse ) {
        return maximalLowerElements( resources.iterator(), rel, inverse );
    }

    /**
     * <p>
     * Given a collection of resources, and a relation defining a partial order over
     * those resources, answer the sub-collection that contains only those elements
     * that appear in the maximal generator of the relation.  Specifically, a resource
     * <code>x</code> is excluded from the return value if there is another resource
     * <code>y</code> in the input collection such that <code>y&nbsp;Rel&nbsp;x</code> holds.
     * </p>
     *
     * @param resources An iterator over a collection of resources
     * @param rel A property defining a partial-ordering on <code>resources</code>
     * @param inverse If true, we invert the given property (by reversing the order
     * of the arguments), which allows us to use eg subClassOf as a partial order
     * operator for both sub-class and super-class relationships
     * @return The list that contains only those <code>resources</code> are not
     * greater than another resource under the partial order.
     */
    public static <T extends Resource> List<T> maximalLowerElements( Iterator<T> resources, Property rel, boolean inverse ) {
        List<T> in = new ArrayList<>();
        List<T> out = new ArrayList<>();
        List<T> drop = new ArrayList<>();

        while (resources.hasNext()) {
            in.add( resources.next() );
        }

        while (! in.isEmpty()) {
            T r = in.remove( 0 );
            boolean rCovered = testResourceCovered( in, rel, inverse, r ) ||
                               testResourceCovered( out, rel, inverse, r ) ||
                               testResourceCovered( drop, rel, inverse, r );

            // if r is not covered by another resource, we can add it to the output
            (rCovered ? drop : out).add( r );
        }

        return out;
    }

    private static boolean testResourceCovered( List< ? extends Resource> l, Property rel, boolean inverse, Resource r ) {
        boolean rCovered = false;
        for (Iterator< ? extends Resource> i = l.iterator();  !rCovered && i.hasNext(); ) {
            Resource next = i.next();
            rCovered = inverse ? r.hasProperty( rel, next ) : next.hasProperty( rel, r );
        }
        return rCovered;
    }


    /**
     * <p>Remove from the given list l of {@link Resource Resources}, any Resource that is equivalent
     * to the reference resource <code>ref</code> under the relation <code>p</code>. Typically,
     * <code>p</code> will be <code>owl:subClassOf</code> or <code>owl:subPropertyOf</code>
     * or some similar predicate.  A resource R is defined to be equivalent to <code>ref</code>
     * iff <code>R&nbsp;p&nbsp;ref</code> is true <em>and</em> <code>ref&nbsp;p&nbsp;R</code> is true.
     * </p>
     * <p>The equivalent resources are removed from list <code>l</code>
     * </em>in place</em>, the return value is the list of <em>removed</em> resources.</p>
     * @param l A list of resources from which the resources equivalent to ref will be removed
     * @param p An equivalence predicate
     * @param ref A reference resource
     * @return A list of the resources removed from the parameter list l
     */
    public static <T extends Resource> List<T> removeEquiv( List<T> l, Property p, Resource ref ) {
        List<T> equiv = new ArrayList<>();

        for ( T r : l )
        {
            if ( r.hasProperty( p, ref ) && ref.hasProperty( p, r ) )
            {
                // resource r is equivalent to the reference resource
                equiv.add( r );
            }
        }

        l.removeAll( equiv );
        return equiv;
    }


    /**
     * <p>Answer a list of lists, which is a partition of the given
     * input list of resources.  The equivalence relation is the predicate p.
     * So, two resources <code>a</code> and <code>b</code>
     * will be in the same partition iff
     * <code>(a p b) && (b p a)</code>.</p>
     * @param <T>
     * @param l A list of resources
     * @param p An equivalence predicate
     * @return A list of lists which are the partitions of <code>l</code>
     * under <code>p</code>
     */
    public static <T extends Resource> List<List<T>> partition( List<T> l, Property p ) {
        // first copy the input so we can mess with it
        List<T> source = new ArrayList<>();
        source.addAll( l );
        List<List<T>> parts = new ArrayList<>();

        while (!source.isEmpty()) {
            // each step through the loop we pick a random element, and
            // create a list of that element and all its equivalent values
            T seed = source.remove( 0 );
            List<T> part = removeEquiv( source, p, seed );
            part.add( seed );

            // add to the partition list
            parts.add( part );
        }

        return parts;
    }


    /**
     * <p>Answer a new resource that occupies the same position in the graph as the current
     * resource <code>old</code>, but that has the given URI.  In the process, the existing
     * statements referring to <code>old</code> are removed.  Since Jena does not allow the
     * identity of a resource to change, this is the closest approximation to a rename operation
     * that works.
     * </p>
     * <p>Renaming a resource to its own URI is a no-op. Resources in the
     * predicate position of statements are not renamed. Intermediate store
     * for the triples mentioning <code>old</code> is required.
     * </p>
     * 
     * <p><b>Note</b>This implementation is a general and simple approach, and
     * in given applications it may be possible to do this operation more efficiently. 
     * </p>
     * @param old An existing resource in a given model
     * @param uri A new URI for resource old, or <code>null</code> to rename old to a bNode
     * @return A new resource that occupies the same position in the graph as old, but which
     * has the new given URI.
     */
    public static Resource renameResource(final Resource old, final String uri) {
        // Work at the graph level. Also, work underneath one layer of inference
        // if it's there. This avoids both fighting the inference engine and the
        // Statement reconstruction work of the Model layer.
        String oldURI = old.getURI() ;
        if ( oldURI != null && oldURI.equals(uri) )
        {
            return old ;
        }
        Node resAsNode = old.asNode() ;
        Model model = old.getModel() ;
        Graph graph = model.getGraph() ;
        Graph rawGraph = graph instanceof InfGraph ? ((InfGraph) graph).getRawGraph() : graph ;
        Resource newRes = model.createResource(uri) ;
        Node newResAsNode = newRes.asNode() ;
       	
        
        boolean changeOccured = false ;
        List<Triple> triples = new ArrayList<>(WINDOW_SIZE) ;
        
        // An optimization to prevent concatenating the two find() operations together every time through the outer loop
        boolean onFirstIterator = true;

        // It's possible there are triples (old wossname old) that are in triples twice. It doesn't matter.
        ExtendedIterator<Triple> it = rawGraph.find(resAsNode, Node.ANY, Node.ANY) ;
        try
        {
            if ( !it.hasNext() )
            {
                it.close() ;
                onFirstIterator = false ;
                it = rawGraph.find(Node.ANY, Node.ANY, resAsNode) ;
            }
            changeOccured = it.hasNext() ;

            while ( it.hasNext() )
            {
                int count = 0 ;
                while ( it.hasNext() && count < WINDOW_SIZE )
                {
                    triples.add(it.next()) ;
                    count++ ;
                }

                it.close() ;
                
                // Iterate over the triples collection twice (this may be more efficient than interleaving deletes and adds)
                for ( Triple t : triples )
                {
                    rawGraph.delete(t) ;
                }
                
                for ( Triple t : triples )
                {
                    Node oldS = t.getSubject(), oldO = t.getObject() ;
                    Node newS = oldS.equals(resAsNode) ? newResAsNode : oldS ;
                    Node newO = oldO.equals(resAsNode) ? newResAsNode : oldO ;
                    
                    rawGraph.add(Triple.create(newS, t.getPredicate(), newO));
                }
                triples.clear();

                it = onFirstIterator ? rawGraph.find(resAsNode, Node.ANY, Node.ANY) : rawGraph.find(Node.ANY, Node.ANY, resAsNode) ;
                if ( onFirstIterator && !it.hasNext() )
                {
                    it.close() ;
                    onFirstIterator = false ;
                    it = rawGraph.find(Node.ANY, Node.ANY, resAsNode) ;
                }
            }
        }
        finally
        {
            it.close() ;
        }
       	
        // If we were underneath an InfGraph, and at least one triple changed, then we have to rebind.
        if ( rawGraph != graph && changeOccured )
        {
            ((InfGraph) graph).rebind() ;
        }
        return newRes ;
    }


    /**
     * <p>Answer a model that contains all of the resources reachable from a given
     * resource by any property, transitively.  The returned graph is the sub-graph
     * of the parent graph of root, whose root node is the given root. Cycles are
     * permitted in the sub-graph.</p>
     * @param root The root node of the sub-graph to extract
     * @return A model containing all reachable RDFNodes from root by any property.
     */
    public static Model reachableClosure( Resource root ) {
        Model m = ModelFactory.createDefaultModel();

        // set of resources we have passed through already (i.e. the occurs check)
        Set<Resource> seen = CollectionFactory.createHashedSet();

        // queue of resources we have not yet visited
        List<RDFNode> queue = new LinkedList<>();
        queue.add( root );

        while (!queue.isEmpty()) {
            Resource r = (Resource) queue.remove( 0 );

            // check for multiple paths arriving at this queue node
            if (!seen.contains( r )) {
                seen.add( r );

                // add the statements to the output model, and queue any new resources
                for (StmtIterator i = r.listProperties(); i.hasNext(); ) {
                    Statement s = i.nextStatement();

                    // don't do the occurs check now in case of reflexive statements
                    m.add( s );

                    if (s.getObject() instanceof Resource) {
                        queue.add( s.getObject() );
                    }
                }
            }
        }

        return m;
    }


    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

}
