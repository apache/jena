/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */


package org.apache.jena.reasoner.rulesys.test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Iterator;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerFactory;
import org.apache.jena.reasoner.rulesys.RDFSFBRuleReasonerFactory;
import org.apache.jena.reasoner.rulesys.RDFSRuleReasonerFactory;
import org.apache.jena.reasoner.test.ReasonerTester;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

/** Test suite to test the production rule version of the RDFS implementation.
 * <p> The tests themselves have been replaced by an updated version
 * of the top level TestRDFSReasoners but this file is maintained for now since
 * the top level timing test can sometimes be useful. </p>
 */
public class TestRDFSRules {
    /** Base URI for the test names */
    public static final String NAMESPACE = "http://www.hpl.hp.com/semweb/2003/query_tester/";

    protected static Logger logger = LoggerFactory.getLogger(TestRDFSRules.class);

    /**
     * Test a single RDFS case.
     * Not run as part of the suite - the name does not start "test".
     */
    public void hiddenTestRDFSReasonerDebug() throws IOException {
        ReasonerTester tester = new ReasonerTester("rdfs/manifest-nodirect-noresource.rdf");
        ReasonerFactory rf = RDFSRuleReasonerFactory.theInstance();

        assertTrue(tester.runTest("http://www.hpl.hp.com/semweb/2003/query_tester/rdfs/test11", rf, this, null),
                   "RDFS hybrid-tgc reasoner test");
    }

    /**
     * Test the basic functioning of the hybrid RDFS rule reasoner
     */
    @Test
    public void testRDFSFBReasoner() throws IOException {
        ReasonerTester tester = new ReasonerTester("rdfs/manifest-nodirect-noresource.rdf");
        ReasonerFactory rf = RDFSFBRuleReasonerFactory.theInstance();
        assertTrue(tester.runTests(rf, this, null), "RDFS hybrid reasoner tests");
    }

    /**
     * Test the basic functioning of the hybrid RDFS rule reasoner with TGC cache
     */
    @Test
    public void testRDFSExptReasoner() throws IOException {
        ReasonerTester tester = new ReasonerTester("rdfs/manifest-nodirect-noresource.rdf");
        ReasonerFactory rf = RDFSRuleReasonerFactory.theInstance();
        assertTrue(tester.runTests(rf, this, null), "RDFS experimental (hybrid+tgc) reasoner tests");
    }

    /**
     * Test the capabilities description.
     */
    @Test
    public void testRDFSDescription() {
        ReasonerFactory rf = RDFSFBRuleReasonerFactory.theInstance();
        Reasoner r = rf.create(null);
        assertTrue(r.supportsProperty(RDFS.subClassOf));
        assertTrue(r.supportsProperty(RDFS.domain));
        assertFalse(r.supportsProperty(OWL.allValuesFrom));
    }

    /**
     * Time a trial list of results from an inf graph.
     */
    private static void doTiming(Reasoner r, Model tbox, Model data, String name, int loop) {
        Resource C1 = ResourceFactory.createResource("http://www.hpl.hp.com/semweb/2003/eg#C1");

        long t1 = System.currentTimeMillis();
        int count = 0;
        for (int lp = 0; lp < loop; lp++) {
            Model m = ModelFactory.createModelForGraph(r.bindSchema(tbox.getGraph()).bind(data.getGraph()));
            count = 0;
            for (Iterator<Statement> i = m.listStatements(null, RDF.type, C1); i.hasNext(); i.next()) count++;
        }
        long t2 = System.currentTimeMillis();
        long time10 = (t2-t1)*10/loop;
        long time = time10/10;
        long timeFraction = time10 - (time*10);
        System.out.println(name + ": " + count +" results in " + time + "." + timeFraction +"ms");
    }
}
