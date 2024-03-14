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
package org.apache.jena.geosparql.implementation.index;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.jena.atlas.lib.Cache;
import org.apache.jena.atlas.lib.CacheFactory;
import org.apache.jena.atlas.lib.cache.CacheCaffeine;

import java.time.Duration;

/**
 *
 */
public class CacheConfiguration {

    public static final int UNLIMITED_MAP = -1;
    public static final int NO_MAP = 0;
    public static final long MAP_EXPIRY_INTERVAL = 5000l;
    public static final int UNLIMITED_INITIAL_CAPACITY = 50000;

    private CacheConfiguration() {
    }

    /**
     * Creates a cache
     *
     * @param maxSize            Maximum size, use {@value #NO_MAP} to disable caching, or {@value #UNLIMITED_MAP} to
     *                           have an unlimited cache size
     * @param expiryMilliseconds Expiry duration in milliseconds, entries which have not been accessed within this
     *                           interval will be automatically expired from the cache
     */
    public static <K, V> Cache<K, V> create(long maxSize, long expiryMilliseconds) {
        if (maxSize == NO_MAP) {
            return CacheFactory.createNullCache();
        }

        long actualMaxSize = maxSize > UNLIMITED_MAP ? maxSize : Long.MAX_VALUE;
        int actualInitialCapacity =
                actualMaxSize == Long.MAX_VALUE ? UNLIMITED_INITIAL_CAPACITY : (int) actualMaxSize / 4;
        return new CacheCaffeine<>(expiryMilliseconds > 0 ? Caffeine.newBuilder()
                                                                    .maximumSize(actualMaxSize)
                                                                    .initialCapacity(actualInitialCapacity)
                                                                    .expireAfterAccess(
                                                                            Duration.ofMillis(expiryMilliseconds))
                                                                    .build() :
                                   Caffeine.newBuilder()
                                           .maximumSize(actualMaxSize)
                                           .initialCapacity(actualInitialCapacity)
                                           .build());
    }
}
