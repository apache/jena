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
package com.hp.hpl.jena.util.iterator;


// Imports
///////////////
import java.util.*;

/**
 * An iterator that represents the concatenation of two individual iterators.
 * The concatenated iterator will range over the elements of the first iterator,
 * followed by the elements of the second.
 * 
 * @deprecated Use <code>Iterator<T> iter = WrappedIterator.create( iter1 ).andThen( iter2 )</code>
 * {@link com.hp.hpl.jena.util.iterator.WrappedIterator}
 */
@Deprecated
public class ConcatenatedIterator<T> implements Iterator<T>
{
    /** The first iterator */
    private Iterator<? extends T> m_iter0 = null;

    /** The second iterator */
    private Iterator<? extends T> m_iter1 = null;

    /** The default value for the iterator, or null if no default */
    protected T m_defaultValue = null;

    /** A flag to show that the default value has been returned */
    protected boolean m_defaultValueSeen = false;



    // Constructors
    //////////////////////////////////

    /**
     * Construct an iterator that is the concatenation of the two
     * given iterators.  Either iterator may be a Java iterator, or a Jena
     * node or resource iterator.
     *
     * @param iter0 The first iterator. Elements of this iterator will appear
     *              first in the elements read from the concatenation.
     * @param iter1 The second iterator. Elements of this iterator will appear
     *              second in the elements read from the concatenation.
     */
    public ConcatenatedIterator( Iterator<? extends T> iter0, Iterator<? extends T> iter1 ) {
        m_iter0 = iter0;
        m_iter1 = iter1;
    }


    // External signature methods
    //////////////////////////////////

    /**
     * Returns true if the iteration has more elements. This will be
     * true if either of the underlying iterators has more elements.
     *
     * @return true if the iterator has more elements.
     */
    @Override
    public boolean hasNext() {
        return m_iter0.hasNext()  ||  m_iter1.hasNext() || (hasDefaultValue() && !m_defaultValueSeen);
    }


    /**
     * Returns the next element in the interation.
     *
     * @return The next object in the iteration, which will correspond to the next object in the
     *         underlying iteration, projected to the range of the projection function.
     * @exception NoSuchElementException - iteration has no more elements.
     */
    @Override
    public T next() {
        boolean next0 = m_iter0.hasNext();
        boolean next1 = m_iter1.hasNext();

        // are there any more values from the encapsulted iterations?
        if (next0 || next1) {
            // Casts necessary : without, it does not compile with 1.6.0 update 11 (it does in Eclipse)
            T next = (next0) ? (T)m_iter0.next() : (T)m_iter1.next();

            // is this the default value?
            if (hasDefaultValue()  &&  m_defaultValue.equals( next )) {
                m_defaultValueSeen = true;
            }

            return next;
        }
        else if (hasDefaultValue()  &&  !m_defaultValueSeen) {
            // return the default value for this iterator
            m_defaultValueSeen = true;
            return m_defaultValue;
        }
        else {
            // no more nodes, so this is an error
            throw new NoSuchElementException( "Tried to access next() element from empty concatenated iterator" );
        }
    }


    /**
     * Removes from the underlying collection the last element returned by
     * the iterator (optional operation). Not supported on a concatenated
     * iterator.
     *
     * @exception UnsupportedOperationException - if the remove operation is not
     *            supported by this Iterator.
     * @exception IllegalStateException - if the next method has not yet been
     *            called, or the remove method has already been called after the
     *            last call to the next method.
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException( "Cannot remove elements from concatenated iterator" );
    }


    /**
     * Set the default value for this iteration, which will be a value that
     * is guaranteed to be returned as a member of the iteration.  To guarantee
     * that the default value is only returned if it has not already been
     * returned by the iterator, setting the default value should occur before
     * the first call to {@link #next}.
     *
     * @param defaultValue The default value for the iteration, or null for
     *                     there to be no default value.  The default default
     *                     value is null.
     */
    public <X extends T> void setDefaultValue( X defaultValue ) {
        m_defaultValue = defaultValue;
    }


    /**
     * Answer true if this iteration has a default value.
     *
     * @return true if there is a default value
     */
    public boolean hasDefaultValue() {
        return m_defaultValue != null;
    }
}
