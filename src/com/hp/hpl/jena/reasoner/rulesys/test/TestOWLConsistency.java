/******************************************************************
 * File:        TestOWLConsistency.java
 * Created by:  Dave Reynolds
 * Created on:  24-Aug-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: TestOWLConsistency.java,v 1.2 2003-08-27 13:11:16 andy_seaborne Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.test;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.util.ModelLoader;

//import java.util.*;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test the preliminary OWL validation rules.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.2 $ on $Date: 2003-08-27 13:11:16 $
 */
public class TestOWLConsistency extends TestCase {
     
    /** The tbox/ontology file to test against sample data */
    public static final String testTbox = "file:testing/reasoners/owl/tbox.owl";
    
    /** A cached copy of the bound reasoner */
    public static Reasoner reasonerCache;
     
    /**
     * Boilerplate for junit
     */ 
    public TestOWLConsistency( String name ) {
        super( name ); 
    }
    
    /**
     * Boilerplate for junit.
     * This is its own test suite
     */
    public static TestSuite suite() {
        return new TestSuite( TestOWLConsistency.class ); 
//        TestSuite suite = new TestSuite();
//        suite.addTest(new TestOWLConsistency( "testInconsistent5" ));
//        return suite;
    }  

    /**
     * Create, or retrieve from cache, an OWL reasoner already bound
     * to the test tbox.
     */
    public Reasoner makeReasoner() {
        if (reasonerCache == null) {
            Model tbox = ModelLoader.loadModel(testTbox);
            reasonerCache = ReasonerRegistry.getOWLReasoner().bindSchema(tbox.getGraph());
        }
        return reasonerCache;
    }
    
    /**
     * Should be consistent.
     */
    public void testConsistent() {
        assertTrue(doTestOn("file:testing/reasoners/owl/consistentData.rdf"));
    }
    
    /**
     * Should find problem due to overlap of disjoint classes.
     */
    public void testInconsistent1() {
        assertTrue( ! doTestOn("file:testing/reasoners/owl/inconsistent1.rdf"));
    }
    
    /**
     * Should find problem due to type violations
     */
    public void testInconsistent2() {
        assertTrue( ! doTestOn("file:testing/reasoners/owl/inconsistent2.rdf"));
    }
    
    /**
     * Should find problem due to count violations
     */
    public void testInconsistent3() {
        assertTrue( ! doTestOn("file:testing/reasoners/owl/inconsistent3.rdf"));
    }
    
    /**
     * Should find distinct values for a functional property
     */
    public void testInconsistent4() {
        assertTrue( ! doTestOn("file:testing/reasoners/owl/inconsistent4.rdf"));
    }
    
    /**
     * Should find type clash due to allValuesFrom rdfs:Literal
     */
    public void testInconsistent5() {
        assertTrue( ! doTestOn("file:testing/reasoners/owl/inconsistent5.rdf"));
    }
    
    /**
     * Run a single consistency test on the given data file.
     */
    private boolean doTestOn(String dataFile) {
//        System.out.println("Test: " + dataFile);
        Model data = ModelLoader.loadModel(dataFile);
        InfModel infmodel = ModelFactory.createInfModel(makeReasoner(), data);
        ValidityReport reportList = infmodel.validate();
        /* Debug only
        if (reportList.isValid()) {
            System.out.println("No reported problems");
        } else {
            for (Iterator i = reportList.getReports(); i.hasNext(); ) {
                ValidityReport.Report report = (ValidityReport.Report)i.next();
                System.out.println("- "  + report);
            }
        }
        */
        return reportList.isValid();
    }
}



/*
    (c) Copyright 2003 Hewlett-Packard Development Company, LP
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