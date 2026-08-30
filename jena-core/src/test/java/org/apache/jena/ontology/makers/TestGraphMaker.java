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

package org.apache.jena.ontology.makers;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphTestLib;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.ontology.models.GraphMaker;
import org.apache.jena.ontology.models.SimpleGraphMaker;
import org.apache.jena.shared.AlreadyExistsException;
import org.apache.jena.shared.DoesNotExistException;
import org.apache.jena.test.JenaTestLib;

public class TestGraphMaker {

    static { JenaTestLib.setup(); }

    public GraphMaker getGraphMaker() {
        return new SimpleGraphMaker();
    }

    private GraphMaker gf;

    @BeforeEach
    public void setUp() {
        gf = getGraphMaker();
    }

    @AfterEach
    public void tearDown() {
        gf.close();
    }

    /**
     * A trivial test that getGraph delivers a proper graph, not cheating with null,
     * and that getGraph() "always" delivers the same Graph.
     */
    @Test
    public void testGetGraph() {
        Graph g1 = gf.getGraph();
        assertFalse(g1 == null, "should deliver a Graph");
        assertSame(g1, gf.getGraph());
        g1.close();
    }

    @Test
    public void testCreateGraph() {
        JenaTestLib.assertDiffer("each created graph must differ", gf.createGraph(), gf.createGraph());
    }

    @Test
    public void testAnyName() {
        gf.createGraph("plain").close();
        gf.createGraph("with.dot").close();
        gf.createGraph("http://electric-hedgehog.net/topic#marker").close();
    }

    /**
     * Test that we can't create a graph with the same name twice.
     */
    @Test
    public void testCannotCreateTwice() {
        String name = jName("bonsai");
        gf.createGraph(name, true);
        assertThrows(AlreadyExistsException.class,
                     () -> gf.createGraph(name, true),
                     "should not be able to create " + name + " twice");
    }

    private String jName(String name) {
        return "jena-test-AbstractTestGraphMaker-" + name;
    }

    @Test
    public void testCanCreateTwice() {
        String name = jName("bridge");
        Graph g1 = gf.createGraph(name, true);
        Graph g2 = gf.createGraph(name, false);
        assertTrue(sameGraph(g1, g2), "graphs should be the same");
        Graph g3 = gf.createGraph(name);
        assertTrue(sameGraph(g1, g3), "graphs should be the same");
    }

    /**
     * Test that we cannot open a graph that does not exist.
     */
    @Test
    public void testCannotOpenUncreated() {
        String name = jName("noSuchGraph");
        assertThrows(DoesNotExistException.class,
                     () -> gf.openGraph(name, true),
                     name + " should not exist");
    }

    /**
     * Test that we *can* open a graph that hasn't been created
     */
    @Test
    public void testCanOpenUncreated() {
        String name = jName("willBeCreated");
        Graph g1 = gf.openGraph(name);
        g1.close();
        gf.openGraph(name, true);
    }

    /**
     * Utility - test that a graph with the given name exists.
     */
    private void testExists(String name) {
        assertTrue(gf.hasGraph(name), name + " should exist");
    }

    /**
     * Utility - test that no graph with the given name exists.
     */
    private void testDoesNotExist(String name) {
        assertFalse(gf.hasGraph(name), name + " should exist");
    }

    /**
     * Test that we can find a graph once its been created. We need to know if two
     * graphs are "the same" here: we have a temporary work-around but it is not
     * sound.
     */
    @Test
    public void testCanFindCreatedGraph() {
        String alpha = jName("alpha"), beta = jName("beta");
        Graph g1 = gf.createGraph(alpha, true);
        Graph h1 = gf.createGraph(beta, true);
        Graph g2 = gf.openGraph(alpha, true);
        Graph h2 = gf.openGraph(beta, true);
        assertTrue(sameGraph(g1, g2), "should find alpha");
        assertTrue(sameGraph(h1, h2), "should find beta");
    }

