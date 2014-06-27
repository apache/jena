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

// Package
///////////////
package com.hp.hpl.jena.rdf.model.impl;


// Imports
///////////////
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.vocabulary.*;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>
 * Standard implementation the list abstraction from rdf.model.
 * </p>
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
    @SuppressWarnings("hiding")
    public static Implementation factory = new Implementation() {
        @Override public EnhNode wrap( Node n, EnhGraph eg ) { 
            if (canWrap( n, eg )) {
                RDFListImpl impl = new RDFListImpl( n, eg );
                
                // pass on the vocabulary terms, if available
                if (eg instanceof OntModel) {
                    Profile prof = ((OntModel) eg).getProfile();
                    impl.m_listFirst = prof.FIRST();
                    impl.m_listRest = prof.REST();
                    impl.m_listNil = prof.NIL();
                    impl.m_listType = prof.LIST();
                }
                
                return impl;
            }
            else {
                throw new JenaException( "Cannot convert node " + n + " to RDFList");
            } 
        }
            
        @Override public boolean canWrap( Node node, EnhGraph eg ) {
            Graph g = eg.asGraph();
            
            // if we are using a language profile, get the first, rest and next resources from there
            Resource first = RDF.first;
            Resource rest = RDF.rest;
            Resource nil = RDF.nil;
            
            if (eg instanceof OntModel) {
                Profile prof = ((OntModel) eg).getProfile();
                first = prof.FIRST();
                rest = prof.REST();
                nil = prof.NIL();
            }
            
            // node will support being an RDFList facet if it has rdf:type rdf:List, is nil, or is in the domain of a list property
            return  
                node.equals( nil.asNode() ) || 
                g.contains( node, first.asNode(), Node.ANY ) ||
                g.contains( node, rest.asNode(), Node.ANY ) ||
                g.contains( node, RDF.type.asNode(), RDF.List.asNode() );
        }
    };

    /** Flag to indicate whether we are checking for valid lists during list operations. Default false. */
    protected static boolean s_checkValid = false;
    
    private static final Logger log = LoggerFactory.getLogger( RDFListImpl.class );
    

    // Instance variables
    //////////////////////////////////

    /** Error message if validity check fails */
    protected String m_errorMsg = null;
    
    /** Pointer to the node that is the tail of the list */
    protected RDFList m_tail = null;
    
    /** The URI for the 'first' property in this list */
    protected Property m_listFirst = RDF.first;
    
    /** The URI for the 'rest' property in this list */
    protected Property m_listRest = RDF.rest;
    
    /** The URI for the 'nil' Resource in this list */
    protected Resource m_listNil = RDF.nil;
    
    /** The URI for the rdf:type of this list */
    protected Resource m_listType = RDF.List;
    
    
    
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
    public Resource listType()          { return m_listType; }
    public Resource listNil()           { return m_listNil; }
    public Property listFirst()         { return m_listFirst; }
    public Property listRest()          { return m_listRest; }
    public Class<? extends RDFList> listAbstractionClass() { return RDFList.class; }
    
    
    /**
     * <p>
     * Answer the number of elements in the list.
     * </p>
     * 
     * @return The length of the list as an integer
     */
    @Override
    public int size() {
        if (s_checkValid) {
            checkValid();
        }
        
        int size = 0;
        
        for (Iterator<RDFNode> i = iterator(); i.hasNext(); i.next()) {
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
    @Override
    public RDFNode getHead() {
        if (s_checkValid) {
            checkValid();
        }
        
        checkNotNil( "Tried to get the head of an empty list" );
        
        return getRequiredProperty( listFirst() ).getObject();
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
    @Override
    public RDFNode setHead( RDFNode value ) {
        if (s_checkValid) {
            checkValid();
        }
        
        checkNotNil( "Tried to set the head of an empty list" );
        
        // first remove the existing head
        Statement current = getRequiredProperty( listFirst() );
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
    @Override
    public RDFList getTail() {
        if (s_checkValid) {
            checkValid();
        }
        
        checkNotNil( "Tried to get the tail of an empty list" );
        
        Resource tail = getRequiredProperty( listRest() ).getResource();
        return tail.as( listAbstractionClass() );
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
    @Override
    public RDFList setTail( RDFList tail ) {
        if (s_checkValid) {
            checkValid();
        }
        
        checkNotNil( "Tried to set the tail of an empty list" );

        return (setTailAux( this, tail, listRest() )).as( listAbstractionClass() );
    }
    
    
    /**
     * Answer true if this list is the empty list.
     * 
     * @return True if this is the empty (nil) list, otherwise false.
     */
    @Override
    public boolean isEmpty() {
        if (s_checkValid) {
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
    @Override
    public RDFList cons( RDFNode value ) {
        if (s_checkValid) {
            checkValid();
        }
        
        // create a new, anonymous typed resource to be the list cell
        // map to a list facet
        return (newListCell( value, this )).as( listAbstractionClass() );
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
    @Override
    public void add( RDFNode value ) {
        if (s_checkValid) {
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
    @Override
    public RDFList with( RDFNode value ) {
        if (s_checkValid) {
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
    @Override
    public RDFNode get( int i ) {
        if (s_checkValid) {
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
    @Override
    public RDFNode replace( int i, RDFNode value ) {
        if (s_checkValid) {
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
    @Override
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
    @Override
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
    @Override
    public int indexOf( RDFNode value, int start ) {
        if (s_checkValid) {
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
            l = l.getRequiredProperty( tail ).getResource();
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
    @Override
    public RDFList append( Iterator<? extends RDFNode> nodes ) {
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
    @Override
    public RDFList append( RDFList list ) {
        if (s_checkValid) {
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
    @Override
    public void concatenate( RDFList list ) {
        if (s_checkValid) {
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
    @Override
    public void concatenate( Iterator<? extends RDFNode> nodes ) {
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
    @Override
    public RDFList copy() {
        if (s_checkValid) {
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
    @Override
    public void apply( ApplyFn fn ) {
        if (s_checkValid) {
            checkValid();
        }
        
        for (Iterator<RDFNode> i = iterator();  i.hasNext(); ) {
            fn.apply( i.next() );
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
    @Override
    public Object reduce( ReduceFn fn, Object initial ) {
        if (s_checkValid) {
            checkValid();
        }
        
        Object acc = initial;
        
        for (Iterator<RDFNode> i = iterator();  i.hasNext();  ) {
            acc = fn.reduce( i.next(), acc );
        }
        
        return acc;
    }
    
    
    /**
     * <p>Answer an iterator of the elements of this list, to each of which
     * the given map function has been applied.</p>
     * @param fn A Map function
     * @return The iterator of the elements of this list mapped with the given map function.
     */
    @Override
    public <T> ExtendedIterator<T> mapWith( Map1<RDFNode, T> fn ) {
        return iterator().mapWith( fn );
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
    @Override
    public RDFList removeHead() {
        if (s_checkValid) {
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
    @Override
    public RDFList remove( RDFNode val ) {
        if (s_checkValid) {
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
     * <p>Deprecated. Since an <code>RDFList</code> does not behave like a Java container, it is not
     * the case that the contents of the list can be removed and the container filled with values
     * again. Therefore, this method name has been deprecated in favour of {@link #removeList}</p>
     * @deprecated Replaced by {@link #removeList}
     */
    @Override
    @Deprecated
    public void removeAll() {
        removeList();
    }
    
    
    /**
     * <p>Remove all of the components of this list from the model. Once this operation
     * has completed, the {@link RDFList} resource on which it was called will no
     * longer be a resource in the model, so further methods calls on the list object
     * (for example, {@link #size} will fail.  Due to restrictions on the encoding
     * of lists in RDF, it is not possible to perform an operation which empties a list
     * and then adds further values to that list. Client code wishing to perform
     * such an operation should do so in two steps: first remove the old list, then 
     * create a new list with the new contents. It is important that RDF statements
     * that reference the old list (in the object position) be updated to point 
     * to the newly created list.  
     * Note that this
     * is operation is only removing the list cells themselves, not the resources
     * referenced by the list - unless being the object of an <code>rdf:first</code>
     * statement is the only mention of that resource in the model.</p>
     */
    @Override
    public void removeList() {
        for ( Statement statement : collectStatements() )
        {
            statement.remove();
        }
    }
    
    
    /**
     * <p>Answer a set of all of the RDF statements whose subject is one of the cells
     * of this list.</p>
     * @return A list of the statements that form the encoding of this list.
     */
    public Set<Statement> collectStatements() {
        Set<Statement> stmts = new HashSet<>();
        RDFList l = this;
        
        do {
            // collect all statements of this list cell
            for (Iterator<Statement> i = l.listProperties(); i.hasNext(); ) {
                stmts.add( i.next() );
            }
            
            // move on to next cell
            l = l.getTail();
        } while (!l.isEmpty());
        
        return stmts;
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
    @Override
    public ExtendedIterator<RDFNode> iterator() {
        return new RDFListIterator( this );
    }
    
    
    /**
     * <p>
     * Answer the contents of this RDF list as a Java list of RDFNode values.
     * </p>
     * 
     * @return The contents of this list as a Java List.
     */
    @Override
    public List<RDFNode> asJavaList() {
        List<RDFNode> l = new ArrayList<>();
        
        for (Iterator<RDFNode> i = iterator();  i.hasNext(); ) {
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
    @Override
    public boolean sameListAs( RDFList list ) {
        if (s_checkValid) {
            checkValid();
        }
        
        Resource r0 = this;
        Resource r1 = list;
        
        Property head = listFirst();
        Property tail = listRest();
        Resource nil = listNil();
        
        // iterate through to the end of the list
        while (!(r0.equals( nil ) || r1.equals( nil ))) {
            RDFNode n0 = r0.getRequiredProperty( head ).getObject();
            RDFNode n1 = r1.getRequiredProperty( head ).getObject();
            
            if (n0 == null || !n0.equals( n1 )) {
                // not equal at this position
                return false;
            }
            else {
                // advance along the lists
                r0 = r0.getRequiredProperty( tail ).getResource();
                r1 = r1.getRequiredProperty( tail ).getResource();
            }
        }
        
        // lists are equal if they terminate together
        return r0.equals( nil ) && r1.equals( nil );
    }
    
    
    /**
     * <p>
     * Answer true lists are operating in strict mode, in which the
     * well- formedness of the list is checked at every operation.
     * </p>
     * 
     * @return True lists are being strictly checked.
     */
    @Override
    public boolean getStrict() {
        return s_checkValid;
    }
    
    
    /**
     * <p>
     * Set a flag to indicate whether to strictly check the well-formedness of
     * lists at each operation. Default false.  Note that the flag that is
     * manipulated is actually a static: it applies to all lists. However, RDFList
     * is a Java interface, and Java does not permit static methods in interfaces.
     * </p>
     * 
     * @param strict The <b>static</b> flag for whether lists will be checked strictly.
     */
    @Override
    public void setStrict( boolean strict ) {
        s_checkValid = strict;
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
    @Override
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
    @Override
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
        // Note: following the RDF WG decision, we no longer assert rdf:type rdf:List for list cells
        Resource cell = getModel().createResource();
        
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
     * Updated 17-06-2003: RDFCore last comments process has decided that the
     * rdf:type of a list is implied by the domain constraints on rdf:first
     * and rdf:rest, so no longer needs to be asserted directly.  The test
     * for rdf:type has therefore been removed.
     * </p>
     * 
     * @returns True if this list cell passes basic validity checks
     */
    protected void checkValid() {
        if (!equals( listNil() )) {
            // note that the rdf:type of list cells is now implied by the RDF M&S
            // so we don't check explicitly
            // checkValidProperty( RDF.type, listType() );
            
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
            if (log.isDebugEnabled()) {
                log.debug( "Failed validity check on " + toString() );
                for (StmtIterator i = listProperties(); i.hasNext(); ) {
                    log.debug( "  this => " + i.next() );
                }
                for (StmtIterator i = getModel().listStatements( null, null, this ); i.hasNext(); ) {
                    log.debug( "  => this " + i.next() );
                }
            }
            throw new InvalidListException( "List node " + toString() + " is not valid: it should have property " +
                                            p.toString() + 
                                            (expected == null ? "" : ( " with value " + expected )) );
        }
        else if (count > 1) {
            throw new InvalidListException( "List node " + toString() + " is not valid: it has more than one value for " +
                                            p.toString() );
        }
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
            l = l.getRequiredProperty( tail ).getResource();
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
            return l.as( listAbstractionClass() );
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
    protected RDFList copy( Iterator<? extends RDFNode> i ) {
        Resource list = null;
        Resource start = null;
        
        Property head = listFirst();
        Property tail = listRest();
        Resource cellType = listType();
        
        if (i.hasNext())
        {
	        while (i.hasNext()){
	            // create a list cell to hold the next value from the existing list
	            Resource cell = getModel().createResource( cellType );
	            cell.addProperty( head, i.next() );
	                
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
        }
        else
        {
        	// create an empty list
        	start = getModel().createList();
        }
            
        return start.as( listAbstractionClass() );
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
        Statement current = root.getRequiredProperty( pTail );
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
    protected class RDFListIterator extends NiceIterator<RDFNode>
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
         * @see Iterator#hasNext
         */
        @Override public boolean hasNext() {
            return !m_head.isEmpty();
        }

        /**
         * @see Iterator#next
         */
        @Override public RDFNode next() {
            m_seen = m_head;
            m_head = m_head.getTail();
            
            return m_seen.getHead();
        }

        /**
         * @see Iterator#remove
         */
        @Override public void remove() {
            if (m_seen == null) {
                throw new IllegalStateException( "Illegal remove from list operator" );
            }
            
            // will remove three statements in a well-formed list
            ((Resource) m_seen).removeProperties();
            m_seen = null;
        }
    }
}
