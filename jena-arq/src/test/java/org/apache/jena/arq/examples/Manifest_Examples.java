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

package org.apache.jena.arq.examples;

import java.util.stream.Stream;

import org.junit.jupiter.api.*;

import org.apache.jena.arq.junit.Scripts;
import org.apache.jena.arq.junit.manifest.TestMakers;
import org.apache.jena.sparql.expr.E_Function;
import org.apache.jena.sparql.expr.NodeValue;

public class Manifest_Examples {
    private static boolean bVerboseWarnings;
    private static boolean bWarnOnUnknownFunction;

    @BeforeAll
    public static void beforeClass() {
        bVerboseWarnings = NodeValue.VerboseWarnings;
        bWarnOnUnknownFunction = E_Function.WarnOnUnknownFunction;
        NodeValue.VerboseWarnings = false;
        E_Function.WarnOnUnknownFunction = false;
    }

    @AfterAll
    public static void afterClass() {
        NodeValue.VerboseWarnings = bVerboseWarnings;
        E_Function.WarnOnUnknownFunction = bWarnOnUnknownFunction;
    }

    @TestFactory
    @DisplayName("ARQ Examples")
    public Stream<DynamicNode> testFactory_ARQ_Examples() {
        return Scripts.manifestTestFactory("testing/ARQ/Examples/manifest.ttl", TestMakers.testMakerSPARQL);
    }

}
