/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            21-Jun-2003
 * Filename           $RCSfile: TestOntModel.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-06-21 15:12:50 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2002-2003, Hewlett-Packard Company, all rights reserved.
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.impl.test;


// Imports
///////////////
import java.io.*;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;

import junit.framework.*;


/**
 * <p>
 * Unit tests on OntModel capabilities.  Many of OntModel's methods are tested by the other
 * abstractions' unit tests.
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: TestOntModel.java,v 1.1 2003-06-21 15:12:50 ian_dickinson Exp $
 */
public class TestOntModel 
    extends TestCase
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////

    public static final String BASE = "http://www.hp.com/test";
    public static final String NS = BASE + "#";
    
    public static final String DOC = "<rdf:RDF" +
                                     "   xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"" +
                                     "   xmlns:owl=\"http://www.w3.org/2002/07/owl#\"" +
                                     "   xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\">" +
                                     "  <owl:Class rdf:about=\"http://www.hp.com/test#D\">" +
                                     "    <rdfs:subClassOf>" +
                                     "      <owl:Class rdf:about=\"http://www.hp.com/test#B\"/>" +
                                     "    </rdfs:subClassOf>" +
                                     "  </owl:Class>" +
                                     "  <owl:Class rdf:about=\"http://www.hp.com/test#B\">" +
                                     "    <rdfs:subClassOf rdf:resource=\"http://www.hp.com/test#A\"" +
                                     "       rdf:type=\"http://www.w3.org/2002/07/owl#Class\"/>" +
                                     "  </owl:Class>" +
                                     "  <owl:Class rdf:about=\"http://www.hp.com/test#C\">" +
                                     "    <rdfs:subClassOf rdf:resource=\"http://www.hp.com/test#B\"/>" +
                                     "  </owl:Class>" +
                                     "  <owl:ObjectProperty rdf:about=\"http://www.hp.com/test#p\">" +
                                     "    <rdfs:domain rdf:resource=\"http://www.hp.com/test#A\"/>" +
                                     "    <rdfs:range rdf:resource=\"http://www.hp.com/test#B\"/>" +
                                     "    <rdfs:range rdf:resource=\"http://www.hp.com/test#C\"/>" +
                                     "  </owl:ObjectProperty>" +
                                     "</rdf:RDF>";
    
    // Instance variables
    //////////////////////////////////

    // Constructors
    //////////////////////////////////

    public TestOntModel( String name ) {
        super( name );
    }
    
    // External signature methods
    //////////////////////////////////

    /** Test writing the base model to an output stream */
    public void testWriteOutputStream() {
        OntModel m = ModelFactory.createOntologyModel();
        
        // set up the model
        OntClass A = m.createClass( NS + "A" );
        OntClass B = m.createClass( NS + "B" );
        OntClass C = m.createClass( NS + "C" );
        OntClass D = m.createClass( NS + "D" );
        
        A.addSubClass( B );
        B.addSubClass( C );
        B.addSubClass( D );
        
        ObjectProperty p = m.createObjectProperty( NS + "p" );
        
        p.addDomain( A );
        p.addRange( B );
        p.addRange( C );
        
        // write to a stream
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        m.write( out );

        String s = out.toString();
        ByteArrayInputStream in = new ByteArrayInputStream( s.getBytes() );
        
        // read it back again
        Model mIn1 = ModelFactory.createDefaultModel();
        mIn1.read( in, BASE );
        
        Model mIn2 = ModelFactory.createDefaultModel();
        mIn2.read( new ByteArrayInputStream( DOC.getBytes() ), BASE );
        
        // should be the same
        assertTrue( "InputStream write/read cycle failed (1)", mIn1.isIsomorphicWith( m.getBaseModel() ) );
        assertTrue( "InputStream write/read cycle failed (2)", mIn2.isIsomorphicWith( m.getBaseModel() ) );
    }
    
    /** Test writing the base model to an output stream */
    public void testWriteWriter() {
        OntModel m = ModelFactory.createOntologyModel();
        
        // set up the model
        OntClass A = m.createClass( NS + "A" );
        OntClass B = m.createClass( NS + "B" );
        OntClass C = m.createClass( NS + "C" );
        OntClass D = m.createClass( NS + "D" );
        
        A.addSubClass( B );
        B.addSubClass( C );
        B.addSubClass( D );
        
        ObjectProperty p = m.createObjectProperty( NS + "p" );
        
        p.addDomain( A );
        p.addRange( B );
        p.addRange( C );
        
        // write to a stream
        StringWriter out = new StringWriter();
        m.write( out );

        String s = out.toString();
        
        // read it back again
        Model mIn1 = ModelFactory.createDefaultModel();
        mIn1.read( new StringReader( s ), BASE );
        
        Model mIn2 = ModelFactory.createDefaultModel();
        mIn2.read( new StringReader( DOC ), BASE );
        
        // should be the same
        assertTrue( "Writer write/read cycle failed (1)", mIn1.isIsomorphicWith( m.getBaseModel() ) );
        assertTrue( "Writer write/read cycle failed (2)", mIn2.isIsomorphicWith( m.getBaseModel() ) );
    }
    
    
    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

}


/*
    (c) Copyright Hewlett-Packard Company 2002-2003
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

