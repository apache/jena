/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena
 * Created            4 Jan 2001
 * Filename           $RCSfile: DAMLList.java,v $
 * Revision           $Revision: 1.7 $
 * Release status     Preview-release $State: Exp $
 *
 * Last modified on   $Date: 2005-02-21 12:05:00 $
 *               by   $Author: andy_seaborne $
 *
 * (c) Copyright 2001, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.daml;


// Imports
///////////////
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.iterator.*;



/**
 * <p>Java representation of a DAML List. A list is the specified interpretation of
 * rdf:parseType="daml:Collection" attributes, where a sequence of values is
 * interpreted as a nested sequence of head/tail list cells.  One consequence of this
 * is that the list is quite specifically ordered, whereas the daml:collection
 * is said to be an unordered collection.  Consquently, we must caution that future
 * versions of the DAML specificiation may create an unordered interpretation of
 * daml:collection, and client code should not rely on the positionality of elements
 * in the current list interpretation.</p>
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian.Dickinson@hp.com">email</a>)
 * @version CVS info: $Id: DAMLList.java,v 1.7 2005-02-21 12:05:00 andy_seaborne Exp $
 */
public interface DAMLList
    extends RDFList
{
    // Constants
    //////////////////////////////////


    // External signature methods
    //////////////////////////////////


    /**
     * Answer an iteration over the values in the list.
     *
     * @return An iterator over the DAML values in the list
     */
    public ExtendedIterator getAll();


    /**
     * <p>Answer the value at the head of the list. Since, strictly speaking, DAML collections
     * are unordered, the position items in the list should not be relied on in client
     * code, as the definition of 'first' in the list may change in future releases.
     * However, the identity
     * <code><pre>
     *      List L = L.getFirst() + L.getRest()
     * </pre></code>
     * is guaranteed, providing that the contents of L do not change.
     *
     * @return The first value in the list.
     */
    public RDFNode getFirst();


    /**
     * <p>Answer a new list formed by creating a new DAMLList element whose first is the
     * given value and whose rest is the current list.  This is the 'cons' operator
     * familiar from other list processing languages.</p>
     *
     * @param value The new value to be added to the head of the list
     * @return a new list whose <code>daml:first</code> is the value, and whose
     *         <code>daml:rest</code> is this list.
     */
    public DAMLList cons( DAMLCommon value );


    /**
     * <p>Answer the list that consists of all values of the list save the first. Since, strictly
     * speaking, DAML collections are unordered, this corresponds to returning the collection
     * minus an unspecified one of its values.  However, the identity
     * <code><pre>
     *      List L = L.getFirst() + L.getRest()
     * </pre></code>
     * is guaranteed, providing that the contents of L do not change.</p>
     *
     * @return a list that contains all the elements of the current list, save the first one.
     */
    public DAMLList getRest();


    /**
     * <p>Answer a count of the items in the list.  Does not check for duplications, so this
     * is the count of entries in the list, not the count of distinct items in the list.</p>
     *
     * @return The number of entries in the list
     */
    public int getCount();



    /**
     * <p>Set the property <code>daml:first</code> for the given list element. This is a single
     * value that denotes the value at this position of the list.</p>
     *
     * @param value The value to be assigned to the 'first' property of a list cell
     */
    public void setFirst( DAMLCommon value );


    /**
     * <p>Set the property <code>daml:rest</code> for the given list element. This is a single
     * value that denotes the tail of the list.</p>
     *
     * @param tail The value to be assigned to the tail of the list.
     */
    public void setRest( DAMLList tail );


    /**
     * <p>Set the property <code>daml:rest</code> for the given list element to be the
     * nil list. This correctly terminates the list at this point.</p>
     */
    public void setRestNil();


    /**
     * <p>Answer the well-known constant denoting the nil list.</p>
     *
     * @return The resource denoting nil
     */
    public DAMLList getNil();


    /**
     * <p>Answer true if the given resource is the nil list.</p>
     *
     * @param resource The resource to be tested
     * @return true if the resource is the nil list
     */
    public boolean isNil( Resource resource );


    /**
     * <p>Find the last list element, i.e.  the one whose rest is nil.</p>
     *
     * @return A list element
     */
    public DAMLList findLast();


    /**
     * <p>Answer the i'th element of the list, if it exists.  If i is
     * less than 1, or is larger than the length of the list, throw
     * an illegal argument exception.</p>
     *
     * @param i The position of the list to return
     * @return The DAML value at the i'th position in the list
     * @exception ListIndexException if i is less than one, or
     *            larger than the length of the list.
     */
    public DAMLCommon getItem( int i );
}


/*
    (c) Copyright 2001, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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

