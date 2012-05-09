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

package com.hp.hpl.jena.mem.test;

import junit.framework.*;

/**
 *
 * @author  bwm
 * @version $Name: not supported by cvs2svn $ $Revision: 1.1 $ $Date: 2009-06-29 08:55:51 $
 */
public class TestSuiteRegression extends Object {

	static public final String testNames[] = {
  "test1",
  "test2",
  "test3",
  "test4",
  "test5",
  "test6",
  "test7",
  "test8",
  "test9",
  "test10",
  "test11",
  "test12",
  "test13",
  "test14",
  "test15",
  "test16",
  "test17",
  "test18",
  "test19",
  "test97",
  "testMatch",
  "testNTripleReader",
  "testReaderInterface",

		
	};
    public static TestSuite suite() {
        return suite(new TestSuite());
    }

    public static TestSuite suite(TestSuite suite) {
    	for (int i=0;i<testNames.length;i++)
        suite.addTest(new TestCaseBasic(testNames[i]));

        return suite;
    }
}
