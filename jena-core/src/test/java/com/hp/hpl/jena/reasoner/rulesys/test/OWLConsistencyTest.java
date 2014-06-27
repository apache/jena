/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.reasoner.rulesys.test;

import java.util.Iterator;

import junit.framework.TestCase;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ReasonerFactory;
import com.hp.hpl.jena.reasoner.ValidityReport;
import com.hp.hpl.jena.reasoner.ValidityReport.Report;

import com.hp.hpl.jena.util.FileManager;

/**
 * Utility for checking OWL validation results.
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
     * Constructor - builds a dummy test which can't be run without setting a
     * reasoner factory
     * 
     * @param tbox
     *            The tbox to be tested, relative to BASE_DIR
     * @param abox
     *            The abox to be tested, relative to BASE_DIR
     * @param expected
     *            The expected result to check against -
     *            INCONSISTENT/WARNINGS/CLEAN
     * @param culprit
     *            Optional culprit object, should validate with equals, set to
     *            null for no test
     */
    public OWLConsistencyTest(String tbox, String abox, int expected,
            Object culprit) {
        super(abox);
        this.tbox = tbox;
        this.abox = abox;
        this.expected = expected;
        this.culprit = culprit;
    }

    /**
     * Constructor builds a runnable test from a dummy test.
     */
    public OWLConsistencyTest(OWLConsistencyTest base, String reasonerName,
            ReasonerFactory rf) {
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

    @Override
    public void runTest() {
        ValidityReport report = testResults();
        switch (expected) {
        case INCONSISTENT:
            assertTrue("expected inconsistent", !report.isValid());
            break;
        case WARNINGS:
            assertTrue("expected just warnings but reports not valid", report
                    .isValid());
            assertFalse("expected warnings but reports clean", report.isClean());
            break;
        case CLEAN:
            assertTrue("expected clean", report.isClean());
        }
        if (culprit != null) {
            boolean foundit = false;
            for (Iterator<Report> i = report.getReports(); i.hasNext();) {
                ValidityReport.Report r = i.next();
                if (r.getExtension() != null
                        && r.getExtension().equals(culprit)) {
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
