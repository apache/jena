/******************************************************************
 * File:        TestReasoners.java
 * Created by:  Dave Reynolds
 * Created on:  19-Jan-03
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: TestReasoners.java,v 1.3 2003-02-03 19:10:25 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.test;

import com.hp.hpl.jena.reasoner.transitiveReasoner.*;
import com.hp.hpl.jena.reasoner.rdfsReasoner1.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.mem.ModelMem;
import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.jena.rdf.model.impl.StatementImpl;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.io.IOException;

/**
 * Unit tests for initial experimental reasoners
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.3 $ on $Date: 2003-02-03 19:10:25 $
 */
public class TestReasoners extends TestCase {
    
    /** Base URI for the test names */
    public static final String NAMESPACE = "http://www.hpl.hp.com/semweb/2003/query_tester/";
    
    /**
     * Boilerplate for junit
     */ 
    public TestReasoners( String name ) {
        super( name ); 
    }
    
    /**
     * Boilerplate for junit.
     * This is its own test suite
     */
    public static TestSuite suite() {
        return new TestSuite(TestReasoners.class);
    }  

    /**
     * Test the basic functioning of a Transitive closure cache 
     */
    // /*
    public void testTransitiveReasoner() throws IOException {
        ReasonerTester tester = new ReasonerTester("transitive/manifest.rdf");
        ReasonerFactory rf = TransitiveReasonerFactory.theInstance();
        assertTrue("transitive reasoner tests", tester.runTests(rf, this, null));
    }
    // */

    /**
     * Test the basic functioning of an RDFS reasoner
     */
    // /*
    public void testRDFSReasoner() throws IOException {
        ReasonerTester tester = new ReasonerTester("rdfs/manifest.rdf");
        ReasonerFactory rf = RDFSReasonerFactory.theInstance();
        assertTrue("RDFS reasoner tests", tester.runTests(rf, this, null));
        // Test effect of switching of property scan - should break container property test case
        Model configuration = new ModelMem();
        configuration.createResource(RDFSReasonerFactory.URI)
                     .addProperty(RDFSReasonerFactory.scanProperties, "false");
        assertTrue("RDFS reasoner tests", 
                    !tester.runTest(NAMESPACE + "rdfs/test17", rf, null, configuration));
    }
    // */
    
    /**
     * Current sub-cases under debug
     */
    /*
    public void testRDFSCase() throws IOException {
        ReasonerTester tester = new ReasonerTester("rdfs/manifest.rdf");
        ReasonerFactory rf = RDFSReasonerFactory.theInstance();
        assertTrue("RDFS reasoner tests", 
                    tester.runTest(NAMESPACE + "rdfs/test17", rf, this, null));
    }
    */

    /**
     * Test the ModelFactory interface
     */
    public void testModelFactoryRDFS() {
        Model data = ModelFactory.createDefaultModel();
        Property p = data.createProperty("urn:x-hp:ex/p");
        Resource a = data.createResource("urn:x-hp:ex/a");
        Resource b = data.createResource("urn:x-hp:ex/b");
        Resource C = data.createResource("urn:x-hp:ex/c");
        data.add(p, RDFS.range, C)
            .add(a, p, b);
        Model result = ModelFactory.createRDFSModel(data);
        StmtIterator i = result.listStatements(new SimpleSelector(b, RDF.type, (RDFNode)null));
        TestUtil.assertIteratorValues(this, i, new Object[] {
            new StatementImpl(b, RDF.type, RDFS.Resource),
            new StatementImpl(b, RDF.type, C)
        });
    }
        
}

/*
    (c) Copyright Hewlett-Packard Company 2003
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

