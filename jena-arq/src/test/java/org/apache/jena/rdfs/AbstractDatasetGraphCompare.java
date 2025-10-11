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
 * Base class that invokes the find() method of a dataset graph with all combinations of patterns.
 */
public abstract class AbstractDatasetGraphCompare {
    public static final Node IN = NodeFactory.createBlankNode("IN");
    public static final Node OUT = NodeFactory.createBlankNode("OUT");

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

    /**
     * Compare the .find() implementations of two dataset graph implementations
     * The find method is invoked with all possible combinations of concrete values and placeholders.
     * The concrete values or obtained from the dataSource.
     *
     * @param dataSource The graph acting as a source of concrete values to use in .find() calls.
     * @param expectedDsg The graph whose responses of .find() are used as the expected results.
     * @param actualDsg The graph whose responses of .find() are compared to the expected results.
     */
    public static Stream<Quad> createFindQuads(DatasetGraph dataSource) {
        Node[] baseLookupPattern = new Node[]{OUT, OUT, OUT, OUT};
        Stream<Quad> result = IntStream.rangeClosed(0, 4).boxed()
            .flatMap(k -> Iter.asStream(Combinations.of(4, k).iterator()))
            .flatMap(ins -> {
                Node[] lookupPattern = Arrays.copyOf(baseLookupPattern, baseLookupPattern.length);

                // Use IN to mark the components that we want to substitute with concrete values.
                // OUT becomes ANY.
                IntStreams.of(ins).forEach(i -> lookupPattern[i] = IN);
                Quad patternQuad = toQuad(List.of(lookupPattern).iterator());

                Set<Quad> lookups = createFindQuads(dataSource, patternQuad);
                return lookups.stream();
            });
        return result;
    }

    public static Quad toQuad(Iterator<Node> it) {
        Quad r = Quad.create(it.next(), it.next(), it.next(), it.next());
        if (it.hasNext()) {
            throw new IllegalArgumentException("Iterator of exactly 4 elements expected.");
        }
        return r;
    }

    public static Node outToAny(Node pattern, Node concrete) {
        Node r = (OUT.equals(pattern)) ? Node.ANY : concrete;
        return r;
    }

    public static Node inToAny(Node node) {
        Node r = (IN.equals(node)) ? Node.ANY : node;
        return r;
    }

    public static Node outToAny(Node node) {
        Node r = (OUT.equals(node)) ? Node.ANY : node;
        return r;
    }

    public static Quad inToAny(Quad quad) {
        return NodeTransformLib.transform(AbstractDatasetGraphCompare::inToAny, quad);
    }

    public static Quad outToAny(Quad quad) {
        return NodeTransformLib.transform(AbstractDatasetGraphCompare::outToAny, quad);
    }

    // !!! This method implicitly gets rid of 'IN' !!!
    // Components of the input quads are processed as follows:
    // If a component of pattern is OUT then it becomes is ANY.
    // Otherwise, *always* return the corresponding component of 'concrete'.
    public static Quad createFindQuad(Quad pattern, Quad concrete) {
        Quad result = Quad.create(
            outToAny(pattern.getGraph(), concrete.getGraph()),
            outToAny(pattern.getSubject(), concrete.getSubject()),
            outToAny(pattern.getPredicate(), concrete.getPredicate()),
            outToAny(pattern.getObject(), concrete.getObject()));
        return result;
    }

    /**
     * Expand a pattern such as (IN, s, OUT, OUT) into { (g1, s, ANY, ANY), (g2, s, ANY, ANY) }
     * based on the concrete quads in dsg.
     */
    public static Set<Quad> createFindQuads(DatasetGraph dsg, Quad pattern) {
        // Replace IN and OUT with ANY - this retains only term nodes.
        Quad p = outToAny(inToAny(pattern));
        Set<Quad> result = Iter.collect(Iter.map(dsg.find(p), q -> createFindQuad(pattern, q)),
                Collectors.toCollection(LinkedHashSet::new));
        return result;
    }

    /**
     * Indirection to configure further test parameters..
     * Allows specifying that result sets should be compared as sets instead of lists.
     * Mainly useful for debugging in order to asses correctness while ignoring cardinality issues.
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
