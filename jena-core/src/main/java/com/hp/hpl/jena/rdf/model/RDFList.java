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
package com.hp.hpl.jena.rdf.model;


// Imports
///////////////
import com.hp.hpl.jena.util.iterator.*;

import java.util.*;


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
 */
public interface RDFList
    extends Resource
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
     * Return a reference to a new list cell whose head is <code>value</code>
     * and whose tail is this list.
     * </p>
     *
     * @param value A new value to add to the head of the list
     * @return The new list, whose head is <code>value</code>
     */
    public RDFList cons( RDFNode value );


    /**
     * <p>
     * Add the given value to the end of the list. This is a side-effecting
     * operation on the underlying model that is only defined if this is not the
     * empty list.  If this list is the empty (nil) list, we cannot perform a
     * side-effecting update without changing the URI of this node (from <code>rdf:nil</code>)
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
    public void add( RDFNode value );


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
    public RDFList with( RDFNode value );


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
    public RDFNode replace( int i, RDFNode value );


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
     * #concatenate concatenate}.
     * </p>
     *
     * @param list The argument list
     * @return A new RDFList that contains all of this elements of this list,
     * followed by all of the elements of the given list.
     */
    public RDFList append( RDFList list );


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
    public RDFList append( Iterator<? extends RDFNode> nodes );


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
    public void concatenate( RDFList list );


    /**
     * <p>
     * Add the nodes returned by the given iterator to the end of this list.
     * </p>
     *
     * @param nodes An iterator whose range is RDFNode
     * @exception EmptyListUpdateException if this list is the nil list
     * @see #concatenate(RDFList) for details on avoiding the empty list update exception.
     */
    public void concatenate( Iterator<? extends RDFNode> nodes );


    /**
     * <p>
     * Answer a list that contains all of the elements of this list in the same
     * order, but is a duplicate copy in the underlying model.
     * </p>
     *
     * @return A copy of the current list
     */
    public RDFList copy();


    /**
     * <p>
     * Apply a function to each value in the list in turn.
     * </p>
     *
     * @param fn The function to apply to each list node.
     */
    public void apply( ApplyFn fn );


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
    public Object reduce( ReduceFn fn, Object initial );


    /**
     * <p>Answer an iterator of the elements of this list, to each of which
     * the given map function has been applied.</p>
     * @param fn A Map function
     * @return The iterator of the elements of this list mapped with the given map function.
     */
    public <T> ExtendedIterator<T> mapWith( Map1<RDFNode, T> fn );


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
    public RDFList removeHead();


    /**
     * <p>Deprecated. Since an <code>RDFList</code> does not behave like a Java container, it is not
     * the case that the contents of the list can be removed and the container filled with values
     * again. Therefore, this method name has been deprecated in favour of {@link #removeList}</p>
     * @deprecated Replaced by {@link #removeList}
     */
    @Deprecated
    public void removeAll();


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
    public void removeList();


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
    public RDFList remove( RDFNode val );


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
    public ExtendedIterator<RDFNode> iterator();


    /**
     * <p>
     * Answer the contents of this RDF list as a Java list of RDFNode values.
     * </p>
     *
     * @return The contents of this list as a Java List.
     */
    public List<RDFNode> asJavaList();


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
    public boolean sameListAs( RDFList list );


    /**
     * <p>
     * Answer true lists are operating in strict mode, in which the
     * well- formedness of the list is checked at every operation.
     * </p>
     *
     * @return True lists are being strictly checked.
     */
    public boolean getStrict();


    /**
     * <p>
     * Set a flag to indicate whether to strictly check the well-formedness of
     * lists at each operation. Default false.  <strong>Note</strong> that the flag that is
     * manipulated is actually a static: it applies to all lists. However, RDFList
     * is a Java interface, and Java does not permit static methods in interfaces.
     * </p>
     *
     * @param strict The <b>static</b> flag for whether lists will be checked strictly.
     */
    public void setStrict( boolean strict );


    /**
     * <p>
     * Answer true if the list is well-formed, by checking that each node is
     * correctly typed, and has a head and tail pointer from the correct
     * vocabulary. If the list is invalid, the reason is available via {@link
     * #getValidityErrorMessage}.
     * </p>
     *
     * @return True if the list is well-formed.
     * @see #getValidityErrorMessage
     */
    public boolean isValid();


    /**
     * <p>
     * Answer the error message returned by the last failed validity check,
     * if any.
     * </p>
     *
     * @return The most recent error message, or null.
     * @see #isValid
     */
    public String getValidityErrorMessage();


    //==============================================================================
    // Inner class definitions
    //==============================================================================

    /**
     * Interface that encapsulates a function to apply to every element in a
     * list.
     */
    public static interface ApplyFn {
        /**
         * <p>
         * Apply a function to the given RDF node.
         * </p>
         *
         * @param node A node from the list.
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
         * Apply a function to the given RDF node.
         * </p>
         *
         * @param node A node from the list.
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
