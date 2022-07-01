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

import static org.apache.jena.atlas.iterator.Iter.iter;
import static org.apache.jena.graph.Node.ANY;
import static org.apache.jena.rdfs.LibTestRDFS.node;
import static org.apache.jena.rdfs.engine.ConstRDFS.rdfType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.atlas.lib.ListUtils;
import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.sse.SSE;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests of {@link DatasetGraphRDFS}.
 * <p>
 * Because inference is "per graph", we are only testing the DatasetGraph addition
 * machinery assuming graph inference is correct.
 */
public class TestDatasetGraphRDFS {

    private static PrintStream out = System.out;
    private static DatasetGraphRDFS dsg;

    @BeforeClass
    public static void beforeClass() {
        String x = StrUtils.strjoinNL
                ("(dataset"
                ,"   (_ :z rdf:type :A)"
                ,"   (:g  :a rdf:type :A)"
                ,")"
                );
        DatasetGraph dsgBase = SSE.parseDatasetGraph(x);
        Graph schema = SSE.parseGraph("(graph (:A rdfs:subClassOf :B))");
        dsg = (DatasetGraphRDFS)RDFSFactory.datasetRDFS(dsgBase, schema);

    }

    @Test public void dsg_access_1() {
        Iterator<Quad> iter1  = dsg.find(null, node("a"), rdfType, null);
        Iter.consume(iter1);
        Iterator<Quad> iter2 = dsg.find(ANY, node("a"), rdfType, null);
        Iter.consume(iter2);
        Iterator<Quad> iter3  = dsg.getWrapped().find(null, node("a"), rdfType, null);
        Iter.consume(iter3);
    }

    @Test public void dsg_find_graph() {
        List<Quad> x = test(node("g"), node("a"), rdfType, null);
        assertTrue(hasNG(x, node("g"))) ;
    }

    @Test public void dsg_find_graph_none() {
        List<Quad> x = test(node("g0"), node("a"), rdfType, null) ;
        assertTrue(x.isEmpty());
    }

    @Test public void dsg_find_graph_null() {
        List<Quad> x = test(null, node("a"), rdfType, null);
        assertTrue(!x.isEmpty());
        assertTrue(hasNG(x, node("g")));
        assertFalse(hasNG(x, Node.ANY));
        assertFalse(hasNG(x, null));
    }

    @Test public void dsg_find_graph_any() {
        List<Quad> x = test(ANY, node("a"), rdfType, null);
        assertTrue(!x.isEmpty());
        assertTrue(hasNG(x, node("g")));
        assertFalse(hasNG(x, Node.ANY));
        assertFalse(hasNG(x, null));
    }

    @Test public void dsg_find_union() {
        List<Quad> x = test(Quad.unionGraph, node("a"), rdfType, null);
        assertTrue(!x.isEmpty());
        assertTrue(hasNG(x, Quad.unionGraph));
        assertFalse(hasNG(x, node("g")));
        assertFalse(hasNG(x, Node.ANY));
        assertFalse(hasNG(x, null));
    }

    @Test public void dsg_contains_1() {
        testContains(node("g"), node("a"), rdfType, null, true) ;
    }

    @Test public void dsg_contains_2() {
        testContains(node("gNode"), node("a"), rdfType, null, false) ;
    }

    @Test public void dsg_contains_3() {
        testContains(null, node("a"), rdfType, null, true) ;
    }

    @Test public void dsg_contains_4() {
        testContains(ANY, node("a"), rdfType, null, true) ;
    }

    private static boolean hasNG(List<Quad> quads, Node graphName) {
        return quads.stream().map(Quad::getGraph).anyMatch(gn -> Lib.equals(gn, graphName));
    }

    private void testContains(Node g, Node s, Node p, Node o, boolean expected) {
        boolean actual = dsg.contains(g, s, p, o);
        assertEquals("Contains: e="+expected+", a="+actual, expected, actual);
    }

    private List<Quad> test(Node g, Node s, Node p, Node o) {
        List<Quad> actual = Iter.toList(dsg.find(g, s, p, o));
        List<Quad> expected = evalByGraph(g, s, p, o);
        boolean b = ListUtils.equalsUnordered(expected, actual);
        if ( ! b ) {
            out.println("Fail: find("+g+", "+s+", "+p+", "+o+")");
            LibTestRDFS.printDiff(out, expected, actual);
        }
        assertTrue(b);
        return actual;
    }

    private List<Quad> evalByGraph(Node g, Node s, Node p, Node o) {
        List<Quad> acc = new ArrayList<>();
        if ( g != null && g.isConcrete() ) {
            oneGraph(acc, g, s, p, o);
        } else {
            dsg.listGraphNodes().forEachRemaining(gn -> oneGraph(acc, gn, s, p, o));
            // Default graph.
            oneGraph(acc, Quad.defaultGraphIRI, s, p, o);
        }
        return acc;
    }

    private static void oneGraph(List<Quad> acc, Node g, Node s, Node p, Node o) {
        if ( ! g.isConcrete()  )
            throw new IllegalStateException();
        iter(dsg.getGraph(g).find(s,p,o)).map(t->Quad.create(g, t)).forEachRemaining(acc::add);
    }
}
