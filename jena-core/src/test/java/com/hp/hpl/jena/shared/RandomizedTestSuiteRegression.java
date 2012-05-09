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

package com.hp.hpl.jena.shared;

import com.hp.hpl.jena.mem.test.TestSuiteRegression;

import junit.framework.*;

/**
 *
 * @author  bwm
 * @version $Name: not supported by cvs2svn $ $Revision: 1.1 $ $Date: 2009-06-29 18:42:05 $
 */
public class RandomizedTestSuiteRegression extends Object {

    public static TestSuite suite() {
    	TestSuite s = new TestSuite();
    	s.setName("Random order models");
        return suite(s);
    }

    public static TestSuite suite(TestSuite suite) {
    	for (int i=0;i<TestSuiteRegression.testNames.length;i++)
            suite.addTest(new RandomizedTestCaseBasic(TestSuiteRegression.testNames[i]));
     	
        return suite;
    }
}
