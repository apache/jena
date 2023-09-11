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

package org.apache.jena.sparql.core ;

import java.util.Iterator ;
import java.util.stream.Stream;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.Closeable ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.query.Dataset ;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.shared.Lock ;
import org.apache.jena.sparql.util.Context ;

/** DatasetGraph: The graph representation of an RDF Dataset. See {@link Dataset}
 * for the Model level of an RDF dataset.
 * <p>
 * Whether a dataset contains a graph if there are no triples
 * is not defined; see the specific implementation.
 */

public interface DatasetGraph extends Transactional, Closeable
{
    // ---- Graph container view

    /** Get the default graph as a Jena Graph */
    public Graph getDefaultGraph() ;

    /** Get the graph named by graphNode : returns null when there is no such graph.
     * NB Whether a dataset contains a graph if there are no triples is not defined - see the specific implementation.
     * Some datasets are "open" - they have all graphs even if no triples.
     */
    public Graph getGraph(Node graphNode) ;

    /**
     * Return a {@link Graph} that is the union of all named graphs in this dataset. This
     * union graph is read-only (its prefix mapping in the current JVM may be changed but
     * that may not persist).
     */
    public Graph getUnionGraph();

    /**
     * Does the DatasetGraph contain a specific named graph?
     * Whether a dataset contains a graph if there are no triples is
     * not defined - see the specific implementation. Some datasets are "open" -
     * they have all graphs even if no triples and this returns true always.
     *
     * @param graphNode
     * @return boolean
     */
    public boolean containsGraph(Node graphNode) ;

    /**
     * Add the given graph to the dataset.
     * <em>Replaces</em> any existing data for the named graph; to add data,
     * get the graph and add triples to it, or add quads to the dataset.
     * Do not assume that the same Java object is returned by {@link #getGraph}
     */
    public void addGraph(Node graphName, Graph graph) ;

    /** Remove all data associated with the named graph.
     * This will include prefixes associated with the graph.
     */
    public void removeGraph(Node graphName) ;

    /** Iterate over all names of named graphs */
    public Iterator<Node> listGraphNodes() ;

    // ---- Quad view

    /** Add a quad */
    public void add(Quad quad) ;

    /** Delete a quad */
    public void delete(Quad quad) ;

    /** Add a quad */
    public void add(Node g, Node s, Node p, Node o) ;

    /** Add the {@code src} DatasetGraph to this one. */
    public default void addAll(DatasetGraph src) {
        src.find().forEachRemaining(this::add);
    }

    /** Delete a quad */
    public void delete(Node g, Node s, Node p, Node o) ;

    /** Delete any quads matching the pattern */
    public void deleteAny(Node g, Node s, Node p, Node o) ;

    /** Iterate over all quads in the dataset graph */
    public default Iterator<Quad> find() {
        return find(Node.ANY, Node.ANY, Node.ANY, Node.ANY);
    }

    /** Find matching quads in the dataset - may include wildcards, Node.ANY or null
     * @see Graph#find(Triple)
     */
    public Iterator<Quad> find(Quad quad) ;

    /** Find matching quads in the dataset (including default graph) - may include wildcards, Node.ANY or null
     * @see Graph#find(Node,Node,Node)
     */
    public Iterator<Quad> find(Node g, Node s, Node p , Node o) ;

    /** Find matching quads in the dataset in named graphs only - may include wildcards, Node.ANY or null
     * @see Graph#find(Node,Node,Node)
     */
    public Iterator<Quad> findNG(Node g, Node s, Node p , Node o) ;

    /** Returns a {@link Stream} of {@link Quad Quads} matching a pattern.
     *
     * @return a stream of quads in this dataset matching the pattern.
     */
    public default Stream<Quad> stream(Node g, Node s, Node p, Node o) {
        return Iter.asStream(find(g, s, p, o));
    }

    /** Returns a {@link Stream} of {@link Quad Quads} in this dataset.
     *
     * @return a stream of quads in this dataset.
     */
    public default Stream<Quad> stream() {
        return stream(Node.ANY, Node.ANY, Node.ANY, Node.ANY);
    }

    /** Test whether the dataset (including default graph) contains a quad - may include wildcards, Node.ANY or null */
    public boolean contains(Node g, Node s, Node p , Node o) ;

    /** Test whether the dataset contains a quad (including default graph)- may include wildcards, Node.ANY or null */
    public boolean contains(Quad quad) ;

    /** Remove everything - remove all named graphs, clear the default graph */
    public void clear() ;

    /** Test whether the dataset is empty */
    public boolean isEmpty() ;

    /** Return a lock for the dataset to help with concurrency control
     * @see Lock
     */
    public Lock getLock() ;

    /** Get the context associated with this object - may be null */
    public Context getContext() ;

    /** Get the size (number of named graphs) - may be -1 for unknown */
    public long size() ;

    /** Close the dataset */
    @Override
    public void close() ;

    /** Prefixes for this DatasetGraph */
    public PrefixMap prefixes();

    /**
     * A {@code DatasetGraph} supports transactions if it provides {@link #begin}/
     * {@link #commit}/{@link #end}. The core storage {@code DatasetGraph}s
     * provide fully serialized transactions. A {@code DatasetGraph} that provides
     * functionality across independent systems can not provide such strong guarantees.
     * For example, it may use MRSW locking and some isolation control.
     * Specifically, it would not necessarily provide {@link #abort}.
     * <p>
     * See {@link #supportsTransactionAbort()} for {@link #abort}.
     * In addition, check details of a specific implementation.
     */
    public boolean supportsTransactions() ;

    /** Declare whether {@link #abort} is supported.
     *  This goes along with clearing up after exceptions inside application transaction code.
     */
    public default boolean supportsTransactionAbort() {
        return false;
    }
}
