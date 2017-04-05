package org.apache.jena.sparql.core.mosaic;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Thread proxy which performs actions using one thread (the proxy).
 * Internally an ExecutorService with one Thread is used with the action methods execute/submit(Runnable) and submit(Callable<T>).
 * Calls to the action methods are FIFO actioned through the LinkedBlockingQueue of the ExecutorService.
 * Useful where Thread affinity is required in a parallel processing environment, e.g. .parallelStream().flatMap(...)
 * 
 * @author dick
 *
 */
public class ThreadProxy {

	private static final Logger LOGGER = LoggerFactory.getLogger(ThreadProxy.class);
	
	protected static final IDFactory ID_FACTORY = IDFactory.valueOf(ThreadProxy.class);
	
	protected static final ThreadGroup THREAD_GROUP = new ThreadGroup(ThreadProxy.class.getName());
	
	protected final String id;
	
	protected final String createdBy;
	
	protected final ExecutorService executorService;
	
	protected final AtomicInteger executeCount;
	
	protected final AtomicInteger submitCount;
	
	protected final AtomicInteger exceptionCount;
	
	public ThreadProxy() {
		super();
		id = ID_FACTORY.next();
		createdBy =  Thread.currentThread().getName();
		executorService = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(),
            new ThreadFactory() {
				protected final IDFactory idFactory = new IDFactory(id);
				@Override
				public Thread newThread(final Runnable runnable) {
					final Thread thread = new Thread(THREAD_GROUP, runnable, idFactory.next());
					thread.setDaemon(true);
					return thread;
				}
			}
		);
		executeCount = new AtomicInteger();
		submitCount = new AtomicInteger();
		exceptionCount = new AtomicInteger();
	}
	
	/**
	 * Execute the given Runnable.
	 */
	public void execute(final Runnable runnable) {
		executeCount.incrementAndGet();
		executorService.execute(runnable);
	}

	/**
	 * Submit the given Runnable returning a Future<?> which will return null via .get().
	 * The call to get() will block so useful to wait until the Runnable has completed.
	 */
	public Future<?> submit(final Runnable runnable) {
		submitCount.incrementAndGet();
		return executorService.submit(runnable);
	}

	/**
	 * Submit the given Callable<T> returning a Future<T>.
	 * submit(() -> {return ...;}).get();
	 */
	public <T> Future<T> submit(final Callable<T> supplier) {
		submitCount.incrementAndGet();
		return executorService.submit(supplier);
	}

	public void close() {
		executorService.shutdown();
	}

	@Override
	public String toString() {
		return id + " Executor Service [" + executorService.toString() + "]";
	}
}
