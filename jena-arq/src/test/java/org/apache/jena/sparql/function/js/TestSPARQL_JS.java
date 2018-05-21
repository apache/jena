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

package org.apache.jena.sparql.function.js;

import junit.framework.TestSuite;
import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.junit.ScriptTestSuiteFactory;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sys.JenaSystem;
import org.junit.runner.RunWith;
import org.junit.runners.AllTests;

@RunWith(AllTests.class)
public class TestSPARQL_JS {
    static final String MANIFEST = "testing/ARQ/JS/manifest.ttl";
    static final String JS_LIB_FILE = "testing/ARQ/JS/test-library.js";
    
    private static void setupJS() {
        Context cxt = ARQ.getContext();
        cxt.set(ARQ.symJavaScriptLibFile, JS_LIB_FILE);
        cxt.set(ARQ.symJavaScriptFunctions, "function inc(x) { return x+1 }");
        EnvJavaScript.reset();
    }
    
    static public TestSuite suite() {
        JenaSystem.init();
        setupJS();
        TestSuite ts = new TestSuite(TestSPARQL_JS.class.getName());
        TestSuite ts2 = ScriptTestSuiteFactory.make(MANIFEST);
        ts.addTest(ts2);
        return ts;
    }
}
