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

package org.apache.jena.ontapi.impl;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphEventManager;
import org.apache.jena.graph.GraphEvents;
import org.apache.jena.graph.GraphListener;
import org.apache.jena.graph.GraphUtil;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.compose.CompositionBase;
import org.apache.jena.graph.impl.SimpleEventManager;
import org.apache.jena.ontapi.UnionGraph;
import org.apache.jena.ontapi.utils.Iterators;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Stream;

/**
 * UnionGraph.
 * <p>
 * It consists of two parts: a {@link #base base graph} and an {@link UnionGraphImpl.SubGraphs sub-graphs} collection.
 * Unlike {@link org.apache.jena.graph.compose.MultiUnion MultiUnion} this implementation explicitly requires primary (base) graph.
 * Underlying sub-graphs are only used for searching; modify operations are performed only on the base graph.
 * This graph allows building graph hierarchy which can be used to link different models.
 * Also, it allows recursion, that is, it can contain itself somewhere in the hierarchy.
 * The {@link PrefixMapping} of this graph is taken from the base graph,
 * and, therefore, any changes in it reflect both the base and this graph.
 */
@SuppressWarnings({"WeakerAccess"})
public class UnionGraphImpl extends CompositionBase implements UnionGraph {

    protected final Graph base;
    protected final SubGraphs subGraphs;
    protected final boolean distinct;

    /**
     * A set of parents, used when collecting cache {@link #descendantBases},
     * which is used in read operations.
     * This allows the {@code UnionGraphImpl} instance
     * to know when the structure has changed to rebuild the cache.
     * Items of this {@code Set} are removed automatically by GC
     * if there are no more strong references (a graph/model is removed, i.e. there is no usage anymore).
     */
    protected final Set<UnionGraphImpl> parents = Collections.newSetFromMap(new WeakHashMap<>());
    /**
     * Internal cache to hold all data graphs with except of {@link #base},
     * used while {@link Graph#find(Triple) #find(..)}.
     * This {@code Set} cannot contain {@link UnionGraph}s.
     */
    protected Set<Graph> descendantBases;

    /**
     * Creates an instance with default settings.
     * <p>
     * Note: it results a distinct graph (i.e. its parameter {@link #distinct} is {@code true}).
     * This means that the method {@link #find(Triple)} does not produce duplicates.
     * The additional duplicate checking may lead to temporary writing
     * the whole graph or some its part into memory in the form of {@code Set},
     * and for huge ontologies it is unacceptable.
     * This checking is not performed if the graph is single (the underlying part is empty).
     * <p>
     * Also notice, a top-level ontology view of in-memory graph is not sensitive to the distinct parameter
     * since it uses only base graphs to collect axiomatic data.
     *
     * @param base {@link Graph}, not {@code null}
     */
    public UnionGraphImpl(Graph base) {
        this(base, true);
    }

    /**
     * Creates a graph for the given {@code base},
     * which will be either distinct or non-distinct, depending on the second parameter.
     * Other settings are default.
     *
     * @param base     {@link Graph}, not {@code null}
     * @param distinct if {@code true} a distinct graph is created
     */
    public UnionGraphImpl(Graph base, boolean distinct) {
        this(base, new EventManagerImpl(), distinct);
    }

    /**
     * Creates a graph for the given {@code base}.
     *
     * @param base         {@link Graph}, not {@code null}
     * @param eventManager {@link UnionGraph.EventManager} or {@code null} to use default fresh event manager
     * @param distinct     if {@code true} a distinct graph is created
     */
    public UnionGraphImpl(Graph base, EventManager eventManager, boolean distinct) {
        this(base, new SubGraphs(), eventManager, distinct);
    }

