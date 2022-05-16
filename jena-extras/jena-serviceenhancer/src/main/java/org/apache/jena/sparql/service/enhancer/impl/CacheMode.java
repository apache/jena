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

package org.apache.jena.sparql.service.enhancer.impl;

public enum CacheMode {
    OFF,
    DEFAULT, // Read if cached, write if not yet cached
    // REFRESH, // Refresh caches; never read from cache but overwrite affected ranges in the cache
    CLEAR; // Like refresh but first clear all ranges of the cache entry

    /** Returns the argument unless it is null in which case the result is OFF */
    public static CacheMode effectiveMode(CacheMode cacheMode) {
        CacheMode result = cacheMode == null ? CacheMode.OFF : cacheMode;
        return result;
    }
}
