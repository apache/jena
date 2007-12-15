/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.optimizer.probability.impl;

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.model.Bag;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Seq;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.engine.optimizer.probability.Histogram;
import com.hp.hpl.jena.sparql.engine.optimizer.probability.HistogramClass;
import com.hp.hpl.jena.sparql.engine.optimizer.probability.Vocabulary;
import com.hp.hpl.jena.sparql.engine.optimizer.probability.impl.Pattern;
import com.hp.hpl.jena.sparql.engine.optimizer.probability.impl.ProbabilityIndex;

import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.sparql.engine.optimizer.Optimizer;

/**
 * The class is a wrapper around the Jena model used
 * for the specialized index. It implements a number of
 * methods to deal with the specific RDF graph, e.g. load
 * the Jena RDF model into Java data structures or create
 * a Jena RDF model given the Java data structures 
 * representing the index.
 * 
 * @author Markus Stocker
 */

public class IndexModelAccess 
{
	/* The index level, 0 for lightweight, 1 (default) for full index */
	private int level = 1 ;
	/* The number of triples contained in the indexed ontology */
	private long size = -1L ;
	/* The number of resources contained in the indexed ontology */
	private long resources = -1L ;
	/* The result set size of variable triple patterns joined over SS, SO, OS and OO */
	private long ssSize = -1L, soSize = -1L, osSize = -1L, ooSize = -1L ;
	/* The data structure holding statistics about the properties */
	private Map properties = new HashMap(); // Map<Property, Long>
	/* The data structure holding the histograms for the properties */
	private Map histograms = new HashMap() ; // Map<Property, Histogram>
	/* The data structure holding the statistics for joined triple patterns */
	private Map patterns = new HashMap() ; // Map<Pattern, Long>
	/* The set of properties which are excluded in the index */
	private Set exclude = new HashSet() ; // Set<Property>
	private static Log log = LogFactory.getLog(IndexModelAccess.class) ;

	/**
	 * The method loads a Jena model representing the specialized index
	 * into Java data structures for runtime efficient access. Essentially
	 * the method extracts the statistical information stored as RDF into
	 * Java objects.
	 * 
	 * @param model
	 */
	public void load(Model model)
	{
		ResIterator resIter = null ;
		//StmtIterator stmtIter = model.listStatements(Vocabulary.PFI, (Property)null, (RDFNode)null) ;
		Resource ontologyR = model.createResource(Vocabulary.PFI.getURI(), OWL.Ontology) ;

		if (ontologyR == null)
		{
			log.fatal("No a valid index model") ;
			return ;
		}
		
		level = ontologyR.getProperty(Vocabulary.level).getInt() ;
		size = ontologyR.getProperty(Vocabulary.size).getLong() ;
		resources = ontologyR.getProperty(Vocabulary.resources).getLong() ;
		ssSize = ontologyR.getProperty(Vocabulary.ssSize).getLong() ;
		soSize = ontologyR.getProperty(Vocabulary.soSize).getLong() ;
		osSize = ontologyR.getProperty(Vocabulary.osSize).getLong() ;
		ooSize = ontologyR.getProperty(Vocabulary.ooSize).getLong() ;
		Bag excludeB = ontologyR.getProperty(Vocabulary.exclude).getBag() ;
	
		// Add the excluded properties to the set
		for (Iterator iter = excludeB.iterator(); iter.hasNext(); )
		{
			Resource resource = (Resource)iter.next() ;
			exclude.add(ResourceFactory.createProperty(resource.getURI())) ;
		}
		
		// Extract the resources of rdf:type rdf:Property, i.e. the index information about the properties
		resIter = model.listSubjectsWithProperty(RDF.type, RDF.Property) ;
		
		while (resIter.hasNext())
		{
			Resource propertyR = (Resource)resIter.nextResource() ;
			
			Property property = ResourceFactory.createProperty(propertyR.getProperty(Vocabulary.property).getResource().getURI()) ;
			long frequency = propertyR.getProperty(Vocabulary.frequency).getLong() ;
			
			properties.put(property, new Long(frequency)) ;
			log.debug("Added property to index: " + property.toString() + ", " + frequency) ;
			
			Resource histogramR = propertyR.getProperty(Vocabulary.histogram).getResource() ;
			Seq classesS = histogramR.getProperty(Vocabulary.classes).getSeq() ;
			
 			Histogram histogram = new Histogram() ;
			histogram.setLowerBound(histogramR.getProperty(Vocabulary.lowerBound).getDouble()) ;
			histogram.setUpperBound(histogramR.getProperty(Vocabulary.upperBound).getDouble()) ;
			histogram.setClassSize(histogramR.getProperty(Vocabulary.classSize).getDouble()) ;
		
			histograms.put(property, histogram) ;
			log.debug("Added histogram to index: [" + histogram.getLowerBound() + "," + histogram.getUpperBound() + "], " + histogram.getClassSize()) ;
			
			for (Iterator iter = classesS.iterator(); iter.hasNext(); )
			{
				Resource histogramClassR = (Resource)iter.next() ;
			
				HistogramClass histogramClass = new HistogramClass() ;
				histogramClass.setLowerBound(histogramClassR.getProperty(Vocabulary.lowerBound).getDouble()) ;
				histogramClass.setFrequency(histogramClassR.getProperty(Vocabulary.frequency).getLong()) ;
				
				histogram.addClass(histogramClass) ;
				log.debug("Added histogram class to index: " + histogramClass.getLowerBound() + ", " + histogramClass.getFrequency()) ;
			}
		}
		
		resIter.close() ;
		
		// Extract the resources of rdf:type rdf:Pattern, i.e. the index information about the joined triple patterns
		resIter = model.listSubjectsWithProperty(RDF.type, Vocabulary.Pattern) ;
		
		while (resIter.hasNext())
		{
			Resource patternR = (Resource)resIter.nextResource() ;
			Property joiningProperty = ResourceFactory.createProperty(patternR.getProperty(Vocabulary.joiningProperty).getResource().getURI()) ;
			Property joinedProperty = ResourceFactory.createProperty(patternR.getProperty(Vocabulary.joinedProperty).getResource().getURI()) ;
			Resource joinType = ResourceFactory.createResource(patternR.getProperty(Vocabulary.joinType).getResource().getURI()) ;
			long frequency = patternR.getProperty(Vocabulary.frequency).getLong() ;
			Pattern pattern = new Pattern(joiningProperty, joinedProperty, joinType) ;
			patterns.put(pattern, new Long(frequency)) ;
			log.debug("Added pattern to index: " + frequency + " " + pattern.toString()) ;
		}
		
		resIter.close() ;
	}
	
