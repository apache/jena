/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian_Dickinson@hp.com
 * Package            Jena
 * Created            5 Jan 2001
 * Filename           $RCSfile: DAMLOntologyImpl.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     Preview-release $State: Exp $
 *
 * Last modified on   $Date: 2003-03-12 17:16:24 $
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
package com.hp.hpl.jena.ontology.daml.impl;


// Imports
///////////////
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFException;

import com.hp.hpl.jena.ontology.daml.DAMLModel;
import com.hp.hpl.jena.ontology.daml.DAMLOntology;
import com.hp.hpl.jena.ontology.daml.LiteralAccessor;

import com.hp.hpl.jena.util.Log;
import com.hp.hpl.jena.util.iterator.ConcatenatedNodeIterator;

import com.hp.hpl.jena.vocabulary.DAML_OIL;
import com.hp.hpl.jena.vocabulary.DAML_OIL_2000_12;
import com.hp.hpl.jena.vocabulary.DAMLVocabulary;



/**
 * Encapsulates the properties known for a given source ontology.
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian_Dickinson@hp.com">email</a>)
 * @version CVS info: $Id: DAMLOntologyImpl.java,v 1.1 2003-03-12 17:16:24 ian_dickinson Exp $
 */
public class DAMLOntologyImpl
    extends DAMLCommonImpl
    implements DAMLOntology
{
    // Constants
    //////////////////////////////////


    // Static variables
    //////////////////////////////////


    // Instance variables
    //////////////////////////////////

    /** Literal value accessor for the version info property */
    private LiteralAccessor m_propVersionInfo = null;



    // Constructors
    //////////////////////////////////

    /**
     * Constructor, takes the URI for this ontology properties, and the underlying
     * model it will be attached to.
     *
     * @param uri The URI of the ontology
     * @param store The RDF store that contains the RDF statements defining the properties of the ontology
     * @param vocabulary Reference to the DAML vocabulary used by this ontology resource.
     */
    public DAMLOntologyImpl( String uri, DAMLModel store, DAMLVocabulary vocabulary ) {
        super( uri, store, vocabulary );

        setRDFType( getVocabulary().Ontology() );
    }


    /**
     * Constructor, takes the name and namespace for this ontology properties, and the underlying
     * model it will be attached to.
     *
     * @param namespace The namespace the ontology inhabits, or null
     * @param name The name of the ontology
     * @param store The RDF store that contains the RDF statements defining the properties of the ontology
     * @param vocabulary Reference to the DAML vocabulary used by this ontology resource.
     */
    public DAMLOntologyImpl( String namespace, String name, DAMLModel store, DAMLVocabulary vocabulary ) {
        super( namespace, name, store, vocabulary );

        setRDFType( getVocabulary().Ontology() );
    }



    // External signature methods
    //////////////////////////////////


    /**
     * Property value accessor for the version info property of the ontology.
     *
     * @return A literal accessor that gives access to the version info of the ontology.
     */
    public LiteralAccessor prop_versionInfo() {
        if (m_propVersionInfo == null) {
            m_propVersionInfo = new LiteralAccessorImpl( getVocabulary().versionInfo(), this );
        }

        return m_propVersionInfo;
    }


    /**
     * Answer an iteration of resources that represent the URI's of the
     * ontologies that this ontology imports.
     *
     * @return An iterator over the resources representing imported ontologies
     */
    public NodeIterator getImportedOntologies() {
        return new ConcatenatedNodeIterator( getPropertyValues( DAML_OIL.imports ),
                                             getPropertyValues( DAML_OIL_2000_12.imports ) );
    }


    /**
     * Add the given ontology to the list of ontologies managed by the
     * knowledge store, and add it as an imoport property to this ontology object.
     *
     * @param uri The URI of the model.
     */
    public void addImportedOntology( String uri ) {
        try {
            // assert the model we're importing
            addProperty( getVocabulary().imports(), getModel().createLiteral( uri ) );

            // notify the knowledge store about the included model
            if (getDAMLModel().getLoader().getLoadImportedOntologies()) {
               getModel().read( uri );
            }
        }
        catch (RDFException e) {
            Log.severe( "RDF exception while adding imports property: " + e, e );
        }
    }


    /**
     * Answer a key that can be used to index collections of this DAML instance for
     * easy access by iterators.  Package access only.
     *
     * @return a key object.
     */
    Object getKey() {
        return DAML_OIL.Ontology.getURI();
    }



    // Internal implementation methods
    //////////////////////////////////


    //==============================================================================
    // Inner class definitions
    //==============================================================================


}

