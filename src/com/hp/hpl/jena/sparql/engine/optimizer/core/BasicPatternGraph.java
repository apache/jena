/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.optimizer.core;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.engine.optimizer.core.GraphNode;
import com.hp.hpl.jena.sparql.engine.optimizer.core.ConnectedGraph;
import com.hp.hpl.jena.sparql.engine.optimizer.core.BasicPatternJoin;
import com.hp.hpl.jena.sparql.engine.optimizer.heuristic.Heuristic;
import com.hp.hpl.jena.sparql.engine.optimizer.heuristic.HeuristicBasicPattern;

/**
 * Abstraction of a BasicPattern as a Graph. A BasicPattern is abstracted as a list
 * of connected graphs. The class manages this list and constructs the components
 * as ConnectedGraphs.
 * 
 * @author Markus Stocker
 * @version $Id$
 */

public class BasicPatternGraph 
{
	// The heurisic technique used to estimate the costs for (joined triple patterns)
	private HeuristicBasicPattern heuristic ;
	// The list of ConnectedGraph components of the BasicPatternGraph
	private List components = new ArrayList() ; // List<ConntectedGraph>
	private static Log log = LogFactory.getLog(BasicPatternGraph.class) ;
	
	/**
	 * Init the BasicPatternGraph of a BasicPattern as a list of connected graphs.
	 * 
	 * @param pattern
	 * @param heuristic
	 */
	public BasicPatternGraph(BasicPattern pattern, Heuristic heuristic)
	{
		this.heuristic = (HeuristicBasicPattern)heuristic ;
		
		// First get the list of BasicPatterns of triples which are joined together
		List patterns = listJoinedBasicPatterns(pattern) ; // List<BasicPattern>
		
		log.debug("Number of BasicPatternGraph components: " + patterns.size()) ;
		
		/* Create a ConnectedGraph for each identified pattern, 
		 * i.e. each component of BasicPatternGraph */
		for (Iterator iter = patterns.iterator(); iter.hasNext(); )
			buildGraphComponent((BasicPattern)iter.next()) ;
	}
	
	/**
	 * Optimize the BasicPatternGraph by optimizing its components (ConnectedGraph).
	 * 
	 * @return BasicPattern
	 */
	public BasicPattern optimize()
	{
		// Init the optimized BasicPattern
		BasicPattern pattern = new BasicPattern() ;
		
		// Step through the components of BasicPatternGraph ...
		for (Iterator iter = components.iterator(); iter.hasNext(); )
		{
			ConnectedGraph component = (ConnectedGraph)iter.next() ;
			// ... and optimize them.
			pattern.addAll((BasicPattern)component.optimize()) ;
		}
		
		log.debug("Optimized BasicPattern: " + pattern.toString()) ;
		
		return pattern ;
	}
	
	/**
	 * The method returns the size of the components list,
	 * i.e. the number of components identified for the BasicPatternGraph
	 * 
	 * @return Integer
	 */
	public int numberOfConnectedComponents()
	{
		return components.size() ;
	}
	
	/**
	 * Return a component of the BasicPatternGraph.
	 * 
	 * @param index
	 * @return ConnectedGraph
	 */
	public ConnectedGraph getComponent(int index)
	{
		return (ConnectedGraph)components.get(index) ;
	}
	
	/**
	 * Return the list of the BasicPatternGraph components
	 * 
	 * @return List<ConnectedGraph>
	 */
	public List getComponents()
	{
		return components ; // List<ConnectedGraph>
	}
	
	/*
	 * The method returns a list of distinct nodes defined in BasicPattern
	 */
	private Vector getNodes(BasicPattern pattern)
	{
		Vector nodes = new Vector() ; // Vector<Node>
		
		// Set trough the BasicPattern and get a set of nodes
		for (Iterator iter = pattern.iterator(); iter.hasNext(); )
		{
			Triple triple = (Triple)iter.next() ;
			nodes.addAll(getNodes(triple)) ;
		}
		
		return nodes ;
	}
	
	/*
	 * The method returns a list of nodes defined in a triple.
	 * Predicates should not be considered for join evaluation
	 */
	private Vector getNodes(Triple triple)
	{
		Vector nodes = new Vector() ; // Vector<Node>
		
		Node subject = triple.getSubject() ;
		Node predicate = triple.getPredicate() ;
		Node object = triple.getObject() ;
		
		// Check the subject for variable and if it is already contained in the set
		if (!nodes.contains(subject))
			nodes.add(subject) ;
		if (!nodes.contains(predicate))
			nodes.add(predicate) ;
		if (!nodes.contains(object))
			nodes.add(object) ;
		
		return nodes ;
	}
	
	/*
	 * The method identifies if a given triple pattern matches a node
	 * for the S/O. Predicates should not be considered for join 
	 * evaluation.
	 */
	private boolean tripleContainsNode(Triple triple, Node node)
	{		
		if (triple.subjectMatches(node))
			return true ;
		if (triple.predicateMatches(node))
			return true ;
		if (triple.objectMatches(node))
			return true ;

		return false ;
	}
	
