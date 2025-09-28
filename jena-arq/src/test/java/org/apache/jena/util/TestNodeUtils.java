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

package org.apache.jena.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.NodeUtils;

public class TestNodeUtils {

    private static final Node g = SSE.parseNode(":g");
    private static final Node s = SSE.parseNode(":s");
    private static final Node p = SSE.parseNode(":p");
    private static final Node o = SSE.parseNode(":o");

    @Test public void valid_triple_01() { testValidityTriple(true, "(:s :p :o)"); }
    @Test public void valid_triple_02() { testValidityTriple(true, Triple.create(s, p, o)); }
    @Test public void valid_triple_03() { testValidityTriple(true, "(:s :p 123)"); }
    @Test public void valid_triple_04() { testValidityTriple(true, "(:s :p _:b)"); }
    @Test public void valid_triple_05() { testValidityTriple(true, "(_:b :p :o)"); }

    // RDF 1.2
    //@Test public void valid_triple_06() { testValidityTriple(true, "(:s :p <<( :x :y :x)>>)"); }


    @Test public void invalid_triple_01() { testValidityTriple(false, "('literal' :p :o)"); }
    @Test public void invalid_triple_02() { testValidityTriple(false, "(:s 'literal' :o)"); }
    @Test public void invalid_triple_03() { testValidityTriple(false, "(:s :p ?var)"); }
    @Test public void invalid_triple_04() { testValidityTriple(false, "(:s ?var :o)"); }
    @Test public void invalid_triple_05() { testValidityTriple(false, "(?var :p :o)"); }

    // _ is Node.ANY for triples.

    @Test public void invalid_triple_10() { testValidityTriple(false, "(_ :p :o)"); }
    @Test public void invalid_triple_11() { testValidityTriple(false, "(:s _ :o)"); }
    @Test public void invalid_triple_12() { testValidityTriple(false, "(:s :p _)"); }

    private void testValidityTriple(boolean expected, String triple) {
        testValidityTriple(expected, SSE.parseTriple(triple));
    }

    private void testValidityTriple(boolean expected, Triple triple) {
        Node s = triple.getSubject(), p = triple.getPredicate(), o = triple.getObject();
        assertEquals(expected, NodeUtils.isValidAsRDF(s, p, o));
    }

    @Test public void concrete_triple_in_quad() { assertTrue(Quad.create(null, s, p, o).isConcrete()); }

    @Test public void valid_quad_01() { testValidityQuad(true, "(:g :s :p :o)"); }
    @Test public void valid_quad_02() { testValidityQuad(true, Quad.create(g, s, p, o)); }

    @Test public void valid_quad_03() { testValidityQuad(true, "(:g :s :p _:b)"); }
    @Test public void valid_quad_04() { testValidityQuad(true, "(:g _:b :p :o)"); }
    @Test public void valid_quad_05() { testValidityQuad(true, "(_:b :s :p :o)"); }
    @Test public void valid_quad_06() { testValidityQuad(true, "(:g :s :p 'literal')"); }

    // _ in the graph position is null/default graph
    @Test public void valid_quad_09() { testValidityQuad(true, "(_ :s :p :o)"); }

    // RDF 1.2
    //@Test public void valid_quad_06() { testValidityQuad(true, "(:s :p <<( :x :y :x)>>)"); }

    @Test public void invalid_quad_01() { testValidityQuad(false, "(:g :s 'literal' :o)"); }
    @Test public void invalid_quad_02() { testValidityQuad(false, "(:g 'literal' :p :o)"); }
    @Test public void invalid_quad_03() { testValidityQuad(false, "('literal' :s :p :o)"); }

    @Test public void invalid_quad_04() { testValidityQuad(false, "(:g :s :p ?var)"); }
    @Test public void invalid_quad_05() { testValidityQuad(false, "(:g :s ?var :o)"); }
    @Test public void invalid_quad_06() { testValidityQuad(false, "(:g ?var :p :o)"); }
    @Test public void invalid_quad_07() { testValidityQuad(false, "(?var :s :p :o)"); }

    @Test public void invalid_quad_10() { testValidityQuad(false, "(:g _ :p :o)"); }
    @Test public void invalid_quad_11() { testValidityQuad(false, "(:g :s _ :o)"); }
    @Test public void invalid_quad_12() { testValidityQuad(false, "(:g :s :p _)"); }

    private void testValidityQuad(boolean expected, String quad) {
        testValidityQuad(expected, SSE.parseQuad(quad));
    }

    private void testValidityQuad(boolean expected, Quad quad) {
        Node g = quad.getGraph(), s = quad.getSubject(), p = quad.getPredicate(), o = quad.getObject();
        assertEquals(expected, NodeUtils.isValidAsRDF(g, s, p, o));
    }


}
