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

import java.io.Serializable;
import java.util.List;

import com.google.common.collect.RangeMap;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeMap;
import com.google.common.collect.TreeRangeSet;
import org.apache.jena.sparql.service.enhancer.slice.api.SliceMetaDataBasic;

public class SliceMetaDataImpl
    implements SliceMetaDataBasic, Serializable
{
    private static final long serialVersionUID = 1L;

    /**
     * If the value is null then the range is considered as successfully loaded.
     * If a throwable is present then there was an error processing the range
     */
    protected RangeSet<Long> loadedRanges;
    protected RangeMap<Long, List<Throwable>> failedRanges;
    protected long minimumKnownSize;
    protected long maximumKnownSize;

    public SliceMetaDataImpl() {
        this(
                TreeRangeSet.create(),
                TreeRangeMap.create(),
                0,
                Long.MAX_VALUE
        );
    }

    public SliceMetaDataImpl(RangeSet<Long> loadedRanges, RangeMap<Long, List<Throwable>> failedRanges,
            long minimumKnownSize, long maximumKnownSize) {
        super();
        this.loadedRanges = loadedRanges;
        this.failedRanges = failedRanges;
        this.minimumKnownSize = minimumKnownSize;
        this.maximumKnownSize = maximumKnownSize;
    }

    @Override
    public RangeSet<Long> getLoadedRanges() {
        return loadedRanges;
    }

    public void setLoadedRanges(RangeSet<Long> loadedRanges) {
        this.loadedRanges = loadedRanges;
    }

    @Override
    public RangeMap<Long, List<Throwable>> getFailedRanges() {
        return failedRanges;
    }

    @Override
    public long getMinimumKnownSize() {
        return minimumKnownSize;
    }

    @Override
    public long getMaximumKnownSize() {
        return maximumKnownSize;
    }

    @Override
    public void setMinimumKnownSize(long minimumKnownSize) {
        this.minimumKnownSize = minimumKnownSize;
    }

    @Override
    public void setMaximumKnownSize(long maximumKnownSize) {
        this.maximumKnownSize = maximumKnownSize;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((failedRanges == null) ? 0 : failedRanges.hashCode());
        result = prime * result + ((loadedRanges == null) ? 0 : loadedRanges.hashCode());
        result = prime * result + (int) (maximumKnownSize ^ (maximumKnownSize >>> 32));
        result = prime * result + (int) (minimumKnownSize ^ (minimumKnownSize >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SliceMetaDataImpl other = (SliceMetaDataImpl) obj;
        if (failedRanges == null) {
            if (other.failedRanges != null)
                return false;
        } else if (!failedRanges.equals(other.failedRanges))
            return false;
        if (loadedRanges == null) {
            if (other.loadedRanges != null)
                return false;
        } else if (!loadedRanges.equals(other.loadedRanges))
            return false;
        if (maximumKnownSize != other.maximumKnownSize)
            return false;
        if (minimumKnownSize != other.minimumKnownSize)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "SliceMetaDataImpl [loadedRanges=" + loadedRanges + ", failedRanges=" + failedRanges
                + ", minimumKnownSize=" + minimumKnownSize + ", maximumKnownSize=" + maximumKnownSize + "]";
    }
}
