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

package org.apache.jena.mem.spliterator;

import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;

import org.apache.jena.atlas.iterator.ActionCount;
import org.apache.jena.jmh.JmhDefaultOptions;

import org.junit.Assert;
import org.junit.Test;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;

import static org.junit.Assert.assertNotNull;

@State(Scope.Benchmark)
public class TestSparseArraySpliteratorsTryAdvance {


    final static int[] stepsWithNull = new int[]{1, 2, 3, 4, 5};
    @Param({"1000000", "2000000", "3000000", "5000000"})
    public int param0_arraySize;
    @Param({
            "memvalue.SparseArraySpliterator",
            "mem2.SparseArraySpliterator"
    })
    public String param1_iteratorImplementation;
    final List<Object[]> arraysWithNulls = new ArrayList<>(stepsWithNull.length);
    final List<Integer> elementsCounts = new ArrayList<>(stepsWithNull.length);

    @Benchmark
    public long testSpliteratorTryAdvance() {
        long total = 0;
        for (int i = 0; i < stepsWithNull.length; i++) {
            var arrayWithNulls = arraysWithNulls.get(i);
            var elementsCount = elementsCounts.get(i);
            var actionCounter = new ActionCount<>();

            var sut = createSut(arrayWithNulls, elementsCount);

            do {
            } while (sut.tryAdvance(actionCounter));

            total += actionCounter.getCount();
            Assert.assertEquals(elementsCount.longValue(), actionCounter.getCount());
        }
        return total;
    }


    public Spliterator<Object> createSut(Object[] arrayWithNulls, int elementsCount) {
        @SuppressWarnings("UnnecessaryLocalVariable") var count = elementsCount;
        Runnable checkForConcurrentModification = () -> {
            if (count != elementsCount) {
                throw new RuntimeException("Concurrent modification detected");
            }
        };
        return switch (param1_iteratorImplementation) {
            case "memvalue.SparseArraySpliterator" ->
                    new org.apache.jena.memvalue.SparseArraySpliterator<>(arrayWithNulls, count, checkForConcurrentModification);
            case "mem2.SparseArraySpliterator" ->
                    new SparseArraySpliterator<>(arrayWithNulls, checkForConcurrentModification);
            default ->
                    throw new IllegalArgumentException("Unknown spliterator implementation: " + param1_iteratorImplementation);
        };
    }

    @Setup(Level.Trial)
    public void setupTrial() {
        for (int i = 0; i < stepsWithNull.length; i++) {
            var arrayWithNulls = new Object[param0_arraySize];
            var stepsWithNull = TestSparseArraySpliteratorsTryAdvance.stepsWithNull[i];
            var elementsCount = 0;
            for (int k = 0; k < arrayWithNulls.length; k += 1 + stepsWithNull) {
                arrayWithNulls[k] = new Object();
                elementsCount++;
            }
            this.arraysWithNulls.add(i, arrayWithNulls);
            this.elementsCounts.add(i, elementsCount);
        }
    }

    @Test
    public void benchmark() throws Exception {
        var opt = JmhDefaultOptions.getDefaults(this.getClass())
                .warmupIterations(10)
                .measurementIterations(100)
                .build();
        var results = new Runner(opt).run();
        assertNotNull(results);
    }

}
