package org.apache.jena.sparql.util;

import static org.apache.jena.query.ReadWrite.WRITE;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.shared.Lock;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;

public abstract class ViewDatasetGraph extends Pair.OfSameType<DatasetGraph> implements DatasetGraph {
	
    private final Context context;
	
    private final Lock lock;

	public ViewDatasetGraph(DatasetGraph left, DatasetGraph right) {
		this(left, right, Context.emptyContext);
	}


    public ViewDatasetGraph(DatasetGraph left, DatasetGraph right, Context c) {
		super(left, right);
		this.context = c;
		this.lock = new PairLock(left.getLock(), right.getLock());
	}


	@Override
    public void commit() {
        throw new UnsupportedOperationException();
    }
	
    @Override
    public void begin(ReadWrite readWrite) {
        if (readWrite.equals(WRITE)) throw new UnsupportedOperationException();
        forEach(dsg -> dsg.begin(readWrite));
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
    public void clear() {
        throw new UnsupportedOperationException();
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
    public void close() {}

    @Override
    public boolean supportsTransactions() {
        return both(DatasetGraph::supportsTransactions);
    }

    @Override
    public boolean supportsTransactionAbort() {
        return false;
    }

    private static class PairLock extends Pair.OfSameType<Lock> implements Lock {

        public PairLock(Lock left, Lock right) {
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
