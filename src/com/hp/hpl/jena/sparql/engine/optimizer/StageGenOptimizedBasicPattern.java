/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.optimizer;

import java.util.List;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.mem.faster.GraphMemFaster;
import com.hp.hpl.jena.sparql.ARQException;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.main.Stage;
import com.hp.hpl.jena.sparql.engine.main.StageBasic;
import com.hp.hpl.jena.sparql.engine.main.StageGenerator;
import com.hp.hpl.jena.sparql.engine.main.StageList;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.sparql.engine.optimizer.core.BasicPatternOptimizer;
import com.hp.hpl.jena.sparql.engine.optimizer.util.Constants;
import com.hp.hpl.jena.sparql.engine.optimizer.util.Config;

/**
 * The class implements the ARQ optimizer for basic graph patterns.
 * Then BasicPattern is statically optimized and a BasicStage is 
 * compiled out of the optimized BasicPattern.
 * 
 * @author Markus Stocker
 * @version $Id$
 */

public class StageGenOptimizedBasicPattern implements StageGenerator 
{
	private StageGenerator other = null ;
	private Config config = null ;
	
	public StageGenOptimizedBasicPattern(StageGenerator other, Config config)
	{	
		this.other = other ;
		this.config = config ;
	}
	
	/**
	 * The method compiles a BasicPattern into a StageList. The BasicPattern 
	 * is statically optimized (i.e. reordered by some heuristics, e.g. selectivity estimation)
	 * 
	 * @param pattern
	 * @param execCxt
	 * @return StageList
	 * @see com.hp.hpl.jena.sparql.engine.main.StageGenerator#compile(com.hp.hpl.jena.sparql.core.BasicPattern, com.hp.hpl.jena.sparql.engine.ExecutionContext)
	 */
	public StageList compile(BasicPattern pattern, ExecutionContext execCxt)
	{
		StageList sList = new StageList() ;
		Context context = execCxt.getContext() ;
		Graph graph = execCxt.getActiveGraph() ;
		
		// Do this only for in-memory models
		if (graph instanceof GraphMemFaster)
		{
			// This is mainly for test cases (TestEnabled)
			context.set(Constants.isEnabled, true) ;
			BasicPatternOptimizer optimizer = new BasicPatternOptimizer(context, graph, pattern, config) ;
			BasicPattern optimized = optimizer.optimize() ;
			
			/*
			 * Check for consistency between the original and the optimized pattern.
			 * The two patterns are consistent iff the optimized pattern contains every 
			 * triple contained in the original pattern and the size of the two patterns
			 * is equal.
			 */
			if (! isConsistent(pattern, optimized))
				throw new ARQException("Optimizer returned an inconsistent pattern: " + pattern + " " + optimized) ;
			
			Stage basicStage = new StageBasic(optimized) ;
			sList.add(basicStage) ;
        
			return sList ;
		}
		
		context.set(Constants.isEnabled, false) ;
		
		return other.compile(pattern, execCxt) ;
	}
	
	private boolean isConsistent(BasicPattern pattern, BasicPattern optimized)
	{
		List patternL = pattern.getList() ;
		List optimizedL = optimized.getList() ;
		
		if (patternL.size() == optimizedL.size() && patternL.containsAll(optimizedL))
			return true ;
		
		return false ;
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