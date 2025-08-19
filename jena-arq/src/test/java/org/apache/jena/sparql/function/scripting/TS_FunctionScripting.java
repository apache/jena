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

import org.junit.platform.suite.api.AfterSuite;
import org.junit.platform.suite.api.BeforeSuite;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import org.apache.jena.sparql.expr.E_Function;

@Suite
@SelectClasses({
        TestNV.class,
        TestScriptFunction.class,
        Manifest_SPARQL_Scripting.class
})
public class TS_FunctionScripting {
    static boolean b = false;

    @BeforeSuite
    public static void beforeClass() {
        b = E_Function.WarnOnUnknownFunction;
        E_Function.WarnOnUnknownFunction = false;
    }

    @AfterSuite
    public static void afterClass() {
        E_Function.WarnOnUnknownFunction = b;
    }
}
