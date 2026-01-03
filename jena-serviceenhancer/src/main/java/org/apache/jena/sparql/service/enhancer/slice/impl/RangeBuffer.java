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

package org.apache.jena.sparql.service.enhancer.slice.impl;

import java.io.IOException;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;


public interface RangeBuffer<A>
    extends BufferLike<A>
{
    /** A set of ranges from which reading is valid. The range set may be shared among several range buffers and may thus include
     *  ranges outside of the range formed by the buffer's offset and capacity */
    RangeSet<Long> getRanges();

    /** The offset within the range set where this buffer starts; may be null if the offset cannot be represented in a single value such as
     * in a union of two buffers which have different offsets */
    Long getOffsetInRanges();

    Buffer<A> getBackingBuffer();

    /** Return a set of contributions by this buffer for the given lookup range
     *  While this method does not expose which parts of the global range are covered,
     *  this method allows to check whether there are any gaps in the read */
    RangeSet<Long> getCoveredRanges(Range<Long> localRange);

    default void transferFrom(long thisOffset, RangeBuffer<A> other, long otherOffset, long length) throws IOException {
        transfer(other, otherOffset, this, thisOffset, length);
    }

    default void transferTo(long thisOffset, RangeBuffer<A> other, long otherOffset, long length) throws IOException {
        transfer(this, thisOffset, other, otherOffset, length);
    }

    public static <A> void transfer(RangeBuffer<A> src, long srcOffset, RangeBuffer<A> tgt, long tgtOffset, long length) throws IOException {
        Range<Long> readRange = Range.closedOpen(srcOffset, srcOffset + length);

        RangeSet<Long> validReadRanges = src.getCoveredRanges(readRange);

        int n = 4 * 1024;
        A buffer = tgt.getArrayOps().create(n);

        // for (Range<Long> range :  src.getRanges().subRangeSet(readRange).asRanges()) {
        for (Range<Long> range : validReadRanges.asRanges()) {
            ContiguousSet<Long> cs = ContiguousSet.create(range, DiscreteDomain.longs());
            int remaining = cs.size();
            long first = cs.first();
            while (remaining > 0) {
                int x = Math.min(remaining, n);
                src.readInto(buffer, 0, first, x);
                long o = srcOffset - tgtOffset + first;
                tgt.write(o, buffer, 0, x);
                remaining -= x;
                first += x;
            }
        }
    }
}
