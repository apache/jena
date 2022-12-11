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

package org.apache.jena.sparql.service.enhancer.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.iterator.IteratorCloseable;
import org.apache.jena.ext.com.google.common.collect.Streams;
import org.junit.Assert;
import org.junit.Test;

public class TestServiceEnhancerBatcher {

    private static final List<Entry<String, Integer>> testData01 = List.<Entry<String, Integer>>of(
            Map.entry("a", 0),
            Map.entry("a", 1),
            Map.entry("b", 2),
            Map.entry("b", 3),
            Map.entry("b", 4),
            Map.entry("a", 5),
            Map.entry("c", 6),
            Map.entry("a", 7),
            Map.entry("c", 8),
            Map.entry("c", 9),
            Map.entry("d", 10),
            Map.entry("d", 11),
            Map.entry("c", 12),
            Map.entry("c", 13),
            Map.entry("d", 14),
            Map.entry("e", 15));

    /** Test that grouping items correctly considers maxBatchSize and maxOutOfBindItemCount
     * Batch size = 3; up to two out-of-band items are allowed to form a batch.
     */
    @Test
    public void testBatcher_01_3_2() {
        // The numbers below refer to the value-component of the test data
        List<List<Integer>> expectedBatchIds = List.<List<Integer>>of(
                List.of(0, 1),
                List.of(2, 3, 4),
                List.of(5, 7),
                List.of(6, 8, 9),
                List.of(10, 11, 14),
                List.of(12, 13),
                List.of(15)
        );
        eval(testData01, 3, 2, expectedBatchIds);
    }

    /** Form batches up to size 3 allowing 0 out-of-band items; every change in the group id starts a new batch */
    @Test
    public void testBatcher_01_3_0() {
        // The numbers below refer to the value-component of the test data
        List<List<Integer>> expectedBatchIds = List.<List<Integer>>of(
                List.of(0, 1),
                List.of(2, 3, 4),
                List.of(5),
                List.of(6),
                List.of(7),
                List.of(8, 9),
                List.of(10, 11),
                List.of(12, 13),
                List.of(14),
                List.of(15)
        );
        eval(testData01, 3, 0, expectedBatchIds);
    }

    public static void eval(
            List<Entry<String, Integer>> input,
            int maxBatchSize,
            int maxOutOfBandItemCount,
            List<List<Integer>> expectedBatchIds) {
        IteratorCloseable<GroupedBatch<String, Long, Entry<String, Integer>>> it = new Batcher<String, Entry<String, Integer>>
            (Entry::getKey, maxBatchSize, maxOutOfBandItemCount).batch(Iter.iter(input.iterator()));
        // it.forEachRemaining(System.err::println);

        // For each obtained batch extract the list of values
        List<List<Integer>> actualBatchIds = Streams.stream(it)
                .map(groupedBatch -> groupedBatch.getBatch().getItems().values().stream().map(Entry::getValue).collect(Collectors.toList()))
                .collect(Collectors.toList());

        Assert.assertEquals(expectedBatchIds, actualBatchIds);
    }

    /** This test creates random input, batches it and checks that the number of batched items matches that of the input */
    @Test
    public void testBatcher_largeInput() {
        int expectedItemCount = 10000;
        int maxBatchSize = 10;
        int maxGroupCount = 10;
        Random rand = new Random(0);

        List<Entry<String, Integer>> testData = new ArrayList<>(expectedItemCount);
        int capacity = expectedItemCount;
        int i = 0;
        while (capacity > 0) {
            String groupName = Character.toString((char)('a' + rand.nextInt(maxGroupCount)));
            int batchSize = Math.min(capacity, 1 + rand.nextInt(maxBatchSize - 1));
            for (int j = 0; j < batchSize; ++j) {
                testData.add(Map.entry(groupName, i++));
            }
            capacity -= batchSize;
        }

        Stream<GroupedBatch<String, Long, Entry<String, Integer>>> stream = Streams.stream(
                new Batcher<String, Entry<String, Integer>>(Entry::getKey, 4, 4).batch(Iter.iter(testData.iterator())));

        int actualItemCount = stream.mapToInt(groupedBatch -> {
            int r = groupedBatch.getBatch().getItems().size();
            // Sanity check that batches are not larger than the allowed maximum size
            Assert.assertTrue("Batch exceeded maximum size", r <= maxBatchSize);
            return r;
        }).sum();

        Assert.assertEquals(actualItemCount, expectedItemCount);
    }
}
