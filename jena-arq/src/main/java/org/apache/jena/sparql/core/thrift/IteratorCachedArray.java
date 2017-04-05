package org.apache.jena.sparql.core.thrift;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class IteratorCachedArray<E> extends IteratorCached<E> {

	protected final E[] cache;
	
	protected int limit = 0;
	
	protected int current = 0;
	
	@SuppressWarnings("unchecked")
	public IteratorCachedArray(Iterator<E> wrapped, int count) {
		super(wrapped, count);
		cache = ((E[]) new Object[count]);
	}

	@Override
	public boolean hasNext() {
		if (current == limit) {
			limit = 0;
			current = 0;
			while (wrapped.hasNext() && limit < count) {
				cache[limit] = wrapped.next();
				limit++;
			}
		}
		return (current < limit);
	}

	@Override
	public E next() {
		if (current == limit) {
			throw new NoSuchElementException();
		}
		final E next = cache[current];
		cache[current] = null;
		current++;
		return next;
	}

	@Override
	public String toString() {
		return super.toString() + " " + current + "/" + limit;
	}
}
