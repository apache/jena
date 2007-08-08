/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.optimizer.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.engine.optimizer.core.BasicPatternGraph;
import com.hp.hpl.jena.sparql.engine.optimizer.heuristic.HeuristicBasicPattern;

/**
 * Implementation of basic graph pattern optimizer.
 * 
 * @author Markus Stocker
 * @version $Id$
 */

public class BasicPatternOptimizer extends OptimizerBase 
{
	private BasicPattern pattern = null ;
	private BasicPatternGraph basicPatternGraph = null ;
	private HeuristicBasicPattern heuristic = null ;
	private static Log log = LogFactory.getLog(BasicPatternOptimizer.class) ;
	
	/**
	 * Initialize the BGP optimizer.
	 * The default heuristic is used (variable counting)
	 * 
	 * @param cxt
	 * @param pattern
	 */
	public BasicPatternOptimizer(Context context, Graph graph, BasicPattern pattern)
	{
		super(context, graph) ;
		
		this.pattern = pattern ;
		this.heuristic = broker.getBasicPatternHeuristic() ;
	}
	
	/**
	 * Init the BGP optimizer with a specific heuristic.
	 * 
	 * @param cxt
	 * @param pattern
	 * @param heuristic
	 */
	/*
	public BasicPatternOptimizer(Context cxt, BasicPattern pattern, HeuristicBasicPattern heuristic)
	{
		super(cxt) ;
		
		this.pattern = pattern ;
		this.heuristic = heuristic ;
	}
	*/
	
	/**
	 * Given a BasicPattern, the method abstracts the BasicPattern as a BasicPatternGraph
	 * and inits the optimization program.
	 * 
	 * @return BasicPattern
	 */
	public BasicPattern optimize()
	{
		log.debug("Init ARQo") ;

		// Create an abstracted graph representation of the BasicPattern
		basicPatternGraph = new BasicPatternGraph(pattern, heuristic) ;
		
		// Optimize the abstracted graph and return the optimized BasicPattern
		return basicPatternGraph.optimize() ;
	}
	
	/**
	 * Return the BasicPatternGraph, used mainly to explain optimizations.
	 * 
	 * @return BasicPatternGraph
	 */
	public BasicPatternGraph getBasicPatternGraph()
	{
		// If the method is executed without previously executing optimize(), optimize first
		if (basicPatternGraph == null)
			optimize() ;
		
		return basicPatternGraph ;
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