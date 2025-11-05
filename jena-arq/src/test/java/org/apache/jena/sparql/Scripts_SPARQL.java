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

package org.apache.jena.sparql;

import java.util.stream.Stream;

import org.junit.jupiter.api.*;

import org.apache.jena.arq.TestConsts;
import org.apache.jena.arq.junit.Scripts;
import org.apache.jena.arq.junit.sparql.SparqlTests;
import org.apache.jena.sparql.expr.E_Function;
import org.apache.jena.sparql.expr.NodeValue;

public class Scripts_SPARQL {
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
    @DisplayName("ARQ-SPARQL")
    public Stream<DynamicNode> testFactorySPARQL_ARQ() {
        return all("testing/ARQ/Syntax/manifest-syntax.ttl",
                   "testing/ARQ/manifest-arq.ttl",
                   "testing/ARQ/Serialization/manifest.ttl");
    }

    // Test from rdf-tests (and other replaces).
    @TestFactory
    @DisplayName("SPARQL 1.0")
    public Stream<DynamicNode> testFactorySPARQL_10() {
        return all(TestConsts.SPARQL10_TESTS_DIR+"manifest.ttl");
    }

    @TestFactory
    @DisplayName("SPARQL 1.1")
    public Stream<DynamicNode> testFactorySPARQL_11() {
        return all(TestConsts.SPARQL11_TESTS_DIR+"manifest-sparql11-query.ttl",
                   // Not CSV tests - no comparision supported.
                   // No XML results - part of SPARQL 1.0.
                   TestConsts.SPARQL11_TESTS_DIR+"json-res/manifest.ttl",
                   TestConsts.SPARQL11_TESTS_DIR+"manifest-sparql11-update.ttl");
    }

    @TestFactory
    @DisplayName("SPARQL 1.2")
    public Stream<DynamicNode> testFactorySPARQL_12() {
        return all(TestConsts.SPARQL12_TESTS_DIR+"manifest.ttl");
    }

    private static Stream<DynamicNode> all(String... manifests) {
        if ( manifests == null || manifests.length == 0 )
            throw new ARQException("No manifest files");
        return Scripts.all(SparqlTests::makeSPARQLTest, manifests);
    }
}
