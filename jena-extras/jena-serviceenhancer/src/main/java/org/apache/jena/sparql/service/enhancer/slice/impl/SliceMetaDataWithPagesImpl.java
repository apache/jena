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

import java.util.List;

import org.apache.jena.ext.com.google.common.collect.RangeMap;
import org.apache.jena.ext.com.google.common.collect.RangeSet;

public class SliceMetaDataWithPagesImpl
    extends SliceMetaDataImpl
    implements SliceMetaDataWithPages
{
    private static final long serialVersionUID = 1L;

    protected int pageSize;

    public SliceMetaDataWithPagesImpl() {
        this(1024 * 64);
    }

    public SliceMetaDataWithPagesImpl(int pageSize, RangeSet<Long> loadedRanges,
            RangeMap<Long, List<Throwable>> failedRanges, long minimumKnownSize, long maximumKnownSize) {
        super(loadedRanges, failedRanges, minimumKnownSize, maximumKnownSize);

        this.pageSize = pageSize;
    }

    public SliceMetaDataWithPagesImpl(int pageSize) {
        super();
        this.pageSize = pageSize;
    }

    @Override
    public int getPageSize() {
        return pageSize;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + pageSize;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        SliceMetaDataWithPagesImpl other = (SliceMetaDataWithPagesImpl) obj;
        if (pageSize != other.pageSize)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "SliceMetaDataWithPagesImpl [pageSize=" + pageSize + ", toString()=" + super.toString() + "]";
    }
}
