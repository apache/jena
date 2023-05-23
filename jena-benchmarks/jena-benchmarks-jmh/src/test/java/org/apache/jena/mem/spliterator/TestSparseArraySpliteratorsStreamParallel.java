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

package org.apache.jena.mem.spliterator;

import org.apache.jena.atlas.iterator.ActionCount;
import org.apache.jena.mem.SparseArraySpliterator;
import org.apache.jena.mem.SparseArraySubSpliterator;
import org.junit.Assert;
import org.junit.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;


@State(Scope.Benchmark)
public class TestSparseArraySpliteratorsStreamParallel {


    final static int[] stepsWithNull = new int[] {1, 2, 3, 4, 5};

    List<Object[]> arraysWithNulls = new ArrayList<>(stepsWithNull.length);

    List<Integer> elementsCounts = new ArrayList<>(stepsWithNull.length);


    @Param({"1000000", "2000000", "3000000", "5000000"})
    public int param0_arraySize;

    @Param({
            "SparseArraySpliterator",
            "SparseArraySubSpliterator",
    })
    public String param1_iteratorImplementation;

    @Benchmark
    public long testSpliteratorForeachRemaining() {
        long total = 0;
        for(int i = 0; i < stepsWithNull.length; i++) {
            var arrayWithNulls = arraysWithNulls.get(i);
            var elementsCount = elementsCounts.get(i);
            var actionCounter = new ActionCount<>();

            var sut = createSut(arrayWithNulls, elementsCount);

            StreamSupport.stream(sut, true).forEachOrdered(actionCounter);

            total += actionCounter.getCount();
            Assert.assertEquals(elementsCount.longValue(), actionCounter.getCount());
        }
        return total;
    }

    public Spliterator<Object> createSut(Object[] arrayWithNulls, int elementsCount) {
        var count = elementsCount;
        Runnable checkForConcurrentModification = () -> {
            if (count != elementsCount) {
                throw new RuntimeException("Concurrent modification detected");
            }
        };
        switch (param1_iteratorImplementation) {
            case "SparseArraySpliterator":
                return new SparseArraySpliterator<>(arrayWithNulls, 0, checkForConcurrentModification);

            case "SparseArraySubSpliterator":
                return new SparseArraySubSpliterator<>(arrayWithNulls, 0, checkForConcurrentModification);

            default:
                throw new IllegalArgumentException("Unknown spliterator implementation: " + param1_iteratorImplementation);
        }
    }

    @Setup(Level.Trial)
    public void setupTrial() throws Exception {
        for(int i = 0; i < stepsWithNull.length; i++) {
            var arrayWithNulls = new Object[param0_arraySize];
            var stepsWithNull = this.stepsWithNull[i];
            var elementsCount = 0;
            for (int k = 0; k < arrayWithNulls.length; k+=1+ stepsWithNull) {
                arrayWithNulls[k] = new Object();
                elementsCount++;
            }
            this.arraysWithNulls.add(i, arrayWithNulls);
            this.elementsCounts.add(i, elementsCount);
        }
    }

    @Test
    public void benchmark() throws Exception {
        var opt = new OptionsBuilder()
                // Specify which benchmarks to run.
                // You can be more specific if you'd like to run only one benchmark per test.
                .include(this.getClass().getName())
                // Set the following options as needed
                .mode (Mode.AverageTime)
                .timeUnit(TimeUnit.SECONDS)
                .warmupTime(TimeValue.NONE)
                .warmupIterations(10)
                .measurementIterations(100)
                .measurementTime(TimeValue.NONE)
                .threads(1)
                .forks(1)
                .shouldFailOnError(true)
                .shouldDoGC(true)
                //.jvmArgs("-XX:+UnlockDiagnosticVMOptions", "-XX:+PrintInlining")
                .jvmArgs("-Xmx12G")
                //.addProfiler(WinPerfAsmProfiler.class)
                .resultFormat(ResultFormatType.JSON)
                .result(this.getClass().getSimpleName() + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".json")
                .build();
        var results = new Runner(opt).run();
        Assert.assertNotNull(results);
    }

}
