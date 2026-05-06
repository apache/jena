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

package org.apache.jena.mem.graph;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.jmh.JmhDefaultOptions;
import org.apache.jena.mem.GraphMemRoaring;
import org.apache.jena.mem.collection.FastHashSet;
import org.apache.jena.mem.graph.helper.Context;
import org.apache.jena.mem.graph.helper.Releases;

import org.junit.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;

import java.util.List;

import static org.junit.Assert.assertNotNull;


@State(Scope.Benchmark)
public class TestGraphFindByMatchAndCount {

    @Param({
            "../testing/cheeses-0.1.ttl",
            "../testing/pizza.owl.rdf",
            "../testing/data.nt.gz",
    })
    public String param0_GraphUri;

    @Param({
            "GraphMemFast (current)",
            "GraphMemIndexedSet EAGER (current)",
//            "GraphMemIndexedSet LAZY (current)",
//            "GraphMemIndexedSet LAZY_PARALLEL (current)",
//            "GraphMemIndexedSet MINIMAL (current)",
            "GraphMemRoaring EAGER (current)",
//            "GraphMemRoaring LAZY (current)",
//            "GraphMemRoaring LAZY_PARALLEL (current)",
//            "GraphMemRoaring MINIMAL (current)",
    })
    public String param1_GraphImplementation;

    private Context testContext;

    private Graph sutCurrent;
    private NodeSet subjectsToFindCurrent;
    private NodeSet predicateToFindCurrent;
    private NodeSet objectsToFindCurrent;
    private NodeTupleSet subjectPredicateToFindCurrent;
    private NodeTupleSet subjectObjectsToFindCurrent;
    private NodeTupleSet predicateObjectsToFindCurrent;

    private org.apache.shadedJena560.graph.Graph sut560;
    private NodeSet560 subjectsToFind560;
    private NodeSet560 predicateToFind560;
    private NodeSet560 objectsToFind560;
    private NodeTupleSet560 subjectPredicateToFind560;
    private NodeTupleSet560 subjectObjectsToFind560;
    private NodeTupleSet560 predicateObjectsToFind560;

    @Benchmark
    public int findAndCountS__() {
        var total = 0;
        switch (testContext.getJenaVersion()) {
            case CURRENT: {
                for(var s: subjectsToFindCurrent) {
                    total += (int) Iter.count(sutCurrent.find(Triple.createMatch(s, null, null)));
                }
            }
            break;
            case JENA_5_6_0: {
                for(var s: subjectsToFind560) {
                    total += (int) Iter.count(sut560.find(org.apache.shadedJena560.graph.Triple.createMatch(s, null, null)));
                }
            }
            break;
            default: throw new IllegalArgumentException("Unknown Jena version: " + testContext.getJenaVersion());
        }
        return total;
    }

    @Benchmark
    public int findAndCount_P_() {
        var total = 0;
        switch (testContext.getJenaVersion()) {
            case CURRENT: {
                for(var p: predicateToFindCurrent) {
                    total += (int) Iter.count(sutCurrent.find(Triple.createMatch(null, p, null)));
                }
            }
            break;
            case JENA_5_6_0: {
                for(var p: predicateToFind560) {
                    total += (int) Iter.count(sut560.find(org.apache.shadedJena560.graph.Triple.createMatch(null, p, null)));
                }
            }
            break;
            default: throw new IllegalArgumentException("Unknown Jena version: " + testContext.getJenaVersion());
        }
        return total;
    }

    @Benchmark
    public int findAndCount__O() {
        var total = 0;
        switch (testContext.getJenaVersion()) {
            case CURRENT: {
                for(var o: objectsToFindCurrent) {
                    total += (int) Iter.count(sutCurrent.find(Triple.createMatch(null, null, o)));
                }
            }
            break;
            case JENA_5_6_0: {
                for(var o: objectsToFind560) {
                    total += (int) Iter.count(sut560.find(org.apache.shadedJena560.graph.Triple.createMatch(null, null, o)));
                }
            }
            break;
            default: throw new IllegalArgumentException("Unknown Jena version: " + testContext.getJenaVersion());
        }
        return total;
    }

    @Benchmark
    public int findAndCountSP_() {
        var total = 0;
        switch (testContext.getJenaVersion()) {
            case CURRENT: {
                for(var pair: subjectPredicateToFindCurrent) {
                    total += (int) Iter.count(sutCurrent.find(Triple.createMatch(pair.getLeft(), pair.getRight(), null)));
                }
            }
            break;
            case JENA_5_6_0: {
                for(var pair: subjectPredicateToFind560) {
                    total += (int) Iter.count(sut560.find(org.apache.shadedJena560.graph.Triple.createMatch(pair.getLeft(), pair.getRight(), null)));
                }
            }
            break;
            default: throw new IllegalArgumentException("Unknown Jena version: " + testContext.getJenaVersion());
        }
        return total;
    }

    @Benchmark
    public int findAndCountS_O() {
        var total = 0;
        switch (testContext.getJenaVersion()) {
            case CURRENT: {
                for(var pair: subjectObjectsToFindCurrent) {
                    total += (int) Iter.count(sutCurrent.find(Triple.createMatch(pair.getLeft(), null, pair.getRight())));
                }
            }
            break;
            case JENA_5_6_0: {
                for(var pair: subjectObjectsToFind560) {
                    total += (int) Iter.count(sut560.find(org.apache.shadedJena560.graph.Triple.createMatch(pair.getLeft(), null, pair.getRight())));
                }
            }
            break;
            default: throw new IllegalArgumentException("Unknown Jena version: " + testContext.getJenaVersion());
        }
        return total;
    }

