/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            23-May-2003
 * Filename           $RCSfile: OntTestBase.java,v $
 * Revision           $Revision: 1.11 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2004-12-06 13:50:27 $
 *               by   $Author: andy_seaborne $
 *
 * (c) Copyright 2002, 2003, 2004 Hewlett-Packard Development Company, LP
 * (see footer for full conditions)
 *****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.ontology.impl.test;


// Imports
///////////////
import java.util.*;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.test.TestUtil;

import junit.framework.*;


/**
 * <p>
 * Generic test case for ontology unit testing
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: OntTestBase.java,v 1.11 2004-12-06 13:50:27 andy_seaborne Exp $
 */
public abstract class OntTestBase 
    extends TestSuite
{
    // Constants
    //////////////////////////////////

    public static final String BASE = "http://jena.hpl.hp.com/testing/ontology";
    public static final String NS = BASE + "#";
    
    
    // Static variables
    //////////////////////////////////

    // Instance variables
    //////////////////////////////////

    
    // Constructors
    //////////////////////////////////

    public OntTestBase( String name ) {
        super( name );
        TestCase[] tc = getTests();
        
        for (int i = 0;  i < tc.length;  i++) {
            addTest( tc[i] );
        }
    }
    
    // External signature methods
    //////////////////////////////////


    // Internal implementation methods
    //////////////////////////////////

    /** Return the array of tests for the suite */
    protected  OntTestCase[] getTests() {
        return null;
    }
    
    
    //==============================================================================
    // Inner class definitions
    //==============================================================================

    protected abstract class OntTestCase
        extends TestCase
    {
        protected boolean m_inOWL;
        protected boolean m_inOWLLite;
        protected boolean m_inDAML;
        protected boolean m_inRDFS;
        protected String m_langElement;
        protected boolean m_owlLang = true;
        protected boolean m_owlLiteLang = false;
        protected boolean m_rdfsLang = false;
        protected boolean m_damlLang = false;

        public OntTestCase( String langElement, boolean inOWL, boolean inOWLLite, boolean inDAML, boolean inRDFS ) {
            super( "Ontology API test " + langElement );
            m_langElement = langElement;
            m_inOWL = inOWL;
            m_inOWLLite = inOWLLite;
            m_inDAML = inDAML;
            m_inRDFS = inRDFS;
        }

        public void runTest()
            throws Exception
        {
            // we don't want inferencing for these unit tests
            runTest( ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM, null ), m_inOWL );
            
            m_owlLiteLang = true;
            
            runTest( ModelFactory.createOntologyModel( OntModelSpec.OWL_LITE_MEM, null ), m_inOWLLite );
            
            // now DAML
            m_owlLang = false;
            m_owlLiteLang = false;
            m_damlLang = true;
            
            runTest( ModelFactory.createOntologyModel( OntModelSpec.DAML_MEM, null ), m_inDAML );
            
            // now RDFS
            
            m_rdfsLang = true;
            m_damlLang = false;
            runTest( ModelFactory.createOntologyModel( OntModelSpec.RDFS_MEM, null ), m_inRDFS);
        }
    
        protected void runTest( OntModel m, boolean inModel )
            throws Exception 
        {
            boolean profileEx = false;
        
            try {
                ontTest( m );
            }
            catch (ProfileException e) {
                profileEx = true;
            }
        
            assertEquals( "language element " + m_langElement + " was " + (inModel ? "" : "not") + " expected in model " + m.getProfile().getLabel(), inModel, !profileEx );
        }
    
        /** Does the work in the test sub-class */
        protected abstract void ontTest( OntModel m ) throws Exception;
    
        /** Test that an iterator delivers the expected values */
        protected void iteratorTest( Iterator i, Object[] expected ) {
            TestUtil.assertIteratorValues( this, i, expected );
        }
    
        public void setUp() {
            // ensure the ont doc manager is in a consistent state
            OntDocumentManager.getInstance().reset( true );
        }
        
        protected boolean owlFull() {
            return m_owlLang && !m_owlLiteLang;
        }
        
    }
}


/*
    (c) Copyright 2002, 2003, 2004 Hewlett-Packard Development Company, LP
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

