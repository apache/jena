/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian_Dickinson@hp.com
 * Package            Jena
 * Created            7 Sept 2001
 * Filename           $RCSfile: XMLDatatypeRegistry.java,v $
 * Revision           $Revision: 1.3 $
 * Release status     Preview-release $State: Exp $
 *
 * Last modified on   $Date: 2003-02-20 23:27:18 $
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

import java.util.*;

import com.hp.hpl.jena.rdf.model.RDFException;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Literal;

import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;

import com.hp.hpl.jena.vocabulary.RDF;

import com.hp.hpl.jena.ontology.daml.DAMLModel;

import com.hp.hpl.jena.util.Log;



/**
 * Provides a registry that can map RDF values encoded using XML Schema type definitions
 * into Java objects or scalars.  A basic set of mappings is provided, but other type
 * mappings can be added by the user.
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian_Dickinson@hp.com">email</a>)
 * @version CVS info: $Id: XMLDatatypeRegistry.java,v 1.3 2003-02-20 23:27:18 ian_dickinson Exp $
 */
public class XMLDatatypeRegistry
{
    // Constants
    //////////////////////////////////

    /** The URI for XML schema datatypes (xsd) namespace */
    public static final String XSD_NAMESPACE_URI = "http://www.w3.org/2000/10/XMLSchema#";

    /** Resource to denote the type of an xsd integer */
    public static final Resource XSD_INTEGER = new ResourceImpl( XSD_NAMESPACE_URI + "integer" );



    // Static variables
    //////////////////////////////////



    // Instance variables
    //////////////////////////////////

    /** Map for type URI to translator object */
    protected Map m_registry = new HashMap();


    // Constructors
    //////////////////////////////////

    /**
     * Constructor for the registry, which will register a basic set of mappings.
     */
    public XMLDatatypeRegistry() {
        registerDefaultTypes();
    }


    // External signature methods
    //////////////////////////////////

    /**
     * Answer the datatype translator for a a resource whose <code>rdf:type</code>
     * is the given URI.
     *
     * @param uri The uri of the type
     * @return the corresponding translator, if one is registered, or null otherwise.
     */
    public DatatypeTranslator getTranslator( String uri ) {
        return (DatatypeTranslator) m_registry.get( uri );
    }


    /**
     * Register a translator for the type whose uri is as given.
     *
     * @param uri The uri of the type to be registered
     * @param translator The translator that will map objects of the given type
     *                   between Java and XML/RDF.
     */
    public void registerTranslator( String uri, DatatypeTranslator translator ) {
        m_registry.put( uri, translator );
    }


    /**
     * Answer an iteration over the types of URI's that are registered.
     *
     * @return an iteration, each element of which is a string representing a URI.
     */
    public Iterator getRegisteredTypes() {
        return m_registry.keySet().iterator();
    }


    /**
     * Answer true if the given type URI is registered in this registry
     *
     * @param typeURI The URI of the type to be tested, as a string.
     * @return boolean true if the type is registered.
     */
    public boolean isRegisteredType( String typeURI ) {
        return m_registry.containsKey( typeURI );
    }



    // Internal implementation methods
    //////////////////////////////////

