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

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.stream.IntStreams;
import org.apache.commons.numbers.combinatorics.Combinations;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.graph.NodeTransformLib;
import org.junit.jupiter.api.DynamicTest;

/**
 * Test to check consistency of the find() method.
 *
 * Base class for generating tests that invoke the find() method of a dataset graph
 * with all combinations of patterns.
 */
public abstract class AbstractDatasetGraphCompare {
    private String testLabel;

    public AbstractDatasetGraphCompare(String testLabel) {
        super();
        this.testLabel = testLabel;
    }

    public String getTestLabel() {
        return testLabel;
    }

    /** By default consider cardinality when evaluating result sets. */
    protected boolean defaultCompareAsSet() {
        return true;
    }


    /** Sub classes can use this method to generate dynamic tests. */
    public GraphFindTestBuilder prepareFindTests(DatasetGraph referenceDsg, DatasetGraph testDsg, DatasetGraph dataDsg) {
        List<Quad> findQuads = createFindQuads(dataDsg).toList();
        return new GraphFindTestBuilder(testLabel, referenceDsg, testDsg, findQuads)
            .compareAsSet(defaultCompareAsSet());
    }

    /** Derive a reference dataset by materalizing the dataset into a copy. */
    public GraphFindTestBuilder prepareFindTests(DatasetGraph testDsg, DatasetGraph dataDsg) {
        DatasetGraph referenceDsg = DatasetGraphFactory.create();
        referenceDsg.addAll(testDsg);

        List<Quad> findQuads = createFindQuads(dataDsg).toList();
        return new GraphFindTestBuilder(testLabel, referenceDsg, testDsg, findQuads)
            .compareAsSet(defaultCompareAsSet());
    }

    /** Materialize the dataset as the reference dataset. Use reference dataset as dataDsg. */
    public GraphFindTestBuilder prepareFindTests(DatasetGraph testDsg) {
        DatasetGraph referenceDsg = DatasetGraphFactory.create();
        referenceDsg.addAll(testDsg);
        return prepareFindTests(testDsg, referenceDsg, testDsg)
            .compareAsSet(defaultCompareAsSet());
    }

    // Markers for find-quad generation w.r.t. a dataset and a
    // "meta pattern" such as (IN, foo, OUT, OUT).
    // IN becomes substituted with concrete values, out becomes ANY.
    private static final Node IN = NodeFactory.createBlankNode("IN");
    private static final Node OUT = NodeFactory.createBlankNode("OUT");

    /**
     * Generate the set of find patterns for each quad in the source dataset.
     * This is the set of combinations by substituting components with ANY.
     * For example, the derivations for a concrete quad (g, s, p, o) are:
     * <pre>
     * {(g, s, p, ANY), (g, s, ANY, o), (g, s, ANY, ANY), ...}
     * </pre>
     */
    public static Stream<Quad> createFindQuads(DatasetGraph dataSource) {
        Node[] baseMetaPattern = new Node[]{OUT, OUT, OUT, OUT};
        Stream<Quad> result = IntStream.rangeClosed(0, 4).boxed()
            .flatMap(k -> Iter.asStream(Combinations.of(4, k).iterator()))
            .flatMap(ins -> {
                Node[] metaPattern = Arrays.copyOf(baseMetaPattern, baseMetaPattern.length);

                // Use IN to mark the components that we want to substitute with concrete values.
                // OUT becomes ANY.
                IntStreams.of(ins).forEach(i -> metaPattern[i] = IN);
                Quad metaQuad = toQuad(List.of(metaPattern).iterator());

                Set<Quad> lookups = createFindQuads(dataSource, metaQuad);
                return lookups.stream();
            });
        return result;
    }

    private static Quad toQuad(Iterator<Node> it) {
        Quad r = Quad.create(it.next(), it.next(), it.next(), it.next());
        if (it.hasNext()) {
            throw new IllegalArgumentException("Iterator of exactly 4 elements expected.");
        }
        return r;
    }

    private static Node outToAny(Node pattern, Node concrete) {
        Node r = (OUT.equals(pattern)) ? Node.ANY : concrete;
        return r;
    }

    private static Node inToAny(Node node) {
        Node r = (IN.equals(node)) ? Node.ANY : node;
        return r;
    }

    private static Node outToAny(Node node) {
        Node r = (OUT.equals(node)) ? Node.ANY : node;
        return r;
    }

    private static Quad inToAny(Quad metaQuad) {
        return NodeTransformLib.transform(AbstractDatasetGraphCompare::inToAny, metaQuad);
    }

    private static Quad outToAny(Quad metaQuad) {
        return NodeTransformLib.transform(AbstractDatasetGraphCompare::outToAny, metaQuad);
    }

    // !!! This method implicitly gets rid of 'IN' !!!
    // Components of the input quads are processed as follows:
    // If a component of pattern is OUT then it becomes is ANY.
    // Otherwise, *always* return the corresponding component of 'concrete'.
    private static Quad createFindQuad(Quad meta, Quad concrete) {
        Quad result = Quad.create(
            outToAny(meta.getGraph(), concrete.getGraph()),
            outToAny(meta.getSubject(), concrete.getSubject()),
            outToAny(meta.getPredicate(), concrete.getPredicate()),
            outToAny(meta.getObject(), concrete.getObject()));
        return result;
    }

    /**
     * Expand a pattern such as (IN, s, OUT, OUT) into { (g1, s, ANY, ANY), (g2, s, ANY, ANY) }
     * based on the concrete quads in dsg.
     */
    private static Set<Quad> createFindQuads(DatasetGraph dsg, Quad metaQuad) {
        // Replace IN and OUT with ANY - this retains only term nodes.
        Quad p = outToAny(inToAny(metaQuad));
        Set<Quad> result = Iter.collect(Iter.map(dsg.find(p), q -> createFindQuad(metaQuad, q)),
                Collectors.toCollection(LinkedHashSet::new));
        return result;
    }

    /**
     * Builder that accepts a reference dataset, a test dataset
     * and a list of quads for which to produce {@link DynamicTest} instances.
     *
     * <p>
     * In addition, it allows to configure whether result sets should be compared as sets instead of lists.
     * Useful to assess correctness in disregard of cardinality.
     */
    public static class GraphFindTestBuilder {
        private String testLabel;
        private DatasetGraph referenceDsg;
        private DatasetGraph testDsg;
        private List<Quad> findQuads;
        private boolean compareAsSet;

        protected GraphFindTestBuilder(String testLabel, DatasetGraph referenceDsg, DatasetGraph testDsg, List<Quad> findQuads) {
            super();
            this.testLabel = Objects.requireNonNull(testLabel);
            this.referenceDsg = Objects.requireNonNull(referenceDsg);
            this.testDsg = Objects.requireNonNull(testDsg);
            this.findQuads = findQuads;
        }

        public List<Quad> getFindQuads() {
            return findQuads;
        }

        public GraphFindTestBuilder compareAsSet(boolean onOrOff) {
            this.compareAsSet = onOrOff;
            return this;
        }

        public List<DynamicTest> build() {
            List<DynamicTest> tests = findQuads.stream().map(q -> DynamicTest.dynamicTest(testLabel + " " + q,
                new GraphFindExecutable(testLabel, q, referenceDsg, testDsg, compareAsSet))).toList();
            return tests;
        }
    }
}
