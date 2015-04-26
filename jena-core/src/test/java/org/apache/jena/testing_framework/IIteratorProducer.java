package org.apache.jena.testing_framework;

import java.util.List;

import org.apache.jena.util.iterator.ExtendedIterator;

public interface IIteratorProducer<T> {

	/**
	 * Get a new instance of the iterator.
	 * 
	 * @return
	 */
	public ExtendedIterator<T> newInstance();

	/**
	 * Clean up after a test
	 */
	public void cleanUp();

	/**
	 * The list of items found in the iterator. Does not have to be in order.
	 * 
	 * @return
	 */
	public List<T> getList();

	/**
	 * True if delete is supported by the iterator
	 * 
	 * @return
	 */
	public boolean supportsDelete();

	/**
	 * True if this is an iterator on a copy so that delete works but getting a
	 * new copy for the iterator test will return the original list.
	 * 
	 * @return
	 */
	public boolean isCopy();
}
