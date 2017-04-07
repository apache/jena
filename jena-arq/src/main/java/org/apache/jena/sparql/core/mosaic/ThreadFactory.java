package org.apache.jena.sparql.core.mosaic;

public interface ThreadFactory {
	
	static String nameFor(final Class<?> c, final String suffix) {
		return c.getName() + "[" + suffix + "]";
	}

	static Thread user(final Runnable runnable, final String name) {
        final Thread thread = new Thread(runnable, name);
		return thread;
	}

	static Thread daemon(final Runnable runnable, final String name) {
        final Thread thread = new Thread(runnable, name);
		thread.setDaemon(true);
		return thread;
	}
	
	static void waitFor(final Runnable runnable) {
		final Thread thread = new Thread(runnable);
		thread.start();
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