    /**
     * Weak test for "same graph": adding this to one is visible in t'other. Stopgap
     * for use in testCanFindCreatedGraph. TODO: clean that test up (left over from
     * RDB days)
     */
    private boolean sameGraph(Graph g1, Graph g2) {
        Node S = GraphTestLib.node("S"), P = GraphTestLib.node("P"), O = GraphTestLib.node("O");
        g1.add(Triple.create(S, P, O));
        g2.add(Triple.create(O, P, S));
        return g2.contains(S, P, O) && g1.contains(O, P, S);
    }

    /**
     * Test that we can remove a graph from the factory without disturbing another
     * graph's binding.
     */
    @Test
    public void testCanRemoveGraph() {
        String alpha = jName("bingo"), beta = jName("brillo");
        gf.createGraph(alpha, true);
        gf.createGraph(beta, true);
        testExists(alpha);
        testExists(beta);
        gf.removeGraph(alpha);
        testExists(beta);
        testDoesNotExist(alpha);
    }

    @Test
    public void testHasnt() {
        assertFalse(gf.hasGraph("john"), "no such graph");
        assertFalse(gf.hasGraph("paul"), "no such graph");
        assertFalse(gf.hasGraph("george"), "no such graph");
        /* */
        gf.createGraph("john", true);
        assertTrue(gf.hasGraph("john"), "john now exists");
        assertFalse(gf.hasGraph("paul"), "no such graph");
        assertFalse(gf.hasGraph("george"), "no such graph");
        /* */
        gf.createGraph("paul", true);
        assertTrue(gf.hasGraph("john"), "john still exists");
        assertTrue(gf.hasGraph("paul"), "paul now exists");
        assertFalse(gf.hasGraph("george"), "no such graph");
        /* */
        gf.removeGraph("john");
        assertFalse(gf.hasGraph("john"), "john has been removed");
        assertTrue(gf.hasGraph("paul"), "paul still exists");
        assertFalse(gf.hasGraph("george"), "no such graph");
    }

    // Up to Jena5, the graph created did open/close counting.
    // But only some graph implements provided this.

    @Test
    public void testCarefulClose() {
        Graph x = gf.createGraph("x");
        Graph y = gf.openGraph("x");
        x.add(GraphTestLib.triple("a BB c"));
        x.close();
        y.add(GraphTestLib.triple("p RR q"));
        y.close();
    }

    /**
     * Test that a maker with no graphs lists no names.
     */
    @Test
    public void testListNoGraphs() {
        Set<String> s = gf.listGraphs().toSet();
        if ( s.size() > 0 )
            fail("found names from 'empty' graph maker: " + s);
    }

    /**
     * Test that a maker with three graphs inserted lists those three grapsh; we
     * don't mind what order they appear in. We also use funny names to ensure that
     * the spelling that goes in is the one that comes out [should really be in a
     * separate test].
     */
    @Test
    public void testListThreeGraphs() {
        String x = "x", y = "y/sub", z = "z:boo";
        Graph X = gf.createGraph(x);
        Graph Y = gf.createGraph(y);
        Graph Z = gf.createGraph(z);
        Set<String> wanted = JenaTestLib.setOfStrings(x + " " + y + " " + z);
        assertEquals(wanted, Iter.toSet(gf.listGraphs()));
        X.close();
        Y.close();
        Z.close();
    }

    /**
     * Test that a maker with some things put in and then some removed gets the right
     * things listed.
     */
    @Test
    public void testListAfterDelete() {
        String x = "x_y", y = "y//zub", z = "a:b/c";
        Graph X = gf.createGraph(x);
        Graph Y = gf.createGraph(y);
        Graph Z = gf.createGraph(z);
        gf.removeGraph(x);
        Set<String> s = Iter.toSet(gf.listGraphs());
        assertEquals(JenaTestLib.setOfStrings(y + " " + z), s);
        X.close();
        Y.close();
        Z.close();
    }

}
