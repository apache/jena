/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            24 Jan 2003
 * Filename           $RCSfile: OntologyList.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     @releaseStatus@ $State: Exp $
 *
 * Last modified on   $Date: 2003-01-29 22:13:34 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology;


// Imports
///////////////
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import com.hp.hpl.jena.rdf.model.*;

import java.util.List;


/**
 * <p>
 * Provides a convenience encapsulation for lists formed from chains of RDF
 * statements arranged to form a head/tail cons-cell structure.  The properties
 * that form the links between cells, and from cells to values, are specified by
 * a vocabulary interface, so this abstraction is designed to cope equally well
 * with DAML lists, RDF lists, and user-defined lists.
 * </p>
 * <p>
 * A well-formed list has cells that are made up of three statements: one
 * denoting the <code>rdf:type</code> of the list cell, one denoting the link
 * to the value of the list at that point, and one pointing to the list tail. If
 * a list cell is not well-formed, list operations may fail in unpredictable
 * ways. However, to explicitly check that the list is well-formed at all times
 * is expensive.  Therefore the list operates in two modes: in <i>strict</i>
 * mode, the well-formedness of the list is checked at the start of each list
 * operation, and an {@link InvalidListException} is thrown if the list is not
 * well- formed. This ensures that list operations are safe, but will slow down
 * processing. In <i>non-strict</i> mode, this checking is switched off, but can
 * be invoked explicitly by clients by calling {@link #isValid}. By default, RDF
 * lists are processed in non-strict mode.
 * </p>
 * <p>
 * @todo Still to investigate: interaction of lists with writers to generate
 * appropriately compact serialisations, such as <code>rdf:parseType=&quot;
 * Collection&quot;</code>
 * </p>
 * 
 * @author Ian Dickinson, HP Labs 
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version Release ($Id: OntologyList.java,v 1.1 2003-01-29 22:13:34 ian_dickinson Exp $)
 */
public interface OntologyList
{
    // Constants
    //////////////////////////////////


    // External signature methods
    //////////////////////////////////

