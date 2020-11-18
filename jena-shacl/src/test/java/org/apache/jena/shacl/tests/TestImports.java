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

package org.apache.jena.shacl.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.other.G;
import org.apache.jena.riot.system.IRIResolver;
import org.apache.jena.shacl.Imports;
import org.junit.Test;

public class TestImports {
    // Work in absolute URIs.
    private static String FILES = IRIResolver.resolveString("src/test/files/imports");
    private static Node g1 = NodeFactory.createURI("http://example/graph1");
    private static Node g2 = NodeFactory.createURI("http://example/graph2");
    private static Node g3 = NodeFactory.createURI("http://example/graph3");
    private static Node g4 = NodeFactory.createURI("http://example/graph4");
    private static Node g5 = NodeFactory.createURI("http://example/graph5");

    private static Node u1 = NodeFactory.createURI(FILES+"/graph1.ttl");
    private static Node u2 = NodeFactory.createURI(FILES+"/graph2.ttl");
    private static Node u3 = NodeFactory.createURI(FILES+"/graph3.ttl");
    private static Node u4 = NodeFactory.createURI(FILES+"/graph4.ttl");
    private static Node u5 = NodeFactory.createURI(FILES+"/graph5.ttl");

    private static Node predicate = NodeFactory.createURI("http://example/p");

    @Test public void testImports1() {
        Graph graph = RDFDataMgr.loadGraph(FILES+"/graph1.ttl");
        Node base = Imports.base(graph);
        assertEquals(g1, base);
    }

    @Test public void testImports2() {
        Graph graph = RDFDataMgr.loadGraph(FILES+"/graph1.ttl");
        List<Node> imports = Imports.imports(graph);
        assertEquals(2, imports.size());
        assertTrue(imports.contains(u2));
        assertTrue(imports.contains(u3));
    }

    @Test public void testImports3() {
        Graph graph = RDFDataMgr.loadGraph(FILES+"/graph1.ttl");

        Pair<Node, List<Node>> pair = Imports.baseAndImports(graph);
        Node base = pair.getLeft();
        List<Node> imports = pair.getRight();

        assertEquals(g1, base);
        assertEquals(2, imports.size());
        assertTrue(imports.contains(u2));
        assertTrue(imports.contains(u3));
    }

    @Test public void testImportsLoading1() {
        Graph graph = Imports.loadWithImports(FILES+"/graph1.ttl");
        // Used blank nodes to detect loaded once or multiple times.
        //RDFDataMgr.write(System.out, graph, Lang.TTL);
        assertTrue(G.containsOne(graph, g1, predicate, null));
        assertTrue(G.containsOne(graph, g2, predicate, null));
        assertTrue(G.containsOne(graph, g3, predicate, null));
        assertTrue(G.containsOne(graph, g4, predicate, null));
        assertTrue(G.containsOne(graph, g5, predicate, null));
    }

    @Test public void testImportsLoading2() {
        Graph graph1 = RDFDataMgr.loadGraph(FILES+"/graph1.ttl");
        Graph graph = Imports.withImports(FILES+"/graph1.ttl",graph1);
        assertTrue(G.containsOne(graph, g1, predicate, null));
        assertTrue(G.containsOne(graph, g2, predicate, null));
        assertTrue(G.containsOne(graph, g3, predicate, null));
        assertTrue(G.containsOne(graph, g4, predicate, null));
        assertTrue(G.containsOne(graph, g5, predicate, null));
    }

    @Test public void testImportsLoading3() {
        Graph graph1 = RDFDataMgr.loadGraph(FILES+"/graph1.ttl");
        Graph graph = Imports.withImports(graph1);
        // Will be read again due to not knowing it URI.
        // Skip test.
        // assertTrue(G.containsOne(graph, g1, p, null));
        assertTrue(G.containsOne(graph, g2, predicate, null));
        assertTrue(G.containsOne(graph, g3, predicate, null));
        assertTrue(G.containsOne(graph, g4, predicate, null));
        assertTrue(G.containsOne(graph, g5, predicate, null));
    }


}

