/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian_Dickinson@hp.com
 * Package            Jena
 * Created            4 Jan 2001
 * Filename           $RCSfile: DAMLListImpl.java,v $
 * Revision           $Revision: 1.1.1.1 $
 * Release status     Preview-release $State: Exp $
 *
 * Last modified on   $Date: 2002-12-19 19:15:23 $
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
package com.hp.hpl.jena.ontology.daml.common;


// Imports
///////////////

import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFException;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.RDFNode;


import com.hp.hpl.jena.util.Log;

import com.hp.hpl.jena.ontology.daml.DAMLModel;
import com.hp.hpl.jena.ontology.daml.DAMLList;
import com.hp.hpl.jena.ontology.daml.DAMLCommon;

import com.hp.hpl.jena.vocabulary.DAML_OIL;
import com.hp.hpl.jena.vocabulary.DAML_OIL_2000_12;
import com.hp.hpl.jena.vocabulary.DAMLVocabulary;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import java.util.Iterator;



/**
 * Java representation of a DAML List. A list is the specified interpretation of
 * rdf:parseType="daml:Collection" attributes, where a sequence of values is
 * interpreted as a nested sequence of head/tail list cells.  One consequence of this
 * is that the list is quite specifically ordered, whereas the daml:collection
 * is said to be an unordered collection.  Consquently, we must caution that future
 * versions of the DAML specificiation may create an unordered interpretation of
 * daml:collection, and client code should not rely on the positionality of elements
 * in the current list interpretation.
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian_Dickinson@hp.com">email</a>)
 * @version CVS info: $Id: DAMLListImpl.java,v 1.1.1.1 2002-12-19 19:15:23 bwm Exp $
 */
