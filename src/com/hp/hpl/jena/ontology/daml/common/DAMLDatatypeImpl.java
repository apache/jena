/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian_Dickinson@hp.com
 * Package            Jena
 * Created            17 Sept 2001
 * Filename           $RCSfile: DAMLDatatypeImpl.java,v $
 * Revision           $Revision: 1.1.1.1 $
 * Release status     Preview-release $State: Exp $
 *
 * Last modified on   $Date: 2002-12-19 19:15:16 $
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

import com.hp.hpl.jena.vocabulary.DAMLVocabulary;

import com.hp.hpl.jena.ontology.daml.DAMLModel;
import com.hp.hpl.jena.ontology.daml.DAMLDatatype;

import com.hp.hpl.jena.util.Log;



/**
 * Encapsulates a DAML dataype, that represents values from a concrete domain by
 * encoding their type using XML schema.
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian_Dickinson@hp.com">email</a>)
 * @version CVS info: $Id: DAMLDatatypeImpl.java,v 1.1.1.1 2002-12-19 19:15:16 bwm Exp $
 */
public class DAMLDatatypeImpl
    extends DAMLCommonImpl
    implements DAMLDatatype
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
     * Constructor, takes the name and namespace for this datatype, and the underlying
     * model it will be attached to.
     *
     * @param namespace The namespace the datatype inhabits, or null
     * @param name The name of the datatype
     * @param model Reference to the DAML model that will contain statements about this DAML datatype.
     * @param vocabulary Reference to the DAML vocabulary used by this datatype.
     */
    public DAMLDatatypeImpl( String namespace, String name, DAMLModel model, DAMLVocabulary vocabulary ) {
        super( namespace, name, model, vocabulary );
        setRDFType( getVocabulary().Datatype() );
    }


    /**
     * Constructor, takes URI for this datatype, and the underlying
     * model it will be attached to.
     *
     * @param uri The URI of the datatype
     * @param store Reference to the DAML store that will contain statements about this DAML datatype.
     * @param vocabulary Reference to the DAML vocabulary used by this datatype.
     */
    public DAMLDatatypeImpl( String uri, DAMLModel store, DAMLVocabulary vocabulary ) {
        super( uri, store, vocabulary );
        setRDFType( getVocabulary().Datatype() );
    }



    // External signature methods
    //////////////////////////////////

    /**
     * Answer the traslator that can map between Java values and the serialised
     * form that represents the value in the RDF graph.
     *
     * @return a datatype translator.
     */
    public DatatypeTranslator getTranslator() {
        return getDAMLModel().getDatatypeRegistry().getTranslator( getURI() );
    }


    /**
     * Answer a key that can be used to index collections of this DAML value for
     * easy access by iterators.  Package access only.
     *
     * @return a key object.
     */
    Object getKey() {
        return getVocabulary().Datatype();
    }



    // Internal implementation methods
    //////////////////////////////////


    //==============================================================================
    // Inner class definitions
    //==============================================================================

}


