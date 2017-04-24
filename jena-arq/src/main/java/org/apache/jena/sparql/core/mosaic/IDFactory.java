package org.apache.jena.sparql.core.mosaic;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Convenience class to generate ID's.
 * 
 * @author dick
 *
 */
public class IDFactory {

	/**
	 * Indirection to UUID.randomUUID().toString().
	 */
	public static String createUUID() {
		return UUID.randomUUID().toString();
	}

	protected static final String JVM_ID = createUUID();
	
	/**
	 * Indirection to return JVM_ID.
	 */
	public static String jvmID() {
		return JVM_ID;
	}
	
	public static IDFactory valueOf(final Class<?> c) {
		return new IDFactory(c.getName() + ".");
	}
	
	public static boolean isLocal(final String id) {
		return jvmID().equals(id);
	}
	
	protected final String prefix;

	protected final AtomicInteger sequence;

	public IDFactory(final String prefix) {
		super();
		this.prefix = prefix;
		this.sequence = new AtomicInteger();
	}

	public String next() {
		return prefix + sequence.incrementAndGet();
	}
	
	public String suffix(final String suffix) {
		return prefix + suffix;
	}

	@Override
	public String toString() {
		return prefix;
	}
}
