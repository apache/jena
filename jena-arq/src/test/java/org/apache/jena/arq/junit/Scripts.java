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

import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.function.Executable;

import org.apache.jena.arq.junit.manifest.*;
import org.apache.jena.arq.junit.riot.ParsingStepForTest;
import org.apache.jena.atlas.lib.StreamOps;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.ReaderRIOTFactory;
import org.apache.jena.sparql.ARQException;

public class Scripts {

    private static TestMakers runnableGenerator = TestMakers.system();

    private static Stream<DynamicNode> manifestTestFactory0(String filename, String namePrefix, TestMaker testMaker) {
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

    /**
     * Return a function make tests from ManifestEntry entries that tries all
     * installed test makers.
     * @see TestMakers
     */
    public static TestMaker testMakerGeneral() { return runnableGenerator.testMaker(); }

    public static Stream<DynamicNode> manifestTestFactory(String filename, String namePrefix, TestMaker testMaker) {
        return manifestTestFactory0(filename, namePrefix, testMaker);
    }

    public static Stream<DynamicNode> manifestTestFactory(String filename, TestMaker testMaker) {
        return manifestTestFactory(filename, null, testMaker);
    }

    /** Make tests; tries all installed test makers. */
    public static Stream<DynamicNode> manifestTestFactory(String filename) {
        return manifestTestFactory0(filename, (String)null, testMakerGeneral());
    }

   /** Make tests, tries all installed test types. */
   public static Stream<DynamicNode> manifestTestFactory(String filename, String namePrefix) {
       return manifestTestFactory0(filename, namePrefix, testMakerGeneral());
   }

    /** Specifically SPARQL tests */
    public static Stream<DynamicNode> manifestTestFactorySPARQL(String filename) {
        return manifestTestFactorySPARQL(filename, null);
    }

    /** Specifically SPARQL tests */
    public static Stream<DynamicNode> manifestTestFactorySPARQL(String filename, String namePrefix) {
        return manifestTestFactory0(filename, namePrefix, TestMakers.testMakerSPARQL);
    }

    /** Specifically RIOT tests */
    public static Stream<DynamicNode> manifestTestFactoryRIOT(String filename) {
        return manifestTestFactoryRIOT(filename, null);
    }

    /** Specifically RIOT tests */
    public static Stream<DynamicNode> manifestTestFactoryRIOT(String filename, String namePrefix) {
        return manifestTestFactory0(filename, namePrefix, TestMakers.testMakerRIOT);
    }

    /** Produce tests from a number of test manifests. */
    public static Stream<DynamicNode> all(TestMaker testMaker, String... manifests ) {
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

    /** Does the URI of the test contain a substring? */
    public static boolean entryContainsSubstring(ManifestEntry entry, String fingerprint) {
        String testURI = entry.getURI();
        if ( testURI == null )
            return false;
        return testURI.contains(fingerprint);
    }
}
