/*
 *  (c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 *  All rights reserved.
 *
 */


package com.hp.hpl.jena.db.impl;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * Interface for a specialized graphs that are optimized for reification.
 *
 * @author csayers
 * @version $Revision: 1.5 $
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
	public ExtendedIterator findReifiedNodes(Triple t, CompletionFlag complete);


	/**
	 * Finds the reified triple corresponding to a particular node in the specialized graph.
	 * @param t the TripleMatch
	 * @param complete is true if the graph can guarantee that no other specialized graph 
	 * could hold any matching triples.
	 * @return ExtendedIterator which iterates over any matching nodes
	 */
	public Triple findReifiedTriple(Node n, CompletionFlag complete);
}

/*
    (c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
