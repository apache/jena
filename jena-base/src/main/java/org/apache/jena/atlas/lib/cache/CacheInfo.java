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

package org.apache.jena.atlas.lib.cache;

import org.apache.jena.ext.com.google.common.cache.CacheStats ;

/** Simplified version of Guava's CacheStats (and abstracting away from Guava cache implementation) */
public class CacheInfo {
    public final long requests;
    public final long hits;
    public final long misses;
    public final double hitRate;
    public final int cacheSize;

    public CacheInfo(int cacheSize, CacheStats stats) {
        this(cacheSize, stats.requestCount(), stats.hitCount(), stats.missCount(), stats.hitRate() ) ;
    }

    public CacheInfo(int cacheSize, long requests, long hits, long misses, double hitRate) {
        this.cacheSize = cacheSize ;
        this.requests = requests ;
        this.hits = hits ;
        this.misses = misses ;
        this.hitRate = hitRate ;
    }

    @Override
    public String toString() {
        return String.format("size=%,d  count=%,d  hits=%,d  misses=%,d  rate=%.1f",
                             cacheSize, requests, hits, misses, hitRate) ;
    }
}
