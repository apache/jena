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

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

import org.apache.jena.jmh.JmhDefaultOptions;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.mem.GraphMemRoaring;
import org.apache.jena.mem.graph.helper.Context;
import org.apache.jena.mem.graph.helper.Releases;

import org.junit.Assert;
import org.junit.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;

@State(Scope.Benchmark)
public class TestGraphContainsAnything {


    @Param({
            "../testing/cheeses-0.1.ttl",
            "../testing/pizza.owl.rdf",
            "../testing/data.nt.gz",
    })
    public String param0_GraphUri;

    @Param({
            "GraphMemFast (current)",
            "GraphMemValue (current)",
//            "GraphMemRoaring EAGER (current)",
//            "GraphMemRoaring LAZY (current)",
//            "GraphMemRoaring LAZY_PARALLEL (current)",
//            "GraphMemRoaring MINIMAL (current)",
//            "GraphMemValue (Jena 5.6.0)",
            "GraphMemFast (Jena 5.6.0)",
            "GraphMemValue (Jena 5.6.0)",
    })
    public String param1_GraphImplementation;
    java.util.function.Function<String, Boolean> graphContains;
    private Graph sutCurrent;
    private org.apache.shadedJena560.graph.Graph sut560;
    private List<Triple> triplesToFindCurrent;
    private List<org.apache.shadedJena560.graph.Triple> triplesToFind560;

    @Benchmark
    public boolean graphContainsS__() {
        return graphContains.apply("S__");
    }

    @Benchmark
    public boolean graphContains_P_() {
        return graphContains.apply("_P_");
    }

    @Benchmark
    public boolean graphContains__O() {
        return graphContains.apply("__O");
    }

    @Benchmark
    public boolean graphContainsSP_() {
        return graphContains.apply("SP_");
    }

    @Benchmark
    public boolean graphContainsS_O() {
        return graphContains.apply("S_O");
    }

    @Benchmark
    public boolean graphContains_PO() {
        return graphContains.apply("_PO");
    }



    private boolean graphContainsCurrent(String pattern) {
        var containsPredicate = getContainsPredicateByPatternCurrent(pattern);
        var found = false;
        for (var t : triplesToFindCurrent) {
            found = containsPredicate.test(t);
            Assert.assertTrue(found);
        }
        return found;
    }

    private boolean graphContains560(String pattern) {
        var containsPredicate = getContainsPredicateByPattern560(pattern);
        var found = false;
        for (var t : triplesToFind560) {
            found = containsPredicate.test(t);
            Assert.assertTrue(found);
        }
        return found;
    }

    Predicate<Triple> getContainsPredicateByPatternCurrent(String pattern) {
        return switch (pattern) {
            case "S__" -> t -> sutCurrent.contains(t.getSubject(), null, null);
            case "_P_" -> t -> sutCurrent.contains(null, t.getPredicate(), null);
            case "__O" -> t -> sutCurrent.contains(null, null, t.getObject());
            case "SP_" -> t -> sutCurrent.contains(t.getSubject(), t.getPredicate(), null);
            case "S_O" -> t -> sutCurrent.contains(t.getSubject(), null, t.getObject());
            case "_PO" -> t -> sutCurrent.contains(null, t.getPredicate(), t.getObject());
            default -> throw new IllegalArgumentException("Unknown pattern: " + pattern);
        };
    }

    Predicate<org.apache.shadedJena560.graph.Triple> getContainsPredicateByPattern560(String pattern) {
        return switch (pattern) {
            case "S__" -> t -> sut560.contains(t.getSubject(), null, null);
            case "_P_" -> t -> sut560.contains(null, t.getPredicate(), null);
            case "__O" -> t -> sut560.contains(null, null, t.getObject());
            case "SP_" -> t -> sut560.contains(t.getSubject(), t.getPredicate(), null);
            case "S_O" -> t -> sut560.contains(t.getSubject(), null, t.getObject());
            case "_PO" -> t -> sut560.contains(null, t.getPredicate(), t.getObject());
            default -> throw new IllegalArgumentException("Unknown pattern: " + pattern);
        };
    }

    @Setup(Level.Trial)
    public void setupTrial() {
        var trialContext = new Context(param1_GraphImplementation);
        switch (trialContext.getJenaVersion()) {
            case CURRENT: {
                this.sutCurrent = Releases.current.createGraph(trialContext.getGraphClass());
                this.graphContains = this::graphContainsCurrent;

                var triples = Releases.current.readTriples(param0_GraphUri);
                triples.forEach(this.sutCurrent::add);

                // init index if needed
                if(this.sutCurrent instanceof GraphMemRoaring roaringGraph
                   && !roaringGraph.isIndexInitialized()) {
                    roaringGraph.initializeIndexParallel();
                }

                /*clone the triples because they should not be the same objects*/
                this.triplesToFindCurrent = Releases.current.cloneTriples(triples);

                    /* Shuffle is import because the order might play a role. We want to test the performance of the
                       contains method regardless of the order */
                Collections.shuffle(this.triplesToFindCurrent, new Random(4721));
            }
            break;
            case JENA_5_6_0: {
                this.sut560 = Releases.v560.createGraph(trialContext.getGraphClass());
                this.graphContains = this::graphContains560;

                var triples = Releases.v560.readTriples(param0_GraphUri);
                triples.forEach(this.sut560::add);

                    /* Shuffle is import because the order might play a role. We want to test the performance of the
                       contains method regardless of the order */
                this.triplesToFind560 = Releases.v560.cloneTriples(triples);
                Collections.shuffle(this.triplesToFind560, new Random(4721));
            }
            break;
            default:
                throw new IllegalArgumentException("Unknown Jena version: " + trialContext.getJenaVersion());
        }
    }

    @Test
    public void benchmark() throws Exception {
        var opt = JmhDefaultOptions.getDefaults(this.getClass())
                .build();
        var results = new Runner(opt).run();
        Assert.assertNotNull(results);
    }
}
