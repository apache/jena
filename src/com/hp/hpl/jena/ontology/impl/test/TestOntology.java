/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            26-Mar-2003
 * Filename           $RCSfile: TestOntology.java,v $
 * Revision           $Revision: 1.4 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-05-23 11:13:05 $
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
import junit.framework.TestSuite;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.ontology.path.*;
import com.hp.hpl.jena.vocabulary.*;


/**
 * <p>
 * Unit test cases for the Ontology class
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: TestOntology.java,v 1.4 2003-05-23 11:13:05 ian_dickinson Exp $
 */
public class TestOntology
    extends PathTestCase 
{
    // Constants
    //////////////////////////////////

    // Static variables
    //////////////////////////////////



    // Instance variables
    //////////////////////////////////

    // Constructors
    //////////////////////////////////

    public TestOntology( String s ) {
        super( s );
    }
    
    protected String getTestName() {
        return "TestOntology";
    }
    
    public static TestSuite suite() {
        return new TestOntology( "TestOntology" ).getSuite();
    }
    
    
    /** Fields are testID, pathset, property, profileURI, sourceData, expected, count, valueURI, rdfTypeURI, valueLit */
    protected Object[][] psTestData() {
        return new Object[][] {
            {   
                "OWL Ontology.imports",
                new PS() { 
                    public PathSet ps( OntModel m ) {
                        return ((Ontology) m.getResource( BASE )
                               .as( Ontology.class )).p_imports(); } 
                },
                OWL.imports,
                ProfileRegistry.OWL_LANG,
                "file:testing/ontology/owl/Ontology/test.rdf",
                T,
                new Integer( 1 ),
                "http://www.w3.org/2000/01/rdf-schema",
                null,
                null
            },
            {
                "OWL Ontology.versionInfo",
                new PS() { 
                   public PathSet ps( OntModel m ) { 
                       return ((Ontology) m.getResource( BASE )
                              .as( Ontology.class )).p_versionInfo(); } 
                },
                OWL.versionInfo,
                ProfileRegistry.OWL_LANG,
                "file:testing/ontology/owl/Ontology/test.rdf",
                T,
                new Integer( 1 ),
                null,
                null,
                "test version info"
            },
            {
                "OWL Ontology.priorVersion",
                new PS() { 
                  public PathSet ps( OntModel m ) { 
                      return ((Ontology) m.getResource( BASE )
                             .as( Ontology.class )).p_priorVersion(); } 
                },
                OWL.priorVersion,
                ProfileRegistry.OWL_LANG,
                "file:testing/ontology/owl/Ontology/test.rdf",
                T,
                new Integer( 1 ),
                "http://jena.hpl.hp.com/testing/test-ontology-1a",
                null,
                null
            },
            {
                "OWL Ontology.backwardCompatibleWith",
                new PS() { 
                    public PathSet ps( OntModel m ) { 
                        return ((Ontology) m.getResource( BASE )
                               .as( Ontology.class )).p_backwardCompatibleWith(); } 
                },
                OWL.backwardCompatibleWith,
                ProfileRegistry.OWL_LANG,
                "file:testing/ontology/owl/Ontology/test.rdf",
                T,
                new Integer( 1 ),
                "http://jena.hpl.hp.com/testing/test-ontology-1b",
                null,
                null
            },
            {   
                "OWL Ontology.incompatibleWith",
                new PS() { 
                    public PathSet ps( OntModel m ) { 
                        return ((Ontology) m.getResource( BASE )
                               .as( Ontology.class )).p_incompatibleWith(); } 
                },
                OWL.incompatibleWith,
                ProfileRegistry.OWL_LANG,
                "file:testing/ontology/owl/Ontology/test.rdf",
                T,
                new Integer( 1 ),
                "http://jena.hpl.hp.com/testing/test-ontology-1c",
                null,
                null
            },
/*            {   
                "OWL Ontology.comment",
                new PS() { 
                    public PathSet ps( OntModel m ) { 
                        return ((Ontology) m.getResource( BASE )
                               .as( Ontology.class )).p_comment(); } 
                },
                RDFS.comment,
                ProfileRegistry.OWL_LANG,
                "file:testing/ontology/owl/Ontology/test.rdf",
                T,
                new Integer( 1 ),
                null,
                null,
                "a comment"
            },
            {   
                "OWL Ontology.label",
                new PS() { 
                    public PathSet ps( OntModel m ) { 
                        return ((Ontology) m.getResource( BASE )
                               .as( Ontology.class )).p_label(); } 
                },
                RDFS.label,
                ProfileRegistry.OWL_LANG,
                "file:testing/ontology/owl/Ontology/test.rdf",
                T,
                new Integer( 1 ),
                null,
                null,
                "a label"
            },
            {   
                "OWL Ontology.seeAlso",
                new PS() { 
                    public PathSet ps( OntModel m ) { 
                        return ((Ontology) m.getResource( BASE )
                               .as( Ontology.class )).p_seeAlso(); } 
                },
                RDFS.seeAlso,
                ProfileRegistry.OWL_LANG,
                "file:testing/ontology/owl/Ontology/test.rdf",
                T,
                new Integer( 1 ),
                null,
                null,
                "xyz"
            },
            {   
                "OWL Ontology.isDefinedBy",
                new PS() { 
                    public PathSet ps( OntModel m ) { 
                        return ((Ontology) m.getResource( BASE )
                               .as( Ontology.class )).p_isDefinedBy(); } 
                },
                RDFS.isDefinedBy,
                ProfileRegistry.OWL_LANG,
                "file:testing/ontology/owl/Ontology/test.rdf",
                T,
                new Integer( 1 ),
                null,
                null,
                "abc"
            },
*/            {   
                "DAML Ontology.imports",
                new PS() { 
                    public PathSet ps( OntModel m ) { 
                        return ((Ontology) m.getResource( BASE )
                               .as( Ontology.class )).p_imports(); } 
                },
                DAML_OIL.imports,
                ProfileRegistry.DAML_LANG,
                "file:testing/ontology/daml/Ontology/test.rdf",
                T,
                new Integer( 1 ),
                "http://www.w3.org/2000/01/rdf-schema",
                null,
                null
            },
            {
                "DAML Ontology.versionInfo",
                new PS() { 
                   public PathSet ps( OntModel m ) { 
                       return ((Ontology) m.getResource( BASE )
                              .as( Ontology.class )).p_versionInfo(); } 
                },
                DAML_OIL.versionInfo,
                ProfileRegistry.DAML_LANG,
                "file:testing/ontology/daml/Ontology/test.rdf",
                T,
                new Integer( 1 ),
                null,
                null,
                "test version info"
            },
            {
                "DAML Ontology.priorVersion",
                new PS() { 
                  public PathSet ps( OntModel m ) { 
                      return ((Ontology) m.getResource( BASE )
                             .as( Ontology.class )).p_priorVersion(); } 
                },
                null,
                ProfileRegistry.DAML_LANG,
                "file:testing/ontology/daml/Ontology/test.rdf",
                F,
                null,
                null,
                null,
                null
            },
            {
                "DAML Ontology.backwardCompatibleWith",
                new PS() { 
                    public PathSet ps( OntModel m ) { 
                        return ((Ontology) m.getResource( BASE )
                               .as( Ontology.class )).p_backwardCompatibleWith(); } 
                },
                null,
                ProfileRegistry.DAML_LANG,
                "file:testing/ontology/daml/Ontology/test.rdf",
                F,
                null,
                null,
                null,
                null
            },
            {   
                "DAML Ontology.incompatibleWith",
                new PS() { 
                    public PathSet ps( OntModel m ) { 
                        return ((Ontology) m.getResource( BASE )
                               .as( Ontology.class )).p_incompatibleWith(); } 
                },
                null,
                ProfileRegistry.DAML_LANG,
                "file:testing/ontology/daml/Ontology/test.rdf",
                F,
                null,
                null,
                null,
                null
            },
/*            {   
                "DAML Ontology.comment",
                new PS() { 
                    public PathSet ps( OntModel m ) { 
                        return ((Ontology) m.getResource( BASE )
                               .as( Ontology.class )).p_comment(); } 
                },
                RDFS.comment,
                ProfileRegistry.DAML_LANG,
                "file:testing/ontology/daml/Ontology/test.rdf",
                T,
                new Integer( 1 ),
                null,
                null,
                "a comment"
            },
            {   
                "DAML Ontology.label",
                new PS() { 
                    public PathSet ps( OntModel m ) { 
                        return ((Ontology) m.getResource( BASE )
                               .as( Ontology.class )).p_label(); } 
                },
                RDFS.label,
                ProfileRegistry.DAML_LANG,
                "file:testing/ontology/daml/Ontology/test.rdf",
                T,
                new Integer( 1 ),
                null,
                null,
                "a label"
            },
            {   
                "DAML Ontology.seeAlso",
                new PS() { 
                    public PathSet ps( OntModel m ) { 
                        return ((Ontology) m.getResource( BASE )
                               .as( Ontology.class )).p_seeAlso(); } 
                },
                RDFS.seeAlso,
                ProfileRegistry.DAML_LANG,
                "file:testing/ontology/daml/Ontology/test.rdf",
                T,
                new Integer( 1 ),
                null,
                null,
                "xyz"
            },
            {   
                "DAML Ontology.isDefinedBy",
                new PS() { 
                    public PathSet ps( OntModel m ) { 
                        return ((Ontology) m.getResource( BASE )
                               .as( Ontology.class )).p_isDefinedBy(); } 
                },
                RDFS.isDefinedBy,
                ProfileRegistry.DAML_LANG,
                "file:testing/ontology/daml/Ontology/test.rdf",
                T,
                new Integer( 1 ),
                null,
                null,
                "abc"
            },
*/      };
    }
    
    
    // External signature methods
    //////////////////////////////////

    
    
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