    /**
     * The base constructor.
     * A well-formed ontology {@link UnionGraph} is expected to
     * have a plain (non-union) graph as a {@link #getBaseGraph() root}
     * and {@code UnionGraph} as {@link #getSubGraphs() leaves}.
     *
     * @param base         {@link Graph}, not {@code null}
     * @param subGraphs    {@link SubGraphs} or {@code null} to use default empty sub-graph container
     * @param eventManager {@link EventManager} or {@code null} to use default fresh event manager
     * @param distinct     if {@code true}, the method {@link #find(Triple)} returns an iterator avoiding duplicates
     * @throws NullPointerException if base graph is {@code null}
     */
    protected UnionGraphImpl(Graph base, SubGraphs subGraphs, EventManager eventManager, boolean distinct) {
        this.base = Objects.requireNonNull(base, "Null base graph.");
        this.subGraphs = Objects.requireNonNull(subGraphs, "Null SubGraphs");
        this.gem = Objects.requireNonNull(eventManager, "Null EventManager");
        this.distinct = distinct;
    }

    @Override
    public PrefixMapping getPrefixMapping() {
        return getBaseGraph().getPrefixMapping();
    }

    /**
     * Answers the ont event manager for this graph.
     * Override to use in {@link org.apache.jena.graph.impl.GraphBase#add(Triple)}.
     *
     * @return {@link GraphEventManager}, not {@code null}
     */
    @Override
    public EventManager getEventManager() {
        return (EventManager) gem;
    }

    /**
     * Answers {@code true} iff this graph is distinct.
     * See {@link #UnionGraphImpl(Graph)} description.
     *
     * @return boolean
     */
    @Override
    public boolean isDistinct() {
        return distinct;
    }

    /**
     * Returns the base (primary) graph.
     *
     * @return {@link Graph}, not {@code null}
     */
    @Override
    public Graph getBaseGraph() {
        return base;
    }

    /**
     * Returns the underlying graph, possible empty.
     *
     * @return {@link SubGraphs}, not {@code null}
     */
    public SubGraphs getSubGraphs() {
        return subGraphs;
    }

    @Override
    public boolean hasSubGraph() {
        return !getSubGraphs().isEmpty();
    }

    @Override
    public Stream<Graph> subGraphs() {
        return getSubGraphs().graphs();
    }

    @Override
    public Stream<UnionGraph> superGraphs() {
        return parents.stream().map(it -> it);
    }

    @Override
    public void performAdd(Triple t) {
        getEventManager().onAddTriple(this, t);
        if (!subGraphs.contains(t)) {
            base.add(t);
        }
    }

    @Override
    public void performDelete(Triple t) {
        getEventManager().onDeleteTriple(this, t);
        base.delete(t);
    }

    @Override
    public void remove(Node s, Node p, Node o) {
        checkOpen();
        Triple t = Triple.createMatch(s, p, o);
        UnionGraph.EventManager em = getEventManager();
        em.onDeleteTriple(this, t);
        GraphUtil.remove(this, s, p, o);
        em.notifyEvent(this, GraphEvents.remove(s, p, o));
    }

    @Override
    public void clear() {
        checkOpen();
        UnionGraph.EventManager em = getEventManager();
        em.onClear(this);
        base.clear();
        em.notifyEvent(this, GraphEvents.removeAll);
    }

    /**
     * Adds the specified graph to the underlying graph collection.
     * Note: for a well-formed ontological {@code UnionGraph}
     * the input {@code graph} must be also a {@code UnionGraph}, even it has no hierarchy structure.
     *
     * @param graph {@link Graph}, not {@code null}
     * @return this instance
     */
    @Override
    public UnionGraph addSubGraph(Graph graph) {
        Objects.requireNonNull(graph);
        checkOpen();
        EventManager eventManager = getEventManager();
        eventManager.onAddSubGraph(this, graph);
        getSubGraphs().add(graph);
        addParent(graph);
        resetGraphsCache();
        eventManager.notifySubGraphAdded(this, graph);
        if (graph instanceof UnionGraph subGraph) {
            subGraph.getEventManager().notifySuperGraphAdded(subGraph, this);
        }
        return this;
    }