	/**
	 * The method creates the Jena model for the specialized index given
	 * the probabilistic index as a representation of the statistics
	 * used during runtime. Essentially, the method extracts the
	 * statistical information from Java objects and constructs the 
	 * related RDF graph which is returned as a Jena model. This model
	 * may be persitently stored for future use.
	 * 
	 * @param index
	 * @return Model
	 */
	public Model create(ProbabilityIndex index)
	{
		Map properties = index.getProperties() ; // Map<Property, Long>
		Map histograms = index.getHistograms() ; // Map<Property, Histogram>
		Map patterns = index.getPatterns() ; // Map<Pattern, Long>
 		Set exclude = index.getExProperty() ; // Set<Property>
 		
		Model model = ModelFactory.createDefaultModel() ;

		// Create a resource with general information about the index
		Resource ontologyR = model.createResource(Vocabulary.PFI.getURI(), OWL.Ontology) ;
		ontologyR.addProperty(Vocabulary.ssSize, new Long(index.getIndexedSSSize()).toString()) ;
		ontologyR.addProperty(Vocabulary.soSize, new Long(index.getIndexedSOSize()).toString()) ;
		ontologyR.addProperty(Vocabulary.osSize, new Long(index.getIndexedOSSize()).toString()) ;
		ontologyR.addProperty(Vocabulary.ooSize, new Long(index.getIndexedOOSize()).toString()) ;
  		ontologyR.addProperty(Vocabulary.size, new Long(index.getIndexedSize()).toString()) ;
		ontologyR.addProperty(Vocabulary.resources, new Long(index.getIndexedNumRes()).toString()) ;
		ontologyR.addProperty(Vocabulary.version, Optimizer.VERSION) ;
		ontologyR.addProperty(Vocabulary.level, new Integer(index.getLevel()).toString()) ;
		
		// Serialize the list of properties which are excluded in the index
		Bag excludeB = model.createBag() ;
		ontologyR.addProperty(Vocabulary.exclude, excludeB) ;
		
		for (Iterator iter = exclude.iterator(); iter.hasNext(); )
		{
			Property property = (Property)iter.next() ;
			excludeB.add(property) ;
		}
		
		// Create resources for each property holding it's frequency
		for (Iterator iter = properties.keySet().iterator(); iter.hasNext(); )
		{
			Property property = (Property)iter.next() ;
			long frequency = ((Long)properties.get(property)).longValue() ;

			Resource propertyR = model.createResource() ;
			propertyR.addProperty(RDF.type, RDF.Property) ;
			propertyR.addProperty(Vocabulary.property, property) ;
			propertyR.addProperty(Vocabulary.frequency, new Long(frequency).toString()) ;
		
			// Create the histogram resource for this property
			Histogram histogram = (Histogram)histograms.get(property) ;
			Resource histogramR = model.createResource() ;
			histogramR.addProperty(RDF.type, Vocabulary.Histogram) ;
			histogramR.addProperty(Vocabulary.lowerBound, new Double(histogram.getLowerBound()).toString()) ;
			histogramR.addProperty(Vocabulary.upperBound, new Double(histogram.getUpperBound()).toString()) ;
			histogramR.addProperty(Vocabulary.classSize, new Double(histogram.getClassSize()).toString()) ;
			
			Seq histogramS = model.createSeq() ;
			Set histogramClasses = histogram.getClasses() ; // Set<HistogramClass>
			
			for (Iterator it = histogramClasses.iterator(); it.hasNext(); )
			{
				HistogramClass histogramClass = (HistogramClass)it.next() ;
				double classLowerBound = histogramClass.getLowerBound() ;
				long classFrequency = histogramClass.getFrequency() ;
				
				Resource histogramClassR = model.createResource() ;
				histogramClassR.addProperty(RDF.type, Vocabulary.HistogramClass) ;
				histogramClassR.addProperty(Vocabulary.lowerBound, new Double(classLowerBound).toString()) ;
				histogramClassR.addProperty(Vocabulary.frequency, new Long(classFrequency).toString()) ;
				
				histogramS.add(histogramClassR) ;
			}
			
			histogramR.addProperty(Vocabulary.classes, histogramS) ;
			propertyR.addProperty(Vocabulary.histogram, histogramR) ;
		}
		
		// Create resources for patterns
		for (Iterator iter = patterns.keySet().iterator(); iter.hasNext(); )
		{
			Pattern pattern = (Pattern)iter.next() ;
			long frequency = ((Long)patterns.get(pattern)).longValue() ;
			
			Resource patternR = model.createResource() ;
			patternR.addProperty(RDF.type, Vocabulary.Pattern) ;
			patternR.addProperty(Vocabulary.joiningProperty, pattern.getJoiningProperty()) ;
			patternR.addProperty(Vocabulary.joinedProperty, pattern.getJoinedProperty()) ;
			patternR.addProperty(Vocabulary.joinType, pattern.getJoinType()) ;
			patternR.addProperty(Vocabulary.frequency, new Long(frequency).toString()) ;
		}
		
		return model ;
	}
	
