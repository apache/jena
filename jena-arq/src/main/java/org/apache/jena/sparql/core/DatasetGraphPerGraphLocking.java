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

package org.apache.jena.sparql.core;

import static java.util.stream.Collectors.toList;
import static org.apache.jena.atlas.iterator.Iter.filter;
import static org.apache.jena.graph.GraphUtil.addInto;
import static org.apache.jena.query.ReadWrite.READ;
import static org.apache.jena.query.ReadWrite.WRITE;
import static org.apache.jena.sparql.core.Quad.defaultGraphIRI;
import static org.apache.jena.sparql.core.Quad.isDefaultGraph;
import static org.apache.jena.util.iterator.WrappedIterator.createNoRemove;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.GraphBase;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.sparql.JenaTransactionException;
import org.apache.jena.sparql.core.mem.TransactionalComponent;
import org.apache.jena.sparql.core.mem.TriTable;
import org.apache.jena.util.iterator.ExtendedIterator;

public class DatasetGraphPerGraphLocking extends DatasetGraphCollection {

    private final Map<Node, LockableGraph> graphs = new ConcurrentHashMap<>();

    private final ThreadLocal<LockableGraph> graphInTransaction = new ThreadLocal<>();

    private LockableGraph graphInTransaction() {
        return graphInTransaction.get();
    }

    private final ThreadLocal<ReadWrite> transactionType = new ThreadLocal<>();

    private ReadWrite transactionType() {
        return transactionType.get();
    }

    @Override
    public Iterator<Node> listGraphNodes() {
        return filter(graphs.keySet().iterator(), gn -> !isDefaultGraph(gn));
    }

    @Override
    public boolean supportsTransactions() {
        return true;
    }

    @Override
    public boolean isInTransaction() {
        return transactionType() != null;
    }

    @Override
    public void begin(ReadWrite readWrite) {
        if (isInTransaction()) throw new JenaTransactionException("Cannot nest transactions!");
        transactionType.set(readWrite);
    }

    private void graphAction(Consumer<LockableGraph> action) {
        if (graphInTransaction() != null) action.accept(graphInTransaction());
    }

    @Override
    public void commit() {
        if (!isInTransaction()) throw new JenaTransactionException("Cannot commit outside a transaction!");
        graphAction(LockableGraph::commit);
        cleanAfterTransaction();
    }

    @Override
    public void abort() {
        if (!isInTransaction()) throw new JenaTransactionException("Cannot abort outside a transaction!");
        graphAction(LockableGraph::abort);
        cleanAfterTransaction();
    }

    @Override
    public void end() {
        cleanAfterTransaction();
    }

    private void cleanAfterTransaction() {
        graphInTransaction.remove();
        transactionType.remove();
    }

    @Override
    public Graph getDefaultGraph() {
        return getGraph(defaultGraphIRI);
    }

    @Override
    public void setDefaultGraph(final Graph g) {
        addGraph(defaultGraphIRI, g);
    }

    @Override
    public Graph getGraph(final Node graphName) {
        if (isInTransaction()) {
            // are we using the current graph?
            if (graphInTransaction()!= null && graphName.equals(graphInTransaction().graphName())) return graphInTransaction();
            // are we starting work with a graph?
            if (graphInTransaction() == null) {
                LockableGraph graph = graphs.computeIfAbsent(graphName, LockableGraph::new);
                graph.begin(transactionType());
                graphInTransaction.set(graph);
                return graph;
            }
            // we already have a graph in hand and cannot use another!
            throw new JenaTransactionRegionException("Cannot work with more than one graph per transaction!");
        }
        // we must work without a transaction
        return graphs.computeIfAbsent(graphName, LockableGraph::new);
    }

    @Override
    public void clear() {
        if (!isInTransaction()) _clear(graphs.values());
        else switch (transactionType()) {
        case READ:
            throw new JenaTransactionException("Cannot clear during a READ transaction!");
        case WRITE:
            // avoid trying to nest a transaction on a graph we may have in hand
            _clear(graphs.values().stream().filter(g -> !g.equals(graphInTransaction())).collect(toList()));
            graphAction(LockableGraph::clear);
        }

    }

