/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian_Dickinson@hp.com
 * Package            Jena
 * Created            4 Jan 2001
 * Filename           $RCSfile: DAMLDatatypePropertyImpl.java,v $
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
import com.hp.hpl.jena.ontology.daml.DAMLDatatypeProperty;
import com.hp.hpl.jena.ontology.daml.LiteralAccessor;
import com.hp.hpl.jena.ontology.daml.DAMLModel;

import com.hp.hpl.jena.vocabulary.DAMLVocabulary;
import com.hp.hpl.jena.vocabulary.DAML_OIL;



/**
 * Java encapsulation of a datatype property in a DAML ontology.  A datatype property
 * is a partition of the class of all properties, whose values are drawn from concrete
 * domains represented by XML schema expressions.
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian_Dickinson@hp.com">email</a>)
 * @version CVS info: $Id: DAMLDatatypePropertyImpl.java,v 1.1 2003-03-12 17:16:24 ian_dickinson Exp $
 */
public class DAMLDatatypePropertyImpl 
extends DAMLPropertyImpl
implements DAMLDatatypeProperty {
    // Constants
    //////////////////////////////////
    
    
    // Static variables
    //////////////////////////////////
    
    
    // Instance variables
    //////////////////////////////////
    
    /** Property accessor for range */
    private LiteralAccessor m_propDatatypeRange = null;
    
    
    
    // Constructors
    //////////////////////////////////
    
    /**
     * Constructor, takes the name and namespace for this property, and the underlying
     * model it will be attached to.
     *
     * @param namespace The namespace the property inhabits, or null
     * @param name The name of the property
     * @param store The RDF store that contains the RDF statements defining the properties of the property
     * @param vocabulary Reference to the DAML vocabulary used by this property.
     */
    public DAMLDatatypePropertyImpl( String namespace, String name, DAMLModel store, DAMLVocabulary vocabulary ) {
        super( namespace, name, store, vocabulary );
        setRDFType( getVocabulary().DatatypeProperty() );
    }
    
    
    /**
     * Constructor, takes the URI for this property, and the underlying
     * model it will be attached to.
     *
     * @param uri The URI of the property
     * @param store The RDF store that contains the RDF statements defining the properties of the property
     * @param vocabulary Reference to the DAML vocabulary used by this property.
     */
    public DAMLDatatypePropertyImpl( String uri, DAMLModel store, DAMLVocabulary vocabulary ) {
        super( uri, store, vocabulary );
        setRDFType( getVocabulary().DatatypeProperty() );
    }
    
    
    
    // External signature methods
    //////////////////////////////////
    
    
    
    
    /**
     * Answer a key that can be used to index collections of this DAML property for
     * easy access by iterators.  Package access only.
     *
     * @return a key object.
     */
    Object getKey() {
        return DAML_OIL.Property.getURI();
    }
    
    
    
    
    
    // Internal implementation methods
    //////////////////////////////////
    
    
    
    
    
    //==============================================================================
    // Inner class definitions
    //==============================================================================
    
    
}
