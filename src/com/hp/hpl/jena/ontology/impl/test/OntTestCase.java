/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            26-Mar-2003
 * Filename           $RCSfile: OntTestCase.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-03-31 14:33:19 $
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
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.ontology.path.*;
import com.hp.hpl.jena.ontology.path.impl.NamedUnitPathExpr;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

import junit.framework.*;


/**
 * <p>
 * Shared base class for testing the ontology API components
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: OntTestCase.java,v 1.2 2003-03-31 14:33:19 ian_dickinson Exp $
 */
public abstract class OntTestCase 
    extends TestCase
{
    // Constants
    //////////////////////////////////

    public static final Boolean T = Boolean.TRUE;
    public static final Boolean F = Boolean.FALSE;
    
    /** standard test namespace */
    public static final String BASE = "http://jena.hpl.hp.com/testing/ontology";
    public static final String NS = BASE + "#";
    
    
    // Static variables
    //////////////////////////////////

    // Instance variables
    //////////////////////////////////

    // Constructors
    //////////////////////////////////

    public OntTestCase( String name ) {
        super( name );
    }
    
    
    // External signature methods
    //////////////////////////////////

    /** Answer a test suite built from the test data supplied by the sub-class */
    protected TestSuite getSuite() {
        TestSuite s = new TestSuite( getTestName() );
        Object[][] td = testData();
        
        for (int i = 0;  i < td.length;  i++) {
            s.addTest( new OntologyPropertyTest( td[i] ) );
        }
        
        return s;
    }


    // Internal implementation methods
    //////////////////////////////////

    /** Answer the name of the test */
    protected abstract String getTestName();
    
    /** Answer the array of test data that sets up the test */
    protected abstract Object[][] testData();
    
    
    //==============================================================================
    // Inner class definitions
    //==============================================================================

    /**
     * This class implements the actual JUnit test case defined by the data in the test 
     * sub-classes
     */
    public static class OntologyPropertyTest
       extends TestCase
    {
        protected PS m_ps;
        protected Property m_property;
        protected String m_profileURI;
        protected String m_source;
        protected boolean m_inProfile;
        protected int m_count;
        protected String m_resourceValURI;
        protected Object m_literalVal;
        protected OntModel m_model;
        protected Resource m_resourceType;
        
        public OntologyPropertyTest( Object[] tData ) {
            super( (String) tData[0] );
            
            m_ps             = (PS)       tData[1];                     // the inner class for generating the path set
            m_property       = (Property) tData[2];                     // the property we're testing (mostly for documentation purposes)
            m_profileURI     = (String)   tData[3];                     // the language profile we're testing against
            m_source         = (String)   tData[4];                     // the URI of the source data document to load
            m_inProfile      = ((Boolean) tData[5]).booleanValue();     // flag for whether this property is expected in the profile
            m_count          =                                          // expected count of values for the path set
                (tData[6] == null) ? 0 : ((Integer) tData[6]).intValue();
            m_resourceValURI = (String)   tData[7];                     // the URI of the resource value of the path set, or null
            m_resourceType   = (Resource) tData[8];                     // the URI of the rdf:type of the anon resource value, or null 
            m_literalVal     =            tData[9];                     // the Java value of the literal value of the path set
        }
        
        
        // External signature methods
        /////////////////////////////
        
        public void setUp() {
            m_model = ModelFactory.createOntologyModel( m_profileURI );
            m_model.read( m_source );
        }
        
        public void tearDown() {
        }
        
        /**
         * To run the test, we generate the path set and check that it conforms to
         * the expectations from the given test conditions
         */
        public void runTest() {
            // first check if the property evaluates to a path set
            PathSet ps;
            try {
                ps = m_ps.ps( m_model );
            }
            catch (OntologyException e) {
                ps = null;
            }
            
            // test whether this element should or should not be in the language profile
            assertEquals( "property should " + (m_inProfile ? " " : "not ") + "be in language profile", m_inProfile, ps != null );
            
            // if it is in the language profile, check that it is working properly
            if (m_inProfile) {
                // now check that the property is correct
                PathExpr pe = ps.getExpression();
                
                // uses Secret Knowledge (tm)
                assertEquals( "Property named in path expression not correct", m_property, ((NamedUnitPathExpr) pe).getProperty() );
                
                // check the count 
                assertEquals( "Property set size for " + m_property + " not correct", m_count, ps.size() );
                
                // are we expecting a value?
                if (m_resourceValURI != null) {
                    // a resource value - check the URI
                    assertTrue( "Property value for " + m_property + " should be a resource", ps.getValue() instanceof Resource );
                    assertEquals( "Property value for " + m_property + " not correct", m_model.getResource( m_resourceValURI ), ps.getValue() );
                }
                else if (m_resourceType != null) {
                    // an anon resource value - check the rdf:type
                    assertTrue( "Property value for " + m_property + " should be a resource", ps.getValue() instanceof Resource );
                    assertEquals( "Property value for " + m_property + " has wrong type", 
                                  m_resourceType, 
                                  ((Resource) ps.getValue()).getProperty( RDF.type ).getResource() );
                }
                else if (m_literalVal != null) {
                    // a literal value 
                    RDFNode v = ps.getValue();
                    assertTrue( "Property value for " + m_property + " should be a literal", v instanceof Literal );
                    assertTrue( "Property value for " + m_property + " failed equality test", ((Literal) v).getValue().equals( m_literalVal ) );
                }
            }
        }
    }
    
    
    /**
     * <p>
     * Interface to get the path set to test, for building inner classes in the test
     * data sets (because bloody Java doesn't have first-class code objects like a 
     * decent lanuage).
     * </p>
     */
    public interface PS {
        public PathSet ps( OntModel m );
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
