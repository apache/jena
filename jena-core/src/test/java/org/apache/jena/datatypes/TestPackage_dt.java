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

package org.apache.jena.datatypes;

import junit.framework.JUnit4TestAdapter ;
import junit.framework.TestSuite ;

/**
    Collected test suite for the .datatype package.
    (many other tests are elsewhere)
*/

public class TestPackage_dt extends TestSuite {

    static public TestSuite suite() {
        return new TestPackage_dt();
    }
    
    /** Creates new TestPackage */
    private TestPackage_dt() {
        super("datatypes");
        addTest(new JUnit4TestAdapter(TestDatatypes.class)) ;
        addTest(new JUnit4TestAdapter(TestDatatypeValues.class)) ;
    }
}
