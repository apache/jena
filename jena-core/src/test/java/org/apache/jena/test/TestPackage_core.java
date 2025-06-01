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

import junit.framework.*;
import org.apache.jena.rdf.model.impl.RDFReaderFImpl;
import org.apache.jena.sys.JenaSystem;

/**
 * Jena core test suite.
 */
public class TestPackage_core extends TestCase {

    static public TestSuite suite() {
        JenaSystem.init();
        // Include old Turtle parser - not up-to-date but enouhg to read test input files
        RDFReaderFImpl.alternative(new X_RDFReaderF());

        TestSuite ts = new TestSuite() ;
        ts.setName("Jena Core") ;

        addTest(ts,  "System setup",       adaptJUnit4(TestSystemSetup.class));

        addTest(ts,  "IRIx",                adaptJUnit4(org.apache.jena.irix.TS_IRIx.class));
        addTest(ts,  "LangTagX",            adaptJUnit4(org.apache.jena.langtagx.TS_LangTagX.class));
        addTest(ts,  "Datatypes",           adaptJUnit4(org.apache.jena.datatypes.TS3_dt.class));
        // ** COMPLEX
        addTest(ts,  "Enhanced",            org.apache.jena.enhanced.test.TS3_enh.suite());
        addTest(ts,  "Graph",               adaptJUnit4(org.apache.jena.graph.test.TS3_graph.class));
        // mem2
        addTest(ts,  "Mem",                 adaptJUnit4(org.apache.jena.mem2.TS4_GraphMem2.class));

        // Old GraphMem/GraphMemValue.
        // TO CHECK
        addTest(ts,  "MemValue",            adaptJUnit4(org.apache.jena.mem.test.TS3_GraphMemValue.class));

        // ** COMPLEX (rename class and create TS3_Model for these two)
        addTest(ts,  "Model",               org.apache.jena.rdf.model.test.TS3_Model1.suite());
        // ** COMPLEX
        addTest(ts,  "StandardModels",      org.apache.jena.rdf.model.test.TS3_StandardModels.suite() );

        addTest(ts,  "XML Input [ARP/IRIx]",  org.apache.jena.rdfxml.xmlinput1.TS3_xmlinput1.suite());
        addTest(ts,  "XML Output",          org.apache.jena.rdfxml.xmloutput.TS3_xmloutput.suite());

        addTest(ts,  "Util",                adaptJUnit4(org.apache.jena.util.TS3_coreutil.class));
        addTest(ts,  "Jena iterator",       adaptJUnit4(org.apache.jena.util.iterator.test.TS3_coreiter.class));
        addTest(ts,  "Assembler",           adaptJUnit4(org.apache.jena.assembler.test.TS3_Assembler.class));
        addTest(ts,  "Vocabularies",        adaptJUnit4(org.apache.jena.vocabulary.test.TS_Vocabularies.class));
        addTest(ts,  "Shared",              adaptJUnit4(org.apache.jena.shared.TestSharedPackage.class));
        addTest(ts,  "ModelUtils",          adaptJUnit4(org.apache.jena.test.TestModelUtil.class));

        // ** COMPLEX
        addTest(ts,  "Composed graphs",     org.apache.jena.graph.compose.test.TS3_compose.suite() );

        addTest(ts,  "Reasoners",           adaptJUnit4(org.apache.jena.reasoner.test.TS3_reasoners.class));
        addTest(ts,  "RuleReasoners",       adaptJUnit4(org.apache.jena.reasoner.rulesys.TS3_RuleReasoners.class));

        addTest(ts,  "Ontology ModelMaker", adaptJUnit4(org.apache.jena.ontology.makers.TS3_ModelMakers.class));
        addTest(ts,  "Ontology",            adaptJUnit4(org.apache.jena.ontology.impl.TS3_ont.class));

        // Local TTL parser for tests - not fully compliant.
        addTest(ts,  "Turtle",              adaptJUnit4(org.apache.jena.ttl_test.test.turtle.TS_TestTurtle.class));
        // ** Generated tests
        addTest(ts,  "Turtle:Manifest",     org.apache.jena.ttl_test.test.turtle.TurtleTestSuiteManifest.suite()) ;

        return ts ;
    }

    /* ORIGINAL
    static public TestSuite suite() {
        JenaSystem.init();
        // Reads Turtle (old parser, not up-to-date but we need something for testing.)
        RDFReaderFImpl.alternative(new X_RDFReaderF());

        TestSuite ts = new TestSuite() ;
        ts.setName("Jena") ;

        addTest(ts,  "System setup", TestSystemSetup.suite());
        addTest(ts,  "IRIx", org.apache.jena.irix.TS_IRIx.suite());
        addTest(ts,  "LangTagX", org.apache.jena.langtagx.TS_LangTagX.suite());
        addTest(ts,  "Enhanced", org.apache.jena.enhanced.test.TestPackage_enh.suite());
        addTest(ts,  "Datatypes", org.apache.jena.datatypes.TestPackage_dt.suite());
        addTest(ts,  "Graph", org.apache.jena.graph.test.TestPackage_graph.suite());
        addTest(ts,  "Mem", org.apache.jena.mem.test.TestMemPackage.suite() );
        addTest(ts,  "Mem2", org.apache.jena.mem.test.TestGraphMemPackage.suite() );
        addTest(ts,  "Model", org.apache.jena.rdf.model.test.TestPackage_model.suite());
        addTest(ts,  "StandardModels", org.apache.jena.rdf.model.test.TestStandardModels.suite() );

        addTest(ts,  "XML Input [ARP/IRIx]", org.apache.jena.rdfxml.xmlinput1.TestPackage_xmlinput1.suite());
        addTest(ts,  "XML Output", org.apache.jena.rdfxml.xmloutput.TestPackage_xmloutput.suite());
        addTest(ts,  "Util", org.apache.jena.util.TestPackage_util.suite());
        addTest(ts,  "Jena iterator", org.apache.jena.util.iterator.test.TestPackage_iter.suite() );
        addTest(ts,  "Assembler", org.apache.jena.assembler.test.TestAssemblerPackage.suite() );
        addTest(ts,  "Vocabularies", org.apache.jena.vocabulary.test.TestVocabularies.suite() );
        addTest(ts,  "Shared", org.apache.jena.shared.TestSharedPackage.suite() );
        addTest(ts,  "Composed graphs", org.apache.jena.graph.compose.test.TestPackage_compose.suite() );
        addTest(ts,  "Reasoners", org.apache.jena.reasoner.test.TestPackage_reasoners.suite());
        addTest(ts,  "Ontology ModelMaker", org.apache.jena.ontology.makers.TestPackage_ModelMakers.suite() );
        addTest(ts,  "Ontology", org.apache.jena.ontology.impl.TestPackage_ont.suite() );

        // Local TTL parser for tests - not fully compliant.
        addTest(ts,  "Turtle", org.apache.jena.ttl_test.test.turtle.TurtleTestSuite.suite()) ;
        addTest(ts,  "ModelUtils", org.apache.jena.test.TestModelUtil.suite());

        return ts ;
    }
*/
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
