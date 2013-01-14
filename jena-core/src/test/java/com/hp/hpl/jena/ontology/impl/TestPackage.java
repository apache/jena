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

package com.hp.hpl.jena.ontology.impl;


import junit.framework.*;

/**
    Collected test suite for the .ontology.impl package.
*/

public class TestPackage extends TestSuite {

    static public TestSuite suite() {
        return new TestPackage();
    }

    /** Creates new TestPackage */
    private TestPackage() {
        super("ontology.impl");
        addTest( "TestOntGraph", TestOntGraph.suite() );
        addTest( "TestResource", TestResource.suite() );
        addTest( "TestAxioms", TestAxioms.suite() );
        addTest( "TestClassExpression", TestClassExpression.suite() );
        addTest( "TestOntDocumentManager", TestOntDocumentManager.suite() );
        addTest( "TestOntology", TestOntology.suite() );
        addTest( "TestProperty", TestProperty.suite() );
        addTest( "TestListSyntaxCategories", TestListSyntaxCategories.suite() );
        addTest( "TestCreate", TestCreate.suite() );
        addTest( "TestIndividual", TestIndividual.suite() );
        addTest( "TestAllDifferent", TestAllDifferent.suite() );
        addTestSuite( TestOntModelSpec.class );
        addTest( new TestSuite( TestOntReasoning.class  ) );
        addTest( new TestSuite( TestOntModel.class ) );
        addTest( new TestSuite( TestOntClass.class ));
        addTest( new TestSuite( TestFrameView.class ));
        addTest( new TestSuite( TestOntTools.class ));
    }

    private void addTest(String name, TestSuite tc) {
        tc.setName(name);
        addTest(tc);
    }

}
