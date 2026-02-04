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
package org.apache.jena.sparql.service.enhancer.cache;

import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Scheduler;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.apache.jena.atlas.lib.Closeable;
import org.apache.jena.sparql.service.enhancer.claimingcache.AsyncClaimingCacheImplCaffeine;
import org.apache.jena.sparql.service.enhancer.claimingcache.RefFuture;

public class TestAsyncClaimingCache {
    @Test
    @Disabled
    public void test() throws InterruptedException {

        int maxCacheSize = 10;

        AsyncClaimingCacheImplCaffeine<String, String> cache = AsyncClaimingCacheImplCaffeine.<String, String>newBuilder(
                Caffeine.newBuilder().maximumSize(maxCacheSize).expireAfterWrite(1, TimeUnit.MILLISECONDS).scheduler(Scheduler.systemScheduler()))
            .setCacheLoader(key -> "Loaded " + key)
            .setAtomicRemovalListener((k, v, c) -> System.out.println("Evicted " + k))
            .setClaimListener((k, v) -> System.out.println("Claimed: " + k))
            .setUnclaimListener((k, v) -> System.out.println("Unclaimed: " + k))
            .build();

        RefFuture<String> ref = cache.claim("hell");

        Closeable disposable = cache.addEvictionGuard(k -> k.contains("hell"));

        System.out.println(ref.await());
        ref.close();

        TimeUnit.SECONDS.sleep(5);

        RefFuture<String> reclaim = cache.claim("hell");

        disposable.close();

        reclaim.close();

        TimeUnit.SECONDS.sleep(5);

        System.out.println("done");
    }
}
