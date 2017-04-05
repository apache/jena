package org.apache.jena.sparql.core.mosaic;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.jena.query.ReadWrite;
import org.apache.jena.shared.JenaException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to support a distributed Transactional.
 * 
 * Thread affinity is maintained by associating a Thread per DatasetGraph. 
 * 
 * A TransactionalDistributed should be declared via a ThreadLocal and thus provides a MRMW.
 * 
 * @author dick
 *
 */
public class TransactionalDistributed implements Transactional {

	private static final Logger LOGGER = LoggerFactory.getLogger(TransactionalDistributed.class);
	
	protected final ForkJoinPool forkJoinPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
	
	protected final String id = Thread.currentThread().toString();

	protected final AtomicInteger readCount = new AtomicInteger();
	
	protected final AtomicInteger writeCount = new AtomicInteger();
	
	protected volatile ReadWrite readWrite = null;
	
	protected final Set<DatasetGraph> children = ConcurrentHashMap.newKeySet(32);
	
	protected final ConcurrentMap<DatasetGraph, ThreadProxy> workers = new ConcurrentHashMap<>(32);

	protected class FJWT extends ForkJoinWorkerThread {

		public FJWT(final ForkJoinPool pool) {
			super(pool);
		}
		
	}
	
	public TransactionalDistributed() {
		super();
	}
	
	/**
	 * Execute the given Runnable.
	 */
	public void execute(final Runnable task) {
		try {
			forkJoinPool.submit(task).get();
		} catch (final Exception exception) {
			throw new JenaException(exception);
		}
	}
	
	/**
	 * Submit the given Callable.
	 */
	public <T> Future<T> submit(final Callable<T> task) {
		try {
			return forkJoinPool.submit(task);
		} catch (final Exception exception) {
			throw new JenaException(exception);
		}
	}
	
	/*
	 * Distributed transactional methods. The general goal is to maintain transactions on the child DatasetGraph's.  
	 */

	public ReadWrite getType() {
		return readWrite;
	}
	
	public boolean isIn(final ReadWrite compare) {
		return Objects.equals(readWrite, Objects.requireNonNull(compare));
	}
	
	protected Set<DatasetGraph> getChildren() {
		return children;
	}

	protected ConcurrentMap<DatasetGraph, ThreadProxy> getWorkers() {
		return workers;
	}

	/**
	 * Get the ThreadProxy for the given DatasetGraph.
	 */
	protected ThreadProxy getWorker(final DatasetGraph datasetGraph) {
		return getWorkers().computeIfAbsent(datasetGraph, (dg) -> {return new ThreadProxy();});
	}
	
	/**
	 * Submit the given Runnable to the correct ThreadProxy based on the given datasetGraph.
	 */
	public void execute(final DatasetGraph datasetGraph, final Runnable runnable) {
		getWorker(datasetGraph).execute(() -> {
			begin(datasetGraph);
			runnable.run();
		});
	}
	
	/**
	 * Submit the given Supplier to the correct ThreadProxy based on the given datasetGraph.
	 */
	public <T> Future<T> submit(final DatasetGraph datasetGraph, final Supplier<T> supplier) {
		return getWorker(datasetGraph).submit(() -> {
			begin(datasetGraph);
			return supplier.get();
		});
	}
	
	/**
	 * Begin a transaction on the given DatasetGraph.
	 */
	protected DatasetGraph begin(final DatasetGraph datasetGraph) {
		if (!isInTransaction()) {
			throw new JenaException("No parent transaction");
		}
		try {
			if (!children.contains(datasetGraph)) {
				if (datasetGraph.supportsTransactions()) {
					datasetGraph.begin(readWrite);
				}
				children.add(datasetGraph);
			}
			return datasetGraph;
		} catch (final Exception exception) {
			throw new JenaException(exception);
		}
	}
	
	/*
	 * Transactional
	 */
	
	@Override
	public void begin(final ReadWrite readWrite) {
		if (this.readWrite != null) {
			throw new JenaException("Already in a transaction " + this.readWrite);
		}
		this.readWrite = readWrite;
	}
	
	@Override
	public void commit() {
		List<Exception> threw = new LinkedList<>();
		children
			.forEach(datasetGraph -> {
				try {
					getWorker(datasetGraph).execute(() -> {
						if (datasetGraph.supportsTransactions()) {
							datasetGraph.commit();
						}
					});
				} catch (final Exception exception) {
					threw.add(exception);
				}
			});
		this.children.clear();
		this.readWrite = null;
		if (!threw.isEmpty()) {
			final JenaException jenaException = new JenaException();
			threw.forEach((exception) -> {jenaException.addSuppressed(exception);});
			throw jenaException;
		}
	}
	
	@Override
	public void abort() {
		List<Exception> threw = new LinkedList<>();
		children
			.forEach(datasetGraph -> {
				try {
					getWorker(datasetGraph).execute(() -> {
						if (datasetGraph.supportsTransactions() && datasetGraph.supportsTransactionAbort()) {
							datasetGraph.abort();
						} else {
							LOGGER.warn("Attempt to call Transactional.abort().", datasetGraph.getClass());
						}
					});
				} catch (final Exception exception) {
					threw.add(exception);
				}
			});
		this.children.clear();
		this.readWrite = null;
		if (!threw.isEmpty()) {
			final JenaException jenaException = new JenaException();
			threw.forEach((exception) -> {jenaException.addSuppressed(exception);});
			throw jenaException;
		}
	}
	
	@Override
	public void end() {
		if (readWrite != null && readWrite.equals(ReadWrite.WRITE)) {
			LOGGER.warn("End without commit/abort");
		}
		List<Exception> threw = new LinkedList<>();
		children
			.forEach(datasetGraph -> {
				try {
					getWorker(datasetGraph).execute(() -> {
						if (datasetGraph.supportsTransactions()) {
							datasetGraph.end();
						}
					});
				} catch (final Exception exception) {
					threw.add(exception);
				}
			});
		this.children.clear();
		this.workers.values().forEach(threadDelegate -> {
			threadDelegate.close();
		});
		this.workers.clear();
		this.readWrite = null;
		if (!threw.isEmpty()) {
			final JenaException jenaException = new JenaException();
			threw.forEach((exception) -> {jenaException.addSuppressed(exception);});
			throw jenaException;
		}
	}
	
	protected void common() {
	}
	
	@Override
	public boolean isInTransaction() {
		return readWrite != null;
	}

	@Override
	public String toString() {
		final StringBuilder text = new StringBuilder(1024);
		text.append("ID [" + id + "]");
		text.append("\nType [" + readWrite + "]");
		text.append("\nCounts [Read [" + readCount.get() + "] Write [" + writeCount.get() + "]]");
		text.append("\nChildren [\n" + getChildren().stream().map((datasetGraph) -> {return datasetGraph.getClass().getName();}).collect(Collectors.joining("\n")) + "]");
		text.append("\nWorkers [\n" + workers.entrySet().stream().<String>map(entry -> {return entry.getKey().getClass().toString() + " " + entry.getValue().toString();})
			.collect(Collectors.joining("\n")) + "]");
		return text.toString();
	}
	
	
}
