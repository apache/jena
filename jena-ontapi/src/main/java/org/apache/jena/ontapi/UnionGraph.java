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

package org.apache.jena.ontapi;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphEventManager;
import org.apache.jena.graph.GraphListener;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.shared.PrefixMapping;

import java.util.stream.Stream;

/**
 * Hierarchical graph.
 * <p>
 * It consists of two parts:
 * a {@link #getBaseGraph() base graph} and an {@link #subGraphs() sub-graphs} collection.
 * A hierarchical UnionGraph may have parents, called super-graphs ({@link #superGraphs()}.
 * Underlying sub-graphs are only used for searching; modify operations are performed only on the base graph.
 * This graph allows building graph hierarchy which can be used to link different models.
 * Also, it allows recursion, that is, it can contain itself somewhere in the hierarchy.
 * The {@link PrefixMapping} of this graph is taken from the base graph,
 * and, therefore, any changes in it reflect both the base and this graph.
 * <p>
 * Well-formed OWL ontology {@code UnionGraph} is expected to contain {@link UnionGraph}s as subgraphs,
 * where graphs also connected by {@code owl:imports} relationships.
 */
@SuppressWarnings({"WeakerAccess"})
public interface UnionGraph extends Graph {

    @Override
    EventManager getEventManager();

    /**
     * Answers {@code true} iff this graph is distinct,
     * which means that the method {@link Graph#find(Triple)} does not produce duplicates.
     *
     * @return boolean
     */
    boolean isDistinct();

    /**
     * Returns the base (primary) data graph.
     *
     * @return {@link Graph}, not {@code null}
     */
    Graph getBaseGraph();

    /**
     * Answers {@code true} iff this {@code UnionGraph} has sub-graphs.
     *
     * @return boolean
     */
    boolean hasSubGraph();

    /**
     * Lists all sub-graphs.
     * The {@link #getBaseGraph() base graph} is not included in the result.
     *
     * @return {@link Stream} of sub-{@link Graph graph}s
     */
    Stream<Graph> subGraphs();

    /**
     * Lists all parent graphs.
     * The {@link #getBaseGraph() base graph} is not included in the result.
     *
     * @return {@link Stream} of sub-{@link UnionGraph graph}s
     */
    Stream<UnionGraph> superGraphs();

    /**
     * Adds the specified graph to the underlying graph collection.
     * If the specified graph is {@link UnionGraph}, this graph becomes a super graph (see {@link #superGraphs()})
     *
     * @param graph {@link Graph}, not {@code null}
     * @return this instance
     */
    UnionGraph addSubGraph(Graph graph);

    /**
     * Removes the specified graph from the underlying graph collection.
     *
     * @param graph {@link Graph}, not {@code null}
     * @return this instance
     */
    UnionGraph removeSubGraph(Graph graph);

    /**
     * Adds the specified graph to the underlying graph collection if it is absent.
     *
     * @param graph {@link Graph}, not {@code null}
     * @return this instance
     */
    default UnionGraph addSubGraphIfAbsent(Graph graph) {
        if (!contains(graph)) {
            addSubGraph(graph);
        }
        return this;
    }

    /**
     * Answers {@code true} iff this {@code UnionGraph} contains the specified graph as a subgraph.
     *
     * @param graph {@link Graph} to test
     * @return boolean
     */
    default boolean contains(Graph graph) {
        return subGraphs().anyMatch(it -> it.equals(graph));
    }

    /**
     * An enhanced {@link GraphEventManager Jena Graph Event Manager} and {@link Listener}s.
     */
    interface EventManager extends GraphEventManager, Listener {

        /**
         * Turns off all listeners.
         */
        void off();

        /**
         * Turns on all listeners.
         */
        void on();

        /**
         * Lists all encapsulated listeners.
         *
         * @return Stream of {@link GraphListener}s
         */
        Stream<GraphListener> listeners();

        /**
         * Lists all encapsulated listeners for the given type.
         *
         * @param type {@code L}
         * @return Stream of {@link GraphListener}s
         * @param <L> {@code Class}-type of {@link GraphListener}
         */
        @SuppressWarnings("unchecked")
        default <L extends GraphListener> Stream<L> listeners(Class<L> type) {
            return listeners().filter(it -> type.isAssignableFrom(it.getClass())).map(it -> (L) it);
        }
    }

    interface Listener extends GraphListener {

        /**
         * Called before {@link UnionGraph#add(Node, Node, Node)} &amp; {@link UnionGraph#add(Triple)}.
         *
         * @param graph  {@link UnionGraph}
         * @param triple {@link Triple}
         */
        void onAddTriple(UnionGraph graph, Triple triple);

        /**
         * Called before
         * {@link UnionGraph#delete(Node, Node, Node)}
         * &amp; {@link UnionGraph#delete(Triple)}
         * &amp; {@link UnionGraph#remove(Node, Node, Node)}.
         *
         * @param graph  {@link UnionGraph}
         * @param triple {@link Triple}
         */
        void onDeleteTriple(UnionGraph graph, Triple triple);

        /**
         * Called before {@link UnionGraph#clear()}.
         *
         * @param graph {@link UnionGraph}
         */
        void onClear(UnionGraph graph);

        /**
         * Called before {@link UnionGraph#addSubGraph(Graph)}.
         *
         * @param graph    {@link UnionGraph}
         * @param subGraph {@link Graph}
         */
        void onAddSubGraph(UnionGraph graph, Graph subGraph);

        /**
         * Called before {@link UnionGraph#removeSubGraph(Graph)}.
         *
         * @param graph    {@link Graph}
         * @param subGraph {@link Graph}
         */
        void onRemoveSubGraph(UnionGraph graph, Graph subGraph);

        /**
         * Called after {@link UnionGraph#addSubGraph(Graph)}.
         * Note it differs from {@link #notifyAddGraph(Graph, Graph)},
         * which is called when RDF data added into the {@link UnionGraph#getBaseGraph()}.
         *
         * @param graph    {@link UnionGraph}
         * @param subGraph {@link Graph}
         */
        void notifySubGraphAdded(UnionGraph graph, Graph subGraph);

        /**
         * Called after {@link UnionGraph#addSubGraph(Graph)}.
         *
         * @param graph      {@link UnionGraph}
         * @param superGraph {@link UnionGraph}
         */
        void notifySuperGraphAdded(UnionGraph graph, UnionGraph superGraph);

        /**
         * Called after {@link UnionGraph#removeSubGraph(Graph)}.
         * Note it differs from {@link #notifyDeleteGraph(Graph, Graph)},
         * which is called when RDF data is added into the {@link UnionGraph#getBaseGraph()}.
         *
         * @param graph    {@link Graph}
         * @param subGraph {@link Graph}
         */
        void notifySubGraphRemoved(UnionGraph graph, Graph subGraph);

    }
}
