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

package com.hp.hpl.jena.graph.query.test;

import com.hp.hpl.jena.graph.query.regexptrees.test.TestPerlyParser;
import com.hp.hpl.jena.graph.query.regexptrees.test.TestRegexpTrees;

import junit.framework.*;

/**
 	@author kers
*/
public class TestGraphQueryPackage extends TestCase
    {
    public TestGraphQueryPackage()
        {}
        
    public static TestSuite suite()
        {
        TestSuite result = new TestSuite();
        result.addTest( TestBufferPipe.suite() );
        result.addTest( TestMatchers.suite() );
        result.addTest( TestQueryNode.suite() );
        result.addTest( TestQueryNodeFactory.suite() );
        result.addTest( TestQueryTriple.suite() );
        result.addTest( TestStageElements.suite() );
        result.addTest( QueryTest.suite() );
        result.addTest( TestQueryReification.suite() );
        result.addTest( TestSimpleTripleSorter.suite() );
        result.addTest( TestExpressions.suite() );
        result.addTest( TestExpressionConstraints.suite() );
        result.addTest( TestEarlyConstraints.suite() );
        result.addTest( TestPerlyParser.suite() );
        result.addTest( TestRegexpTrees.suite() );
        result.addTest( TestDomain.suite() );
        result.setName(TestGraphQueryPackage.class.getSimpleName());
        return result;
        }
    }
