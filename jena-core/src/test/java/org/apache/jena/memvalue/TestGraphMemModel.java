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

package org.apache.jena.memvalue;

import static org.apache.jena.junit.GraphHelper.txnBegin;
import static org.apache.jena.junit.GraphHelper.txnCommit;
import static org.apache.jena.junit.GraphHelper.txnRollback;
import static org.apache.jena.junit.GraphHelper.txnRun;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Set;

import org.junit.jupiter.api.Test;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.*;
import org.apache.jena.graph.impl.LiteralLabelFactory;
import org.apache.jena.junit.NodeCreateUtils;
import org.apache.jena.util.iterator.ExtendedIterator;

/**
 * Tests of a graph which has support for (Java object) value handling.
 * <p>
 * This is used by the Model API.
 * <p>
 * Jena5+ : Only {@link GraphMemValue} supports this. Other graph are "same term", not
 * "same value" and language tags are held in canonical form.
 */
public class TestGraphMemModel extends BaseTestGraph_JU6 {

    @Override
    public Graph getNewGraph() {
        return GraphMemFactory.createGraphMemForModel();
    }

    @Test
    public void testSizeAfterRemove() {
        Graph g = getGraphWith("x p y");
        ExtendedIterator<Triple> it = g.find(GraphTestLib.triple("x ?? ??"));
        it.removeNext();
        assertEquals(0, g.size());
    }

    /**
     * Check that contains respects by-value semantics.
     */
    @Test
    public void testContainsByValue() {
        Graph g1 = getGraphWith("x P '1'xsd:integer");

        boolean b = g1.contains(GraphTestLib.triple("x P '01'xsd:int"));
        if ( !b )
            System.err.println("No value match: " + g1.getClass().getSimpleName());

        assertTrue(g1.contains(GraphTestLib.triple("x P '01'xsd:int")));
        //
        Graph g2 = getGraphWith("x P '1'xsd:int");
        assertTrue(g2.contains(GraphTestLib.triple("x P '1'xsd:integer")));
        //
        Graph g3 = getGraphWith("x P '123'xsd:string");
        assertTrue(g3.contains(GraphTestLib.triple("x P '123'")));
    }

    // From the contract tests

    @SuppressWarnings("deprecation")
    @Test
    public void test_ProgrammaticValue() {
        Graph g = getNewGraph();
        Node ab = NodeFactory.createLiteral(LiteralLabelFactory.createTypedLiteral(Byte.valueOf((byte)42)));
        Node as = NodeFactory.createLiteral(LiteralLabelFactory.createTypedLiteral(Short.valueOf((short)42)));
        Node ai = NodeFactory.createLiteral(LiteralLabelFactory.createTypedLiteral(Integer.valueOf(42)));
        Node al = NodeFactory.createLiteral(LiteralLabelFactory.createTypedLiteral(Long.valueOf(42)));

        Node SB = NodeCreateUtils.create("SB");
        Node SS = NodeCreateUtils.create("SS");
        Node SI = NodeCreateUtils.create("SI");
        Node SL = NodeCreateUtils.create("SL");
        Node P = NodeCreateUtils.create("P");

        g.add(Triple.create(SB, P, ab));
        g.add(Triple.create(SS, P, as));
        g.add(Triple.create(SI, P, ai));
        g.add(Triple.create(SL, P, al));

        assertEquals( 4,
                      Iter.toSet(g.find(Node.ANY, P, NodeCreateUtils.create("42"))).size(),
                      String.format("Should have found 4 elements, does %s really implement literal typing", g.getClass()) );
    }

    @Test
    public void test_Contains_Node_Node_Node_ByValue() {
        Node x = GraphTestLib.node("x");
        Node P = GraphTestLib.node("P");
        Graph g1 = GraphTestLib.graphWith("x P '1'xsd:integer");
        txnRun(g1, () -> {
            assertTrue(g1.contains(x, P, GraphTestLib.node("'01'xsd:int")),
                       String.format("literal type equality failed, does %s really implement literal typing", g1.getClass()));
        });
        //
        Graph g2 = GraphTestLib.graphWith("x P '1'xsd:int");
        txnRun(g2, () -> {
            assertTrue(g2.contains(x, P, GraphTestLib.node("'1'xsd:integer")),"Literal equality with '1'xsd:integer failed");
        });
        //
        Graph g3 = GraphTestLib.graphWith("x P '123'xsd:string");
        txnRun(g3, () -> {
            assertTrue(g3.contains(x, P, GraphTestLib.node("'123'")), "Literal equality with '123' failed");
        });
    }

