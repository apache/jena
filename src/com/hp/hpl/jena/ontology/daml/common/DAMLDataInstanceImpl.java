/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian_Dickinson@hp.com
 * Package            Jena
 * Created            17 Sept 2001
 * Filename           $RCSfile: DAMLDataInstanceImpl.java,v $
 * Revision           $Revision: 1.1.1.1 $
 * Release status     Preview-release $State: Exp $
 *
 * Last modified on   $Date: 2002-12-19 19:15:15 $
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


import com.hp.hpl.jena.util.Log;

import com.hp.hpl.jena.ontology.daml.DAMLModel;
import com.hp.hpl.jena.ontology.daml.DAMLDataInstance;
import com.hp.hpl.jena.ontology.daml.DAMLCommon;
import com.hp.hpl.jena.ontology.daml.PropertyAccessor;
import com.hp.hpl.jena.ontology.daml.PropertyIterator;
import com.hp.hpl.jena.ontology.daml.DAMLDatatype;

import com.hp.hpl.jena.vocabulary.DAMLVocabulary;
import com.hp.hpl.jena.vocabulary.DAML_OIL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import java.util.Iterator;
import java.util.LinkedList;



/**
 * A DAML data instance is an instance of a DAML Dataype (compared to an DAML instance which is
 * an instance of a DAML Class; Class and Datatype are disjoint).
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian_Dickinson@hp.com">email</a>)
 * @version CVS info: $Id: DAMLDataInstanceImpl.java,v 1.1.1.1 2002-12-19 19:15:15 bwm Exp $
 */
public class DAMLDataInstanceImpl
    extends DAMLCommonImpl
    implements DAMLDataInstance
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
    public DAMLDataInstanceImpl( String namespace, String name, DAMLModel store, DAMLVocabulary vocabulary ) {
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
    public DAMLDataInstanceImpl( String uri, DAMLModel store, DAMLVocabulary vocabulary ) {
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
     * Answer the Datatype translator for values encoded by the datatype of this
     * instance.
     *
     * @return the datatype translator defined by the DAMLDatatype that is the rdf:type
     *         of this instance, or null if it is not defined.
     */
    public DatatypeTranslator getTranslator() {
        // get the RDF type of this value; should be only 1
        Iterator i = getRDFTypes( false );

        if (i.hasNext()) {
            // get the type resource
            Resource r = (Resource) i.next();

            // should be a datatype
            if (r instanceof DAMLDatatype) {
                if (i.hasNext()) {
                    Log.warning( "DAMLInstance " + this + " has more than one rdf:type, only returning first one" );
                }

                return (DatatypeTranslator) ((DAMLDatatype) r).getTranslator();
            }
            else {
                Log.warning( "DAMLInstance has rdf:type that is not a DAMLDatatype" );
            }
        }

        return null;
    }


    /**
     * Answer the value of this instance as a Java object, translated from the
     * serialised RDF representation by the Dataype's translator.
     *
     * @return the value of this instance, or null if either the translator or the
     *         serialised value is defined
     */
    public Object getValue() {
        try {
            return getTranslator().deserialize( getProperty( RDF.value ).getObject() );
        }
        catch (RDFException e) {
            Log.severe( "RDF exception while setting value of data instance: " + e, e );
            return null;
        }
    }


    /**
     * Set the value of this instance to the given Java value, which will be
     * serialised into the RDF graph by the datatype's translator.
     *
     * @param value A Java value whose serialisation will be made the value of this
     *              data instance by setting the <code>rdf:value</code> relation.
     */
    public void setValue( Object value ) {
        replaceProperty( RDF.value, getTranslator().serialize( value, getDAMLModel() ) );
    }



    // Internal implementation methods
    //////////////////////////////////


    //==============================================================================
    // Inner class definitions
    //==============================================================================


}
