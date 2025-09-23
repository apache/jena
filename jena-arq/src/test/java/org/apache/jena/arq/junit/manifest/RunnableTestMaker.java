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
import java.util.function.Function;

import org.apache.jena.arq.junit.SurpressedTest;
import org.apache.jena.arq.junit.riot.RiotTests;
import org.apache.jena.arq.junit.riot.SemanticsTests;
import org.apache.jena.arq.junit.sparql.SparqlTests;
import org.apache.jena.graph.Node;

/**
 * Manage a number of test makes so a {@code TestFactory} does not need to know the
 * kind of the tests. Making tests using {@link #testMaker} tries each registered
 * test maker until one return non-null.
 */
public class RunnableTestMaker {

    private final List<Function<ManifestEntry, Runnable>> installed = new ArrayList<>();

    public static RunnableTestMaker std() {
        RunnableTestMaker maker = new RunnableTestMaker();
        maker.installTestMaker(SparqlTests::makeSPARQLTest);
        maker.installTestMaker(RiotTests::makeRIOTTest);
        maker.installTestMaker(SemanticsTests::makeSemanticsTest);
        return maker;
    }

    private RunnableTestMaker() {}

    public RunnableTestMaker(List<Function<ManifestEntry, Runnable>> installed) {
        this();
        for ( Function<ManifestEntry, Runnable> maker : installed ) {
            installTestMaker(maker);
        }
    }

    public void installTestMaker(Function<ManifestEntry, Runnable> testMaker) {
        installed.add(testMaker);
    }

    public Function<ManifestEntry, Runnable> testMaker() {
        return (ManifestEntry entry) -> {
            for ( Function<ManifestEntry, Runnable> engine : installed ) {
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
