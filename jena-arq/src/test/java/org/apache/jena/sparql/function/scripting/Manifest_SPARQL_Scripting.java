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

package org.apache.jena.sparql.function.scripting;

import java.util.stream.Stream;

import org.junit.jupiter.api.*;

import org.apache.jena.arq.junit.Scripts;
import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.util.Context;

public class Manifest_SPARQL_Scripting {
    static final String JS_LIB_FILE = "testing/ARQ/Scripting/test-library.js";

    @BeforeAll public static void enableScripting() {
        System.setProperty(ARQ.systemPropertyScripting, "true");
    }

    @AfterEach public void disbleScripting() {
        System.clearProperty(ARQ.systemPropertyScripting);
    }

    @BeforeAll
    public static void setupJS() {
        Context cxt = ARQ.getContext();
        cxt.set(ARQ.symJavaScriptLibFile, JS_LIB_FILE);
        cxt.set(ARQ.symJavaScriptFunctions, "function inc(x) { return x+1 }");
        String allowList = TestScriptFunction.testLibAllow+",inc";

        cxt.set(ARQ.symCustomFunctionScriptAllowList, allowList);
        ScriptFunction.clearEngineCache();
    }

    @AfterAll
    public static void unsetupJS() {
        Context cxt = ARQ.getContext();
        cxt.remove(ARQ.symJavaScriptLibFile);
        cxt.remove(ARQ.symJavaScriptFunctions);
    }

    @TestFactory
    @DisplayName("SPARQl-JS")
    public Stream<DynamicNode> testFactory_JS() {
        return Scripts.manifestTestFactorySPARQL("testing/ARQ/Scripting/manifest.ttl");
    }
}
