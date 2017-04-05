package org.apache.jena.sparql.core.thrift;

import java.util.Iterator;

public abstract class IteratorCached<E> implements Iterator<E> {

	protected Iterator<E> wrapped;
	
	protected int count;
	
	public IteratorCached(final Iterator<E> wrapped, final int count) {
		super();
		this.wrapped = wrapped;
		this.count = count;
	}

	@Override
	public String toString() {
		return wrapped + " " + count;
	}
}
