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

package org.apache.jena.arq.junit.manifest;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.arq.junit.SurpressedTest;
import org.apache.jena.arq.junit.riot.RiotTests;
import org.apache.jena.arq.junit.riot.SemanticsTests;
import org.apache.jena.arq.junit.sparql.SparqlTests;
import org.apache.jena.graph.Node;

/**
 * Manage a number of test makers so a {@code TestFactory} does not need to know the
 * kind of the tests. Making tests using {@link #testMaker} tries each registered
 * test maker until one returns non-null.
 */
public class TestMakers {

    public static TestMaker testMakerSPARQL = SparqlTests::makeSPARQLTest;
    public static TestMaker testMakerRIOT = RiotTests::makeRIOTTest;

    private final List<TestMaker> installed = new ArrayList<>();
    private static TestMakers systemSetup = systemSetup();

    // The test makers in the codebase for W3C tests.
    // Add more with "install"
    private static TestMakers systemSetup() {
        TestMakers maker = new TestMakers();
        maker.add(testMakerSPARQL);
        maker.add(testMakerRIOT);
        maker.add(SemanticsTests::makeSemanticsTest);
        return maker;
    }

    /**
     * Add a test maker to the system-wide test makers list
     */
    public static void install(TestMaker testMaker) {
        systemSetup.add(testMaker);
    }

    /** Return the system-wide instance of {@link TestMakers}. */
    public static TestMakers system() {
        return systemSetup;
    }

    public void add(TestMaker testMaker) {
        installed.add(testMaker);
    }

    /**
     * Return a function that takes a {@link ManifestEntry} and provides a test maker.
     * The function iterates through the installed {@link TestMaker TestMakers}
     * until it finds one that
     * If no test maker is found, return a {@link SurpressedTest}.
     */
    public TestMaker testMaker() {
        return (ManifestEntry entry) -> {
            for ( TestMaker engine : installed ) {
                Runnable r = engine.apply(entry);
                if ( r != null )
                    return r;
            }
            String testName = entry.getName();
            Node testType = entry.getTestType();
            System.err.println("Unrecognized test : (" + testType + ")" + testName);
            return new SurpressedTest(entry);
        };
    }
}
