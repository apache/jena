package org.apache.jena.sparql.core.mosaic;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ForkJoinPool;

import org.apache.jena.query.ReadWrite;
import org.apache.jena.shared.JenaException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionalMosaic implements Transactional {

	private static final Logger LOGGER = LoggerFactory.getLogger(TransactionalMosaic.class);
	
	protected static class Transaction {
		
		private volatile ReadWrite type = null;
		
		private final Set<? extends Transactional> children = ConcurrentHashMap.newKeySet(32);
		
		private final ForkJoinPool forkJoinPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
		
		private final ConcurrentMap<DatasetGraph, ThreadProxy> workers = new ConcurrentHashMap<>(32);
		
		public ReadWrite getType() {
			return type;
		}
		
		public Set<? extends Transactional> getChildren() {
			return children;
		}

		@Override
		public String toString() {
			return String.format("type=[%s] children=[%s]", getType(), getChildren());
		}
	}
	
	protected final ThreadLocal<Transaction> transaction = ThreadLocal.withInitial(Transaction::new);

	/**
	 * Get the per thread Transaction.
	 * @return Transaction
	 */
	protected Transaction getTransaction() {
		return transaction.get();
	}
	
	public boolean isIn(final ReadWrite readWrite) {
		return Objects.equals(getTransaction().getType(), readWrite);
	}

	public boolean isInRead() {
		return isIn(ReadWrite.READ);
	}

	public boolean isInWrite() {
		return isIn(ReadWrite.WRITE);
	}
	
	@Override
	public void begin(final ReadWrite readWrite) {
		if (isInTransaction()) {
			throw new JenaException("Already in a transaction " + getTransaction());
		}
		getTransaction().type = readWrite;
	}

	@Override
	public void commit() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void abort() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void end() {
		if (isInWrite()) {
			LOGGER.warn("End without commit or abort");
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

	@Override
	public boolean isInTransaction() {
		return getTransaction().getType() != null;
	}

}
