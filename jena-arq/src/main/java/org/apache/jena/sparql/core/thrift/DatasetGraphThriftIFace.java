package org.apache.jena.sparql.core.thrift;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.jena.query.ReadWrite;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.mosaic.ThreadFactory;
import org.apache.jena.sparql.core.mosaic.ThreadProxy;
import org.apache.jena.sparql.util.Context;
import org.apache.thrift.TException;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Thrift orientated shim to a DatasetGraph.
 * 
 * <p>All methods use a guid which is set by the caller to ensure that the same worker thread is called by the pooled Thrift thread.
 * 
 * <p>Methods which return an iterator additionally require an instance guid and return a paged wrapped iterator.
 * 
 * @author dick
 *
 */
public class DatasetGraphThriftIFace implements DatasetGraphThrift.Iface {

	// TODO Load from properties.
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DatasetGraphThriftIFace.class);

	public static final int DEFAULT_BYTE_BUFFER_CAPACITY = 8192;

	protected final DatasetGraph datasetGraph;

	protected final TServer server;
	
	protected final Thread serve;
	
	protected final ConcurrentMap<String, ThreadProxy> workers;

	protected final ConcurrentMap<String, IteratorE2PagedRDF<?>> iterators;
	
	public DatasetGraphThriftIFace(final Context context) {
		super();
		this.datasetGraph = context.get(DatasetGraphThriftFactory.THRIFT_SERVER_DATASET_GRAPH);
		this.workers = new ConcurrentHashMap<>(1024);
		this.iterators = new ConcurrentHashMap<>(1024);
		
		try {
			final InetSocketAddress inetSocketAddress = new InetSocketAddress(context.<String>get(DatasetGraphThriftFactory.THRIFT_SERVER_INET_SOCKET_ADDRESS_NAME), context.get(DatasetGraphThriftFactory.THRIFT_SERVER_INET_SOCKET_ADDRESS_PORT));
			TServerSocket tServerSocket = new TServerSocket(inetSocketAddress);
	        DatasetGraphThrift.Processor<DatasetGraphThrift.Iface> processor = new DatasetGraphThrift.Processor<>(this);
	        this.server = new TThreadPoolServer(new TThreadPoolServer.Args(tServerSocket).processor(processor));

	        serve = ThreadFactory.daemon(
	        	() -> {
					getTServer().serve();
	        	}
	        	,ThreadFactory.nameFor(DatasetGraphThriftIFace.class, inetSocketAddress.toString())
        	);
			serve.start();
		} catch (final Exception exception) {
			throw new UnsupportedOperationException(exception);
		}
		

	}
	
	protected TServer getTServer() {
		return this.server;
	}
	
	protected ConcurrentMap<String, ThreadProxy> getWorkers() {
		return this.workers;
	}
	
	protected ThreadProxy getWorker(final String uuid) {
		ThreadProxy worker = getWorkers().get(uuid);
		if (worker == null) {
			worker = new ThreadProxy();
			getWorkers().put(uuid, worker);
		}
		return worker;
	}
	
	protected void removeWorker(final String uuid) {
		final ThreadProxy worker = getWorkers().get(uuid);
		if (worker != null) {
			worker.close();
			getWorkers().remove(uuid);
		}
	}

	/**
	 * Add the given iterator and return the created UUID.
	 */
	protected String addIterator(final IteratorE2PagedRDF<?> iteratorRDF2Paged) {
		final String uuid = DatasetGraphThriftFactory.createUUID();
		iterators.put(uuid, iteratorRDF2Paged);
		return uuid;
	}
	
	/*
	 * Other
	 */

	/**
	 * Return the next iterator page for the given uuid, removing the iterator when hasNext() returns false.
	 */
	@Override
	public ByteBuffer nextIteratorPage(final String uuid) throws TException {
		final IteratorE2PagedRDF<?> iteratorRDF2Paged = iterators.get(uuid);
		ByteBuffer byteBuffer;
		if (iteratorRDF2Paged == null) {
			byteBuffer = ByteBuffer.allocate(0);
		} else if (iteratorRDF2Paged.hasNext()) {
			byteBuffer = iteratorRDF2Paged.next().getBuffer();
		} else {
			iterators.remove(uuid);
			byteBuffer = ByteBuffer.allocate(0);
		}
		return byteBuffer;
	}
	
	@Override
	public void closeIterator(final String uuid) {
		iterators.remove(uuid);
	}
	
	/*
	 * IFace
	 */
	
	@Override
	public boolean containsGraph(final String uuid, ByteBuffer byteBuffer) throws TException {
		try {
			return getWorker(uuid).submit(()-> {
				return datasetGraph.containsGraph(DatasetGraphThriftFactory.thriftToNode(byteBuffer));
			}).get();
		} catch (final Exception exception) {
			throw new TException(exception);
		}
	}

	@Override
	public String listGraphNodes(final String uuid) throws TException {
		try {
			return getWorker(uuid).submit(() -> {
				return addIterator(new IteratorE2PagedRDFNode(datasetGraph.listGraphNodes()));
			}).get();
		} catch (Exception exception) {
			throw new TException(exception);
		}
	}
	
	@Override
	public String find(final String uuid) throws TException {
		try {
			return getWorker(uuid).submit(() -> {
				return addIterator(new IteratorE2PagedRDFQuad(datasetGraph.find()));
			}).get();
		} catch (Exception exception) {
			throw new TException(exception);
		}
	}
	
	@Override
	public String findQ(final String uuid, final ByteBuffer quad) throws TException {
		try {
			return getWorker(uuid).submit(() -> {
				return addIterator(new IteratorE2PagedRDFQuad(datasetGraph.find(DatasetGraphThriftFactory.thriftToQuad(quad))));
			}).get();
		} catch (Exception exception) {
			throw new TException(exception);
		}
	}

	@Override
	public String findGSPO(final String uuid, final ByteBuffer g, final ByteBuffer s, final ByteBuffer p, final ByteBuffer o) throws TException {
		try {
			return getWorker(uuid).submit(() -> {
				return addIterator(new IteratorE2PagedRDFQuad(datasetGraph.find(DatasetGraphThriftFactory.thriftToNode(g), DatasetGraphThriftFactory.thriftToNode(s), DatasetGraphThriftFactory.thriftToNode(p), DatasetGraphThriftFactory.thriftToNode(o))));
			}).get();
		} catch (Exception exception) {
			throw new TException(exception);
		}
	}
	
	/**
	 * Jena findNG(Node, Node, Node, Node)
	 */
	@Override
	public String findNG(String uuid, ByteBuffer g, ByteBuffer s, ByteBuffer p, ByteBuffer o) throws TException {
		try {
			return getWorker(uuid).submit(() -> {
				return addIterator(new IteratorE2PagedRDFQuad(datasetGraph.findNG(DatasetGraphThriftFactory.thriftToNode(g), DatasetGraphThriftFactory.thriftToNode(s), DatasetGraphThriftFactory.thriftToNode(p), DatasetGraphThriftFactory.thriftToNode(o))));
			}).get();
		} catch (Exception exception) {
			throw new TException(exception);
		}
	}

	/**
	 * Jena contains(Node, Node, Node, Node)
	 */
	@Override
	public boolean containsGSPO(String uuid, ByteBuffer g, ByteBuffer s, ByteBuffer p, ByteBuffer o) throws TException {
		try {
			return getWorker(uuid).submit(() -> {
				return datasetGraph.contains(DatasetGraphThriftFactory.thriftToNode(g), DatasetGraphThriftFactory.thriftToNode(s), DatasetGraphThriftFactory.thriftToNode(p), DatasetGraphThriftFactory.thriftToNode(o));
			}).get();
		} catch (Exception exception) {
			throw new TException(exception);
		}
	}

	/**
	 * Jena contains(Quad)
	 */
	@Override
	public boolean containsQ(final String uuid, final ByteBuffer quad) throws TException {
		try {
			return getWorker(uuid).submit(() -> {
				return datasetGraph.contains(DatasetGraphThriftFactory.thriftToQuad(quad));
			}).get();
		} catch (Exception exception) {
			throw new TException(exception);
		}
	}
	
	@Override
	public void clear(final String uuid) throws TException {
		try {
			getWorker(uuid).execute(() -> {
				datasetGraph.clear();
			});
		} catch (Exception exception) {
			throw new TException(exception);
		}
	}

	@Override
	public boolean isEmpty(final String uuid) throws TException {
		try {
			return getWorker(uuid).submit(() -> {
				return datasetGraph.isEmpty();
			}).get();
		} catch (Exception exception) {
			throw new TException(exception);
		}
	}
	
	/*
	 * The two methods replce getLock().
	 * 
	 * Client implements a local Lock proxy which calls the following two methods.
	 */
	
	@Override
	public void enterCriticalSection(final String uuid, final boolean readLockRequested) throws TException {
		try {
			getWorker(uuid).execute(() -> {
				datasetGraph.getLock().enterCriticalSection(readLockRequested);
			});
		} catch (Exception exception) {
			throw new TException(exception);
		}
	}

	@Override
	public void leaveCriticalSection(final String uuid) throws TException {
		try {
			getWorker(uuid).execute(() -> {
				datasetGraph.getLock().leaveCriticalSection();
			});
		} catch (Exception exception) {
			throw new TException(exception);
		}
	}
	
	@Override
	public long size(final String uuid) throws TException {
		try {
			return getWorker(uuid).submit(() -> {
				return datasetGraph.size();
			}).get();
		} catch (Exception exception) {
			throw new TException(exception);
		}
	}
	
	@Override
	public void close(final String uuid) throws TException {
		try {
			if (uuid == null) {
				this.server.stop();
				this.datasetGraph.close();
			} else {
				getWorker(uuid).execute(() -> {
					datasetGraph.close();
				});
			}
		} catch (Exception exception) {
			throw new TException(exception);
		}
	}

	@Override
	public boolean supportsTransactions(String uuid) throws TException {
		try {
			return getWorker(uuid).submit(() -> {
				return datasetGraph.supportsTransactions();
			}).get();
		} catch (Exception exception) {
			throw new TException(exception);
		}
	}

	@Override
	public boolean supportsTransactionAbort(String uuid) throws TException {
		try {
			return getWorker(uuid).submit(() -> {
				return datasetGraph.supportsTransactionAbort();
			}).get();
		} catch (Exception exception) {
			throw new TException(exception);
		}
	}

	/*
	 * Transactional
	 */
	
	@Override
	public void tBegin(final String uuid, final boolean read) throws TException {
		try {
			getWorker(uuid).submit(() -> {
				datasetGraph.begin((read ? ReadWrite.READ : ReadWrite.WRITE));
			}).get();
		} catch (Exception exception) {
			throw new TException(exception);
		}
	}

	@Override
	public void tEnd(final String uuid) throws TException {
		try {
			getWorker(uuid).submit(() -> {
				datasetGraph.end();
			}).get();
		} catch (Exception exception) {
			throw new TException(exception);
		} finally {
			removeWorker(uuid);
		}
	}

	@Override
	public void tCommit(final String uuid) throws TException {
		try {
			getWorker(uuid).submit(() -> {
				datasetGraph.commit();
			}).get();
		} catch (Exception exception) {
			throw new TException(exception);
		}
	}

	@Override
	public void tAbort(final String uuid) throws TException {
		try {
			getWorker(uuid).submit(() -> {
				datasetGraph.abort();
			}).get();
		} catch (Exception exception) {
			throw new TException(exception);
		}
	}

	@Override
	public boolean tIsInTransaction(final String uuid) throws TException {
		try {
			return getWorker(uuid).submit(() -> {
				return datasetGraph.isInTransaction();
			}).get();
		} catch (Exception exception) {
			throw new TException(exception);
		}
	}

	@Override
	public String toString() {
		return server + "\n" + workers + "\n" + iterators; 
	}

}