	/**
	 * Get the level of this index. The value returned is either
	 * 0 for a lightweight index which supports probability calculation
	 * for triple patterns only (no joins) or >0 for the full index.
	 * 
	 * @return int
	 */
	public int getLevel()
	{ return level ; }
	
	/**
	 * Return the size of the indexed ontology. Note that the indexed size
	 * is not necessarily equal to the real size if the index was previously
	 * built and reused. In fact, the index might be outdated.
	 * 
	 * @return long
	 */
	public long getSize()
	{ return size ; }
	
	/**
	 * Return the number of resources of the indexed ontology. 
	 * The same as for getSize() applies to this method: the index might
	 * be outdates, hence, the value returned by this method might not 
	 * be equal to the actual value of the ontology (because new 
	 * resources have been added in the meanwhile).
	 * 
	 * @return long
	 */
	public long getResources()
	{ return resources ; }
	
	/**
	 * Return the subject-subject size as the result set size of the pattern
	 * ?s1 ?p1 ?o1 . ?s1 ?p2 ?o2. This value is used as an upperbound
	 * of SS joins.
	 * 
	 * @return long
	 */
	public long getSSSize()
	{ return ssSize ; }
	
	/**
	 * Return the subject-object size as the result set size of the pattern
	 * ?s1 ?p1 ?o1 . ?s2 ?p2 ?s1. This value is used as an upperbound 
	 * of SO joins.
	 * 
	 * @return long
	 */
	public long getSOSize()
	{ return soSize ; }
	
	/**
	 * Return the object-subject size as the result set size of the pattern
	 * ?s1 ?p1 ?o1 . ?o1 ?p2 ?o2. This value is used as an upperbound 
	 * of OS joins.
	 * 
	 * @return long
	 */
	public long getOSSize() 
	{ return osSize ; }
	
	/**
	 * Return the object-object size as the result set size of the pattern
	 * ?s1 ?p1 ?o1 . ?s2 ?p2 ?o1. This value is used as an upperbound
	 * of OO joins.
	 * 
	 * @return long
	 */
	public long getOOSize()
	{ return ooSize ; }
	
	/**
	 * Return the statistics for the properties, a Map<Property, Long>
	 * holding the properties defined in the ontology and their
	 * occurrences.
	 * 
	 * @return Map
	 */
	public Map getProperties()
	{ return properties ; }
	
	/**
	 * Return the histograms for the properties, a Map<Property, Histogram>
	 * 
	 * @return Map
	 */
	public Map getHistograms() 
	{ return histograms ; }
	
	/**
	 * Return the statistics for the indexed joined triple patterns,
	 * a Map<Pattern, Long>
	 * 
	 * @return Map
	 */
	public Map getPatterns()
	{ return patterns ; }
	
	/**
	 * Return the indexed set of excluded properties
	 * 
	 * @return Set
	 */
	public Set getExProperty()
	{ return exclude ; }
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