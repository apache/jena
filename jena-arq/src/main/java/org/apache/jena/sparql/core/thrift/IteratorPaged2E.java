package org.apache.jena.sparql.core.thrift;

import java.util.Iterator;

public abstract class IteratorPaged2E<E> implements Iterator<E> {

	protected InputStreamPaged inputStreamPaged;
	
	protected E next = null;
	
	public IteratorPaged2E(final InputStreamPaged inputStreamPaged) {
		super();
		this.inputStreamPaged = inputStreamPaged;
	}

	@Override
	public abstract boolean hasNext();

	@Override
	public E next() {
		return next;
	}

}
