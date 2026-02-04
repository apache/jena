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

package org.apache.jena.sparql.service.enhancer.claimingcache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.jena.atlas.lib.Closeable;
import org.apache.jena.sparql.service.enhancer.concurrent.AutoLock;
import org.apache.jena.sparql.service.enhancer.util.LinkedList;
import org.apache.jena.sparql.service.enhancer.util.LinkedList.LinkedListNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;
import com.google.common.collect.Sets;

/**
 * Implementation of async claiming cache.
 * Claimed entries will never be evicted. Conversely, unclaimed items are added to a cache such that timely re-claiming
 * will be fast.
 *
 * Use cases:
 * - Resource sharing: Ensure that the same resource is handed to all clients requesting one by key.
 * - Resource pooling: Claimed resources will never be closed, but unclaimed resources (e.g. something backed by an input stream)
 *   may remain on standby for a while.
 *
 * Another way to view this class is as a mix of a map with weak values and a cache.
 *
 * @param <K>
 * @param <V>
 */
public class AsyncClaimingCacheImplCaffeine<K, V>
    implements AsyncClaimingCache<K, V>
{
    private static final Logger logger = LoggerFactory.getLogger(AsyncClaimingCacheImplCaffeine.class);

    // level1: Claimed items - those items will never be evicted as long as the references are not closed.
    protected Map<K, RefFuture<V>> level1;

    // level2: The caffine cache - items in this cache are not claimed and are subject to eviction according to configuration.
    protected AsyncCache<K, V> level2;
    protected Function<K, CompletableFuture<V>> level3AwareCacheLoader;

    // level3: Items evicted from level2 but caught by at least one eviction guard.
    protected Map<K, V> level3;

    // Runs atomically in the claim action after the entry exists in level1.
    protected BiConsumer<K, RefFuture<V>> claimListener;

    // Runs atomically in the unclaim action before the entry is removed from level1.
    protected BiConsumer<K, RefFuture<V>> unclaimListener;

    // A lock that prevents invalidation while entries are being loaded.
    protected ReentrantReadWriteLock invalidationLock = new ReentrantReadWriteLock();

    protected ReentrantReadWriteLock evictionGuardLock;

    // A list of predicates that decide whether a key is considered protected from eviction
    // Each predicate abstracts matching a set of keys, e.g. a range of integer values.
    // The predicates are assumed to always return the same result for the same argument.
    protected final LinkedList<Predicate<? super K>> evictionGuards;

    protected RemovalListener<K, V> atomicRemovalListener;

    protected Set<K> suppressedRemovalEvents;

    public AsyncClaimingCacheImplCaffeine(
            Map<K, RefFuture<V>> level1,
            AsyncCache<K, V> level2,
            Function<K, V> level3AwareCacheLoader,
            Map<K, V> level3,
            LinkedList<Predicate<? super K>> evictionGuards,
            ReentrantReadWriteLock evictionGuardLock,
            BiConsumer<K, RefFuture<V>> claimListener,
            BiConsumer<K, RefFuture<V>> unclaimListener,
            RemovalListener<K, V> atomicRemovalListener,
            Set<K> suppressedRemovalEvents
            ) {
        super();
        this.level1 = level1;
        this.level2 = level2;
        this.level3AwareCacheLoader = k -> CompletableFuture.completedFuture(level3AwareCacheLoader.apply(k));
        this.level3 = level3;
        this.evictionGuards = evictionGuards;
        this.evictionGuardLock = evictionGuardLock;
        this.claimListener = claimListener;
        this.unclaimListener = unclaimListener;
        this.atomicRemovalListener = atomicRemovalListener;
        this.suppressedRemovalEvents = suppressedRemovalEvents;
    }

    @Override
    public void cleanUp() {
        level2.synchronous().cleanUp();
    }

    protected SynchronizerMap<K> synchronizerMap = new SynchronizerMap<>();

    /**
     * Registers a predicate that 'caches' entries about to be evicted
     * When closing the registration then keys that have not moved back into the cache
     * by reference will be immediately evicted.
     */
    @Override
    public Closeable addEvictionGuard(Predicate<? super K> predicate) {
        LinkedListNode<Predicate<? super K>> linkedListNode;
        try (AutoLock lock = AutoLock.lock(evictionGuardLock.writeLock())) {
            if (logger.isDebugEnabled()) {
                logger.debug("Registering eviction guard: {}. Already active guards: {}.", predicate, evictionGuards);
            }
            linkedListNode = evictionGuards.append(predicate);
        }
        return () -> {
            try (AutoLock lock = AutoLock.lock(evictionGuardLock.writeLock())) {
                if (!linkedListNode.isLinked()) {
                    throw new IllegalStateException("Eviction guard " + predicate + " has already been removed.");
                }

                linkedListNode.unlink();
                if (logger.isDebugEnabled()) {
                    logger.debug("Removed eviction guard: {}. Now active guards: {}.", predicate, evictionGuards);
                }
                runLevel3Eviction();
            }
        };
    }

    /**
     * Remove all items from level3 that do not match any eviction guard.
     * Called while being synchronized on the evictionGuards.
     */
    protected void runLevel3Eviction() {
        Iterator<Entry<K, V>> it = level3.entrySet().iterator();
        while (it.hasNext()) {
            Entry<K, V> e = it.next();
            K k = e.getKey();
            V v = e.getValue();

            boolean isGuarded = evictionGuards.stream().anyMatch(p -> p.test(k));
            if (!isGuarded) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Evicting key {} which is no longer protected by remaining guards {}.", k, evictionGuards);
                }

                atomicRemovalListener.onRemoval(k, v, RemovalCause.COLLECTED);

                // Remove the key from level 3.
                // This is the only place where entries are removed from level 3
                it.remove();
            }
        }
    }

    @Override
    public RefFuture<V> claim(K key) {
        RefFuture<V> result = synchronizerMap.compute(key, synchronizer -> {
            Holder<Boolean> isFreshSecondaryRef = Holder.of(Boolean.FALSE);

            // Guard against concurrent invalidation requests
            RefFuture<V> secondaryRef;

            try (AutoLock autoLock = AutoLock.lock(invalidationLock.readLock())) {
                secondaryRef = level1.computeIfAbsent(key, k -> {
                    // Wrap the loaded reference such that closing the fully loaded reference adds it to level 2

                    if (logger.isTraceEnabled()) {
                        logger.trace("Claiming item [{}] from level2.", key);
                    }
                    CompletableFuture<V> future = level2.get(key, (kk, executor) -> level3AwareCacheLoader.apply(kk));

                    // level2.invalidate(key) triggers level2's removal listener but we are about to add the item to level1
                    // so we don't want to publish a removal event to the outside
                    suppressedRemovalEvents.add(key);
                    level2.synchronous().invalidate(key);
                    suppressedRemovalEvents.remove(key);

                    Holder<RefFuture<V>> holder = Holder.of(null);
                    Ref<CompletableFuture<V>> freshSecondaryRef =
                        RefImpl.create(future, synchronizer, () -> {

                            // This is the unclaim action.

                            RefFuture<V> v = holder.get();

                            if (unclaimListener != null) {
                                unclaimListener.accept(key, v);
                            }

                            // If the future has not completed yet then cancel it (nothing is done for completed futures)
                            // Then the future is added to level2.
                            // If the future fails then the cache entry is removed according to the cache API contract;
                            // otherwise the value will be readily available.
                            RefFutureImpl.cancelFutureOrCloseValue(future, null);
                            level1.remove(key);
                            if (logger.isTraceEnabled()) {
                                logger.trace("Item [{}] was unclaimed. Transferring to level2.", key);
                            }
                            level2.put(key, future);

                            // Run a check whether to free the key's proxy object
                            // in the synchronizerMap if the count is zero
                            synchronizer.clearEntryIfZero();
                        });
                    isFreshSecondaryRef.set(Boolean.TRUE);
                    RefFuture<V> r = RefFutureImpl.wrap(freshSecondaryRef);
                    holder.set(r);
                    return r;
                });
            }

            RefFuture<V> r = secondaryRef.acquire("secondary ref");
            if (claimListener != null) {
                claimListener.accept(key, r);
            }

            if (isFreshSecondaryRef.get()) {
                secondaryRef.close();
            }
            return r;
        });

        return result;
    }

    public static class Builder<K, V>
    {
        protected Caffeine<Object, Object> caffeine;
        protected CacheLoader<K, V> cacheLoader;
        protected BiConsumer<K, RefFuture<V>> claimListener;
        protected BiConsumer<K, RefFuture<V>> unclaimListener;
        protected RemovalListener<K, V> atomicRemovalListener;

        Builder<K, V> setCaffeine(Caffeine<Object, Object> caffeine) {
            this.caffeine = caffeine;
            return this;
        }

        public Builder<K, V> setClaimListener(BiConsumer<K, RefFuture<V>> claimListener) {
            this.claimListener = claimListener;
            return this;
        }

        public Builder<K, V> setUnclaimListener(BiConsumer<K, RefFuture<V>> unclaimListener) {
            this.unclaimListener = unclaimListener;
            return this;
        }

        public Builder<K, V> setCacheLoader(CacheLoader<K, V> cacheLoader) {
            this.cacheLoader = cacheLoader;
            return this;
        }

        /**
         * The given removal listener is run atomically both during eviction and invalidation.
         */
        public Builder<K, V> setAtomicRemovalListener(RemovalListener<K, V> atomicRemovalListener) {
            this.atomicRemovalListener = atomicRemovalListener;
            return this;
        }

        @SuppressWarnings("unchecked")
        public AsyncClaimingCacheImplCaffeine<K, V> build() {
            Map<K, RefFuture<V>> level1 = new ConcurrentHashMap<>();
            Map<K, V> level3 = new ConcurrentHashMap<>();
            LinkedList<Predicate<? super K>> evictionGuards = new LinkedList<>();
            ReentrantReadWriteLock evictionGuardLock = new ReentrantReadWriteLock();

            RemovalListener<K, V> level3AwareAtomicRemovalListener = (k, v, c) -> {
                if (logger.isDebugEnabled()) {
                    logger.debug("Removal triggered. Key: {}, Value: {}, Cause: {}.", k, v, c);
                }
                // Check for actual removal - key no longer present in level1.
                if (!level1.containsKey(k)) {

                    boolean isGuarded = false;
                    try (AutoLock lock = AutoLock.lock(evictionGuardLock.writeLock())) {
                        // Check whether this eviction from level2 should be caught by an eviction guard.
                        for (Predicate<? super K> evictionGuard : evictionGuards) {
                            isGuarded = evictionGuard.test(k);
                            if (isGuarded) {
                                if (logger.isDebugEnabled()) {
                                    logger.debug("Protecting this key from eviction: {}. Number of already protected keys: {}.", k, level3.size());
                                }

                                // try (AutoLock writeLock = AutoLock.lock(evictionGuardLock.writeLock())) {
                                // FIXME level3 is not a concurrent map - either change the map type or synchronize!
                                    level3.put(k, v);
                                // }
                                break;
                            }
                        }

                        if (!isGuarded) {
                            if (atomicRemovalListener != null) {
                                atomicRemovalListener.onRemoval(k, v, c);
                            }
                        }
                    }
                }
            };

            Set<K> suppressedRemovalEvents = Sets.newConcurrentHashSet();

            // Eviction listener is part of the cache's atomic operation
            // Important: The caffeine.evictionListener is atomic but NEVER called
            //   as a consequence of cache.invalidateAll()
            Caffeine<Object, Object> finalLevel2Builder = caffeine.evictionListener((k, v, c) -> {
                K kk = (K)k;
                boolean isEventSuppressed = suppressedRemovalEvents.contains(kk);
                if (!isEventSuppressed) {
                    // CompletableFuture<V> cfv = (CompletableFuture<V>)v;
                    CompletableFuture<V> cfv = CompletableFuture.completedFuture((V)v);
                    V vv = null;
                    if (cfv.isDone()) {
                        try {
                            vv = cfv.get();
                        } catch (InterruptedException | ExecutionException e) {
                            throw new RuntimeException("Should not happen", e);
                        }
                    }
                    level3AwareAtomicRemovalListener.onRemoval(kk, vv, c);
                }
            });

            // Cache loader that checks for existing items in level
            Function<K, V> level3AwareCacheLoader = k -> {
                Object[] tmp = new Object[] { null };
                // Atomically get and remove an existing key from level3.
                // Protect the access from concurrent eviction.
                try (AutoLock lock = AutoLock.lock(evictionGuardLock.writeLock())) {
                    level3.compute(k, (kk, v) -> {
                        tmp[0] = v;
                        return null;
                    });
                }

                V r = (V)tmp[0];
                if (r == null) {
                    try {
                        r = cacheLoader.load(k);
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                }
                return r;
            };

            AsyncCache<K, V> level2 = finalLevel2Builder.buildAsync();

            return new AsyncClaimingCacheImplCaffeine<>(level1, level2, level3AwareCacheLoader, level3, evictionGuards, evictionGuardLock, claimListener, unclaimListener, level3AwareAtomicRemovalListener, suppressedRemovalEvents);
        }
    }

    public static <K, V> Builder<K, V> newBuilder(Caffeine<Object, Object> caffeine) {
        Builder<K, V> result = new Builder<>();
        result.setCaffeine(caffeine);
        return result;
    }

    /**
     * Claim a key only if it is already present.
     *
     * This implementation is a best effort approach:
     * There is a very slim chance that just between testing a key for presence and claiming its entry
     * an eviction occurs - causing claiming of a non-present key and thus triggering a load action.
     */
    @Override
    public RefFuture<V> claimIfPresent(K key) {
        RefFuture<V> result = level1.containsKey(key) || level2.asMap().containsKey(key) ? claim(key) : null;
        return result;
    }

    @Override
    public void invalidateAll() {
        try (AutoLock autoLock = AutoLock.lock(invalidationLock.writeLock())) {
            // Cache<K, V> synchronousCache = level2.synchronous();
            //synchronousCache.asMap().keySet());
            List<K> keys = new ArrayList<>(level2.asMap().keySet());
            invalidateAllInternal(keys);
        }
    }

    @Override
    public void invalidateAll(Iterable<? extends K> keys) {
        try (AutoLock autoLock = AutoLock.lock(invalidationLock.writeLock())) {
            invalidateAllInternal(keys);
        }
    }

    private void invalidateAllInternal(Iterable<? extends K> keys) {
        Map<K, CompletableFuture<V>> map = level2.asMap();
        for (K key : keys) {
            map.compute(key, (k, vFuture) -> {
                V v = null;
                if (vFuture != null && vFuture.isDone()) {
                    try {
                        v = vFuture.get();
                    } catch (Exception e) {
                        if (logger.isWarnEnabled()) {
                            logger.warn("Detected cache entry that failed to load during invalidation", e);
                        }
                    }
                    atomicRemovalListener.onRemoval(k, v, RemovalCause.EXPLICIT);
                }
                return null;
            });
        }
    }

    /**
     * This method returns a snapshot of all keys across all internal cache levels.
     * It should only be used for informational purposes.
     */
    @Override
    public Collection<K> getPresentKeys() {
        Set<K> result = new LinkedHashSet<>();
        result.addAll(level1.keySet());
        result.addAll(level2.asMap().keySet());
        result.addAll(level3.keySet());
        return result;
    }
}
