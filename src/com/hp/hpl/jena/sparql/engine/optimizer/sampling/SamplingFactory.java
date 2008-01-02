/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.optimizer.sampling;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.engine.optimizer.sampling.impl.RandomSampling;

/**
 * The factory is the main entry point to use sampling 
 * techniques and, thus, given a Jena graph model sample
 * it according to specific sampling techniques and
 * a sampling percentage.
 * 
 * Sampling is a transformation function on Jena graph models.
 * Such functions have to be implemented and are subclasses
 * of SamplingBase, hence, they need to implement the 
 * Sampling interface.
 * 
 * @author Markus Stocker
 */

public class SamplingFactory 
{
	/**
	 * Return the sampled graph using the default sampling method.
	 * 
	 * @param model
	 * @param factor
	 * @return Sampling
	 */
	public static Graph defaultSamplingMethod(Model model, double factor)
	{ return defaultSamplingMethod(model.getGraph(), factor) ; }
	
	/**
	 * Return the sampled graph using the default sampling method.
	 * 
	 * @param graph
	 * @param factor
	 * @return Sampling
	 */
	public static Graph defaultSamplingMethod(Graph graph, double factor)
	{
		Sampling sampling = new RandomSampling(graph, factor) ;
		
		return sampling.sample() ;
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