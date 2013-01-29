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

package com.hp.hpl.jena.reasoner.test;

import junit.framework.*;

/**
 * Aggregate tester that runs all the test associated with the reasoner package.
 */

public class TestPackage extends TestSuite {

    static public TestSuite suite() {
        return new TestPackage();
    }
    
    /** Creates new TestPackage */
    private TestPackage() {
        super("reasoners");
        addTest( "TestTransitiveGraphCache", TestTransitiveGraphCache.suite() );
        addTest( "TestReasoners", TestReasoners.suite() );
        addTest( "TestRDFSReasoners", TestRDFSReasoners.suite() );
        addTest( "TestRuleReasoners",  com.hp.hpl.jena.reasoner.rulesys.test.TestPackage.suite() );
        addTest( "TestReasonerPrefixMapping", TestInfPrefixMapping.suite() );
        addTest( "TestInfGraph", TestInfGraph.suite() );
        addTest( "TestInfModel", TestInfModel.suite() );
        addTest( "TestSafeModel", TestSafeModel.suite() );
    }

    // helper method
    private void addTest(String name, TestSuite tc) {
        tc.setName(name);
        addTest(tc);
    }

}
