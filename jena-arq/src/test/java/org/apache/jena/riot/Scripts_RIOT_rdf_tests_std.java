/*
jena-cmds  * Licensed to the Apache Software Foundation (ASF) under one
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

import java.util.stream.Stream;

import org.junit.jupiter.api.*;

import org.apache.jena.arq.TestConsts;
import org.apache.jena.arq.junit.Scripts;

/** Run the RDF Test CG test suites for RDF syntaxes
 * @see Scripts_c14n
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Scripts_RIOT_rdf_tests_std {
    // Excludes the semantics tests.
    // Excludes canonicalization tests (separate)

    @Order(1)
    @TestFactory
    @DisplayName("rdf-tests CG - N-Triples")
    public Stream<DynamicNode> testFactory_n_triples() {
        return Scripts.manifestTestFactory(TestConsts.RDF12_TESTS_DIR+"rdf-n-triples/manifest.ttl");
    }

    @Order(2)
    @TestFactory
    @DisplayName("rdf-tests CG - N-Quads")
    public Stream<DynamicNode> testFactory_n_quads() {
        return Scripts.manifestTestFactory(TestConsts.RDF12_TESTS_DIR+"rdf-n-quads/manifest.ttl");
    }

    @Order(3)
    @TestFactory
    @DisplayName("rdf-tests CG - Turtle")
    public Stream<DynamicNode> testFactory_n_turtle() {
        return Scripts.manifestTestFactory(TestConsts.RDF12_TESTS_DIR+"rdf-turtle/manifest.ttl");
    }

    @Order(4)
    @TestFactory
    @DisplayName("rdf-tests CG - TriG")
    public Stream<DynamicNode> testFactory_trig() {
        return Scripts.manifestTestFactory(TestConsts.RDF12_TESTS_DIR+"rdf-trig/manifest.ttl");
    }

    @Order(5)
    @TestFactory
    @DisplayName("rdf-tests CG - RDF/XML")
    public Stream<DynamicNode> testFactory_rdf_xml() {
        // Caution! RDF 1.1 until RRX updated.
        return Scripts.manifestTestFactory(TestConsts.RDF11_TESTS_DIR+"rdf-xml/manifest.ttl");
    }

    // Canonicalization tests in Scripts_RIOT_c14n_tests

//    @Order(10)
//    @TestFactory
//    @DisplayName("rdf-tests CG - N-Quads Canonicalization")
//    public Stream<DynamicNode> testFactory_n_quads_c14n() {
//        return Scripts.manifestTestFactory(TestConsts.RDF12_TESTS_DIR+"rdf-n-quads/c14n/manifest.ttl");
//    }
//
//    @Order(11)
//    @TestFactory
//    @DisplayName("rdf-tests CG - N-Triples Canonicalization")
//    public Stream<DynamicNode> testFactory_n_Triples_c14n() {
//        return Scripts.manifestTestFactory(TestConsts.RDF12_TESTS_DIR+"rdf-n-triples/c14n/manifest.ttl");
//    }
}
