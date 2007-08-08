/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.optimizer.heuristic;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.sparql.engine.optimizer.heuristic.VariableCounting;
import com.hp.hpl.jena.sparql.engine.optimizer.heuristic.GraphStatisticsHeuristic;

/**
 * The class is a registry for heuristics
 * 
 * @author Markus Stocker
 * @version $Id$
 */

public class HeuristicsRegistry 
{
	private Map registry = new HashMap() ; // Map<String, Heuristic>
	private static Log log = LogFactory.getLog(HeuristicsRegistry.class) ;
	
	public static final String BGP_VARIABLE_COUNTING = "BGP_VARIABLE_COUNTING" ;
	public static final String BGP_GRAPH_STATISTICS_HANDLER = "BGP_GRAPH_STATISTICS_HANDLER" ;
	
	public HeuristicsRegistry() {}
	
	/** The constructor initializes the registry of available heuristics */
	public HeuristicsRegistry(Context context, Graph graph)
	{		
		add(BGP_VARIABLE_COUNTING, new VariableCounting()) ;
		add(BGP_GRAPH_STATISTICS_HANDLER, new GraphStatisticsHeuristic(graph)) ;
	}	
	
	/**
	 * Extract a heuristic instance given it's name,
	 * which is used as key for the registry.
	 * 
	 * @param heuristic
	 * @return Heuristic
	 */
	public Heuristic get(String heuristic)
	{
		if (registry.containsKey(heuristic))
			return (Heuristic)registry.get(heuristic) ;
		else
			log.fatal("Heuristic not found in registry: " + heuristic) ;
		
		return null ;
	}
	
	/**
	 * Check if a heuristic is registred
	 * 
	 * @param heuristic
	 * @return boolean
	 */
	public boolean isRegistred(String heuristic)
	{
		if (registry.containsKey(heuristic))
			return true ;
		
		return false ;
	}
	
	/**
	 * Add a heuristic to the registry including the 
	 * instance of the heuristic and the corresponding name.
	 * 
	 * @param name
	 * @param heuristic
	 */
	public void add(String name, Heuristic heuristic)
	{
		registry.put(name, heuristic) ;
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