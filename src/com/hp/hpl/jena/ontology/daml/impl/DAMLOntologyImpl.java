/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena
 * Created            5 Jan 2001
 * Filename           $RCSfile: DAMLOntologyImpl.java,v $
 * Revision           $Revision: 1.10 $
 * Release status     Preview-release $State: Exp $
 *
 * Last modified on   $Date: 2005-02-21 12:05:26 $
 *               by   $Author: andy_seaborne $
 *
 * (c) Copyright 2001, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.daml.impl;


// Imports
///////////////
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.ontology.daml.*;
import com.hp.hpl.jena.vocabulary.*;



/**
 * <p>Encapsulates the properties known for a given source ontology.</p>
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian.Dickinson@hp.com">email</a>)
 * @version CVS info: $Id: DAMLOntologyImpl.java,v 1.10 2005-02-21 12:05:26 andy_seaborne Exp $
 */
public class DAMLOntologyImpl
    extends DAMLCommonImpl
    implements DAMLOntology
{
    // Constants
    //////////////////////////////////


    // Static variables
    //////////////////////////////////

    /**
     * A factory for generating DAMLDataInstance facets from nodes in enhanced graphs.
     * Note: should not be invoked directly by user code: use 
     * {@link com.hp.hpl.jena.rdf.model.RDFNode#as as()} instead.
     */
    public static Implementation factory = new Implementation() {
        public EnhNode wrap( Node n, EnhGraph eg ) { 
            if (canWrap( n, eg )) {
                return new DAMLOntologyImpl( n, eg );
            }
            else {
                throw new ConversionException( "Cannot convert node " + n.toString() + " to DAMLOntology" );
            } 
        }
            
        public boolean canWrap( Node node, EnhGraph eg ) {
            return hasType( node, eg, DAML_OIL.Ontology );
        }
    };

    // Instance variables
    //////////////////////////////////

    /** Literal value accessor for the version info property */
    private LiteralAccessor m_propVersionInfo = null;



    // Constructors
    //////////////////////////////////

    /**
     * <p>
     * Construct a DAML list represented by the given node in the given graph.
     * </p>
     * 
     * @param n The node that represents the resource
     * @param g The enh graph that contains n
     */
    public DAMLOntologyImpl( Node n, EnhGraph g ) {
        super( n, g );
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
        return listPropertyValues( DAML_OIL.imports );
        //return new ConcatenatedNodeIterator( ,
        //                                     getPropertyValues( DAML_OIL_2000_12.imports ) );
    }


    /**
     * Add the given ontology to the list of ontologies managed by the
     * knowledge store, and add it as an imoport property to this ontology object.
     *
     * @param uri The URI of the model.
     */
    public void addImportedOntology( String uri ) {
        // assert the model we're importing
        addProperty( getVocabulary().imports(), getModel().createLiteral( uri ) );

        // notify the knowledge store about the included model
        if (getDAMLModel().getLoader().getLoadImportedOntologies()) {
           getModel().read( uri );
        }
    }




    // Internal implementation methods
    //////////////////////////////////


    //==============================================================================
    // Inner class definitions
    //==============================================================================


}


/*
    (c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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