    @Test
    public void test_Contains_Triple_ByValue() {
        Graph g1 = GraphTestLib.graphWith("x P '1'xsd:integer");
        txnRun(g1, () -> {
            assertTrue(
                       g1.contains(GraphTestLib.triple("x P '01'xsd:int")),
                       String.format("did not find x P '01'xsd:int, does %s really implement literal typing", g1.getClass())
                    );
        });
        //
        Graph g2 = GraphTestLib.graphWith("x P '1'xsd:int");
        txnRun(g2, () -> {
            assertTrue( g2.contains(GraphTestLib.triple("x P '1'xsd:integer")), "did not find x P '1'xsd:integer" );
        });
        //
        Graph g3 = GraphTestLib.graphWith("x P '123'xsd:string");
        txnRun(g3, () -> assertTrue(g3.contains(GraphTestLib.triple("x P '123'")), "did not find x P '123'xsd:string" ));
    }

    @Test
    public void test_Find_Triple_ProgrammaticValues() {
        Graph g = getNewGraph();
            @SuppressWarnings("deprecation")
            Node ab = NodeFactory.createLiteral(LiteralLabelFactory
                    .createTypedLiteral(Byte.valueOf((byte) 42)));
            @SuppressWarnings("deprecation")
            Node as = NodeFactory.createLiteral(LiteralLabelFactory
                    .createTypedLiteral(Short.valueOf((short) 42)));
            @SuppressWarnings("deprecation")
            Node ai = NodeFactory.createLiteral(
                    LiteralLabelFactory.createTypedLiteral(Integer.valueOf(42)));
            @SuppressWarnings("deprecation")
            Node al = NodeFactory.createLiteral(
                    LiteralLabelFactory.createTypedLiteral(Long.valueOf(42)));

        Node SB = NodeCreateUtils.create("SB");
        Node SS = NodeCreateUtils.create("SS");
        Node SI = NodeCreateUtils.create("SI");
        Node SL = NodeCreateUtils.create("SL");
        Node P = NodeCreateUtils.create("P");

            txnBegin(g);
            try
            {
                g.add(Triple.create(SB, P, ab));
                g.add(Triple.create(SS, P, as));
                g.add(Triple.create(SI, P, ai));
                g.add(Triple.create(SL, P, al));
            } catch (Exception e)
            {
                txnRollback(g);
                fail(e.getMessage());
            }
            txnCommit(g);
            txnBegin(g);
            assertEquals(4, Iter.toSet(g.find(Triple.create(Node.ANY, P, NodeCreateUtils.create("42")))).size(),
                         String.format("Should have found 4 elements, does %s really implement literal typing", g.getClass()));
            txnRollback(g);
        }

    @Test
    public void test_Find_Triple_MatchLanguagedLiteralCaseInsensitive() {
        Graph g = GraphTestLib.graphWith("a p 'chat'en");
        Node chaten = GraphTestLib.node("'chat'en"), chatEN = GraphTestLib.node("'chat'EN");
        assertEquals(chaten, chatEN);
        assertTrue(chaten.sameValueAs(chatEN));
        assertEquals(chaten.getIndexingValue(), chatEN.getIndexingValue());
        txnBegin(g);
        assertEquals(1, g.find(Triple.create(Node.ANY, Node.ANY, chaten)).toList().size());
        assertEquals(1, g.find(Triple.create(Node.ANY, Node.ANY, chatEN)).toList().size());
        txnRollback(g);
    }

    private void literalTypingBasedFindTest(final String data, final int size, final String search, final String results,
                                            boolean reqLitType) {
        if ( !reqLitType ) {
            Graph g = GraphTestLib.graphWith(data);

            Node literal = NodeCreateUtils.create(search);
            //
            txnBegin(g);
            assertEquals(size, g.size(), "graph has wrong size");
            Set<Node> got = g.find(Node.ANY, Node.ANY, literal).mapWith(t -> t.getObject()).toSet();
            assertEquals(GraphTestLib.nodeSet(results), got);
            txnRollback(g);
        }
    }

