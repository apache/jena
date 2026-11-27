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

package org.apache.jena.sparql.service.enhancer.impl.util;

import java.util.Collection;
import java.util.stream.LongStream;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

/**
 * Utility methods for working with (fixed-size) pages.
 */
public class PageUtils {
    public static long getPageIndexForOffset(long offset, long pageSize) {
        return offset / pageSize;
    }

    public static long getIndexInPage(long offset, long pageSize) {
        return offset % pageSize;
    }

    public static int getIndexInPage(long offset, int pageSize) {
        return (int)(offset % pageSize);
    }

    public static long getPageOffsetForId(long pageId, long pageSize) {
        return pageId * pageSize;
    }

    /**
     * Convert a range in item-offset space to page-id space.
     * For example, the offset range [1000,2000) with a page size of 1000 will become the page index range [1,2).
     */
    public static Range<Long> touchedPageIndexRange(Range<Long> range, long pageSize) {
        DiscreteDomain<Long> discreteDomain = DiscreteDomain.longs();
        ContiguousSet<Long> set = ContiguousSet.create(range, discreteDomain);
        long start = getPageIndexForOffset(set.first(), pageSize);
        long end = getPageIndexForOffset(set.last(), pageSize);
        Range<Long> rawRange = Range.closed(start, end);
        Range<Long> canonicalRange = rawRange.canonical(discreteDomain);
        return canonicalRange;
    }

    /**
     * Similar to {@link #touchedPageIndexRange(Range, long)} but for a set of ranges.
     */
    public static RangeSet<Long> touchedPageIndexRangeSet(Collection<Range<Long>> offsetRanges, long pageSize) {
        RangeSet<Long> result = TreeRangeSet.create();
        offsetRanges.forEach(offsetRange -> result.add(touchedPageIndexRange(offsetRange, pageSize)));
        return result;
    }

    /** Return a stream of the page indices touched by the range w.r.t. the page size */
    public static LongStream touchedPageIndices(Range<Long> range, long pageSize) {
        ContiguousSet<Long> set = ContiguousSet.create(range, DiscreteDomain.longs());
        LongStream result = set.isEmpty()
                ? LongStream.empty()
                : LongStream.rangeClosed(
                        getPageIndexForOffset(set.first(), pageSize),
                        getPageIndexForOffset(set.last(), pageSize));
        return result;
    }
}
