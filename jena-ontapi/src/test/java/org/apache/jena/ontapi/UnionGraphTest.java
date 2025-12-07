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

package org.apache.jena.ontapi;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphMemFactory;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.ontapi.impl.UnionGraphImpl;
import org.apache.jena.ontapi.model.OntModel;
import org.apache.jena.ontapi.testutils.ModelTestUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.shared.AddDeniedException;
import org.apache.jena.shared.ClosedException;
import org.apache.jena.shared.DeleteDeniedException;
import org.apache.jena.shared.JenaException;
import org.apache.jena.sparql.graph.GraphReadOnly;
import org.apache.jena.sparql.graph.GraphWrapper;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;

/**
 * To test {@link UnionGraph}.
 */
@SuppressWarnings("WeakerAccess")
public class UnionGraphTest {

    static Graph createNamedGraph(String uri) {
        OntModel m = OntModelFactory.createModel();
        m.setID(uri);
        return m.getBaseGraph();
    }

    static Graph createTestMemGraph(String name) {
        return GraphMemFactory.createDefaultGraph();
    }

    private static void assertClosed(UnionGraph g, boolean expectedClosed) {
        if (expectedClosed) {
            Assertions.assertTrue(g.isClosed());
            Assertions.assertTrue(g.getBaseGraph().isClosed());
            return;
        }
        Assertions.assertFalse(g.isClosed());
        Assertions.assertFalse(g.getBaseGraph().isClosed());
    }

    @Test
    public void testAddRemoveSubGraphs() {
        UnionGraph a = new UnionGraphImpl(createNamedGraph("a"));
        Graph b = createNamedGraph("b");
        a.addSubGraph(b);
        UnionGraph c = new UnionGraphImpl(createNamedGraph("c"));
        a.addSubGraph(c);
        UnionGraph d = new UnionGraphImpl(createNamedGraph("d"));
        c.addSubGraph(d);
        String tree = ModelTestUtils.importsTreeAsString(a);

        Assertions.assertEquals(4, tree.split("\n").length);
        d.addSubGraph(b);
        tree = ModelTestUtils.importsTreeAsString(a);

        Assertions.assertEquals(5, tree.split("\n").length);
        // recursion:
        d.addSubGraph(c);
        tree = ModelTestUtils.importsTreeAsString(a);

        Assertions.assertEquals(6, tree.split("\n").length);

        Graph h = createNamedGraph("H");
        c.addSubGraph(h);
        a.removeSubGraph(b);
        a.addSubGraph(b = new UnionGraphImpl(b));
        ((UnionGraph) b).addSubGraph(h);
        tree = ModelTestUtils.importsTreeAsString(a);

        Assertions.assertEquals(8, tree.split("\n").length);

        // remove recursion:
        d.removeSubGraph(c);
        tree = ModelTestUtils.importsTreeAsString(a);

        Assertions.assertEquals(7, tree.split("\n").length);
    }

    @Test
    public void testWrapAsUnmodified() {
        Triple a = Triple.create(NodeFactory.createURI("a"), RDF.Nodes.type, OWL2.Class.asNode());
        Triple b = Triple.create(NodeFactory.createURI("b"), RDF.Nodes.type, OWL2.Class.asNode());

        Graph base = GraphMemFactory.createDefaultGraph();
        base.getPrefixMapping().setNsPrefixes(OntModelFactory.STANDARD);
        base.add(a);
        Graph unmodified = new GraphReadOnly(base);
        Assertions.assertEquals(1, unmodified.find().toSet().size());
        Assertions.assertEquals(4, unmodified.getPrefixMapping().numPrefixes());

        UnionGraph u = new UnionGraphImpl(unmodified);
        Assertions.assertEquals(4, u.getPrefixMapping().numPrefixes());

        try {
            u.getPrefixMapping().setNsPrefix("x", "http://x#");
            Assertions.fail("Possible to add prefix");
        } catch (JenaException lj) {
            // expected
        }

        Assertions.assertEquals(4, u.getPrefixMapping().numPrefixes());
        try {
            u.add(b);
            Assertions.fail("Possible to add triple");
        } catch (AddDeniedException aj) {
            // expected
        }
        try {
            u.delete(a);
            Assertions.fail("Possible to delete triple");
        } catch (DeleteDeniedException dj) {
            // expected
        }
        Assertions.assertEquals(1, unmodified.find().toSet().size());

        base.add(b);
        base.getPrefixMapping().setNsPrefix("x", "http://x#").setNsPrefix("y", "http://y#");
        Assertions.assertEquals(2, u.find().toSet().size());
        Assertions.assertEquals(6, u.getPrefixMapping().numPrefixes());
    }