    /**
     * Add a basic (built-in) set of mappings between RDF/XML data and Java objects.
     */
    protected void registerDefaultTypes() {
        // add a converter for integers
        m_registry.put( XSD_NAMESPACE_URI + "integer",
                        new DatatypeTranslator() {
                            /** Get an Integer out of a node */
                            public Object deserialize( RDFNode node ) {
                                try {
                                    if (node instanceof Literal) {
                                        // parse the string value into an integer
                                        return new Integer( ((Literal) node).getString() );
                                    }
                                    else if (node instanceof Resource) {
                                        // assume we have have resource whose rdf:value is the value of the node
                                        RDFNode val = ((Resource) node).getProperty( RDF.value ).getObject();
                                        return deserialize( val );
                                    }
                                }
                                catch (RDFException e) {
                                    Log.severe( "RDF exception while converting datatype instance: " + e, e );
                                }

                                return null;
                            }

                            /** Convert an Integer to a node */
                            public RDFNode serialize( Object value, DAMLModel model ) {
                                try {
                                    return model.createLiteral( ((Integer) value).intValue() );
                                }
                                catch (RDFException e) {
                                    Log.severe( "RDF exception while converting datatype instance: " + e, e );
                                    return null;
                                }
                            }
                        } );

        // add a converter for strings
        m_registry.put( XSD_NAMESPACE_URI + "string",
                        new DatatypeTranslator() {
                            /** Get an Integer out of a node */
                            public Object deserialize( RDFNode node ) {
                                try {
                                    if (node instanceof Literal) {
                                        // parse the string value into an integer
                                        return ((Literal) node).getString();
                                    }
                                    else if (node instanceof Resource) {
                                        // assume we have have resource whose rdf:value is the value of the node
                                        RDFNode val = ((Resource) node).getProperty( RDF.value ).getObject();
                                        return deserialize( val );
                                    }
                                }
                                catch (RDFException e) {
                                    Log.severe( "RDF exception while converting datatype instance: " + e, e );
                                }

                                return null;
                            }

                            /** Convert an Integer to a node */
                            public RDFNode serialize( Object value, DAMLModel model ) {
                                try {
                                    return model.createLiteral( (String) value );
                                }
                                catch (RDFException e) {
                                    Log.severe( "RDF exception while converting datatype instance: " + e, e );
                                    return null;
                                }
                            }
                        } );

        // add a converter for reals
        m_registry.put( XSD_NAMESPACE_URI + "real",
                        new DatatypeTranslator() {
                            /** Get a Real out of a node */
                            public Object deserialize( RDFNode node ) {
                                try {
                                    if (node instanceof Literal) {
                                        // parse the string value into an integer
                                        return new Float( ((Literal) node).getString() );
                                    }
                                    else if (node instanceof Resource) {
                                        // assume we have have resource whose rdf:value is the value of the node
                                        RDFNode val = ((Resource) node).getProperty( RDF.value ).getObject();
                                        return deserialize( val );
                                    }
                                }
                                catch (RDFException e) {
                                    Log.severe( "RDF exception while converting datatype instance: " + e, e );
                                }

                                return null;
                            }

                            /** Convert an Integer to a node */
                            public RDFNode serialize( Object value, DAMLModel model ) {
                                try {
                                    return model.createLiteral( ((Float) value).floatValue() );
                                }
                                catch (RDFException e) {
                                    Log.severe( "RDF exception while converting datatype instance: " + e, e );
                                    return null;
                                }
                            }
                        } );

        // add a converter for decimals (we model them as long integers
        m_registry.put( XSD_NAMESPACE_URI + "decimal",
                        new DatatypeTranslator() {
                            /** Get a Real out of a node */
                            public Object deserialize( RDFNode node ) {
                                try {
                                    if (node instanceof Literal) {
                                        // parse the string value into an integer
                                        return new Long( ((Literal) node).getString() );
                                    }
                                    else if (node instanceof Resource) {
                                        // assume we have have resource whose rdf:value is the value of the node
                                        RDFNode val = ((Resource) node).getProperty( RDF.value ).getObject();
                                        return deserialize( val );
                                    }
                                }
                                catch (RDFException e) {
                                    Log.severe( "RDF exception while converting datatype instance: " + e, e );
                                }

                                return null;
                            }

                            /** Convert an Integer to a node */
                            public RDFNode serialize( Object value, DAMLModel model ) {
                                try {
                                    return model.createLiteral( ((Long) value).longValue() );
                                }
                                catch (RDFException e) {
                                    Log.severe( "RDF exception while converting datatype instance: " + e, e );
                                    return null;
                                }
                            }
                        } );
    }



    //==============================================================================
    // Inner class definitions
    //==============================================================================


}
