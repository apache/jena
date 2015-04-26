package org.apache.jena.testing_framework;

import org.apache.jena.rdf.model.Container;
import org.apache.jena.rdf.model.Resource;

public interface IContainerProducer<T extends Container> extends
		IResourceProducer<T> {

	/**
	 * The Resource identifying the continer type. e.g. RDF.seq
	 */
	Resource getContainerType();

	/**
	 * The class of the continaer. e.g. Seq.class
	 */
	Class<? extends Container> getContainerClass();

}
