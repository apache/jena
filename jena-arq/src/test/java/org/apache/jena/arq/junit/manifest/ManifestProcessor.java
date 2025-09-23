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

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.function.Executable;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.vocabulary.VocabTestQuery;
import org.apache.jena.system.G;

/**
 * Manifest to JUnit5 using {@link DynamicNode}.
 * <p>
 * Each manifest file generates a JUnit5 {@link DynamicContainer}
 * and each test generates a JUnit5 {@link DynamicTest} in that
 * {@link DynamicContainer}.
 * <p>
 * Manifest files can contain other manifest files.
 */
public class ManifestProcessor {

    private static int counterTest = 0;
    private static int counterManifests = 0;

    public static int getCounterTest() {
        return counterTest;
    }

    public static int getCounterManifests() {
        return counterManifests;
    }

    public static Stream<DynamicNode> testFactory(String filename, String namePrefix, EntryToTest entryToTest) {
        DynamicContainer tests = ManifestProcessor.buildFrom(filename, namePrefix, entryToTest);
        return Stream.of(tests);
    }

    public static DynamicContainer buildFrom(String filename, String namePrefix, EntryToTest entryToTest) {
        Set<String> visited = new HashSet<>();
        return build(filename, namePrefix, entryToTest, visited);
    }

    // Avoid these manifests.
    private static Set<String> unsupported = Set.of("rdf11/rdf-mt/", "rdf12/rdf-semantics/");
    private static boolean acceptManifest(String filename) {
        for ( String avoid : unsupported )  {
            if ( filename.contains(avoid) )
                return false;
        }
        return true;
    }

    // "visited" is a safety measure to detect loops in included manifests files.
    private static DynamicContainer build(String filenameOrURI, String namePrefix, EntryToTest entryToTest, Set<String> visited) {
        int x = ++counterManifests;
        // Cycle detection.
        // This must work for URIs and files so converting to Path is not an option.
        // If via a symbolic links, then real link, this catches one step later because we didn't normalize.
        if ( visited.contains(filenameOrURI) )
            throw new RuntimeException("Cycle in manifest files detected: "+filenameOrURI);
        visited.add(filenameOrURI);

        Manifest manifest = Manifest.parse(filenameOrURI);
        List<DynamicContainer> subManifests = buildSubManifests(manifest, namePrefix, entryToTest, visited);

        // One test seems to be treated differently. test runs, but name does not show up.
        List<DynamicTest> tests = buildTests(manifest, namePrefix, entryToTest);

        List<DynamicNode> children = new ArrayList<>();
        children.addAll(subManifests);
        children.addAll(tests);
        String containerName = manifest.getName();
        if ( containerName == null || containerName.isBlank() )
            containerName = "Manifest [C"+counterManifests+"] "+filenameOrURI;

        try {
            return DynamicContainer.dynamicContainer(containerName, children);
        } catch (Throwable th) {
            throw th;
        }
    }

    private static List<DynamicTest> buildTests(Manifest manifest, String namePrefix, EntryToTest entryToTest) {
        List<ManifestEntry> entries = manifest.entries();
        Function<ManifestEntry, DynamicTest> mapper = entry->{
            try {
                Executable test = entryToTest.apply(entry);
                if ( test == null )
                    return null;
                String displayName = prepareTestLabel(entry, namePrefix);
                int x = ++counterTest;
                //displayName = "["+x+"] "+displayName;
                DynamicTest dynTest = DynamicTest.dynamicTest(displayName, test);
                return dynTest;
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        };

        List<DynamicTest> dyntests = entries.stream()
                .map(mapper)
                .filter(Objects::nonNull)
                .toList();
        return dyntests;
    }

    private static List<DynamicContainer> buildSubManifests(Manifest manifest, String namePrefix, EntryToTest entryToTest, Set<String> visited) {
        Iterator<String> iterIncludedFiles = manifest.includedManifests();
        List<DynamicContainer> subs = new ArrayList<>();
        iterIncludedFiles.forEachRemaining(filename -> {
            if ( acceptManifest(filename) ) {
                DynamicContainer d1 = build(filename, namePrefix, entryToTest, visited);
                subs.add(d1);
            }
            else {
                System.err.println("Skip : "+ filename);
            }
        });
        return subs;
    }

    private static String prepareTestLabel(ManifestEntry entry, String namePrefix) {
        String label = fixupName(entry.getName());
        if ( namePrefix != null )
            label = namePrefix+label;

        // action URI or action -> qt:query
        String str = null;

        if ( entry.getAction() != null ) {
            if ( entry.getAction().isURI() )
                str = entry.getAction().getURI();
            else if ( entry.getAction().isBlank() ) {
                Graph mGraph = entry.getManifest().getGraph();
                Node queryNode = G.getZeroOrOneSP(mGraph, entry.getAction(), VocabTestQuery.query.asNode());
                if ( queryNode != null && queryNode.isURI() )
                    str = queryNode.getURI();
            }
        }

        if ( str != null ) {
            int x = str.lastIndexOf('/');
            if ( x > 0 && x < str.length() ) {
                String fn = str.substring(x+1) ;
                label = label+" ("+fn+")";
            }
        }
        return label;
    }

    private static String fixupName(String string) {
            // Eclipse used to parse test names and () were special.
    //        string = string.replace('(', '[');
    //        string = string.replace(')', ']');
        return string;
    }
}
