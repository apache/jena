/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: Graph.java,v 1.9 2003-06-19 13:56:39 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph;

import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.shared.PrefixMapping;

import com.hp.hpl.jena.util.iterator.ExtendedIterator;


/**
 * @author Jeremy Carroll
 *
 * 
 */
public interface Graph  {
	
    /** 
        true if this graph's content depends on the other graph. May be
        pessimistic (ie return true if it's not sure). Typically true when a
        graph is a composition of other graphs, eg union.
        
         @param other the graph this graph may depend on
         @return false if this does not depend on other 
    */
    boolean dependsOn( Graph other );
    
    /** returns this Graph's query handler */
    QueryHandler queryHandler();
    
    /** returns this Graph's transaction handler */
    TransactionHandler getTransactionHandler();
    
    /** returns this Graph's bulk-update handler */
    BulkUpdateHandler getBulkUpdateHandler();
    
    /** returns this Graph's capabilities */
    Capabilities getCapabilities();
    
    /** 
        returns this Graph's reifier. Each call on a given Graph gets the same
        Reifier object.
    */
    Reifier getReifier();
    
    /**
        returns this Graph's prefix mapping. Each call on a given Graph gets the
        same PrefixMapping object, which is the one used by the Graph.
    */
    PrefixMapping getPrefixMapping();
    
    /** adds the triple t (if possible) to the set belong to the graph */
	void add(Triple t) throws UnsupportedOperationException;
    
    /** removes the triple t (if possible) from the set belonging to this graph */   
	void delete(Triple t) throws UnsupportedOperationException;
      
	  /** Returns an iterator over Triple.
	   */
	ExtendedIterator find(TripleMatch m);
    
	  /** Returns an iterator over Triple.
	   */
	ExtendedIterator find(Node s,Node p,Node o);
    
	/**
	 * Compare this graph with another using the method
	 * described in 
	 * <a href="http://www.w3.org/TR/rdf-concepts#section-Graph-syntax">
     * http://www.w3.org/TR/rdf-concepts#section-Graph-syntax
     * </a>
	 * @param g Compare against this.
	 * @return boolean True if the two graphs are isomorphic.
	 */
	boolean isIsomorphicWith(Graph g);
    
    /** true iff the graph contains a triple matching (s, p, o).
        [extended notion of `Node` including `null`]
    */
    boolean contains( Node s, Node p, Node o );
    
    /** true iff the graph contains the triple t. */
    boolean contains( Triple t );
    
	/** Free all resources, any further use of this Graph is an error.
	 */
	void close();
	/* TODO
	 *  GraphListener stuff
	 *  complex find
	 * 
	 */ 
	 int size() throws UnsupportedOperationException;
	 
	 int ADD     = 1;
	 int DELETE  = 2;
	 int SIZE    = 4;
	 int ORDERED = 8;
     
	 /**
	  * Returns the bitwise or of ADD, DELETE, SIZE and ORDERED,
	  * to show the capabilities of this implementation of Graph.
	  * So a read-only graph that finds in an unordered fashion,
	  * but can tell you how many triples are in the graph returns
	  * SIZE.
	  */
	 int capabilities();
	 /** Issue listSubjects() listNameSpaces().
	  *  Old code noted that stores could easily
	  *  give one of each filler for subject or property.
	  * Current code uses brute force in ModelCom to achieve
	  * same effect.
	  * Also consider Model.listSubjectsWithProperty()
	  */

}

/*
    (c) Copyright Hewlett-Packard Company 2002
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
