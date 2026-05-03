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

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.function.BiConsumer;

import org.junit.jupiter.api.Test;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.sparql.graph.GraphZero;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.vocabulary.RDF;

public class TestGList {

    static { JenaSystem.init(); }

    private static Node x = SSE.parseNode(":x");
    private static Node p = SSE.parseNode(":p");
    private static Node elt1 = SSE.parseNode("'A'");
    private static Node elt2 = SSE.parseNode("'B'");

    @Test public void glist_members_01() {
        Graph graph = GraphZero.instance();
        List<Node> members = members(graph, RDF.Nodes.nil);
        assertTrue(members.isEmpty());
   }

    @Test public void glist_members_02() {
        test("()", (graph, list)->{
            List<Node> members = members(graph, list);
            assertTrue(members.isEmpty());
        });
    }

    @Test public void glist_members_03() {
        test("('A')", (graph, list)->{
            List<Node> members = members(graph, list);
            assertEquals(1, members.size());
            assertEquals(elt1, members.getFirst());
        });
    }

    @Test public void glist_members_04() {
        test("('A' 'B')", (graph, list)->{
            List<Node> members = members(graph, list);
            assertEquals(2, members.size());
            assertEquals(elt1, members.getFirst());
            assertEquals(elt2, members.getLast());
        });
    }

    @Test public void glist_members_05() {
        test("((:a :b :c))", (graph, list)->{
            List<Node> members = members(graph, list);
            assertEquals(1, members.size());
            assertTrue(members.getFirst().isBlank());

            Node listInner = members.getFirst();
            List<Node> membersInner = members(graph, listInner);
            assertEquals(3, membersInner.size());
        });
    }

    @Test public void glist_members_bad_01() {
        testEx("[ rdf:first 1 ]", (graph, list)->members(graph, list));
    }

    @Test public void glist_members_bad_02() {
        testEx("[ rdf:rest rdf:nil ]", (graph, list)->members(graph, list));
    }

    @Test public void glist_members_bad_03() {
        testWellformed("[ rdf:first 1 ; rdf:rest () ]");
        testEx("[ rdf:first 1 ; rdf:first 2 ; rdf:rest () ]", (graph, list)->members(graph, list));
    }

    @Test public void glist_members_bad_04() {
        testWellformed("[ rdf:first 1 ; rdf:rest ('A') ]");
        testEx("[ rdf:first 1 ; rdf:rest ('A') ; rdf:rest () ]", (graph, list)->members(graph, list));
    }

    @Test public void glist_wellformed_01() {
        testWellformed("()");
    }

    @Test public void glist_wellformed_02() {
        testWellformed("('A')");
    }

    // Allow rdf:type
    @Test public void glist_wellformed_03() {
        testWellformed("[ rdf:first 1 ; rdf:rest rdf:nil; rdf:type :Cell]");
    }

    // Allow rdf:type
    @Test public void glist_wellformed_04() {
        testWellformed("""
                [ rdf:first 1 ;
                  rdf:rest [
                      rdf:first 1 ;
                      rdf:rest rdf:nil ;
                      rdf:type :Cell
                  ]
                ]
                """);
    }

    @Test public void glist_wellformed_bad_01() {
        testNotWellformed("[ rdf:first 1 ]");
    }

    @Test public void glist_wellformed_bad_02() {
        testNotWellformed("[ rdf:rest rdf:nil ]");
    }

    @Test public void glist_wellformed_bad_03() {
        testWellformed("[ rdf:first 1 ; rdf:rest () ]");
        testNotWellformed("[ rdf:first 1 ; rdf:first 2 ; rdf:rest () ]");
    }

    @Test public void glist_wellformed_bad_04() {
        testWellformed("[ rdf:first 1 ; rdf:rest ('A') ]");
        testNotWellformed("[ rdf:first 1 ; rdf:rest ('A') ; rdf:rest () ]");
    }

    @Test public void glist_wellformed_bad_10() {
        testWellformed("[ rdf:first 1 ; rdf:rest [ rdf:first 'A' ; rdf:rest rdf:nil ] ]");
        testNotWellformed("""
                [ rdf:first 1 ;
                  rdf:rest [
                      rdf:first 'A' ;
                      rdf:first 'B' ;
                      rdf:rest rdf:nil
                      ]
                ]
                """);
    }

    @Test public void glist_wellformed_bad_11() {
        testWellformed("""
                [ rdf:first 1 ;
                  rdf:rest [
                      rdf:first 'A' ;
                      rdf:rest rdf:nil
                    ]
                ]
                """);
        testNotWellformed("""
                [ rdf:first 1 ;
                  rdf:rest [
                      rdf:first 'A' ;
                      rdf:rest 'B' ;
                      rdf:rest rdf:nil ]
                ]
                """);
    }

