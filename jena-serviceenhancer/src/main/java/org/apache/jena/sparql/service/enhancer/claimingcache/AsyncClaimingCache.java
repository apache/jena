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

package org.apache.jena.sparql.service.enhancer.claimingcache;

import java.util.Collection;
import java.util.function.Predicate;

import org.apache.jena.sparql.service.enhancer.slice.api.Disposable;

/**
 * Interface for an async cache that allows "claiming" entries.
 * Claiming means making explicit references to entries.
 * As long as an entry is claimed it will not be evicted.
 * Furthermore, eviction guards can be placed that prevent eviction even of
 * non-claimed entries.
 *
 * @param <K> The key type
 * @param <V> The value type
 */
public interface AsyncClaimingCache<K, V> {

    /**
     * Claim a reference to the key's entry.
     */
    RefFuture<V> claim(K key);

    /**
     * Claim a key for which loading has already been triggered or which is already loaded.
     * Calling this method should not trigger loading.
     */
    RefFuture<V> claimIfPresent(K key);

    /**
     * Protect eviction of certain keys as long as the guard is not disposed.
     * Disposable may immediately evict all no longer guarded items */
    Disposable addEvictionGuard(Predicate<? super K> predicate);

    /** Return a snapshot of all present keys */
    Collection<K> getPresentKeys();

    void invalidateAll();
    void invalidateAll(Iterable<? extends K> keys);
}
