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

package org.apache.jena.riot.lang.extra;

import junit.framework.TestSuite;
import org.apache.jena.sys.JenaSystem;
import org.junit.runner.RunWith;
import org.junit.runners.AllTests;

/** Tests for "lang.extra" such as the JavaCC version of the Turtle parser */

@RunWith(AllTests.class)
public class TS_LangExtra
{
    private static final String manifest1 = "testing/RIOT/Lang/TurtleStd/manifest.ttl";
    private static final String manifest2 = "testing/RIOT/Lang/Turtle2/manifest.ttl";
    private static final String manifest3 = "testing/RIOT/Lang/TurtleSubm/manifest.ttl";

    static public TestSuite suite()
    {
        JenaSystem.init();
        TurtleJavaccReaderRIOT.register();

        TestSuite ts = new TestSuite(TS_LangExtra.class.getName());
        ts.addTest(FactoryTestTurtleJavacc.make(manifest1, null, null));
        ts.addTest(FactoryTestTurtleJavacc.make(manifest2, null, null));
        ts.addTest(FactoryTestTurtleJavacc.make(manifest3, null, null));
        return ts;
    }
}
