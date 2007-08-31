/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.optimizer.probability;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.engine.optimizer.util.Config;

/**
 * The interface for probabilistic models. Currently there are 
 * two implementations: (1) the probabilistic index model and
 * (2) the probabilistic data model.
 * 
 * @author Markus Stocker
 * @version $Id$
 */

public interface Probability 
{	
	/**
	 * Create a probabilistic model given the queried data ontology 
	 * and the configuration settings.
	 * 
	 * @param dataModel
	 * @param config
	 */
	public void create(Model dataModel, Config config) ;
	
	/**
	 * Load a probabilistic model given the queried data ontology
	 * the specilized RDF index model and the configuration settings.
	 * For probabilistic models without the specialized RDF index 
	 * the indexModel can be null.
	 * 
	 * @param dataModel
	 * @param indexModel
	 * @param config
	 */
	public void load(Model dataModel, Model indexModel, Config config) ;
	
	/**
	 * Return the probability of the property
	 * 
	 * @param property
	 * @return double
	 */
	public double getProbability(Property property) ;
	
	/**
	 * Return the selectivity of the property. Please note
	 * that the selectivity is the exact subset of triples
	 * matching the property normalized by the total number 
	 * of triples. Further note that this method essentially
	 * executes a SPARQL query to get the exact selectivity
	 * which means that it is potentially slow. Use this method
	 * to compare the selectivity with the probability to get
	 * a metric for the accuracy of the probability calculation.
	 * 
	 * @param property
	 * @return double
	 */
	public double getSelectivity(Property property) ;
	
	/**
	 * Get the probability of the URI of a resource. 
	 * Note that this is the same as the probability of the triple
	 * <resource.getURI()> ?p ?o
	 */
	public double getProbability(Resource resource) ;
	
	/**
	 * Return the exact selectivity of the resource URI
	 * 
	 * @param resource
	 * @return double
	 */
	public double getSelectivity(Resource resource) ;
	
	/**
	 * Return the probability of the triple. Please 
	 * note that the accuracy of this method depends
	 * on the implementation.
	 * 
	 * @param triple
	 * @return double
	 */
	public double getProbability(Triple triple) ;
	
	/** 
	 * Return the selectivity of the triple
	 * 
	 * @param triple
	 * @return double
	 */
	public double getSelectivity(Triple triple) ;
	
	/**
	 * Return the probability of the joined triple patterns,
	 * i.e. the conditional probabilitity P(triple2|triple1). 
	 * Please note that it depends on the implementation if 
	 * this method returns both a meaningful and an accurate 
	 * probability. 
	 * 
	 * @param triple1
	 * @param triple2
	 * @return double
	 */
	public double getProbability(Triple triple1, Triple triple2) ;
	
	/**
	 * Return the selectivity for the joined triple patterns.
	 * 
	 * @param triple1
	 * @param triple2
	 * @return double
	 */
	public double getSelectivity(Triple triple1, Triple triple2) ;
}


/*
 *  (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 *  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */