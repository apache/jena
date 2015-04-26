package org.apache.jena.testing_framework;

import org.apache.jena.rdf.model.Resource;

public interface IResourceProducer<X extends Resource> extends INodeProducer<X> {
	/**
	 * Returns true if the Resource implementation supports non URI values
	 */
	boolean supportsAnonymous();
}