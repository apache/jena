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

import org.apache.jena.atlas.iterator.ActionCount;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.jmh.JmhDefaultOptions;
import org.apache.jena.mem.GraphMemRoaring;
import org.apache.jena.mem.graph.helper.Context;
import org.apache.jena.mem.graph.helper.Releases;

import org.junit.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.Assert.assertNotNull;


@State(Scope.Benchmark)
public class TestGraphStreamByMatchAndCount {

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

    @Param({"800"})
    public int param2_sampleSize;
    Function<String, Object> graphStreamByMatchAndCount;
    private Graph sutCurrent;
    private org.apache.shadedJena560.graph.Graph sut560;
    private List<Triple> triplesToFindCurrent;
    private List<org.apache.shadedJena560.graph.Triple> triplesToFind560;

    private static int count(final Stream<?> stream) {
        var actionCounter = new ActionCount<>();
        stream.forEach(actionCounter);
        return (int) actionCounter.getCount();
    }

    @Benchmark
    public Object graphStreamS__() {
        return graphStreamByMatchAndCount.apply("S__");
    }

    @Benchmark
    public Object graphStream_P_() {
        return graphStreamByMatchAndCount.apply("_P_");
    }

    @Benchmark
    public Object graphStream__O() {
        return graphStreamByMatchAndCount.apply("__O");
    }

    @Benchmark
    public Object graphStreamSP_() {
        return graphStreamByMatchAndCount.apply("SP_");
    }

    @Benchmark
    public Object graphStreamS_O() {
        return graphStreamByMatchAndCount.apply("S_O");
    }

    @Benchmark
    public Object graphStream_PO() {
        return graphStreamByMatchAndCount.apply("_PO");
    }

    private int graphStreamByMatchAndCount(String pattern) {
        var streamFunction = getStreamFunctionByPatternCurrent(pattern);
        var total = 0;
        for (Triple sample : this.triplesToFindCurrent) {
            total += count(streamFunction.apply(sample));
        }
        return total;
    }

    private Object graphStreamByMatchAndCount560(String pattern) {
        var streamFunction = getStreamFunctionByPattern560(pattern);
        var total = 0;
        for (org.apache.shadedJena560.graph.Triple sample : this.triplesToFind560) {
            total += count(streamFunction.apply(sample));
        }
        return total;
    }

    Function<Triple, Stream<Triple>> getStreamFunctionByPatternCurrent(String pattern) {
        return switch (pattern) {
            case "S__" -> t -> sutCurrent.stream(t.getSubject(), null, null);
            case "_P_" -> t -> sutCurrent.stream(null, t.getPredicate(), null);
            case "__O" -> t -> sutCurrent.stream(null, null, t.getObject());
            case "SP_" -> t -> sutCurrent.stream(t.getSubject(), t.getPredicate(), null);
            case "S_O" -> t -> sutCurrent.stream(t.getSubject(), null, t.getObject());
            case "_PO" -> t -> sutCurrent.stream(null, t.getPredicate(), t.getObject());
            default -> throw new IllegalArgumentException("Unknown pattern: " + pattern);
        };
    }

    Function<org.apache.shadedJena560.graph.Triple, Stream<org.apache.shadedJena560.graph.Triple>> getStreamFunctionByPattern560(String pattern) {
        return switch (pattern) {
            case "S__" -> t -> sut560.stream(t.getSubject(), null, null);
            case "_P_" -> t -> sut560.stream(null, t.getPredicate(), null);
            case "__O" -> t -> sut560.stream(null, null, t.getObject());
            case "SP_" -> t -> sut560.stream(t.getSubject(), t.getPredicate(), null);
            case "S_O" -> t -> sut560.stream(t.getSubject(), null, t.getObject());
            case "_PO" -> t -> sut560.stream(null, t.getPredicate(), t.getObject());
            default -> throw new IllegalArgumentException("Unknown pattern: " + pattern);
        };
    }

    @Setup(Level.Trial)
    public void setupTrial() {
        Context trialContext = new Context(param1_GraphImplementation);
        switch (trialContext.getJenaVersion()) {
            case CURRENT: {
                this.sutCurrent = Releases.current.createGraph(trialContext.getGraphClass());
                this.graphStreamByMatchAndCount = this::graphStreamByMatchAndCount;

                var triples = Releases.current.readTriples(param0_GraphUri);
                triples.forEach(this.sutCurrent::add);
                // init index if needed
                if(this.sutCurrent instanceof GraphMemRoaring roaringGraph
                    && !roaringGraph.isIndexInitialized()) {
                    roaringGraph.initializeIndexParallel();
                }

                /*clone the triples because they should not be the same objects*/
                this.triplesToFindCurrent = new ArrayList<>(param2_sampleSize);
                var sampleIncrement = triples.size() / param2_sampleSize;
                for (var i = 0; i < triples.size(); i += sampleIncrement) {
                    this.triplesToFindCurrent.add(Releases.current.cloneTriple(triples.get(i)));
                }
                /* Shuffle is import because the order might play a role. We want to test the performance of the
                       contains method regardless of the order */
                java.util.Collections.shuffle(this.triplesToFindCurrent, new Random(4721));
            }
            break;
            case JENA_5_6_0: {
                this.sut560 = Releases.v560.createGraph(trialContext.getGraphClass());
                this.graphStreamByMatchAndCount = this::graphStreamByMatchAndCount560;

                var triples = Releases.v560.readTriples(param0_GraphUri);
                triples.forEach(this.sut560::add);

                /*clone the triples because they should not be the same objects*/
                this.triplesToFind560 = new ArrayList<>(param2_sampleSize);
                var sampleIncrement = triples.size() / param2_sampleSize;
                for (var i = 0; i < triples.size(); i += sampleIncrement) {
                    this.triplesToFind560.add(Releases.v560.cloneTriple(triples.get(i)));
                }
                /* Shuffle is import because the order might play a role. We want to test the performance of the
                       contains method regardless of the order */
                java.util.Collections.shuffle(this.triplesToFind560, new Random(4721));
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
        assertNotNull(results);
    }
}