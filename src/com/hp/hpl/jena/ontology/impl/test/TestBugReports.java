/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            16-Jun-2003
 * Filename           $RCSfile: TestBugReports.java,v $
 * Revision           $Revision: 1.5 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-08-18 14:30:21 $
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

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.*;
import com.hp.hpl.jena.mem.GraphMem;
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
 * @version CVS $Id: TestBugReports.java,v 1.5 2003-08-18 14:30:21 ian_dickinson Exp $
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
    
    
    /**
     * Bug report by Christoph Kunze (Christoph.Kunz@iao.fhg.de). 18/Aug/03 
     * No transaction support in ontmodel.
     */
    public void test_ck_01() {
        Graph g = new GraphMem() {
            TransactionHandler m_t = new MockTransactionHandler();
            public TransactionHandler getTransactionHandler() {return m_t;}
        };
        Model m0 = ModelFactory.createModelForGraph( g );
        OntModel m1 = ModelFactory.createOntologyModel( OntModelSpec.OWL_LITE_MEM, m0 );
        
        assertFalse( "Transaction not started yet", ((MockTransactionHandler) m1.getGraph().getTransactionHandler()).m_inTransaction );
        m1.begin();
        assertTrue( "Transaction started", ((MockTransactionHandler) m1.getGraph().getTransactionHandler()).m_inTransaction );
        m1.abort();
        assertFalse( "Transaction aborted", ((MockTransactionHandler) m1.getGraph().getTransactionHandler()).m_inTransaction );
        assertTrue( "Transaction aborted", ((MockTransactionHandler) m1.getGraph().getTransactionHandler()).m_aborted);
        m1.begin();
        assertTrue( "Transaction started", ((MockTransactionHandler) m1.getGraph().getTransactionHandler()).m_inTransaction );
        m1.commit();
        assertFalse( "Transaction committed", ((MockTransactionHandler) m1.getGraph().getTransactionHandler()).m_inTransaction );
        assertTrue( "Transaction committed", ((MockTransactionHandler) m1.getGraph().getTransactionHandler()).m_committed);
    }
    
    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

    class MockTransactionHandler extends SimpleTransactionHandler {
        boolean m_inTransaction = false;
        boolean m_aborted = false;
        boolean m_committed = false;
        
        public void begin() {m_inTransaction = true;}
        public void abort() {m_inTransaction = false; m_aborted = true;}
        public void commit() {m_inTransaction = false; m_committed = true;}
    }
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

