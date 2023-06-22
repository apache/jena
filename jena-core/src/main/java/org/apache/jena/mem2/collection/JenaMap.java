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

import org.apache.jena.util.iterator.ExtendedIterator;

import java.util.Spliterator;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A map from keys of type {@code K} to values of type {@code V}.
 *
 * @param <K> the type of the keys in the map
 * @param <V> the type of the values in the map
 */
public interface JenaMap<K, V> extends JenaMapSetCommon<K> {

    /**
     * Try to put a key-value pair into the map. If the key is already present, the value is updated.
     *
     * @param key   the key to put
     * @param value the value to put
     * @return true if the key-value pair was put into the map, false if the key was already present
     */
    boolean tryPut(K key, V value);

    /**
     * Put a key-value pair into the map. If the key is already present, the value is updated.
     *
     * @param key   the key to put
     * @param value the value to put
     */
    void put(K key, V value);

    /**
     * Get the value associated with the provided key.
     *
     * @param key the key to look up
     * @return the value associated with the key, or null if the key is not present
     */
    V get(K key);

    /**
     * Get the value associated with the provided key, or a default value if the key is not present.
     *
     * @param key          the key to look up
     * @param defaultValue the default value to return if the key is not present
     * @return the value associated with the key, or the default value if the key is not present
     */
    V getOrDefault(K key, V defaultValue);

    /**
     * Compute a value for a key if the key is not present.
     * The value is automatically put into the map.
     *
     * @param key                 the key whose value is to retrieved or computed
     * @param absentValueSupplier the function to compute a value for the key, if the key is not present
     * @return the value associated with the key, or the value computed by the function if the key is not present
     */
    V computeIfAbsent(K key, Supplier<V> absentValueSupplier);

    /**
     * Compute a value for a key.
     *
     * @param key            the key to compute a value for
     * @param valueProcessor the function to compute a value for the key. The function is passed the current value
     *                       associated with the key, or null if the key is not present. The function should return
     *                       the new value to associate with the key, or null if the key should be removed from the map.
     */
    void compute(K key, UnaryOperator<V> valueProcessor);

    /**
     * Get an iterator over the values in the map.
     *
     * @return an iterator over the values in the map
     */
    ExtendedIterator<V> valueIterator();

    /**
     * Get a spliterator over the values in the map.
     *
     * @return a spliterator over the values in the map
     */
    Spliterator<V> valueSpliterator();

    /**
     * Get a stream over the values in the map.
     *
     * @return a stream over the values in the map
     */
    default Stream<V> valueStream() {
        return StreamSupport.stream(valueSpliterator(), false);
    }

    /**
     * Get a parallel stream over the values in the map.
     *
     * @return a parallel stream over the values in the map
     */
    default Stream<V> valueStreamParallel() {
        return StreamSupport.stream(valueSpliterator(), false);
    }
}
