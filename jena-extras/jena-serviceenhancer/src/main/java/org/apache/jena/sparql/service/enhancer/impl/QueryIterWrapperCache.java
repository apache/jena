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
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableMap;

import org.apache.jena.atlas.logging.Log;
import org.apache.jena.ext.com.google.common.collect.AbstractIterator;
import org.apache.jena.ext.com.google.common.collect.Iterators;
import org.apache.jena.ext.com.google.common.collect.Table.Cell;
import org.apache.jena.ext.com.google.common.math.LongMath;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.service.enhancer.claimingcache.RefFuture;
import org.apache.jena.sparql.service.enhancer.impl.util.BindingUtils;
import org.apache.jena.sparql.service.enhancer.impl.util.IteratorUtils;
import org.apache.jena.sparql.service.enhancer.impl.util.QueryIterSlottedBase;
import org.apache.jena.sparql.service.enhancer.slice.api.Slice;
import org.apache.jena.sparql.service.enhancer.slice.api.SliceAccessor;

public class QueryIterWrapperCache
    extends QueryIterSlottedBase
{
    protected AbstractIterator<Cell<Integer, Integer, Iterator<Binding>>> mergeLeftJoin;

    protected QueryIterator inputIter;
    protected ServiceResponseCache cache;

    protected int batchSize;
    protected Batch<Integer, PartitionRequest<Binding>> inputBatch;
    protected Var idxVar; // CacheKeyAccessor cacheKeyAccessor;
    protected Node serviceNode;

    protected ServiceCacheKeyFactory cacheKeyFactory;

    protected PartitionRequest<Binding> inputPart; // Value stored here for debugging

    protected long currentOffset = 0;
    protected long processedBindingCount = 0;

    protected Iterator<Binding> currentBatchIt;
    /** The claimed cache entry - prevents premature eviction */
    protected RefFuture<ServiceCacheValue> claimedCacheEntry = null;

    /** The accessor for writing data to the cache */
    protected SliceAccessor<Binding[]> cacheDataAccessor = null;

    protected AbstractIterator<Long> batchOutputIdIt;

    public QueryIterWrapperCache(
            QueryIterator qIter,
            int batchSize,
            ServiceResponseCache cache,
            // Set<Var> joinVars,
            // boolean isLoopJoin,
            ServiceCacheKeyFactory cacheKeyFactory,
            Batch<Integer, PartitionRequest<Binding>> inputBatch,
            Var idxVar,
            Node serviceNode
            ) {
        this.inputIter = qIter;
        this.batchSize = batchSize;
        this.cache = cache;
        this.cacheKeyFactory = cacheKeyFactory;
        this.inputBatch = inputBatch;
        this.idxVar = idxVar;
        this.serviceNode = serviceNode;
        this.currentBatchIt = null;
        mergeLeftJoin = IteratorUtils.partialLeftMergeJoin(
                Iterators.concat(
                        inputBatch.getItems().keySet().iterator(),
                        Arrays.asList(BatchQueryRewriter.REMOTE_END_MARKER).iterator()),
                qIter,
                outputId -> outputId,
                binding -> BindingUtils.getNumber(binding, idxVar).intValue()
            );
    }

    @Override
    protected Binding moveToNext() {
        if (currentBatchIt == null) {
            setupForNextLhsBinding();
            currentBatchIt = Collections.emptyIterator();
        }

        Binding result;
        while (true) {
            if (currentBatchIt.hasNext()) {
                result = currentBatchIt.next();
                break;
            } else {
                prepareNextBatch();

                if (!currentBatchIt.hasNext()) {
                    closeCurrentCacheResources();
                    result = null;
                    break;
                }
            }
        }
        return result;
    }

    protected void setupForNextLhsBinding() {
        closeCurrentCacheResources();

        NavigableMap<Integer, PartitionRequest<Binding>> inputs = inputBatch.getItems();

        if (mergeLeftJoin.hasNext()) {
            Cell<Integer, Integer, Iterator<Binding>> peek = mergeLeftJoin.peek();
            int outputId = peek.getColumnKey();

            if (!BatchQueryRewriter.isRemoteEndMarker(outputId)) {

                inputPart = inputs.get(outputId);
                Binding inputBinding = inputPart.getPartitionKey();
                // System.out.println("Moving to inputBinding " + inputBinding);

                ServiceCacheKey cacheKey = cacheKeyFactory.createCacheKey(inputBinding);
                // System.out.println("Writing to cache key " + cacheKey);

                claimedCacheEntry = cache.getCache().claim(cacheKey);
                ServiceCacheValue c = claimedCacheEntry.await();

                Slice<Binding[]> slice = c.getSlice();
                cacheDataAccessor = slice.newSliceAccessor();
            }
        }
    }

    /**
     *
     * The tricky part is that we first need to consume rhs and write it to the cache.
     * When rhs is consumed a post-action that updates slice metadata has to be performed; but
     * this action depends on the next item in lhs.
     *
     */
    public void prepareNextBatch() {
        NavigableMap<Integer, PartitionRequest<Binding>> inputs = inputBatch.getItems();

        Binding[] arr = new Binding[batchSize];
        long remainingBatchCapacity = batchSize;

        // The batch of bindings under preparation - its content will also exist in the cache
        List<Binding> clientBatch = new ArrayList<>(batchSize);

        while (mergeLeftJoin.hasNext() && remainingBatchCapacity > 0) {
            Cell<Integer, Integer, Iterator<Binding>> cell = mergeLeftJoin.peek();
            int outputId = cell.getColumnKey();
            Iterator<Binding> rhs = cell.getValue();

            boolean isLocalEndMarker = BatchQueryRewriter.isRemoteEndMarker(outputId);

            if (isLocalEndMarker) {
                if (rhs != null) {
                    Iterators.size(rhs); // Consume; expect 1 item
                }
                Iterators.size(mergeLeftJoin); // Consume; expect 1 item

                // Expose the end marker
                if (rhs != null) {
                    clientBatch.add(BindingFactory.binding(idxVar, BatchQueryRewriter.NV_REMOTE_END_MARKER.asNode()));
                }
                break;
            }

            inputPart = inputs.get(outputId);

            // If rhs is consumed we can only update minimum slice sizes
            int arrLen = 0;
            if (rhs != null) {
                while (rhs.hasNext() && arrLen < remainingBatchCapacity) {
                    Binding rawOutputBinding = rhs.next();
                    clientBatch.add(rawOutputBinding);

                    // Cut away the idx value for the binding in the cache
                    Binding outputBinding = BindingUtils.project(rawOutputBinding, rawOutputBinding.vars(), idxVar);
                    arr[arrLen++] = outputBinding;
                }
                remainingBatchCapacity -= arrLen;
                processedBindingCount += arrLen;
            }

            boolean isRhsExhausted = rhs == null || !rhs.hasNext();

            // Submit batch so far
            long inputOffset = inputPart.getOffset();
            long inputLimit = inputPart.getLimit();
            long start = inputOffset + currentOffset;
            long end = start + arrLen;

            currentOffset += arrLen;
            cacheDataAccessor.claimByOffsetRange(start, end);

            cacheDataAccessor.lock();
            try {
                cacheDataAccessor.write(start, arr, 0, arrLen);

                Slice<Binding[]> slice = cacheDataAccessor.getSlice();
                // If rhs is completely empty (without any data) then only update the slice metadata

                if (isRhsExhausted) {
                    mergeLeftJoin.next();

                    Cell<Integer, Integer, Iterator<Binding>> nextTuple = mergeLeftJoin.hasNext()
                            ? mergeLeftJoin.peek()
                            : null;

                    Integer nextKey = nextTuple != null ? nextTuple.getColumnKey() : null;

                    // Important: This is the server's end marker
                    boolean peekedRemoteEndMarker = BatchQueryRewriter.isRemoteEndMarker(nextKey) && nextTuple.getValue() != null;

                    if (peekedRemoteEndMarker) {
                        Log.info(QueryIterWrapperCache.class, "Peeked end marker - result set was not cut off");
                    }

                    // Note: A key is also completed if in total fewer tuples than
                    // the minimum known service result set size were processed
                    boolean isKeyCompleted = (nextTuple != null && nextTuple.getValue() != null);

                    // If not a single binding was delivered by the service then we certainly did not hit a result set limit
                    // If the end marker was seen then we also did not hit a result set limit
                    isKeyCompleted = isKeyCompleted || peekedRemoteEndMarker || processedBindingCount == 0;

                    long requestEnd = inputPart.hasLimit() ? LongMath.saturatedAdd(inputOffset, inputLimit) : Long.MAX_VALUE;
                    boolean isEndKnown = end < requestEnd;

                    if (isKeyCompleted) {
                        if (isEndKnown) {
                            if (currentOffset > 0) {
                                slice.mutateMetaData(metaData -> metaData.setKnownSize(end));
                            } else {
                                // If we saw no binding we don't know at which point the data actually ended
                                // but the start(=end) point is an upper limit
                                // Note: Setting the maximum size to zero will make it a known size of 0
                                slice.mutateMetaData(metaData -> metaData.setMaximumKnownSize(end));
                            }
                        } else {
                            // Data retrieval ended at a limit (e.g. we retrieved 10/10 items)
                            // We don't know whether there is more data - but it gives a lower bound
                            slice.mutateMetaData(metaData -> metaData.setMinimumKnownSize(end));
                        }
                    } else {
                        slice.mutateMetaData(metaData -> metaData.setMinimumKnownSize(end));
                    }
                    currentOffset = 0;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                cacheDataAccessor.unlock();
            }

            if (isRhsExhausted) {
                // Only initialize after unlocking the current cacheDataAccessor
                setupForNextLhsBinding();
            }
        }

        currentBatchIt = clientBatch.iterator();
    }

    protected void closeCurrentCacheResources() {
        if (cacheDataAccessor != null) {
            cacheDataAccessor.close();
            cacheDataAccessor = null;
        }

        if (claimedCacheEntry != null) {
            claimedCacheEntry.close();
            claimedCacheEntry = null;
        }
    }

    @Override
    protected void closeIterator() {
        closeCurrentCacheResources();
        inputIter.close();

        super.closeIterator();
    }
}
