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

package org.apache.jena.test;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.jena.rdf.model.impl.RDFReaderFImpl;
import org.apache.jena.rdf.model.impl.RDFWriterFImpl;
import org.apache.jena.sys.JenaSystem;

/**
 * Jena core test suite.
 */
public class JenaCoreTestAll extends TestCase {

    static public TestSuite suite() {
        JenaSystem.init();
        // Include parsers and writers needed for the tests.
        // These are not up-to-date but enough to work with the test suite.
        RDFReaderFImpl.alternative(new X_RDFReaderF());
        RDFWriterFImpl.alternative(new X_RDFWriterF());

        TestSuite ts = new TestSuite();
        ts.setName("Jena Core");

        addTest(ts,  "System setup",        adaptJUnit4(TestSystemSetup.class));

        addTest(ts,  "IRIx",                adaptJUnit4(org.apache.jena.irix.TS_IRIx.class));
        addTest(ts,  "LangTagX",            adaptJUnit4(org.apache.jena.langtagx.TS_LangTagX.class));
        addTest(ts,  "Datatypes",           adaptJUnit4(org.apache.jena.datatypes.TS3_dt.class));

        // ** COMPLEX
        // Generates tests.
        addTest(ts,  "Enhanced",            org.apache.jena.enhanced.test.TS3_enh.suite());
        addTest(ts,  "Graph",               adaptJUnit4(org.apache.jena.graph.test.TS3_graph.class));
        // mem2
        addTest(ts,  "Mem",                 adaptJUnit4(org.apache.jena.mem.TS4_GraphMem.class));

        // Old GraphMem/GraphMemValue.
        // TO CHECK
        addTest(ts,  "MemValue",            adaptJUnit4(org.apache.jena.memvalue.TS3_GraphMemValue.class));

        // ** COMPLEX
        addTest(ts,  "Model1",              org.apache.jena.rdf.model.test.TS3_Model1.suite());
        // ** COMPLEX
        addTest(ts,  "Model2",              org.apache.jena.rdf.model.test.TS3_StandardModels.suite() );

        // Test suite building
        addTest(ts,  "XML Input [ARP1]",    org.apache.jena.rdfxml.arp1tests.TS3_xmlinput1.suite());
        addTest(ts,  "XML Output",          org.apache.jena.rdfxml.xmloutput.TS3_xmloutput.suite());

        addTest(ts,  "Util",                adaptJUnit4(org.apache.jena.util.TS3_coreutil.class));
        addTest(ts,  "Jena iterator",       adaptJUnit4(org.apache.jena.util.iterator.test.TS3_coreiter.class));
        addTest(ts,  "Assembler",           adaptJUnit4(org.apache.jena.assembler.test.TS3_Assembler.class));
        addTest(ts,  "Vocabularies",        adaptJUnit4(org.apache.jena.vocabulary.test.TS_Vocabularies.class));
        addTest(ts,  "Shared",              adaptJUnit4(org.apache.jena.shared.TS_SharedPackage.class));

        // ** COMPLEX
        addTest(ts,  "Composed graphs",     org.apache.jena.graph.compose.test.TS3_compose.suite() );

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
