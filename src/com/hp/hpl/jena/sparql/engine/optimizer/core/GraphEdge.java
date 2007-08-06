/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.optimizer.core;

import java.util.List;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class implements a GraphEdge used to describe an edge of a Graph.
 * A Graph is used as absraction of a BasicPattern, thus, a GraphEdge is
 * used as abstraction of the join between two triple patterns.
 * 
 * @author Markus Stocker
 * @version $Id$
 */

public class GraphEdge 
{
	// Object id for the graph node, OID are prime numbers only
	private int oid = Integer.MIN_VALUE ;
	// The weight for the graph node
	private double weight = Double.MIN_VALUE ;
	// The list of GraphNodes for the GraphEdge
	private List nodes = new ArrayList() ; // List<GraphNode>
	private static Log log = LogFactory.getLog(GraphEdge.class) ;
	
	/**
	 * Constructur. A GraphEdge is described by it's OID (object ID), the weight
	 * calculated by some heuristic, and the two nodes.
	 * 
	 * @param oid
	 * @param weight
	 * @param node1
	 * @param node2
	 */
	public GraphEdge(int oid, GraphNode node1, GraphNode node2, double weight) 
	{ 
		this.oid = oid ;
		this.weight = weight ;
		
		nodes.add(node1) ;
		nodes.add(node2) ;
		
		log.debug("Create GraphEdge: " + oid + "\t(" + node1.oid() + "," + node2.oid() + ")\t" + weight) ;
	}
	
	/**
	 * The OID of the GraphEdge. The OID has to be a positive integer.
	 * 
	 * @return int
	 */
	public int oid()
	{
		return oid ;
	}
	
	/**
	 * The weight of the GraphEdge. The weight has to be a positive double.
	 * 
	 * @return double
	 */
	public double weight()
	{
		return weight ;
	}
	
	/**
	 * The list of nodes.
	 * 
	 * @return List
	 */
	public List nodes()
	{
		// List<GraphNode>
		return nodes ;
	}
	
	/**
	 * The node1
	 * 
	 * @return GraphNode
	 */
	public GraphNode node1()
	{
		return (GraphNode)nodes.get(0) ;
	}
	
	/**
	 * The node2
	 * 
	 * @return GraphNode
	 */
	public GraphNode node2()
	{
		return (GraphNode)nodes.get(1);
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