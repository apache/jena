/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian_Dickinson@hp.com
 * Package            Jena
 * Created            13 Sept 2001
 * Filename           $RCSfile: NodeIteratorWrapper.java,v $
 * Revision           $Revision: 1.3 $
 * Release status     Preview-release $State: Exp $
 *
 * Last modified on   $Date: 2003-05-21 15:33:22 $
 *               by   $Author: chris-dollin $
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
import com.hp.hpl.jena.shared.*;

import java.util.NoSuchElementException;
import java.util.Iterator;




/**
 * A wrapper for NodeIterator that turns it into a standard Java iterator.  Clumsy name,
 * and anyway the need for it may go away in a future version of Jena.
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian_Dickinson@hp.com">email</a>)
 * @version CVS info: $Id: NodeIteratorWrapper.java,v 1.3 2003-05-21 15:33:22 chris-dollin Exp $
 */
public class NodeIteratorWrapper
    implements Iterator
{
    // Constants
    //////////////////////////////////


    // Static variables
    //////////////////////////////////


    // Instance variables
    //////////////////////////////////

    /** The encapsulated node iterator */
    private NodeIterator m_nIterator = null;


    // Constructors
    //////////////////////////////////

    /**
     * Construct an iterator to map a node iterator to a standard java iterator
     *
     * @param nIterator The NodeIterator we want to wrap
     */
    public NodeIteratorWrapper( NodeIterator nIterator ) {
        m_nIterator = nIterator;
    }


    // External signature methods
    //////////////////////////////////

    /**
     * Determine if there any more Nodes in the iteration.
     *
     * @return true if and only if there are more Resources available
     *         from the iteration.
     */
    public boolean hasNext()
    {
        try {
            return m_nIterator.hasNext();
        }
        catch (JenaException e) {
            throw new RuntimeException( "RDFException while accessing NodeIterator: " + e );
        }
    }


    /**
     * Return the next Node of the iteration.
     *
     * @throws NoSuchElementException if there are no more to be returned.
     * @return The next Resource from the iteration.
     */
    public Object next()
        throws NoSuchElementException
    {
        try {
            return m_nIterator.nextNode();
        }
        catch (JenaException e) {
            throw new RuntimeException( "RDFException while accessing NodeIterator: " + e );
        }
    }


    /**
     * Unsupported Operation.
     * @throws {@link java.lang.UnsupportedOperationException}
     */
    public void remove()
        throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException( "Cannot remove from this iterator" );
    }


    /**
     * Terminate the iteration and free up resources.
     *
     * <p>Some implementations, e.g. on relational databases, hold resources while
     * the iterator still exists.  These will normally be freed when the iteration
     * completes.  However, if an application wishes to ensure they are freed without
     * completing the iteration, this method should be called.</p>
     *
     * @throws RDFException Generic RDF exception.
     */
    public void close()
        throws RDFException
    {
        m_nIterator.close();
    }


    // Internal implementation methods
    //////////////////////////////////


    //==============================================================================
    // Inner class definitions
    //==============================================================================


}
