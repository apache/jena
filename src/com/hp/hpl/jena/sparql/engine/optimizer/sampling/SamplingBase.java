/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.optimizer.sampling;

import com.hp.hpl.jena.graph.Factory;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.engine.optimizer.sampling.Sampling;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * Abstract class for sampling techniques. This class implements
 * some common functionalities for specific sampling techniques
 * implementations (e.g. settings the size of the graph)
 * 
 * @author Markus Stocker
 */

public abstract class SamplingBase implements Sampling 
{
	private long size ;
	private double factor ;
	/** The number of statements contained in the sampled model */
	private long statements ;
	protected Graph graph ;
	
	/**
	 * Constructor for sampling techniques. The factor
	 * is a double of the interval [0,1] reflecting the
	 * percentage.
	 * 
	 * @param model
	 * @param factor
	 */
	public SamplingBase(Graph graph, double factor)
	{
		if (graph == null)
			throw new NullPointerException("Please provide a graph object") ;
		
		if (factor < 0 || factor > 1.0)
			throw new IllegalArgumentException("Factor has to be a value of the interval [0,1]: " + factor) ;
		
		this.graph = graph ;
		// This method might not be accurate, especially for inference models
		// this.size = graph.size() ;
		// It's OK if sampling takes some time (not executed during query evaluation)
		this.size = getSize() ;
		this.factor = factor ;
		this.statements = getNumOfStatements() ;
	}
	
	/**
	 * This method has to be invoked by sub classes which 
	 * implement the Sampling interface. The method implements
	 * the logic required to create a new graph which contains
	 * the number of triples indirectly specified by the factor.
	 * NOTE: the procedure checks if a next triple is already 
	 * contained in the sample graph, in which case the loop selects
	 * a new one. The higher the factor the longer might this 
	 * method run.
	 * 
	 * @return Graph
	 */
	public Graph sample()
	{
		// This is the case for factor 1.0 (i.e. 100%)
		if (size == statements)
			return graph ;
		
		long count = 0 ;
		Graph sample = Factory.createDefaultGraph() ;
		
		while (count < statements)
		{
			Triple triple = getNextTriple() ;
			
			if (! sample.contains(triple))
			{
				sample.add(triple) ;
				count++ ;
			}
		}
		
		return sample ;
	}
	
	/**
	 * To implement by subclassing
	 * 
	 * @return Triple
	 */
	public abstract Triple getNextTriple() ;
	
	/*
	 * This is an implementation to get the accurate size of a model
	 */
	private long getSize()
	{
		long size = 0 ;
		
		ExtendedIterator iter = graph.find(Node.ANY, Node.ANY, Node.ANY) ;
		
		while (iter.hasNext()) 
		{
			iter.next() ;
			size++ ;
		}
		
		return size ;
	}
	
	/*
	 * Return the number of statements contained in the sampled model
	 */
	private long getNumOfStatements()
	{
		return new Double(size * factor).longValue() ;
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