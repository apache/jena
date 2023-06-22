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

package org.apache.jena.atlas.lib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

// Non-parameterized tests
public class TestCache2 {
    // Cache1
    @Test
    public void cache_10() {
        Cache<Integer, String> cache = CacheFactory.createOneSlotCache();
        String str = cache.getIfPresent(1);
        assertNull(str);

        cache.put(1, "1");
        str = cache.getIfPresent(1);
        assertEquals("1", str);

        cache.put(2, "2");
        str = cache.getIfPresent(1);
        assertNull(str);

        cache.put(1, "1");
        str = cache.getIfPresent(2);
        assertNull(str);
        str = cache.getIfPresent(1);
        assertEquals("1", str);
    }

    // Cache + getters
    @Test
    public void cacheGetter_1() {
        Cache<Integer, String> cache = CacheFactory.createCache(2);
        String str = cache.get(1, k->k.toString());
        assertEquals("1", str);
    }

    // Cache + getters
    @Test
    public void cacheGetter_2() {
        Cache<Integer, String> cache = CacheFactory.createCache(2);
        String str1 = cache.get(1, k->k.toString());
        String str2 = cache.get(2, k->k.toString());
        String str3 = cache.get(3, k->k.toString());
        assertEquals("1", str1);
        assertEquals("2", str2);
        assertEquals("3", str3);
        cache.put(1, "10");
        str1 = cache.getIfPresent(1);
        assertEquals("10", str1);
    }

}
