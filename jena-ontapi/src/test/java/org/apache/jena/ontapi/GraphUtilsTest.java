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

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.andrewoma.dexx.collection.Sets;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphMemFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.compose.MultiUnion;
import org.apache.jena.graph.impl.GraphBase;
import org.apache.jena.graph.impl.WrappedGraph;
import org.apache.jena.ontapi.impl.UnionGraphImpl;
import org.apache.jena.ontapi.testutils.ModelTestUtils;
import org.apache.jena.ontapi.utils.Graphs;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ModelGraphInterface;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.graph.GraphWrapper;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;

/**
 * To test {@link Graphs} utility class.
 */
public class GraphUtilsTest {

    // The test graph implementation choice.
    private static Graph createGraph() {
        return GraphMemFactory.createGraphMemForModel();
    }

    private static Stream<Graph> flat(Graph graph) {
        if (graph == null) return Stream.empty();
        return Stream.concat(Stream.of(Graphs.unwrap(graph)), Graphs.directSubGraphs(graph).flatMap(GraphUtilsTest::flat));
    }

    @Test
    public void testListBaseGraphs() {
        UnionGraph u = new UnionGraphImpl(UnionGraphTest.createTestMemGraph("a"));
        u.addSubGraph(UnionGraphTest.createTestMemGraph("b"));
        u.addSubGraph(UnionGraphTest.createTestMemGraph("c"));
        UnionGraph g2 = new UnionGraphImpl(UnionGraphTest.createTestMemGraph("d"));
        g2.addSubGraph(UnionGraphTest.createTestMemGraph("e"));
        u.addSubGraph(g2);
        u.addSubGraph(new WrappedGraph(UnionGraphTest.createTestMemGraph("x")));
        u.addSubGraph(new GraphWrapper(UnionGraphTest.createTestMemGraph("y")));

        Set<Graph> actual = Graphs.dataGraphs(u).collect(Collectors.toSet());
        Assertions.assertEquals(7, actual.size());
        Assertions.assertEquals(flat(u).collect(Collectors.toSet()), actual);
    }

    @Test
    public void testIsSized() {
        Assertions.assertTrue(Graphs.isSized(createGraph()));
        Assertions.assertTrue(Graphs.isSized(new UnionGraphImpl(createGraph())));
        Assertions.assertFalse(Graphs.isSized(new UnionGraphImpl(new GraphWrapper(createGraph()))));

        UnionGraph u1 = new UnionGraphImpl(createGraph());
        u1.addSubGraph(u1);
        Assertions.assertFalse(Graphs.isSized(u1));

        Graph g = new GraphBase() {
            @Override
            protected ExtendedIterator<Triple> graphBaseFind(Triple tp) {
                throw new AssertionError();
            }
        };
        Assertions.assertFalse(Graphs.isSized(g));
    }

    @Test
    public void testIsDistinct() {
        Assertions.assertTrue(Graphs.isDistinct(createGraph()));
        Assertions.assertTrue(Graphs.isDistinct(new UnionGraphImpl(createGraph())));
        Assertions.assertTrue(Graphs.isDistinct(new UnionGraphImpl(new GraphWrapper(createGraph()))));

        UnionGraph u1 = new UnionGraphImpl(createGraph(), false);
        Assertions.assertTrue(Graphs.isDistinct(u1));

        u1.addSubGraph(createGraph());
        Assertions.assertFalse(Graphs.isDistinct(u1));

        Graph g = new GraphBase() {
            @Override
            protected ExtendedIterator<Triple> graphBaseFind(Triple tp) {
                throw new AssertionError();
            }
        };
        Assertions.assertFalse(Graphs.isDistinct(g));
    }

    @Test
    public void testIsSame() {
        Graph g = GraphMemFactory.createDefaultGraph();
        Assertions.assertTrue(Graphs.isSameBase(g, g));

        Graph a = new UnionGraphImpl(g);
        Assertions.assertTrue(Graphs.isSameBase(a, g));

        MultiUnion b = new MultiUnion();
        b.addGraph(g);
        b.addGraph(createGraph());
        Assertions.assertTrue(Graphs.isSameBase(a, b));

        UnionGraph c1 = new UnionGraphImpl(new GraphWrapper(g));
        Assertions.assertTrue(Graphs.isSameBase(a, c1));

        UnionGraph c2 = new UnionGraphImpl(new WrappedGraph(g));
        Assertions.assertTrue(Graphs.isSameBase(a, c2));

        Graph g2 = GraphMemFactory.createDefaultGraph();
        Assertions.assertFalse(Graphs.isSameBase(g, g2));

        Graph d = new UnionGraphImpl(new WrappedGraph(new WrappedGraph(g)));
        Assertions.assertTrue(Graphs.isSameBase(a, d));

        Assertions.assertFalse(Graphs.isSameBase(new UnionGraphImpl(g), new UnionGraphImpl(createGraph())));

        MultiUnion e = new MultiUnion();
        e.addGraph(createGraph());
        e.addGraph(g);
        Assertions.assertFalse(Graphs.isSameBase(b, e));
    }