    @Test
    public void testLiteralTypingBasedFind() {
        literalTypingBasedFindTest("a P 'simple'", 1, "'simple'", "'simple'", false);
        literalTypingBasedFindTest("a P 'simple'xsd:string", 1, "'simple'", "'simple'xsd:string", true);
        literalTypingBasedFindTest("a P 'simple'", 1, "'simple'xsd:string", "'simple'", true);
        // ensure that adding identical strings one with type yields single result
        // and that querying with or without type works
        literalTypingBasedFindTest("a P 'simple'xsd:string", 1, "'simple'xsd:string", "'simple'xsd:string", false);
        literalTypingBasedFindTest("a P 'simple'; a P 'simple'xsd:string", 1, "'simple'", "'simple'xsd:string", true);
        literalTypingBasedFindTest("a P 'simple'; a P 'simple'xsd:string", 1, "'simple'xsd:string", "'simple'", true);
        literalTypingBasedFindTest("a P 'simple'; a P 'simple'xsd:string", 1, "'simple'", "'simple'", true);
        literalTypingBasedFindTest("a P 'simple'; a P 'simple'xsd:string", 1, "'simple'xsd:string", "'simple'xsd:string", true);
        literalTypingBasedFindTest("a P 1", 1, "1", "1", false);
        literalTypingBasedFindTest("a P '1'xsd:float", 1, "'1'xsd:float", "'1'xsd:float", false);
        literalTypingBasedFindTest("a P '1'xsd:double", 1, "'1'xsd:double", "'1'xsd:double", false);
        literalTypingBasedFindTest("a P '1'xsd:float", 1, "'1'xsd:float", "'1'xsd:float", false);
        literalTypingBasedFindTest("a P '1.1'xsd:float", 1, "'1'xsd:float", "", false);
        literalTypingBasedFindTest("a P '1'xsd:double", 1, "'1'xsd:int", "", false);
        literalTypingBasedFindTest("a P 'abc'rdf:XMLLiteral", 1, "'abc'", "", false);
        literalTypingBasedFindTest("a P 'abc'", 1, "'abc'rdf:XMLLiteral", "", false);
        //
        // floats & doubles are not compatible
        //
        literalTypingBasedFindTest("a P '1'xsd:float", 1, "'1'xsd:double", "", false);
        literalTypingBasedFindTest("a P '1'xsd:double", 1, "'1'xsd:float", "", false);
        literalTypingBasedFindTest("a P 1", 1, "'1'", "", false);
        literalTypingBasedFindTest("a P 1", 1, "'1'xsd:integer", "'1'xsd:integer", false);
        literalTypingBasedFindTest("a P 1", 1, "'1'", "", false);
        literalTypingBasedFindTest("a P '1'xsd:short", 1, "'1'xsd:integer", "'1'xsd:short", true);
        literalTypingBasedFindTest("a P '1'xsd:int", 1, "'1'xsd:integer", "'1'xsd:int", true);
    }

    @Test
    public void testBrokenIndexes() {
        Graph g = getGraphWith("x R y; x S z");
        ExtendedIterator<Triple> it = g.find(Node.ANY, Node.ANY, Node.ANY);
        it.removeNext();
        it.removeNext();
        assertFalse(g.find(GraphTestLib.node("x"), Node.ANY, Node.ANY).hasNext());
        assertFalse(g.find(Node.ANY, GraphTestLib.node("R"), Node.ANY).hasNext());
        assertFalse(g.find(Node.ANY, Node.ANY, GraphTestLib.node("y")).hasNext());
    }

    @Test
    public void testBrokenSubject() {
        Graph g = getGraphWith("x brokenSubject y");
        ExtendedIterator<Triple> it = g.find(GraphTestLib.node("x"), Node.ANY, Node.ANY);
        it.removeNext();
        assertFalse(g.find(Node.ANY, Node.ANY, Node.ANY).hasNext());
    }

    @Test
    public void testBrokenPredicate() {
        Graph g = getGraphWith("x brokenPredicate y");
        ExtendedIterator<Triple> it = g.find(Node.ANY, GraphTestLib.node("brokenPredicate"), Node.ANY);
        it.removeNext();
        assertFalse(g.find(Node.ANY, Node.ANY, Node.ANY).hasNext());
    }

    @Test
    public void testBrokenObject() {
        Graph g = getGraphWith("x brokenObject y");
        ExtendedIterator<Triple> it = g.find(Node.ANY, Node.ANY, GraphTestLib.node("y"));
        it.removeNext();
        assertFalse(g.find(Node.ANY, Node.ANY, Node.ANY).hasNext());
    }
}
