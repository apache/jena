/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            03-Apr-2003
 * Filename           $RCSfile: TestCreate.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-04-03 15:19:18 $
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
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.vocabulary.*;

import junit.framework.*;


/**
 * <p>
 * Unit test cases for creating values in ontology models
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: TestCreate.java,v 1.1 2003-04-03 15:19:18 ian_dickinson Exp $
 */
public class TestCreate 
    extends TestCase
{
    // Constants
    //////////////////////////////////
    public static final String BASE = "http://jena.hpl.hp.com/testing/ontology";
    public static final String NS = BASE + "#";

    // Static variables
    //////////////////////////////////

    protected static CreateTestCase[] testCases = new CreateTestCase[] {
        new CreateTestCase( "OWL create ontology", ProfileRegistry.OWL_LANG, BASE ) {
            public OntResource doCreate( OntModel m )   { return m.createOntology( BASE ); }
            public boolean test( OntResource r )        { return r instanceof Ontology;}
        },
        new CreateTestCase( "DAML create ontology", ProfileRegistry.DAML_LANG, BASE ) {
            public OntResource doCreate( OntModel m )   { return m.createOntology( BASE ); }
            public boolean test( OntResource r )        { return r instanceof Ontology;}
        },
        
        new CreateTestCase( "OWL create class", ProfileRegistry.OWL_LANG, NS + "C" ) {
            public OntResource doCreate( OntModel m )   { return m.createClass( NS + "C" ); }
            public boolean test( OntResource r )        { return r instanceof OntClass;}
        },
        new CreateTestCase( "OWL create anon class", ProfileRegistry.OWL_LANG, null ) {
            public OntResource doCreate( OntModel m )   { return m.createClass(); }
            public boolean test( OntResource r )        { return r instanceof OntClass;}
        },
        new CreateTestCase( "DAML create class", ProfileRegistry.DAML_LANG, NS + "C" ) {
            public OntResource doCreate( OntModel m )   { return m.createClass( NS + "C" ); }
            public boolean test( OntResource r )        { return r instanceof OntClass;}
        },
        new CreateTestCase( "DAML create anon class", ProfileRegistry.DAML_LANG, null ) {
            public OntResource doCreate( OntModel m )   { return m.createClass(); }
            public boolean test( OntResource r )        { return r instanceof OntClass;}
        },
        
        new CreateTestCase( "OWL create individual", ProfileRegistry.OWL_LANG, NS + "a" ) {
            public OntResource doCreate( OntModel m )   { 
                OntClass c = m.createClass( NS + "C" );
                return m.createIndividual( c, NS + "a" ); 
            }
            public boolean test( OntResource r )        { return r instanceof Individual;}
        },
        new CreateTestCase( "OWL create anon individual", ProfileRegistry.OWL_LANG, null ) {
            public OntResource doCreate( OntModel m )   { 
                OntClass c = m.createClass( NS + "C" );
                return m.createIndividual( c ); 
            }
            public boolean test( OntResource r )        { return r instanceof Individual;}
        },
        new CreateTestCase( "DAML create individual", ProfileRegistry.DAML_LANG, NS + "a" ) {
            public OntResource doCreate( OntModel m )   { 
                OntClass c = m.createClass( NS + "C" );
                return m.createIndividual( c, NS + "a" ); 
            }
            public boolean test( OntResource r )        { return r instanceof Individual;}
        },
        new CreateTestCase( "DAML create anon individual", ProfileRegistry.DAML_LANG, null ) {
            public OntResource doCreate( OntModel m )   { 
                OntClass c = m.createClass( NS + "C" );
                return m.createIndividual( c ); 
            }
            public boolean test( OntResource r )        { return r instanceof Individual;}
        },
        
        new CreateTestCase( "OWL create object property", ProfileRegistry.OWL_LANG, NS + "p" ) {
            public OntResource doCreate( OntModel m )   { return m.createObjectProperty( NS + "p" ); }
            public boolean test( OntResource r )        { return r instanceof ObjectProperty;}
        },
        new CreateTestCase( "OWL create datatype property", ProfileRegistry.OWL_LANG, NS + "p" ) {
            public OntResource doCreate( OntModel m )   { return m.createDatatypeProperty( NS + "p" ); }
            public boolean test( OntResource r )        { return r instanceof DatatypeProperty;}
        },
        new CreateTestCase( "OWL create annotation property", ProfileRegistry.OWL_LANG, NS + "p" ) {
            public OntResource doCreate( OntModel m )   { return m.createAnnotationProperty( NS + "p" ); }
            public boolean test( OntResource r )        { return r instanceof AnnotationProperty;}
        },
        new CreateTestCase( "DAML create object property", ProfileRegistry.DAML_LANG, NS + "p" ) {
            public OntResource doCreate( OntModel m )   { return m.createObjectProperty( NS + "p" ); }
            public boolean test( OntResource r )        { return r instanceof ObjectProperty;}
        },
        new CreateTestCase( "DAML create datatype property", ProfileRegistry.DAML_LANG, NS + "p" ) {
            public OntResource doCreate( OntModel m )   { return m.createDatatypeProperty( NS + "p" ); }
            public boolean test( OntResource r )        { return r instanceof DatatypeProperty;}
        },

        new CreateTestCase( "OWL create axiom", ProfileRegistry.OWL_LANG, null ) {
            public OntResource doCreate( OntModel m )   { return m.createAxiom( OWL.AllDifferent, null ); }
            public boolean test( OntResource r )        { return r instanceof Axiom;}
        },
        
        new CreateTestCase( "OWL create restriction", ProfileRegistry.OWL_LANG, NS + "C" ) {
            public OntResource doCreate( OntModel m )   { return m.createRestriction( NS + "C" ); }
            public boolean test( OntResource r )        { return r instanceof Restriction;}
        },
        new CreateTestCase( "OWL create anon restriction", ProfileRegistry.OWL_LANG, null ) {
            public OntResource doCreate( OntModel m )   { return m.createRestriction(); }
            public boolean test( OntResource r )        { return r instanceof Restriction;}
        },
        new CreateTestCase( "DAML create restriction", ProfileRegistry.DAML_LANG, NS + "C" ) {
            public OntResource doCreate( OntModel m )   { return m.createRestriction( NS + "C" ); }
            public boolean test( OntResource r )        { return r instanceof Restriction;}
        },
        new CreateTestCase( "DAML create anon restriction", ProfileRegistry.DAML_LANG, null ) {
            public OntResource doCreate( OntModel m )   { return m.createRestriction(); }
            public boolean test( OntResource r )        { return r instanceof Restriction;}
        },
        
    };
    
    // Instance variables
    //////////////////////////////////

    // Constructors
    //////////////////////////////////

    public TestCreate( String name ) {
        super( name );
    }
    
    
    // External signature methods
    //////////////////////////////////

    protected String getTestName() {
        return "TestCreate";
    }
    
    public static TestSuite suite() {
        TestSuite s = new TestSuite( "TestCreate" );
        
        for (int i = 0;  i < testCases.length;  i++) {
            s.addTest( testCases[i] );
        }
        
        return s;
    }

    
    
    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

    protected static class CreateTestCase
        extends TestCase
    {
        protected String m_lang;
        protected String m_uri;
        
        public CreateTestCase( String name, String lang, String uri ) {
            super( name );
            m_lang = lang;
            m_uri = uri;
        }
        
        public void runTest() {
            OntModel m = ModelFactory.createOntologyModel( m_lang );
            
            // do the creation step
            OntResource r = doCreate( m );
            assertNotNull( "Result of creation step should not be null", r );
            
            if (m_uri == null) {
                assertTrue( "Created resource should be anonymous", r.isAnon() );
            }
            else { 
                assertEquals( "Created resource has wrong uri", m_uri, r.getURI() );
            }
            
            assertTrue( "Result test failed", test( r ));
        }
        
        /* get the iterator */
        public OntResource doCreate( OntModel m ) {
            throw new RuntimeException("This method should be overridden");
        }
        
        /* test the Java type of the result, and other tests */
        public boolean test( OntResource r ) {
            return true;
        }
        
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
