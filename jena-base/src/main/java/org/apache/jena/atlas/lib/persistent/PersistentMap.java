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

package org.apache.jena.atlas.lib.persistent;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Stream;

/**
 * A persistent map data structure.
 *
 * @param <K> the type of keys in the map
 * @param <V> the type of values in this map
 * @param <SelfType> the self-type of implementing classes
 */
public interface PersistentMap<K, V, SelfType extends PersistentMap<K, V, SelfType>> {

	/**
	 * @param key
	 * @return the value indexed by {@code key} if it exists
	 */
	default Optional<V> get(final K key) {
		return entryStream().filter(e -> e.getKey().equals(key)).map(Entry::getValue).findFirst();
	}

	/**
	 * @param key
	 * @param value
	 * @return a new {@code SelfType} with a new mapping from {@code key} to {@code value}
	 */
	SelfType plus(K key, V value);

	/**
	 * @param key
	 * @return a new {@code SelfType} without the mapping indexed by {@code key}
	 */
	SelfType minus(K key);

	/**
	 * @param key
	 * @return whether this map contains an entry indexed by {@code key}
	 */
	default boolean containsKey(final K key) {
		return get(key).isPresent();
	}

	/**
	 * @return a {@link Stream} of map entries
	 */
	Stream<Map.Entry<K, V>> entryStream();

	/**
	 * Sends this map's entries through a flattening function.
	 *
	 * @param f a function that flattens one entry into a stream
	 * @return a stream of flattened entries
	 */
	default <R> Stream<R> flatten(final BiFunction<K, V, Stream<R>> f) {
		return entryStream().flatMap(e -> f.apply(e.getKey(), e.getValue()));
	}
	
    /**
     * An immutable view of this as a {@link java.util.Map}.
     */
    java.util.Map<K, V> asMap();
}
