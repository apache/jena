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

package org.openjena.riot.lang;


import junit.framework.TestSuite ;
import org.junit.runner.RunWith ;
import org.junit.runners.AllTests ;
import org.openjena.riot.TestVocabRIOT ;

@RunWith(AllTests.class)
public class TestSuiteTrig extends TestSuite
{
    private static final String manifest1 = "testing/RIOT/TriG/manifest.ttl" ;
    private static final String manifest2 = "testing/RIOT/TriG/manifest-bad.ttl" ;
    
    static public TestSuite suite()
    {
        TestSuite ts = new TestSuite("TriG") ;
        // The good ..
        ts.addTest(FactoryTestRiotTriG.make(manifest1, TestVocabRIOT.TestInOut, "TriG-")) ;
        // .. the bad ...
        ts.addTest(FactoryTestRiotTriG.make(manifest2, TestVocabRIOT.TestBadSyntax, "TriG-")) ;
        return ts ;
    }
}
