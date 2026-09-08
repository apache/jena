/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.test;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Jena core test suite. Any JUnit3 remaining.
 */
public class JenaCoreTestAll_JU3 extends TestCase {

    static public TestSuite suite() {
        JenaTestLib.setup();

        TestSuite ts = new TestSuite();
        ts.setName("Jena Core [legacy]");
        addTest(ts,  "XML Input [ARP1]",    org.apache.jena.rdfxml.arp1tests.TS3_rdfxml_arp.suite());
        return ts;
    }

    private static void addTest(TestSuite ts, String name, TestSuite tc) {
        if ( name != null )
            tc.setName(name);
        ts.addTest(tc);
    }

//    // JUnit4 in a JUnit3 test runner.
//    private static Test adaptJUnit4(Class<?> testClass) {
//        return new JUnit4TestAdapter(testClass);
//    }
//
//    private static void addTest(TestSuite ts, String name, Test test) {
//        // Adds an extra level but does name the test suite.
//        TestSuite ts2 = new TestSuite(name);
//        ts2.addTest(test);
//        ts.addTest(ts2);
//    }
}