    @Test
    public void testCollectPrefixes() {
        Graph a = createGraph();
        Graph b = createGraph();
        Graph c = createGraph();
        a.getPrefixMapping().setNsPrefix("a1", "x1").setNsPrefix("a2", "x2");
        b.getPrefixMapping().setNsPrefix("b1", "x3");
        c.getPrefixMapping().setNsPrefix("b2", "x4");

        Assertions.assertEquals(4, Graphs.collectPrefixes(Arrays.asList(a, b, c)).numPrefixes());
        Assertions.assertEquals(3, Graphs.collectPrefixes(Arrays.asList(a, b)).numPrefixes());
        Assertions.assertEquals(2, Graphs.collectPrefixes(Arrays.asList(b, c)).numPrefixes());
        Assertions.assertEquals(1, Graphs.collectPrefixes(Collections.singleton(b)).numPrefixes());

        try {
            Graphs.collectPrefixes(Sets.of(b, c)).setNsPrefix("X", "x");
            Assertions.fail();
        } catch (PrefixMapping.JenaLockedException j) {
            // expected
        }
    }

    @Test
    public void testMakeUnionGraph() {
        String B = "b";
        String C = "c";
        String D = "d";
        String E = "e";
        String F = "f";
        String G = "g";
        String H = "h";
        String K = "k";

        Model mA = ModelFactory.createDefaultModel();
        Model mB = ModelFactory.createDefaultModel();
        Model mC = ModelFactory.createDefaultModel();
        Model mD = ModelFactory.createDefaultModel();
        Model mE = ModelFactory.createDefaultModel();
        Model mF = ModelFactory.createDefaultModel();
        Model mG = ModelFactory.createDefaultModel();
        Model mH = ModelFactory.createDefaultModel();
        Model mK = ModelFactory.createDefaultModel();

        //   a
        //  / \
        // c    b
        // . \
        // .   d
        // .   |
        // .   e
        // . / | \
        // с   g  f
        //       /
        //      h
        mA.createResource().addProperty(RDF.type, OWL2.Ontology)
                .addProperty(OWL2.imports, mA.createResource(B))
                .addProperty(OWL2.imports, mA.createResource(C));
        mB.createResource(B, OWL2.Ontology);
        mC.createResource(C, OWL2.Ontology).addProperty(OWL2.imports, mC.createResource(D));
        mD.createResource(D, OWL2.Ontology).addProperty(OWL2.imports, mD.createResource(E));
        mE.createResource(E, OWL2.Ontology)
                .addProperty(OWL2.imports, mE.createResource(F))
                .addProperty(OWL2.imports, mE.createResource(G))
                .addProperty(OWL2.imports, mE.createResource(C)); // cycle
        mF.createResource(F, OWL2.Ontology)
                .addProperty(OWL2.imports, mE.createResource(H));
        mG.createResource(G, OWL2.Ontology);
        mH.createResource(H, OWL2.Ontology);
        mK.createResource(K, OWL2.Ontology);

        UnionGraph actual = Graphs.makeOntUnion(
                mA.getGraph(),
                Stream.of(mA, mB, mC, mD, mE, mF, mG, mH, mK)
                        .map(ModelGraphInterface::getGraph)
                        .collect(Collectors.toSet()),
                UnionGraphImpl::new
        );

        Assertions.assertSame(mA.getGraph(), actual.getBaseGraph());
        Assertions.assertEquals(List.of(B, C), ModelTestUtils.getSubGraphsIris(actual));

        UnionGraph gB = (UnionGraph) ModelTestUtils.findSubGraphByIri(actual, B).orElseThrow();
        UnionGraph gC = (UnionGraph) ModelTestUtils.findSubGraphByIri(actual, C).orElseThrow();
        Assertions.assertSame(mB.getGraph(), gB.getBaseGraph());
        Assertions.assertSame(mC.getGraph(), gC.getBaseGraph());
        Assertions.assertFalse(gB.hasSubGraph());
        Assertions.assertEquals(List.of(D), ModelTestUtils.getSubGraphsIris(gC));

        UnionGraph gD = (UnionGraph) ModelTestUtils.findSubGraphByIri(gC, D).orElseThrow();
        Assertions.assertSame(mD.getGraph(), gD.getBaseGraph());
        Assertions.assertEquals(List.of(E), ModelTestUtils.getSubGraphsIris(gD));

        UnionGraph gE = (UnionGraph) ModelTestUtils.findSubGraphByIri(gD, E).orElseThrow();
        Assertions.assertSame(mE.getGraph(), gE.getBaseGraph());
        Assertions.assertEquals(List.of(C, F, G), ModelTestUtils.getSubGraphsIris(gE));

        UnionGraph gCofE = (UnionGraph) ModelTestUtils.findSubGraphByIri(gE, C).orElseThrow();
        UnionGraph gF = (UnionGraph) ModelTestUtils.findSubGraphByIri(gE, F).orElseThrow();
        UnionGraph gG = (UnionGraph) ModelTestUtils.findSubGraphByIri(gE, G).orElseThrow();
        Assertions.assertSame(gC, gCofE);
        Assertions.assertSame(mF.getGraph(), gF.getBaseGraph());
        Assertions.assertSame(mG.getGraph(), gG.getBaseGraph());
        Assertions.assertFalse(gG.hasSubGraph());
        Assertions.assertEquals(List.of(H), ModelTestUtils.getSubGraphsIris(gF));

        UnionGraph gH = (UnionGraph) ModelTestUtils.findSubGraphByIri(gF, H).orElseThrow();
        Assertions.assertSame(mH.getGraph(), gH.getBaseGraph());
        Assertions.assertFalse(gH.hasSubGraph());
    }

