/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena
 * Created            8 Aug 2001
 * Filename           $RCSfile: ConcatenatedIterator.java,v $
 * Revision           $Revision: 1.7 $
 * Release status     Preview-release $State: Exp $
 *
 * Last modified on   $Date: 2003-08-27 13:07:54 $
 *               by   $Author: andy_seaborne $
 *
 * (c) Copyright 2001, 2002, 2003, Hewlett-Packard Development Company, LP
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.util.iterator;


// Imports
///////////////
import java.util.*;

import com.hp.hpl.jena.rdf.model.*;


/**
 * An iterator that represents the concatenation of two individual iterators.
 * The concatenated iterator will range over the elements of the first iterator,
 * followed by the elements of the second.
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian.Dickinson@hp.com">email</a>)
 * @version CVS info: $Id: ConcatenatedIterator.java,v 1.7 2003-08-27 13:07:54 andy_seaborne Exp $
 */
public class ConcatenatedIterator
    implements Iterator
{
    // Constants
    //////////////////////////////////


    // Static variables
    //////////////////////////////////


    // Instance variables
    //////////////////////////////////

    /** The first iterator */
    private Iterator m_iter0 = null;

    /** The second iterator */
    private Iterator m_iter1 = null;

    /** The default value for the iterator, or null if no default */
    protected Object m_defaultValue = null;

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
    public ConcatenatedIterator( Iterator iter0, Iterator iter1 ) {
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
    public Object next() {
        boolean next0 = m_iter0.hasNext();
        boolean next1 = m_iter1.hasNext();

        // are there any more values from the encapsulted iterations?
        if (next0 || next1) {
            Object next = (next0) ? m_iter0.next() : m_iter1.next();

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
    public void setDefaultValue( Object defaultValue ) {
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



    // Internal implementation methods
    //////////////////////////////////


    //==============================================================================
    // Inner class definitions
    //==============================================================================


}


/*
    (c) Copyright 2001, 2002, 2003 Hewlett-Packard Development Company, LP
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

