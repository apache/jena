package org.apache.jena.sparql.core.thrift;

import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.shared.JenaException;
import org.apache.jena.shared.Lock;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.GraphView;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.mosaic.DatasetGraphShimWrite;
import org.apache.jena.sparql.util.Context;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client implementation for a DatasetGraphThrift.
 * 
 * The client connects to an iface and uses a thread local UUID to maintain thread affinity when requesting actions.
 * 
 * A number of convenience methods are used mostly returning iterators.
 * 
 * @author dick
 *
 */
public class DatasetGraphThriftClient implements DatasetGraph {

	protected static final Logger LOGGER = LoggerFactory.getLogger(DatasetGraphThriftClient.class);
	
	protected final TTransport transport;
	
	protected final TProtocol protocol;
	
	protected final DatasetGraphThrift.Client client;

	/**
	 * Calls to IFaceThriftDatasetGraph require a UUID so this automatically assigns one per thread.
	 */
	protected ThreadLocal<String> uuid;
	
	protected Lock lock;
	
//	protected final DatasetGraphShimWrite datasetGraphShimWrite;
	
	public DatasetGraphThriftClient(final Context context) {
		try {
	        transport = new TSocket(context.<String>get(DatasetGraphThriftFactory.THRIFT_SERVER_INET_SOCKET_ADDRESS_NAME), context.get(DatasetGraphThriftFactory.THRIFT_SERVER_INET_SOCKET_ADDRESS_PORT));
	        transport.open();

	        protocol = new TBinaryProtocol(transport);
	        
	        client = new DatasetGraphThrift.Client(protocol);

	        uuid = ThreadLocal.<String>withInitial(() -> {
    			return DatasetGraphThriftFactory.createUUID();
	    	});

	        lock = new Lock() {
					@Override
					public void enterCriticalSection(final boolean readLockRequested) {
						try {
							getClient().enterCriticalSection(getUUID(), readLockRequested);
						} catch (final Exception exception) {
							throw new JenaException(exception);
						}
					}

	        		@Override
					public void leaveCriticalSection() {
						try {
							getClient().leaveCriticalSection(getUUID());
						} catch (final Exception exception) {
							throw new JenaException(exception);
						}
					}
	        };
		} catch (final Exception exception) {
			throw new JenaException(exception);
		}
	}

	/*
	 * Other.
	 */
	
	protected DatasetGraphThrift.Client getClient() {
		return client;
	}
	
	/**
	 * Indirection, returns the thread local UUID.
	 */
	protected String getUUID() {
		return uuid.get();
	}
	
	/*
	 * DatasetGraph
	 */
	
	@Override
	public Graph getDefaultGraph() {
		return GraphView.createDefaultGraph(this);
	}

	@Override
	public Graph getGraph(Node graphNode) {
		return GraphView.createNamedGraph(this, graphNode);
	}

