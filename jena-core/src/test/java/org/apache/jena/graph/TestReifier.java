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

package org.apache.jena.graph;

import java.lang.reflect.Constructor;

import junit.framework.TestSuite;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.junit.NodeCreateUtils;
import org.apache.jena.mem.GraphMemFast;
import org.apache.jena.rdf.model.impl.ReifierStd;
import org.apache.jena.shared.AlreadyReifiedException;
import org.apache.jena.shared.CannotReifyException;
import org.apache.jena.shared.JenaException;
import org.apache.jena.test.JenaTestBase;
import org.apache.jena.test.JenaTestLib;
import org.apache.jena.vocabulary.RDF;

/**
 * This class tests the reifiers of ordinary graphs. Old test suite - kept to ensure
 * compatibility for the one and only Standard mode
 */
public class TestReifier extends JenaTestBase {
    protected final Class<? extends Graph> graphClass;

    public TestReifier(String name) {
        super(name);
        graphClass = null;
    }

    public TestReifier(Class<? extends Graph> graphClass, String name) {
        super(name);
        this.graphClass = graphClass;
    }

    private Graph getGraph() {
        try {
            Constructor<? > cons = JenaTestLib.getConstructor(graphClass, new Class[]{});
            if ( cons != null )
                return (Graph)cons.newInstance();
            Constructor<? > cons2 = JenaTestLib.getConstructor(graphClass, new Class[]{this.getClass()});
            if ( cons2 != null )
                return (Graph)cons2.newInstance(this);
            throw new JenaException("no suitable graph constructor found for " + graphClass);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new JenaException(e);
        }
    }

    private final Graph getGraphWith(String facts) {
        Graph result = getGraph();
        GraphTestLib.graphAdd(result, facts);
        return result;
    }

    /**
     * Answer the empty graph if cond is false, otherwise the graph with the given
     * facts.
     */
    protected final Graph graphWithUnless(boolean cond, String facts) {
        return GraphTestLib.graphWith(cond ? "" : facts);
    }

    protected final Graph graphWithIf(boolean cond, String facts) {
        return graphWithUnless(!cond, facts);
    }

    public void testGetGraphNotNull() {
        assertNotNull(getGraph());
    }

    /**
     * Check that the standard reifier will note, but not hide, reification quads.
     */
    public void testStandard() {
        Graph g = getGraph();
        assertFalse(ReifierStd.hasTriple(g, GraphTestLib.triple("s p o")));
        g.add(NodeCreateUtils.createTriple("x rdf:subject s"));
        assertEquals(1, g.size());
        g.add(NodeCreateUtils.createTriple("x rdf:predicate p"));
        assertEquals(2, g.size());
        g.add(NodeCreateUtils.createTriple("x rdf:object o"));
        assertEquals(3, g.size());
        g.add(NodeCreateUtils.createTriple("x rdf:type rdf:Statement"));
        assertEquals(4, g.size());
        assertTrue(ReifierStd.hasTriple(g, GraphTestLib.triple("s p o")));
    }

    /**
     * Test that the Standard reifier will expose implicit quads arising from
     * reifyAs().
     */
    public void testStandardExplode() {
        Graph g = getGraph();
        ReifierStd.reifyAs(g, GraphTestLib.node("a"), GraphTestLib.triple("p Q r"));
        Graph r = GraphMemFactory.createDefaultGraph();
        GraphTestLib.graphAdd(r, "a rdf:type rdf:Statement; a rdf:subject p; a rdf:predicate Q; a rdf:object r");
        assertEquals(4, g.size());
        GraphTestLib.assertIsomorphic(r, g);
    }

    /**
     * Ensure that over-specifying a reification means that we don't get a triple
     * back. Goodness knows why this test wasn't in right from the beginning.
     */
    public void testOverspecificationSuppressesReification() {
        Graph g = getGraph();
        GraphTestLib.graphAdd(g, "x rdf:subject A; x rdf:predicate P; x rdf:object O; x rdf:type rdf:Statement");
        assertEquals(GraphTestLib.triple("A P O"), ReifierStd.getTriple(g, GraphTestLib.node("x")));
        GraphTestLib.graphAdd(g, "x rdf:subject BOOM");
        assertEquals(null, ReifierStd.getTriple(g, GraphTestLib.node("x")));
    }

    public void testReificationSubjectClash() {
        testReificationClash("x rdf:subject SS");
    }

    public void testReificationPredicateClash() {
        testReificationClash("x rdf:predicate PP");
    }