    @Benchmark
    public int findAndCount_PO() {
        var total = 0;
        switch (testContext.getJenaVersion()) {
            case CURRENT: {
                for(var pair: predicateObjectsToFindCurrent) {
                    total += (int) Iter.count(sutCurrent.find(Triple.createMatch(null, pair.getLeft(), pair.getRight())));
                }
            }
            break;
            case JENA_5_6_0: {
                for(var pair: predicateObjectsToFind560) {
                    total += (int) Iter.count(sut560.find(org.apache.shadedJena560.graph.Triple.createMatch(null, pair.getLeft(), pair.getRight())));
                }
            }
            break;
            default: throw new IllegalArgumentException("Unknown Jena version: " + testContext.getJenaVersion());
        }
        return total;
    }

    @Setup(Level.Trial)
    public void setupTrial() {
        testContext = new Context(param1_GraphImplementation);
        switch (testContext.getJenaVersion()) {
            case CURRENT: {
                this.sutCurrent = Releases.current.createGraph(testContext.getGraphClass());

                var triples = Releases.current.readTriples(param0_GraphUri);
                triples.forEach(this.sutCurrent::add);

                // init index if needed
                if(this.sutCurrent instanceof GraphMemRoaring roaringGraph
                        && !roaringGraph.isIndexInitialized()) {
                    roaringGraph.initializeIndexParallel();
                }

                /*clone the triples because they should not be the same objects*/
                List<Triple> triplesToFindCurrent = Releases.current.cloneTriples(triples);
                subjectsToFindCurrent = new NodeSet();
                predicateToFindCurrent = new NodeSet();
                objectsToFindCurrent = new NodeSet();
                subjectPredicateToFindCurrent = new NodeTupleSet();
                subjectObjectsToFindCurrent = new NodeTupleSet();
                predicateObjectsToFindCurrent = new NodeTupleSet();
                for(var t: triplesToFindCurrent) {
                    subjectsToFindCurrent.tryAdd(t.getSubject());
                    predicateToFindCurrent.tryAdd(t.getPredicate());
                    objectsToFindCurrent.tryAdd(t.getObject());
                    subjectPredicateToFindCurrent.tryAdd(Pair.of(t.getSubject(), t.getPredicate()));
                    subjectObjectsToFindCurrent.tryAdd(Pair.of(t.getSubject(), t.getObject()));
                    predicateObjectsToFindCurrent.tryAdd(Pair.of(t.getPredicate(), t.getObject()));
                }
            }
            break;
            case JENA_5_6_0: {
                this.sut560 = Releases.v560.createGraph(testContext.getGraphClass());

                var triples = Releases.v560.readTriples(param0_GraphUri);
                triples.forEach(this.sut560::add);

                    /* Shuffle is import because the order might play a role. We want to test the performance of the
                       contains method regardless of the order */
                List<org.apache.shadedJena560.graph.Triple> triplesToFind560 = Releases.v560.cloneTriples(triples);
                subjectsToFind560 = new NodeSet560();
                predicateToFind560 = new NodeSet560();
                objectsToFind560 = new NodeSet560();
                subjectPredicateToFind560 = new NodeTupleSet560();
                subjectObjectsToFind560 = new NodeTupleSet560();
                predicateObjectsToFind560 = new NodeTupleSet560();
                for(var t: triplesToFind560) {
                    subjectsToFind560.tryAdd(t.getSubject());
                    predicateToFind560.tryAdd(t.getPredicate());
                    objectsToFind560.tryAdd(t.getObject());
                    subjectPredicateToFind560.tryAdd(Pair.of(t.getSubject(), t.getPredicate()));
                    subjectObjectsToFind560.tryAdd(Pair.of(t.getSubject(), t.getObject()));
                    predicateObjectsToFind560.tryAdd(Pair.of(t.getPredicate(), t.getObject()));
                }
            }
            break;
            default:
                throw new IllegalArgumentException("Unknown Jena version: " + testContext.getJenaVersion());
        }
    }

    private static class NodeSet extends FastHashSet<Node> {
        @Override
        protected Node[] newKeysArray(int size) {
            return new Node[size];
        }
    }

    private static class NodeSet560 extends FastHashSet<org.apache.shadedJena560.graph.Node> {
        @Override
        protected org.apache.shadedJena560.graph.Node[] newKeysArray(int size) {
            return new org.apache.shadedJena560.graph.Node[size];
        }
    }

    private static class NodeTupleSet extends FastHashSet<Pair<Node, Node>> {
        @SuppressWarnings("unchecked")
        @Override
        protected Pair<Node, Node>[] newKeysArray(int size) {

            return new Pair[size];
        }
    }

    private static class NodeTupleSet560 extends FastHashSet<Pair<org.apache.shadedJena560.graph.Node, org.apache.shadedJena560.graph.Node>> {
        @SuppressWarnings("unchecked")
        @Override
        protected Pair<org.apache.shadedJena560.graph.Node, org.apache.shadedJena560.graph.Node>[] newKeysArray(int size) {
            return new Pair[size];
        }
    }

    @Test
    public void benchmark() throws Exception {
        var opt = JmhDefaultOptions.getDefaults(this.getClass())
                .build();
        var results = new Runner(opt).run();
        assertNotNull(results);
    }
}