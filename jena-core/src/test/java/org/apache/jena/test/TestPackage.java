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

import junit.framework.TestCase ;
import junit.framework.TestSuite ;
import org.apache.jena.rdf.model.impl.RDFReaderFImpl;

/**
 * All developers should edit this file to add their tests.
 * Please try to name your tests and test suites appropriately.
 * Note, it is better to name your test suites on creation
 * rather than in this file.
 */
public class TestPackage extends TestCase {

    static public TestSuite suite() {
        // Reads Turtle (old parser, not up-to-date but we need something for testing.)
        RDFReaderFImpl.alternative(new X_RDFReaderF());

        TestSuite ts = new TestSuite() ;
        ts.setName("Jena") ;
        addTest(ts,  "System setup", TestSystemSetup.suite());
        addTest(ts,  "IRI", org.apache.jena.irix.TS_IRIx.suite());
        addTest(ts,  "Enhanced", org.apache.jena.enhanced.test.TestPackage.suite());
        addTest(ts,  "Datatypes", org.apache.jena.datatypes.TestPackage.suite()) ;
        addTest(ts,  "Graph", org.apache.jena.graph.test.TestPackage.suite());
        addTest(ts,  "Mem", org.apache.jena.mem.test.TestMemPackage.suite() );
        addTest(ts,  "Mem2", org.apache.jena.mem.test.TestGraphMemPackage.suite() );
        addTest(ts,  "Model", org.apache.jena.rdf.model.test.TestPackage.suite());
        addTest(ts,  "StandardModels", org.apache.jena.rdf.model.test.TestStandardModels.suite() );
        addTest(ts,  "Turtle", org.apache.jena.ttl_test.turtle.TurtleTestSuite.suite()) ;
        addTest(ts,  "XML Output", org.apache.jena.rdfxml.xmloutput.TestPackage_xmloutput.suite());
        addTest(ts,  "Util", org.apache.jena.util.TestPackage.suite());
        addTest(ts,  "Jena iterator", org.apache.jena.util.iterator.test.TestPackage.suite() );
        addTest(ts,  "Assembler", org.apache.jena.assembler.test.TestAssemblerPackage.suite() );
        addTest(ts,  "ARP", org.apache.jena.rdfxml.xmlinput.TestPackage_xmlinput.suite());
        addTest(ts,  "Vocabularies", org.apache.jena.vocabulary.test.TestVocabularies.suite() );
        addTest(ts,  "Shared", org.apache.jena.shared.TestSharedPackage.suite() );
        addTest(ts,  "Reasoners", org.apache.jena.reasoner.test.TestPackage.suite());
        addTest(ts,  "Composed graphs", org.apache.jena.graph.compose.test.TestPackage.suite() );
        addTest(ts,  "Ontology", org.apache.jena.ontology.impl.TestPackage.suite() );
        return ts ;
    }

    private static void addTest(TestSuite ts, String name, TestSuite tc) {
        if ( name != null )
            tc.setName(name);
        ts.addTest(tc);
    }



}
