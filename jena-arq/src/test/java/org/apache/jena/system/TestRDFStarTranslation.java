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

package org.apache.jena.system;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.function.Function;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.IsoMatcher;
import org.apache.jena.vocabulary.RDF;
import org.junit.Test;

public class TestRDFStarTranslation {

    private static final Node s = SSE.parseNode(":s");
    private static final Node p = SSE.parseNode(":p");
    private static final Node o = SSE.parseNode(":o");
    private static final Node q = SSE.parseNode(":q");
    private static final Node z = SSE.parseNode(":z");
    private static final Node a = SSE.parseNode(":a");

    private static final Node rdfSubject = RDF.Nodes.subject;
    private static final Node rdfPredicate = RDF.Nodes.predicate;
    private static final Node rdfObject = RDF.Nodes.object;

    private static boolean isomorphic(Graph g1, Graph g2) {
        // Copes with embedded triples.
        return IsoMatcher.isomorphic(g1, g2);
    }

    @Test public void rdfx_basic() {
        // No RDF-star triple terms - no change.
        Graph g = data("(graph (:s :p :o))");

        Graph g1 = RDFStar.encodeAsRDF(g);
        assertTrue(isomorphic(g, g1));
        assertEquals(1, g1.size());

        Graph g2 = RDFStar.encodeAsRDF(g1);
        assertTrue(isomorphic(g, g2));
        assertEquals(1, g2.size());
    }

    @Test public void rdfx_01() {
        // One term
        Graph graph = data("(graph (<<:s :p :o>> :q :z) (:s1 :p1 :o1) )");
        testEncode(graph, 5, (g)-> G.getOnePO(g, q, z));
    }

    @Test public void rdfx_02() {
        // One term, used twice, subj/subj
        Graph graph = data("(graph (<<:s :p :o>> :q :z) (<<:s :p :o>> :q2 :z2) )");
        testEncode(graph, 5, (g)-> G.getOnePO(g, q, z));
    }

    @Test public void rdfx_03() {
        // One term, used twice, subj/obj
        Graph graph = data("(graph (<<:s :p :o>> :q :z) (:a :q <<:s :p :o>>) )");
        testEncode(graph, 5, (g)-> G.getOnePO(g, q, z));
    }

    @Test public void rdfx_04() {
        // One term, used subj/obj same triple.
        Graph graph = data("(graph (<<:s :p :o>> :q <<:s :p :o>>) (<<:s :p :o>> :q :z) )");
        testEncode(graph, 5, (g)-> G.getOnePO(g, q, z));
    }

    @Test public void rdfx_05() {
        // Two terms
        Graph graph = data("(graph (<<:s :p :o>> :q <<:s1 :p1 :o1>>) (<<:s :p :o>> :q :z) )");
        testEncode(graph, 8, (g)-> G.getOnePO(g, q, z));
    }

    @Test public void rdfx_10() {
        // One term
        testEncodeDecode("(graph (<<:s :p :o>> :q :z) (:s1 :p1 :o1) )");
    }

    @Test public void rdfx_11() {
        // One term, used twice, subj/subj
        testEncodeDecode("(graph (<<:s :p :o>> :q :z) (<<:s :p :o>> :q2 :z2) )");
    }

    @Test public void rdfx_12() {
        // One term, used twice, subj/obj
        testEncodeDecode("(graph (<<:s :p :o>> :q :z) (:a :q <<:s :p :o>>) )");
    }

    @Test public void rdfx_13() {
        // One term, used subj/obj same triple.
        testEncodeDecode("(graph (<<:s :p :o>> :q <<:s :p :o>>) (<<:s :p :o>> :q :z) )");
    }

    @Test public void rdfx_14() {
        // Two terms, used subj/obj same triple.
        testEncodeDecode("(graph (<<:s :p :o>> :q <<:s1 :p1 :o1>>) (<<:s :p :o>> :q :z) )");
    }

    @Test public void rdfx_15() {
        testEncodeDecode("(graph (<< <<:s :p :o>> :r :z>> :q :a) )");
    }

    @Test public void rdfx_18() {
        String data = StrUtils.strjoinNL
            ("(graph"
            ,"  (<<:s :p :o>> :q :z)"
            ,"  (<<:s :p :o>> :r <<:s :p :o>>)"
            ,"  (:a :q <<:s :p :o>>)"
            ,"  (:s :p :o)"
            ,")");
        testEncodeDecode(data);
    }

    @Test public void rdfx_19() {
        testEncodeDecode("(graph (<< <<:s :p :o>> :r <<:s1 :p1 :o1>>>> :q <<:s :p :o>>) )");
    }

    static Graph data(String dataStr) {
        Graph g = SSE.parseGraph(dataStr);
        g.getPrefixMapping().setNsPrefix("", "http://example/");
        g.getPrefixMapping().setNsPrefix("rdf", RDF.getURI());
        return g;
    }

