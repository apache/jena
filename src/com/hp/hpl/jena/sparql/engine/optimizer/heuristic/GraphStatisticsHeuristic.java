/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.optimizer.heuristic;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.GraphStatisticsHandler;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.engine.optimizer.heuristic.HeuristicBasicPattern;
import com.hp.hpl.jena.sparql.engine.optimizer.heuristic.VariableCounting;

/**
 * This heuristic is a wrapper around the Jena graph statistics handler
 * which returns exact selectivities for SPO.
 * 
 * @author Markus Stocker
 * @version $Id$
 */

public class GraphStatisticsHeuristic extends HeuristicBasicPattern
{	
	private int size = 0 ;
	private double minCost = -1d ;
	private GraphStatisticsHandler graphStatisticsHandler = null ;
	private VariableCounting vc = new VariableCounting() ;
	private static Log log = LogFactory.getLog(GraphStatisticsHeuristic.class) ;
	
	public GraphStatisticsHeuristic(Graph graph)
	{
		this.size = graph.size() ;
		this.minCost = 1d / size ;
		this.graphStatisticsHandler = graph.getStatisticsHandler() ;
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
		if (graphStatisticsHandler == null)
			return vc.getCost(triple1) ;
		
		Node s = triple1.getSubject() ;
		Node p = triple1.getPredicate() ;
		Node o = triple1.getObject() ;
		
		double sc = new Double(graphStatisticsHandler.getStatistic(s, Node.ANY, Node.ANY)).doubleValue() / size ;
		double pc = new Double(graphStatisticsHandler.getStatistic(Node.ANY, p, Node.ANY)).doubleValue() / size ;
		double oc = new Double(graphStatisticsHandler.getStatistic(Node.ANY, Node.ANY, o)).doubleValue() / size ;
		
		log.debug("Cost: " + sc + ", " + s) ;
		log.debug("Cost: " + pc + ", " + p) ;
		log.debug("Cost: " + oc + ", " + o) ;
		
		// If one of them is 0, believe it and return 0
		if (sc == 0d || pc == 0d || oc == 0d)
			return 0d ;
		
		double c = sc * pc * oc ;
		
		if (c > minCost)
			return c ;
		
		return minCost ;
	}
	
	/**
	 * The Jena graph statistics handler does not support
	 * joined triple pattern selectivity estimation.
	 * The method uses the variable counting heuristic
	 * to return a cost for joined triple patterns.
	 * 
	 * @param triple1
	 * @param triple2
	 * @return Double
	 */
	public double getCost(Triple triple1, Triple triple2)
	{
		return vc.getCost(triple1, triple2) ;
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