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

import java.util.Collections;
import java.util.NavigableMap;
import java.util.TreeMap;

import com.google.common.collect.DiscreteDomain;

/**
 * Batch implementation backed by a navigable map.
 */
public class BatchImpl<K extends Comparable<K>, T>
    implements Batch<K, T>
{
    protected K firstKey;
    protected DiscreteDomain<K> discreteDomain;
    protected NavigableMap<K, T> items;

    // Note: The contained lists should be considered immutable
    protected NavigableMap<K, T> unmodifiableItems;

    public BatchImpl(K firstKey, DiscreteDomain<K> discreteDomain) {
        super();
        this.firstKey = firstKey;
        this.discreteDomain = discreteDomain;
        this.items = new TreeMap<>();
        this.unmodifiableItems = Collections.unmodifiableNavigableMap(items);
    }

    public static <T> Batch<Integer, T> forInteger() {
        return new BatchImpl<>(0, DiscreteDomain.integers());
    }

    public static <T> Batch<Long, T> forLong() {
        return new BatchImpl<>(0l, DiscreteDomain.longs());
    }

    /**
     * Items must be added with ascending indexes.
     * Adding an item with a lower index than already seen raises an IllegalArgumentException
     */
    @Override public void put(K index, T item) {
        K nextValidIndex = getNextValidIndex();
        int cmp = index.compareTo(nextValidIndex);
        if (cmp < 0) {
            throw new IllegalArgumentException("Index is lower than an existing one");
        }

        items.put(index, item);
    }

    @Override
    public K getNextValidIndex() {
        K result = items.isEmpty()
                ? firstKey
                : discreteDomain.next(items.lastKey());
        return result;
    }

    /** Returns an immutable view of the items in the batch */
    @Override
    public NavigableMap<K, T> getItems() {
        return unmodifiableItems;
    }

    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public String toString() {
        return "Batch [size=" + size() + ", itemRanges=" + items + "]";
    }
}