    protected void addParent(Graph graph) {
        if (!(graph instanceof UnionGraphImpl)) {
            return;
        }
        ((UnionGraphImpl) graph).parents.add(this);
    }

    /**
     * Removes the specified graph from the underlying graph collection.
     *
     * @param graph {@link Graph}, not {@code null}
     * @return this instance
     */
    @Override
    public UnionGraph removeSubGraph(Graph graph) {
        Objects.requireNonNull(graph);
        checkOpen();
        EventManager eventManager = getEventManager();
        eventManager.onRemoveSubGraph(this, graph);
        getSubGraphs().remove(graph);
        removeUnion(graph);
        resetGraphsCache();
        eventManager.notifySubGraphRemoved(this, graph);
        return this;
    }

    protected void removeUnion(Graph graph) {
        if (!(graph instanceof UnionGraphImpl)) {
            return;
        }
        ((UnionGraphImpl) graph).parents.remove(this);
    }

    /**
     * Clears the {@link #descendantBases cache}.
     */
    protected void resetGraphsCache() {
        getAllLinkedUnionGraphs().forEach(x -> x.descendantBases = null);
    }

    /**
     * Lists all indivisible (base) data {@code Graph}s
     * that are encapsulated either in the hierarchy
     * or (which is possible) inside the {@link #getBaseGraph() base} (root) graph itself.
     *
     * @return <b>distinct</b> {@link ExtendedIterator} of {@link Graph}s, including the base graph
     * @see UnionGraph#getBaseGraph()
     */
    public ExtendedIterator<Graph> listSubGraphBases() {
        return Iterators.create(descendantBases == null ? descendantBases = getAllBaseGraphs() : descendantBases);
    }

    /**
     * Performs the find operation.
     * Override {@code graphBaseFind} to return an iterator that will report when a deletion occurs.
     *
     * @param m {@link Triple} the matcher to match against, not {@code null}
     * @return an {@link ExtendedIterator iterator} of all triples matching {@code m} in the union of the graphs
     * @see org.apache.jena.graph.compose.MultiUnion#graphBaseFind(Triple)
     */
    @Override
    protected final ExtendedIterator<Triple> graphBaseFind(Triple m) {
        return SimpleEventManager.notifyingRemove(this, createFindIterator(m));
    }

