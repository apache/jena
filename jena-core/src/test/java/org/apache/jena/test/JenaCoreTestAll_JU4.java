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

package org.apache.jena.test;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Jena core test suite.
 * Tests using JUnit 4, and JUnit3 wrapped as JUnit4.
 */
public class JenaCoreTestAll_JU4 extends TestCase {

    static public TestSuite suite() {
        JenaTestLib.setup();

        TestSuite ts = new TestSuite();
        ts.setName("Jena Core [legacy]");

//JU6        addTest(ts,  "IRIx",                adaptJUnit4(org.apache.jena.irix.TS_IRIx.class));
//JU6        addTest(ts,  "LangTagX",            adaptJUnit4(org.apache.jena.langtagx.TS4_LangTagX.class));
//JU6        addTest(ts,  "Datatypes",           adaptJUnit4(org.apache.jena.datatypes.TS4_dt.class));

        // ** COMPLEX
        // Generates tests.
        addTest(ts,  "Enhanced",            org.apache.jena.enhanced.TS3_enh.suite());
        addTest(ts,  "Graph",               adaptJUnit4(org.apache.jena.graph.TS3_graph.class));

//JU6        addTest(ts,  "Mem",                 adaptJUnit4(org.apache.jena.mem.TS4_GraphMem.class));
//JU6        addTest(ts,  "MemValue",            adaptJUnit4(org.apache.jena.memvalue.TS3_GraphMemValue.class));

        // ** COMPLEX
        addTest(ts,  "Model1",              org.apache.jena.rdf.model.TS3_Model1.suite());
        // ** COMPLEX
        addTest(ts,  "Default Model",       org.apache.jena.rdf.model.TestDefaultModel.suite());

        // Test suite building
        addTest(ts,  "XML Input [ARP1]",    org.apache.jena.rdfxml.arp1tests.TS3_xmlinput1.suite());
//JU6        addTest(ts,  "XML Output",          org.apache.jena.rdfxml.xmloutput.TS3_xmloutput.suite());

//JU6        addTest(ts,  "Util",                adaptJUnit4(org.apache.jena.util.TS4_coreutil.class));
//JU6        addTest(ts,  "Jena iterator",       adaptJUnit4(org.apache.jena.util.iterator.test.TS3_coreiter.class));

        addTest(ts,  "Assembler",           adaptJUnit4(org.apache.jena.assembler.TS3_Assembler.class));

//JU6        addTest(ts,  "Vocabularies",        adaptJUnit4(org.apache.jena.vocabulary.TS3_Vocabularies.class));
//JU6        addTest(ts,  "Shared",              adaptJUnit4(org.apache.jena.shared.TS_SharedPackage.class));

        // ** COMPLEX
        addTest(ts,  "Composed graphs",     org.apache.jena.graph.compose.TS3_compose.suite() );

        addTest(ts,  "Reasoners",           adaptJUnit4(org.apache.jena.reasoner.test.TS3_reasoners.class));
        addTest(ts,  "RuleReasoners",       adaptJUnit4(org.apache.jena.reasoner.rulesys.TS3_RuleReasoners.class));

        addTest(ts,  "Ontology ModelMaker", adaptJUnit4(org.apache.jena.ontology.makers.TS3_ModelMakers.class));
        addTest(ts,  "Ontology",            adaptJUnit4(org.apache.jena.ontology.impl.TS3_ont.class));

        // Local TTL parser for tests - not fully compliant.
        addTest(ts,  "Turtle",              adaptJUnit4(org.apache.jena.ttl_test.test.turtle.TS_TestTurtle.class));
        // ** Generated tests
        addTest(ts,  "Turtle:Manifest",     org.apache.jena.ttl_test.test.turtle.TurtleTestSuiteManifest.suite());
        return ts;
    }

    // JUnit4 in a JUnit3 test runner.
    private static Test adaptJUnit4(Class<?> testClass) {
        return new JUnit4TestAdapter(testClass);
    }

    private static void addTest(TestSuite ts, String name, TestSuite tc) {
        if ( name != null )
            tc.setName(name);
        ts.addTest(tc);
    }

    private static void addTest(TestSuite ts, String name, Test test) {
        // Extra level but does name the test suite.
        TestSuite ts2 = new TestSuite(name);
        ts2.addTest(test);
        ts.addTest(ts2);
    }
}
