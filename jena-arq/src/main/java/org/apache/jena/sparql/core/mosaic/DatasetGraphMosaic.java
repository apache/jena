package org.apache.jena.sparql.core.mosaic;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.shared.JenaException;
import org.apache.jena.shared.Lock;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.GraphView;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.thrift.IteratorCachedArray;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;
import org.slf4j.Logger;

/**
 * A DatasetGraph which distributes actions across a number of child DatasetGraph's.
 * 
 * As Jena requires Thread affinity when working with transactions this class uses ThreadProxy.
 * 
 * Most DatasetGraph methods call into a set of convenience methods which perform common tasks, i.e. mosaicIterator(Function<DatasetGraph, Iterator<T>>).
 * 
 * @author dick
 *
 */
public class DatasetGraphMosaic implements DatasetGraph {
	
	private static final Logger LOGGER = getLogger(DatasetGraphMosaic.class);
	
	protected static final IDFactory ID_FACTORY = IDFactory.valueOf(DatasetGraphMosaic.class);
	
	public static final Symbol MOSAIC_STREAM_SEQUENTIAL = Symbol.create(ID_FACTORY.suffix("mosaicStreamSequential"));

	public static final Symbol WRAP_ITERATOR = Symbol.create(ID_FACTORY.suffix("wrapIterator"));

	protected final String id = IDFactory.createUUID();
	
	protected volatile Boolean closed = false;
	
	protected final Topology topology = new Topology();
	
	protected final Set<Tessera> mosaic;
	
	protected final ThreadLocal<TransactionalDistributed> transactional;

	protected final WeakHashMap<Thread, TransactionalDistributed> monitor;
	
	protected final Lock lock;
	
	protected final DatasetGraphShimWrite shimWrite;
	
	protected final AtomicInteger readCount = new AtomicInteger();

	protected final AtomicInteger writeCount = new AtomicInteger();

	protected final AtomicInteger transactionCount = new AtomicInteger();
	
	protected final Context context;
	
	public DatasetGraphMosaic(final Context context) {
		super();
		
		mosaic = ConcurrentHashMap.newKeySet(256);
		
		transactional = new ThreadLocal<>();
		
		monitor = new WeakHashMap<>(32);
		
		lock = new LockMRAndMW();

		shimWrite = (DatasetGraphShimWrite) context.get(DatasetGraphShimWrite.SHIM_WRITE, DatasetGraphShimWrite.RO);
		
		this.context = context;
	}
	
	/*
	 * Convenience methods to perform common actions.
	 */

	public Boolean isClosed() {
		return closed;
	}
	
	protected void exceptionIfClosed() {
		if (isClosed()) {
			throw new JenaException("Datasetgraph is closed");
		}
	}
	
	public boolean isLocal() {
		return IDFactory.isLocal(id);
	}

	protected Topology getTopology() {
		return topology;
	}
	
	/**
	 * Indirection to the Mosaic (a Set of DatasetGraph's).
	 */
	protected Set<Tessera> getMosaic() {
		return mosaic;
	}

	/**
	 * Return the Mosaic as a sequential stream.
	 */
	protected Stream<Tessera> sequentialMosaic() {
		return getMosaic().stream();
	}
	
	/**
	 * Return the Mosaic as a parallel stream.
	 */
	protected Stream<Tessera> parallelMosaic() {
		return getMosaic().parallelStream();
	}

	/**
	 * Indirection to return the Mosaic as a Stream.
	 * By default this will return parallelMosaic().
	 */
	protected Stream<Tessera> streamMosaic() {
		return parallelMosaic();
	}

	/**
	 * Return a parallel Stream<T> from the given Iterable<T>.
	 */
	protected <T> Stream<T> stream(final Iterable<T> iterable) {
		return StreamSupport.stream(iterable.spliterator(), true);
	}

	/**
	 * Return a parallel Stream<T> from the given Iterator<T>.
	 */
	protected <T> Stream<T> stream(final Iterator<T> iterator) {
		return stream(() -> iterator);
	}
	
