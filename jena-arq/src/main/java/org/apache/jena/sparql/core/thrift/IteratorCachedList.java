package org.apache.jena.sparql.core.thrift;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

public class IteratorCachedList<E> extends IteratorCached<E> {

	protected List<E> cache = new LinkedList<E>();
	
	protected Iterator<E> iterator = Collections.emptyIterator();

	protected Boolean hasNext = null;
	
	public IteratorCachedList(Iterator<E> wrapped, int count) {
		super(wrapped, count);
	}
	
	@Override
	public boolean hasNext() {
		if (hasNext == null) {
			hasNext = iterator.hasNext();
			if (!hasNext) {
				cache.clear();
				int index = 0;
				while (wrapped.hasNext() && index++ < count) {
					cache.add(wrapped.next());
				}
				iterator = cache.iterator();
				hasNext = iterator.hasNext();
			}
		}
		return hasNext;
	}

	@Override
	public E next() {
		if (hasNext == null || !hasNext) {
			throw new NoSuchElementException();
		}
		hasNext = null;
		final E next = iterator.next();
		iterator.remove();
		return next;
	}
}
