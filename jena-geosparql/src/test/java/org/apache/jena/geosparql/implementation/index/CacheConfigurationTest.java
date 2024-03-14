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

import org.apache.jena.atlas.lib.Cache;
import org.apache.jena.atlas.lib.cache.CacheCaffeine;
import org.junit.Assert;
import org.junit.Test;

public class CacheConfigurationTest {

    @Test
    public void cache_creation_null() {
        Cache<String, String> cache = CacheConfiguration.create(CacheConfiguration.NO_MAP, 0L);
        cache.put("test", "test");
        Assert.assertFalse(cache.containsKey("test"));
    }

    @Test
    public void cache_creation_simple_01() {
        Cache<Integer, String> cache = CacheConfiguration.create(100, 0L);

        for (int i = 0; i < 1_000; i++) {
            cache.put(i, Integer.toString(i));
        }

        // Should be fewer items than we added since we exceeded the max size
        forceCleanUp(cache);
        Assert.assertNotEquals(1_000, cache.size());
    }

    @Test
    public void cache_creation_simple_02() {
        Cache<Integer, String> cache = CacheConfiguration.create(100_000, 0L);

        for (int i = 0; i < 1_000; i++) {
            cache.put(i, Integer.toString(i));
        }

        // Should be exactly as many items as we added as we aren't remotely near the max size
        forceCleanUp(cache);
        Assert.assertEquals(1_000, cache.size());
    }

    @Test
    public void cache_creation_unlimited() {
        Cache<Integer, String> cache = CacheConfiguration.create(CacheConfiguration.UNLIMITED_MAP, 0L);

        for (int i = 0; i < 1_000; i++) {
            cache.put(i, Integer.toString(i));
        }

        // Should be exactly as many items as we added as we aren't remotely near the max size
        forceCleanUp(cache);
        Assert.assertEquals(1_000, cache.size());
    }

    @Test
    public void cache_creation_expiring() throws InterruptedException {
        Cache<Integer, String> cache = CacheConfiguration.create(100, 100);

        for (int i = 0; i < 1_000; i++) {
            cache.put(i, Integer.toString(i));
        }

        // Should be fewer items than we added since we exceeded the max size
        forceCleanUp(cache);
        Assert.assertNotEquals(1_000L, cache.size());

        // Wait for everything to expire and check now empty
        Thread.sleep(250L);
        forceCleanUp(cache);
        Assert.assertEquals(0L, cache.size());
    }

    private static <K, V> void forceCleanUp(Cache<K, V> cache) {
        if (cache instanceof CacheCaffeine<K, V> caffeine) {
            caffeine.cleanUp();
        }
    }
}
