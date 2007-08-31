/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.optimizer.probability.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.sparql.engine.optimizer.probability.Histogram;
import com.hp.hpl.jena.sparql.engine.optimizer.probability.impl.ProbabilityIndex;
import com.hp.hpl.jena.sparql.engine.optimizer.util.Config;
import com.hp.hpl.jena.sparql.engine.optimizer.core.BasicPatternJoin;

/**
 * The implementation for the probabilistic index model based
 * on the specialized RDF index. This implementation
 * provides probability estimations for both single triple
 * patterns and joined triple patterns. Hence, it is more 
 * powerful but less accurate compared to the probabilistic
 * data model for the probability estimation of triple patterns.
 * 
 * @author Markus Stocker
 * @version $Id$
 */

public class ProbabilityIndexModel extends ProbabilityBase
{
	// The indexed number of triples in the ontology
	private long indexedSize = -1L ;
	/*
	 * The size of the cartesian product of two triple patterns, 
	 * used for the normalization of joined triple pattern probabilities
	 */
	private long squaredIndexedSize = -1L ;
	// The minimal joined probability, 1 / squaredIndexedSize
	private double minJoinedProbability = 0d ;
	// The indexed number of resources in the ontology
	private long indexedNumRes = 0L ;
	// The object of the specialized RDF index
	private ProbabilityIndex index = new ProbabilityIndex() ;
	// The mapping of the histograms for the properties
	private Map histograms = new HashMap() ; // Map<Property, Histogram>
	// The mapping of the joined triple patterns with their sizes
	private Map patterns = new HashMap() ; // Map<Pattern, Long>
	// The upper bound sizes of variable patterns joined over the SS, SO, OS and OO
	private long indexedSSSize = -1L, indexedSOSize = -1L, indexedOSSize = -1L, indexedOOSize = -1L ;
	private static Log log = LogFactory.getLog(ProbabilityIndexModel.class) ;
	
	/**
	 * Create a probabilistic index model. Please note that
	 * the index is created and this make take a while.
	 * The config parameter allows to set specific configurations.
	 * 
	 * @param dataModel
	 * @param indexLevel
	 * @param config
	 */
	public void create(Model dataModel, Config config)
	{
		super.create(dataModel) ;
		index.create(dataModel, config) ;
		init(config) ;
	}
	
	/**
	 * Load a probabilistic index model. The specialized RDF
	 * index has to be build previously and it's Jena model
	 * is a parameter of this method.
	 * 
	 * @param dataModel
	 * @param indexModel
	 * @param config
	 */
	public void load(Model dataModel, Model indexModel, Config config)
	{
		super.load(dataModel) ;
		index.load(indexModel) ;
		init(config) ;
	}
	
	/**
	 * The method used to create and return the specialized
	 * RDF index. The third parameter allows to set specific
	 * configurations.
	 * 
	 * @param dataModel
	 * @param config
	 * @return Model
	 */
	public Model index(Model dataModel, Config config)
	{
		create(dataModel, config) ;
		
		return index.getModel() ;
	}
	
	/**
	 * The estimated probability of a triple pattern
	 * calculated as the multiplication of the probability 
	 * of its elements, the subject, predicate and object
	 * 
	 * @param triple
	 * @return double
	 */
	public double getProbability(Triple triple)
	{		
		double sp = getSubjectProbability(triple) ;
		double pp = getPredicateProbability(triple) ;
		double op = getObjectProbability(triple) ;
		
		double p = -1d ;
		
		if (sp == 0 || pp == 0 || op == 0)
			p = 0d ;
		else
			p = check(getProbability(sp, pp, op)) ;
 		
 		log.debug("Probability: " + sp + " [" + triple.getSubject() + "]") ;
 		log.debug("Probability: " + pp + " [" + triple.getPredicate() + "]") ;
 		log.debug("Probability: " + op + " [" + triple.getObject() + "]") ;
		log.debug("Probability: " + p + " [" + triple + "]") ;
		
		return p ;
	}
	
