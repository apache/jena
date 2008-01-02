/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.optimizer.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.Triple;

/**
 * This class implements a GraphNode used to describe the node of a Graph.
 * A Graph is used as abstraction of a BasicPattern, thus, a GraphNode is 
 * used as abstraction of a triple pattern. Graph nodes are identified by
 * object IDs and mapped by OID with the triple pattern.
 * 
 * @author Markus Stocker
 */

public class GraphNode 
{
	// Object id for the graph node
	private int oid = Integer.MIN_VALUE ;
	// The triple pattern which corresponds to the node
	private Triple triple = null;
	// The weight for the graph node
	private double weight = Double.MIN_VALUE ;
	private static Log log = LogFactory.getLog(GraphNode.class) ;
	
	/**
	 * Constructur. A GraphNode is described by it's OID (object ID) and the weight
	 * calculated by some heuristic.
	 * 
	 * @param oid
	 * @param weight
	 */
	public GraphNode(int oid, Triple triple, double weight) 
	{ 
		this.oid = oid ;
		this.triple = triple ;
		this.weight = weight ;
		
		log.debug("Create GraphNode: " + oid + "\t" + triple.toString() + "\t" + weight) ;
	}
	
	/**
	 * The OID of the GraphNode. The OID has to be a positive integer.
	 * 
	 * @return Integer
	 */
	public int oid()
	{
		return oid ;
	}
	
	/**
	 * The weight of the GraphNode. The weight has to be a positive double.
	 * 
	 * @return Double
	 */
	public double weight()
	{
		return weight ;
	}
	
	/**
	 * The triple pattern which corresponds to the graph node
	 * 
	 * @return Triple
	 */
	public Triple triple()
	{
		return triple ;
	}
	
	public boolean equals(GraphNode node)
	{
		if (this.triple.matches(node.triple()))
			return true ;
		
		return false ;
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