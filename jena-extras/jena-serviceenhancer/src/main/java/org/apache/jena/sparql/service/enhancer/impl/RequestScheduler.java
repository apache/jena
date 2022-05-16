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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.iterator.IteratorCloseable;
import org.apache.jena.ext.com.google.common.collect.AbstractIterator;

/**
 * Accumulates items from an input iterator into batches.
 * Every returned batch will start with the first item index not covered by any prior batch.
 *
 * Parameters are:
 * <ul>
 * <li>maxBulkSize: The maximum number of items allowed in a batch returned by a call to next()</li>
 * <li>maxeReadAhead: The maximum number of items allowed to read from the input iterator in order to fill a batch</li>
 * <li>maxInputDistance: The index of items w.r.t. to the input iterator in a batch must not be farther apart than this distance</li>
 * </ul>
 *
 * A batch is guaranteed to have at least one item.
 * If any of the thresholds is exceeded a batch will have fewer items that its maximum allowed size.
 *
 *
 * @param <G> group key type (e.g. service IRI)
 * @param <I> item type (e.g. Binding)
 */
public class RequestScheduler<G, I> {
    protected int maxBulkSize;

    /** Allow reading at most this number of items ahead for the input iterator to completely fill
     *  the batch request for the next response */
    protected int maxReadAhead = 300;

    /** Do not group inputs into the same batch if their ids are this (or more) of that amount apart */
    protected int maxInputDistance = 50;

    // protected Iterator<I> inputIterator;
    protected Function<I, G> inputToGroup;

    public RequestScheduler(Function<I, G> inputToGroup, int maxBulkSize) {
        super();
        this.inputToGroup = inputToGroup;
        this.maxBulkSize = maxBulkSize;
    }

    public IteratorCloseable<GroupedBatch<G, Long, I>> group(IteratorCloseable<I> inputIterator) {
        return new Grouper(inputIterator);
    }

    class Grouper
        extends AbstractIterator<GroupedBatch<G, Long, I>> implements IteratorCloseable<GroupedBatch<G, Long, I>>
    {
        protected IteratorCloseable<I> inputIterator;

        /** The position of the inputIterator */
        protected long inputIteratorOffset;

        /** The offset of the next item being returned */
        protected long nextResultOffset;

        protected long nextInputId;

        // the outer navigable map has to lowest offset of the batch
        protected Map<G, NavigableMap<Long, Batch<Long, I>>> groupToBatches = new HashMap<>();

        // Offsets of the group keys
        protected NavigableMap<Long, G> nextGroup = new TreeMap<>();

        public Grouper(IteratorCloseable<I> inputIterator) {
            this(inputIterator, 0);
        }

        public Grouper(IteratorCloseable<I> inputIterator, int inputIteratorOffset) {
            super();
            this.inputIterator = inputIterator;
            this.inputIteratorOffset = inputIteratorOffset;
            this.nextResultOffset = inputIteratorOffset;
        }

        @Override
        protected GroupedBatch<G, Long, I> computeNext() {
            G resultGroupKey = Optional.ofNullable(nextGroup.firstEntry()).map(Entry::getValue).orElse(null);
            G lastGroupKey = null;

            // Cached references
            NavigableMap<Long, Batch<Long, I>> batches = null;
            Batch<Long, I> batch = null;

            while (inputIterator.hasNext() && inputIteratorOffset - nextResultOffset < maxReadAhead) {
                I input = inputIterator.next();
                G groupKey = inputToGroup.apply(input);

                if (!Objects.equals(groupKey, lastGroupKey)) {
                    lastGroupKey = groupKey;

                    if (resultGroupKey == null) {
                        resultGroupKey = groupKey;
                    }

                    batches = groupToBatches.computeIfAbsent(groupKey, x -> new TreeMap<>());
                    if (batches.isEmpty()) {
                        batch = BatchImpl.forLong();
                        batches.put(inputIteratorOffset, batch);
                        nextGroup.put(inputIteratorOffset, groupKey);
                    } else {
                        batch = batches.lastEntry().getValue();
                    }
                }

                // Check whether we need to start a new request
                // Either because the batch is full or the differences between the input ids is too great
                long batchEndOffset = batch.getNextValidIndex();
                long distance = nextInputId - batchEndOffset;

                // If the item is consecutive add it to the list
                int batchSize = batch.size();
                if (distance > maxInputDistance || batchSize >= maxBulkSize) {
                    batch = BatchImpl.forLong();
                    batches.put(inputIteratorOffset, batch);
                }
                batch.put(inputIteratorOffset, input);
                ++inputIteratorOffset;

                // If the batch of the result group just became full then break
                if (groupKey.equals(resultGroupKey) && batchSize + 1 >= maxBulkSize) {
                    break;
                }
            }

            // Return and remove the first batch from our data structures

            GroupedBatch<G, Long, I> result;
            Iterator<Entry<Long, G>> nextGroupIt = nextGroup.entrySet().iterator();
            if (nextGroupIt.hasNext()) {
                Entry<Long, G> e = nextGroupIt.next();
                resultGroupKey = e.getValue();
                nextGroupIt.remove();
                nextInputId = e.getKey();

                NavigableMap<Long, Batch<Long, I>> nextBatches = groupToBatches.get(resultGroupKey);
                Iterator<Batch<Long, I>> nextBatchesIt = nextBatches.values().iterator();
                Batch<Long, I> resultBatch = nextBatchesIt.next();
                nextBatchesIt.remove();

                result = new GroupedBatchImpl<>(resultGroupKey, resultBatch);
            } else {
                result = endOfData();
            }
            return result;
        }

        @Override
        public void close() {
            Iter.close(inputIterator);
        }
    }

//
//	public static void main(String[] args) {
//		Var v = Var.alloc("v");
//		Iterator<Binding> individualIt = IntStream.range(0, 10)
//				.mapToObj(x -> BindingFactory.binding(v, NodeValue.makeInteger(x).asNode()))
//				.iterator();
//
//		Op op = Algebra.compile(QueryFactory.create("SELECT * { ?v ?p ?o }"));
//		OpService opService = new OpService(v, op, false);
//		OpServiceInfo serviceInfo = new OpServiceInfo(opService);
//
//
//		RequestScheduler<Node, Binding> scheduler = new RequestScheduler<>(b ->
//			NodeFactory.createLiteral("group" + (NodeValue.makeNode(b.get(v)).getInteger().intValue() % 3)), 2);
//		Iterator<ServiceBatchRequest<Node, Binding>> batchIt = scheduler.group(individualIt);
//
//		OpServiceExecutorImpl opExecutor = null;
//
//		RequestExecutor executor = new RequestExecutor(opExecutor, serviceInfo, batchIt);
//		// executor.exec();
//
////		while (batchIt.hasNext()) {
////			System.out.println(batchIt.next());
////		}
//
//
//	}
//
}