	/**
	 * The exact selectivity calculated by executing a SPARQL query.
	 * Please note that, although accurate, this might be slow and 
	 * depending on the specific usage inappropriate.
	 * 
	 * @param triple
	 * @return double
	 */
	public double getSelectivity(Triple triple)
	{
		double s = new Long(selectivity.calculate(triple)).doubleValue() / indexedSize ;
		
		log.debug("Selectivity: " + s + " [" + triple + "]") ;
		
		return s ;
	}
	
	/**
	 * Get the estimated probability of joined triple patterns. The second
	 * triple pattern (triple2) might be null, in which case the 
	 * probability of the single triple pattern (triple1) is returned.
	 * If the index does not support joined probability calculation
	 * (because of the index level 0), the probability returned is
	 * 1.0 and a warning is sent to logging. This method is by far
	 * the most complex and it considers different correction heuristics
	 * to estimate the probabilities of joined triple patterns.
	 * 
	 * @param triple1
	 * @param triple2
	 * @return double
	 */
	public double getProbability(Triple triple1, Triple triple2)
	{
		if (triple2 == null)
			return getProbability(triple1) ;
		
		if (! index.allowsJoinedProbability())
		{
			log.warn("The index does not support joined probability estimation (return 1.0") ;
			return 1d ;
		}
		
		double p = 1d ;
		Node predicate1 = triple1.getPredicate() ;
		Node predicate2 = triple2.getPredicate() ;
		// Does not consider bound/unbound, just SS, SO, OS, OO
		String genericJoinType = BasicPatternJoin.genericType(triple1, triple2) ;
		// Does consider if bound or unbound, e.g. uSS, bSS
		String specificJoinType = BasicPatternJoin.specificType(triple1, triple2) ;
		
		if (! BasicPatternJoin.isJoined(triple1, triple2))
		{
			// Not joined
			if (BasicPatternJoin.varsOnly(triple1, triple2))
			{
				// Not joined, vars only: cartesian product, i.e. matches everything
				log.debug("Triple pattens are not joined and contain variables only (return 1.0): [" + triple1 + ", " + triple2 + "]") ;
				
				return 1d ;
			}
			else
			{
				// Not joined: cartesian product of the two result sets
				// Get the estimated result set size for the first triple pattern
				long st1 = new Double(getProbability(triple1) * indexedSize).longValue() ;
				// Get the estimated result set size for the second triple pattern
				long st2 = new Double(getProbability(triple2) * indexedSize).longValue() ;
				// The patterns are not joined, hence, the result set size is the cross product between the intermediate result set sizes
				long ts = st1 * st2 ;

				p = checkJoined(new Double(ts).doubleValue() / squaredIndexedSize) ;
				log.debug("Triple patterns are not joined (return " + p + "): [" + triple1 + ", " + triple2 + "]") ;
				
				return p ;
			}
		}
		
		if (predicate1.isURI() && predicate2.isURI())
		{
			Pattern pattern = getPattern(triple1, triple2) ;
			
			if (patterns.containsKey(pattern))
			{
				double correction = 1d ;
				double indexProbability = ((Long)patterns.get(pattern)).doubleValue() / squaredIndexedSize ;
				// Override the specific join type ignore PP joins
				specificJoinType = BasicPatternJoin.genericTypeIgnorePP(triple1, triple2) ;
				
				// Consider bound elements
				if (specificJoinType.equals(BasicPatternJoin.bSS))
					correction = getSubjectProbability(triple1) * getObjectProbability(triple1) * getObjectProbability(triple2) ;
				else if (specificJoinType.equals(BasicPatternJoin.bSO))
					correction = getSubjectProbability(triple1) * getObjectProbability(triple1) * getSubjectProbability(triple2) ;
				else if (specificJoinType.equals(BasicPatternJoin.bOS))
					correction = getObjectProbability(triple1) * getSubjectProbability(triple1) * getObjectProbability(triple2) ;
				else if (specificJoinType.equals(BasicPatternJoin.bOO))
					correction = getObjectProbability(triple1) * getSubjectProbability(triple1) * getSubjectProbability(triple2) ;
				else
					correction = getSubjectProbability(triple1) * getObjectProbability(triple1) * getSubjectProbability(triple2) * getObjectProbability(triple2) ;
				
				double correctedProbability = indexProbability  * correction ;
				
				// If the index returns 0, believe it, do not check the minimum
				if (correctedProbability == 0)
					p = correctedProbability ;
				else
					p = checkJoined(correctedProbability) ;
				
				log.debug("Index probability: " + indexProbability) ;
				log.debug("Pattern: " + pattern) ;
				log.debug("Correction: " + correction) ;
				log.debug("Corrected probability: " +  correctedProbability) ;
				log.debug("Checked probability: " + p + " [" + triple1 + ", " + triple2 + "]") ;
				
				return p ;
			}
			else 
				// The pattern is not contained in the index, could be something like ?x1 :p ?y1 . ?x2 :p ?y2, i.e. PP join
				log.warn("Pattern not found in index: " + pattern) ;
		}

		// Joined, but index lookup is not possibile because predicates are not given
		double correction = 1d; 
		
		if (genericJoinType.equals(BasicPatternJoin.SS))
			correction = new Long(indexedSSSize).doubleValue() / squaredIndexedSize ;
		else if (genericJoinType.equals(BasicPatternJoin.SO))
			correction = new Long(indexedSOSize).doubleValue() / squaredIndexedSize ;
		else if (genericJoinType.equals(BasicPatternJoin.OS))
			correction = new Long(indexedOSSize).doubleValue() / squaredIndexedSize ;
		else if (genericJoinType.equals(BasicPatternJoin.OO))
			correction = new Long(indexedOOSize).doubleValue() / squaredIndexedSize ;
		
		if (specificJoinType.equals(BasicPatternJoin.bSS)
				|| specificJoinType.equals(BasicPatternJoin.bSO))
			correction *= getSubjectProbability(triple1) ;
		else if (specificJoinType.equals(BasicPatternJoin.bOS)
				|| specificJoinType.equals(BasicPatternJoin.bOO))
			correction *= getObjectProbability(triple1) ;
		
		// The correction is 0 when the index tells this, hence, believe it
		if (correction == 0)
			p = correction ;
		else
			p = checkJoined(getProbability(triple1) * getProbability(triple2) * correction) ;
		
		log.debug("Correction: " + correction) ;
		log.debug("Corrected probability: " + p + " [" + triple1 + ", " + triple2 + "]") ;
		
		return p ;
	}
	
