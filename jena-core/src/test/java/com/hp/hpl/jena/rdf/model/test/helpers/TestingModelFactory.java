package com.hp.hpl.jena.rdf.model.test.helpers;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.shared.PrefixMapping ;

/**
 * Interface that defines the Testing Model Factory.
 * 
 * Implementations of this class will produce models that are to be tested by the
 * AbstractTestPackage implementations.
 */
public interface TestingModelFactory
{
	abstract public Model createModel();

	abstract public PrefixMapping getPrefixMapping();

	abstract public Model createModel( Graph base );
}
