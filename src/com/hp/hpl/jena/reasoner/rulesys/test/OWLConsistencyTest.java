/******************************************************************
 * File:        OWLConsistencyTester.java
 * Created by:  Dave Reynolds
 * Created on:  14-Feb-2005
 * 
 * (c) Copyright 2005, Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: OWLConsistencyTest.java,v 1.2 2005-02-15 16:28:04 der Exp $
 *****************************************************************/

package com.hp.hpl.jena.reasoner.rulesys.test;

import java.util.Iterator;

import junit.framework.TestCase;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ReasonerFactory;
import com.hp.hpl.jena.reasoner.ValidityReport;
import com.hp.hpl.jena.util.FileManager;

/**
 * Utility for checking OWL validation results.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.2 $
 */

public class OWLConsistencyTest extends TestCase {

    /** The base directory for finding the datafiles */
    public static final String BASE_DIR = "file:testing/reasoners/owl/";
    
    /** The tbox to be tested, relative to BASE_DIR */
    protected String tbox;
    
    /** The abox to be tested, relative to BASE_DIR */
    protected String abox;
        
    /** The expected result to check against */
    protected int expected;
    
    /** The factory for the reasoner to test */
    protected ReasonerFactory rf;
    
    /** Flag for expected result = inconsistent */
    public static final int INCONSISTENT = 1;
    
    /** Flag for expected result = consistent but at least 1 warning */
    public static final int WARNINGS = 2;
    
    /** Flag for expected result = no errors ow warnings */
    public static final int CLEAN = 3;
    
    /** Optional culprit object, should validate with equals */
    protected Object culprit;
    
    /**
     * Constructor - builds a dummy test which can't be run without setting a reasoner factory 
     * @param tbox The tbox to be tested, relative to BASE_DIR
     * @param abox The abox to be tested, relative to BASE_DIR
     * @param expected The expected result to check against - INCONSISTENT/WARNINGS/CLEAN
     * @param culprit Optional culprit object, should validate with equals, set to null for no test
     */
    public OWLConsistencyTest(String tbox, String abox, int expected, Object culprit) {
        super(abox);
        this.tbox = tbox;
        this.abox = abox;
        this.expected = expected;
        this.culprit = culprit;
    }

    
    /**
     * Constructor builds a runnable test from a dummy test.
     */
    public OWLConsistencyTest(OWLConsistencyTest base, String reasonerName, ReasonerFactory rf) {
        super(reasonerName + ":" + base.abox);
        this.tbox = base.tbox;
        this.abox = base.abox;
        this.expected = base.expected;
        this.culprit = base.culprit;
        this.rf = rf;
    }
    
    /** 
     * Define the reasoner to use for the tests.
     */
    public void setReasonerFactory(ReasonerFactory rf) {
        this.rf = rf;
    }

    /**
     * Run the consistency check, returning a ValidityReport.
     * @param rf The factory for the reasoner to test
     */
    public ValidityReport testResults() {
        Model t = FileManager.get().loadModel(BASE_DIR + tbox);
        Model a = FileManager.get().loadModel(BASE_DIR + abox);
        // Work around non-deterministic bug in bindSchema
//        Reasoner r = rf.create(null).bindSchema(t);
        Reasoner r = rf.create(null);
        a.add(t);
        InfModel im = ModelFactory.createInfModel(r, a);
        return im.validate();
    }
    
    /** 
     * Run the consistency check and validate the result against expectations.
     * @param rf The factory for the reasoner to test
     */
    public void runTest() {
        ValidityReport report = testResults();
        switch (expected) {
        case INCONSISTENT:
            assertTrue( "expected inconsistent", ! report.isValid() );
            break;
        case WARNINGS:
            assertTrue( "expected warnings", report.isValid() && !report.isClean() );
            break;
        case CLEAN:
            assertTrue( "expected clean", report.isClean() );
        }
        if (culprit != null) {
            boolean foundit = false;
            for (Iterator i = report.getReports(); i.hasNext(); ) {
                ValidityReport.Report r = (ValidityReport.Report)i.next();
                if (r.getExtension() != null && r.getExtension().equals(culprit)) {
                    foundit = true;
                    break;
                }
            }
            if (!foundit) {
                assertTrue("Expcted to find a culprint " + culprit, false);
            }
        }
    }
    
}



/*
    (c) Copyright 2005 Hewlett-Packard Development Company, LP
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