	@Override
	public boolean containsGraph(final Node graphNode) {
		try {
			return getClient().containsGraph(getUUID(), DatasetGraphThriftFactory.toThrift(graphNode));
		} catch (final Exception exception) {
			throw new JenaException(exception);
		}
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
		try {
			return new IteratorPaged2RDFNode(new InputStreamPaged() {
				
				protected final String uuid = getClient().listGraphNodes(getUUID());
				
				@Override
				protected Page nextPage() {
					try {
						return new Page(getClient().nextIteratorPage(uuid)); 
					} catch (final Exception exception) {
						throw new JenaException(exception);
					}
				}
			});
		} catch (final Exception exception) {
			throw new JenaException(exception);
		}
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

	/**
	 * Return a paged RDF quad iterator based on the supplied UUID.
	 */
	protected IteratorPaged2RDFQuad iteratorPaged2RDFQuad(final Supplier<String> f) {
		try {
			return new IteratorPaged2RDFQuad(new InputStreamPaged() {
				
				protected final String uuid = f.get();
				
				@Override
				protected Page nextPage() {
					try {
						return new Page(getClient().nextIteratorPage(uuid)); 
					} catch (final Exception exception) {
						throw new JenaException(exception);
					}
				}
			});
		} catch (final Exception exception) {
			throw new JenaException(exception);
		}
	}

	protected void wrappedException(final Runnable r) {
		try {
			r.run();
		} catch (final Exception exception) {
			throw new JenaException(exception);
		}
	}
	
	/**
	 * Convenience method to catch the TException thrown by every Thrift Client method call and return as a JenaException with cause.
	 */
	protected <T> T wrappedException(final Callable<T> r) {
		try {
			return r.call();
		} catch (final Exception exception) {
			throw new JenaException(exception);
		}
	}
	
	@Override
	public Iterator<Quad> find() {
		return iteratorPaged2RDFQuad(() -> wrappedException(() -> getClient().find(getUUID())));
	}

	@Override
	public Iterator<Quad> find(final Quad quad) {
		return iteratorPaged2RDFQuad(() -> wrappedException(() -> getClient().findQ(getUUID(), DatasetGraphThriftFactory.toThrift(quad))));
	}

	@Override
	public Iterator<Quad> find(final Node g, final Node s, final Node p, final Node o) {
		return iteratorPaged2RDFQuad(() -> wrappedException(() -> getClient().findGSPO(getUUID(), DatasetGraphThriftFactory.toThrift(g), DatasetGraphThriftFactory.toThrift(s), DatasetGraphThriftFactory.toThrift(p), DatasetGraphThriftFactory.toThrift(o))));
	}

	@Override
	public Iterator<Quad> findNG(Node g, Node s, Node p, Node o) {
		return iteratorPaged2RDFQuad(() -> wrappedException(() -> getClient().findNG(getUUID(), DatasetGraphThriftFactory.toThrift(g), DatasetGraphThriftFactory.toThrift(s), DatasetGraphThriftFactory.toThrift(p), DatasetGraphThriftFactory.toThrift(o))));
	}

	@Override
	public boolean contains(Node g, Node s, Node p, Node o) {
		return wrappedException(() -> getClient().containsGSPO(getUUID(), DatasetGraphThriftFactory.toThrift(g), DatasetGraphThriftFactory.toThrift(s), DatasetGraphThriftFactory.toThrift(p), DatasetGraphThriftFactory.toThrift(o)));
	}

	@Override
	public boolean contains(Quad quad) {
		return wrappedException(() -> getClient().containsQ(getUUID(), DatasetGraphThriftFactory.toThrift(quad)));
	}

	@Override
	public void clear() {
		try {
			getClient().clear(getUUID());
		} catch (final Exception exception) {
			throw new JenaException(exception);
		}
	}

	@Override
	public boolean isEmpty() {
		try {
			return getClient().isEmpty(getUUID());
		} catch (final Exception exception) {
			throw new JenaException(exception);
		}
	}

	@Override
	public Lock getLock() {
		return lock;
	}

	@Override
	public Context getContext() {
		// TODO Get the Context from the IFace, somehow!
		return Context.emptyContext;
	}

	@Override
	public long size() {
		try {
			return getClient().size(getUUID());
		} catch (final Exception exception) {
			throw new JenaException(exception);
		}
	}

	@Override
	public void close() {
		try {
			client.close(getUUID());
			client.close(null);
	        transport.close();
		} catch (final Exception exception) {
			throw new JenaException(exception);
		}
	}

	@Override
	public boolean supportsTransactions() {
		try {
			return getClient().supportsTransactions(getUUID());
		} catch (final TException tException) {
			throw new JenaException(tException);
		}
	}

	@Override
	public boolean supportsTransactionAbort() {
		try {
			return getClient().supportsTransactionAbort(getUUID());
		} catch (final TException tException) {
			throw new JenaException(tException);
		}
	}

	/*
	 * Transactional
	 */
	
	@Override
	public void begin(final ReadWrite readWrite) {
		try {
			getClient().tBegin(getUUID(), readWrite.equals(ReadWrite.READ));
		} catch (final TException tException) {
			throw new JenaException(tException);
		}
	}

	@Override
	public void commit() {
		try {
			getClient().tCommit(getUUID());
		} catch (final TException tException) {
			throw new JenaException(tException);
		}
	}

	@Override
	public void abort() {
		try {
			getClient().tAbort(getUUID());
		} catch (final TException tException) {
			throw new JenaException(tException);
		}
	}

	@Override
	public void end() {
		try {
			getClient().tEnd(getUUID());
		} catch (final TException tException) {
			throw new JenaException(tException);
		}
	}

	@Override
	public boolean isInTransaction() {
		try {
			return getClient().tIsInTransaction(getUUID());
		} catch (final TException tException) {
			throw new JenaException(tException);
		}
	}

	@Override
	public String toString() {
		return transport + "\n" + protocol + "\n" + client; 
	}
}
