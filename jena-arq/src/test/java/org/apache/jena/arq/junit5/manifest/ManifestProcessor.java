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

package org.apache.jena.arq.junit5.manifest;

import java.nio.file.Path;
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
 *
 */
public class ManifestProcessor {

    private static int counterTest = 0;
    private static int counterContainer = 0;

    public static Stream<DynamicNode> testFactory(String filename, EntryToTest entryToTest) {
        DynamicContainer tests = ManifestProcessor.buildFrom(filename, entryToTest);
        return Stream.of(tests);
    }

    public static DynamicContainer buildFrom(String filename, EntryToTest entryToTest) {
        Set<Path> visited = new HashSet<>();
        return build(filename, entryToTest, visited);
    }

    // "visited" is a safety measure to detect loops in included manifests files.
    private static DynamicContainer build(String filename, EntryToTest entryToTest, Set<Path> visited) {
        int x = ++counterContainer;
        Path path =  Path.of(filename).toAbsolutePath();
        if ( visited.contains(path) )
            throw new RuntimeException("Cycle in manifest files detected: "+path);
        visited.add(path);

        Manifest manifest = Manifest.parse(filename);
        List<DynamicContainer> subManifests = buildSubManifests(manifest, entryToTest, visited);

        // One test seems to be treated differently. test runs, but name does not show up.
        List<DynamicTest> tests = buildTests(manifest, entryToTest);


        List<DynamicNode> children = new ArrayList<>();
        children.addAll(subManifests);

        // Add tests
        if ( tests.size() == 1 ) {
            // Otherwise JUnit5 only shows the container name. (this maybe an Eclipse thing)
            DynamicTest test = tests.get(0);
            DynamicContainer here =  DynamicContainer.dynamicContainer(test.getDisplayName(), tests);
            children.add(here);
        } else {
            children.addAll(tests);
        }

        String containerName = manifest.getName();
        //containerName = "{"+x+"} "+containerName;
        //System.out.println("Container of children="+children.size());

        try {
            return DynamicContainer.dynamicContainer(containerName, children);
        } catch (Throwable th) {
            throw th;
        }
    }

    private static List<DynamicTest> buildTests(Manifest manifest, EntryToTest entryToTest) {
        List<ManifestEntry> entries = manifest.entries();
        Function<ManifestEntry, DynamicTest> mapper = entry->{
            try {
                Executable test = entryToTest.apply(entry);
                if ( test == null )
                    return null;
                String displayName = prepareTestLabel(entry, null);
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

    private static List<DynamicContainer> buildSubManifests(Manifest manifest, EntryToTest entryToTest, Set<Path> visited) {
        Iterator<String> iterIncludedFiles = manifest.includedManifests();
        List<DynamicContainer> subs = new ArrayList<>();
        iterIncludedFiles.forEachRemaining(filename -> {
            DynamicContainer d1 = build(filename, entryToTest, visited);
            subs.add(d1);
        });
        return subs;
    }

    private static String prepareTestLabel(ManifestEntry entry, String prefix) {
        String label = fixupName(entry.getName());
        if ( prefix != null )
            label = prefix+label;

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
