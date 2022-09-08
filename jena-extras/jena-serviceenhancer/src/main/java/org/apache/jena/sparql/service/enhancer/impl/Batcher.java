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
 * The batcher transform an iterator of input items into an iterator of batches.
 * Thereby, items are assigned incremental ids.
 * Every returned batch will start with the lowest item index not covered by any prior batch.
 *
 * Parameters are:
 * <ul>
 * <li>maxBulkSize: The maximum size of the batches which to form</li>
 * <li>maxOutOfBandItemCount: The maximum number of out-of-band-items when forming the next batch.
 *       Once this threshold is reached then a batch is returned even if it wasn't full yet.</li>
 * </ul>
 *
 * A batch is guaranteed to have at least one item.
 *
 * @param <G> group key type (e.g. type of service IRIs)
 * @param <I> item type (e.g. Binding)
 */
public class Batcher<G, I> {
    /** The maximum size of the batches formed by this class */
    protected int maxBulkSize;

    /** Allow up to this number of out-of-band-items when forming the next batch.
     * Once this threshold is reached then a batch is returned even if it wasn't full yet.
     */
    protected int maxOutOfBandItemCount;

    /** Function to map an input item to a group key*/
    protected Function<I, G> itemToGroupKey;

    public Batcher(Function<I, G> itemToGroupKey, int maxBulkSize, int maxOutOfBandItemCount) {
        super();
        this.itemToGroupKey = itemToGroupKey;
        this.maxBulkSize = maxBulkSize;
        this.maxOutOfBandItemCount = maxOutOfBandItemCount;
    }

    public IteratorCloseable<GroupedBatch<G, Long, I>> batch(IteratorCloseable<I> inputIterator) {
        return new IteratorGroupedBatch(inputIterator);
    }

