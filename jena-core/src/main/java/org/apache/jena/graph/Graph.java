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

package org.apache.jena.graph;

import java.util.Objects;
import java.util.stream.Stream;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.impl.GraphBase ;
import org.apache.jena.shared.AddDeniedException ;
import org.apache.jena.shared.DeleteDeniedException ;
import org.apache.jena.shared.PrefixMapping ;
import org.apache.jena.util.iterator.ExtendedIterator ;
import org.apache.jena.util.iterator.NullIterator ;

/**
    The interface to be satisfied by implementations maintaining collections
    of RDF triples. The core interface is small (add, delete, find, contains) and
    is augmented by additional classes to handle more complicated matters
    such as event management.
    @see GraphBase for an implementation framework.
*/
public interface Graph
    {
    /**
        An immutable empty graph.
    */
    public static final Graph emptyGraph = new GraphBase()
        { @Override
        public ExtendedIterator<Triple> graphBaseFind( Triple tm ) { return NullIterator.instance(); } };

    /**
        true if this graph's content depends on the other graph. May be
        pessimistic (ie return true if it's not sure). Typically true when a
        graph is a composition of other graphs, eg union.

         @param other the graph this graph may depend on
         @return false if this does not depend on other
    */
    boolean dependsOn( Graph other );

    /** returns this Graph's transaction handler */
    TransactionHandler getTransactionHandler();

    /** returns this Graph's capabilities */
    Capabilities getCapabilities();

    /**
        Answer this Graph's event manager.
    */
    GraphEventManager getEventManager();

    /**
        returns this Graph's prefix mapping. Each call on a given Graph gets the
        same PrefixMapping object, which is the one used by the Graph.
    */
    PrefixMapping getPrefixMapping();

    /**
        Add the triple t (if possible) to the set belonging to this graph
        @param t the triple to add to the graph
        @throws AddDeniedException if the triple cannot be added
     */
    void add( Triple t ) throws AddDeniedException;

    /**
     * Add the triple comprised of s,p,o to the set belonging to this graph
     *
     * @throws AddDeniedException if the triple cannot be added
     */
    default void add(Node s, Node p, Node o) throws AddDeniedException {
        Objects.requireNonNull(s, "Subject must not be null");
        Objects.requireNonNull(p, "Predicate must not be null");
        Objects.requireNonNull(o, "Object must not be null");
        add(Triple.create(s, p, o));
    }

    /**
        Delete the triple t (if possible) from the set belonging to this graph

        @param  t the triple to delete to the graph
        @throws DeleteDeniedException if the triple cannot be removed
    */
	void delete(Triple t) throws DeleteDeniedException;

    /**
     * Delete the triple comprised of s,p,o from the set belonging to this graph
     *
     * @throws AddDeniedException if the triple cannot be added
     */
    default void delete(Node s, Node p, Node o) throws DeleteDeniedException {
        Objects.requireNonNull(s, "Subject must not be null");
        Objects.requireNonNull(p, "Predicate must not be null");
        Objects.requireNonNull(o, "Object must not be null");
        delete(Triple.create(s, p, o));
    }

	/**
        Returns an iterator over all the Triples that match the triple pattern.

        @param m a Triple encoding the pattern to look for
        @return an iterator of all triples in this graph that match m
	 */
	ExtendedIterator<Triple> find(Triple m);

	/** Returns an iterator over Triples matching a pattern.
     *
     * @return an iterator of triples in this graph matching the pattern.
	 */
	ExtendedIterator<Triple> find(Node s, Node p, Node o);

	/** Returns a {@link Stream} of Triples matching a pattern.
	 *
	 * @return a stream  of triples in this graph matching the pattern.
	 */
	default Stream<Triple> stream(Node s, Node p, Node o) {
	    return Iter.asStream(find(s,p,o));
	}

	/** Returns a {@link Stream} of all triples in the graph.
	 *
	 * @return a stream  of triples in this graph.
	 */
	default Stream<Triple> stream() {
	    return stream(Node.ANY, Node.ANY, Node.ANY);
	}

	/** Returns an iterator over all Triples in the graph.
     * Equivalent to {@code find(Node.ANY, Node.ANY, Node.ANY)}
     *
     * @return an iterator of all triples in this graph
     */
    default ExtendedIterator<Triple> find() { return find(Node.ANY, Node.ANY, Node.ANY); }

    /**
	 * Compare this graph with another using the method
	 * described in
	 * <a href="http://www.w3.org/TR/rdf-concepts#section-Graph-syntax">
     * http://www.w3.org/TR/rdf-concepts#section-Graph-syntax
     * </a>
     *
     * Note: this implementation does not handle correctly blank nodes in
     * quoted triples (RDF-star). If you need to work with RDF-star,
     * use the slower implementation in
     * {@link org.apache.jena.sparql.util.IsoMatcher}.
     *
	 * @param g Compare against this.
	 * @return boolean True if the two graphs are isomorphic.
	 */
	boolean isIsomorphicWith(Graph g);

    /**
        Answer true iff the graph contains a triple matching (s, p, o).
        s/p/o may be concrete or fluid. Equivalent to find(s,p,o).hasNext,
        but an implementation is expected to optimise this in easy cases.
    */
    boolean contains( Node s, Node p, Node o );

    /**
        Answer true iff the graph contains a triple that t matches; t may be
        fluid.
    */
    boolean contains( Triple t );

    /**
        Remove all the statements from this graph.
    */
    void clear();

    /**
       Remove all triples that match by find(s, p, o)
    */
    void remove( Node s, Node p, Node o );

	/** Free all resources, any further use of this Graph is an error.
	 */
	void close();

    /**
        Answer true iff this graph is empty. "Empty" means "has as few triples as it
        can manage", because an inference graph may have irremovable axioms
        and their consequences.
    */
    boolean isEmpty();

    /**
     * For a concrete graph this returns the number of triples in the graph. For graphs which
     * might infer additional triples it results an estimated lower bound of the number of triples.
     * For example, an inference graph might return the number of triples in the raw data graph.
     */
	 int size();

    /**
        Answer true iff .close() has been called on this Graph.
    */
    boolean isClosed();
    }
