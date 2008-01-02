/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.optimizer.probability.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.engine.optimizer.util.Config;

/**
 * The implementation for the probabilistic data model based
 * on the Jena graph statistics handler. This implementation
 * provides accurate probabilities for S, P or O by exploiting
 * the information returned from the Jena graph statistics handler
 * about the number of triples matching either S, P or O.
 * The implementation allows probability calculation for single
 * triples but not for joined triples. Please note that for the 
 * implementation provided by this class it is required that the
 * ontology graph implements a Jena graph statistics handler.
 * 
 * @author Markus Stocker
 */

public class ProbabilityDataModel extends ProbabilityBase
{	
	private static Log log = LogFactory.getLog(ProbabilityDataModel.class) ;
	
	/**
	 * Create a probabilistic data model based on the ontology
	 * 
	 * @param dataModel
	 * @param config
	 */
	public void create(Model dataModel, Config config)
	{
		super.create(dataModel) ;
		init(config) ;
	}
	
	/**
	 * Load a probabilistic data model based on the ontology. This
	 * constructor is very similar to the create() constructor. The 
	 * only difference, is that this constructor is slightly more 
	 * efficient since instead to get the size of the model by
	 * iterating over the statements (like the create() constructor)
	 * it gets the size by the method model.size(). This method is
	 * not accurate in each case, especially for inference models.
	 * Hence if the exact size is required, the create() constructor
	 * should be used. Note that the size is used to get both
	 * probabilities and selectivities, hence the figures might
	 * be different. The index model param may be null in this case
	 * (no index model is required to run this probabilistic model).
	 * 
	 * @param dataModel
	 * @param indexModel
	 * @param config
	 */
	public void load(Model dataModel, Model indexModel, Config config)
	{
		super.load(dataModel) ;
		init(config) ;
	}
	
	/**
	 * The probability of a triple pattern which is calculated as the
	 * multiplication of the probabilities for S, P and O. For an unbound
	 * S, P or O, the probability is 1. Else, the graph statistics handler
	 * is used to calculate the probability of S, P and O. Note that this
	 * method requires 
	 * 
	 * @param triple
	 * @return double
	 */
	public double getProbability(Triple triple)
	{		
		Node subject = triple.getSubject() ;
		Node predicate = triple.getPredicate() ;
		Node object = triple.getObject() ;
		
		// The subject probability
		double sp = getProbability(subject, new Triple(subject, Node.ANY, Node.ANY)) ;
		// The predicate probability
		double pp = getProbability(predicate, new Triple(Node.ANY, predicate, Node.ANY)) ;
		// The object probability
		double op = getProbability(object, new Triple(Node.ANY, Node.ANY, object)) ;
		
		double p = -1d ;
		
		// If one of them is 0, believe it and return 0
		if (sp == 0d || pp == 0d || op == 0d)
			p = 0d ;
		else 
			p = check(getProbability(sp, pp, op)) ;
 		
 		log.debug("Probability: " + sp + " [" + subject + "]") ;
 		log.debug("Probability: " + pp + " [" + predicate + "]") ;
 		log.debug("Probability: " + op + " [" + object + "]") ;
		log.debug("Probability: " + p + " [" + triple + "]") ;
 		
 		return p ;
	}
	
	/**
	 * Return the exact selectivity of the triple pattern.
	 * Note that this method essentially executes the pattern
	 * as a SPARQL query, hence, it might not be suited to use
	 * in applications because of the time required for the 
	 * evaluation. The method is useful to test the accuracy 
	 * of the estimated probability.
	 * 
	 * @param triple
	 * @return double
	 */
	public double getSelectivity(Triple triple)
	{		
		double s = 1d ; 
	
		if (dataGraphSize > 0)
			s = new Long(selectivity.calculate(triple)).doubleValue() / dataGraphSize ;
		 
		log.debug("Selectivity: " + s + " [" + triple + "]") ;
		
		return s ;
	}
	
	/**
	 * Return the probability of joined triple patterns.
	 * The second parameter might be null, in which case 
	 * the probability of the single triple pattern is 
	 * returned. Please note that the implementation provided
	 * by the probabilistic data model does not support 
	 * probability computation for joined triple patterns.
	 * This method returns 1.0 for the probability of
	 * joined triple patterns.
	 * 
	 * @param triple1
	 * @param triple2
	 * @return double
	 */
	public double getProbability(Triple triple1, Triple triple2)
	{
		if (triple2 == null)
			return getProbability(triple1) ;
		
		double p = 1d ;
		
		log.debug("This model does not estimate joined probabilities: " + p + " [" + triple1 + ", " + triple2 + "]") ;
		
		return p ; 
	}
	
	/**
	 * Return the selectivity of joined triple patterns.
	 * Please note the comments for the methods getSelectivity(Triple)
	 * and getProbability(Triple, Triple).
	 * 
	 * @param triple1
	 * @param triple2
	 * @return double
	 */
	public double getSelectivity(Triple triple1, Triple triple2)
	{
		if (triple2 == null)
			return getSelectivity(triple1) ;
		
		double s = 1d ;
		
		if (squaredDataGraphSize > 0)
			s = new Long(selectivity.calculate(triple1, triple2)).doubleValue() / squaredDataGraphSize ; 
		
		log.debug("Selectivity: " + s + " [" + triple1 + ", " + triple2 + "]") ;
		
		return s ;
	}
	
	/*
	 * Return the probability of a node in a given triple.
	 * This method uses the Jena graph statistics handler
	 * for the probability estimation. Please note that
	 * the handler shouldn't be null.
	 */
	private double getProbability(Node node, Triple triple)
	{	
		double s = 1d ;
		
		if (node.isVariable())
			return s ;
		
		if (graphStatisticsHandler == null)
		{
			log.debug("GraphStatisticsHandler required!") ;
			return s ;
		}
		
		long size = graphStatisticsHandler.getStatistic(triple.getSubject(), triple.getPredicate(), triple.getObject()) ;
		
		if (dataGraphSize > 0)
			s = new Long(size).doubleValue() / dataGraphSize ;
		
		return s ;
	}
	
	/*
	 * The method initializes some fields used by the class
	 */
	private void init(Config config)
	{
		if (dataGraphSize > 0)
			minProbability = 1d / dataGraphSize ;
		else
			minProbability = 0d ;
		
		if (config != null)
		{
			if (!config.limitMinProbability())
				// The min probability should not be constrained
				this.minProbability = Double.MIN_VALUE ;
		}
		
		if (dataModel != null)
			isLoaded = true ;
		else
			log.debug("The probabilistic data model requires the model of the ontology") ;
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