	/*
	 * The method creates a set of BasicPatterns with joined triple patterns
	 * out of the original BasicPattern which has to be optimized. 
	 */
	private List listJoinedBasicPatterns(BasicPattern pattern)
	{
		/* Queue of nodes to consider */
		Vector nodes = new Vector() ; // Vector<Node>
		/* The set contains a list of joined BasicPattern */
		List patterns = new ArrayList(); // List<BasicPattern>
		/* Hash set of the triple hash codes. This helper list is used
		 * to consider triples defined in BasicPattern just once */
		Set considered = new HashSet() ; // HashSet<Triple>
		
		// Step through the triples of BasicPattern
		for (Iterator iter = pattern.iterator(); iter.hasNext(); )
		{
			Triple triple1 = (Triple)iter.next() ;
			
			// Process the triple only if not yet considered
			if (! considered.contains(triple1))
			{
				log.debug("Consider triple1: " + triple1) ;
				// If there are no variables, simply add the triple
				addTripleToBasicPattern(patterns, triple1, null) ;
				// Mark the triple as considered
				considered.add(triple1) ;
				// Add the variables of the triple to the queue
				nodes.addAll(getNodes(triple1)) ;

				while (nodes.size() > 0)
				{
					// If the queue contains variables, process them
					Node node = (Node)nodes.remove(0) ;
					log.debug("Check the node: " + node.toString()) ;
					// Search for triple patterns which match the variable
					for (Iterator it = pattern.iterator(); it.hasNext(); )
					{
						Triple triple2 = (Triple)it.next() ;
						
						if (! considered.contains(triple2))
						{
							log.debug("Consider triple2: " + triple2) ;
							// Check if the triple matches the variable
							if (tripleContainsNode(triple2, node))
							{
								log.debug("The triple contains the node: " + triple2.toString()) ;
								// Add the inspected triple to the right BasicPattern of the set
								addTripleToBasicPattern(patterns, triple2, node) ;
								// Mark the triple as considered (for future variable checks)
								considered.add(triple2) ;
								// Add the variables contained in the triple in the queue of vars
								nodes.addAll(getNodes(triple2)) ;
							}
						}
					}
				}
			}
		}
		
		// List<BasicPattern>
		return patterns ;
	}
	
	/*
	 * The method adds a given triple pattern which matches a variable
	 * to the corresponding BasicPattern of a set of BasicPatterns which
	 * contains the matched variable. Thus, triple patterns which share
	 * a variable are clustered into the same BasicPattern. If no BasicPattern
	 * can be found in the set, create a new one and add it to the set.
	 */
	private void addTripleToBasicPattern(List patterns, Triple triple, Node node)
	{
		// First get the right BasicPattern from the set
		// List<BasicPattern>
		for (Iterator iter = patterns.iterator(); iter.hasNext(); )
		{
			BasicPattern pattern = (BasicPattern)iter.next() ;
			
			// Check if the pattern contains the variable
			if (getNodes(pattern).contains(node))
			{
				log.debug("Add the triple to an existing pattern") ;
				
				// The pattern contains the variable, thus add the triple to this BasicPattern
				pattern.add(triple) ;
				
				return ;
			}
		}

		log.debug("Create a new pattern for the triple: " + triple.toString()) ;
		
		// If no BasicPattern is found in the set, create a new one and add it to the set
		BasicPattern pattern = new BasicPattern() ;
		// Add the triple to this BasicPattern
		pattern.add(triple) ;
		// Add the BasicPattern to the set
		patterns.add(pattern) ;
	}
	
	/*
	 * The method creates the ConnectedGraph for a BasicPattern.
	 * Please note that the BasicPattern considered here contains only
	 * a subset of the triples defined for the original BasicPattern.
	 */
	private void buildGraphComponent(BasicPattern pattern)
	{	
		log.debug("Build ConnectedGraph for component: " + pattern.toString()) ;
		
		ConnectedGraph component = new ConnectedGraph() ;
		
		// First we add for each triple pattern a node to the component
		for (Iterator iter = pattern.iterator(); iter.hasNext(); )
		{
			Triple triple = (Triple)iter.next() ;
			double weight = heuristic.getCost(triple) ;
		
			component.createNode(triple, weight) ;
		}
		
		List nodes = component.getNodes() ; // List<GraphNode>
		// Create a temporary list of the nodes, used to identify distinct edges
		List tmp = new ArrayList() ; // List<GraphNode>
		// Make sure to create a copy and not simply a reference to nodes (-> concurrency exception)
		tmp.addAll(nodes) ;
		
		// Second build the set of eadges
		for (Iterator iter1 = nodes.iterator(); iter1.hasNext(); )
		{
			GraphNode node1 = (GraphNode)iter1.next() ;
			
			/* Remove the considered node from the temporary map of nodes 
			 * to avoid twice comparison. Remove the node before iterating
			 * over tmp in order to avoid the comparison of a node with itself. */
			tmp.remove(node1) ;
			
			/* Iterator over the remaining nodes, and check if there is an edge
			 * (i.e. a join between the triples */
			for (Iterator iter2 = tmp.iterator(); iter2.hasNext(); )
			{
				GraphNode node2 = (GraphNode)iter2.next() ;
				
				if (BasicPatternJoin.isJoined(node1, node2))
				{
					double weight = heuristic.getCost(node1.triple(), node2.triple()) ;
					component.createEdge(node1, node2, weight) ;
				}
			}
		}
		
		// Add the component to the list of components of the BasicPatternGraph
		components.add(component) ;
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