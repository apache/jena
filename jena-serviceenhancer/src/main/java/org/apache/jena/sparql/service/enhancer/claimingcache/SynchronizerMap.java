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

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * A helper to synchronize actions on keys.
 * This class maps keys to proxy objects for synchronization and
 * provides methods to remove the proxy objects when they are no longer needed.
 *
 * @param <K> The key type.
 */
public class SynchronizerMap<K> {
    private ConcurrentHashMap<K, VolatileCounter> map = new ConcurrentHashMap<>();

    public static class SynchronizerImpl<K>
        implements Synchronizer
    {
        private SynchronizerMap<K> map;

        /** The id. This is the value of the counter when the key was acquired. */
        private final int id;
        private final K key;
        private final VolatileCounter counter;

        public SynchronizerImpl(SynchronizerMap<K> map, K key, VolatileCounter counter, int id) {
            super();
            this.map = map;
            this.key = key;
            this.counter = counter;
            this.id = id;
        }

        @Override
        public void accept(Runnable action) {
            synchronized (counter) {
                action.run();
            }
        }

        public void clearEntryIfZero() {
            map.clearEntryIfZero(key);
        }

        private void dec() {
            counter.dec();
        }

        @Override
        public String toString() {
            return "Synchronizer on " +  System.identityHashCode(map) + ", "
                    + String.join(", ", "id: " + id, "key: " + key, "current count: " + counter.get());
        }
    }

    public <T> T compute(K key, Function<SynchronizerImpl<K>, T> handler) {
        SynchronizerImpl<K> synchronizer = acquire(key);
        Holder<T> result = Holder.of(null);
        synchronizer.accept(() -> {
            // Decrement the refcount of the synchronizer. Does not clear the key's proxy object.
            synchronizer.dec();

            T r = handler.apply(synchronizer);
            result.set(r);
        });
        return result.get();
    }

    private SynchronizerImpl<K> acquire(K key) {
        Holder<Integer> id = Holder.of(null);
        VolatileCounter counter = map.compute(key, (k, before) -> {
            VolatileCounter r = before == null ? new VolatileCounter(1) : before.inc();
            id.set(r.get()); // Atomically expose the current value of the counter
            return r;
        });
        SynchronizerImpl<K> result = new SynchronizerImpl<>(this, key, counter, id.get());
        // System.out.println("Acquired: " + result);
        return result;
    }

    private void clearEntryIfZero(K key) {
        map.compute(key, this::clearEntryIfZero);
    }

    /** This method is run atomically */
    private VolatileCounter clearEntryIfZero(K key, VolatileCounter counter) {
        if (counter == null) {
            throw new IllegalStateException("No counter for key " + key);
        }
        int count = counter.get();
        if (counter.get() < 0) {
            throw new IllegalStateException("Negative count for key " + key + ": " + count);
        }

        VolatileCounter result = count == 0
                ? null
                : counter;

//        if (count == 0) {
//            System.out.println("Cleared entry for key " + key);
//        }

        return result;
    }
}