    @Test
    public void testCloseRecursiveGraph() {
        UnionGraph a = new UnionGraphImpl(GraphMemFactory.createDefaultGraph());
        UnionGraph b = new UnionGraphImpl(GraphMemFactory.createDefaultGraph());
        UnionGraph c = new UnionGraphImpl(GraphMemFactory.createDefaultGraph());
        UnionGraph d = new UnionGraphImpl(GraphMemFactory.createDefaultGraph());
        UnionGraph e = new UnionGraphImpl(GraphMemFactory.createDefaultGraph());
        assertClosed(a, false);
        assertClosed(b, false);
        assertClosed(c, false);
        assertClosed(d, false);
        assertClosed(e, false);

        c.addSubGraph(a);
        b.addSubGraph(c);
        c.addSubGraph(b).addSubGraph(d).addSubGraph(e);
        a.addSubGraph(c);

        c.close();
        assertClosed(a, true);
        assertClosed(b, true);
        assertClosed(c, true);
        assertClosed(d, true);
        assertClosed(e, true);
    }

    @Test
    public void testCloseHierarchyGraph() {
        UnionGraphImpl a = new UnionGraphImpl(GraphMemFactory.createDefaultGraph());
        UnionGraphImpl b = new UnionGraphImpl(GraphMemFactory.createDefaultGraph());
        UnionGraphImpl c = new UnionGraphImpl(GraphMemFactory.createDefaultGraph());
        assertClosed(a, false);
        assertClosed(b, false);
        assertClosed(c, false);

        a.addSubGraph(b.addSubGraph(c));

        b.close();
        assertClosed(b, true);
        assertClosed(c, true);
        assertClosed(a, false);

        UnionGraphImpl d = new UnionGraphImpl(GraphMemFactory.createDefaultGraph());
        try {
            b.addSubGraph(d);
            Assertions.fail("Possible to add a sub-graph");
        } catch (ClosedException ce) {
            // expected
        }
        try {
            b.removeSubGraph(c);
            Assertions.fail("Possible to remove a sub-graph");
        } catch (ClosedException ce) {
            // expected
        }
        Assertions.assertNotNull(a.addSubGraph(d));
        Assertions.assertEquals(4, a.listSubGraphBases().toList().size());

        Assertions.assertNotNull(a.removeSubGraph(b));
        Assertions.assertEquals(2, a.listSubGraphBases().toList().size());
    }

    @Test
    public void testListBaseGraphs1() {
        Graph a = createTestMemGraph("a");
        Graph b = createTestMemGraph("b");
        Graph c = createTestMemGraph("c");
        UnionGraphImpl u1 = new UnionGraphImpl(a);
        UnionGraphImpl u2 = new UnionGraphImpl(b);
        UnionGraphImpl u3 = new UnionGraphImpl(c);
        u1.addSubGraph(u1);
        u1.addSubGraph(u2);
        u1.addSubGraph(u3);
        u1.addSubGraph(b);
        Assertions.assertEquals(new HashSet<>(Arrays.asList(a, b, c)), u1.listSubGraphBases().toSet());
    }

    @Test
    public void testListBaseGraphs2() {
        Graph a = createTestMemGraph("a");
        Graph b = createTestMemGraph("b");
        Graph c = createTestMemGraph("c");
        Graph d = createTestMemGraph("d");
        UnionGraphImpl u1 = new UnionGraphImpl(new UnionGraphImpl(a).addSubGraph(d));
        UnionGraphImpl u2 = new UnionGraphImpl(b);
        UnionGraphImpl u3 = new UnionGraphImpl(new UnionGraphImpl(c));
        u1.addSubGraph(u1);
        u1.addSubGraph(u2);
        u1.addSubGraph(u3);
        u1.addSubGraph(b);
        Assertions.assertEquals(new HashSet<>(Arrays.asList(a, b, c, d)), u1.listSubGraphBases().toSet());
    }

    @Test
    public void testListParents() {
        UnionGraph a = new UnionGraphImpl(createNamedGraph("A"));
        UnionGraph b = new UnionGraphImpl(createNamedGraph("B"));
        UnionGraph c = new UnionGraphImpl(createNamedGraph("C"));
        UnionGraph d = new UnionGraphImpl(createNamedGraph("D"));
        UnionGraph e = new UnionGraphImpl(createNamedGraph("E"));

        a.addSubGraph(b);
        a.addSubGraph(c);
        b.addSubGraph(d);
        a.addSubGraph(d);
        d.addSubGraph(e);
        e.addSubGraph(a);
        e.addSubGraph(e);

        Assertions.assertEquals(List.of(e), a.superGraphs().collect(Collectors.toList()));
        Assertions.assertEquals(List.of(a), b.superGraphs().collect(Collectors.toList()));
        Assertions.assertEquals(List.of(a), c.superGraphs().collect(Collectors.toList()));
        Assertions.assertEquals(Set.of(a, b), d.superGraphs().collect(Collectors.toSet()));
        Assertions.assertEquals(Set.of(d, e), e.superGraphs().collect(Collectors.toSet()));
    }

