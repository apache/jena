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

import org.apache.commons.lang3.time.StopWatch;
import org.apache.jena.graph.Triple;
import org.apache.jena.mem.graph.helper.Context;
import org.apache.jena.mem.graph.helper.JMHDefaultOptions;
import org.apache.jena.mem.graph.helper.Releases;
import org.junit.Assert;
import org.junit.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;

import java.math.BigDecimal;
import java.util.List;

@State(Scope.Benchmark)
public class TestGraphMemoryConsumption {

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
    private Context trialContext;

    private List<Triple> allTriplesCurrent;
    private List<org.apache.shadedJena480.graph.Triple> allTriples480;

    java.util.function.Supplier<Object> graphFill;

    @Benchmark
    public Object graphFill() {
        return graphFill.get();
    }

    private Object graphFillCurrent() {
        var memoryBefore = runGcAndGetUsedMemoryInMB();
        var stopwatch = StopWatch.createStarted();
        var sut = Releases.current.createGraph(trialContext.getGraphClass());
        allTriplesCurrent.forEach(sut::add);
        stopwatch.stop();
        var memoryAfter = runGcAndGetUsedMemoryInMB();
        System.out.println(String.format("graphs: %d time to fill graphs: %s additional memory: %5.3f MB",
                sut.size(),
                stopwatch.formatTime(),
                (memoryAfter - memoryBefore)));
        return sut;
    }

    private Object graphFill480() {
        var memoryBefore = runGcAndGetUsedMemoryInMB();
        var stopwatch = StopWatch.createStarted();
        var sut = Releases.v480.createGraph(trialContext.getGraphClass());
        allTriples480.forEach(sut::add);
        stopwatch.stop();
        var memoryAfter = runGcAndGetUsedMemoryInMB();
        System.out.println(String.format("graphs: %d time to fill graphs: %s additional memory: %5.3f MB",
                sut.size(),
                stopwatch.formatTime(),
                (memoryAfter - memoryBefore)));
        return sut;
    }

    /**
     * This method is used to get the memory consumption of the current JVM.
     * @return the memory consumption in MB
     */
    private static double runGcAndGetUsedMemoryInMB() {
        System.runFinalization();
        System.gc();
        Runtime.getRuntime().runFinalization();
        Runtime.getRuntime().gc();
        return BigDecimal.valueOf(Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory()).divide(BigDecimal.valueOf(1024l)).divide(BigDecimal.valueOf(1024l)).doubleValue();
    }

    @Setup(Level.Trial)
    public void setupTrial() throws Exception {
        this.trialContext = new Context(param1_GraphImplementation);
        switch (this.trialContext.getJenaVersion()) {
            case CURRENT:
                this.allTriplesCurrent = Releases.current.readTriples(param0_GraphUri);
                this.graphFill = this::graphFillCurrent;
                break;
            case JENA_4_8_0:
                this.allTriples480 = Releases.v480.readTriples(param0_GraphUri);
                this.graphFill = this::graphFill480;
                break;
            default:
                throw new IllegalArgumentException("Unknown Jena version: " + this.trialContext.getJenaVersion());
        }
    }

    @Test
    public void benchmark() throws Exception {
        var opt = JMHDefaultOptions.getDefaults(this.getClass())
                .warmupIterations(3)
                .measurementIterations(3)
                .build();
        var results = new Runner(opt).run();
        Assert.assertNotNull(results);
    }
}