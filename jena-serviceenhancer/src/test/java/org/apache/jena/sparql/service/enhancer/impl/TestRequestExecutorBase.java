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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Iterators;

import org.junit.jupiter.api.Test;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.apache.jena.sparql.service.enhancer.impl.RequestExecutorBase.Granularity;
import org.apache.jena.sparql.service.enhancer.impl.util.iterator.AbortableIterator;
import org.apache.jena.sparql.service.enhancer.impl.util.iterator.AbortableIterators;

public class TestRequestExecutorBase {
    @Test
    public void testWrapper() {
        for (int i = 0; i < 2; ++i) {
            test();
        }
    }

    public void test() {
        int numGroups = 1000;
        int numItemsPerGroup = 10;

        int taskSlots = 20;
        int readAhead = 100000;

        Iterator<Entry<String, String>> plainIt = IntStream.range(0, numGroups).boxed()
            .flatMap(i -> IntStream.range(0, numItemsPerGroup).mapToObj(j -> Map.entry("group" + i, "item" + j)))
            .iterator();

        Batcher<String, Entry<String, String>> batcher = new Batcher<>(Entry::getKey, 3, 10);

        AbortableIterator<GroupedBatch<String, Long, Entry<String, String>>> batchedIt = batcher.batch(AbortableIterators.wrap(plainIt));

        System.out.println(batchedIt.toString());
        // if (true) { return; }

        RequestExecutorBase<String, Entry<String, String>, Entry<Long, String>> executor = new RequestExecutorBase<>(
            new AtomicBoolean(),
            Granularity.BATCH,
            batchedIt,
            taskSlots,
            readAhead) {
                // The creator may be called from the main thread - but what it returns will be run on a separate thread.
                @Override
                protected IteratorCreator<Entry<Long, String>> processBatch(boolean isInNewThread, String groupKey,
                        List<Entry<String, String>> batch, List<Long> reverseMap) {

                    // Create an iterator that defers each item by 1 ms.
                    return () -> AbortableIterators.wrap(IntStream.range(0,  batch.size())
                            .peek(item -> {
                                try {
                                    Thread.sleep(1);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            })
                            .mapToObj(i -> Map.entry(reverseMap.get(i), batch.get(i).getValue()))
                            .iterator());
                }

                @Override protected long    extractInputOrdinal(Entry<Long, String> input) { return input.getKey(); }
                @Override protected void    checkCanExecInNewThread() {}
                @Override public  void      output(IndentedWriter out, SerializationContext sCxt) {}
                @Override protected boolean isCancelled() { return false; }
            };


        Stopwatch sw = Stopwatch.createStarted();
        int size = Iterators.size(executor);
        System.out.println(size);
        System.out.println("Time taken: " + sw.elapsed(TimeUnit.MILLISECONDS) * 0.001f);
//		while (executor.hasNext()) {
//			Entry<Long, String> item = executor.next();
//			System.out.println(item);
//		}

        executor.close();
    }
}
