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

package org.apache.jena.sparql.service.enhancer.slice.api;

import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;

import org.apache.jena.atlas.lib.Sync;

/**
 * A concurrently accessible sequence of data of possibly unknown size.
 *
 * @param <T> The array type for transferring data in blocks
 */
public interface Slice<T>
    extends SliceMetaDataBasic, HasArrayOps<T>, Sync
{
    ReadWriteLock getReadWriteLock();
    Condition getHasDataCondition();

    /**
     * Protect a set of ranges from eviction.
     * If the slice does make use of eviction then this method can return null.
     * Otherwise, a disposable must be returned. As long as it is not disposed, the
     * no data in the range may get lost due to eviction.
     *
     * This method should not be used directly but via {@link SliceAccessor#addEvictionGuard}.
     */
    Disposable addEvictionGuard(RangeSet<Long> range);

    /**
     * Read the metadata and check whether the slice has a known size and
     * there is only a single range of loaded data starting from offset 0 to that size.
     */
    default boolean isComplete() {
        boolean result = computeFromMetaData(false, metaData -> {
            long knownSize = metaData.getKnownSize();
            Set<Range<Long>> ranges = metaData.getLoadedRanges().asRanges();

            Range<Long> range = ranges.size() == 1 ? ranges.iterator().next() : null;

            long endpoint = range != null ? range.upperEndpoint() : -1;

            boolean r = endpoint >= 0 && knownSize == endpoint; // implied: knownSize >= 0
            return r;
        });

        return result;
    }

    default void mutateMetaData(Consumer<? super SliceMetaDataBasic> fn) {
        computeFromMetaData(true, metaData -> { fn.accept(metaData); return null; });
    }

    default void readMetaData(Consumer<? super SliceMetaDataBasic> fn) {
        computeFromMetaData(false, metaData -> { fn.accept(metaData); return null; });
    }

    /**
     * Lock the metadata and then invoke a value returning function on it.
     * Afterwards release the lock. Returns the obtained value.
     *
     * @param <X> The type of the value being computed
     * @param isWrite If true then lock for writing, otherwise for reading
     * @param fn The custom computing function
     * @return The computed value
     */
    default <X> X computeFromMetaData(boolean isWrite, Function<? super SliceMetaDataBasic, X> fn) {
        X result;
        ReadWriteLock rwl = this.getReadWriteLock();
        Lock lock = isWrite ? rwl.writeLock() : rwl.readLock();
        lock.lock();
        try {
            result = fn.apply(this);

            if (isWrite) {
                this.getHasDataCondition().signalAll();
            }
        } finally {
            lock.unlock();
        }

        return result;
    }

    /**
     * An accessor which allows for 'claiming' a sub-range of this slice. The claimed range can be incrementally
     * modified which may re-use already allocated resources (e.g. claimed pages) and thus improve performance.
     *
     * Sub-ranges of a slice can be loaded and iterated or inserted into.
     * The sub-ranges can be modified dynamically.
     */
    SliceAccessor<T> newSliceAccessor();

    /** Reset this slice - removes all data and sets the size to unknown */
    void clear();
}
