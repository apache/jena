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

package org.apache.jena.sparql.service.enhancer.impl.util;

import java.util.Collection;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;

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

    public static NavigableSet<Long> touchedPageIndices(Collection<Range<Long>> ranges, long pageSize) {
        NavigableSet<Long> result = ranges.stream()
            .flatMapToLong(range -> PageUtils.touchedPageIndices(range, pageSize))
            .boxed()
            .collect(Collectors.toCollection(TreeSet::new));

        return result;
    }

}