    public void testReificationObjectClash() {
        testReificationClash("x rdf:object OO");
    }

    protected void testReificationClash(String clashingStatement) {
        Graph g = getGraph();
        Triple SPO = NodeCreateUtils.createTriple("S P O");
        ReifierStd.reifyAs(g, GraphTestLib.node("x"), SPO);
        assertTrue(ReifierStd.hasTriple(g, SPO));
        GraphTestLib.graphAdd(g, clashingStatement);
        assertEquals(null, ReifierStd.getTriple(g, GraphTestLib.node("x")));
        assertFalse(ReifierStd.hasTriple(g, SPO));
    }

    /**
     * Test that reifying a triple explicitly has some effect on the graph only for
     * Standard reifiers.
     */
    public void testManifestQuads() {
        Graph g = getGraph();
        ReifierStd.reifyAs(g, GraphTestLib.node("A"), GraphTestLib.triple("S P O"));
        String reified = "A rdf:type rdf:Statement; A rdf:subject S; A rdf:predicate P; A rdf:object O";
        GraphTestLib.assertIsomorphic(GraphTestLib.graphWith(reified), g);
    }

    public void testHiddenVsReification() {
        Graph g = getGraph();
        ReifierStd.reifyAs(g, GraphTestLib.node("A"), GraphTestLib.triple("S P O"));
        assertTrue(ReifierStd.findEither(g, Triple.ANY, false).hasNext());
    }

    public void testRetrieveTriplesByNode() {
        Graph G = getGraph();
        Node N = NodeFactory.createBlankNode(), M = NodeFactory.createBlankNode();
        ReifierStd.reifyAs(G, N, GraphTestLib.triple("x R y"));
        assertEquals("gets correct triple", GraphTestLib.triple("x R y"), ReifierStd.getTriple(G, N));
        ReifierStd.reifyAs(G, M, GraphTestLib.triple("p S q"));
        JenaTestLib.assertDiffer("the anon nodes must be distinct", N, M);
        assertEquals("gets correct triple", GraphTestLib.triple("p S q"), ReifierStd.getTriple(G, M));

        assertTrue("node is known bound", ReifierStd.hasTriple(G, M));
        assertTrue("node is known bound", ReifierStd.hasTriple(G, N));
        assertFalse("node is known unbound", ReifierStd.hasTriple(G, NodeFactory.createURI("any:thing")));
    }

    public void testRetrieveTriplesByTriple() {
        Graph G = getGraph();
        Triple T = GraphTestLib.triple("x R y"), T2 = GraphTestLib.triple("y R x");
        Node N = GraphTestLib.node("someNode");
        ReifierStd.reifyAs(G, N, T);
        assertTrue("R must have T", ReifierStd.hasTriple(G, T));
        assertFalse("R must not have T2", ReifierStd.hasTriple(G, T2));
    }

    public void testReifyAs() {
        Graph G = getGraph();
        Node X = NodeFactory.createURI("some:uri");
        assertEquals("node used", X, ReifierStd.reifyAs(G, X, GraphTestLib.triple("x R y")));
        assertEquals("retrieves correctly", GraphTestLib.triple("x R y"), ReifierStd.getTriple(G, X));
    }

    public void testAllNodes() {
        Graph G = getGraph();
        ReifierStd.reifyAs(G, GraphTestLib.node("x"), GraphTestLib.triple("cows eat grass"));
        ReifierStd.reifyAs(G, GraphTestLib.node("y"), GraphTestLib.triple("pigs can fly"));
        ReifierStd.reifyAs(G, GraphTestLib.node("z"), GraphTestLib.triple("dogs may bark"));
        assertEquals("", GraphTestLib.nodeSet("z y x"), Iter.toSet(ReifierStd.allNodes(G)));
    }

    public void testRemoveByNode() {
        Graph G = getGraph();
        Node X = GraphTestLib.node("x"), Y = GraphTestLib.node("y");
        ReifierStd.reifyAs(G, X, GraphTestLib.triple("x R a"));
        ReifierStd.reifyAs(G, Y, GraphTestLib.triple("y R a"));
        ReifierStd.remove(G, X, GraphTestLib.triple("x R a"));
        assertFalse("triple X has gone", ReifierStd.hasTriple(G, X));
        assertEquals("triple Y still there", GraphTestLib.triple("y R a"), ReifierStd.getTriple(G, Y));
    }

