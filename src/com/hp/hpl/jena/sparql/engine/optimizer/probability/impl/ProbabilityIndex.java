/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.optimizer.probability.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.engine.optimizer.probability.Histogram;
import com.hp.hpl.jena.sparql.engine.optimizer.probability.impl.IndexModelAccess;
import com.hp.hpl.jena.sparql.engine.optimizer.probability.impl.Pattern;
import com.hp.hpl.jena.sparql.engine.optimizer.probability.impl.Selectivity;
import com.hp.hpl.jena.sparql.engine.optimizer.util.Config;
import com.hp.hpl.jena.sparql.engine.optimizer.core.BasicPatternJoin;

/**
 * This class implements the specialized index required for
 * the probabilistic index model as a Java object. The IndexModelAccess
 * class is used to access the RDF graph and extract statistical
 * information from or store it to it. The first thing to do
 * when using this class is to either load() or create() the index.
 * 
 * @author Markus Stocker
 */

public class ProbabilityIndex 
{
	/*
	 * The index level
	 * 0 = triple pattern probability only
	 * >0 = complete (default)
	 */
	private int level = 1 ;
	// The indexed number of triples of the ontology, this might be out-of-date 
	private long indexedSize = 0L ;
	// The indexed number of resources of the ontology, this might be out-of-date too
	private long indexedNumRes = 0L ;
	/*
	 * The subject - subject join size: ?s ?p1 ?o1 . ?s ?p2 ?o2
	 * This is the upper bound of the size of joined triple patterns
	 */
	private long ssSize = -1L, soSize = -1L, osSize = -1L, ooSize = -1L ;
	// The class for selectivity calculation, i.e. responsible for executing SPARQL queries
	private Selectivity selectivity = null ;
	// The Jena model for the specialized RDF index
	private Model indexModel = null ;
	// The class for accessing the index model
	private IndexModelAccess ga = new IndexModelAccess() ;
	// A Java data structure for the statistics of occurrences of properties
	private Map properties = new HashMap() ; // Map<Property, Long>
	// A Java data structure for the histograms of properties
	private Map histograms = new HashMap() ; // Map<Property, Histogram>
	// A Java data structure for the statistics of joined triple patterns
	private Map patterns = new HashMap() ; // Map<Pattern, Long>
	// A set of Jena Property objects that are excluded in the index
	private Set exclude = new HashSet() ; // Set<Property>
	private static Log log = LogFactory.getLog(ProbabilityIndex.class) ;
	
	/**
	 * Load a specialized RDF index as Jena model from the RDF representation
	 * into the memory
	 * 
	 * @param indexModel
	 */
	public void load(Model indexModel)
	{
		this.indexModel = indexModel ;
		
		ga.load(indexModel) ;
		indexedSize = ga.getSize() ;
		level = ga.getLevel() ;
		indexedNumRes = ga.getResources() ;
		properties = ga.getProperties() ;
		histograms = ga.getHistograms() ;
		patterns = ga.getPatterns() ;
		ssSize = ga.getSSSize() ;
		soSize = ga.getSOSize() ;
		osSize = ga.getOSSize() ;
		ooSize = ga.getOOSize() ;
		exclude = ga.getExProperty() ;
	}
	
	/**
	 * Create a specialized RDF index as Jena model.
	 * The third parameter exProperty is a set of Jena
	 * Property objects which are ignored during indexing.
	 * This parameter might be null.
	 * 
	 * @param dataModel
	 * @param config
	 */
	public void create(Model dataModel, Config config)
	{
		if (config != null)
		{
			level = config.getIndexLevel() ;
			exclude = config.getExProperty() ;
		}
		
		selectivity = new Selectivity(dataModel) ;
		
		// Index to allow probability calculation for triple patterns
		createTripleIndex(dataModel) ;
		
		if (level > 0)
			// Index to allow probability calculation for joined triple patterns
			createTriplesIndex(dataModel) ;
		
		indexModel = ga.create(this) ;
	}
	
	/**
	 * Return the specialized RDF index Jena model.
	 * Perform a load() or create() first.
	 * 
	 * @return Model
	 */
	public Model getModel()
	{ return indexModel ; }
	