	/**
	 * Get the exact selectivity of joined triple patterns 
	 * by executing a corresponding SPARQL query
	 * 
	 * @param triple1
	 * @param triple2
	 * @return double
	 */
	public double getSelectivity(Triple triple1, Triple triple2)
	{
		if (triple2 == null)
			return getSelectivity(triple1) ;
		
		double s = new Double(selectivity.calculate(triple1, triple2)).doubleValue() / squaredIndexedSize ;
		
		log.debug("Selectivity: " + s + " [" + triple1 + ", " + triple2 + "]") ;
		
		return s ;
	}
	
	/**
	 * Return the minimum probability of joined triple patterns
	 * 
	 * @return double
	 */
	public double getMinJoinedProbability()
	{ return minJoinedProbability ; }
	
	/**
	 * Return the indexed number of triples in the ontology
	 * (in the ontology which was indexed, a process that 
	 * might have been executed a long time ago, hence, the
	 * figures might not be accurate).
	 * 
	 * @return long
	 */
	public long getIndexedSize()
	{ return indexedSize ; }
	
	/**
	 * The size of the cartesian product of two triple patterns
	 * 
	 * @return long
	 */
	public long getSquaredIndexedSize() 
	{ return squaredIndexedSize ; }
	
	/**
	 * Return the Java index object (for testing purposes!)
	 * 
	 * @return ProbabilityIndex
	 */
	public ProbabilityIndex getIndex()
	{ return index ; }
	
	// Return the probability of the subject of a triple pattern
	private double getSubjectProbability(Triple triple)
	{
		double s = 1d ;
		
		// Avoid divide by zero
		if (indexedNumRes > 0)
			s = new Long(getSubjectSize(triple)).doubleValue() / indexedNumRes ; 
	
		return s ;
	}
	
