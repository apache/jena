/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: Graph.java,v 1.21 2003-08-25 16:54:40 chris-dollin Exp $
*/

package com.hp.hpl.jena.graph;

import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.shared.*;

import com.hp.hpl.jena.util.iterator.*;


/**
    The interface to be satisfied by implementations maintaining collections
    of RDF triples. The core interface is small (add, delete, find, contains) and
    is augmented by additional classes to handle more complicated matters
    such as reification, query handling, bulk update, event management,
    and transaction handling.
<p>
    For <code>add(Triple)</code> see GraphAdd.
    
    @author Jeremy Carroll, Chris Dollin
*/
public interface Graph  extends GraphAdd
    {
	
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
        Answer this Graph's event manager.
    */
    GraphEventManager getEventManager(); 
   
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

    /** 
        Remove the triple t (if possible) from the set belonging to this graph 
    
        @param  t the triple to add to the graph
        @throws DeleteDeniedException if the triple cannot be removed  
    */   
	void delete(Triple t) throws DeleteDeniedException;
      
    /** 
        Returns an iterator over all the Triples that match the triple pattern.
	   
        @param m a Triple[Match] encoding the pattern to look for
        @return an iterator of all triples in this graph that match m
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
    
    /** 
        Answer true iff the graph contains a triple matching (s, p, o).
        s/p/o may be concrete or fluid. Equivalent to find(s,p,o).hasNext,
        but an implementation is expected to optimise this in easy cases.
    */
    boolean contains( Node s, Node p, Node o );
    
    /** 
        Answer true iff the graph contains a triple that t matches; t may be
        fluid.
    */
    boolean contains( Triple t );
    
	/** Free all resources, any further use of this Graph is an error.
	 */
	void close();
    
    /**
        Answer true iff this graph is empty. [Used to be in QueryHandler, but moved in
        here because it's a more primitive operation.]
    */
    boolean isEmpty();
    
    /**
     * For a concrete graph this returns the number of triples in the graph. For graphs which
     * might infer additional triples it results an estimated lower bound of the number of triples.
     * For example, an inference graph might return the number of triples in the raw data graph. 
     */
	 int size();

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
