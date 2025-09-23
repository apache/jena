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

package org.apache.jena.arq.junit;

import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.function.Executable;

import org.apache.jena.arq.junit.manifest.EntryToTest;
import org.apache.jena.arq.junit.manifest.ManifestEntry;
import org.apache.jena.arq.junit.manifest.ManifestProcessor;
import org.apache.jena.arq.junit.manifest.RunnableTestMaker;
import org.apache.jena.arq.junit.riot.ParsingStepForTest;
import org.apache.jena.arq.junit.riot.RiotTests;
import org.apache.jena.arq.junit.sparql.SparqlTests;
import org.apache.jena.atlas.lib.StreamOps;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.ReaderRIOTFactory;
import org.apache.jena.sparql.ARQException;

public class Scripts {

    public static Function<ManifestEntry, Runnable> testMakerSPARQL = SparqlTests::makeSPARQLTest;
    public static Function<ManifestEntry, Runnable> testMakerRIOT = RiotTests::makeRIOTTest;

    static RunnableTestMaker runnableGenerator = RunnableTestMaker.std();
    public static Function<ManifestEntry, Runnable> testMaker() { return runnableGenerator.testMaker(); }

    public static Stream<DynamicNode> manifestTestFactory(String filename, String namePrefix, Function<ManifestEntry, Runnable> testMaker) {
        // When does this happen?
        EntryToTest f = entry -> {
            Runnable r = testMaker.apply(entry);
            if ( r == null )
                return null;
            // Convert the runnable to an Executable
            Executable e = ()->r.run();
            return e;
        };
        return ManifestProcessor.testFactory(filename, namePrefix, f);
    }

    public static Stream<DynamicNode> manifestTestFactory(String filename, Function<ManifestEntry, Runnable> testMaker) {
        return manifestTestFactory(filename, null, testMaker);
    }

    /** Make tests, tries both SPARQL and RIOT test types. */
    public static Stream<DynamicNode> manifestTestFactory(String filename) {
        return manifestTestFactory(filename, (String)null);
    }

    /** Make tests, tries both SPARQL and RIOT test types. */
    public static Stream<DynamicNode> manifestTestFactory(String filename, String namePrefix) {
        return manifestTestFactory(filename, namePrefix, testMaker());
    }

    /** Specifically SPARQL tests */
    public static Stream<DynamicNode> manifestTestFactorySPARQL(String filename) {
        return manifestTestFactorySPARQL(filename, null);
    }

    /** Specifically SPARQL tests */
    public static Stream<DynamicNode> manifestTestFactorySPARQL(String filename, String namePrefix) {
        return manifestTestFactory(filename, namePrefix, testMakerSPARQL);
    }

    /** Specifically RIOT tests */
    public static Stream<DynamicNode> manifestTestFactoryRIOT(String filename) {
        return manifestTestFactoryRIOT(filename, null);
    }

    /** Specifically RIOT tests */
    public static Stream<DynamicNode> manifestTestFactoryRIOT(String filename, String namePrefix) {
        return manifestTestFactory(filename, namePrefix, testMakerRIOT);
    }

    /** Produce tests from a number of test manifests. */
    public static Stream<DynamicNode> all(Function<ManifestEntry, Runnable> testMaker, String... manifests ) {
        if ( manifests == null || manifests.length == 0 )
            throw new ARQException("No manifest files");
        Stream<DynamicNode> x = null;
        for (String manifest : manifests ) {
            Stream<DynamicNode> z = manifestTestFactory(manifest, null, testMaker);
            x = StreamOps.concat(x, z);
        }
        return x;
    }

    /** Create tests that use a specific RIOT Factory for a language.
     *
     * This needs to be done when the test is built.
     * "ParsingStepForTest" is the wrapper code to parse test input.
     * It builds a function, with a binding to the parser factory,
     * so that at test execution time, the parser specified here will be used.
     */
    public static Stream<DynamicNode> withAltParserFactory(Lang lang, ReaderRIOTFactory factory, String filename) {
        ParsingStepForTest.registerAlternative(lang, factory);
        try {
            return Scripts.manifestTestFactoryRIOT(filename);
        } finally {
            ParsingStepForTest.unregisterAlternative(lang);
        }
    }

    /** Does the URI of the test conain a substring? */
    public static boolean entryContainsSubstring(ManifestEntry entry, String fingerprint) {
        String testURI = entry.getURI();
        if ( testURI == null )
            return false;
        return testURI.contains(fingerprint);
    }

}
