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

package com.hp.hpl.jena.test;

import static jena.cmdline.CmdLineUtils.setLog4jConfiguration;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.hp.hpl.jena.assembler.test.TestAssemblerPackage;

/**
 * All developers should edit this file to add their tests.
 * Please try to name your tests and test suites appropriately.
 * Note, it is better to name your test suites on creation
 * rather than in this file.
 */
public class TestPackage extends TestCase{

    static {
    	setLog4jConfiguration(JenaTest.log4jFilenameTests) ;
    }
	
    static public TestSuite suite() {
        TestSuite ts = new TestSuite() ;
        ts.setName("Jena") ;
        addTest(ts,  "Enhanced", com.hp.hpl.jena.enhanced.test.TestPackage.suite());
        addTest(ts,  "Graph", com.hp.hpl.jena.graph.test.TestPackage.suite());
        addTest(ts,  "Mem", com.hp.hpl.jena.mem.test.TestMemPackage.suite() );
        addTest(ts,  "Mem2", com.hp.hpl.jena.mem.test.TestGraphMemPackage.suite() );
        addTest(ts,  "Model", com.hp.hpl.jena.rdf.model.test.TestPackage.suite());
        addTest(ts,  "N3", com.hp.hpl.jena.n3.N3TestSuite.suite());
        addTest(ts,  "Turtle", com.hp.hpl.jena.n3.turtle.TurtleTestSuite.suite()) ;
        addTest(ts,  "XML Output", com.hp.hpl.jena.rdfxml.xmloutput.TestPackage.suite());
        addTest(ts,  "Util", com.hp.hpl.jena.util.TestPackage.suite());
        addTest(ts,  "Jena iterator", com.hp.hpl.jena.util.iterator.test.TestPackage.suite() );
        addTest(ts,  "Assembler", TestAssemblerPackage.suite() );
        addTest(ts,  "ARP", com.hp.hpl.jena.rdfxml.xmlinput.TestPackage.suite());
        addTest(ts,  "Vocabularies", com.hp.hpl.jena.vocabulary.test.TestVocabularies.suite() );
        addTest(ts,  "Shared", com.hp.hpl.jena.shared.TestSharedPackage.suite() );
        addTest(ts,  "Reasoners", com.hp.hpl.jena.reasoner.test.TestPackage.suite());
        addTest(ts,  "Composed graphs", com.hp.hpl.jena.graph.compose.test.TestPackage.suite() );
        addTest(ts,  "Ontology", com.hp.hpl.jena.ontology.impl.TestPackage.suite() );
        addTest(ts,  "cmd line utils", jena.test.TestPackage.suite() );
        return ts ;
    }

    private static void addTest(TestSuite ts, String name, TestSuite tc) {
        if ( name != null )
            tc.setName(name);
        ts.addTest(tc);
    }

}
