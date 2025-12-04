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

package org.apache.jena.sparql.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.sse.SSE;

public class TestQuad {
    private static Node g1 = SSE.parseNode(":g1");
    private static Node s1 = SSE.parseNode(":s1");
    private static Node p1 = SSE.parseNode(":p1");
    private static Node o1 = SSE.parseNode(":o1");

    private static Node g2 = SSE.parseNode(":g2");
    private static Node s2 = SSE.parseNode(":s2");
    private static Node p2 = SSE.parseNode(":p2");
    private static Node o2 = SSE.parseNode(":o2");


    @Test public void quad_01() {
        Quad quad = Quad.create(g1, s1, p1, o1);
        assertEquals(g1, quad.getGraph());
        assertEquals(s1, quad.getSubject());
        assertEquals(p1, quad.getPredicate());
        assertEquals(o1, quad.getObject());
    }

    @Test public void quad_02() {
        Quad quad = Quad.create(null, s1, p1, o1);
        assertEquals(null, quad.getGraph());
        assertThrows(UnsupportedOperationException.class, ()->Quad.create(g1, null, p1, o1));
        assertThrows(UnsupportedOperationException.class, ()->Quad.create(g1, s1, null, o1));
        assertThrows(UnsupportedOperationException.class, ()->Quad.create(g1, s1, p1, null));
    }

    @Test public void quad_03() {
        Quad quad = Quad.createMatch(null, null, null, null);
        assertEquals(null, quad.getGraph());
        assertEquals(Node.ANY, quad.getSubject());
        assertEquals(Node.ANY, quad.getPredicate());
        assertEquals(Node.ANY, quad.getObject());
    }

    @Test public void quad_match_01() {
        Quad quad = Quad.create(g1, s1, p1, o1);
        Quad mq = Quad.create(g1, s1, p1, o1);
        assertTrue(mq.matches(quad));
        assertTrue(quad.matches(mq));
    }

    @Test public void quad_match_02() {
        Quad quad1 = Quad.create(g1, s1, p1, o1);
        Quad mq = Quad.create(g2, s2, p2, o2);
        assertFalse(mq.matches(quad1));
        assertFalse(quad1.matches(mq));
    }

    @Test public void quad_match_10() {
        Quad quad1 = Quad.create(g1, s1, p1, o1);
        Quad mq = Quad.create(Node.ANY, s1, p1, o1);
        assertTrue(mq.matches(quad1));
        assertTrue(quad1.matches(mq));
    }

    @Test public void quad_match_11() {
        Quad quad1 = Quad.create(g1, s1, p1, o1);
        Quad mq = Quad.create(g1, Node.ANY, p1, o1);
        assertTrue(mq.matches(quad1));
        assertTrue(quad1.matches(mq));
    }

    @Test public void quad_match_12() {
        Quad quad1 = Quad.create(g1, s1, p1, o1);
        Quad mq = Quad.create(g1, s1, Node.ANY, o1);
        assertTrue(mq.matches(quad1));
        assertTrue(quad1.matches(mq));
    }

    @Test public void quad_match_13() {
        Quad quad1 = Quad.create(g1, s1, p1, o1);
        Quad mq = Quad.create(g1, s1, p1, Node.ANY);
        assertTrue(mq.matches(quad1));
        assertTrue(quad1.matches(mq));
    }

    @Test public void quad_match_20() {
        Quad quad1 = Quad.create(g1, s1, p1, o1);
        Quad mq = Quad.create(Node.ANY, s2, p1, o1);
        assertFalse(mq.matches(quad1));
        assertFalse(quad1.matches(mq));
    }

    @Test public void quad_match_21() {
        Quad quad1 = Quad.create(g1, s1, p1, o1);
        Quad mq = Quad.create(g2, Node.ANY, p1, o1);
        assertFalse(mq.matches(quad1));
        assertFalse(quad1.matches(mq));
    }

    @Test public void quad_match_22() {
        Quad quad1 = Quad.create(g1, s1, p1, o1);
        Quad mq = Quad.create(g1, s1, Node.ANY, o2);
        assertFalse(mq.matches(quad1));
        assertFalse(quad1.matches(mq));
    }

    @Test public void quad_match_23() {
        Quad quad1 = Quad.create(g1, s1, p1, o1);
        Quad mq = Quad.create(g2, s1, p1, Node.ANY);
        assertFalse(mq.matches(quad1));
        assertFalse(quad1.matches(mq));
    }
}
