package org.apache.jena.sparql.core.mosaic;

import java.util.Iterator;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.shared.JenaException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.util.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper around a DatasetGraph which allows Transactional methods to be tried, i.e. non blocking.
 * 
 * @author dick
 *
 */
public class DatasetGraphTessera implements DatasetGraph {

	private static final Logger LOGGER = LoggerFactory.getLogger(DatasetGraphTessera.class);
	
	public static DatasetGraphTessera wrap(final DatasetGraph datasetGraph) {
		DatasetGraphTessera datasetGraphTessera = null;
		
// TODO If we check against the class it pulls in jena-tdb as a dependency. Using "" doesn't help as we need to cast to DatasetGraphTransaction to call .getWrapped()
//		if (datasetGraph.getClass().getName().equals(DatasetGraphInMemory.class.getName())) {
//			datasetGraphTry = new DatasetGraphTessera(datasetGraph, new LockTryMRPlusSW());
//		} else if (datasetGraph.getClass().getName().equals("org.apache.jena.tdb.transaction.DatasetGraphTransaction")) {
//			final DatasetGraph wrapped = ((DatasetGraphTransaction) datasetGraph).getWrapped();
//			if (wrapped.getClass().getName().equals("org.apache.jena.tdb.store.DatasetGraphTDB")) {
//				datasetGraphTry = new DatasetGraphTessera(datasetGraph, new LockTryMRPlusSW());
//			}
//		}
		
		datasetGraphTessera = new DatasetGraphTessera(datasetGraph, new LockTryMRPlusSW());
		
// TODO This null check is only required if we are checking the wrapped class!
//		if (datasetGraphTessera == null) {
//			throw new UnsupportedOperationException(datasetGraph.getClass().getName());
//		}

		return datasetGraphTessera;
	}
	
	protected final DatasetGraph datasetGraph;
	
	protected final LockTry lockTry;
	
	protected final ThreadLocal<ReadWrite> transactionType;
	
	protected DatasetGraphTessera(final DatasetGraph datasetGraph, final LockTry lockTry) {
		super();
		this.datasetGraph = datasetGraph;
		this.lockTry = lockTry;
		this.transactionType = new ThreadLocal<>();
	}

	/*
	 * Try.
	 */

	protected DatasetGraph getDatasetGraph() {
		return this.datasetGraph;
	}
	
	public boolean tryBegin(final ReadWrite readWrite) {
		if (isInTransaction()) {
			throw new JenaException("Already in a transaction");
		}
		boolean result = getLock().tryEnterCriticalSection(readWrite.equals(ReadWrite.READ));
		if (result) {
			try {
				getDatasetGraph().begin(readWrite);
				transactionType.set(readWrite);
			} catch (final Exception exception) {
				transactionType.remove();
				getLock().leaveCriticalSection();
				result = false;
			}
		}
		return result;
	}
	
	public <T> T rdfLoad(final String uri) {
		
		return (T) null;
	}
	
	/*
	 * DatasetGraph.
	 */
	
	@Override
	public Graph getDefaultGraph() {
		return getDatasetGraph().getDefaultGraph();
	}

	@Override
	public Graph getGraph(Node graphNode) {
		return getDatasetGraph().getGraph(graphNode);
	}

	@Override
	public boolean containsGraph(Node graphNode) {
		return getDatasetGraph().containsGraph(graphNode);
	}

	@Override
	public void setDefaultGraph(Graph g) {
		getDatasetGraph().setDefaultGraph(g);
	}

	@Override
	public void addGraph(Node graphName, Graph graph) {
		getDatasetGraph().addGraph(graphName, graph);
	}

	@Override
	public void removeGraph(Node graphName) {
		getDatasetGraph().removeGraph(graphName);
	}

	@Override
	public Iterator<Node> listGraphNodes() {
		return getDatasetGraph().listGraphNodes();
	}

	@Override
	public void add(Quad quad) {
		getDatasetGraph().add(quad);
	}

	@Override
	public void delete(Quad quad) {
		getDatasetGraph().delete(quad);
	}

	@Override
	public void add(Node g, Node s, Node p, Node o) {
		getDatasetGraph().add(g, s, p, o);		
	}

	@Override
	public void delete(Node g, Node s, Node p, Node o) {
		getDatasetGraph().delete(g, s, p, o);
	}

	@Override
	public void deleteAny(Node g, Node s, Node p, Node o) {
		getDatasetGraph().deleteAny(g, s, p, o);
	}

	@Override
	public Iterator<Quad> find() {
		return getDatasetGraph().find();
	}

	@Override
	public Iterator<Quad> find(Quad quad) {
		return getDatasetGraph().find(quad);
	}

	@Override
	public Iterator<Quad> find(Node g, Node s, Node p, Node o) {
		return getDatasetGraph().find(g, s, p, o);
	}

	@Override
	public Iterator<Quad> findNG(Node g, Node s, Node p, Node o) {
		return getDatasetGraph().findNG(g, s, p, o);
	}

	@Override
	public boolean contains(Node g, Node s, Node p, Node o) {
		return getDatasetGraph().contains(g, s, p, o);
	}

	@Override
	public boolean contains(Quad quad) {
		return getDatasetGraph().contains(quad);
	}

	@Override
	public void clear() {
		getDatasetGraph().clear();
	}

	@Override
	public boolean isEmpty() {
		return getDatasetGraph().isEmpty();
	}

	@Override
	public LockTry getLock() {
		return this.lockTry;
	}

	@Override
	public Context getContext() {
		return getDatasetGraph().getContext();
	}

	@Override
	public long size() {
		return getDatasetGraph().size();
	}

	@Override
	public void close() {
		getDatasetGraph().close();
	}

	@Override
	public boolean supportsTransactions() {
		return getDatasetGraph().supportsTransactions();
	}

	@Override
	public boolean supportsTransactionAbort() {
		return getDatasetGraph().supportsTransactionAbort();
	}

	/*
	 * Transactional
	 */

	@Override
	public void begin(final ReadWrite readWrite) {
		if (isInTransaction()) {
			throw new JenaException("Already in a transaction");
		}
		getLock().enterCriticalSection(readWrite.equals(ReadWrite.READ));
		try {
			getDatasetGraph().begin(readWrite);
			transactionType.set(readWrite);
		} catch (final Exception exception) {
			transactionType.remove();
			getLock().leaveCriticalSection();
		}
	}

	@Override
	public void commit() {
		if (!isInTransaction()) {
			throw new JenaException("Not in a transaction");
		}
		try {
			getDatasetGraph().commit();
		} catch (final Exception exception) {
			
		} finally {
			transactionType.remove();
			getLock().leaveCriticalSection();
		}
	}

	@Override
	public void abort() {
		if (!isInTransaction()) {
			throw new JenaException("Not in a transaction");
		}
		try {
			getDatasetGraph().abort();
		} catch (final Exception exception) {
			
		} finally {
			transactionType.remove();
			getLock().leaveCriticalSection();
		}
	}

	@Override
	public void end() {
		if (!isInTransaction()) {
			return;
		}
		if (transactionType.get().equals(ReadWrite.READ)) {
			getDatasetGraph().end();
		} else if (transactionType.get().equals(ReadWrite.WRITE)) {
			LOGGER.warn("Automatic abort() from end()");
			abort();
		}
		transactionType.remove();
	}

	@Override
	public boolean isInTransaction() {
		return transactionType.get() != null;
	}

	@Override
	public String toString() {
		return getClass() + " DatasetGraph [" + getDatasetGraph().getClass() + "] LockTry [" + lockTry + "] Transaction [" + transactionType.get() + "]";
	}
}
