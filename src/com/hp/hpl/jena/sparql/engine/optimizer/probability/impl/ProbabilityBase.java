/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.optimizer.probability.impl;

import com.hp.hpl.jena.graph.GraphStatisticsHandler;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.engine.optimizer.probability.Probability;
import com.hp.hpl.jena.sparql.engine.optimizer.probability.impl.Selectivity;

/**
 * Abstract class for probabilistic models
 * 
 * @author Markus Stocker
 */

public abstract class ProbabilityBase implements Probability 
{
	/* The probability of one triple for the specific ontology */
	protected double minProbability = 0d ;
	/* The number of triples for the ontology */
	protected long dataGraphSize = 0L ;
	/* The size of the cartesian product of two triples */
	protected long squaredDataGraphSize = 0L ;
	/* The Jena model of the ontology */
	protected Model dataModel = null ;
	/* The Jena model of the specialized index */
	protected Model indexModel = null ;
	/* An object which allows the exact selectivity computation by executing SPARQL queries */
	protected Selectivity selectivity = null ;
	/* The Jena graph of the ontology */
	protected Graph dataGraph = null ;
	/* Flag to check whether or not the framework is loaded */
	protected boolean isLoaded = false ;
	/* The Jena graph statistics handler for accurate size information about SPO */
	protected GraphStatisticsHandler graphStatisticsHandler = null ;
	
	/**
	 * Create a probabilistic model based on the ontology
	 * 
	 * @param dataModel
	 */
	public void create(Model dataModel) 
	{ 
		if (dataModel == null)
			return ;
		
		this.dataModel = dataModel ;
		this.dataGraph = dataModel.getGraph();
		this.dataGraphSize = getSize(dataModel) ;
		this.squaredDataGraphSize = dataGraphSize * dataGraphSize ;
		this.graphStatisticsHandler = dataGraph.getStatisticsHandler() ;
		this.selectivity = new Selectivity(dataModel) ;
	}
	
	/**
	 * Load a probabilistic model based on the ontology
	 * 
	 * @param dataModel
	 */
	public void load(Model dataModel) 
	{		
		if (dataModel == null)
			return ;
		
		this.dataModel = dataModel ;
		this.dataGraph = dataModel.getGraph() ;
		this.dataGraphSize = dataGraph.size() ;
		this.squaredDataGraphSize = dataGraphSize * dataGraphSize ;
		this.graphStatisticsHandler = dataGraph.getStatisticsHandler() ;
		this.selectivity = new Selectivity(dataModel) ;
	}
	
	/** To implement by sub classes */
	public abstract double getProbability(Triple triple) ;
	
	/** To implement by sub classes */
	public abstract double getSelectivity(Triple triple) ;
	
	/** To implement by sub classes */
	public abstract double getProbability(Triple triple1, Triple triple2) ;
	
	/** To implement by sub classes */
	public abstract double getSelectivity(Triple triple1, Triple triple2) ;
	
	/** 
	 * Executes the probability method for the corresponding triple, 
	 * i.e. <Node.ANY, Property.URI, Node.ANY> 
	 * 
	 * @return double
	 * */
	public double getProbability(Property property)
	{ return getProbability(new Triple(Node.ANY, property.asNode(), Node.ANY)) ; }
	
	/** 
	 * Executes the selectivity method for the corresponding triple, 
	 * i.e. <Node.ANY, Property.URI, Node.ANY> 
	 * 
	 * @return double
	 * */
	public double getSelectivity(Property property)
	{ return getSelectivity(new Triple(Node.ANY, property.asNode(), Node.ANY)) ; }
	
	/** 
	 * Executes the probability method for the corresponding triple,
	 * i.e. <Resource.URI, Node.ANY, Node.ANY>
	 * 
	 * @return double
	 */ 
	public double getProbability(Resource resource)
	{ return getProbability(new Triple(resource.asNode(), Node.ANY, Node.ANY)) ; }
	
	/**
	 * Executes the selectivity method for the corresponding triple,
	 * i.e. <Resource.URI, Node.ANY, Node.ANY>
	 * 
	 * @return double
	 */
	public double getSelectivity(Resource resource)
	{ return getSelectivity(new Triple(resource.asNode(), Node.ANY, Node.ANY)) ; }
	
	/**
	 * The probability for a single triple
	 * 
	 * @return double
	 */
	public double getMinProbability()
	{ return minProbability ; }
	
	/**
	 * The size of the ontology, i.e. the number of statements
	 * 
	 * @return long
	 */
	public long getDataGraphSize()
	{ return dataGraphSize ; }
	
	/** 
	 * The size of the cartesian product of two unbound variable triples 
	 * 
	 * @return double
	 */
	public long getSquaredDataGraphSize() 
	{ return squaredDataGraphSize ; }
	
	/**
	 * Returns true if the probabilistic framework is loaded
	 * 
	 * @return boolean
	 */
	public boolean isLoaded()
	{ return isLoaded ; }

	
	/*
	 * The probability for a triple with SPO, modeled as 
	 * the product of the probability for S, P and O
	 * 
	 * @param sp
	 * @param pp
	 * @param op
	 * @return double
	 */
	protected double getProbability(double sp, double pp, double op)
	{
		return sp * pp * op ;
	}
	
	/*
	 * Check if the probability is lower than the min possible probability
	 * i.e. the selectivity of a single triple
	 * 
	 * @return double
	 */
	protected double check(double p)
	{
		if (p < minProbability)
			return minProbability ;

		return p ;
	}
	
	/*
	 * Returns the exact size of a model by counting the statements
	 * 
	 * @return long
	 */
	private long getSize(Model model)
	{
		long size = 0L ;
		
		StmtIterator stmtIter = model.listStatements() ;
		
		while (stmtIter.hasNext())
		{
			stmtIter.next() ;
			size++ ;
		}
		
		stmtIter.close() ;
		
		return size ;
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