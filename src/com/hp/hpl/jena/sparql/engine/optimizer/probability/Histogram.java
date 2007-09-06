/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.optimizer.probability;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.engine.optimizer.probability.HistogramClass;

/**
 * The class represents a histogram for the object
 * value distribution for a predicate.
 * 
 * @author Markus Stocker
 * @version $Id$
 */

public class Histogram 
{
	// The number of classes for the histogram
	private int nrOfClasses = 10 ;
	private double lowerBound = 0d, upperBound = 0d, classSize = 0d ;
	private List elementList = new ArrayList() ; // List<Double>
	private Map histogram = new TreeMap() ; // Map<Double, HistogramClass>
	private static Log log = LogFactory.getLog(Histogram.class) ;
	
	/**
	 * Add the elements of a histogram to it, i.e. the triples object
	 * 
	 * @param elements
	 */
	public void addElements(List elements)
	{
		// List<Node>
		for (Iterator iter = elements.iterator(); iter.hasNext(); )
		{
			Node node = (Node)iter.next() ;
			
			Double element = toElement(node) ;
			
			if (element != null)
				elementList.add(element) ;
		}
		
		// Sort the list of hash codes
		Collections.sort(elementList) ;
		
		lowerBound = ((Double)elementList.get(0)).doubleValue() ;
		upperBound = ((Double)elementList.get(elementList.size() - 1)).doubleValue() ;
		classSize = (upperBound - lowerBound) / nrOfClasses ;
		
		log.debug("Number of elements: " + elementList.size()) ;
		log.debug("Histogram lower bound: " + lowerBound) ;
		log.debug("Histogram upper bound: " + upperBound) ;
		log.debug("Histogram class size: " + classSize) ;
		
		// Initialize the histogram classes for the histogram
		for (int i = 0; i < nrOfClasses; i++)
		{
			double classLowerBound = lowerBound + (i * classSize) ;
			HistogramClass histogramClass = new HistogramClass() ;
			histogramClass.setLowerBound(classLowerBound) ;
			
			histogram.put(new Double(classLowerBound), histogramClass) ;
			
			log.debug("Create histogram class with lower bound: " + classLowerBound) ;
		}
		
		for (Iterator iter = elementList.iterator(); iter.hasNext(); )
		{			
			Double element = (Double)iter.next() ;
			double classLowerBound = getLowerBound(element.doubleValue()) ;
			HistogramClass histogramClass = (HistogramClass)histogram.get(new Double(classLowerBound)) ;
			histogramClass.increment() ;
		}
	}
	
	/**
	 * Return the histogram class frequency for a node (an object).
	 * If the object is not found in the histogram, the method returns
	 * Integer.MAX_VALUE. This is important for the selectivity estimation
	 * of patterns where the predicate is unbound and the object bound, since
	 * we calculate the object selectivity over every histogram and for some,
	 * the object might fall into no histogram class and thus no frequency value,
	 * is returned, i.e. we return Integer.MAX_VALUE. Returning 0 is a bad
	 * idea, since it is a value which actually might be returned by a 
	 * histogram class lookup and thus we cannot decide whether or not 
	 * the value should be considered.
	 * 
	 * @param node
	 * @return long
	 */
	public long getClassFrequency(Node node)
	{
		// Get the node hash code, calculate it's lower bound and get the histogram class
		Double element = toElement(node) ;
		
		// Look for the element only if it fits in the histogram boundaries
		if (element.doubleValue() >= lowerBound && element.doubleValue() <= upperBound)
		{
			HistogramClass histogramClass = (HistogramClass)histogram.get(new Double(getLowerBound(element.doubleValue()))) ;
	
			return histogramClass.getFrequency() ;
		}
		
		log.debug("The element is not contained in this histogram (return 0): " + element + " (" + lowerBound + ", " + upperBound + ")") ;
		
		return 0L ;
	}
	
	/**
	 * Return a list of histogram classes
	 * 
	 * @return Set<HistogramClass>
	 */
	public Set getClasses()
	{
		Set histogramClasses = new HashSet() ; // Set<HistogramClass>
		
		for (Iterator iter = histogram.values().iterator(); iter.hasNext(); )
			histogramClasses.add((HistogramClass)iter.next()) ;
		
		return histogramClasses ;
	}
	
	/**
	 * Add a histogram class to the histogram
	 * 
	 * @param histogramClass
	 */
	public void addClass(HistogramClass histogramClass)
	{
		// Use the histogram class lower bound as hash map key
		histogram.put(new Double(histogramClass.getLowerBound()), histogramClass) ;
	}
	
	/**
	 * Set the histogram lower bound
	 * 
	 * @param lowerBound
	 */
	public void setLowerBound(double lowerBound)
	{
		this.lowerBound = lowerBound ;
	}
	
	/**
	 * Get the histogram lower bound
	 * 
	 * @return double
	 */
	public double getLowerBound()
	{
		return this.lowerBound ;
	}
	
	/**
	 * Set the histogram upper bound
	 * 
	 * @param upperBound
	 */
	public void setUpperBound(double upperBound)
	{
		this.upperBound = upperBound ;
	}
	
	/**
	 * Get the histogram upper bound
	 * 
	 * @return double
	 */
	public double getUpperBound()
	{
		return this.upperBound ;
	}
	
	/** 
	 * Set the histogram class size
	 * 
	 * @param classSize
	 */
	public void setClassSize(double classSize)
	{
		this.classSize = classSize ;
 	}
	
	/**
	 * Get the histogram class size
	 * 
	 * @return double
	 */
	public double getClassSize()
	{
		return this.classSize ;
	}
	
	/**
	 * Return the number of elements contained in the histogram.
	 * 
	 * @return long
	 */
	public long size()
	{
		long size = 0L ;
		
		// Step through the histogram classes and add up the frequencies
		for (Iterator iter = histogram.keySet().iterator(); iter.hasNext(); )
		{
			Double key = (Double)iter.next() ;
			HistogramClass histogramClass = (HistogramClass)histogram.get(key) ;
			size += histogramClass.getFrequency() ;
		}
		
		return size ;
	}
	
	private double getLowerBound(double element)
	{
		int i = 0 ;
		double bound = lowerBound + (i * classSize);
		double nextBound = lowerBound + (++i * classSize) ;
		
		// Search in which histogram class the element falls into
		while (element > nextBound)
		{
			bound = nextBound ;
			nextBound = lowerBound + (++i * classSize) ;
		}
		
		return bound ;
	}
	
	// Turn a node into a histogram element, i.e. it's hash code
	private Double toElement(Node node)
	{
		if (node.isLiteral())
			return new Double(node.getLiteralLexicalForm().hashCode()) ;
		else if (node.isURI())
			return new Double(node.getURI().hashCode()) ;
		else if (node.isBlank())
			return new Double(node.getBlankNodeId().getLabelString().hashCode()) ;
		
		return null ;
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