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
package org.apache.jena.mem.collection;

/**
 * Extension of {@link JenaMap} that exposes index-based access and lets callers
 * supply a precomputed hash code for the key. Indices are stable handles to
 * entries (returned by {@link #putAndGetIndex(Object, Object)}) and remain
 * valid until the corresponding entry is removed.
 * <p>
 * The hash-code overloads are a performance shortcut for callers that already
 * have the hash at hand (typically because the same key is stored in several
 * collections). The supplied hash code MUST equal {@code key.hashCode()}, or
 * the map will misbehave.
 *
 * @param <K> the type of the keys in the map
 * @param <V> the type of the values in the map
 */
public interface JenaMapIndexed<K, V> extends JenaMap<K, V> {

    /**
     * Returns the index of the entry with the given key, or a negative value
     * if no such entry exists.
     *
     * @param key the key to look up
     * @return the index of the entry, or a negative value if absent
     */
    int indexOf(K key);

    /**
     * Returns the key stored at the given index.
     *
     * @param index the index of the entry
     * @return the key at that index
     */
    K getKeyAt(int index);

    /**
     * Returns the value stored at the given index.
     *
     * @param index the index of the entry
     * @return the value at that index
     */
    V getValueAt(int index);

    /**
     * Put a key-value pair and return the index of the affected entry.
     * If the key is already present, its value is updated and the existing
     * index is returned.
     *
     * @param key   the key to put. ({@code null} is not allowed)
     * @param value the value to put. ({@code null} is not allowed)
     * @return the index of the entry holding {@code key}
     */
    int putAndGetIndex(K key, V value);
}
