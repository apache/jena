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
package org.apache.jena.mem2.collection;

import org.apache.jena.mem2.iterator.SparseArrayIterator;
import org.apache.jena.mem2.spliterator.SparseArraySpliterator;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.util.ConcurrentModificationException;
import java.util.Spliterator;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Implementation of {@link JenaMap} based on {@link HashCommonBase}.
 */
public abstract class HashCommonMap<K, V> extends HashCommonBase<K> implements JenaMap<K, V> {

    protected V[] values;

    /**
     * Initialise this hashed thingy to have <code>initialCapacity</code> as its
     * capacity and the corresponding threshold. All the key elements start out
     * null.
     */
    protected HashCommonMap(int initialCapacity) {
        super(initialCapacity);
        this.values = newValuesArray(keys.length);
    }

    @Override
    public void clear(int initialCapacity) {
        super.clear(initialCapacity);
        this.values = newValuesArray(keys.length);
    }

    protected abstract V[] newValuesArray(int size);

    @Override
    public boolean tryPut(K key, V value) {
        final var slot = findSlot(key);
        if (slot < 0) {
            values[~slot] = value;
            return false;
        }
        keys[slot] = key;
        values[slot] = value;
        if (++size > threshold) grow();
        return true;
    }

    @Override
    public void put(K key, V value) {
        final var slot = findSlot(key);
        if (slot < 0) {
            values[~slot] = value;
            return;
        }
        keys[slot] = key;
        values[slot] = value;
        if (++size > threshold) grow();
    }

    @Override
    public V get(K key) {
        final var slot = findSlot(key);
        if (slot < 0) return values[~slot];
        return null;
    }

    @Override
    public V getOrDefault(K key, V defaultValue) {
        final var slot = findSlot(key);
        if (slot < 0) return values[~slot];
        return defaultValue;
    }

    @Override
    public V computeIfAbsent(K key, Supplier<V> absentValueSupplier) {
        final var slot = findSlot(key);
        if (slot < 0) return values[~slot];
        final var value = absentValueSupplier.get();
        keys[slot] = key;
        values[slot] = value;
        if (++size > threshold) grow();
        return value;
    }

    @Override
    public void compute(K key, UnaryOperator<V> valueProcessor) {
        final var slot = findSlot(key);
        if (slot < 0) {
            final var value = valueProcessor.apply(values[~slot]);
            if (value == null) {
                removeFrom(~slot);
            } else {
                values[~slot] = value;
            }
        } else {
            final var value = valueProcessor.apply(null);
            if (value == null)
                return;
            keys[slot] = key;
            values[slot] = value;
            if (++size > threshold) grow();
        }
    }


    @Override
    protected void grow() {
        final K[] oldContents = keys;
        final V[] oldValues = values;
        keys = newKeysArray(calcGrownCapacityAndSetThreshold());
        values = newValuesArray(keys.length);
        for (int i = 0; i < oldContents.length; i += 1) {
            final K key = oldContents[i];
            if (key != null) {
                final int slot = findSlot(key);
                keys[slot] = key;
                values[slot] = oldValues[i];
            }
        }
    }

    /**
     * Remove the triple at element <code>i</code> of <code>contents</code>.
     * This is an implementation of Knuth's Algorithm R from tAoCP vol3, p 527,
     * with exchanging of the roles of i and j so that they can be usefully renamed
     * to <i>here</i> and <i>scan</i>.
     * <p>
     * It relies on linear probing but doesn't require a distinguished REMOVED
     * value. Since we resize the table when it gets fullish, we don't worry [much]
     * about the overhead of the linear probing.
     * <p>
     * Iterators running over the keys may miss elements that are moved from the
     * bottom of the table to the top because of Iterator::remove. removeFrom
     * returns such a moved key as its result, and null otherwise.
     */
    @Override
    protected void removeFrom(int here) {
        size -= 1;
        while (true) {
            keys[here] = null;
            values[here] = null;
            int scan = here;
            while (true) {
                if (--scan < 0) scan += keys.length;
                if (keys[scan] == null) return;
                final int r = initialIndexFor(keys[scan].hashCode());
                if ((scan > r || r >= here) && (r >= here || here >= scan) && (here >= scan || scan > r)) {
                    keys[here] = keys[scan];
                    values[here] = values[scan];
                    here = scan;
                    break;
                }
            }
        }
    }

    @Override
    public ExtendedIterator<V> valueIterator() {
        final var initialSize = size;
        final Runnable checkForConcurrentModification = () -> {
            if (size != initialSize) throw new ConcurrentModificationException();
        };
        return new SparseArrayIterator<>(values, checkForConcurrentModification);
    }

    @Override
    public Spliterator<V> valueSpliterator() {
        final var initialSize = size;
        final Runnable checkForConcurrentModification = () -> {
            if (size != initialSize) throw new ConcurrentModificationException();
        };
        return new SparseArraySpliterator<>(values, checkForConcurrentModification);
    }
}
