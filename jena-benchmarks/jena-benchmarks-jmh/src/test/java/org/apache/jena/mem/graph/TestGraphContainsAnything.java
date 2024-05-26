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

package org.apache.jena.mem.graph;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.mem.graph.helper.Context;
import org.apache.jena.mem.graph.helper.JMHDefaultOptions;
import org.apache.jena.mem.graph.helper.Releases;
import org.junit.Assert;
import org.junit.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

@State(Scope.Benchmark)
public class TestGraphContainsAnything {


    @Param({
            "../testing/cheeses-0.1.ttl",
            "../testing/pizza.owl.rdf",
            "../testing/BSBM/bsbm-1m.nt.gz",
    })
    public String param0_GraphUri;

    @Param({
            "GraphMem (current)",
            "GraphMem2Fast (current)",
            "GraphMem2Legacy (current)",
            "GraphMem2Roaring (current)",
            "GraphMem (Jena 4.8.0)",
    })
    public String param1_GraphImplementation;
    java.util.function.Function<String, Boolean> graphContains;
    private Graph sutCurrent;
    private org.apache.shadedJena480.graph.Graph sut480;
    private List<Triple> triplesToFindCurrent;
    private List<org.apache.shadedJena480.graph.Triple> triplesToFind480;

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

    private boolean graphContains480(String pattern) {
        var containsPredicate = getContainsPredicateByPattern480(pattern);
        var found = false;
        for (var t : triplesToFind480) {
            found = containsPredicate.test(t);
            Assert.assertTrue(found);
        }
        return found;
    }

    Predicate<Triple> getContainsPredicateByPatternCurrent(String pattern) {
        switch (pattern) {
            case "S__":
                return t -> sutCurrent.contains(t.getSubject(), null, null);
            case "_P_":
                return t -> sutCurrent.contains(null, t.getPredicate(), null);
            case "__O":
                return t -> sutCurrent.contains(null, null, t.getObject());
            case "SP_":
                return t -> sutCurrent.contains(t.getSubject(), t.getPredicate(), null);
            case "S_O":
                return t -> sutCurrent.contains(t.getSubject(), null, t.getObject());
            case "_PO":
                return t -> sutCurrent.contains(null, t.getPredicate(), t.getObject());
            default:
                throw new IllegalArgumentException("Unknown pattern: " + pattern);
        }
    }

    Predicate<org.apache.shadedJena480.graph.Triple> getContainsPredicateByPattern480(String pattern) {
        switch (pattern) {
            case "S__":
                return t -> sut480.contains(t.getSubject(), null, null);
            case "_P_":
                return t -> sut480.contains(null, t.getPredicate(), null);
            case "__O":
                return t -> sut480.contains(null, null, t.getObject());
            case "SP_":
                return t -> sut480.contains(t.getSubject(), t.getPredicate(), null);
            case "S_O":
                return t -> sut480.contains(t.getSubject(), null, t.getObject());
            case "_PO":
                return t -> sut480.contains(null, t.getPredicate(), t.getObject());
            default:
                throw new IllegalArgumentException("Unknown pattern: " + pattern);
        }
    }

    @Setup(Level.Trial)
    public void setupTrial() throws Exception {
        var trialContext = new Context(param1_GraphImplementation);
        switch (trialContext.getJenaVersion()) {
            case CURRENT: {
                this.sutCurrent = Releases.current.createGraph(trialContext.getGraphClass());
                this.graphContains = this::graphContainsCurrent;

                var triples = Releases.current.readTriples(param0_GraphUri);
                triples.forEach(this.sutCurrent::add);

                /*clone the triples because they should not be the same objects*/
                this.triplesToFindCurrent = Releases.current.cloneTriples(triples);

                    /* Shuffle is import because the order might play a role. We want to test the performance of the
                       contains method regardless of the order */
                Collections.shuffle(this.triplesToFindCurrent, new Random(4721));
            }
            break;
            case JENA_4_8_0: {
                this.sut480 = Releases.v480.createGraph(trialContext.getGraphClass());
                this.graphContains = this::graphContains480;

                var triples = Releases.v480.readTriples(param0_GraphUri);
                triples.forEach(this.sut480::add);

                    /* Shuffle is import because the order might play a role. We want to test the performance of the
                       contains method regardless of the order */
                this.triplesToFind480 = Releases.v480.cloneTriples(triples);
                Collections.shuffle(this.triplesToFind480, new Random(4721));
            }
            break;
            default:
                throw new IllegalArgumentException("Unknown Jena version: " + trialContext.getJenaVersion());
        }
    }

    @Test
    public void benchmark() throws Exception {
        var opt = JMHDefaultOptions.getDefaults(this.getClass())
                .build();
        var results = new Runner(opt).run();
        Assert.assertNotNull(results);
    }
}