	/**
	 * Return a  Stream<T> which is a distinct aggregate of applying the given function to each DatasetGraph in the mosaic.
	 * Uses streamMosaic() so will be parallel if streamMosaic() is parallel...
	 */
	public <T> Stream<T> mosaicFind(final Function<DatasetGraph, Stream<T>> find) {
		try {
			// Get the TransactionalDistributed because we reference it in the .flatMap(Function<DatasetGraph, Stream<T>>).
			final TransactionalDistributed transactionalDistributed = getTransactionalDistributed();
			return transactionalDistributed.submit(
				() -> {
					return streamMosaic()
						.flatMap(
							(tesserae) -> {
								try {
									// This will be called on a different Thread so we need to ensure the correct ThreadProxy is used.
									return transactionalDistributed.submit(
										tesserae.getDatasetGraph(),
										() -> {
											return find.apply(tesserae.getDatasetGraph());
										}
									).get();
								} catch (final Exception exception) {
									return Stream.empty();
								}
							}
						)
						.distinct();
				}
			).get();
		} catch (final Exception exception) {
			LOGGER.warn("mosaicFind", exception);
			return Stream.empty();
		}
	}

	protected Stream<DatasetGraphTessera> mosaicFilter(final Predicate<DatasetGraph> predicate) {
		try {
			final TransactionalDistributed transactionalDistributed = getTransactionalDistributed();
			return transactionalDistributed.submit(() -> streamMosaic()
				.filter((tesserae) -> {
					try {
						return transactionalDistributed.submit(tesserae.getDatasetGraph(), () -> {
							return predicate.test(tesserae.getDatasetGraph());
						}).get();
					} catch (Exception exception) {
						LOGGER.warn("mosaicFilter", exception);
						return false;
					}
				})
				.map(Tessera::getDatasetGraphTry)	
			)
			.get();
		} catch (final Exception exception) {
			LOGGER.warn("mosaicFilter", exception);
			return Stream.empty();
		}
	}
	
	/**
	 * Return a Boolean which is an any match aggregate of applying the given function to each DatasetGraph in the set.
	 */
	protected Boolean mosaicAnyMatch(final Function<DatasetGraph, Boolean> match) {
		try {
			final TransactionalDistributed transactionalDistributed = getTransactionalDistributed();
			return transactionalDistributed.submit(
				() -> parallelMosaic().anyMatch(
					(tesserae) -> {
						try {
							return transactionalDistributed.submit(tesserae.getDatasetGraph(), () -> {
								return match.apply(tesserae.getDatasetGraph());
							}).get();
						} catch (Exception exception) {
							LOGGER.warn("mosaicMatchAny", exception);
							return false;
						}
					}
				)).get();
		} catch (final Exception exception) {
			LOGGER.warn("mosaicMatchAny", exception);
			return false;
		}
	}

	/**
	 * Return a Boolean which is an any match aggregate of applying the given function to each DatasetGraph in the set.
	 */
	protected Boolean mosaicAllMatch(final Function<DatasetGraph, Boolean> match) {
		try {
			final TransactionalDistributed transactionalDistributed = getTransactionalDistributed();
			return transactionalDistributed.submit(
				() -> parallelMosaic().allMatch(
					(tessera) -> {
						try {
							return transactionalDistributed.submit(tessera.getDatasetGraph(), () -> {
								return match.apply(tessera.getDatasetGraph());
							}).get();
						} catch (Exception exception) {
							LOGGER.warn("mosaicAllMAtch", exception);
							return false;
						}
					}
				)).get();
		} catch (final Exception exception) {
			LOGGER.warn("mosaicAllMatch", exception);
			return false;
		}
	}

	/**
	 * Apply the given Consumer to each DatasetGraph in the set.
	 */
	protected void mosaicForEach(final Consumer<DatasetGraph> consumer) {
		try {
			final TransactionalDistributed transactionalDistributed = getTransactionalDistributed();
			transactionalDistributed.execute(() -> parallelMosaic().forEach((tessera) -> {transactionalDistributed.execute(tessera.getDatasetGraph(), () -> {consumer.accept(tessera.getDatasetGraph());});}));
		} catch (final Exception exception) {
			LOGGER.warn("mosaicForEach", exception);
		}
	}

