/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian_Dickinson@hp.com
 * Package            Jena
 * Created            4 Jan 2001
 * Filename           $RCSfile: DAMLInstanceImpl.java,v $
 * Revision           $Revision: 1.3 $
 * Release status     Preview-release $State: Exp $
 *
 * Last modified on   $Date: 2003-02-20 17:11:57 $
 *               by   $Author: ian_dickinson $
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
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Property;

import com.hp.hpl.jena.util.iterator.ConcatenatedIterator;

import com.hp.hpl.jena.ontology.daml.DAMLModel;
import com.hp.hpl.jena.ontology.daml.DAMLInstance;
import com.hp.hpl.jena.ontology.daml.PropertyAccessor;
import com.hp.hpl.jena.ontology.daml.PropertyIterator;

import com.hp.hpl.jena.vocabulary.DAMLVocabulary;
import com.hp.hpl.jena.vocabulary.DAML_OIL;

import java.util.Iterator;



/**
 * Java representation of a DAML Instance.
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian_Dickinson@hp.com">email</a>)
 * @version CVS info: $Id: DAMLInstanceImpl.java,v 1.3 2003-02-20 17:11:57 ian_dickinson Exp $
 */
public class DAMLInstanceImpl
    extends DAMLCommonImpl
    implements DAMLInstance
{
    // Constants
    //////////////////////////////////


    // Static variables
    //////////////////////////////////


    // Instance variables
    //////////////////////////////////

    /** Property accessor for sameIndividualAs */
    protected PropertyAccessor m_propsameIndividualAs = null;



    // Constructors
    //////////////////////////////////

    /**
     * Constructor, takes the name and namespace for this instance, and the underlying
     * model it will be attached to.  Note that it is assumed that the RDF store
     * will contain a statement of the class to which this instance belongs.
     *
     * @param namespace The namespace the instance inhabits, or null
     * @param name The name of the instance
     * @param store The RDF store that contains the RDF statements defining the properties of the instance
     * @param vocabulary Reference to the DAML vocabulary used by this instance.
     */
    public DAMLInstanceImpl( String namespace, String name, DAMLModel store, DAMLVocabulary vocabulary ) {
        super( namespace, name, store, vocabulary );
    }


    /**
     * Constructor, takes the URI for this instance, and the underlying
     * model it will be attached to.  Note that it is assumed that the RDF store
     * will contain a statement of the class to which this instance belongs.
     *
     * @param uri The URI of the instance
     * @param store The RDF store that contains the RDF statements defining the properties of the instance
     * @param vocabulary Reference to the DAML vocabulary used by this instance.
     */
    public DAMLInstanceImpl( String uri, DAMLModel store, DAMLVocabulary vocabulary ) {
        super( uri, store, vocabulary );
    }




    // External signature methods
    //////////////////////////////////


    /**
     * Answer a key that can be used to index collections of this DAML instance for
     * easy access by iterators.  Package access only.
     *
     * @return a key object.
     */
    Object getKey() {
        return DAML_OIL.Thing.getURI();
    }


    /**
     * Property accessor for <code>daml:sameIndividualAs</code> property on a DAML instance.
     *
     * @return a property accessor
     */
    public PropertyAccessor prop_sameIndividualAs() {
        if (m_propsameIndividualAs == null) {
            m_propsameIndividualAs = new PropertyAccessorImpl( getVocabulary().sameIndividualAs(), this );
        }

        return m_propsameIndividualAs;
    }


    /**
     * Return an iterator over all of the instances that are the same as this one,
     * by generating the transitive closure over the <code>daml:samePropertyAs</code>
     * property.
     *
     * @return an iterator whose values will all be DAMLInstance objects
     */
    public Iterator getSameInstances() {
        return new PropertyIterator( this, getVocabulary().sameIndividualAs(),
                                     getVocabulary().sameIndividualAs(), true, true );
    }



    /**
     * Answer an iterator over all of the DAML objects that are equivalent to this
     * instance, which will be the union of <code>daml:equivalentTo</code> and
     * <code>daml:sameIndividualAs</code>.
     *
     * @return an iterator ranging over every equivalent DAML instance - each value of
     *         the iteration should be a DAMLInstance object.
     */
    public Iterator getEquivalentValues() {
        ConcatenatedIterator i = new ConcatenatedIterator(
                       // first the iterator over the equivalentTo values
                       super.getEquivalentValues(),
                       // followed by the sameClassAs values
                       new PropertyIterator( this, getVocabulary().sameIndividualAs(), getVocabulary().sameIndividualAs(), true, false, false ) );

        // ensure that the iteration includes self
        i.setDefaultValue( this );

        return i;
    }


    /**
     * Answer a property accessor for a user defined property.
     *
     * @param property An RDF or DAML property
     * @return a property accessor, that simplifies some of the basic operations
     *         of a given property on a given object
     */
    public PropertyAccessor accessProperty( Property property ) {
        return new PropertyAccessorImpl( property, this );
    }


    // Internal implementation methods
    //////////////////////////////////

    /**
     * Answer a value that will be a default type to include in an iteration of
     * the value's rdf types.  Typically there is no default (null), but for an
     * instance we want to ensure that the default type is daml:Thing.
     *
     * @return The default type: <code>daml:Thing</code>
     */
    protected Resource getDefaultType() {
        return getVocabulary().Thing();
    }



    //==============================================================================
    // Inner class definitions
    //==============================================================================


}