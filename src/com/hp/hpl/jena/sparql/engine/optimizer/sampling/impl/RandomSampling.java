/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.optimizer.sampling.impl;

import java.util.Random;
import java.util.List;
import java.util.ArrayList;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.engine.optimizer.sampling.SamplingBase;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * An implementation of a sampling technique based on random selection
 * of a given number of statements in a Jena graph.
 * 
 * @author Markus Stocker
 */

public class RandomSampling extends SamplingBase 
{	
	private Random rand = new Random() ;
	private List triples = new ArrayList() ; // List<Triple>
	
	/**
	 * Do some preliminary setup work. Graph cannot be null
	 * and factor has to be a double value of the intervall [0,1].
	 * 
	 * @param graph
	 * @param factor
	 */
	public RandomSampling(Graph graph, double factor)
	{
		super(graph, factor) ;
		
		getTriples() ;
	}
	
	/**
	 * Implementation of the interface. Provide a logic to
	 * identify the next triple to add to the sampled graph.
	 * 
	 * @return Triple
	 */
	public Triple getNextTriple()
	{
		int next = 0 ;
		int size = triples.size() ;

		if (size > 0)
			next = rand.nextInt(size) ;
		
		return (Triple)triples.remove(next) ;
	}
	
	/*
	 * Get a list of the triples required to randomly select
	 * them to add to the sample. Please note that this is 
	 * problematic since a List can contain less elements
	 * than a Jena graph model. Don't be surprised if 
	 * this method will fail for big models.
	 */
	private void getTriples()
	{
		ExtendedIterator iter = graph.find(Node.ANY, Node.ANY, Node.ANY) ;
		
		while (iter.hasNext())
			triples.add((Triple)iter.next()) ;
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