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

package org.apache.jena.arq.junit.riot;

import org.apache.jena.arq.junit.SkipTest;
import org.apache.jena.arq.junit.manifest.ManifestEntry;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.util.SplitIRI;
import org.apache.jena.vocabulary.TestManifest;

public class SemanticsTests {
    /** RDF Semantics / Model Theory tests.
     * These tests are ignored.
     */

    public static String NSX = TestManifest.NS;
    public static Node typePositiveEntailmentTest =  NodeFactory.createURI(NSX+"PositiveEntailmentTest");
    public static Node typeNegativeEntailmentTest =  NodeFactory.createURI(NSX+"NegativeEntailmentTest");

    public static Runnable makeSemanticsTest(ManifestEntry entry) {
        //Resource manifest = entry.getManifest();
        Node item = entry.getEntry();
        String testName = entry.getName();
        Node action = entry.getAction();
        Node result = entry.getResult();
        Graph graph = entry.getManifest().getGraph();

        Node testType = entry.getTestType();
        if ( testType == null )
            return null;
        try {

            // == Not supported : Model Theory / Semantics tests.

            String NSX = TestManifest.NS;
            if ( equalsType(testType, typePositiveEntailmentTest) )
                return new SkipTest(entry);
            if ( equalsType(testType, typeNegativeEntailmentTest) )
                return new SkipTest(entry);
            return null;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /*package*/static boolean equalsType(Node testType, Node vocabType) {
        return testType.equals(vocabType);
    }

    private static String rebase(Node input, String baseIRI) {
        if ( input.isBlank() )
            return baseIRI;
        String inputURI = input.getURI();
        if ( baseIRI == null )
            return inputURI;
        int splitPoint = SplitIRI.splitpoint(input.getURI());
        if ( splitPoint < 0 )
            return inputURI;

        String x = SplitIRI.localname(inputURI) ;
        baseIRI = baseIRI+x;
        return baseIRI;
    }

    /*package*/ static String fragment(String uri) {
        if ( uri == null )
            return null;
        int j = uri.lastIndexOf('#');
        String frag = (j >= 0) ? uri.substring(j) : uri;
        return frag;
    }
}
