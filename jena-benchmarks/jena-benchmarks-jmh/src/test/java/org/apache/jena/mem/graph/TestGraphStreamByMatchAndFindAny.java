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
import org.junit.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


@State(Scope.Benchmark)
public class TestGraphStreamByMatchAndFindAny {

    @Param({
            "../testing/cheeses-0.1.ttl",
            "../testing/pizza.owl.rdf",
            "../testing/BSBM/bsbm-1m.nt.gz",
    })
    public String param0_GraphUri;

    @Param({
            "GraphMem (current)",
            "GraphMem (Jena 4.8.0)",
    })
    public String param1_GraphImplementation;

    private Graph sutCurrent;
    private org.apache.shadedJena480.graph.Graph sut480;

    private List<Triple> triplesToFindCurrent;
    private List<org.apache.shadedJena480.graph.Triple> triplesToFind480;

    java.util.function.Function<String, Object> graphStream;

    @Benchmark
    public Object graphStreamS__() {
        return graphStream.apply("S__");
    }

    @Benchmark
    public Object graphStream_P_() {
        return graphStream.apply("_P_");
    }

    @Benchmark
    public Object graphStreamSP_() {
        return graphStream.apply("SP_");
    }

    @Benchmark
    public Object graphStreamS_O() {
        return graphStream.apply("S_O");
    }

    @Benchmark
    public Object graphStream_PO() {
        return graphStream.apply("_PO");
    }

    @Benchmark
    public Object graphStream__O() {
        return graphStream.apply("__O");
    }

    private Object graphStreamByMatchAndFindAnyCurrent(String pattern) {
        var streamFunction = getStreamFunctionByPatternCurrent(pattern);
        Triple t = null;
        for (Triple sample : this.triplesToFindCurrent) {
            final Optional<Triple> ot = streamFunction.apply(sample).findAny();
            assertTrue(ot.isPresent());
            t = ot.get();
            assertNotNull(t);
        }
        return t;
    }

    private Object graphFindByMatcheAndFindAny480(String pattern) {
        var streamFunction = getStreamFunctionByPattern480(pattern);
        org.apache.shadedJena480.graph.Triple t = null;
        for (org.apache.shadedJena480.graph.Triple sample : this.triplesToFind480) {
            final Optional<org.apache.shadedJena480.graph.Triple> ot = streamFunction.apply(sample).findAny();
            assertTrue(ot.isPresent());
            t = ot.get();
            assertNotNull(t);
        }
        return t;
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
            case CURRENT:
                {
                    this.sutCurrent = Releases.current.createGraph(trialContext.getGraphClass());
                    this.graphStream = this::graphStreamByMatchAndFindAnyCurrent;

                    var triples = Releases.current.readTriples(param0_GraphUri);
                    triples.forEach(this.sutCurrent::add);

                    /*clone the triples because they should not be the same objects*/
                    this.triplesToFindCurrent = Releases.current.cloneTriples(triples);
                }
                break;
            case JENA_4_8_0:
                {
                    this.sut480 = Releases.v480.createGraph(trialContext.getGraphClass());
                    this.graphStream = this::graphFindByMatcheAndFindAny480;

                    var triples = Releases.v480.readTriples(param0_GraphUri);
                    triples.forEach(this.sut480::add);

                    /*clone the triples because they should not be the same objects*/
                    this.triplesToFind480 = Releases.v480.cloneTriples(triples);
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