    class IteratorGroupedBatch
        extends AbstractIterator<GroupedBatch<G, Long, I>> implements IteratorCloseable<GroupedBatch<G, Long, I>>
    {
        protected IteratorCloseable<I> inputIterator;

        /** The position of the inputIterator */
        protected long inputIteratorOffset;

        // Offsets of the group keys
        protected NavigableMap<Long, G> nextGroup = new TreeMap<>();

        // The outer navigable map has to lowest offset among all the group key's related batches
        protected Map<G, NavigableMap<Long, Batch<Long, I>>> groupToBatches = new HashMap<>();

        public IteratorGroupedBatch(IteratorCloseable<I> inputIterator) {
            this(inputIterator, 0);
        }

        public IteratorGroupedBatch(IteratorCloseable<I> inputIterator, int inputIteratorOffset) {
            super();
            this.inputIterator = inputIterator;
            this.inputIteratorOffset = inputIteratorOffset;
        }

        @Override
        protected GroupedBatch<G, Long, I> computeNext() {
            // For the current result group key and corresponding batch determine how many out-of-band
            // items we have already consumed from the input iterator
            // Any item that does not contribute to the current result batch counts as out-of-band

            // The key of the first pending batch - null if there is none yet
            Optional<G> resultGroupKeyOpt = Optional.ofNullable(nextGroup.firstEntry()).map(Entry::getValue);
            G resultGroupKey = resultGroupKeyOpt.orElse(null);

            Optional<Entry<Long, Batch<Long, I>>> resultBatchEntry = resultGroupKeyOpt
                .map(groupToBatches::get).map(offsetToBatch -> offsetToBatch.firstEntry());

            long resultBatchMinOffset = resultBatchEntry.map(Entry::getKey).orElse(inputIteratorOffset);

            // The result batch - null if there is none
            Optional<Batch<Long, I>> resultBatch = resultBatchEntry.map(Entry::getValue);

            // Get the highest offset of the result batch (if there is one)
            // If a new batch has yet to be created its item ids start with inputIteratorOffset
            long resultBatchMaxOffset = resultBatch.map(Batch::getNextValidIndex).orElse(inputIteratorOffset);

            int resultBatchSize = resultBatch.map(Batch::getItems).map(Map::size).orElse(0);

            long outOfBandItemCountInResultBatch = resultBatchMaxOffset - resultBatchMinOffset - resultBatchSize;
            long outOfBandItemCountInOtherBatches = inputIteratorOffset - resultBatchMaxOffset;

            // The outOfBandItemCount may be counted up in the loop below
            long outOfBandItemCount = outOfBandItemCountInResultBatch + outOfBandItemCountInOtherBatches;

            // The following four variables are re-initialized whenever the groupKey changes
            G previousGroupKey = null; // The input iterator must never yield a null group key
            Batch<Long, I> currentBatch = null;
            NavigableMap<Long, Batch<Long, I>> currentBatches = null;
            int currentBatchSize = -1;

            // Only look at the input iterator if the current batch is not yet full
            if (resultBatchSize < maxBulkSize) {
                while (inputIterator.hasNext() && outOfBandItemCount <= maxOutOfBandItemCount) {
                    I input = inputIterator.next();
                    G currentGroupKey = itemToGroupKey.apply(input);
                    Objects.requireNonNull(currentGroupKey); // Sanity check

                    if (!Objects.equals(currentGroupKey, previousGroupKey)) {
                        previousGroupKey = currentGroupKey;

                        currentBatches = groupToBatches.computeIfAbsent(currentGroupKey, x -> new TreeMap<>());
                        if (currentBatches.isEmpty()) {
                            currentBatch = BatchImpl.forLong();
                            currentBatches.put(inputIteratorOffset, currentBatch);
                            nextGroup.put(inputIteratorOffset, currentGroupKey);
                        } else {
                            currentBatch = currentBatches.lastEntry().getValue();
                        }
                        currentBatchSize = currentBatch.size();

                        if (resultGroupKey == null) {
                            resultGroupKey = currentGroupKey;
                        }
                    }

                    // Add the item to the current batch if it is not full
                    // Otherwise start a new batch
                    Batch<Long, I> insertTargetBatch;
                    if (currentBatchSize >= maxBulkSize) {
                        insertTargetBatch = BatchImpl.forLong();
                        currentBatches.put(inputIteratorOffset, insertTargetBatch);
                        nextGroup.put(inputIteratorOffset, currentGroupKey);
                    } else {
                        insertTargetBatch = currentBatch;
                        // We are just about to add an item to the current batch
                        ++currentBatchSize;
                    }
                    insertTargetBatch.put(inputIteratorOffset, input);
                    ++inputIteratorOffset;

                    // If we just completely filled up the result batch then break
                    if (currentGroupKey.equals(resultGroupKey)) {
                        if (currentBatchSize >= maxBulkSize) {
                            break;
                        }
                    } else {
                        ++outOfBandItemCount;
                    }

                    // Update the state if we started a new batch (because the current one was full)
                    if (insertTargetBatch != currentBatch) {
                        currentBatch = insertTargetBatch;
                        currentBatchSize = insertTargetBatch.size();
                    }
                }
            }

            // Return and remove the first batch from our data structures
            GroupedBatch<G, Long, I> result;
            Iterator<Entry<Long, G>> nextGroupIt = nextGroup.entrySet().iterator();
            if (nextGroupIt.hasNext()) {
                Entry<Long, G> e = nextGroupIt.next();
                resultGroupKey = e.getValue();
                nextGroupIt.remove();
                // nextInputId = e.getKey();

                NavigableMap<Long, Batch<Long, I>> nextBatches = groupToBatches.get(resultGroupKey);
                Iterator<Batch<Long, I>> nextBatchesIt = nextBatches.values().iterator();
                Batch<Long, I> resultBatchTmp = nextBatchesIt.next();
                nextBatchesIt.remove();

                result = new GroupedBatchImpl<>(resultGroupKey, resultBatchTmp);
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
}
