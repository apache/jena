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

import java.util.function.Function;

import org.apache.jena.ext.com.google.common.collect.Range;
import org.apache.jena.ext.com.google.common.collect.RangeSet;
import org.apache.jena.query.Query;

/** Utility methods for working with guava {@link Range} instances */
public class RangeUtils {
    public static <C extends Comparable<C>> RangeSet<C> gaps(Range<C> requestRange, RangeSet<C> availableRanges) {
        RangeSet<C> absentRanges = availableRanges.complement();
        RangeSet<C> gaps = absentRanges.subRangeSet(requestRange);
        return gaps;
    }

    public static Range<Long> toRange(Query query) {
        Range<Long> result = toRange(query.getOffset(), query.getLimit());
        return result;
    }

    public static Range<Long> toRange(Long offset, Long limit) {
        Long min = offset == null || offset.equals(Query.NOLIMIT) ? 0 : offset;
        Long delta = limit == null || limit.equals(Query.NOLIMIT) ? null : limit;
        Long max = delta == null ? null : min + delta;

        Range<Long> result = max == null
                ? Range.atLeast(min)
                : Range.closedOpen(min, max);

        return result;
    }

    /** Shift the endpoints of the range of type 'Long' by the given distance */
    public static Range<Long> shiftLong(Range<Long> rawRange, long distance) {
        return map(rawRange, v -> v + distance);
    }

    /** Perform a map operation on all present endpoints */
    public static <I extends Comparable<I>, O extends Comparable<O>> Range<O> map(
            Range<I> range,
            Function<? super I, ? extends O> mapper)
    {
        Range<O> result;

        if (range.hasLowerBound()) {
            if (range.hasUpperBound()) {
                result = Range.range(mapper.apply(range.lowerEndpoint()), range.lowerBoundType(), mapper.apply(range.upperEndpoint()), range.upperBoundType());
            } else {
                result = Range.downTo(mapper.apply(range.lowerEndpoint()), range.lowerBoundType());
            }
        } else {
            if (range.hasUpperBound()) {
                result = Range.upTo(mapper.apply(range.upperEndpoint()), range.upperBoundType());
            } else {
                result = Range.all();
            }
        }

        return result;
    }
}
