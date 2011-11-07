/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.db.impl;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * Interface for a specialized graphs that are optimized for reification.
 *
 * @author csayers
 * @version $Revision: 1.1 $
 * 
 */
public interface SpecializedGraphReifier extends SpecializedGraph {
        
	/** 
	 * Add a reified triple to the specialized graph.
	 * 
	 * Note that when calling add, the call will either fail (complete=false)
	 * indicating the graph can not store the quad, or succeed (complete=true)
	 * indicating that a subsequent call to contains(node, triple) will return true
	 * and that the add operation is complete.
	 * Adding the same triple twice is not an error.  However adding the same 
	 * node twice is an error and should throw a Reifier.AlreadyReifiedException.
	 * 
	 * @param n is the Node to be added
	 * @param t is the triple to be added
	 * @param complete is true if a subsequent call to contains(triple) will return true.
	 * @throws Reifier.AlreadyReifiedException if the node already reifies a triple
	 */
	public void add(Node n, Triple t, CompletionFlag complete) throws AlreadyReifiedException;
		
	/** 
	 * Attempt to delete a reified triple from the specialized graph.
	 * 
	 * @param t is the triple to be deleted
	 * @param complete is true if either (i) the triple was in the graph and was deleted, or 
	 * (ii) the triple was not in the graph the graph can guarantee that a call to add(Triple)
	 * would have succeeded, had it been made for that same triple.
	 */
	public void delete(Node n, Triple t, CompletionFlag complete);
	
	/**
	 * Tests if a reified triple is contained in the specialized graph.
	 * @param n is the node to be tested - may be null to indicate any node
	 * @param t is the triple to be tested
	 * @param complete is true if the graph can guarantee that no other specialized graph 
	 * could hold any matching triples.
	 * @return boolean result to indicte if the tripple was contained
	 */
	public boolean contains(Node n, Triple t, CompletionFlag complete);
                        
	/**
	 * Finds matching reified triples in the specialized graph and returns their nodes.
//	 * @param t the TripleMatch
     * @param t the Triple
	 * @param complete is true if the graph can guarantee that no other specialized graph 
	 * could hold any matching triples.
	 * @return ExtendedIterator which iterates over any matching nodes
	 */
//	public ExtendedIterator findReifiedNodes(TripleMatch t, CompletionFlag complete);
	public ExtendedIterator<Node> findReifiedNodes(Triple t, CompletionFlag complete);


	/**
	 * Finds the reified triple corresponding to a particular node in the specialized graph.
	 * @param t the TripleMatch
	 * @param complete is true if the graph can guarantee that no other specialized graph 
	 * could hold any matching triples.
	 * @return ExtendedIterator which iterates over any matching nodes
	 */
	public Triple findReifiedTriple(Node n, CompletionFlag complete);
}