public class DAMLListImpl
    extends DAMLCommonImpl
    implements DAMLList
{
    // Constants
    //////////////////////////////////


    // Static variables
    //////////////////////////////////


    // Instance variables
    //////////////////////////////////


    // Constructors
    //////////////////////////////////

    /**
     * Constructor, takes the URI for this list, and the underlying
     * model it will be attached to.
     *
     * @param uri The URI of the list
     * @param store The RDF store that contains the RDF statements defining the properties of the list
     * @param vocabulary Reference to the DAML vocabulary used by this list.
     */
    public DAMLListImpl( String uri, DAMLModel store, DAMLVocabulary vocabulary ) {
        super( uri, store, vocabulary  );

        setRDFType( getVocabulary().List() );
    }


    /**
     * Constructor, takes the name and namespace for this list, and the underlying
     * model it will be attached to.
     *
     * @param namespace The namespace the list inhabits, or null
     * @param name The name of the list
     * @param store The RDF store that contains the RDF statements defining the properties of the list
     * @param vocabulary Reference to the DAML vocabulary used by this list.
     */
    public DAMLListImpl( String namespace, String name, DAMLModel store, DAMLVocabulary vocabulary  ) {
        super( namespace, name, store, vocabulary  );

        setRDFType( getVocabulary().List() );
    }




    // External signature methods
    //////////////////////////////////


    /**
     * Add an element to the list.  The element will in fact be added to the end
     * of the list, but DAML collections are, strictly speaking, unordered, so the
     * position of the element in the list should not be relied upon.  This is a
     * side-effectful operation.  See {@link #cons}
     * for an operation that always returns a new list (i.e. no side-effects on
     * this list).
     *
     * @param value A DAML value to add to the list
     */
    public void add( DAMLCommon value ) {
        if (isEmpty()) {
            setFirst( value );
            setRestNil();
        }
        else {
            // find the list element that contains the empty list
            DAMLList last = findLast();

            DAMLList newList = (DAMLList) getDAMLModel().createDAMLValue( null, getVocabulary().List(), getVocabulary() );

            newList.setFirst( value );
            newList.setRestNil();

            // extend the list with this new element
            last.setRest( newList );
        }
    }


    /**
     * Remove the given value from the list.  If the value is not in the list, has
     * no effect.  If the value is in the list multiple times, only one of them
     * will be removed.
     *
     * @param value A DAML value to be removed from the list.
     */
    public void remove( DAMLCommon value ) {
        DAMLList parent = null;
        DAMLList child = this;

        // traverse the list to find the value
        while (!child.isEmpty()  &&  !this.getFirst().equals( value )) {
            parent = child;
            child = child.getRest();
        }

        // check to see if we found the value (if not, ignore it)
        if (!child.isEmpty()) {
            // we did find a value
            if (parent == null) {
                // the value we are removing is at the head of the list
                // removing first and last properties make the parent an empty list
                removeProperty( getVocabulary().first(), value );
                removeProperty( getVocabulary().rest(), child );
            }
            else {
                // change the parent to omit the child
                parent.setRest( child.getRest() );
            }
        }
    }


    /**
     * Answer an iteration over the values in the list.
     *
     * @return An iterator over the DAML values in the list
     */
    public Iterator getAll() {
        return new DAMLListIterator( this );
    }


    /**
     * Answer the first value from the list. Since, strictly speaking, DAML collections
     * are unordered, the position items in the list should not be relied on in client
     * code, as the definition of 'first' in the list may change in future releases.
     * However, the identity
     * <code><pre>
     *      List L = L.getFirst() + L.getRest()
     * </pre></code>
     * is guaranteed, providing that the contents of L do not change.
     *
     * @return The first value in the list, or, strictly, an unspecified value from the list.
     */
    public DAMLCommon getFirst() {
        return (DAMLCommon) getFirstResource();
    }


    /**
     * Answer a new list that consists of all values of the list save the first. Since, strictly
     * speaking, DAML collections are unordered, this corresponds to returning the collection
     * minus an unspecified one of its values.  However, the identity
     * <code><pre>
     *      List L = L.getFirst() + L.getRest()
     * </pre></code>
     * is guaranteed, providing that the contents of L do not change.
     *
     * @return a list that contains all the elements of the current list save one.
     */
    public DAMLList getRest() {
        Resource rest = getRestResource();

        if (isNil( rest )) {
            // rest of the list is nil, so ensure we return the right value
            return getVocabulary().nil();
        }
        else {
            if (rest instanceof com.hp.hpl.jena.ontology.daml.DAMLList) {
                // this should always be the case for well-formed lists
                return (DAMLList) rest;
            }
            else {
                Log.severe( "Badly formed list: rest of list should be type List, found " + rest );
                return null;
            }
        }
    }


    /**
     * Answer a count of the items in the list.  Does not check for duplications, so this
     * is the count of entries in the list, not the count of distinct items in the list.
     *
     * @return The number of entries in the list
     */
    public int getCount() {
        int count = 0;

        for (Iterator i = getAll();  i.hasNext();  i.next()) {
            count++;
        }

        return count;
    }



    /**
     * Answer true if the list has no values. The nil list is empty,
     * as is the list with no first and no rest. Note that we retain both
     * conditions as we define the primary resource of a DAML value to be
     * invariant (for indexing purposes). Therefore, a newly created list
     * must be empty, but must not be identical to the nil list since we
     * could never then change it to be a list of values.
     *
     * @return true for an empty list
     */
    public boolean isEmpty() {
        try {
            return equals( getNil() )  ||
                   (!hasProperty( getVocabulary().first() )  &&
                    !hasProperty( getVocabulary().rest()  ));
        }
        catch (RDFException e) {
            Log.severe( "RDF exception " + e, e );
            throw new RuntimeException( "RDF Exception " + e );
        }
    }


    /**
     * Set the property <code>daml:first</code> for the given list element. This is a single
     * value that denotes the value at this position of the list.
     *
     * @param value The value to be assigned to the 'first' property of a list cell
     */
    public void setFirst( DAMLCommon value ) {
        setPropertyValue( getVocabulary().first(), value );
    }


    /**
     * Set the property <code>daml:rest</code> for the given list element. This is a single
     * value that denotes the tail of the list.
     *
     * @param value The value to be assigned to the tail of the list.
     */
    public void setRest( DAMLList tail ) {
        setPropertyValue( getVocabulary().rest(), tail );
    }


    /**
     * Set the property <code>daml:rest</code> for the given list element to be the
     * nil list. This correctly terminates the list at this point.
     */
    public void setRestNil() {
        setRest( getVocabulary().nil() );
    }


    /**
     * Answer a new list formed by creating a new DAMLList element whose first is the
     * given value and whose rest is the current list.  This is the 'cons' operator
     * familiar from other list processing languages.
     *
     * @param The new value to be added to the head of the list
     * @return a new list whose <code>daml:first</code> is the value, and whose
     *         <code>daml:rest</code> is this list.
     */
    public DAMLList cons( DAMLCommon value ) {
        // create a new list cell
        DAMLList l = new DAMLListImpl( null, getDAMLModel(), getVocabulary() );

        // set head and tail
        l.setFirst( value );
        l.setRest( this );

        return l;
    }


    /**
     * Answer the well-known constant denototing the nil list.
     *
     * @return The resource denoting nil
     */
    public DAMLList getNil() {
        return getVocabulary().nil();
    }


    /**
     * Answer true if the given resource is the nil list.  Will check the
     * namespace of the current vocabulary only.
     *
     * @param resource A resource, that may be the nil list
     * @return true if the resource has the same URI as the nil list.
     */
    public boolean isNil( Resource resource ) {
        return resource.equals( getVocabulary().nil() );
    }


    /**
     * Find the last list element, i.e.  the one whose rest is nil.
     *
     * @return A list element
     */
    public DAMLList findLast() {
        if (getRestResource().equals( getNil() )) {
            // end of the line ... all change please
            return this;
        }
        else {
            return getRest().findLast();
        }
    }


    /**
     * Answer the i'th element of the list, if it exists.  If i is
     * less than 1, or is larger than the length of the list, throw
     * an illegal argument exception.
     *
     * @param i The position of the list to return
     * @return The DAML value at the i'th position in the list
     * @exception java.lang.IllegalArgumentException if i is less than one, or
     *            larger than the length of the list.
     */
    public DAMLCommon getItem( int i ) {
        if (i < 1) {
            // there is no zero'th element
            throw new IllegalArgumentException( "Element " + i + " of a list is not defined" );
        }
        else {
            // try to find the right list cell
            int count = i;
            DAMLList l = this;

            while (!l.isEmpty()) {
                if (count == 1) {
                    // this is the one
                    return l.getFirst();
                }
                else {
                    // go to the next list cell
                    l = l.getRest();
                    count--;
                }
            }

            // if we get here, i > length( this )
            throw new IllegalArgumentException( "Tried to getItem( " + i + " ) of a list without that many elements" );
        }
    }


    // Internal implementation methods
    //////////////////////////////////



    /**
     * Answer the resource that is the 'rest' property of the list
     *
     * @return RDF resource
     */
    protected Resource getRestResource() {
        try {
            return (Resource) getPropertyValue( getVocabulary().rest() );
        }
        catch (RuntimeException e) {
            Log.debug( "rdf error: " + e, e );
            throw e;
        }
    }


    /**
     * Answer the RDF resource that is the value of the first property
     * on this list cell
     */
    protected Resource getFirstResource() {
        return (Resource) getPropertyValue( getVocabulary().first() );
    }


    /**
     * Answer a key that can be used to index collections of this DAML list for
     * easy access by iterators.  Package access only.
     *
     * @return a key object.
     */
    Object getKey() {
        return DAML_OIL.List.getURI();
    }



    //==============================================================================
    // Inner class definitions
    //==============================================================================


    /**
     * An iterator over the elements of a DAML list
     */
    protected class DAMLListIterator
        implements Iterator
    {
        // Instance variables
        /////////////////////

        /** The list cell we are currently pointing to */
        private DAMLList m_list = null;


        // Constructors
        /////////////////////

        /**
         * Construct an iteration over a DAML list
         */
        public DAMLListIterator( DAMLList list ) {
            m_list = list;
        }


        // External signature methods
        //////////////////////////////

        /**
         * Answer true if the iteration has more elements.
         *
         * @return true if there are more elements in the iteration
         */
        public boolean hasNext() {
            return (m_list != null)  &&  !(m_list.isEmpty());
        }


        /**
         * Answer the next DAML value from the iteration over the
         * values in the list
         *
         * @return A DAMLCommon value
         */
        public Object next() {
            DAMLCommon val = m_list.getFirst();
            m_list = m_list.getRest();

            return val;
        }


        /**
         * Remove an element from the list. Not supported.
         */
        public void remove() {
            throw new UnsupportedOperationException( "Remove operation not supported on DAML lists" );
        }
    } // Inner class DAMLListIterator


}
