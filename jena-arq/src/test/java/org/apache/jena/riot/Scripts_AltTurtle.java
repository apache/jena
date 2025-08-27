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

import org.junit.jupiter.api.*;

import org.apache.jena.arq.junit.Scripts;
import org.apache.jena.arq.junit.riot.ParsingStepForTest;
import org.apache.jena.riot.lang.extra.TurtleJCC;
import org.apache.jena.sys.JenaSystem;

/** Execute turtle test with alt parser. */
public class Scripts_AltTurtle {

    @TestFactory
    @DisplayName("Scripts AltTurtle TurtleStd")
    public Stream<DynamicNode> testFactory1() {
        return Scripts.manifestTestFactoryRIOT("testing/RIOT/Lang/TurtleStd/manifest.ttl");
    }

    @TestFactory
    @DisplayName("Scripts AltTurtle Extra Turtle")
    public Stream<DynamicNode> testFactory2() {
        return Scripts.manifestTestFactoryRIOT("testing/RIOT/Lang/Turtle2/manifest.ttl");
    }

    @TestFactory
    @DisplayName("Scripts AltTurtle rdf-tests")
    public Stream<DynamicNode> testFactory3() {
        return Scripts.manifestTestFactoryRIOT("testing/rdf-tests-cg/turtle/manifest.ttl");
    }

    // Switch parsers!
    // This needs to be capture during test build time.
    // ParseForTest is the wrapper code to parse test input.

    @BeforeAll public static void beforeClass() {
        JenaSystem.init();
        // Register language and parser factory.
        TurtleJCC.register();
        ParsingStepForTest.registerAlternative(Lang.TURTLE, TurtleJCC.factory);
    }

    @AfterAll public static void afterClass() {
        ParsingStepForTest.unregisterAlternative(Lang.TURTLE);
    }
}
