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

package com.hp.hpl.jena.rdf.model.test;

import junit.framework.*;

/**
    Collected test suite for the .graph package.
    @author  jjc + kers
*/

public class TestPackage extends TestSuite {

    static public TestSuite suite() {
        return new TestPackage();
    }
    
    /** Creates new TestPackage */
    private TestPackage() {
        super("Model");
        addTest( "TestModel", TestModelFactory.suite() );
        addTest( "TestModelFactory", TestModelFactory.suite() );
        addTest( "TestSimpleListStatements", TestSimpleListStatements.suite() );
        addTest( "TestModelPolymorphism", TestModelPolymorphism.suite() );
        addTest( "TestSimpleSelector", TestSimpleSelector.suite() );
        addTest( "TestStatements", TestStatements.suite() );
        addTest( "TestRDFNodes", TestRDFNodes.suite() );
        addTest( "TestReifiedStatements", TestReifiedStatements.suite() );
        addTest( "TestIterators", TestIterators.suite() );
        addTest( "TestContains", TestContains.suite() );
        addTest( "TestLiteralImpl", TestLiteralImpl.suite() );
        addTest( "TestResourceImpl", TestResourceImpl.suite() );
        addTest( "TestHiddenStatements", TestHiddenStatements.suite() );
        addTest( "TestNamespace", TestNamespace.suite() );
        addTest( "TestModelBulkUpdate", TestModelBulkUpdate.suite() );
        addTest( "TestConcurrency", TestConcurrency.suite() ) ;
        addTest( "TestModelMakerImpl", TestModelMakerImpl.suite() );
        addTest( "TestModelPrefixMapping", TestModelPrefixMapping.suite() );
        addTest( TestContainers.suite() );
        addTest( "TestStandardModels", TestStandardModels.suite() );
        addTest( "TestQuery", TestQuery.suite() );
        addTest( "TestSelectors", TestSelectors.suite() );
        addTest( "TestModelEvents", TestModelEvents.suite() );
        addTest( "TestReaderEvents", TestReaderEvents.suite() );
        addTest( "TestList", TestList.suite() );
        addTest( "TestAnonID", TestAnonID.suite() );
        addTestSuite( TestLiteralsInModel.class );
        addTest( TestRemoveSPO.suite() );
        addTest( TestListSubjectsEtc.suite() );
        addTest( TestModelExtract.suite() );
        addTest( TestModelRead.suite() );
        addTestSuite( TestPropertyImpl.class );
        addTestSuite( TestRemoveBug.class );
        }

    private void addTest(String name, TestSuite tc) {
        tc.setName(name);
        addTest(tc);
    }

}
