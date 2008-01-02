/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.optimizer.probability;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.engine.optimizer.util.Config;
import com.hp.hpl.jena.sparql.engine.optimizer.probability.impl.ProbabilityDataModel;
import com.hp.hpl.jena.sparql.engine.optimizer.probability.impl.ProbabilityIndexModel;
import com.hp.hpl.jena.sparql.engine.optimizer.probability.impl.ProbabilityDefaultModel;

/**
 * A factory for probabilistic models. There are currently three models:
 * (1) the default model (2) the index model and (3) the data model.
 * The default model is a wrapper around both the index and the data
 * model. Essentially, it merges the *best* of both models.
 * The index model requires a Jena model of the specialized 
 * index to support joined triple pattern probability calculation.
 * The data model requires the Jena model of the queried ontology.
 * Please note that the data model returns probabilities only if
 * the corresponding graph implements the GraphStatisticsHandler.
 * This is currently true only for non-inference in-memory models.
 * The probabilities returned by a Jena data model which implements
 * the GraphStatisticsHandler are the most accurate (i.e. more
 * accurate than the probabilities returned by the index model.
 * However, the data model allows probability calculation for
 * triple patterns only (and only for non-inference in-memory 
 * Jena models) whereas the index model allows probability calculation
 * for joined triple patterns and virtually for any Jena model.
 * The index model is however less accurate than the data model
 * for triple pattern probability calculation.
 * 
 * @author Markus Stocker
 */

public class ProbabilityFactory
{	
	/**
	 * Create a probabilistic index model by creating first the
	 * required index. Note that building the index might
	 * required a significant amount of time and resources.
	 * If the index level is 0, a lightweight index
	 * is constructed which supports the probability estimation
	 * of triple patterns only. If the index level is > 0 (i.e. 1)
	 * the full index is constructed which supports the
	 * probability estimation of joined triple patterns.
	 * Please note that the full index might require some time
	 * depending on the number of distinct properties defined 
	 * in the ontology. The third parameter is a set of Jena
	 * Property objects which are excluded during indexing.
	 * 
	 * @param dataModel
	 * @param config
	 * @return Probability
	 */
	public static Probability createIndexModel(Model dataModel, Config config)
	{ 
		ProbabilityIndexModel probability = new ProbabilityIndexModel() ;
		probability.create(dataModel, config) ;
		
		return probability ;
	}
	
	/**
	 * Create the probabilistic index model with the full index.
	 * 
	 * @param dataModel
	 * @return Probability
	 */
	public static Probability createIndexModel(Model dataModel)
	{
		return createIndexModel(dataModel, new Config()) ;
	}
	
	/**
	 * Create and return the specialized Jena model of the index
	 * required for the probabilistic framework. This method may be used
	 * to create the index once and store it to a file (by using Model.write()).
	 * The index may be loaded and used for future use of the probabilistic
	 * framework (which is faster than to always create the index model).
	 * The method allows to specify the index level (0 for a lightweight 
	 * index, >0 for the full index). Please note that the model returned
	 * by the method is a Jena model of the specialized index (not 
	 * a probabilistic model), hence, it cannot be used to query
	 * for probabilities. However, you can load the Jena index model 
	 * by using the loadIndexModel() method provided by the factory.
	 * The third parameter exProperty is a set of Jena Property objects
	 * which have to be ignored during indexing. For instance, if this 
	 * set contains the RDF.type property, no statistics are generated
	 * for it (no frequencies, no histograms, no joined patterns). This
	 * parameter may be null.
	 * 
	 * @param dataModel
	 * @param config
	 * @return Model
	 */
	public static Model createIndex(Model dataModel, Config config)
	{
		ProbabilityIndexModel probability = new ProbabilityIndexModel() ;
		
		return probability.index(dataModel, config) ;
	}
	
	/**
	 * Create and return the full specialized Jena model of the index
	 * required for the probabilistic framework.
	 * 
	 * @param dataModel
	 * @return Model
	 */
	public static Model createIndex(Model dataModel)
	{
		return createIndex(dataModel, new Config()) ;
	}
	
	/**
	 * Create and return the full specialized Jena model of the index
	 * required for the probabilistic framework. 
	 * 
	 * @param dataGraph
	 * @return Model
	 */
	public static Model createIndex(Graph dataGraph, Config config)
	{
		return createIndex(ModelFactory.createModelForGraph(dataGraph), config) ;
	}
	
	/**
	 * Create and return the full specialized Jena model of the index
	 * required for the probabilistic framework. The index level 1 is
	 * used.
	 * 
	 * @param dataGraph
	 * @return Model
	 */
	public static Model createIndex(Graph dataGraph)
	{
		return createIndex(ModelFactory.createModelForGraph(dataGraph), new Config()) ;
	}
	
