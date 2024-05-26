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

import org.apache.jena.atlas.iterator.ActionCount;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.mem.graph.helper.Context;
import org.apache.jena.mem.graph.helper.JMHDefaultOptions;
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

    @Param({"800"})
    public int param2_sampleSize;
    Function<String, Object> graphStreamByMatchAndCount;
    private Graph sutCurrent;
    private org.apache.shadedJena480.graph.Graph sut480;
    private List<Triple> triplesToFindCurrent;
    private List<org.apache.shadedJena480.graph.Triple> triplesToFind480;

    private static int count(final Stream<?> stream) {
        var actionCounter = new ActionCount<>();
        stream.forEach(actionCounter::accept);
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

    private Object graphStreamByMatchAndCount480(String pattern) {
        var streamFunction = getStreamFunctionByPattern480(pattern);
        var total = 0;
        for (org.apache.shadedJena480.graph.Triple sample : this.triplesToFind480) {
            total += count(streamFunction.apply(sample));
        }
        return total;
    }

    Function<Triple, Stream<Triple>> getStreamFunctionByPatternCurrent(String pattern) {
        switch (pattern) {
            case "S__":
                return t -> sutCurrent.stream(t.getSubject(), null, null);
            case "_P_":
                return t -> sutCurrent.stream(null, t.getPredicate(), null);
            case "__O":
                return t -> sutCurrent.stream(null, null, t.getObject());
            case "SP_":
                return t -> sutCurrent.stream(t.getSubject(), t.getPredicate(), null);
            case "S_O":
                return t -> sutCurrent.stream(t.getSubject(), null, t.getObject());
            case "_PO":
                return t -> sutCurrent.stream(null, t.getPredicate(), t.getObject());
            default:
                throw new IllegalArgumentException("Unknown pattern: " + pattern);
        }
    }

    Function<org.apache.shadedJena480.graph.Triple, Stream<org.apache.shadedJena480.graph.Triple>> getStreamFunctionByPattern480(String pattern) {
        switch (pattern) {
            case "S__":
                return t -> sut480.stream(t.getSubject(), null, null);
            case "_P_":
                return t -> sut480.stream(null, t.getPredicate(), null);
            case "__O":
                return t -> sut480.stream(null, null, t.getObject());
            case "SP_":
                return t -> sut480.stream(t.getSubject(), t.getPredicate(), null);
            case "S_O":
                return t -> sut480.stream(t.getSubject(), null, t.getObject());
            case "_PO":
                return t -> sut480.stream(null, t.getPredicate(), t.getObject());
            default:
                throw new IllegalArgumentException("Unknown pattern: " + pattern);
        }
    }

    @Setup(Level.Trial)
    public void setupTrial() throws Exception {
        Context trialContext = new Context(param1_GraphImplementation);
        switch (trialContext.getJenaVersion()) {
            case CURRENT: {
                this.sutCurrent = Releases.current.createGraph(trialContext.getGraphClass());
                this.graphStreamByMatchAndCount = this::graphStreamByMatchAndCount;

                var triples = Releases.current.readTriples(param0_GraphUri);
                triples.forEach(this.sutCurrent::add);

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
            case JENA_4_8_0: {
                this.sut480 = Releases.v480.createGraph(trialContext.getGraphClass());
                this.graphStreamByMatchAndCount = this::graphStreamByMatchAndCount480;

                var triples = Releases.v480.readTriples(param0_GraphUri);
                triples.forEach(this.sut480::add);

                /*clone the triples because they should not be the same objects*/
                this.triplesToFind480 = new ArrayList<>(param2_sampleSize);
                var sampleIncrement = triples.size() / param2_sampleSize;
                for (var i = 0; i < triples.size(); i += sampleIncrement) {
                    this.triplesToFind480.add(Releases.v480.cloneTriple(triples.get(i)));
                }
                /* Shuffle is import because the order might play a role. We want to test the performance of the
                       contains method regardless of the order */
                java.util.Collections.shuffle(this.triplesToFind480, new Random(4721));
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
        assertNotNull(results);
    }
}