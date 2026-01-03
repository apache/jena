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

import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import org.apache.jena.sparql.service.enhancer.claimingcache.AsyncClaimingCache;
import org.apache.jena.sparql.service.enhancer.claimingcache.AsyncClaimingCacheImplGuava;
import org.apache.jena.sparql.service.enhancer.claimingcache.RefFuture;
import org.apache.jena.sparql.service.enhancer.impl.util.LockUtils;
import org.apache.jena.sparql.service.enhancer.impl.util.PageUtils;
import org.apache.jena.sparql.service.enhancer.slice.api.ArrayOps;
import org.apache.jena.sparql.service.enhancer.slice.api.Disposable;
import org.apache.jena.sparql.service.enhancer.slice.api.Slice;
import org.apache.jena.sparql.service.enhancer.slice.api.SliceMetaDataBasic;
import org.apache.jena.sparql.service.enhancer.slice.api.SliceWithPages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A slice implementation that starts to discard pages once there are too many.
 */
public class SliceInMemoryCache<A>
    extends SliceBase<A>
    implements SliceWithPages<A>
{
    private static final Logger logger = LoggerFactory.getLogger(SliceInMemoryCache.class);

    protected SliceMetaDataWithPages metaData;
    protected AsyncClaimingCache<Long, BufferView<A>> pageCache;

    protected SliceInMemoryCache(ArrayOps<A> arrayOps, int pageSize, AsyncClaimingCacheImplGuava.Builder<Long, BufferView<A>> cacheBuilder) {
        super(arrayOps);
        this.metaData = new SliceMetaDataWithPagesImpl(pageSize);
        this.pageCache = cacheBuilder
                .setCacheLoader(this::loadPage)
                .setAtomicRemovalListener(n -> evictPage(n.getKey()))
                .build();
    }

    public static <A> Slice<A> create(ArrayOps<A> arrayOps, int pageSize, int maxCachedPages) {
        AsyncClaimingCacheImplGuava.Builder<Long, BufferView<A>> cacheBuilder = AsyncClaimingCacheImplGuava.newBuilder(
            CacheBuilder.newBuilder().maximumSize(maxCachedPages));

        return new SliceInMemoryCache<>(arrayOps, pageSize, cacheBuilder);
    }

    protected void evictPage(long pageId) {
        long pageOffset = getPageOffsetForPageId(pageId);
        int pageSize = metaData.getPageSize();

        Range<Long> pageRange = Range.closedOpen(pageOffset, pageOffset + pageSize);
        if (logger.isDebugEnabled()) {
            logger.debug("Attempting to evict page " + pageId + " with range " + pageRange);
        }
        LockUtils.runWithLock(readWriteLock.writeLock(), () -> {
            metaData.getLoadedRanges().remove(pageRange);
        });
        if (logger.isDebugEnabled()) {
            logger.debug("Evicted page " + pageId + " with range " + pageRange);
        }

    }

    protected BufferView<A> loadPage(long pageId) {
        long pageOffset = getPageOffsetForPageId(pageId);

        Buffer<A> buffer = BufferOverArray.create(arrayOps, metaData.getPageSize());
        RangeBuffer<A> rangeBuffer = RangeBufferImpl.create(metaData.getLoadedRanges(), pageOffset, buffer);

        BufferView<A> result = new BufferView<>() {
            @Override
            public RangeBuffer<A> getRangeBuffer() {
                return rangeBuffer;
            }

            @Override
            public long getGeneration() {
                return 0;
            }

            @Override
            public ReadWriteLock getReadWriteLock() {
                return readWriteLock;
            }
        };
        return result;
    }

    @Override
    protected SliceMetaDataBasic getMetaData() {
        return metaData;
    }

    @Override
    public void sync() {
        // Nothing to do
    }

    @Override
    public long getPageSize() {
        return metaData.getPageSize();
    }

    @Override
    public RefFuture<BufferView<A>> getPageForPageId(long pageId) {
        return pageCache.claim(pageId);
    }

    @Override
    public Disposable addEvictionGuard(RangeSet<Long> ranges) {
        long pageSize = getPageSize();
        Set<Long> pageIds = PageUtils.touchedPageIndices(ranges.asRanges(), pageSize);

        if (logger.isDebugEnabled()) {
            logger.debug("Added eviction guard over ranges " + ranges + " affecting page ids " + pageIds);
        }

        Disposable core = pageCache.addEvictionGuard(key -> pageIds.contains(key));
        return () -> {
            if (logger.isDebugEnabled()) {
                logger.debug("Removed eviction guard over ranges " + ranges + " affecting page ids " + pageIds);
            }
            core.close();
        };
    }

    @Override
    public void clear() {
        ReadWriteLock rwl = getReadWriteLock();
        Lock writeLock = rwl.writeLock();
        writeLock.lock();
        try {
            pageCache.invalidateAll();
            setMinimumKnownSize(0);
            setMaximumKnownSize(Long.MAX_VALUE);
            getFailedRanges().clear();
            getLoadedRanges().clear();
        } finally {
            writeLock.unlock();
        }
    }
}
