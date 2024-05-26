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

package org.apache.jena.mem2.set.triple;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.jena.graph.Triple;
import org.apache.jena.mem.graph.helper.JMHDefaultOptions;
import org.apache.jena.mem.graph.helper.Releases;
import org.junit.Assert;
import org.junit.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;

@State(Scope.Benchmark)
public class TestSetMemoryConsumption {

    @Param({
            "../testing/cheeses-0.1.ttl",
            "../testing/pizza.owl.rdf",
            "../testing/BSBM/bsbm-1m.nt.gz",
    })
    public String param0_GraphUri;

    @Param({
            "HashSet",
            "HashCommonTripleSet",
            "FastHashTripleSet"
    })
    public String param1_SetImplementation;
    java.util.function.Supplier<Object> fillSet;
    private List<Triple> triples;

    /**
     * This method is used to get the memory consumption of the current JVM.
     *
     * @return the memory consumption in MB
     */
    private static double runGcAndGetUsedMemoryInMB() {
        System.runFinalization();
        System.gc();
        Runtime.getRuntime().runFinalization();
        Runtime.getRuntime().gc();
        return BigDecimal.valueOf(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()).divide(BigDecimal.valueOf(1024L)).divide(BigDecimal.valueOf(1024L)).doubleValue();
    }

    @Benchmark
    public Object fillSet() {
        var memoryBefore = runGcAndGetUsedMemoryInMB();
        var stopwatch = StopWatch.createStarted();
        var sut = fillSet.get();
        stopwatch.stop();
        var memoryAfter = runGcAndGetUsedMemoryInMB();
        System.out.printf("graphs: %d time to fill graphs: %s additional memory: %5.3f MB%n",
                triples.size(),
                stopwatch.formatTime(),
                (memoryAfter - memoryBefore));
        return sut;
    }

    private Object fillHashSet() {
        var sut = new HashSet<Triple>();
        triples.forEach(sut::add);
        Assert.assertEquals(triples.size(), sut.size());
        return sut;
    }

    private Object fillHashCommonTripleSet() {
        var sut = new HashCommonTripleSet();
        triples.forEach(sut::addUnchecked);
        Assert.assertEquals(triples.size(), sut.size());
        return sut;
    }

    private Object fillFastHashTripleSet() {
        var sut = new FastHashTripleSet();
        triples.forEach(sut::addUnchecked);
        Assert.assertEquals(triples.size(), sut.size());
        return sut;
    }


    @Setup(Level.Trial)
    public void setupTrial() throws Exception {
        triples = Releases.current.readTriples(param0_GraphUri);
        switch (param1_SetImplementation) {
            case "HashSet":
                this.fillSet = this::fillHashSet;
                break;
            case "HashCommonTripleSet":
                this.fillSet = this::fillHashCommonTripleSet;
                break;
            case "FastHashTripleSet":
                this.fillSet = this::fillFastHashTripleSet;
                break;
            default:
                throw new IllegalArgumentException("Unknown set implementation: " + param1_SetImplementation);
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