    @Test public void glist_wellformed_bad_12() {
        testWellformed("[ rdf:first 1 ; rdf:rest [ rdf:first 'A' ; rdf:rest rdf:nil ] ]");
        testNotWellformed("[ rdf:first 1 ; rdf:rest [ rdf:first 'A' ; ] ]");
    }

    @Test public void glist_wellformed_bad_13() {
        testWellformed("[ rdf:first 1 ; rdf:rest [ rdf:first 'A' ; rdf:rest rdf:nil ] ]");
        testNotWellformed("[ rdf:first 1 ; rdf:rest [ rdf:rest rdf:nil ] ]");
    }

    @Test public void glist_wellformed_bad_14() {
        testWellformed("[ rdf:first 1 ; rdf:rest [ rdf:first 'A' ; rdf:rest rdf:nil ] ]");
        testNotWellformed("[ rdf:first 1 ; rdf:rest [ ] ]");
    }

    @Test public void glist_wellformed_cyclic_01() {
        String graphStr = """
                :head rdf:first 0 ; rdf:rest :x .
                :x rdf:first 1 ; rdf:rest :y .
                :y rdf:first 1 ; rdf:rest :x .
                """;
        Graph graph = graph(graphStr);
        Node list = x;
        boolean b = GList.isWellformedList(graph, list);
        assertFalse(b);
        assertThrows(RDFDataException.class,  ()->GList.isWellformedListEx(graph, list));
    }

    @Test public void glist_isWellformedEx_message_no_rest() {
        // A cell with rdf:first but no rdf:rest should cause the "no rdf:next" error message
        String s = "[ rdf:first 1 ]";
        String g = """
                PREFIX :     <http://example/>
                PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
                :x :p """ + s;
        Graph graph = RDFParser.fromString(g, Lang.TURTLE).toGraph();
        Node list = G.getOneSP(graph, SSE.parseNode(":x"), SSE.parseNode(":p"));
        RDFDataException ex = assertThrows(RDFDataException.class, ()->GList.isWellformedListEx(graph, list));
        String msg = ex.getMessage();
        assertTrue(msg != null && msg.contains("no rdf:next"), "Expected exception message to mention 'no rdf:next', got: " + msg);
    }

    @Test public void glist_length_01() {
        testlength("()", 0);
    }

    @Test public void glist_length_02() {
        testlength("(1 2 3)", 3);
    }

    @Test public void glist_length_03() {
        testlength("( () )", 1);
    }

    @Test public void glist_length_04() {
        testlength("( (:a :b) )", 1);
    }

    @Test public void glist_length_05() {
        testlength("( :A (:a :b) )", 2);
    }

    @Test public void glist_length_06() {
        testlength("( (:a :b) :Z)", 2);
    }

    @Test public void glist_length_bad_01() {
        assertThrows(RDFDataException.class,  ()->testlength("[ rdf:first 1 ; rdf:rest [ rdf:first 'A' ; ] ]", -1));
    }

    @Test public void glist_indexOf_01() {
        test("('A')", (graph, list)->{
            Node elt = NodeFactory.createLiteralString("A");
            int idx = GList.indexOf(graph, list, elt);
            assertEquals(0, idx);
        });
    }

    @Test public void glist_indexOf_02() {
        test("('A')", (graph, list)->{
            Node elt = NodeFactory.createLiteralString("B");
            int idx = GList.indexOf(graph, list, elt);
            assertEquals(-1, idx);
        });
    }

    @Test public void glist_indexOf_03() {
        test("('A' 'A' 'A')", (graph, list)->{
            Node elt = NodeFactory.createLiteralString("A");
            int idx = GList.indexOf(graph, list, elt);
            assertEquals(0, idx);
        });
    }


    @Test public void glist_indexOf_04() {
        // Does not recurse in lists in lists.
        test("(('A'))", (graph, list)->{
            Node elt = NodeFactory.createLiteralString("A");
            int idx = GList.indexOf(graph, list, elt);
            assertEquals(-1, idx);
        });
    }

    @Test public void glist_indexOf_05() {
        test("()", (graph, list)->{
            Node elt = NodeFactory.createLiteralString("A");
            int idx = GList.indexOf(graph, list, elt);
            assertEquals(-1, idx);
        });
    }

    @Test public void glist_get_01() {
        test("('A')", (graph, list)->{
            Node x = GList.get(graph, list, 0);
            assertEquals("A", x.getLiteral().getLexicalForm());
        });
    }

    @Test public void glist_get_02() {
        test("('A')", (graph, list)->{
            Node x = GList.get(graph, list, 1);
            assertNull(x);
        });
    }

    @Test public void glist_get_03() {
        test("()", (graph, list)->{
            Node x = GList.get(graph, list, 0);
            assertNull(x);
        });
    }