    @Test
    public void testGraphCycleImportsWithConnector() {
        Model mA = OntModelFactory.createDefaultModel(createNamedGraph("a"));
        Model mB = OntModelFactory.createDefaultModel(createNamedGraph("b"));
        Model mC = OntModelFactory.createDefaultModel(createNamedGraph("c"));
        Model mD = OntModelFactory.createDefaultModel(createNamedGraph("d"));
        Model mE = OntModelFactory.createDefaultModel(createNamedGraph("e"));

        UnionGraph a = new UnionGraphImpl(mA.getGraph());
        UnionGraph b = new UnionGraphImpl(mB.getGraph());
        UnionGraph c = new UnionGraphImpl(mC.getGraph());
        UnionGraph d = new UnionGraphImpl(mD.getGraph());
        UnionGraph e = new UnionGraphImpl(mE.getGraph());

        UnionGraph wA = new UnionGraphImpl(new GraphWrapper(mA.getGraph()));
        UnionGraph wB = new UnionGraphImpl(new GraphWrapper(mB.getGraph()));
        UnionGraph wC = new UnionGraphImpl(new GraphWrapper(mC.getGraph()));

        UnionGraphConnector.connect(a, wA);
        UnionGraphConnector.connect(b, wB);
        UnionGraphConnector.connect(c, wC);

        a.addSubGraph(b);
        b.addSubGraph(a);
        b.addSubGraph(c);
        Assertions.assertEquals(1, a.subGraphs().count());
        Assertions.assertEquals(2, b.subGraphs().count());
        Assertions.assertEquals(0, c.subGraphs().count());

        mA.createResource("A", OWL2.Class);
        Assertions.assertEquals(4, a.stream().count());
        Assertions.assertEquals(4, b.stream().count());
        Assertions.assertEquals(1, c.stream().count());

        mB.createResource("B", OWL2.Class);
        Assertions.assertEquals(5, a.stream().count());
        Assertions.assertEquals(5, b.stream().count());
        Assertions.assertEquals(1, c.stream().count());

        mC.createResource("C", OWL2.Class);
        Assertions.assertEquals(6, a.stream().count());
        Assertions.assertEquals(6, b.stream().count());
        Assertions.assertEquals(2, c.stream().count());

        Assertions.assertEquals(6, wA.stream().count());
        Assertions.assertEquals(6, wB.stream().count());
        Assertions.assertEquals(2, wC.stream().count());

        a.addSubGraph(d);
        Assertions.assertEquals(7, a.stream().count());
        Assertions.assertEquals(7, b.stream().count());
        Assertions.assertEquals(2, c.stream().count());
        Assertions.assertEquals(7, wA.stream().count());
        Assertions.assertEquals(7, wB.stream().count());
        Assertions.assertEquals(2, wC.stream().count());

        wB.addSubGraph(e);
        Assertions.assertEquals(8, a.stream().count());
        Assertions.assertEquals(8, b.stream().count());
        Assertions.assertEquals(2, c.stream().count());
        Assertions.assertEquals(8, wA.stream().count());
        Assertions.assertEquals(8, wB.stream().count());
        Assertions.assertEquals(2, wC.stream().count());

        wB.removeSubGraph(e);
        Assertions.assertEquals(7, a.stream().count());
        Assertions.assertEquals(7, b.stream().count());
        Assertions.assertEquals(2, c.stream().count());
        Assertions.assertEquals(7, wA.stream().count());
        Assertions.assertEquals(7, wB.stream().count());
        Assertions.assertEquals(2, wC.stream().count());
    }

    static class UnionGraphConnector extends UnionGraphImpl.EventManagerImpl {

        private final UnionGraph connection;

        UnionGraphConnector(UnionGraph connection) {
            this.connection = connection;
        }

        public static void connect(UnionGraph a, UnionGraph b) {
            if (a.getEventManager().listeners(UnionGraphConnector.class).noneMatch(it -> b.equals(it.connection))) {
                a.getEventManager().register(new UnionGraphConnector(b));
            }
            if (b.getEventManager().listeners(UnionGraphConnector.class).noneMatch(it -> a.equals(it.connection))) {
                b.getEventManager().register(new UnionGraphConnector(a));
            }
        }

        @Override
        public void notifySubGraphAdded(UnionGraph graph, Graph subGraph) {
            if (connection.subGraphs().noneMatch(subGraph::equals)) {
                connection.addSubGraph(subGraph);
            }
            super.notifySubGraphAdded(graph, subGraph);
        }

        @Override
        public void notifySubGraphRemoved(UnionGraph graph, Graph subGraph) {
            if (connection.subGraphs().anyMatch(subGraph::equals)) {
                connection.removeSubGraph(subGraph);
            }
            super.notifySubGraphRemoved(graph, subGraph);
        }
    }
}
