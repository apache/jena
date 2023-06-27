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

import static org.junit.Assert.*;

import org.apache.jena.graph.*;
import org.junit.Test;

/** Tests for {@link Node_Triple} and other unusual nodes */
public class TestNodeExtras {

    private static Node s = NodeFactory.createBlankNode();
    private static Node p = NodeCreateUtils.create("eg:p");
    private static Node o = NodeCreateUtils.create("'abc'");

    private static Triple triple1 = Triple.create(s,p,o);
    private static Triple triple2 = Triple.create(s,p,o);

    private static Triple triple9 = Triple.create(NodeFactory.createBlankNode(),p,o);

    private static Node_Triple newTripleTerm(Triple triple)             { return new Node_Triple(triple); }

    private static Node_Triple newTripleTerm(Node s, Node p , Node o)   { return new Node_Triple(s,p,o); }

    private static Node_Graph newGraphTerm(Graph graph)                 { return new Node_Graph(graph); }

    private static Node_Graph newGraphTerm()                            { return new Node_Graph(GraphMemFactory.empty()); }

     @Test public void term_triple_1() {
        Node nt = newTripleTerm(s,p,o);
        assertTrue(nt.isNodeTriple());
        assertNotNull(nt.getTriple());
        assertNotNull(nt.getTriple());
        assertEquals(triple1, nt.getTriple());

        assertEquals(nt, nt);
        assertNotEquals(nt.getTriple().hashCode(), nt.hashCode());
        assertTrue(nt.sameValueAs(nt));
    }

    @Test public void term_triple_2() {
        Node nt1 = newTripleTerm(s,p,o);
        Node nt2 = newTripleTerm(s,p,o);

        assertEquals(nt1, nt2);
        assertEquals(nt1.hashCode(), nt2.hashCode());
        assertTrue(nt1.sameValueAs(nt2));
    }

    @Test public void term_triple_3() {
        Node nt1 = newTripleTerm(triple1);
        Node nt2 = newTripleTerm(triple2);
        assertNotSame(nt1.getTriple(), nt2.getTriple());
        assertNotSame(nt1, nt2);
        assertEquals(nt1, nt2);
        assertEquals(nt1.hashCode(), nt2.hashCode());
    }

    @Test public void term_triple_4() {
        Node nt1 = newTripleTerm(triple1);
        Node nt9 = newTripleTerm(triple9);
        assertNotSame(nt1.getTriple(), nt9.getTriple());
        assertNotSame(nt1, nt9);
        assertNotEquals(nt1, nt9);
        assertFalse(nt1.sameValueAs(nt9));
    }

    @Test(expected=UnsupportedOperationException.class)
    public void term_triple_bad_1() {
        Node n = NodeFactory.createLiteral("abc");
        n.getTriple();
    }

    @Test(expected=UnsupportedOperationException.class)
    public void term_triple_bad_2() {
        Node n = NodeFactory.createURI("http://example/abc");
        n.getTriple();
    }

    @Test
    public void term_graph_1() {
        Node nGraph = newGraphTerm();
        assertTrue(nGraph.isNodeGraph());
        assertNotNull(nGraph.getGraph());

        assertEquals(nGraph, nGraph);
        assertNotEquals(nGraph.getGraph().hashCode(), nGraph.hashCode());
        assertTrue(nGraph.sameValueAs(nGraph));
    }
}
