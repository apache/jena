/**
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

package org.apache.jena.riot;

import junit.framework.TestSuite ;
import org.apache.jena.riot.langsuite.FactoryTestRiot ;
import org.junit.runner.RunWith ;
import org.junit.runners.AllTests ;

/** The test suites - these are driven by a manifest file and use external files for tests */

@RunWith(AllTests.class)
public class TS_LangSuite extends TestSuite
{
    private static final String manifest1 = "testing/RIOT/Lang/manifest-all.ttl" ;

    static public TestSuite suite()
    {
        RIOT.init() ;
        TestSuite ts = new TestSuite("RIOT Lang") ;
        ts.addTest(FactoryTestRiot.make(manifest1, null, null)) ;
        return ts ;
    }
}
