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

/*
 * MegaTestSuite.java
 *
 * Created on September 18, 2001, 7:41 PM
 */

package com.hp.hpl.jena.regression;

import com.hp.hpl.jena.mem.test.TestSuiteRegression;

import junit.framework.TestSuite;

/**
 * All developers should edit this file to add their tests.
 * Please try to name your tests and test suites appropriately.
 * Note, it is better to name your test suites on creation
 * rather than in this file.
 * @author  jjc
 */
public class MegaTestSuite extends TestSuite {

    /** Creates new MegaTestSuite */
    static public TestSuite suite() {
        return new MegaTestSuite();
    }
    private MegaTestSuite() {
        super( "Jena");
        addTest( TestSuiteRegression.suite() );
        addTest( NewRegression.suite() );
    }
    }
