/*
 * Copyright 2014 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.reasoner.sparqlinrules.test;


import com.hp.hpl.jena.reasoner.test.TestInfGraph;
import com.hp.hpl.jena.reasoner.test.TestInfModel;
import com.hp.hpl.jena.reasoner.test.TestInfPrefixMapping;
import com.hp.hpl.jena.reasoner.test.TestRDFSReasoners;
import com.hp.hpl.jena.reasoner.test.TestReasoners;
import com.hp.hpl.jena.reasoner.test.TestSafeModel;
import com.hp.hpl.jena.reasoner.test.TestTransitiveGraphCache;
import junit.framework.*;

public class TestPackage extends TestSuite {

    static public TestSuite suite() {
        return new TestPackage();
    }
    
    /** Creates new TestPackage */
    private TestPackage() {
        super("SparqlInRules");
        addTest( "SparqlinRulesTest", SparqlinRulesTest.suite() );
        addTest( "SparqlinRulesTest1", SparqlinRulesTest1.suite() );
    }

    // helper method
    private void addTest(String name, TestSuite tc) {
        tc.setName(name);
        addTest(tc);
    }

}