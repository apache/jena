package org.apache.jena.sparql.core.thrift;

import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class IteratorE2Paged<E> implements Iterator<Page> {

	protected Iterator<E> iterator = null;
	
	protected Page next = null;

	protected OutputStreamPaged outputStreamPaged = new OutputStreamPaged();
	
	public IteratorE2Paged(final Iterator<E> iterator) {
		super();
		this.iterator = iterator;
	}

	protected abstract void writeElement(final E e);

	/**
	 * Consume the iterator until we have filled the page we started on.
	 */
	protected void createPages() {
		while (iterator.hasNext()) {
			Page page = outputStreamPaged.writePage();
			writeElement(iterator.next());
			if (page != outputStreamPaged.pages().peekLast()) {
				break;
			}
		}
	}
	
	@Override
	public boolean hasNext() {
		/*
		 * We are interested in two pages states.
		 * 		size = 0 i.e. it's empty.
		 * 		size = 1 and the page buffer has space remaining.
		 */
		if ((outputStreamPaged.pages().size() == 0) || (outputStreamPaged.pages().size() == 1 && outputStreamPaged.pages().peekFirst().getBuffer().hasRemaining())) {
			createPages();
		}
		next = outputStreamPaged.pages().pollFirst();
		if (next != null) {
			next.getBuffer().flip();
		}
		return (next != null);
	}

	@Override
	public Page next() {
		if (next == null) {
			throw new NoSuchElementException();
		}
		final Page page = next;
		next = null;
		return page;
	}

	@Override
	public String toString() {
		return outputStreamPaged.toString();
	}

}
