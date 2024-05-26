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
import org.apache.jena.mem.graph.helper.Context;
import org.apache.jena.mem.graph.helper.JMHDefaultOptions;
import org.apache.jena.mem.graph.helper.Releases;
import org.junit.Assert;
import org.junit.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;

import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;


@State(Scope.Benchmark)
public class TestGraphStreamAll {

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
    java.util.function.Supplier<Object> graphStream;
    java.util.function.Supplier<Object> graphStreamParallel;
    private Graph sutCurrent;
    private org.apache.shadedJena480.graph.Graph sut480;

    @Benchmark
    public Object graphStream() {
        return graphStream.get();
    }

    @Benchmark
    public Object graphStreamParallel() {
        return graphStreamParallel.get();
    }

    private Object graphStreamCurrent() {
        var list = sutCurrent.stream().collect(Collectors.toList());
        assertEquals(sutCurrent.size(), list.size());
        return list;
    }

    private Object graphStream480() {
        var list = sut480.stream().collect(Collectors.toList());
        assertEquals(sut480.size(), list.size());
        return list;
    }

    private Object graphStreamParallelCurrent() {
        var list = sutCurrent.stream().parallel().collect(Collectors.toList());
        assertEquals(sutCurrent.size(), list.size());
        return list;
    }

    private Object graphStreamParallel480() {
        var list = sut480.stream().parallel().collect(Collectors.toList());
        assertEquals(sut480.size(), list.size());
        return list;
    }

    @Setup(Level.Trial)
    public void setupTrial() throws Exception {
        Context trialContext = new Context(param1_GraphImplementation);
        switch (trialContext.getJenaVersion()) {
            case CURRENT: {
                this.sutCurrent = Releases.current.createGraph(trialContext.getGraphClass());
                this.graphStream = this::graphStreamCurrent;
                this.graphStreamParallel = this::graphStreamParallelCurrent;

                var triples = Releases.current.readTriples(param0_GraphUri);
                triples.forEach(this.sutCurrent::add);
            }
            break;
            case JENA_4_8_0: {
                this.sut480 = Releases.v480.createGraph(trialContext.getGraphClass());
                this.graphStream = this::graphStream480;
                this.graphStreamParallel = this::graphStreamParallel480;

                var triples = Releases.v480.readTriples(param0_GraphUri);
                triples.forEach(this.sut480::add);
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