	/**
	 * Perform a lookup for the number of triples matching a property.
	 * Please note that the method returns 0 if the property is not found in
	 * the index. This should be remembered, especially when certain
	 * property URIs are set to the exclude list.
	 * 
	 * @param property
	 * @return long
	 */
	public long lookup(Property property)
	{
		// If the property is not found in the index, return 0
		long num = 0L ;
		
		if (properties.containsKey(property))
			num = ((Long)properties.get(property)).longValue() ;
		else
			log.debug("Index not found (return " + num + "): " + property.toString()) ;
		
		return num ;
	}
	
	/**
	 * Answer if the index allows the computation of joined probabilities,
	 * i.e. if the index level is >0
	 * 
	 * @return boolean
	 */
	public boolean allowsJoinedProbability()
	{ 
		if (level > 0)
			return true ;
		
		return false ;
	}
	
	/**
	 * Return the index level
	 * 
	 * @return int
	 */
	public int getLevel()
	{ return level ; }
	
	/**
	 * Return the indexed number of resources. Please note that this
	 * information might be outdated if compared with the effective
	 * number of resources contained in an ontology which was 
	 * previously indexed and whose index is loaded here. Consider
	 * to check the probabilities with the selectivities as a metric
	 * for the quality of the estimation.
	 * 
	 * @return long
	 */
	public long getIndexedNumRes()
	{ return indexedNumRes ; }
	
	/**
	 * Return the indexed size of the ontology, i.e. the number of statements
	 * 
	 * @return long
	 */
	public long getIndexedSize()
	{ return indexedSize ; }
	
	/**
	 * Return the mapping for the histograms with each property
	 * 
	 * @return Map
	 */
	public Map getHistograms()
	{ return histograms ; } // Map<Property, Histogram>
	
	/**
	 * Return the mapping for the properties and their frequencies
	 * 
	 * @return Map
	 */
	public Map getProperties()
	{ return properties ; } // Map<Property, Long>
	
	/**
	 * Return the mapping of joined triple patterns and their frequencies
	 * 
	 * @return Map
	 */
	public Map getPatterns()
	{ return patterns ; } // Map<Pattern, Long>
	
	/**
	 * Return the indexed upper bound number of records returned by the joined
	 * triple patterns with only variables and a join over the subjects
	 * 
	 * @return long
	 */
	public long getIndexedSSSize()
	{ return ssSize ; }
	
	/**
	 * Return the indexed upper bound number of records returned by the joined
	 * triple patterns with only variables and a join over the subject and object
	 * 
	 * @return long
	 */
	public long getIndexedSOSize()
	{ return soSize ; }
	
	/**
	 * Return the indexed upper bound number of records returned by the joined
	 * triple patterns with only variables and a join over the object and subject
	 * 
	 * @return long
	 */
	public long getIndexedOSSize()
	{ return osSize ; }

	/**
	 * Return the indexed upper bound number of records returned by the joined
	 * triple patterns with only variables and a join over the objects
	 * 
	 * @return long
	 */
	public long getIndexedOOSize()
	{ return ooSize ; }
	
	/**
	 * Return the set of properties which are excluded in the index.
	 * 
	 * @return Set
	 */
	public Set getExProperty()
	{ return exclude ; }
	
	/*
	 * Create the index required for the probabilities of single triple patterns
	 * 
	 * @param dataModel
	 */
	private void createTripleIndex(Model dataModel)
	{ 
		long frequency ;
		Map objects = new HashMap() ; // Map<Property, ArrayList<Node>>
		
		ResIterator resIter = dataModel.listSubjects() ;
		
		while (resIter.hasNext())
		{
			resIter.next() ;
			indexedNumRes++ ;
		}
		
		log.debug("Number of resources: " + indexedNumRes) ;
		
		// Build stats for properties and calculate the size of the ontology
		StmtIterator stmtIter = dataModel.listStatements() ;
	
		while (stmtIter.hasNext())
		{
			Statement stmt = stmtIter.nextStatement() ;
			Property predicate = stmt.getPredicate() ;
		
			// Check if the property is in exclude list
			if (! exclude(predicate))
			{
				frequency = 0L ;
				Node object = stmt.getObject().asNode() ;
				List objectsL = new ArrayList(); // List<Node>
				
				if (properties.containsKey(predicate))
					frequency = ((Long)properties.get(predicate)).longValue() ;
				
				if (objects.containsKey(predicate))
					objectsL = (List)objects.get(predicate) ;
				
				objectsL.add(object) ;
				
				properties.put(predicate, new Long(++frequency)) ;
				objects.put(predicate, objectsL) ;
			}
			
			indexedSize++ ;
		}
		
		log.debug("Model size: " + indexedSize) ;
		log.debug("Number of properties: " + properties.size()) ;

		// BEGIN -- Just for debug purpose
		for (Iterator iter = properties.keySet().iterator(); iter.hasNext(); )
		{
			Property property = (Property)iter.next() ;
			frequency = ((Long)properties.get(property)).longValue() ;
			log.debug("Indexed property and frequency: " + frequency + " " + property.toString()) ;
		}
		// END
		
		for (Iterator iter = objects.keySet().iterator(); iter.hasNext(); )
		{
			Property property = (Property)iter.next() ;
			List objectsL = (List)objects.get(property) ; // List<Node>
			Histogram histogram = new Histogram() ;
			histogram.addElements(objectsL) ;
			log.debug("Create histogram for property: " + property) ;
			log.debug("Number of histogram objects: " + objectsL.size()) ;
			histograms.put(property, histogram) ;
		}
	}

