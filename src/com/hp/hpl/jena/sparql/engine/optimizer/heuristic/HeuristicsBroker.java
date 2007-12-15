/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.optimizer.heuristic;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.sparql.engine.optimizer.probability.Probability;
import com.hp.hpl.jena.sparql.engine.optimizer.util.Constants;
import com.hp.hpl.jena.sparql.engine.optimizer.heuristic.HeuristicsRegistry;

/**
 * The heuristic broker implements a decision making program
 * for the selection of heuristic techniques required 
 * for optimizer implementations. For instance, the BasicPatternOptimizer
 * requires heuristics to estimate the execution costs of edges and nodes.
 * This class implements the logic which heuristic should be used, depending
 * on the context (availability of indexes) and the query (complexity).
 * 
 * @author Markus Stocker
 */

public class HeuristicsBroker 
{
	//private Graph graph = null ;
	private Probability probability = null ;
	private HeuristicsRegistry registry = null ;
		
	public HeuristicsBroker(Context context, Graph graph)
	{
		registry = new HeuristicsRegistry(context, graph) ;
		
		//this.graph = graph ;
		this.probability = (Probability)context.get(Constants.PF) ;
	}
	
	/**
	 * Return the best available heuristic depending on the context,
	 * i.e. the availability of specialized indexes like SEI and QPI.
	 * 
	 * @return HeuristicBasicPattern
	 */
	public HeuristicBasicPattern getBasicPatternHeuristic()
	{	
		if (probability != null)
			return getBasicPatternHeuristic(HeuristicsRegistry.BGP_PROBABILISTIC_FRAMEWORK) ;
		
		//if (graph != null && graph.getStatisticsHandler() != null)
			//return getBasicPatternHeuristic(HeuristicsRegistry.BGP_GRAPH_STATISTICS_HANDLER) ;
		
		// Default, use the variable counting heuristic
		return getBasicPatternHeuristic(HeuristicsRegistry.BGP_OPTIMAL_NO_STATS) ;
	}
	
	/**
	 * Return the selected basic graph pattern heuristic
	 * 
	 * @param basicPatternHeuristic
	 * @return HeuristicBasicPattern
	 */
	public HeuristicBasicPattern getBasicPatternHeuristic(String basicPatternHeuristic)
	{		
		if (registry.isRegistred(basicPatternHeuristic))
			return (HeuristicBasicPattern)registry.get(basicPatternHeuristic) ;
		
		return getBasicPatternHeuristic() ;
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