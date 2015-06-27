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

package org.apache.jena.util;

import junit.framework.*;

/**
 * All developers should edit this file to add their tests.
 * Please try to name your tests and test suites appropriately.
 * Note, it is better to name your test suites on creation
 * rather than in this file.
 */
public class TestPackage extends TestSuite {

    static public TestSuite suite() {
        return new TestPackage();
    }
    
    /** Creates new TestPackage */
    private TestPackage() {
        super( "util" );
        addTest( "TestTokenzier",         TestTokenizer.suite());
        addTest( "TestFileUtils",         TestFileUtils.suite() );
        addTest( "TestHashUtils",         TestCollectionFactory.suite() );
        addTest( "TestLocationMapper",    TestLocationMapper.suite() ) ;
        addTest( "TestFileManager",       TestFileManager.suite()) ;
        addTest( "TestMonitors",          TestMonitors.suite()) ;
        addTest( "TestPrintUtil",         TestPrintUtil.suite()) ;
        addTest( TestIteratorCollection.suite() );
        addTest( "TestSplitIRI_XML",      TestSplitIRI_XML.suite()) ;
        addTest( "TestSplitIRI_TTL",      TestSplitIRI_TTL.suite()) ;
        addTestSuite( TestLocators.class );
        addTestSuite( TestOneToManyMap.class );
    }

    private void addTest(String name, TestSuite tc) {
        tc.setName(name);
        addTest(tc);
    }        
    private void addTest(String name, Test tc) {
        addTest(tc);
    }

}