    @Test public void glist_get_04() {
        test("('A' 'B' 'C')", (graph, list)->{
            Node x = GList.get(graph, list, 1);
            assertEquals("B", x.getLiteral().getLexicalForm());
        });
    }

    @Test public void glist_get_negative_index() {
        test("('A')", (graph, list)->{
            assertNull(GList.get(graph, list, -1));
        });
    }

    @Test public void glist_forEach_01() {
        test("()", (graph, list)->{
            StringBuilder sb = new StringBuilder();
            GList.forEach(graph, list, n -> {
                // numeric literals; use lexical form
                sb.append(n.getLiteral().getLexicalForm()).append(",");
            });
            assertEquals("", sb.toString());
        });
    }

    @Test public void glist_forEach_02() {
        test("(1 2 3)", (graph, list)->{
            StringBuilder sb = new StringBuilder();
            GList.forEach(graph, list, n -> {
                // numeric literals; use lexical form
                sb.append(n.getLiteral().getLexicalForm()).append(",");
            });
            assertEquals("1,2,3,", sb.toString());
        });
    }

    @Test public void glist_iterator_traverse() {
        test("('A' 'B' 'C')", (graph, list)->{
            List<Node> seen = new ArrayList<>();
            Iterator<Node> it = GList.iterator(graph, list);
            while (it.hasNext()) {
                seen.add(it.next());
            }
            List<Node> expected = GList.elements(graph, list);
            assertEquals(expected, seen);
        });
    }

    @Test public void glist_iterator_empty() {
        Graph graph = graph(":s :p :o");
        Iterator<Node> it = GList.iterator(graph, RDF.Nodes.nil);
        assertFalse(it.hasNext());
    }

    @Test public void glist_closedCells_reject_extra_predicate() {
        // Create a list cell that has an extra non-list triple :p :o
        // Use closedCells = true to exercise that branch.
        test("[ rdf:first 1 ; rdf:rest rdf:nil ; :p :o ]", (graph, list)->{
            assertFalse(GList.isWellformedList(graph, list, true));
            assertThrows(RDFDataException.class, ()->GList.isWellformedListEx(graph, list, true));
        });
    }

    @Test public void glist_indexOf_blanknode_identity() {
        // Outer list contains one inner list (a blank node). indexOf should handle blank-node identity.
        test("((:a :b :c))", (graph, list)->{
            // get the actual inner blank node from the members method
            List<Node> members = members(graph, list);
            assertEquals(1, members.size());
            Node inner = members.get(0);

            // Searching for the same blank node returns 0
            assertEquals(0, GList.indexOf(graph, list, inner));

            // Searching for a different (not-equal) blank node returns -1
            Node otherBNode = NodeFactory.createBlankNode();
            assertEquals(-1, GList.indexOf(graph, list, otherBNode));
        });
    }

    @Test public void glist_contains_01() {
        test("()", (graph, list)->{
            Node c = NodeFactory.createLiteralString("C");
            assertFalse(GList.contains(graph, list, c));
        });
    }

    @Test public void glist_contains_02() {
        test("('A' 'B')", (graph, list)->{
            Node a = NodeFactory.createLiteralString("A");
            Node c = NodeFactory.createLiteralString("C");
            assertTrue(GList.contains(graph, list, a));
            assertFalse(GList.contains(graph, list, c));
        });
    }

    // ----

    private List<Node> members(Graph graph, Node list) {
        // Do the checking form first in case it throws RDFDataException.
        List<Node> x1 = GList.members(graph, list);
        List<Node> x2 = GList.elements(graph, list);
        assertEquals(x1,  x2);
        return x1;
    }

    private static void testWellformed(String string) {
        test(string, (graph, list)->{
            assertTrue(GList.isWellformedList(graph, list));
        });
    }

    private static void testNotWellformed(String string) {
        test(string, (graph, list)->{
            assertFalse(GList.isWellformedList(graph, list));
            assertThrows(RDFDataException.class, ()->GList.isWellformedListEx(graph, list));
        });
    }

    private static void testlength(String string, int expected) {
        test(string, (graph, list)->{
            long actual = GList.listLength(graph, list);
            assertEquals(expected, actual);
        });
    }

    private static void test(String string, BiConsumer<Graph, Node> action) {
        String graphStr = ":x :p "+string;
        Graph graph = graph(graphStr);
        Node list = G.getOneSP(graph, x, p);
        action.accept(graph,  list);
    }

    private static void testEx(String string, BiConsumer<Graph, Node> action) {
        String graphStr = ":x :p "+string;
        Graph graph = graph(graphStr);
        Node list = G.getOneSP(graph, x, p);
        assertThrows(RDFDataException.class, ()->action.accept(graph,  list));
    }


    private static Graph graph(String str) {
        String setup = """
                PREFIX :     <http://example/>
                PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
                """;
        return RDFParser.fromString(setup+str, Lang.TURTLE).toGraph();
    }

}
