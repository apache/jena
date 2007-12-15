/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.optimizer.util;

import java.util.Set;
import java.util.HashSet;
import com.hp.hpl.jena.sparql.engine.optimizer.probability.IndexLevel;

/**
 * This is a configuration class for specific settings used for
 * the optimizer (e.g. index level, user defined heuristics, 
 * Jena graph models)
 * 
 * @author Markus Stocker
 */

public class Config 
{
	private double samplingFactor = 1.0 ;
	private int indexLevel = IndexLevel.FULL ;
	private Set exProperty = new HashSet() ;
	private String basicPatternHeuristic = null ;
	private boolean limitMinProbability = true ;
	
	/**
	 * Standard constructor, set the default configuration settings.
	 */
	public Config() { }
	
	/**
	 * Set a specific basic pattern heuristic.
	 * Please use the static fields provided by the HeuristicsRegistry.
	 * 
	 * @param basicPatternHeuristic
	 */
	public Config(String basicPatternHeuristic)
	{ this.basicPatternHeuristic = basicPatternHeuristic ; }
	
	/**
	 * Set a specific index level
	 * Please use the static fields provided by IndexLevel.
	 * 
	 * @param indexLevel
	 */
	public Config(int indexLevel)
	{ this.indexLevel = indexLevel ; }
	
	/**
	 * Set a boolean value to consider or ignore the lower cost bound
	 * during cost estimation.
	 * 
	 * @param limitMinProbability
	 */
	public Config(boolean limitMinProbability)
	{ this.limitMinProbability = limitMinProbability ; }
	
	/**
	 * Set a specific basic pattern heuristic and index level.
	 * Please use the static fields provided by the HeuristicsRegistry
	 * and IndexLevel.
	 * 
	 * @param basicPatternHeuristic
	 * @param indexLevel
	 */
	public Config(String basicPatternHeuristic, int indexLevel)
	{
		this.basicPatternHeuristic = basicPatternHeuristic ;
		this.indexLevel = indexLevel ;
	}
	
	/**
	 * Set a specific set of properties to exclude during indexing.
	 * 
	 * @param exProperty
	 */
	public Config(Set exProperty)
	{ this.exProperty = exProperty ; }
	
	/**
	 * Set an index level and a specific set of properties
	 * which should be excluded during indexing.
	 * 
	 * @param indexLevel
	 * @param exProperty
	 */
	public Config(int indexLevel, Set exProperty)
	{
		this.indexLevel = indexLevel ;
		this.exProperty = exProperty ;
	}
	
	/**
	 * Set the sampling factor which is a value of the
	 * intervall [0,1] and represents the percentage
	 * of the ontology used for index.
	 * 
	 * NOT YET IMPLEMENTED, setting this will not have any effect!
	 * 
	 * @param double
	 */
	public void setSamplingFactor(double samplingFactor)
	{ this.samplingFactor = samplingFactor ; }
	
	/**
	 * Return the sampling factor.
	 * 
	 * @return double
	 */
	public double getSamplingFactor()
	{ return samplingFactor ; }
	
	/**
	 * Set the index level. Use the static fields of IndexLevel
	 * for convenience. 
	 * 
	 * @param indexLevel
	 */
	public void setIndexLevel(int indexLevel)
	{ this.indexLevel = indexLevel ; }
	
	/**
	 * Returns the manually set index level
	 * 
	 * @return int
	 */
	public int getIndexLevel()
	{ return indexLevel ; }
	
	/**
	 * Set a used specific basic pattern heuristic. Use the static fields
	 * of the HeuristicsRegistry for convenience. 
	 * 
	 * @param basicPatternHeuristic
	 */
	public void setBasicPatternHeuristic(String basicPatternHeuristic)
	{ this.basicPatternHeuristic = basicPatternHeuristic ; }
	
	/**
	 * Returns the label of the manually specified basic pattern heuristic.
	 * The label has to correspond to a heuristic registred by the
	 * HeuristicsRegistry.
	 * 
	 * @return String
	 */
	public String getBasicPatternHeuristic()
	{ return basicPatternHeuristic ; }
	
	/**
	 * Set a set of Java Property objects which are excluded during indexing.
	 * 
	 * @param exProperty
	 */
	public void setExProperty(Set exProperty)
	{ this.exProperty = exProperty ; }
	
	/**
	 * Get the set of Java Property objects to exclude during indexing.
	 * The set may be empty, but not null.
	 * 
	 * @return Set
	 */
	public Set getExProperty()
	{ return exProperty ; }
	
	/**
	 * This allows to specify if during cost estimation the min possible
	 * cost should be considered as lower bound for the estimation or not.
	 * The cost estimation algorithms may compute in certain cases a 
	 * cost which is lower then the lower bound (usually 1/N where N is
	 * the number of triples contained in the ontology). Although a cost
	 * (expressed as a probability) cannot be lower than the cost of 
	 * a single triple, the limitation might have undesired side effects.
	 * For instance, the limitation might estimate multiple patterns
	 * with the same lowest cost, although in reality this isn't true.
	 * By ignoring the lower bound, the optimizer (although wrong) still
	 * preserves an ordering, one that might reflect the real cost.
	 * 
	 * @param limitMinProbability
	 */
	public void setLimitMinProbability(boolean limitMinProbability)
	{ this.limitMinProbability = limitMinProbability ; }
	
	public boolean limitMinProbability()
	{ return limitMinProbability ; }
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