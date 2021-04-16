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

package org.apache.jena.rdfs;

import static org.apache.jena.rdfs.engine.ConstRDFS.ANY;
import static org.apache.jena.rdfs.engine.ConstRDFS.rdfType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.other.G;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.sse.SSE;
import org.junit.Test;

/**
 * Specific detailed tests.
 */
public class TestMiscRDFS {

    private static Graph vocabSC = SSE.parseGraph("(graph (:S rdfs:subClassOf :T))");
    private static Graph dataSC = SSE.parseGraph("(graph (:x rdf:type :S))");

    private static Graph vocabDomain = SSE.parseGraph("(graph (:p rdfs:domain :D) )");
    private static Graph dataDomain = SSE.parseGraph("(graph (:x :p 123))");

    private static Graph vocabDomainSC = SSE.parseGraph("(graph (:p rdfs:domain :R) (:R rdfs:subClassOf :T) )");
    private static Graph dataDomainSC = SSE.parseGraph("(graph (:x :p 123))");

    private static Graph vocabRangeSC = SSE.parseGraph("(graph (:q rdfs:domain :R) (:p rdfs:domain :R0) (:R rdfs:subClassOf :T) )");
    private static Graph dataRangeSC = SSE.parseGraph("(graph (:x :p 123) (:y :q :z))");

    private static Node node(String str) { return SSE.parseNode(str);}
    private static void exactlyOne(Graph graph, Node s, Node p, Node o) { G.getOne(graph, s, p, o); }
    private static long count(Graph graph, Node s, Node p, Node o) { return Iter.count(graph.find(s, p, o)); }
    private static long count(DatasetGraph dsg, Node g, Node s, Node p, Node o) { return Iter.count(dsg.find(g, s, p, o)); }

    @Test public void emptyDataEmptyVocab() {
        Graph vocab = GraphFactory.createDefaultGraph();
        Graph data = GraphFactory.createDefaultGraph();
        Graph rdfs = RDFSFactory.graphRDFS(data, vocab);
        assertTrue(rdfs.isEmpty());
        assertFalse(rdfs.contains(ANY, ANY, ANY));
        assertEquals(0, count(rdfs, ANY, ANY, ANY));
    }

    @Test public void domainOnly() {
        Graph rdfs = RDFSFactory.graphRDFS(dataDomain, vocabDomain);
        exactlyOne(rdfs, null, rdfType, node(":D"));
        assertEquals(2, count(rdfs, ANY, ANY, ANY));
    }

    @Test public void domainST() {
        Graph rdfs = RDFSFactory.graphRDFS(dataDomainSC, vocabDomainSC);
        exactlyOne(rdfs, null, rdfType, node(":T"));
        exactlyOne(rdfs, null, rdfType, node(":R"));
        exactlyOne(rdfs, null, null, node(":R"));
        assertEquals(3, count(rdfs, ANY, ANY, ANY));
    }

    @Test public void rangeST() {
        Graph rdfs = RDFSFactory.graphRDFS(dataRangeSC, vocabRangeSC);
        exactlyOne(rdfs, null, rdfType, node(":T"));
        exactlyOne(rdfs, null, rdfType, node(":R"));
        exactlyOne(rdfs, null, null, node(":R"));
        assertEquals(5, count(rdfs, ANY, ANY, ANY));
    }

    @Test public void dataset() {
        Graph vocab = SSE.parseGraph("(graph (:S rdfs:subClassOf :T))");
        DatasetGraph dsg0 = DatasetGraphFactory.createTxnMem();
        DatasetGraph dsg = RDFSFactory.datasetRDFS(dsg0, vocab);

        dsg.add(Quad.defaultGraphIRI, node(":s0"), rdfType, node(":S"));
        assertEquals(0, count(dsg, Quad.unionGraph, ANY, rdfType, ANY));
        assertEquals(2, count(dsg, ANY, ANY, rdfType, ANY));

        dsg.add(node(":g1"), node(":s1"), rdfType, node(":S"));
        dsg.add(node(":g2"), node(":s2"), rdfType, node(":S"));

        assertEquals(4, count(dsg, Quad.unionGraph, ANY, rdfType, ANY));
        assertEquals(2, count(dsg, Quad.defaultGraphIRI, ANY, rdfType, ANY));
        assertEquals(6, count(dsg, ANY, ANY, rdfType, ANY));
        assertEquals(3, count(dsg0, ANY, ANY, rdfType, ANY));

        assertEquals(1, Iter.count(dsg.findNG(node(":g1"), null, null, node(":T"))));
        assertEquals(2, Iter.count(dsg.findNG(node(":g1"), node(":s1"), rdfType, ANY)));

        Graph g = dsg.getDefaultGraph();

        exactlyOne(g, ANY, ANY, node(":S"));
        exactlyOne(g, ANY, ANY, node(":T"));

    }
}
