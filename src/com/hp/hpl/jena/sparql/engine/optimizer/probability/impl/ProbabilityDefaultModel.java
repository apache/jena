/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.optimizer.probability.impl;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.engine.optimizer.probability.Probability;
import com.hp.hpl.jena.sparql.engine.optimizer.util.Config;

/**
 * The probabilistic default model does nothing special but
 * it merges both the probabilistic data and index model.
 * In a nutshell, it uses the data model and if the data model
 * does not provide meaningful probability figures it uses
 * the index model. The probabilistic data model is not as 
 * powerful and versatile as the probabilistic index model since
 * it supports probability estimation only for single triple
 * patterns and only non-inference in-memory models (i.e.
 * Jena graphs that implement the GraphStatisticsHandler).
 * However, the probabilistic data model is more accurate 
 * than the index model when the graph statistics handler
 * is provided by the Jena graph. Hence, the implementation
 * provided by the probabilistic default model tries to
 * integrate the best from both worlds.
 * 
 * @author Markus Stocker
 */


public class ProbabilityDefaultModel implements Probability
{	
	// The probabilistic data model
	private ProbabilityDataModel probabilityDataModel = new ProbabilityDataModel() ;
	// The probabilistic index model
	private ProbabilityIndexModel probabilityIndexModel = new ProbabilityIndexModel() ;
	
	/**
	 * This constructor is for convenience and loads a 
	 * probabilistic default model by loading both the
	 * probabilistic data and index model. Note that
	 * the specialized RDF Jena model has to be provided.
	 * Please consider to build it previously with 
	 * the index() method provided by this class.
	 */
	public ProbabilityDefaultModel(Model dataModel, Model indexModel, Config config)
	{
		load(dataModel, indexModel, config) ;
	}
	
	/**
	 * Create the probabilistic default model by creating both
	 * the probabilistic data and index model. The difference
	 * to the load() method is that this method creates the index
	 * required for the probabilistic index model. If this 
	 * specialized RDF index (represented as a Jena model) is 
	 * available, it might be a better idea to use the load()
	 * method with the Jena model for the index in order to 
	 * save the time required to build the index. The full
	 * index is build in this case. 
	 * 
	 * @param dataModel
	 * @param config
	 */
	public void create(Model dataModel, Config config)
	{
		probabilityDataModel.create(dataModel, config) ;
		probabilityIndexModel.create(dataModel, config) ;
	}
	
	/**
	 * Load the probabilistic default model by loading both the
	 * probabilistic data and index model.
	 * 
	 * @param dataModel
	 * @param indexModel
	 * @param config
	 */
	public void load(Model dataModel, Model indexModel, Config config)
	{
		probabilityDataModel.load(dataModel, indexModel, config) ;
		probabilityIndexModel.load(dataModel, indexModel, config) ;
	}
	
	/**
	 * Create the specilized RDF index required for the 
	 * probabilistic index model with respect to the 
	 * configuration options provided.
	 * 
	 * @param dataModel
	 * @param config
	 * @return Model
	 */
	public Model index(Model dataModel, Config config)
	{
		return probabilityIndexModel.index(dataModel, config) ;
	}
	
	/**
	 * Create the default specialized RDF index
	 * 
	 * @param dataModel
	 * @return Model
	 */
	public Model index(Model dataModel)
	{
		return index(dataModel, new Config()) ;
	}
	
	/**
	 * The implementation of the Probability interface.
	 * The method returns the probability of the URI provided 
	 * by the resource as a subject in triple statements.
	 * If the probability returned by the data model is meaningless
	 * (i.e. lower than 0) then the probabilistic index model
	 * is used to return the probability. 
	 * 
	 * @param resource
	 * @return double
	 */
	public double getProbability(Resource resource)
	{
		double p = -1 ;

		if (probabilityDataModel.isLoaded())
			p = probabilityDataModel.getProbability(resource) ;
		
		if (p < 0)
			p = probabilityIndexModel.getProbability(resource) ;
		
		return p ;
	}
	
	/**
	 * Return the exact selectivity of the URI provided by the resource
	 * 
	 * @param resource
	 * @return double
	 */
	public double getSelectivity(Resource resource)
	{
		return probabilityIndexModel.getSelectivity(resource) ;
 	}
	
	/**
	 * Return the probability of the property
	 * 
	 * @param property
	 * @return double
	 */
	public double getProbability(Property property)
	{
		double p = -1;
		
		if (probabilityDataModel.isLoaded())
			p = probabilityDataModel.getProbability(property) ;
		
		if (p < 0)
			p = probabilityIndexModel.getProbability(property) ;
		
		return p ;
	}
	
	/**
	 * Return the exact selectivity of the property. Please note
	 * that the getSelectivity() methods are not estimations
	 * but are slow since they execute a SPARQL query for the pattern.
	 * 
	 * @param property
	 * @return double
	 */
	public double getSelectivity(Property property)
	{
		return probabilityIndexModel.getSelectivity(property) ;
	}
	
	/**
	 * Return get probability of the triple pattern
	 * 
	 * @param triple
	 * @return double
	 */
	public double getProbability(Triple triple)
	{		
		double p = -1 ;
		
		if (probabilityDataModel.isLoaded())
			p = probabilityDataModel.getProbability(triple) ;
		
		if (p < 0)
			p = probabilityIndexModel.getProbability(triple) ;
		
		return p ;
	}
	
	/**
	 * Return the exact selectivity of the triple pattern
	 * 
	 * @param triple
	 * @return double
	 */
	public double getSelectivity(Triple triple)
	{
		return probabilityIndexModel.getSelectivity(triple) ;
	}
	
	/**
	 * Return the probability of the joined triple patterns.
	 * Please note that the probabilistic data model does not support
	 * the probability calculation for joined triple patterns, hence,
	 * the probabilistic index model is used. 
	 * 
	 * @param triple1
	 * @param triple2
	 * @return double
	 */
	public double getProbability(Triple triple1, Triple triple2)
	{
		if (triple2 == null)
			return getProbability(triple1) ;
		
		return probabilityIndexModel.getProbability(triple1, triple2) ;
	}
	
	/**
	 * Return the exact selectivity of the joined triple patterns.
	 * 
	 * @param triple1
	 * @param triple2
	 * @return double
	 */
	public double getSelectivity(Triple triple1, Triple triple2) 
	{
		if (triple2 == null)
			return getSelectivity(triple1) ;
		
		return probabilityIndexModel.getSelectivity(triple1, triple2) ;
	}
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