	// Return the estimated size of the subject
	private long getSubjectSize(Triple triple)
	{ 
		// If variable, the subject matches everything
		if (triple.getSubject().isVariable())
			return indexedNumRes ;
		
		// If not, it matches it matches one resource
		return 1L ;
	}
	
	// Return the probability of the predicate
	private double getPredicateProbability(Triple triple)
	{
		double s = 1d ;
		
		if (indexedSize > 0)
			s = new Long(getPredicateSize(triple)).doubleValue() / indexedSize ; 
	
		return s ;
	}
	
	/*
	 * Return the exact size of the predicate 
	 * (exact because the specialized index holds exact information about the frequencies of properties)
	 */
	private long getPredicateSize(Triple triple)
	{
		Node predicate = triple.getPredicate() ;
		
		if (predicate.isVariable())
			return indexedSize ;
		
		return index.lookup(ResourceFactory.createProperty(predicate.getURI())) ;
	}
	
	// Return the object probability
	private double getObjectProbability(Triple triple)
	{
		double s = 1d ;
		
		if (indexedSize > 0)
			s = new Long(getObjectSize(triple)).doubleValue() / indexedSize;
		
		return s ;
	}
	
	// Return the object size
	private long getObjectSize(Triple triple)
	{
		Node object = triple.getObject() ;
		
		if (object.isVariable())
			return indexedSize ;
		
		long size = 0L ;
		Node predicate = triple.getPredicate() ;
		
		// The predicate is bound, in which case we do a lookup of the object in the corresponding histogram
		if (predicate.isURI())
		{
			Property property = ResourceFactory.createProperty(predicate.getURI()) ;
			
			if (histograms.containsKey(property))
			{
				Histogram histogram = (Histogram)histograms.get(property) ;
				return histogram.getClassFrequency(object) ;
			}
			else
			{
				log.warn("The predicate has no corresponding histogram, is it in exclusion list? (return " + size + "): " + predicate) ;
				return size ;
			}
		}
	
		// The predicate is unbound, in which case we sum the object size from all histograms
		for (Iterator iter = histograms.keySet().iterator(); iter.hasNext(); )
		{
			Property property = (Property)iter.next() ;
			Histogram histogram = (Histogram)histograms.get(property) ;
				
			size += histogram.getClassFrequency(object) ;
		}
			
		return size ;
	}
	
	/*
	 * Check if the probability is lower than the min possible probability
	 * for joined triple patterns
	 */
	protected double checkJoined(double p)
	{
		if (p < minJoinedProbability)
			return minJoinedProbability ;
		
		if (p > 1d)
			return 1d ;
		
		return p ;
	}
	
	// Return the index pattern for two triples
	private Pattern getPattern(Triple triple1, Triple triple2)
	{
		// Override the generic join type and ignore PP joins
		String genericJoinType = BasicPatternJoin.genericTypeIgnorePP(triple1, triple2) ;
		Property joiningProperty = ResourceFactory.createProperty(triple1.getPredicate().getURI()) ;
		Property joinedProperty = ResourceFactory.createProperty(triple2.getPredicate().getURI()) ;
		Resource joinType = ResourceFactory.createResource(genericJoinType) ;
		
		return new Pattern(joiningProperty, joinedProperty, joinType) ;
	}
	
	// Perform some initializations
	private void init(Config config) 
	{
		this.indexedSize = index.getIndexedSize() ;
		this.indexedNumRes = index.getIndexedNumRes() ;
		this.histograms = index.getHistograms() ;
		this.patterns = index.getPatterns() ;
		this.indexedSSSize = index.getIndexedSSSize() ;
		this.indexedSOSize = index.getIndexedSOSize() ;
		this.indexedOSSize = index.getIndexedOSSize() ;
		this.indexedOOSize = index.getIndexedOOSize() ;
		this.squaredIndexedSize = indexedSize * indexedSize ;
		this.minProbability = 1d / indexedSize ;
		this.minJoinedProbability = 1d / squaredIndexedSize ;
		
		if (config != null)
		{
			if (!config.limitMinProbability())
			{
				// The min probability should not be constrained
				this.minProbability = Double.MIN_VALUE ;
				this.minJoinedProbability = Double.MIN_VALUE ;
			}
		}
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