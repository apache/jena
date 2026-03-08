/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.system;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.Test;

import org.apache.jena.graph.Graph;
import org.apache.jena.rdfxml.xmloutput.impl.FindNamespacesRDFXML;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;

/** Test FindNamespacesRDFXML  - easier heer se we can use full turtle */
public class TestFindNamespaces {
    private static String testStr = """
        PREFIX : <http://example/>

        <http://x-subj/s> <http://x-pred/p1> 'abc'^^<http://ns-dt/dType> .

        :s <http://x-pred/p1> <<( :s1 <http://x-pred-tt/p1>   'abc'^^<http://ns-dt-tt/dType> )>> .

        :s <http://x-pred/p1> <<(
                                <http://x-subj/s1>
                                <http://x-pred-tt/p1>
                                <<( :s1 <http://x-pred-tt2/p1> 'abc'^^<http://ns-dt-tt2/dType> )>>
                              )>> .
         """;

    @Test
    public void findNamespace() {
        Graph graph = RDFParser.fromString(testStr, Lang.TURTLE).toGraph();
        Set<String> ns = FindNamespacesRDFXML.namespacesForRDFXML(graph);

        // Not http://x-subj/s, not http://example/
        Set<String> expected = Set.of("http://x-pred/", "http://x-pred-tt/", "http://x-pred-tt2/"
                                      // datatypes.
                                      //, "http://ns-dt-tt2/", "http://ns-dt-tt/"
                                      );
        if ( ! expected.equals(ns) ) {
            System.err.println("== TestFindNamespaces");
            System.err.println("Expected: "+expected);
            System.err.println("Actual:   "+ns);
        }
        assertEquals(expected, ns);
    }
}
