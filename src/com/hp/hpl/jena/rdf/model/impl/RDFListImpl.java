/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            24 Jan 2003
 * Filename           $RCSfile: RDFListImpl.java,v $
 * Revision           $Revision: 1.3 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-06-16 13:40:13 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.rdf.model.impl;


// Imports
///////////////
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.vocabulary.*;

import java.util.*;



/**
 * <p>
 * Standard implementation the list abstraction from rdf.model.
 * </p>
 * 
 * @author Ian Dickinson, HP Labs 
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: RDFListImpl.java,v 1.3 2003-06-16 13:40:13 ian_dickinson Exp $
 */
public class RDFListImpl
    extends ResourceImpl
    implements RDFList
{
    // Constants
    //////////////////////////////////


    // Static variables
    //////////////////////////////////

    /**
     * A factory for generating RDFList facets from nodes in enhanced graphs.
     */
    public static Implementation factory = new Implementation() {
        public EnhNode wrap( Node n, EnhGraph eg ) { 
            if (canWrap( n, eg )) {
                return new RDFListImpl( n, eg );
            }
            else {
                throw new JenaException( "Cannot convert node " + n + " to RDFList");
            } 
        }
            
        public boolean canWrap( Node node, EnhGraph eg ) {
            // node will support being an RDFList facet if it has rdf:type rdf:List or equivalent
            return node.equals( listNil().asNode() ) || 
                   eg.asGraph().find( node, RDF.type.asNode(), listType().asNode() ).hasNext();
        }
    };



    // Instance variables
    //////////////////////////////////

    /** Flag to indicate whether we are checking for valid lists during list operations. Default false. */
    protected boolean m_checkValid = false;
    
    /** Error message if validity check fails */
    protected String m_errorMsg = null;
    
    /** Pointer to the node that is the tail of the list */
    protected RDFList m_tail = null;
    
    
    
    // Constructors
    //////////////////////////////////

    /**
     * <p>
     * Construct an implementation of RDFList in the given graph, where the
     * given node is the head of the list.
     * </p>
     * 
     * @param n The node that is the head of the list, currently
     * @param g The enh graph that contains n
     */
    public RDFListImpl( Node n, EnhGraph g ) {
        super( n, g );
    }
    
    
    // External signature methods
    //////////////////////////////////

    // vocabulary terms
    public static Resource listType()  { return RDF.List; }
    public static Resource listNil()   { return RDF.nil; }
    public static Property listFirst() { return RDF.first; }
    public static Property listRest()  { return RDF.rest; }
    
    
    /**
     * <p>
     * Answer the number of elements in the list.
     * </p>
     * 
     * @return The length of the list as an integer
     */
    public int size() {
        if (m_checkValid) {
            checkValid();
        }
        
        int size = 0;
        
        for (Iterator i = iterator(); i.hasNext(); i.next()) {
            size++;
        } 
        return size;
    }
    
    
    /**
     * <p>
     * Answer the value that is at the head of the list.
     * </p>
     * 
     * @return The value that is associated with the head of the list.
     * @exception EmptyListException if this list is the empty list
     */
    public RDFNode getHead() {
        if (m_checkValid) {
            checkValid();
        }
        
        checkNotNil( "Tried to get the head of an empty list" );
        
        return getProperty( listFirst() ).getObject();
    }
    
    
    /**
     * <p>
     * Update the head of the list to have the given value, and return the
     * previous value.
     * </p>
     * 
     * @param value The value that will become the value of the list head
     * @exception EmptyListException if this list is the empty list
     */
    public RDFNode setHead( RDFNode value ) {
        if (m_checkValid) {
            checkValid();
        }
        
        checkNotNil( "Tried to set the head of an empty list" );
        
        // first remove the existing head
        Statement current = getProperty( listFirst() );
        RDFNode n = current.getObject();
        current.remove();
        
        // now add the new head value to the graph
        addProperty( listFirst(), value );
        
        return n;
    }
    
    
    /**
     * <p>
     * Answer the list that is the tail of this list.
     * </p>
     * 
     * @return The tail of the list, as a list
     * @exception EmptyListException if this list is the empty list
     */
    public RDFList getTail() {
        if (m_checkValid) {
            checkValid();
        }
        
        checkNotNil( "Tried to get the tail of an empty list" );
        
        Resource tail = getProperty( listRest() ).getResource();
        return (RDFList) tail.as( RDFList.class );
    }
    
    
    /**
     * <p>
     * Update the list cell at the front of the list to have the given list as
     * tail. The old tail is returned, and remains in the model.
     * </p>
     * 
     * @param tail The new tail for this list.
     * @return The old tail.
     */
    public RDFList setTail( RDFList tail ) {
        if (m_checkValid) {
            checkValid();
        }
        
        checkNotNil( "Tried to set the tail of an empty list" );

        return (RDFList) (setTailAux( this, tail, listRest() )).as( RDFList.class );
    }
    
    
    /**
     * Answer true if this list is the empty list.
     * 
     * @return True if this is the empty (nil) list, otherwise false.
     */
    public boolean isEmpty() {
        if (m_checkValid) {
            checkValid();
        }
        
        return equals( listNil() );
    }
    
    
    /**
     * <p>
     * Return a reference to a new list cell whose head is <code>value</code>
     * and whose tail is this list.
     * </p>
     * 
     * @param value A new value to add to the head of the list
     * @return The new list, whose head is <code>value</code>
     */
    public RDFList cons( RDFNode value ) {
        if (m_checkValid) {
            checkValid();
        }
        
        // create a new, anonymous typed resource to be the list cell
        // map to a list facet
        return (RDFList) (newListCell( value, this )).as( RDFList.class );
    }
    
    
    /**
     * <p>
     * Add the given value to the end of the list. This is a side-effecting 
     * operation on the underlying model that is only defined if this is not the
     * empty list.  If this list is the empty (nil) list, we cannot perform a 
     * side-effecting update without changing the URI of this node (from <code>rdf:nil</code)
     * to a blank-node for the new list cell) without violating a Jena invariant.
     * Therefore, this update operation will throw an exception if an attempt is
     * made to add to the nil list.  Safe ways to add to an empty list include
     * {@link #with} and {@link #cons}.
     * </p>
     * 
     * @param value A value to add to the end of the list
     * @exception EmptyListUpdateException if an attempt is made to 
     * <code>add</code> to the empty list.
     */
    public void add( RDFNode value ) {
        if (m_checkValid) {
            checkValid();
        }
        
        // if this is the empty list, we have to barf
        if (isEmpty()) {
            throw new EmptyListUpdateException( "Attempt to add() to the empty list (rdf:nil)" );
        }
        
        // get the tail of the list (which may be cached)
        RDFList tail = findElement( true, 0 );
        
        // now do the concatenate
        setTailAux( tail, newListCell( value, listNil() ), listRest() );
    }
    
    
    /**
     * <p>
     * Answer the list that is this list with the given value added to the end
     * of the list.  This operation differs from {@link #add} in that it will
     * always work, even on an empty list, but the return value is the updated
     * list.  Specifically, in the case of adding a value to the empty list, the
     * returned list will not be the same as this list. <strong>Client code should
     * not assume that this is an in-place update, but should ensure that the resulting
     * list is asserted back into the graph into the appropriate relationships.</strong>
     * </p>
     * 
     * @param value A value to add to the end of the list
     * @return The list that results from adding a value to the end of this list 
     */
    public RDFList with( RDFNode value ) {
        if (m_checkValid) {
            checkValid();
        }
        
        // if this is the empty list, we create a new node containing value - i.e. cons
        if (isEmpty()) {
            return cons( value );
        }
        
        // get the tail of the list (which may be cached)
        RDFList tail = findElement( true, 0 );
        
        // now do the concatenate
        setTailAux( tail, newListCell( value, listNil() ), listRest() );
        return this;
    }
    
    
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
    public RDFNode get( int i ) {
        if (m_checkValid) {
            checkValid();
        }
        
        checkNotNil( "Tried to get an element from the empty list" );
        return findElement( false, i ).getHead();
    }
    
    
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
    public RDFNode replace( int i, RDFNode value ) {
        if (m_checkValid) {
            checkValid();
        }
        
        checkNotNil( "Tried to replace a value in the empty list" );
        return findElement( false, i ).setHead( value );
    }
    
    
    /**
     * <p>
     * Answer true if the given node appears as the value of a value of any
     * of the cells of this list.
     * </p>
     * 
     * @param value A value to test for
     * @return True if the list contains value.
     */
    public boolean contains( RDFNode value ) {
        return indexOf( value, 0 ) >= 0;
    }
    
    
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
    public int indexOf( RDFNode value ) {
        return indexOf( value, 0 );
    }
    
    
    /**
     * <p>
     * Answer the index of the first occurrence of the given value in the list
     * after index <code>start</code>, or -1 if the value is not in the list
     * after the given start point.
     * </p>
     * 
     * @param value The value to search for
     * @param start The index into the list to start searching from
     * @return The index (from zero, the front of the list) of the first
     * occurrence of <code>value</code> in the list not less than
     * <code>start</code>, or <code>-1</code> if not found.
     * @exception ListIndexException if <code>start</code> is greater than the
     * length of the list.
     */
    public int indexOf( RDFNode value, int start ) {
        if (m_checkValid) {
            checkValid();
        }
        
        // first get to where we start
        Resource l = findElement( false, start );
        int index = start;
        
        Property head = listFirst();
        Property tail = listRest();
        Resource nil = listNil();
        
        boolean found = l.hasProperty( head, value );
        
        // search for the element whose value is, er, value
        while (!found  &&  !l.equals( nil )) {
            l = l.getProperty( tail ).getResource();
            index++;
            found = l.hasProperty( head, value );
        }

        return found ? index : -1;
    }
    
    
    /**
     * <p>
     * Answer a new list that is formed by adding each element of this list to
     * the head of the the list formed from the 
     * given <code>nodes</code>. This is a non side-effecting
     * operation on either this list or the given list, but generates a copy
     * of this list.  For a more storage efficient alternative, see {@link
     * #concatenate concatenate}.
     * </p>
     * 
     * @param nodes An iterator whose range is RDFNode
     * @return A new RDFList that contains all of this elements of this list,
     * followed by all of the elements of the given iterator.
     */
    public RDFList append( Iterator nodes ) {
        return append( copy( nodes) );
    }
            
            
    /**
     * <p>
     * Answer a new list that is formed by adding each element of this list to
     * the head of the given <code>list</code>. This is a non side-effecting
     * operation on either this list or the given list, but generates a copy
     * of this list.  For a more storage efficient alternative, see {@link
     * #concatenate concatenate}.
     * </p>
     * 
     * @param list The argument list
     * @return A new RDFList that contains all of this elements of this list,
     * followed by all of the elements of the given list.
     */
    public RDFList append( RDFList list ) {
        if (m_checkValid) {
            checkValid();
        }
        
        if (isEmpty()) {
            // special case
            return list;
        }
        else {
            // could do this recursively, but for long lists it's better to iterate
            // do the copy, then change the last tail pointer to point to the arg
            RDFList copy = copy( iterator() );
            copy.concatenate( list );
            return copy;
        }
    }
    
    
    /**
     * <p>
     * Change the tail of this list to point to the given list, so that this
     * list becomes the list of the concatenation of the elements of both lists.
     * This is a side-effecting operation on this list; for a non side-effecting
     * alternative, see {@link #append}.  Due to the problem of maintaining
     * the URI invariant on a node, this operation will throw an exception if an
     * attempt is made to concatenate onto an empty list.  To avoid this, test for
     * an empty list: if true replace the empty list with the argument list, otherwise
     * proceed with the concatenate as usual.  An alternative solution is to use
     * {@link #append} and replace the original list with the return value.
     * </p>
     * 
     * @param list The argument list to concatenate to this list
     * @exception EmptyListUpdateException if this list is the nil list
     */
    public void concatenate( RDFList list ) {
        if (m_checkValid) {
            checkValid();
        }
        
        if (isEmpty()) {
            // concatenating list onto the empty list is an error
            throw new EmptyListUpdateException( "Tried to concatenate onto the empty list" );
        }
        else {
            // find the end of this list and link it to the argument list
            findElement( true, 0 ).setTail( list );
        }
    }
    
    
    /**
     * <p>
     * Add the nodes returned by the given iterator to the end of this list.
     * </p>
     * 
     * @param nodes An iterator whose range is RDFNode
     * @exception EmptyListUpdateException if this list is the nil list
     * @see #concatenate(RDFList) for details on avoiding the empty list update exception.
     */
    public void concatenate( Iterator nodes ) {
        // make a list of the nodes and add to the end of this
        concatenate( copy( nodes ) );
    }
            
        
    /**
     * <p>
     * Answer a list that contains all of the elements of this list in the same
     * order, but is a duplicate copy in the underlying model.
     * </p>
     * 
     * @return A copy of the current list
     */
    public RDFList copy() {
        if (m_checkValid) {
            checkValid();
        }
        
        return copy( iterator() );
    }
    
    
    /**
     * <p>
     * Apply a function to each value in the list in turn.
     * </p>
     * 
     * @param fn The function to apply to each list node.
     */
    public void apply( ApplyFn fn ) {
        if (m_checkValid) {
            checkValid();
        }
        
        for (Iterator i = iterator();  i.hasNext(); ) {
            fn.apply( (RDFNode) i.next() );
        }
    }
    
    
    /**
     * <p>
     * Apply a function to each value in the list in turn, accumulating the
     * results in an accumulator. The final value of the accumulator is returned
     * as the value of <code>reduce()</code>.
     * </p>
     * 
     * @param fn The reduction function to apply
     * @param initial The initial value for the accumulator
     * @return The final value of the accumulator.
     */
    public Object reduce( ReduceFn fn, Object initial ) {
        if (m_checkValid) {
            checkValid();
        }
        
        Object acc = initial;
        
        for (Iterator i = iterator();  i.hasNext();  ) {
            acc = fn.reduce( (RDFNode) i.next(), acc );
        }
        
        return acc;
    }
    
    
    /**
     * <p>
     * Remove the value from the head of the list.  The tail of the list remains
     * in the model.  Note that no changes are made to list cells that point to
     * this list cell as their tail.  Immediately following a
     * <code>removeHead</code> operation, such lists will be in a non-valid
     * state.
     * </p>
     * 
     * @return The remainder of the list after the head is removed (i&#046;e&#046; the
     * pre-removal list tail)
     */
    public RDFList removeHead() {
        if (m_checkValid) {
            checkValid();
        }
        
        checkNotNil( "Attempted to delete the head of a nil list" );
        
        RDFList tail = getTail();
        removeProperties();
        
        return tail;
    }
    
    
    /**
     * <p>Remove the given value from this list. If <code>val</code> does not occur in
     * the list, no action is taken.  Since removing the head of the list will invalidate
     * the list head cell, in general the list must return the list that results from this
     * operation. However, in many cases the return value will be the same as the object
     * that this method is invoked on</p>
     * 
     * @param val The value to be removed from the list
     * @return The resulting list, which will be the same as the current list in most
     * cases, except when <code>val</code> occurs at the head of the list.
     */
    public RDFList remove( RDFNode val ) {
        if (m_checkValid) {
            checkValid();
        }
        
        RDFList prev = null;
        RDFList cell = this;
        boolean searching = true;
        
        while (searching && !cell.isEmpty()) {
            if (cell.getHead().equals( val )) {
                // found the value to be removed
                RDFList tail = cell.getTail();
                if (prev != null) {
                    prev.setTail( tail );
                }
                
                cell.removeProperties();
                
                // return this unless we have removed the head element
                return (prev == null) ? tail : this;
            }
            else {
                // not found yet
                prev = cell;
                cell = cell.getTail();
            }
        }
        
        // not found
        return this;
    }
        
    
    /**
     * Remove all of the elements of this list from the model.
     */
    public void removeAll() {
        RDFList l = this;
        
        while (!l.isEmpty()) {
            l = l.removeHead();
        }
    }
    
    
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
    public ClosableIterator iterator() {
        return new RDFListIterator( this );
    }
    
    
    /**
     * <p>
     * Answer the contents of this RDF list as a Java list of RDFNode values.
     * </p>
     * 
     * @return The contents of this list as a Java List.
     */
    public List asJavaList() {
        List l = new ArrayList();
        
        for (Iterator i = iterator();  i.hasNext(); ) {
            l.add( i.next() );
        }
        
        return l;
    }
    
    
    /**
     * <p>
     * Answer true if this list has the same elements in the same order as the
     * given list.  Note that the standard <code>equals</code> test just tests
     * for equality of two given list cells.  While such a test is sufficient
     * for many purposes, this test provides a broader equality definition, but
     * is correspondingly more expensive to test.
     * </p>
     * 
     * @param list The list to test against
     * @return True if the given list and this list are the same length, and
     * contain equal elements in the same order.
     */
    public boolean sameListAs( RDFList list ) {
        if (m_checkValid) {
            checkValid();
        }
        
        Resource r0 = this;
        Resource r1 = list;
        
        Property head = listFirst();
        Property tail = listRest();
        Resource nil = listNil();
        
        // iterate through to the end of the list
        while (!(r0.equals( nil ) || r1.equals( nil ))) {
            RDFNode n0 = r0.getProperty( head ).getObject();
            RDFNode n1 = r1.getProperty( head ).getObject();
            
            if (n0 == null || !n0.equals( n1 )) {
                // not equal at this position
                return false;
            }
            else {
                // advance along the lists
                r0 = r0.getProperty( tail ).getResource();
                r1 = r1.getProperty( tail ).getResource();
            }
        }
        
        // lists are equal if they terminate together
        return r0.equals( nil ) && r1.equals( nil );
    }
    
    
    /**
     * <p>
     * Answer true if this list is operating in strict mode, in which the
     * well- formedness of the list is checked at every operation.
     * </p>
     * 
     * @return True if the list is being strictly checked.
     */
    public boolean isStrict() {
        return m_checkValid;
    }
    
    
    /**
     * <p>
     * Set a flag to indicate whether to strictly check the well-formedness of
     * the list at each operation. Default false.
     * </p>
     * 
     * @param strict If true, list will be checked strictly.
     */
    public void setStrict( boolean strict ) {
        m_checkValid = strict;
    }
    
    
    /**
     * <p>
     * Answer true if the list is well-formed, by checking that each node is
     * correctly typed, and has a head and tail pointer from the correct
     * vocabulary.
     * </p>
     * 
     * @return True if the list is well-formed.
     */
    public boolean isValid() {
        m_errorMsg = null;
        
        try {
            checkValid();
        }
        catch (InvalidListException e) {
            m_errorMsg = e.getMessage();
        }
        
        return (m_errorMsg == null);
    }


    /**
     * <p>
     * Answer the error message returned by the last failed validity check,
     * if any.
     * </p>
     * 
     * @return The most recent error message, or null.
     */
    public String getValidityErrorMessage() {
        return m_errorMsg;
    }
    
    
    /**
     * <p>
     * Construct a new list cell with the given value and tail.
     * </p>
     * 
     * @param value The value at the head of the new list cell
     * @param tail The tail of the list cell
     * @return A new list cell as a resource
     */
    public Resource newListCell( RDFNode value, Resource tail ) {
        Resource cell = getModel().createResource( listType() );
        
        // set the head and tail
        cell.addProperty( listFirst(), value );
        cell.addProperty( listRest(), tail );
        
        return cell;        
    }
    
    
    // Internal implementation methods
    //////////////////////////////////

    /**
     * <p>
     * Answer true if this is a valid list cell, which means either that it
     * is nil, or it has the appropriate type and a first and next relation.
     * </p>
     * 
     * @return True if this list cell passes basic validity checks
     */
    protected void checkValid() {
        if (!equals( listNil() )) {
            // note that the rdf:type of nil is implied by the RDF M&S
            checkValidProperty( RDF.type, listType() );
            
            checkValidProperty( listFirst(), null );
            checkValidProperty( listRest(), null );
        }
    }
    
    private void checkValidProperty( Property p, RDFNode expected ) {
        int count = 0;
        
        for (StmtIterator j = getModel().listStatements( this, p, expected );  j.hasNext();  j.next()) { 
            count++;
        }
        
        // exactly one value is expected
        if (count == 0) {
            throw new InvalidListException( "List node " + toString() + " is not valid: it should have property " +
                                            p.toString() + 
                                            (expected == null ? "" : ( " with value " + expected )) );
        }
        /* removed temporarily - ijd TODO 
        else if (count > 1) {
            throw new InvalidListException( "List node " + toString() + " is not valid: it has more than one value for " +
                                            p.toString() );
        }*/
    }
    
    
    
    /**
     * <p>
     * Check that the current list cell is not the nil list, and throw an empty
     * list exception if it is.
     * </p>
     * 
     * @param msg The context message for the empty list exception
     * @exception EmptyListException if the list is the nil list
     */
    protected void checkNotNil( String msg ) {
        if (isEmpty()) {
            throw new EmptyListException( msg );
        }
    }
    
    
    /**
     * <p>
     * Find and return an element of this list - either the last element before
     * the end of the list, or the i'th element from the front (starting from
     * zero).  Note that this method assumes the pre-condition that
     * <code>this</code> is not the empty list.
     * </p>
     * 
     * @param last If true, find the element whose tail is nil
     * @param index If <code>last</code> is false, find the index'th element
     * from the head of the list
     * @return The list cell
     * @exception ListIndexException if try to access an element beyond the end
     * of the list
     * @exception InvalidListException if try to find the end of a badly formed
     * list
     */
    protected RDFList findElement( boolean last, int index ) {
        Property tail = listRest();
        Resource nil = listNil();
        
        Resource l = this;
        int i = index;
        boolean found = (last && l.hasProperty( tail, nil )) || (!last && (i == 0));
        
        // search for the element whose tail is nil, or whose index is now zero
        while (!found  &&  !l.equals( nil )) {
            l = l.getProperty( tail ).getResource();
            found = (last && l.hasProperty( tail, nil )) || (!last && (--i == 0));
        }
        
        if (!found) {
            // premature end of list
            if (!last) {
                throw new ListIndexException( "Tried to access element " + index + " that is beyond the length of the list" );
            }
            else {
                throw new InvalidListException( "Could not find last element of list (suggests list is not valid)" );
            }
        }
        else {
            return (RDFList) l.as( RDFList.class );
        }
    }
    

    /**
     * <p>
     * Create a copy of the list of nodes returned by an iterator.
     * </p>
     * 
     * @param i An iterator of RDFNodes
     * @return A list formed from all of the nodes of i, in sequence
     */
    protected RDFList copy( Iterator i ) {
        Resource list = null;
        Resource start = null;
        
        Property head = listFirst();
        Property tail = listRest();
        Resource cellType = listType();
        
        while (i.hasNext()){
            // create a list cell to hold the next value from the existing list
            Resource cell = getModel().createResource( cellType );
            cell.addProperty( head, (RDFNode) i.next() );
                
            // point the previous list cell to this one
            if (list != null) {
                list.addProperty( tail, cell ); 
            }
            else {
                // must be the first cell we're adding
                start = cell;
            }
                
            list = cell;
        }
            
        // finally close the list
        list.addProperty( tail, listNil() );
            
        return (RDFList) start.as( RDFList.class );
    }
    
    
    /**
     * <p>
     * Helper method for setting the list tail, that assumes we have
     * a resource that is a list.
     * </p>
     * 
     * @param root The resource representing the list cell we're setting the
     * tail of
     * @param tail The new tail for this list, as a resource.
     * @return The old tail, as a resource.
     */
    protected static Resource setTailAux( Resource root, Resource tail, Property pTail ) {
        Statement current = root.getProperty( pTail );
        Resource oldTail = current.getResource();
            
        // out with the old, in with the new
        current.remove();
        root.addProperty( pTail, tail );
            
        return oldTail;
    }
        
        
    
    //==============================================================================
    // Inner class definitions
    //==============================================================================

    /**
     * <p>
     * Iterator that can step along chains of list pointers to the end of the
     * list.
     * </p>
     */
    protected class RDFListIterator
        implements ClosableIterator
    {
        // Instance variables
        
        /** The current list node */
        protected RDFList m_head;
        
        /** The most recently seen node */
        protected RDFList m_seen = null;
        
        
        // Constructor
        //////////////
        
        /**
         * Construct an iterator for walking the list starting at head
         */
        protected RDFListIterator( RDFList head ) {
            m_head = head;
        }
        
        
        // External contract methods
        ////////////////////////////
        
        /**
         * @see ClosableIterator#close
         */
        public void close() {
            // this iterator does not need to be closed
        }

        /**
         * @see Iterator#hasNext
         */
        public boolean hasNext() {
            return !m_head.isEmpty();
        }

        /**
         * @see Iterator#next
         */
        public Object next() {
            m_seen = m_head;
            m_head = m_head.getTail();
            
            return m_seen.getHead();
        }

        /**
         * @see Iterator#remove
         */
        public void remove() {
            if (m_seen == null) {
                throw new IllegalStateException( "Illegal remove from list operator" );
            }
            
            // will remove three statements in a well-formed list
            ((Resource) m_seen).removeProperties();
            m_seen = null;
        }
    }
}


/*
    (c) Copyright Hewlett-Packard Company 2002-2003
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