	/*
	 * Create the index required for the probabilities of joined triple patterns (performed only with index level >0)
	 * 
	 * @param dataModel
	 */
	private void createTriplesIndex(Model dataModel)
	{	
		// Index the ss, so, os, oo 
		ssSize = selectivity(new Triple(Node.createVariable("s1"), Node.createVariable("p1"), Node.createVariable("o1")),
							 new Triple(Node.createVariable("s1"), Node.createVariable("p2"), Node.createVariable("o2"))) ;
		soSize = selectivity(new Triple(Node.createVariable("s1"), Node.createVariable("p1"), Node.createVariable("o1")),
							 new Triple(Node.createVariable("s2"), Node.createVariable("p2"), Node.createVariable("s1"))) ;
		osSize = selectivity(new Triple(Node.createVariable("s1"), Node.createVariable("p1"), Node.createVariable("o1")),
				 			 new Triple(Node.createVariable("o1"), Node.createVariable("p2"), Node.createVariable("o2"))) ;
		ooSize = selectivity(new Triple(Node.createVariable("s1"), Node.createVariable("p1"), Node.createVariable("o1")),
	 			 			 new Triple(Node.createVariable("s2"), Node.createVariable("p2"), Node.createVariable("o1"))) ;
		
		for (Iterator iter = properties.keySet().iterator(); iter.hasNext(); )
		{
			Property property1 = (Property)iter.next() ;
			
			// Check if property is in exclude list
			if (! exclude(property1))
			{
				for (Iterator it = properties.keySet().iterator(); it.hasNext(); )
				{
					Property property2 = (Property)it.next() ;
			
					if (! exclude(property2))
					{
						index(new Pattern(property1, property2, ResourceFactory.createResource(BasicPatternJoin.SS))) ;
						index(new Pattern(property1, property2, ResourceFactory.createResource(BasicPatternJoin.SO))) ;
						index(new Pattern(property1, property2, ResourceFactory.createResource(BasicPatternJoin.OS))) ;
						index(new Pattern(property1, property2, ResourceFactory.createResource(BasicPatternJoin.OO))) ;
					}
				}
			}
		}
	}
	
	/*
	 * Return the selectivity of joined triple patterns by executing a corresponding SPARQL query
	 * 
	 * @param triple1
	 * @param triple2
	 * @return long
	 */
	private long selectivity(Triple triple1, Triple triple2)
	{
		long frequency = selectivity.calculate(triple1, triple2) ;
		
		log.debug("Indexed: " + frequency + " [" + triple1 + ", " + triple2 + "]") ;
		
		return frequency ;
	}
	
	/*
	 * Index a pattern of joined triple patterns (two of them)
	 *  
	 * @param pattern
	 */
	private void index(Pattern pattern)
	{
		long frequency = selectivity.calculate(pattern) ;
		
		patterns.put(pattern, new Long(frequency)) ;
		
		log.debug("Indexed pattern and frequency: " + frequency + " " + pattern.toString()) ;
	}
	
	/*
	 * A list of property URIs that are excluded during the index process
	 * 
	 * @param property
	 * @return boolean
	 */
	private boolean exclude(Property property)
	{
		if (exclude == null)
			return false ;
		
		if (exclude.contains(property)) 
			return true ;
			
		return false ;
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