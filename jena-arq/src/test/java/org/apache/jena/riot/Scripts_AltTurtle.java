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

package org.apache.jena.riot;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;

import org.apache.jena.arq.TestConsts;
import org.apache.jena.arq.junit.Scripts;
import org.apache.jena.riot.lang.turtlejcc.TurtleJCC;
import org.apache.jena.sys.JenaSystem;

/** Execute turtle test with alt parser. */
public class Scripts_AltTurtle {

    @TestFactory
    @DisplayName("Scripts AltTurtle rdf-tests")
    public Stream<DynamicNode> testFactory() {
        return Scripts.manifestTestFactoryRIOT(TestConsts.RDF12_TESTS_DIR+"rdf-turtle/manifest.ttl");
    }

    // If run on its own.
    @BeforeAll public static void beforeAll() {
        JenaSystem.init();
        TurtleJCC.register();
    }
}