    private void testEncodeDecode(String str) {
        Graph graph = data(str);
        Graph g1 = RDFStar.encodeAsRDF(graph);
        testNoTripleTerms(g1);
        Graph g2 = RDFStar.decodeFromRDF(g1);
        // check for no triple terms.
        boolean b = isomorphic(graph, g2);
        if ( ! b ) {
            System.out.println("-- Mismatch");
            RDFDataMgr.write(System.out, graph, RDFFormat.TURTLE_BLOCKS);
            System.out.println("-- Encode");
            RDFDataMgr.write(System.out, g1, RDFFormat.TURTLE_BLOCKS);
            System.out.println("-- Decode");
            RDFDataMgr.write(System.out, g2, RDFFormat.TURTLE_BLOCKS);
            System.out.println("-----");
        }

        assertTrue(b);
    }

    private void testNoTripleTerms(Graph graph) {
        assertFalse(G.find(graph, null, null, null).filterKeep(RDFStar::tripleHasNodeTriple).hasNext());
    }

    private void testEncode(Graph graph, int expectedSize, Function<Graph, Node> getReif) {
        Graph g1 = RDFStar.encodeAsRDF(graph);
        testNoTripleTerms(g1);
        //RDFDataMgr.write(System.out, g1, RDFFormat.TURTLE_BLOCKS);
        assertEquals("Encoded", expectedSize, g1.size());

        // Check there is the expected reification.
        Node reif = getReif.apply(g1);
        assertTrue(G.containsOne(g1, reif, rdfSubject, s));
        assertTrue(G.containsOne(g1, reif, rdfPredicate, p));
        assertTrue(G.containsOne(g1, reif, rdfObject, o));
    }

    private void testInPlace(String str) {
        Graph graph = data(str);

        Graph g1a = RDFStar.encodeAsRDF(graph);
        Graph g1 = RDFStar.encodeAsRDFInPlace(graph);

        // Decode inplace.

        assertSame(graph, g1);
        testNoTripleTerms(g1);

        boolean b = isomorphic(g1,g1a);
        if ( ! b ) {
            System.out.println("-- Mismatch");
            RDFDataMgr.write(System.out, data(str), RDFFormat.TURTLE_BLOCKS);
            System.out.println("-- Encode");
            RDFDataMgr.write(System.out, g1, RDFFormat.TURTLE_BLOCKS);
//            System.out.println("-- Decode");
//            RDFDataMgr.write(System.out, g2, RDFFormat.TURTLE_BLOCKS);
            System.out.println("-----");
        }
        assertTrue(b);
    }

    @Test public void rdfx_inplace_01() {
        // One term
        testInPlace("(graph (<<:s :p :o>> :q :z) (:s1 :p1 :o1) )");
    }

    @Test public void rdfx_inplace_02() {
        // One term, used twice, subj/subj
        testInPlace("(graph (<<:s :p :o>> :q :z) (<<:s :p :o>> :q2 :z2) )");
    }

    @Test public void rdfx_inplace_03() {
        // One term, used twice, subj/obj
        testInPlace("(graph (<<:s :p :o>> :q :z) (:a :q <<:s :p :o>>) )");
    }

    @Test public void rdfx_inplace_04() {
        // One term, used subj/obj same triple.
        testInPlace("(graph (<<:s :p :o>> :q <<:s :p :o>>) (<<:s :p :o>> :q :z) )");
    }

    @Test public void rdfx_inplace_05() {
        // Two terms
        testInPlace("(graph (<<:s :p :o>> :q <<:s1 :p1 :o1>>) (<<:s :p :o>> :q :z) )");
    }

    @Test public void rdfx_inplace_10() {
        // One term
        testInPlace("(graph (<<:s :p :o>> :q :z) (:s1 :p1 :o1) )");
    }

    @Test public void rdfx_inplace_11() {
        // One term, used twice, subj/subj
        testInPlace("(graph (<<:s :p :o>> :q :z) (<<:s :p :o>> :q2 :z2) )");
    }

    @Test public void rdfx_inplace_12() {
        // One term, used twice, subj/obj
        testInPlace("(graph (<<:s :p :o>> :q :z) (:a :q <<:s :p :o>>) )");
    }

    @Test public void rdfx_inplace_13() {
        // One term, used subj/obj same triple.
        testInPlace("(graph (<<:s :p :o>> :q <<:s :p :o>>) (<<:s :p :o>> :q :z) )");
    }

    @Test public void rdfx_inplace_14() {
        // Two terms, used subj/obj same triple.
        testInPlace("(graph (<<:s :p :o>> :q <<:s1 :p1 :o1>>) (<<:s :p :o>> :q :z) )");
    }

    @Test public void rdfx_inplace_15() {
        testInPlace("(graph (<< <<:s :p :o>> :r :z>> :q :a) )");
    }

    @Test public void rdfx_inplace_18() {
        String data = StrUtils.strjoinNL
            ("(graph"
            ,"  (<<:s :p :o>> :q :z)"
            ,"  (<<:s :p :o>> :r <<:s :p :o>>)"
            ,"  (:a :q <<:s :p :o>>)"
            ,"  (:s :p :o)"
            ,")");
        testInPlace(data);
    }

    @Test public void rdfx_inplace_19() {
        testEncodeDecode("(graph (<< <<:s :p :o>> :r <<:s1 :p1 :o1>>>> :q <<:s :p :o>>) )");
    }
}