    /**
     * <p>
     * Answer the number of elements in the list.
     * </p>
     * 
     * @return The size of the list as an integer
     */
    public int size();
    
    
    /**
     * <p>
     * Answer the value that is at the head of the list.
     * </p>
     * 
     * @return The value that is associated with the head of the list.
     * @exception EmptyListException if this list is the empty list
     */
    public RDFNode getHead();
    
    
    /**
     * <p>
     * Update the head of the list to have the given value, and return the
     * previous value.
     * </p>
     * 
     * @param value The value that will become the value of the list head
     * @exception EmptyListException if this list is the empty list
     */
    public RDFNode setHead( RDFNode value );
    
    
    /**
     * <p>
     * Answer the list that is the tail of this list.
     * </p>
     * 
     * @return The tail of the list, as a list
     * @exception EmptyListException if this list is the empty list
     */
    public RDFList getTail();
    
    
    /**
     * <p>
     * Update the list cell at the front of the list to have the given list as
     * tail. The old tail is returned, and remains in the model.
     * </p>
     * 
     * @param tail The new tail for this list.
     * @return The old tail.
     */
    public RDFList setTail( RDFList tail );
    
    
    /**
     * Answer true if this list is the empty list.
     * 
     * @return True if this is the empty (nil) list, otherwise false.
     */
    public boolean isEmpty();
    
    
    /**
     * <p>
     * Answer the vocabulary that defines the properties and resources used to
     * make the assocations forming this list.  Every list must have a defined
     * vocabulary.
     * </p>
     * 
     * @return A list vocabulary
     */
    public OntologyList.Vocabulary getVocabulary();
    
    
    /**
     * <p>
     * Return a reference to a new list cell whose head is <code>value</code>
     * and whose tail is this list.
     * </p>
     * 
     * @param value A new value to add to the head of the list
     * @return The new list, whose head is <code>value</code>
     */
    public RDFList add( RDFNode value );
    
    
    /**
     * <p>
     * Answer the node that is the i'th element of the list, assuming that the 
     * head is item zero.  If the list is too short to have an i'th element,
     * throws a {@link ListIndexException}.
     * </p>
     * 
     * @param i The index into the list, from 0
     * @return The list value at index i, or null
     * @exception ListIndexException if the list has fewer than (i + 1)
     * elements.
     */
    public RDFNode get( int i );
    
    
    /**
     * <p>
     * Replace the value at the i'th position in the list with the given value.
     * If the list is too short to have an i'th element, throws a {@link
     * ListIndexException}.
     * </p>
     * 
     * @param i The index into the list, from 0
     * @param value The new value to associate with the i'th list element
     * @return The value that was previously at position i in the list
     * @exception ListIndexException if the list has fewer than (i + 1)
     * elements.
     */
    public RDFNode replace( int i );
    
    
    /**
     * <p>
     * Answer true if the given node appears as the value of a value of any
     * of the cells of this list.
     * </p>
     * 
     * @param value A value to test for
     * @return True if the list contains value.
     */
    public boolean contains( RDFNode value );
    
    
    /**
     * <p>
     * Answer the index of the first occurrence of the given value in the list,
     * or -1 if the value is not in the list.
     * </p>
     * 
     * @param value The value to search for
     * @return The index of the first occurrence of value in the list, or
     * <code>-1</code> if not found.
     */
    public int indexOf( RDFNode value );
    
    
    /**
     * <p>
     * Answer the index of the first occurrence of the given value in the list
     * after index <code>start</code>, or -1 if the value is not in the list
     * after the given start point.
     * </p>
     * 
     * @param value The value to search for
     * @param start The index into the list to start searching from
     * @return The index of the first occurrence of value in the list not less
     * than <code>start</code>, or <code>-1</code> if not found.
     * @exception ListIndexException if <code>start</code> is greater than the
     * length of the list.
     */
    public int indexOf( RDFNode value, int start );
    
    
    /**
     * <p>
     * Answer a new list that is formed by adding each element of this list to
     * the head of the given <code>list</code>. This is a non side-effecting
     * operation on either this list or the given list, but generates a copy
     * of this list.  For a more storage efficient alternative, see {@link
     * #concatenate}.
     * </p>
     * 
     * @param list The argument list
     * @return A new RDFList that contains all of this elements of this list,
     * followed by all of the elements of the given list.
     */
    public RDFList append( RDFList list );
    
    
    /**
     * <p>
     * Change the tail of this list to point to the given list, so that this
     * list becomes the list of the concatenation of the elements of both lists.
     * This is a side-effecting operation on this list; for a non side-effecting
     * alternative, see {@link #append}.
     * </p>
     * 
     * @param list The argument list to concatenate to this list
     */
    public void concatenate( RDFList list );
    
    
    /**
     * <p>
     * Apply a function to each node of the list in turn.
     * </p>
     * 
     * @param fn The function to apply to each list node.
     */
    public void apply( ApplyFn fn );
    
    
    /**
     * <p>
     * Apply a function to each node of the list in turn, accumulating the
     * results in an accumulator. The final value of the accumulator is returned
     * as the value of <code>reduce()</code>.
     * </p>
     * 
     * @param fn The reduction function to apply
     * @param initial The initial value for the accumulator
     * @return The final value of the accumulator.
     */
    public Object reduce( ReduceFn fn, Object initial );
    
    
    /**
     * <p>
     * Remove the value from the head of the list.  The tail of the list remains
     * in the model.
     * </p>
     * 
     * @return RDFNode The value of the head of the list, that has been removed.
     */
    public RDFNode removeHead();
    
    
    /**
     * Remove all of the elements of this list from the model.
     */
    public void removeAll();
    
    
    /**
     * <p>
     * Answer an iterator over the elements of the list. Note that this iterator
     * does not take a snapshot of the list, so changes to the list statements
     * in the model while iterating will affect the behaviour of the iterator.
     * To get an iterator that is not affected by model changes, use {@link
     * #asJavaList}.
     * </p>
     * 
     * @return A closable iterator over the elements of the list.
     */
    public ClosableIterator iterator();
    
    
    /**
     * <p>
     * Answer the contents of this RDF list as a Java list of RDFNode values.
     * </p>
     * 
     * @return The contents of this list as a Java List.
     */
    public List asJavaList();
    
    
    /**
     * <p>
     * Answer true if this list is operating in strict mode, in which the
     * well- formedness of the list is checked at every operation.
     * </p>
     * 
     * @return True if the list is being strictly checked.
     */
    public boolean isStrict();
    
    
    /**
     * <p>
     * Set a flag to indicate whether to strictly check the well-formedness of
     * the list at each operation. Default false.
     * </p>
     * 
     * @param strict If true, list will be checked strictly.
     */
    public void setStrict( boolean strict );
    
    
    /**
     * <p>
     * Answer true if the list is well-formed, by checking that each node is
     * correctly typed, and has a head and tail pointer from the correct
     * vocabulary.
     * </p>
     * 
     * @return True if the list is well-formed.
     */
    public boolean isValid();
    
    
    
    //==============================================================================
    // Inner class definitions
    //==============================================================================

    public static interface Vocabulary {
        
        /**
         * <p>Answer the constant Resource that represents the end of the list
         * in this formulation.
         * </p>
         * 
         * @return The nil resource.
         */
        public Resource getNil();
        
        /**
         * <p>Answer the Property that represents the link to the value at the
         * head of the list.
         * </p>
         * 
         * @return The list head property
         */
        public Property getHead();
        
        /**
         * <p>Answer the Property that represents the link to the tail of the
         * list.
         * </p>
         * 
         * @return The list tail property
         */
        public Property getTail();
        
        /**
         * <p>Answer the resource that will be the type of each cell in the list
         * </p>
         * 
         * @return A resource that should be the rdf:type of each list cell
         */
        public Resource getCellType();
    }
    
    
    /**
     * Interface that encapsulates a function to apply to every element in a
     * list.
     */
    public static interface ApplyFn {
        /** 
         * <p>
         * Apply a function to the given RDF node 
         * </p>
         * 
         * @param node A node from the list
         */
        public void apply( RDFNode node );
    }
    
    
    /**
     * Interface that encapsulates a function to apply to each element of a list
     * in turn, and passing the result to an accumulator.
     */
    public static interface ReduceFn {
        /** 
         * <p>
         * Apply a function to the given RDF node 
         * </p>
         * 
         * @param node A node from the list
         * @param accumulator The accumulator for the reduction, which will
         * either be an initial value passed to {@link RDFList#reduce}, or the
         * output from <code>reduce</code> applied to the previous node in the
         * list.
         * @return The result of applying the reduction function to the current
         * node and the accumulator.
         */
        public Object reduce( RDFNode node, Object accumulator );
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
