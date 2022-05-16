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
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.jena.ext.com.google.common.collect.RangeMap;
import org.apache.jena.ext.com.google.common.collect.RangeSet;
import org.apache.jena.sparql.service.enhancer.slice.api.ArrayOps;
import org.apache.jena.sparql.service.enhancer.slice.api.Slice;
import org.apache.jena.sparql.service.enhancer.slice.api.SliceMetaDataBasic;

public abstract class SliceBase<A>
    implements Slice<A>
{

    protected ArrayOps<A> arrayOps;

    // A read/write lock for synchronizing reads/writes to the slice
    protected ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    // A condition that is signalled whenever content or metadata changes
    protected Condition hasDataCondition = readWriteLock.writeLock().newCondition();

    public SliceBase(ArrayOps<A> arrayOps) {
        super();
        this.arrayOps = arrayOps;
    }

    protected abstract SliceMetaDataBasic getMetaData();


    @Override
    public RangeSet<Long> getLoadedRanges() {
        return getMetaData().getLoadedRanges();
    }

    @Override
    public RangeMap<Long, List<Throwable>> getFailedRanges() {
        return getMetaData().getFailedRanges();
    }

    @Override
    public long getMinimumKnownSize() {
        return getMetaData().getMinimumKnownSize();
    }

    @Override
    public void setMinimumKnownSize(long size) {
        getMetaData().setMinimumKnownSize(size);
    }

    @Override
    public long getMaximumKnownSize() {
        return getMetaData().getMaximumKnownSize();
    }

    @Override
    public void setMaximumKnownSize(long size) {
        getMetaData().setMaximumKnownSize(size);
    }

    @Override
    public ReadWriteLock getReadWriteLock() {
        return readWriteLock;
    }

    @Override
    public Condition getHasDataCondition() {
        return hasDataCondition;
    }

    @Override
    public ArrayOps<A> getArrayOps() {
        return arrayOps;
    }
}
