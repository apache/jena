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

package com.hp.hpl.jena.graph.test;

import junit.framework.TestSuite ;

/**
    Collected test suite for the .graph package.
*/

public class TestPackage extends TestSuite {

    static public TestSuite suite() {
        return new TestPackage();
    }
    
    /** Creates new TestPackage */
    private TestPackage() {
        super("graph");
        addTest( TestFindLiterals.suite() );
        addTest( TestLiteralLabels.suite() );
        addTest( TestNode.suite() );
        addTest( TestTriple.suite() );
        addTest( TestTripleField.suite() );
        addTest( TestNodeToTriplesMap.suite() );
        addTest( TestReifier.suite() );   
        addTest( TestTypedLiterals.suite() );
        addTest( TestDateTime.suite() );
        addTest( TestFactory.suite() );
        addTest( TestGraph.suite() );
        addTest( TestSimpleGraphMaker.suite() );
        addTest( TestFileGraph.suite() );
        addTest( TestFileGraphMaker.suite() );
        addTest( TestGraphExtract.suite() );
        addTest( TestCapabilities.suite() );
        addTest( TestGraphUtils.suite() );
        addTest( TestGraphPrefixMapping.suite() );       
        addTest( TestGraphMatchWithInference.suite());
        addTestSuite( TestGraphEvents.class );
        addTestSuite( TestGraphBaseToString.class );
    }
}
