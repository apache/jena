/*
 *  (c) Copyright Hewlett-Packard Company 2003
 *  All rights reserved.
 *
 */


package com.hp.hpl.jena.db.impl;

import java.util.List;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * Interface for a specialized graph.
 * 
 * Each specialized graph is optimized for a particular type of statement.
 * 
 * An implemenation of GraphRDB will contain a list of specialized graphs
 * and will attempt to perform each operation on each specialized graph
 * in the list until one indicates the operation is complete.
 * 
 * The list of specialized graphs is immutable.  This aids optimization.
 * For example, if a specialied graph is asked to perform an operatin 
 * on a triple, and it knows that it would have added it if asked, then 
 * it can advise the calling GraphRDB that the operaton is complete even 
 * though it doesn't know anything about other specialized graphs later
 * in the list.
 *
 * @author csayers
 * @version $Revision: 1.3 $
 * 
 */
public interface SpecializedGraph {
        
	/** 
	 * Attempt to add a triple to the specialized graph
	 * 
	 * Note that when calling add, the call will either fail (complete=false)
	 * indicating the graph can not store the triple, or succeed (complete=true)
	 * indicating that a subsequent call to contains(triple) will return true
	 * and that the add operation is complete.
	 * Adding the same triple twice is not an error and should still cause
	 * complete to be true.
	 * 
	 * If the triple can't be stored for any reason other than incompatability
	 * (for example, a lack of disk space) then the implemenation should throw
	 * a runtime exception.
	 * 
	 * @param t is the triple to be added
	 * @param complete is true if a subsequent call to contains(triple) will return true.
	 */
	public void add(Triple t, CompletionFlag complete);
    
	/** 
	 * Attempt to add a list of triples to the specialized graph
	 * 
	 * As each triple is successfully added it is removed from the List.
	 * If complete is true then the entire List was added and the List will 
	 * be empty upon return.  if complete is false, then at least one triple 
	 * remains in the List.
	 * 
	 * If a triple can't be stored for any reason other than incompatability
	 * (for example, a lack of disk space) then the implemenation should throw
	 * a runtime exception.
	 * 
	 * @param triples List of triples to be added.  This is modified by the call.
	 * @param complete is true if a subsequent call to contains(triple) will return true for all triples originally in the List.
	 */
	public void add(List triples, CompletionFlag complete);
    
    /** 
     * Attempt to add all the triples from a graph to the specialized graph
     * 
     * Caution - this call changes the graph passed in, deleting from 
     * it each triple that is successfully added.
     * 
     * Note that when calling add, if complete is true, then the entire
     * graph was added successfully and the graph g will be empty upon
     * return.  If complete is false, then some triples in the graph could 
     * not be added.  Those triples remain in g after the call returns.
     * 
     * If the triple can't be stored for any reason other than incompatability
     * (for example, a lack of disk space) then the implemenation should throw
     * a runtime exception.
     * 
	 * @param g is a graph containing triples to be added
	 * @param complete is true if a subsequent call to contains(triple) will return true for any triple in g.
     */
    public void add(Graph g, CompletionFlag complete);

    /** 
     * Attempt to delete a triple from the specialized graph
     * 
	 * @param t is the triple to be deleted
	 * @param complete is true if either (i) the triple was in the graph and was deleted, or 
	 * (ii) the triple was not in the graph the graph can guarantee that a call to add(Triple)
	 * would have succeeded, had it been made for that same triple.
     */
    public void delete(Triple t, CompletionFlag complete);
    
	/** 
	 * Attempt to delete a list of triples from the specialized graph
	 * 
	 * As each triple is successfully deleted it is removed from the List.
	 * If complete is true then the entire List was deleted and the List will 
	 * be empty upon return.  If complete is false, then at least one triple 
	 * remains in the List.
	 * 
	 * @param triples List of triples to be deleted.  This is modified by the call.
	 * @param complete is true iff delete(Triple, complete) would have set 
	 * complete==true for all triples in the List.
	 */
	public void delete(List triples, CompletionFlag complete);

    /** 
     * Compute the number of unique triples added to the Specialized Graph.
     * 
     * @return int count.
     */
    public int tripleCount();
    
	/**
	 * Tests if a triple is contained in the specialized graph
	 * @param t is the triple to be tested
	 * @param complete is true if the graph can guarantee that no other specialized graph 
     * could hold any matching triples.
	 * @return boolean result to indicte if the tripple was contained
	 */
    public boolean contains(Triple t, CompletionFlag complete);
            
	/**
	 * Finds matching triples contained in the specialized graph
	 * @param m
	 * @param complete is true if the graph can guarantee that no other specialized graph 
     * could hold any matching triples.
	 * @return ExtendedIterator which iterates over any matching triples
	 */
    public ExtendedIterator find(TripleMatch m, CompletionFlag complete);
    
    /**
        Finds matching triples contained in the specialized graph
        @param s the subject of the match
        @param p the predicate of the match
        @param o the object of the match
        @param complete is true if the graph can guarantee that no other specialized graph 
        could hold any matching triples.
        @return ExtendedIterator which iterates over any matching triples
    */
    public ExtendedIterator find( Node s, Node p, Node o, CompletionFlag complete );
    
    /**
     * Clear the specialized graph
     * 
     * This removes any triples stored in the graph.
     */
    public void clear();
    
    /**
     * Close specialized graph.
     * 
     * This frees any resources used by the graph.
     * It is an error to perform any operation on a graph after closing it.
     */
    public void close();
    
    public class CompletionFlag {
    	boolean done;
    	
    	public CompletionFlag() { done = false; }
    	
    	public boolean isDone() { return done; }
    	
    	public void setDone() { done = true; }   	
    }
}

/*
    (c) Copyright Hewlett-Packard Company 2003
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
