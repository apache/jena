/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            4 Mar 2003
 * Filename           $RCSfile: TestOntDocumentManager.java,v $
 * Revision           $Revision: 1.5 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2003-06-22 19:20:44 $
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
import junit.framework.*;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;



/**
 * <p>
 * Unit tests for document manager
 * </p>
 *
 * @author Ian Dickinson, HP Labs
 *         (<a  href="mailto:Ian.Dickinson@hp.com" >email</a>)
 * @version CVS $Id: TestOntDocumentManager.java,v 1.5 2003-06-22 19:20:44 ian_dickinson Exp $
 */
public class TestOntDocumentManager
    extends TestCase
{
    // Constants
    //////////////////////////////////

    private static Boolean F = Boolean.FALSE;
    private static Boolean T = Boolean.TRUE;

    // Static variables
    //////////////////////////////////

    public static final Integer cnt( int x ) {return new Integer(x);}
    
    /* Data for various combinations of test import conditions */
    public static Object[][] s_testData = new Object[][] {
        // directory to look in             marker count        imports     path (null = default)
        {  "testing/ontology/testImport1",  cnt(1),             T,          null },
        {  "testing/ontology/testImport2",  cnt(2),             T,          null },
        {  "testing/ontology/testImport2",  cnt(1),             F,          null },
        {  "testing/ontology/testImport3",  cnt(3),             T,          null },
        {  "testing/ontology/testImport4",  cnt(2),             T,          null },
        {  "testing/ontology/testImport5",  cnt(2),             T,          "file:testing/ontology/testImport5/ont-policy.rdf" }
    };
    
    
    // Instance variables
    //////////////////////////////////


    // Constructors
    //////////////////////////////////

    public TestOntDocumentManager( String s ) {
        super( s );
    }

    public static TestSuite suite() {
        TestSuite suite = new TestSuite( "TestOntDocumentManager" );
        
        // add the fixed test cases
        suite.addTest( new TestOntDocumentManager( "testInitialisation") );
        // TODO requires bug fix from kers
        // suite.addTest( new TestOntDocumentManager( "testManualAssociation") );
        suite.addTest( new TestOntDocumentManager( "testIgnoreImport") );
        
        // add the data-driven test cases
        for (int i = 0;  i < s_testData.length;  i++) {
            suite.addTest( new DocManagerImportTest( (String) s_testData[i][0], 
                                                     ((Integer) s_testData[i][1]).intValue(), 
                                                     ((Boolean) s_testData[i][2]).booleanValue(), 
                                                     (String) s_testData[i][3]) );
        }
        
        return suite;
    }
    
    
    // External signature methods
    //////////////////////////////////

    public void testInitialisation() {
        OntDocumentManager mgr = new OntDocumentManager();
        
        assertTrue( "Should be at least one specification loaded", mgr.listDocuments().hasNext() );
        assertNotNull( "cache URL for owl should not be null", mgr.doAltURLMapping( "http://www.w3.org/2002/07/owl" ));
        assertEquals( "cache URL for owl not correct", "file:vocabularies/owl.owl", mgr.doAltURLMapping( "http://www.w3.org/2002/07/owl" ));
        String s = mgr.getPrefixForURI( "http://www.w3.org/2002/07/owl" );
        // TODO - requires bug fix from kers 
        // assertEquals( "prefix for owl not correct", "owl", mgr.getPrefixForURI( "http://www.w3.org/2002/07/owl" ));
        
        mgr = new OntDocumentManager( "" );
        assertTrue( "Should be no specification loaded", !mgr.listDocuments().hasNext() );
        
        // make sure we don't fail on null
        mgr = new OntDocumentManager( null );
        assertTrue( "Should be no specification loaded", !mgr.listDocuments().hasNext() );
        
    }
    
    public void testManualAssociation() {
        OntDocumentManager mgr = new OntDocumentManager( null );
        
        mgr.addPrefixMapping( "http://www.w3.org/2002/07/owl", "owl" );
        assertEquals( "prefix for owl not correct", "owl", mgr.getPrefixForURI( "http://www.w3.org/2002/07/owl" ));
        assertEquals( "URI for owl not correct", "http://www.w3.org/2002/07/owl", mgr.getURIForPrefix( "owl" ));
        
        mgr.addAltEntry( "http://www.w3.org/2002/07/owl", "file:foo.bar" );
        assertEquals( "Failed to retrieve cache location", "file:foo.bar", mgr.doAltURLMapping( "http://www.w3.org/2002/07/owl" ) ); 
        
        mgr.addLanguageEntry( "http://www.w3.org/2002/07/owl", "http://www.w3.org/2002/07/owl" );
        assertEquals( "Failed to retrieve language", "http://www.w3.org/2002/07/owl", mgr.getLanguage( "http://www.w3.org/2002/07/owl" ) ); 
    }
    
    
    public void testIgnoreImport() {
        OntDocumentManager dm = new OntDocumentManager();
        
        dm.addIgnoreImport( "file:testing/ontology/testImport3/c.owl" );
        
        OntModelSpec spec = new OntModelSpec( null, dm, null, ProfileRegistry.OWL_LANG );
        OntModel m = ModelFactory.createOntologyModel( spec, null );
        assertNotNull( "Ontology model should not be null", m );
            
        m.read( "file:testing/ontology/testImport3/a.owl" );
        assertEquals( "Marker count not correct", 2, countMarkers( m ));
    }
    
        
        
    /* count the number of marker statements in the combined model */
    public static int countMarkers( Model m ) {
        int count = 0;
            
        Resource marker = m.getResource( "http://jena.hpl.hp.com/2003/03/testont#Marker" );
        for (StmtIterator i = m.listStatements( null, RDF.type, marker ); i.hasNext();  ) {
            count++;
            i.next();
        }
            
        return count;
    }
    
    // Internal implementation methods
    //////////////////////////////////


    //==============================================================================
    // Inner class definitions
    //==============================================================================
    
    /**
     * Document manager imports test case. Each test case starts with a root model (always a.owl in some
     * directory), and loads the model. Depending on the model contents, and the settings of the doc
     * manager, other models will be loaded. Each model is set to contain a fixed number of marker
     * statements of the form:
     * <code><pre>
     *   <Marker rdf:ID="a0" />
     * </pre></code>
     * the test for having correctly loaded the models is to count the markers and compare to the predicted
     * total.
     */
    static class DocManagerImportTest
        extends TestCase
    {
        String m_dir;
        int m_count;
        String m_path;
        boolean m_processImports;
        
        /* constuctor */
        DocManagerImportTest( String dir, int count, boolean processImports, String path ) {
            super( dir );
            m_dir = dir;
            m_count = count;
            m_path = path;
            m_processImports = processImports;
        }
        
        // external contract methods
        
        public void runTest() {
            OntDocumentManager dm = new OntDocumentManager();
            
            // adjust the doc manager properties according to the test setup 
            dm.setProcessImports( m_processImports );
            if (m_path != null) {
                dm.setMetadataSearchPath( m_path, true );
            }
            
            // now load the model - we always start from a.owl in the given directory
            OntModelSpec spec = new OntModelSpec( null, dm, null, ProfileRegistry.OWL_LANG );
            OntModel m = ModelFactory.createOntologyModel( spec, null );
            assertNotNull( "Ontology model should not be null", m );
            
            m.read( "file:" + m_dir + "/a.owl" );
            assertEquals( "Marker count not correct", m_count, countMarkers( m ));
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