    /**
     * Answers {@code true} if the graph contains any triple matching {@code t}.
     *
     * @param t {@link Triple}, not {@code null}
     * @return boolean
     * @see org.apache.jena.graph.compose.MultiUnion#graphBaseContains(Triple)
     */
    @Override
    public boolean graphBaseContains(Triple t) {
        if (base.contains(t)) {
            return true;
        }
        if (subGraphs.isEmpty()) {
            return false;
        }
        Iterator<Graph> graphs = listSubGraphBases();
        while (graphs.hasNext()) {
            Graph g = graphs.next();
            if (g == base) {
                continue;
            }
            if (g.contains(t)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int graphBaseSize() {
        if (subGraphs.isEmpty()) {
            return base.size();
        }
        return super.graphBaseSize();
    }

    @Override
    public boolean isEmpty() {
        if (subGraphs.isEmpty()) {
            return base.isEmpty();
        }
        return Iterators.findFirst(find()).isEmpty();
    }

    /**
     * Creates an extended iterator to be used in {@link Graph#find(Triple)}.
     *
     * @param m {@link Triple} pattern, not {@code null}
     * @return {@link ExtendedIterator} of {@link Triple}s
     * @see org.apache.jena.graph.compose.Union#_graphBaseFind(Triple)
     * @see org.apache.jena.graph.compose.MultiUnion#multiGraphFind(Triple)
     */
    @SuppressWarnings("JavadocReference")
    protected ExtendedIterator<Triple> createFindIterator(Triple m) {
        if (subGraphs.isEmpty()) {
            return base.find(m);
        }
        if (!distinct) {
            return Iterators.flatMap(listSubGraphBases(), x -> x.find(m));
        }
        Set<Triple> seen = createSet();
        return Iterators.flatMap(listSubGraphBases(), x -> CompositionBase.recording(rejecting(x.find(m), seen), seen));
    }

    /**
     * Creates a {@code Set} to be used while {@link Graph#find()}.
     * The returned set may contain a huge number of items.
     * And that's why this method has protected access -
     * implementations are allowed to override it for better performance.
     *
     * @return Set of {@link Triple}s
     */
    protected Set<Triple> createSet() {
        return new HashSet<>();
    }

    /**
     * Closes the graph including all related graphs.
     * Caution: this is an irreversible operation,
     * once closed, a graph cannot be reopened.
     */
    @Override
    public void close() {
        listSubGraphBases().forEachRemaining(Graph::close);
        getAllUnderlyingUnionGraphs().forEach(x -> x.closed = true);
    }

    /**
     * Calculates and returns a {@code Set} of all indivisible {@link Graph graph}s
     * that are placed somewhere lower in the hierarchy.
     * The method also includes in consideration a rare possibility
     * when the {@link #getBaseGraph() base} graph is also an {@code UnionGraph}.
     * The primary indivisible part of the base graph (which is usually a base graph itself)
     * is added to the beginning of the returned collection.
     *
     * @return a {@code Set} (ordered) of {@link Graph}s
     */
    protected Set<Graph> getAllBaseGraphs() {
        Set<Graph> res = new LinkedHashSet<>();
        Set<UnionGraphImpl> visited = new HashSet<>();
        Deque<Graph> queue = new ArrayDeque<>();
        queue.add(this);
        while (!queue.isEmpty()) {
            Graph next = queue.removeFirst();
            if (next instanceof UnionGraphImpl u) {
                if (visited.add(u)) {
                    queue.add(u.base);
                    queue.addAll(u.subGraphs.graphs);
                }
            } else {
                res.add(next);
            }
        }
        return res;
    }

    /**
     * Recursively collects a {@code Set} of all {@link UnionGraph UnionGraph}s
     * that are related to this instance somehow,
     * i.e. are present in the hierarchy lower or higher.
     * This union graph instance is also included in the returned {@code Set}.
     *
     * @return Set of {@link UnionGraph}s
     */
    protected Set<UnionGraphImpl> getAllLinkedUnionGraphs() {
        Set<UnionGraphImpl> res = new LinkedHashSet<>();
        Deque<UnionGraphImpl> queue = new ArrayDeque<>();
        queue.add(this);
        while (!queue.isEmpty()) {
            UnionGraphImpl next = queue.removeFirst();
            if (res.add(next)) {
                next.parents.stream().filter(res::add).forEach(queue::add);
                next.getAllUnderlyingUnionGraphs().stream().filter(res::add).forEach(queue::add);
            }
        }
        return res;
    }

    /**
     * Recursively collects all {@link UnionGraph}s that underlies this instance, inclusive.
     *
     * @return Set of {@link UnionGraph}s
     */
    protected Set<UnionGraphImpl> getAllUnderlyingUnionGraphs() {
        Set<UnionGraphImpl> res = new LinkedHashSet<>();
        Deque<UnionGraphImpl> queue = new ArrayDeque<>();
        queue.add(this);
        while (!queue.isEmpty()) {
            UnionGraphImpl next = queue.removeFirst();
            if (res.add(next)) {
                next.unionSubGraphs().forEach(queue::add);
            }
        }
        return res;
    }

    private Stream<UnionGraphImpl> unionSubGraphs() {
        return getSubGraphs().graphs()
                .filter(g -> g instanceof UnionGraphImpl)
                .map(u -> (UnionGraphImpl) u);
    }

    @Override
    public String toString() {
        // do not print the whole graph since it is expensive
        return "UnionGraph{@" + hashCode() + "}";
    }

    /**
     * A container to hold all sub-graphs, that make up the hierarchy.
     * Such a representation of sub-graphs collection in the form of separate class allows
     * sharing its instance among different {@code UnionGraph} instances
     * to impart whole hierarchy structure when it is needed.
     */
    public static class SubGraphs {
        protected final Collection<Graph> graphs;

        protected SubGraphs() {
            this(new ArrayList<>());
        }

        protected SubGraphs(Collection<Graph> graphs) {
            this.graphs = Objects.requireNonNull(graphs);
        }

        /**
         * Lists all sub-graphs.
         *
         * @return {@link ExtendedIterator} of sub-{@link Graph graph}s
         */
        public ExtendedIterator<Graph> listGraphs() {
            return Iterators.create(graphs);
        }

        /**
         * Lists all sub-graphs.
         *
         * @return {@code Stream} of sub-{@link Graph graph}s
         */
        public Stream<Graph> graphs() {
            return graphs.stream();
        }

        /**
         * Answers {@code true} iff this container is empty.
         *
         * @return boolean
         */
        public boolean isEmpty() {
            return graphs.isEmpty();
        }

        /**
         * Removes the given graph from the underlying collection.
         * Maybe overridden to produce corresponding event.
         *
         * @param graph {@link Graph}
         */
        public void remove(Graph graph) {
            graphs.remove(graph);
        }

        /**
         * Adds the given graph into the underlying collection.
         * Maybe overridden to produce corresponding event.
         *
         * @param graph {@link Graph}
         */
        public void add(Graph graph) {
            graphs.add(Objects.requireNonNull(graph));
        }

        /**
         * Tests if the given triple belongs to any of the sub-graphs.
         *
         * @param t {@link Triple} to test
         * @return boolean
         * @see Graph#contains(Triple)
         */
        protected boolean contains(Triple t) {
            if (graphs.isEmpty()) {
                return false;
            }
            for (Graph g : graphs) {
                if (g.contains(t)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * An extended {@link org.apache.jena.graph.GraphEventManager Jena Graph Event Manager},
     * a holder for {@link org.apache.jena.graph.GraphListener}s.
     */
    public static class EventManagerImpl extends SimpleEventManager implements EventManager {

        private final List<GraphListener> inactive = new ArrayList<>();

        @Override
        public void onAddTriple(UnionGraph graph, Triple triple) {
            listeners(Listener.class).forEach(it -> it.onAddTriple(graph, triple));
        }

        @Override
        public void onDeleteTriple(UnionGraph graph, Triple triple) {
            listeners(Listener.class).forEach(it -> it.onDeleteTriple(graph, triple));
        }

        @Override
        public void onAddSubGraph(UnionGraph graph, Graph subGraph) {
            listeners(Listener.class).forEach(it -> it.onAddSubGraph(graph, subGraph));
        }

        @Override
        public void onClear(UnionGraph graph) {
            listeners(Listener.class).forEach(it -> it.onClear(graph));
        }

        @Override
        public void notifySubGraphAdded(UnionGraph graph, Graph subGraph) {
            listeners(Listener.class).forEach(it -> it.notifySubGraphAdded(graph, subGraph));
        }

        @Override
        public void notifySuperGraphAdded(UnionGraph graph, UnionGraph superGraph) {
            listeners(Listener.class).forEach(it -> it.notifySuperGraphAdded(graph, superGraph));
        }

        @Override
        public void onRemoveSubGraph(UnionGraph graph, Graph subGraph) {
            listeners(Listener.class).forEach(it -> it.onRemoveSubGraph(graph, subGraph));
        }

        @Override
        public void notifySubGraphRemoved(UnionGraph graph, Graph subGraph) {
            listeners(Listener.class).forEach(it -> it.notifySubGraphRemoved(graph, subGraph));
        }

        @Override
        public void off() {
            inactive.addAll(listeners);
            listeners.clear();
        }

        @Override
        public void on() {
            listeners.addAll(inactive);
            inactive.clear();
        }

        /**
         * Lists all encapsulated listeners.
         *
         * @return Stream of {@link GraphListener}s
         */
        @Override
        public Stream<GraphListener> listeners() {
            return listeners.stream();
        }
    }
}