    private static void _clear(Collection<LockableGraph> graphs) {
        try {
            graphs.forEach(LockableGraph::beginWrite);
            graphs.forEach(LockableGraph::clear);
            graphs.forEach(LockableGraph::commit);
        } finally {
            graphs.forEach(LockableGraph::end);
        }
    }

    @Override
    public void addGraph(Node graphName, Graph triples) {
        wholeGraphAction(graphName, g -> addInto(getGraph(g), triples));
    }

    @Override
    public void removeGraph(Node graphName) {
        wholeGraphAction(graphName, graphs::remove);
    }

    private void wholeGraphAction(Node graphName, Consumer<Node> action) {
        getGraph(graphName).clear();
        action.accept(graphName);
    }

    /**
     * A graph with a distinguished node, which we use as a name.
     */
    private abstract static class PointedGraph extends GraphBase {

        private final Node distinguishedNode;

        public PointedGraph(Node graphName) {
            this.distinguishedNode = graphName;
        }

        public Node graphName() {
            return distinguishedNode;
        }
    }

    /**
     * A {@link PointedGraph} that features a write-lock and supports transactions. If a mutation is made outside a
     * transaction, it is auto-wrapped in a transaction.
     */
    private static class LockableGraph extends PointedGraph implements TransactionalComponent {

        public LockableGraph(Node graphName) {
            super(graphName);
        }

        /**
         * We permit only one concurrent writer per named graph.
         */
        private final Lock writeLock = new ReentrantLock(true);

        private final ThreadLocal<ReadWrite> currentTransactionType = new ThreadLocal<>();

        private ReadWrite currentTransactionType() {
            return currentTransactionType.get();
        }

        private final TriTable table = new TriTable();

        @Override
        protected ExtendedIterator<Triple> graphBaseFind(Triple t) {
            if (currentTransactionType() == null) {
                begin(READ);
                try {
                    return _find(t);
                } finally {
                    end();
                }
            }
            return _find(t);
        }

        /**
         * Must be called inside a transaction!
         * 
         * @param t the triple-pattern to search
         * @return matches from the in-transaction table
         */
        private ExtendedIterator<Triple> _find(Triple t) {
            return createNoRemove(table.find(t.getSubject(), t.getPredicate(), t.getObject()).iterator());
        }

        @Override
        public void performAdd(Triple t) {
            performMutation(t, table::add);
        }

        @Override
        public void performDelete(Triple t) {
            performMutation(t, table::delete);
        }

        private void performMutation(Triple t, Consumer<Triple> action) {
            final ReadWrite readWrite = currentTransactionType();
            if (readWrite == null) {
                begin(WRITE);
                try {
                    action.accept(t);
                    commit();
                } finally {
                    end();
                }
            } else switch (readWrite) {
            case READ:
                throw new JenaTransactionException("Cannot write during a READ transaction!");
            case WRITE:
                action.accept(t);
            }
        }

        @Override
        protected int graphBaseSize() {
            // TODO make this efficient, somehow
            return super.graphBaseSize();
        }

        @Override
        public void begin(ReadWrite readWrite) {
            if (currentTransactionType() != null) throw new JenaTransactionException("Cannot nest transactions!");
            if (WRITE.equals(readWrite)) writeLock.lock();
            currentTransactionType.set(readWrite);
            table.begin(readWrite);
        }

        public void beginWrite() {
            begin(WRITE);
        }

        @Override
        public void commit() {
            table.commit();
            finishTransaction();
        }

        @Override
        public void abort() {
            table.abort();
            finishTransaction();
        }

        @Override
        public void end() {
            table.end();
            finishTransaction();
        }

        private void finishTransaction() {
            final ReadWrite readWrite = currentTransactionType();
            currentTransactionType.remove();
            if (WRITE.equals(readWrite)) writeLock.unlock();
        }
    }
}
