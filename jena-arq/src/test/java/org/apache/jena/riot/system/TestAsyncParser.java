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

package org.apache.jena.riot.system;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.jena.graph.Graph;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.util.IsoMatcher;
import org.junit.Test;

public class TestAsyncParser {

    private static String DIR = "testing/RIOT/Parser/";

    @Test public void async_parse_1() { test(DIR+"empty.ttl"); }
    @Test public void async_parse_2() { test(DIR+"data.ttl"); }

    private static void test(String filename) {
        Graph graph1 = GraphFactory.createDefaultGraph();
        Graph graph2 = GraphFactory.createDefaultGraph();

        RDFParser.source(filename).parse(graph1);
        AsyncParser.asyncParse(List.of(filename), StreamRDFLib.graph(graph2));

        assertEquals(graph1.size(), graph2.size());
        assertTrue( IsoMatcher.isomorphic(graph1, graph2));
    }

}