    public void testRemoveFromNothing() {
        Graph G = getGraph();
        G.delete(GraphTestLib.triple("quint rdf:subject S"));
    }

    public void testException() {
        Graph G = getGraph();
        Node X = GraphTestLib.node("x");
        ReifierStd.reifyAs(G, X, GraphTestLib.triple("x R y"));
        ReifierStd.reifyAs(G, X, GraphTestLib.triple("x R y"));
        try {
            ReifierStd.reifyAs(G, X, GraphTestLib.triple("x R z"));
            fail("did not detect already reified node");
        } catch (AlreadyReifiedException e) {}
    }

    public void testKevinCaseA() {
        Graph G = getGraph();
        Node X = GraphTestLib.node("x"), a = GraphTestLib.node("a"), b = GraphTestLib.node("b"), c = GraphTestLib.node("c");
        G.add(Triple.create(X, RDF.Nodes.type, RDF.Nodes.Statement));
        ReifierStd.reifyAs(G, X, Triple.create(a, b, c));
    }

    public void testKevinCaseB() {
        Graph G = getGraph();
        Node X = GraphTestLib.node("x"), Y = GraphTestLib.node("y");
        Node a = GraphTestLib.node("a"), b = GraphTestLib.node("b"), c = GraphTestLib.node("c");
        G.add(Triple.create(X, RDF.Nodes.subject, Y));
        try {
            ReifierStd.reifyAs(G, X, Triple.create(a, b, c));
            fail("X already has subject Y: cannot make it a");
        } catch (CannotReifyException e) {
            JenaTestLib.pass();
        }
    }

    public void testQuadRemove() {
        Graph g = getGraph();
        assertEquals(0, g.size());
        Triple s = NodeCreateUtils.createTriple("x rdf:subject s");
        Triple p = NodeCreateUtils.createTriple("x rdf:predicate p");
        Triple o = NodeCreateUtils.createTriple("x rdf:object o");
        Triple t = NodeCreateUtils.createTriple("x rdf:type rdf:Statement");
        g.add(s);
        g.add(p);
        g.add(o);
        g.add(t);
        assertEquals(4, g.size());
        g.delete(s);
        g.delete(p);
        g.delete(o);
        g.delete(t);
        assertEquals(0, g.size());
    }

    public void testEmpty() {
        Graph g = getGraph();
        assertTrue(g.isEmpty());
        GraphTestLib.graphAdd(g, "x rdf:type rdf:Statement");
        assertFalse(g.isEmpty());
        GraphTestLib.graphAdd(g, "x rdf:subject Deconstruction");
        assertFalse(g.isEmpty());
        GraphTestLib.graphAdd(g, "x rdf:predicate rdfs:subTypeOf");
        assertFalse(g.isEmpty());
        GraphTestLib.graphAdd(g, "x rdf:object LiteraryCriticism");
        assertFalse(g.isEmpty());
    }

    public void testReifierEmptyFind() {
        Graph g = getGraph();
        assertEquals(GraphTestLib.tripleSet(""), ReifierStd.findExposed(g, Triple.ANY).toSet());
    }

    public void testReifierFindSubject() {
        testReifierFind("x rdf:subject S");
    }

    public void testReifierFindObject() {
        testReifierFind("x rdf:object O");
    }

    public void testReifierFindPredicate() {
        testReifierFind("x rdf:predicate P");
    }

    public void testReifierFindComplete() {
        testReifierFind("x rdf:predicate P; x rdf:subject S; x rdf:object O; x rdf:type rdf:Statement");
    }

    public void testReifierFindFilter() {
        Graph g = getGraph();
        GraphTestLib.graphAdd(g, "s rdf:subject S");
        assertEquals(GraphTestLib.tripleSet(""), ReifierStd.findExposed(g, GraphTestLib.triple("s otherPredicate S")).toSet());
    }

    protected void testReifierFind(String triples) {
        testReifierFind(triples, "?? ?? ??");
    }

    protected void testReifierFind(String triples, String pattern) {
        Graph g = getGraph();
        GraphTestLib.graphAdd(g, triples);
        assertEquals(GraphTestLib.tripleSet(triples), ReifierStd.findExposed(g, GraphTestLib.triple(pattern)).toSet());
    }

    public static TestSuite suite() {
        TestSuite result = new TestSuite();
        result.addTest(MetaTestGraph.suite(TestReifier.class, GraphMemFast.class));
        result.setName(TestReifier.class.getSimpleName());
        return result;
    }

}
