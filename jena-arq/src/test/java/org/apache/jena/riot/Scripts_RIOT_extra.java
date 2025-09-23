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

/** Run additional RIOT tests - not the RDF Test CG test suites for RDF syntaxes. */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Scripts_RIOT_extra {

    // Turtle2
    // RDF/JSON

    @Order(1)
    @TestFactory
    @DisplayName("RIOT - RDF/JSON")
    public Stream<DynamicNode> testFactory_rdf_json() {
        return Scripts.manifestTestFactory(TestConsts.RIOT_TESTS_DIR+"RDF-JSON/manifest.ttl");
    }

    @Order(2)
    @TestFactory
    @DisplayName("RIOT - Turtle2")
    public Stream<DynamicNode> testFactory_turtle2() {
        return Scripts.manifestTestFactory(TestConsts.RIOT_TESTS_DIR+"Turtle2/manifest.ttl");
    }
}
