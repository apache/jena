/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            16-Jun-2003
 * Filename           $RCSfile: TestBugReports.java,v $
 * Revision           $Revision: 1.4 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-07-31 17:49:55 $
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
import com.hp.hpl.jena.vocabulary.OWL;

import junit.framework.*;


/**
 * <p>
 * Unit tests that are derived from user bug reports
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: TestBugReports.java,v 1.4 2003-07-31 17:49:55 ian_dickinson Exp $
 */
public class TestBugReports 
    extends TestCase
{
    // Constants
    //////////////////////////////////

    public static String NS = "http://example.org/test#";
    
    
    // Static variables
    //////////////////////////////////

    // Instance variables
    //////////////////////////////////

    public TestBugReports( String name ) {
        super( name );
    }
    
    
    // Constructors
    //////////////////////////////////

    // External signature methods
    //////////////////////////////////

    /** 
     * Bug report by Mariano Rico Almodóvar [Mariano.Rico@uam.es] on June 16th. Said to raise exception.
     */
    public void test_mra_01() {
        OntModel m = ModelFactory.createOntologyModel(
                                       OntModelSpec.DAML_MEM,
                                       null,
                                       null);
        String myDicURI = "http://somewhere/myDictionaries/1.0#";
        String damlURI  = "http://www.daml.org/2001/03/daml+oil#";
        m.setNsPrefix("DAML", damlURI);

        String c1_uri = myDicURI + "C1";
        OntClass c1 = m.createClass(c1_uri);

        DatatypeProperty p1 = m.createDatatypeProperty( myDicURI + "P1");
        p1.setDomain(c1);

        ByteArrayOutputStream strOut = new ByteArrayOutputStream();

        m.write(strOut,"RDF/XML-ABBREV", myDicURI);
        //m.write(System.out,"RDF/XML-ABBREV", myDicURI);
        
    }
    
    /** Bug report from Holger Knublauch on July 25th 2003. Cannot convert owl:Class to an OntClass */
    public void test_hk_01() {
        // synthesise a mini-document
        String base = "http://jena.hpl.hp.com/test#";
        String doc = "<rdf:RDF" +
        "   xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"" +
        "   xmlns:owl=\"http://www.w3.org/2002/07/owl#\">" +
        "  <owl:Ontology rdf:about=\"\">" +
        "    <owl:imports rdf:resource=\"http://www.w3.org/2002/07/owl\" />" +
        "  </owl:Ontology>" +
        "</rdf:RDF>";
        
        // read in the base ontology, which includes the owl language definition
        // note OWL_MEM => no reasoner is used
        OntModel m = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM, null );
        m.read( new ByteArrayInputStream( doc.getBytes() ), base );

        // we need a resource corresponding to OWL Class but in m
        Resource owlClassRes = m.getResource( OWL.Class.getURI() );
        
        // now can we see this as an OntClass?
        OntClass c = (OntClass) owlClassRes.as( OntClass.class );
        assertNotNull( "OntClass c should not be null", c );
        
        //(OntClass) (ontModel.getProfile().CLASS()).as(OntClass.class);


    }
    
    /**
     * Bug report by federico.carbone@bt.com, 30-July-2003.   A literal can be
     * turned into an individual.
     */
    public void test_fc_01() {
        OntModel m = ModelFactory.createOntologyModel();
        
        ObjectProperty p = m.createObjectProperty( NS + "p" ); 
        Restriction r = m.createRestriction( p );
        HasValueRestriction hv = r.convertToHasValueRestriction( m.createLiteral( 1 ) );
        
        RDFNode n = hv.getHasValue();
        assertFalse( "Should not be able to convert literal to individual", n.canAs( Individual.class ) );
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

