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

package org.apache.jena.graph.test;

import static org.apache.jena.graph.test.GraphTestBase.graphAdd ;
import static org.apache.jena.graph.test.GraphTestBase.node ;
import static org.apache.jena.graph.test.GraphTestBase.triple ;
import static org.junit.Assert.assertEquals ;
import static org.junit.Assert.assertFalse ;
import static org.junit.Assert.assertTrue ;

import java.util.List ;

import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.graph.impl.GraphPlain ;
import org.apache.jena.util.iterator.ExtendedIterator ;
import org.junit.BeforeClass ;
import org.junit.Test ;

public class TestGraphPlain {

    private static Graph graph;

    @SuppressWarnings("deprecation")
    @BeforeClass public static void setUp() {
        graph = new org.apache.jena.mem.GraphMem();

        if ( ! graph.getCapabilities().handlesLiteralTyping() )
            throw new IllegalArgumentException("Test graph does not do the value thing");
        graphAdd(graph, "s p o ; s p 1 ; s p 01");
    }

    @Test public void contains1() {
        Triple triple = triple("s p 001");
        assertTrue(graph.contains(triple));
        Graph plain = GraphPlain.plain(graph);
        assertFalse(plain.contains(triple));
    }

    @Test public void contains2() {
        Triple triple = triple("s p 1");
        assertTrue(graph.contains(triple));
        Graph plain = GraphPlain.plain(graph);
        assertTrue(plain.contains(triple));
    }

    @Test public void contains3() {
        Triple triple = triple("s1 p 1");
        assertFalse(graph.contains(triple));
        Graph plain = GraphPlain.plain(graph);
        assertFalse(plain.contains(triple));
    }

    @Test public void contains4() {
        Node s = node("s");
        Node p = node("p");
        Node x = node("001");

        assertTrue(graph.contains(s,p,x));
        Graph plain = GraphPlain.plain(graph);
        assertFalse(plain.contains(s,p,x));
    }

    @Test public void find1() {
        find_test(graph, 2);
        Graph plain = GraphPlain.plain(graph);
        find_test(plain, 1);
    }

    @Test public void find2() {
        Graph plain = GraphPlain.plain(graph);
        Node s = node("s");
        Node p = node("p");
        Node x = node("001");
        List<Triple> list = plain.find(s,p,x).toList();
        assertEquals(0, list.size());
    }

    @Test public void find3() {
        Graph plain = GraphPlain.plain(graph);
        Node s = node("s");
        Node p = node("p");
        Node x = node("??");
        List<Triple> list = plain.find(s,p,x).toList();
        assertEquals(3, list.size());
    }

    private static void find_test(Graph testGraph, int n) {
        Triple triple = triple("?? p 1");
        ExtendedIterator<Triple> iter = testGraph.find(triple);
        //assertTrue(iter.hasNext());
        List<Triple> list = iter.toList();
        assertEquals(n, list.size());
    }
}
