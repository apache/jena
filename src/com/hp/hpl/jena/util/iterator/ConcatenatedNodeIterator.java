/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian_Dickinson@hp.com
 * Package            Jena
 * Created            8 Aug 2001
 * Filename           $RCSfile: ConcatenatedNodeIterator.java,v $
 * Revision           $Revision: 1.1.1.1 $
 * Release status     Preview-release $State: Exp $
 *
 * Last modified on   $Date: 2002-12-19 19:21:13 $
 *               by   $Author: bwm $
 *
 * (c) Copyright Hewlett-Packard Company 2001
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.util.iterator;


// Imports
///////////////

import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFException;
import com.hp.hpl.jena.rdf.model.RDFNode;



/**
 * An iterator that represents the concatenation of two individual RDF Node iterators.
 * The concatenated iterator will range over the elements of the first iterator,
 * followed by the elements of the second.
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian_Dickinson@hp.com">email</a>)
 * @version CVS info: $Id: ConcatenatedNodeIterator.java,v 1.1.1.1 2002-12-19 19:21:13 bwm Exp $
 */
public class ConcatenatedNodeIterator
    implements NodeIterator
{
    // Constants
    //////////////////////////////////


    // Static variables
    //////////////////////////////////


    // Instance variables
    //////////////////////////////////

    /** The first iterator */
    private NodeIterator m_iter0 = null;

    /** The second iterator */
    private NodeIterator m_iter1 = null;


    // Constructors
    //////////////////////////////////

    /**
     * Construct an iterator that is the concatenation of the two
     * given iterators.
     *
     * @param iter0 The first iterator. Elements of this iterator will appear
     *              first in the elements read from the concatenation.
     * @param iter1 The second iterator. Elements of this iterator will appear
     *              second in the elements read from the concatenation.
     * @return An iterator over the concatenation of the elements of the two
     *         given iterators.
     */
    public ConcatenatedNodeIterator( NodeIterator iter0, NodeIterator iter1 ) {
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
    public boolean hasNext()
        throws RDFException
    {
        return m_iter0.hasNext()  ||  m_iter1.hasNext();
    }


    /**
     * Returns the next element in the interation.
     *
     * @return The next object in the iteration, which will correspond to the next object in the
     *         underlying iteration, projected to the range of the projection function.
     * @exception NoSuchElementException - iteration has no more elements.
     */
    public RDFNode next()
        throws RDFException
    {
        return (m_iter0.hasNext()) ? m_iter0.next() : m_iter1.next();
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
     * Close the concatenated iterator, which has the effect of closing both of the
     * underlying iterators.
     */
    public void close()
        throws RDFException
    {
        m_iter0.close();
        m_iter1.close();
    }


    // Internal implementation methods
    //////////////////////////////////


    //==============================================================================
    // Inner class definitions
    //==============================================================================


}

