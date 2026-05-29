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

package org.apache.jena.sparql.service.enhancer.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.service.enhancer.claimingcache.AsyncClaimingCache;
import org.apache.jena.sparql.service.enhancer.claimingcache.AsyncClaimingCacheImplCaffeine;
import org.apache.jena.sparql.service.enhancer.claimingcache.RefFuture;
import org.apache.jena.sparql.service.enhancer.impl.util.Lazy;
import org.apache.jena.sparql.service.enhancer.init.ServiceEnhancerConstants;
import org.apache.jena.sparql.service.enhancer.slice.api.ArrayOps;
import org.apache.jena.sparql.service.enhancer.slice.api.Slice;
import org.apache.jena.sparql.service.enhancer.slice.impl.SliceInMemoryCache;
import org.apache.jena.sparql.util.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.base.Preconditions;

public class ServiceResponseCache {
    private static final Logger logger = LoggerFactory.getLogger(ServiceResponseCache.class);

    // Default parameters (can cache up to 150K bindings for 300 queries amounting to up to 45M bindings)
    public static final int DFT_MAX_ENTRY_COUNT = 300;
    public static final int DFT_PAGE_SIZE = 10000;
    public static final int DFT_MAX_PAGE_COUNT = 15;

    // service / op / joinVars / binding / idx
    protected AsyncClaimingCache<ServiceCacheKey, ServiceCacheValue> cache;

    protected AtomicLong entryCounter = new AtomicLong(0l);

    /** Secondary index over cache keys */
    protected Map<Long, ServiceCacheKey> idToKey = new ConcurrentHashMap<>();

    public record SimpleConfig(int maxCacheSize, int pageSize, int maxPageCount) {}

    public ServiceResponseCache() {
        this(DFT_MAX_ENTRY_COUNT, DFT_PAGE_SIZE, DFT_MAX_PAGE_COUNT);
    }

    public ServiceResponseCache(SimpleConfig config) {
        this(config.maxCacheSize(), config.pageSize(), config.maxPageCount());
    }

    public ServiceResponseCache(int maxCacheSize, int pageSize, int maxPageCount) {
        this(maxCacheSize, () -> SliceInMemoryCache.create(ArrayOps.createFor(Binding.class), pageSize, maxPageCount));
    }

    public ServiceResponseCache(int maxCacheSize, Supplier<Slice<Binding[]>> sliceFactory) {
        super();
        AsyncClaimingCacheImplCaffeine.Builder<ServiceCacheKey, ServiceCacheValue> builder =
                AsyncClaimingCacheImplCaffeine.newBuilder(Caffeine.newBuilder().maximumSize(maxCacheSize));
        builder = builder
            .setCacheLoader(key -> {
                long id = entryCounter.getAndIncrement();
                idToKey.put(id, key);
                Slice<Binding[]> slice = sliceFactory.get();
                ServiceCacheValue r = new ServiceCacheValue(id, slice);
                if (logger.isDebugEnabled()) {
                    logger.debug("Loaded cache entry: {} - {}", key.getServiceNode(), id);
                }
                return r;
            })
            .setAtomicRemovalListener((k, v, c) -> {
                // We are not yet handling cancellation of loading a key; in that case the value may not yet be available
                // Handle it here here with null for v?
                if (v != null) {
                    long id = v.getId();
                    if (logger.isDebugEnabled()) {
                        logger.debug("Removed cache entry: {} - {}", k.getServiceNode(), id);
                    }
                    idToKey.remove(id);
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Removed cache entry without value {}", k);
                    }
                }
            });
        cache = builder.build();
    }

    public AsyncClaimingCache<ServiceCacheKey, ServiceCacheValue> getCache() {
        return cache;
    }

    public RefFuture<ServiceCacheValue> claim(ServiceCacheKey key) {
        return cache.claim(key);
    }

    public Map<Long, ServiceCacheKey> getIdToKey() {
        return idToKey;
    }

    public void invalidateAll() {
        cache.invalidateAll();
    }

    /** Return the global instance (if any) in ARQ.getContex() */
    public static ServiceResponseCache get() {
        return get(ARQ.getContext());
    }

    public static ServiceResponseCache get(Context cxt) {
        Lazy<ServiceResponseCache> tmp = cxt.get(ServiceEnhancerConstants.serviceCache);
        return tmp == null ? null : tmp.get();
    }

    public static void set(Context cxt, ServiceResponseCache cache) {
        cxt.put(ServiceEnhancerConstants.serviceCache, Lazy.ofInstance(cache));
    }

    public static void set(Context cxt, Lazy<ServiceResponseCache> cache) {
        cxt.put(ServiceEnhancerConstants.serviceCache, cache);
    }

    public static ServiceResponseCache.SimpleConfig buildConfig(Context cxt) {
        int maxEntryCount = cxt.getInt(ServiceEnhancerConstants.serviceCacheMaxEntryCount, ServiceResponseCache.DFT_MAX_ENTRY_COUNT);
        int pageSize = cxt.getInt(ServiceEnhancerConstants.serviceCachePageSize, ServiceResponseCache.DFT_PAGE_SIZE);
        int maxPageCount = cxt.getInt(ServiceEnhancerConstants.serviceCacheMaxPageCount, ServiceResponseCache.DFT_MAX_PAGE_COUNT);

        Preconditions.checkArgument(maxEntryCount > 0, ServiceEnhancerConstants.serviceCacheMaxEntryCount + " requires a value greater than 0");
        Preconditions.checkArgument(pageSize > 0, ServiceEnhancerConstants.serviceCachePageSize + " requires a value greater than 0");
        Preconditions.checkArgument(maxPageCount > 0, ServiceEnhancerConstants.serviceCacheMaxPageCount + " requires a value greater than 0");

        return new ServiceResponseCache.SimpleConfig(maxEntryCount, pageSize, maxPageCount);
    }
}
