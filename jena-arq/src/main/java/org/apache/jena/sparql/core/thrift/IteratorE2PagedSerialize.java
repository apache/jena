package org.apache.jena.sparql.core.thrift;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Iterator;

public class IteratorE2PagedSerialize<E> extends IteratorE2Paged<E> {

	protected ObjectOutputStream objectOutputStream;
	
	public IteratorE2PagedSerialize(final Iterator<E> iterator) {
		super(iterator);
		try {
			objectOutputStream = new ObjectOutputStream(outputStreamPaged);
		} catch (IOException ioException) {
			throw new UnsupportedOperationException(ioException);
		}
	}

	@Override
	protected void writeElement(final E e) {
		try {
			objectOutputStream.writeUnshared(e);
		} catch (IOException ioException) {
			throw new UnsupportedOperationException(ioException);
		}
	}

}