	/**
	 * Load a probabilistic default model by giving the Jena model of
	 * the queried ontology and the Jena model of the specialized index.
	 * The specialized index has to be constructed first (see the 
	 * createIndex() method provided by the factory). 
	 * 
	 * @param dataModel
	 * @param indexModel
	 * @param config
	 * @return Probability
	 */
	public static Probability loadDefaultModel(Model dataModel, Model indexModel, Config config)
	{
		Probability probability = new ProbabilityDefaultModel(dataModel, indexModel, config) ;
		
		return probability ;
	}
	
	/**
	 * Load a probabilistic default model by giving the Jena graph of 
	 * the queried ontology and the Jena model of the specialized index.
	 * The specialized index has to be constructed first (see the 
	 * createIndex() method provided by the factory).
	 *  
	 * @param dataGraph
	 * @param indexModel
	 * @param config
	 * @return Probability
	 */
	public static Probability loadDefaultModel(Graph dataGraph, Model indexModel, Config config)
	{		
		return loadDefaultModel(ModelFactory.createModelForGraph(dataGraph), indexModel, config) ;
	}
	
	/**
	 * Create a probabilistic default model by giving the Jena model of
	 * the queried ontology. The required index is automatically built.
	 * The full index is created (index level 1)
	 * 
	 * @param dataModel
	 * @param config
	 * @return Probability
	 */
	public static Probability createDefaultModel(Model dataModel, Config config)
	{
		Model indexModel = ProbabilityFactory.createIndex(dataModel) ;
		Probability probability = new ProbabilityDefaultModel(dataModel, indexModel, config) ;
		
		return probability ;
	}
	
	/**
	 * Create a probabilistic default model by giving the Jena model of
	 * the queried ontology. The required index is automatically built.
	 * The full index is created (index level 1)
	 * 
	 * @param dataGraph
	 * @param config
	 * @return Probability
	 */
	public static Probability createDefaultModel(Graph dataGraph, Config config)
	{		
		return createDefaultModel(ModelFactory.createModelForGraph(dataGraph), config) ;
	}
	
	/**
	 * Load a probabilistic index model by giving the Jena model of
	 * the queried ontology and the Jena model of the specialized index.
	 * The probabilistic model returned is an instance of ProbabilityIndexModel
	 * and allows probability calculation of joined triple patterns.
	 * 
	 * @param dataModel
	 * @param indexModel
	 * @param config
	 * @return Probability
	 */
	public static Probability loadIndexModel(Model dataModel, Model indexModel, Config config)
	{
		ProbabilityIndexModel probability = new ProbabilityIndexModel() ;
		probability.load(dataModel, indexModel, config) ;
		
		return probability ;
	}
	
	/**
	 * Load a probabilistic index model by giving the Jena graph of 
	 * the queried ontology and the Jena model of the specialized index.
	 * The probabilistic model returned is an instance of ProbabilityIndexModel
	 * and allows probability calculation of joined triple patterns.
	 * 
	 * @param dataGraph
	 * @param indexModel
	 * @param config
	 * @return Probability
	 */
	public static Probability loadIndexModel(Graph dataGraph, Model indexModel, Config config)
	{
		return loadIndexModel(ModelFactory.createModelForGraph(dataGraph), indexModel, config) ;
	}
	
	
	/**
	 * Load a probabilistic data model by giving the Jena model of
	 * the queried ontology. Please note that the Jena graph corresponding
	 * to the model needs to implement a GraphStatisticsHandler which
	 * returns meaningful information about the SPO statistics of the graph.
	 * Currently this is the case only for non-inference in-memory models.
	 * The probabilistic model returned is an instance of ProbabilityDataModel
	 * and allows probability calculation of triple patterns only.
	 * 
	 * @param dataModel
	 * @param config
	 * @return Probability
	 */
	public static Probability loadDataModel(Model dataModel, Config config)
	{ 
		ProbabilityDataModel probability = new ProbabilityDataModel() ;
		probability.load(dataModel, null, config) ;
	
		return probability ;
	}
	
	/**
	 * Load a probabilistic data model by giving the Jena graph of
	 * the queried ontology. Please note that the Jena graph needs
	 * to implement a GraphStatisticsHandler which returns meaningful
	 * information about the SPO statistics of the graph.
	 * Currently this is the case for non-inference in-memory models only.
	 * The probabilistic model returned is an instance of ProbabilityDataModel
	 * and allows probability calculation of triple patterns only.
	 * 
	 * @param dataGraph
	 * @param config
	 * @return Probability
	 */
	public static Probability loadDataModel(Graph dataGraph, Config config)
	{
		return loadDataModel(ModelFactory.createModelForGraph(dataGraph), config) ;
	}
}


/*
 *  (c) Copyright 2004, 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
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