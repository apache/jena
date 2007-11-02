/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.optimizer.heuristic;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.GraphStatisticsHandler;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.engine.optimizer.heuristic.HeuristicBasicPattern;

/**
 * This is the default heuristic used in ARQ which does not require
 * any pre-computed statistics
 * 
 * @author Markus Stocker
 * @version $Id$
 */

public class OptimalNoStats extends HeuristicBasicPattern
{
	private VariableCountingUnbound vcp = null ;
	private GraphStatisticsHeuristic gsh = null ;
	private GraphStatisticsHandler graphStatisticsHandler = null ;
	
	public OptimalNoStats(Graph graph)
	{
		this.graphStatisticsHandler = graph.getStatisticsHandler() ;
		this.vcp = new VariableCountingUnbound() ;
		
		if (graphStatisticsHandler != null)
			this.gsh = new GraphStatisticsHeuristic(graph) ;
	}
	
	/**
	 * If the Jena graph statistics handler is not null, 
	 * the method returns a cost estimation for the triple patterns
	 * which considers the accurate size for SPO returned by
	 * the Jena graph statistics handler. If the Jena graph statistics
	 * handler is null, the method returns a cost based on the variable
	 * counting heuristic.
	 * 
	 * @param triple1
	 * @return Double
	 */
	public double getCost(Triple triple1) 
	{
		if (gsh == null)
			return vcp.getCost(triple1) ;
		
		return gsh.getCost(triple1) ;
	}
	
	/**
	 * The Jena graph statistics handler does not support
	 * joined triple pattern selectivity estimation.
	 * The method uses the variable counting heuristic
	 * to return a cost for joined triple patterns.
	 * Bound predicates are estimated as low selective (1.0)
	 * The VPC heuristic is executed.
	 * 
	 * @param triple1
	 * @param triple2
	 * @return Double
	 */
	public double getCost(Triple triple1, Triple triple2)
	{
		double p1 = 0d ; 
		double p2 = 0d ;
		
		if (gsh == null)
		{
			p1 = vcp.getCost(triple1) ;
			p2 = vcp.getCost(triple2) ;
		}
		else
		{
			p1 = gsh.getCost(triple1) ;
			p2 = gsh.getCost(triple2) ;
		}
		
		if (p1 < p2)
			return p1 + vcp.getCost(triple1, triple2) ;
		
		return p2 + vcp.getCost(triple1, triple2) ;
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