	/**
	 * Indirection to wrap an Iterator, i.e. with IteratorCachedArray(Iterator, 256).
	 */
	protected <T> Iterator<T> wrapIterator(final Iterator<T> iterator) {
		if (context.isTrue(WRAP_ITERATOR)) {
			return new IteratorCachedArray<>(iterator, 512);
		} else {
			return iterator;
		}
	}
	
	protected <T> Iterator<T> iteratorCached(final Stream<T> stream) {
		return new IteratorCachedArray<>(stream.iterator(), 512);
	}
	
	/**
	 * Add a datasetGraph to the Mosaic. 
	 */
	public DatasetGraphMosaic add(final DatasetGraph datasetGraph) {
		getMosaic().add(new Tessera(datasetGraph));
//		getMosaic().add(datasetGraph);
		return this;
	}
	
	/**
	 * Remove a datasetGraph from the Mosaic. 
	 */
//	public DatasetGraphMosaic remove(final DatasetGraph datasetGraph) {
//		getMosaic().remove(datasetGraph);
//		return this;
//	}
	
	/**
	 * Indirection for TransactionalDistributed.
	 * NB. If you call execute(Runnable) or submit(Callable<T>) and you need to reference the TransactionalDistributed, i.e. you need Thread affinity for the parent Transactional,
	 * you will need to hold a local final reference. Otherwise a new ThreadLocal will be created and a JenaException("No parent  transaction"); will be thrown.
	 * If null is returned from .get() a new TransactionalDistributed will be created.
	 */
	protected TransactionalDistributed getTransactionalDistributed() {
		TransactionalDistributed transactionalDistributed = transactional.get();
		if (transactionalDistributed == null) {
			transactionalDistributed = new TransactionalDistributed();
			transactional.set(transactionalDistributed);
			monitor.put(Thread.currentThread(), transactionalDistributed);
		}
		return transactionalDistributed;
	}

	/**
	 * Peek the current TransactionalDistributed, i.e. this can return null...
	 */
	protected TransactionalDistributed peekTransactionDistributed() {
		return transactional.get();
	}
	
	protected DatasetGraphShimWrite getShimWrite() {
		return this.shimWrite;
	}
	
	public <T> T rdfLoad(final Map<String, Object> context) {
		// TODO Write load to disk? Thread to perform work?
		sequentialMosaic()
			.forEach((datasetGraphTry) -> {

			});
		return (T) null;
	}
	
	/*
	 * DatasetGraph
	 */

	@Override
	public Graph getDefaultGraph() {
		return GraphView.createDefaultGraph(this);
	}

	@Override
	public Graph getGraph(final Node graphNode) {
		return GraphView.createNamedGraph(this, graphNode);
	}

	@Override
	public boolean containsGraph(final Node graphNode) {
		return mosaicAnyMatch((datasetGraph) -> {return datasetGraph.containsGraph(graphNode);});
	}

	@Override
	public void setDefaultGraph(Graph g) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addGraph(Node graphName, Graph graph) {
		// TODO Auto-generated method stub
	}

	@Override
	public void removeGraph(final Node graphName) {
		mosaicForEach((datasetGraph) -> {datasetGraph.removeGraph(graphName);});
	}

	@Override
	public Iterator<Node> listGraphNodes() {
		return mosaicFind((datasetGraph) -> {return stream(datasetGraph.listGraphNodes());}).iterator();
	}

	@Override
	public void add(Quad quad) {
		getShimWrite().add(quad);
	}

	@Override
	public void add(Node g, Node s, Node p, Node o) {
		getShimWrite().add(g, s, p, o);
	}

	@Override
	public void delete(final Quad q) {
		getShimWrite().delete(q);
	}

	@Override
	public void delete(final Node g, final Node s, final Node p, final Node o) {
		getShimWrite().delete(g, s, p, o);
	}

	@Override
	public void deleteAny(final Node g, final Node s, final Node p, final Node o) {
		getShimWrite().deleteAny(g, s, p, o);
	}
	
	@Override
	public Iterator<Quad> find() {
		return mosaicFind((datasetGraph) -> {return stream(datasetGraph.find());}).iterator();
	}

	@Override
	public Iterator<Quad> find(final Quad quad) {
		return mosaicFind((datasetGraph) -> {return stream(datasetGraph.find(quad));}).iterator();
	}