    @Test
    public void testIsOntUnionGraph() {
        String A = "a";
        String B = "b";
        String C = "c";
        String D = "d";
        String E = "e";

        Model mA = ModelFactory.createDefaultModel();
        Model mB = ModelFactory.createDefaultModel();
        Model mC = ModelFactory.createDefaultModel();
        Model mD = ModelFactory.createDefaultModel();
        Model mE = ModelFactory.createDefaultModel();
        mA.createResource(A, OWL2.Ontology)
                .addProperty(OWL2.imports, mA.createResource(B))
                .addProperty(OWL2.imports, mA.createResource(C))
                .addProperty(OWL2.imports, mD.createResource(E));
        mB.createResource(B, OWL2.Ontology);
        mC.createResource(C, OWL2.Ontology).addProperty(OWL2.imports, mC.createResource(D));
        mD.createResource(D, OWL2.Ontology);

        UnionGraph u = new UnionGraphImpl(mA.getGraph())
                .addSubGraph(new UnionGraphImpl(mB.getGraph()))
                .addSubGraph(new UnionGraphImpl(mC.getGraph())
                        .addSubGraph(new UnionGraphImpl(mD.getGraph())));

        Assertions.assertTrue(Graphs.isOntUnionGraph(u, false));

        u.addSubGraph(new UnionGraphImpl(mE.getGraph()));
        Assertions.assertFalse(Graphs.isOntUnionGraph(u, false));

        mE.createResource(E, OWL2.Ontology);
        Assertions.assertTrue(Graphs.isOntUnionGraph(u, false));
    }

    @Test
    public void testListAllGraphs() {
        UnionGraph a = new UnionGraphImpl(UnionGraphTest.createNamedGraph("A"));
        UnionGraph b = new UnionGraphImpl(UnionGraphTest.createNamedGraph("B"));
        UnionGraph c = new UnionGraphImpl(UnionGraphTest.createNamedGraph("C"));
        UnionGraph d = new UnionGraphImpl(UnionGraphTest.createNamedGraph("D"));
        UnionGraph e = new UnionGraphImpl(UnionGraphTest.createNamedGraph("E"));
        UnionGraph f = new UnionGraphImpl(UnionGraphTest.createNamedGraph("F"));

        //     a    f
        //  /  | \ /
        // |   b  c
        // | /
        // d
        //  \
        //   e
        //  / \
        // e   a
        a.addSubGraph(b);
        a.addSubGraph(c);
        a.addSubGraph(d);
        b.addSubGraph(d);
        d.addSubGraph(e);
        e.addSubGraph(a);
        e.addSubGraph(e);
        f.addSubGraph(c);

        Stream.of(a, b, c, d, e, f).forEach(graph -> {
            List<UnionGraph> actual = Graphs.flatHierarchy(a)
                    .sorted(Comparator.comparing(it -> it.getBaseGraph().toString()))
                    .collect(Collectors.toList());
            Assertions.assertEquals(List.of(a, b, c, d, e, f), actual);
        });

    }
}
