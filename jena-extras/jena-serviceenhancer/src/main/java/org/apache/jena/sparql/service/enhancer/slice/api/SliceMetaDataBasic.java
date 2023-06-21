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

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

import org.apache.jena.sparql.service.enhancer.impl.util.RangeUtils;

public interface SliceMetaDataBasic {
    RangeSet<Long> getLoadedRanges();
    RangeMap<Long, List<Throwable>> getFailedRanges();

    long getMinimumKnownSize();
    void setMinimumKnownSize(long size);

    long getMaximumKnownSize();
    void setMaximumKnownSize(long size);

    /** Updates the maximum known size iff the argument is less than the current known maximum */
    default SliceMetaDataBasic updateMaximumKnownSize(long size) {
        long current = getMaximumKnownSize();

        if (size < current) {
            setMaximumKnownSize(size);
        }

        return this;
    }

    /** Updates the minimum known size iff the argument is greater than the current known minimum */
    default SliceMetaDataBasic updateMinimumKnownSize(long size) {
        long current = getMinimumKnownSize();

        if (size > current) {
            setMinimumKnownSize(size);
        }

        return this;
    }

    default long getKnownSize() {
        long minSize = getMinimumKnownSize();
        long maxSize = getMaximumKnownSize();

        return minSize == maxSize ? minSize : -1;
    }

    default SliceMetaDataBasic setKnownSize(long size) {
        Preconditions.checkArgument(size >= 0, "Negative known size");

        setMinimumKnownSize(size);
        setMaximumKnownSize(size);

        return this;
    }

    default RangeSet<Long> getGaps(Range<Long> requestRange) {
        long maxKnownSize = getMaximumKnownSize();
        Range<Long> maxKnownRange = Range.closedOpen(0l, maxKnownSize);

        boolean isConnected = requestRange.isConnected(maxKnownRange);

        RangeSet<Long> result;
        if (isConnected) {
            Range<Long> effectiveRequestRange = requestRange.intersection(maxKnownRange);
            RangeSet<Long> loadedRanges = getLoadedRanges();
            result = RangeUtils.gaps(effectiveRequestRange, loadedRanges);
        } else {
            result = TreeRangeSet.create();
        }

        return result;
    }
}
