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

import static java.util.Arrays.asList;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Function;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.Cache;

/**
 * A simple fixed size cache that uses the hash code to address a slot.
 * The size is always a power of two, to be able to use optimized bit-operations.
 * The clash policy is to overwrite.
 * <p>
 * The cache has very low overhead - there is no object creation during lookup or insert.
 * <p>
 * This cache is not thread safe.
 */
public class CacheSimple<K, V> implements Cache<K, V> {
    private final V[] values;
    private final K[] keys;
    private final int sizeMinusOne;
    private int currentSize = 0;

    /**
     * Constructs a fixes size cache.
     * The size is always a power of two, to be able to use optimized bit-operations.
     * @param miniumSize  If the size is already a power of two it will be used as fixed size for the cache,
     *                    otherwise the next larger power of two will be used.
     *                    (e.g. minimumSize = 10 results in 16 as fixed size for the cache)
     */
    public CacheSimple(int miniumSize) {
        var size = Integer.highestOneBit(miniumSize);
        if (size < miniumSize){
            size <<= 1;
        }
        this.sizeMinusOne = size-1;

        @SuppressWarnings("unchecked")
        V[] x = (V[])new Object[size];
        values = x;

        @SuppressWarnings("unchecked")
        K[] z = (K[])new Object[size];
        keys = z;
    }

    @Override
    public void clear() {
        Arrays.fill(values, null);
        Arrays.fill(keys, null);
        // drop handler
        currentSize = 0;
    }

    @Override
    public boolean containsKey(K key) {
        Objects.requireNonNull(key);
        return key.equals(keys[calcIndex(key)]);
    }

    private int calcIndex(K key) {
        return key.hashCode() & sizeMinusOne;
    }

    @Override
    public V getIfPresent(K key) {
        Objects.requireNonNull(key);
        final int idx = calcIndex(key);
        if (key.equals(keys[idx])) {
            return values[idx];
        }
        return null;
    }

    @Override
    public V get(K key, Function<K, V> function) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(function);
        final int idx = calcIndex(key);
        final boolean isExistingKeyNotNull = keys[idx] != null;
        if(isExistingKeyNotNull && keys[idx].equals(key)) {
            return values[idx];
        } else {
            final var value = function.apply(key);
            if(value != null) {
                values[idx] = value;
                if(!isExistingKeyNotNull) {
                    currentSize++;
                }
                keys[idx] = key;
            }
            return value;
        }
    }

    @Override
    public void put(K key, V thing) {
        Objects.requireNonNull(key);
        if (thing == null) {
            remove(key);
            return;
        }
        final int idx = calcIndex(key);
        if(!thing.equals(values[idx])) {
            values[idx] = thing;
        }
        if(!key.equals(keys[idx])) {
            if(keys[idx] == null) { //add value
                currentSize++;
            }
            keys[idx] = key;
        }
    }

    @Override
    public void remove(K key) {
        Objects.requireNonNull(key);
        final int idx = calcIndex(key);
        if (key.equals(keys[idx])) {
            keys[idx] = null;
            values[idx] = null;
            currentSize--;
        }
    }

    @Override
    public long size() {
        return currentSize;
    }

    @Override
    public Iterator<K> keys() {
        return Iter.iter(asList(keys)).filter(Objects::nonNull);
    }

    @Override
    public boolean isEmpty() {
        return currentSize == 0;
    }

    int getAllocatedSize() {
        return keys.length;
    }
}
