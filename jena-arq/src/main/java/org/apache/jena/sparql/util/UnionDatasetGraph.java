package org.apache.jena.sparql.util;

import static org.apache.jena.query.ReadWrite.WRITE;

import java.util.Iterator;
import java.util.function.Function;

import org.apache.jena.ext.com.google.common.collect.Iterators;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.compose.Union;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.shared.Lock;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;

public class UnionDatasetGraph extends org.apache.jena.atlas.lib.Union<DatasetGraph> implements DatasetGraph {

    private final Lock lock;

    private final Context context;

    public UnionDatasetGraph(DatasetGraph left, DatasetGraph right) {
        this(left, right, Context.emptyContext);
    }

    public UnionDatasetGraph(DatasetGraph left, DatasetGraph right, Context context) {
        super(left, right);
        this.context = context;
        this.lock = new UnionLock(left.getLock(), right.getLock());
    }

    private Graph union(Function<DatasetGraph, Graph> op) {
        return new Union(op.apply(left), op.apply(right));
    }

    <T> Iterator<T> fromEach(Function<DatasetGraph, Iterator<T>> op) {
        return Iterators.concat(op.apply(left), op.apply(right));
    }

    @Override
    public void begin(ReadWrite readWrite) {
        if (readWrite.equals(WRITE)) throw new UnsupportedOperationException();
        forEach(dsg -> dsg.begin(readWrite));
    }

    @Override
    public void commit() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void abort() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void end() {
        forEach(DatasetGraph::end);
    }

    @Override
    public boolean isInTransaction() {
        return either(DatasetGraph::isInTransaction);
    }

    @Override
    public Graph getDefaultGraph() {
        return union(DatasetGraph::getDefaultGraph);
    }

    @Override
    public Graph getGraph(Node graphNode) {
        return union(dsg -> dsg.getGraph(graphNode));
    }

    @Override
    public Graph getUnionGraph() {
        return union(DatasetGraph::getUnionGraph);
    }

    @Override
    public boolean containsGraph(Node graphNode) {
        return either(dsg -> dsg.containsGraph(graphNode));
    }

    @Override
    public void setDefaultGraph(Graph g) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addGraph(Node graphName, Graph graph) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeGraph(Node graphName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<Node> listGraphNodes() {
        return fromEach(DatasetGraph::listGraphNodes);
    }

    @Override
    public void add(Quad quad) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(Quad quad) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(Node g, Node s, Node p, Node o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(Node g, Node s, Node p, Node o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteAny(Node g, Node s, Node p, Node o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<Quad> find() {
        return fromEach(DatasetGraph::find);
    }

    @Override
    public Iterator<Quad> find(Quad quad) {
        return fromEach(dsg -> dsg.find(quad));
    }

    @Override
    public Iterator<Quad> find(Node g, Node s, Node p, Node o) {
        return fromEach(dsg -> dsg.find(g, s, p, o));
    }

    @Override
    public Iterator<Quad> findNG(Node g, Node s, Node p, Node o) {
        return fromEach(dsg -> dsg.findNG(g, s, p, o));
    }

    @Override
    public boolean contains(Node g, Node s, Node p, Node o) {
        return either(dsg -> dsg.contains(g, s, p, o));
    }

    @Override
    public boolean contains(Quad quad) {
        return either(dsg -> dsg.contains(quad));
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        return both(DatasetGraph::isEmpty);
    }

    @Override
    public Lock getLock() {
        return lock;
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public long size() {
        return left.size() + right.size();
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsTransactions() {
        return both(DatasetGraph::supportsTransactions);
    }

    @Override
    public boolean supportsTransactionAbort() {
        return both(DatasetGraph::supportsTransactionAbort);
    }

    private static class UnionLock extends org.apache.jena.atlas.lib.Union<Lock> implements Lock {

        public UnionLock(Lock left, Lock right) {
            super(left, right);
        }

        @Override
        public void enterCriticalSection(boolean readLockRequested) {
            forEach(lock -> lock.enterCriticalSection(readLockRequested));
        }

        @Override
        public void leaveCriticalSection() {
            forEach(Lock::leaveCriticalSection);
        }
    }
}
