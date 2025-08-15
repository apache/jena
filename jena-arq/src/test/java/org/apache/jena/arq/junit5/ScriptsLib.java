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

package org.apache.jena.arq.junit5;

import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.function.Executable;

import org.apache.jena.arq.junit5.manifest.EntryToTest;
import org.apache.jena.arq.junit5.manifest.ManifestEntry;
import org.apache.jena.arq.junit5.manifest.ManifestProcessor;
import org.apache.jena.arq.junit5.manifest.RunnableTestMaker;

public class ScriptsLib {

    static RunnableTestMaker runnableGenerator = RunnableTestMaker.std();
    static Function<ManifestEntry, Runnable> testMaker = runnableGenerator.testMaker();

    public static Stream<DynamicNode> manifestTestFactory(String filename, Function<ManifestEntry, Runnable> testMaker) {
        // When does this happen?
        EntryToTest f = entry -> {
            Runnable r = testMaker.apply(entry);
            if ( r == null )
                return null;
            // Convert the runnable to an Executable
            Executable e = ()->r.run();
            return e;
        };
        return ManifestProcessor.testFactory(filename, f);
    }

    /**
     * Make tests, tries both SPARQL and RIOT test types.
     */
    public static Stream<DynamicNode> manifestTestFactory(String filename) {
        return manifestTestFactory(filename, testMaker);
    }

    /** Specifically SPARQL tests */
    public static Stream<DynamicNode> manifestTestFactorySPARQL(String filename) {
        //return manifestTestFactory(filename, SparqlTests::makeSPARQLTest);
        return manifestTestFactory(filename);
    }

    /** Specifically RIOT tests */
    public static Stream<DynamicNode> manifestTestFactoryRIOT(String filename) {
        //return manifestTestFactory(filename, RiotTests::makeRIOTTest);
        return manifestTestFactory(filename);
    }
}