	@Override
	public Iterator<Quad> find(final Node g, final Node s, final Node p, final Node o) {
		return mosaicFind((datasetGraph) -> {return stream(datasetGraph.find(g, s, p, o));}).iterator();
	}

	@Override
	public Iterator<Quad> findNG(Node g, Node s, Node p, Node o) {
		return mosaicFind((datasetGraph) -> {return stream(datasetGraph.findNG(g, s, p, o));}).iterator();
	}

	@Override
	public boolean contains(final Node g, final Node s, final Node p, final Node o) {
		return mosaicAnyMatch((datasetGraph) -> {return datasetGraph.contains(g, s, p, o);});
	}

	@Override
	public boolean contains(final Quad quad) {
		return mosaicAnyMatch((datasetGraph) -> {return datasetGraph.contains(quad);});
	}

	@Override
	public void clear() {
		mosaicForEach((datasetGraph) -> {datasetGraph.clear();});
	}

	@Override
	public boolean isEmpty() {
		return mosaicAllMatch((datasetGraph) -> {return datasetGraph.isEmpty();});
	}

	@Override
	public Lock getLock() {
		return this.lock;
	}

	@Override
	public Context getContext() {
		return context;
	}

	@Override
	public long size() {
		return -1;
	}

	@Override
	public void close() {
		if (transactionCount.get() != 0) {
			throw new JenaException("Transaction active");
		}
		parallelMosaic().forEach(
			(tessera) -> {
				try {
					tessera.getDatasetGraph().close();
				} catch (final Exception exception) {
					LOGGER.warn("close", exception);
				}
			});
	}

	@Override
	public boolean supportsTransactions() {
		return true;
	}

	@Override
	public boolean supportsTransactionAbort() {
		return true;
	}

	/*
	 * Transactional
	 */
	
	@Override
	public void begin(final ReadWrite readWrite) {
		exceptionIfClosed();
		try {
			getTransactionalDistributed().begin(readWrite);
			if (readWrite.equals(ReadWrite.READ)) {
				readCount.incrementAndGet();
				getTopology().getMetric().<Integer>compute(Metric.READ, (v) -> {return (v == null ? 1 : v++);});
			} else {
				writeCount.incrementAndGet();
				getTopology().getMetric().<Integer>compute(Metric.WRITE, (v) -> {return (v == null ? 1 : v++);});
			}
		} finally {
			transactionCount.incrementAndGet();
		}
	}

	@Override
	public void commit() {
		exceptionIfClosed();
		getTransactionalDistributed().commit();
	}

	@Override
	public void abort() {
		exceptionIfClosed();
		getTransactionalDistributed().abort();		
	}

	@Override
	public void end() {
		exceptionIfClosed();
		try {
			final TransactionalDistributed transactionalDistributed = getTransactionalDistributed();
			if (transactionalDistributed.isIn(ReadWrite.READ)) {
				transactionalDistributed.end();
			} else if (transactionalDistributed.isIn(ReadWrite.WRITE)) {
				transactionalDistributed.abort();
				transactionalDistributed.end();
			}
		} finally {
			transactionCount.decrementAndGet();
		}
	}

	/**
	 * Answer if the calling thread is in a transaction.
	 */
	@Override
	public boolean isInTransaction() {
		return getTransactionalDistributed().isInTransaction();
	}

	/*
	 * Object
	 */
	

	@Override
	public String toString() {
		final StringBuilder text = new StringBuilder(1024);
		text.append("Context [" + getContext() + "]");
		text.append("\nTransactional\nAll [" + transactionCount + "] Read [" + readCount.get() + "] Write [" + writeCount.get() + "]");
		text.append("\n(ThreadLocal) [" + peekTransactionDistributed() + "]");
		text.append("\n(Monitor) [" + monitor.entrySet().stream().map((entry) -> {return entry.getKey().getName() + " " + entry.getValue();}).collect(Collectors.joining(",")) + "]");
		text.append("\nMosaic [\n" + getMosaic().stream().map((datasetGraph) -> {return datasetGraph.toString();}).collect(Collectors.joining("\n")) + "]");
		return text.toString();
	}
}
