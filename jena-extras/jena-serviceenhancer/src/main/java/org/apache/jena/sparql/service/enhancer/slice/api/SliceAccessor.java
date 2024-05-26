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

import java.io.IOException;

import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;

/**
 * Abstraction over a sequence of pages to view their content as
 * consecutive items. The underlying pages may be claimed by multiple page ranges held by different clients.
 * Clients must eventually close pages ranges in order to allow for resources to be freed.
 *
 * Note: The page range abstraction enables consumers and producers to claim the same pages independently.
 * A consumer does not have to wait for the producers to advertise pages they are working on, instead the (low-level/internal) consumer can
 * simply claim the pages it wants to read in advance and then schedule any needed executors.
 *
 * The claim range can be mutated which performs only the necessary
 * (un)claim operations.
 *
 * @param <A> The array type for transferring data in blocks
 */
public interface SliceAccessor<A>
    extends AutoCloseable
{
    Slice<A> getSlice();

    /**
     * Adds an eviction guard (if the slice supports it) and binds its life cycle
     * to this accessor.
     * Closing an accessor thus also removes all eviction guards created by it.
     *
     * This method must be called after acquiring a read lock on the slice's metadata.
     *
     * @param ranges The set of ranges which to protected from eviction
     */
     void addEvictionGuard(RangeSet<Long> ranges);

     default void addEvictionGuard(Range<Long> range) {
         addEvictionGuard(ImmutableRangeSet.of(range));
     }

    /**
     * Set or update the claimed range - this will immediately request references to any pages providing the data for that range.
     * Pages outside of that range are considered as no longer needed pages will immediately be released.
     *
     * This method prepares the pages which can be subsequently locked.
     * Calling this method while the page range is locked ({@link #lock()}) raises an {@link IllegalStateException}.
     *
     * @param startOffset
     * @param endOffset
     */
    void claimByOffsetRange(long startOffset, long endOffset);

    /**
     * Lock the range for writing
     */
    void lock();

    /**
     * Put a sequence of items into the claimed range
     * Attempts to put items outside of the claimed range raises an {@link IndexOutOfBoundsException}
     *
     * The page range should be locked when calling this method.
     */
    void write(long offset, A arrayWithItemsOfTypeT, int arrOffset, int arrLength) throws IOException;

    /**
     * Read operation that assumes a prior check for available ranges has been performed.
     * Only use this method after locking.
     */
    int unsafeRead(A tgt, int tgtOffset, long srcOffset, int length) throws IOException;

    /**
     * Unlock the range
     */
    void unlock();

    /**
     * Releases all currently held pages.
     * Future requests via {@link #claimByOffsetRange(long, long)} are allowed.
     *
     */
    void releaseAll();

    /**
     * Closes the page range. Implementations of this method should call
     * {{@link #releaseAll()} and an addition prevent any further claims.
     */
    @Override
    void close();
}

