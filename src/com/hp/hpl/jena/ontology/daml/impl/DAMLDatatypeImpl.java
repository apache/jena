/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian_Dickinson@hp.com
 * Package            Jena
 * Created            17 Sept 2001
 * Filename           $RCSfile: DAMLDatatypeImpl.java,v $
 * Revision           $Revision: 1.7 $
 * Release status     Preview-release $State: Exp $
 *
 * Last modified on   $Date: 2005-02-21 12:05:18 $
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

import com.hp.hpl.jena.vocabulary.*;

import com.hp.hpl.jena.datatypes.*;
import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.ontology.ConversionException;
import com.hp.hpl.jena.ontology.daml.*;



/**
 * Encapsulates a DAML dataype, that represents values from a concrete domain by
 * encoding their type using XML schema.
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian.Dickinson@hp.com">email</a>)
 * @version CVS info: $Id: DAMLDatatypeImpl.java,v 1.7 2005-02-21 12:05:18 andy_seaborne Exp $
 */
public class DAMLDatatypeImpl
    extends DAMLCommonImpl
    implements DAMLDatatype
{
    // Constants
    //////////////////////////////////


    // Static variables
    //////////////////////////////////

    /**
     * A factory for generating DAMLClass facets from nodes in enhanced graphs.
     * Note: should not be invoked directly by user code: use 
     * {@link com.hp.hpl.jena.rdf.model.RDFNode#as as()} instead.
     */
    public static Implementation factory = new Implementation() {
        public EnhNode wrap( Node n, EnhGraph eg ) { 
            if (canWrap( n, eg )) {
                return new DAMLDatatypeImpl( n, eg );
            }
            else {
                throw new ConversionException( "Cannot convert node " + n.toString() + " to DAMLDatatype" );
            } 
        }
            
        public boolean canWrap( Node node, EnhGraph eg ) {
            // node will support being an DAMLDataype facet if it has rdf:type daml:Datatype
            return eg.asGraph().contains( node, RDF.type.asNode(), DAML_OIL.Datatype.asNode() );
        }
    };



    // Instance variables
    //////////////////////////////////


    // Constructors
    //////////////////////////////////

    /**
     * <p>
     * Construct a DAML class represented by the given node in the given graph.
     * </p>
     * 
     * @param n The node that represents the resource
     * @param g The enh graph that contains n
     */
    public DAMLDatatypeImpl( Node n, EnhGraph g ) {
        super( n, g );
    }



    // External signature methods
    //////////////////////////////////

    /**
     * <p>Answer the traslator that can map between Java values and the serialised
     * form that represents the value in the RDF graph.</p>
     *
     * @return a datatype translator.
     */
    public RDFDatatype getTranslator() {
        return TypeMapper.getInstance().getTypeByName( getURI() );
    }



    // Internal implementation methods
    //////////////////////////////////


    //==============================================================================
    // Inner class definitions
    //==============================================================================

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

