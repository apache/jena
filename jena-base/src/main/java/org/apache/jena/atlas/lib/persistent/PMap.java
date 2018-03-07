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

import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Stream;

import com.github.andrewoma.dexx.collection.Map;
import com.github.andrewoma.dexx.collection.Maps;

/**
 * An implementation of {@link PersistentMap} using {@link com.github.andrewoma.dexx.collection.Map}.
 *
 * @param <K> the type of keys in the map
 * @param <V> the type of values in this map
 * @param <SelfType> the self-type of implementing classes
 */
public abstract class PMap<K, V, SelfType extends PMap<K, V, SelfType>> implements PersistentMap<K, V, SelfType> {

	private final Map<K, V> wrappedMap;

	private Map<K, V> wrappedMap() {
		return wrappedMap;
	}

	/**
	 * @param wrappedMap
	 */
	protected PMap(final Map<K, V> wrappedMap) {
		this.wrappedMap = wrappedMap;
	}

	protected PMap() {
		this(Maps.of());
	}

    @Override
    public java.util.Map<K, V> asMap() {
        return wrappedMap.asMap();
    }

	/**
	 * @param wrapped a map that supplies the internal state to be used
	 * @return a new {@code SelfType} that holds the supplied internal state
	 */
	abstract protected SelfType wrap(final Map<K, V> wrapped);

	@Override
	public SelfType plus(final K key, final V value) {
		return wrap(wrappedMap().put(key, value));
	}

	@Override
	public SelfType minus(final K key) {
		return wrap(wrappedMap().remove(key));
	}

	@Override
	public Optional<V> get(final K key) {
		return Optional.ofNullable(wrappedMap().get(key));
	}

	@Override
	public boolean containsKey(final K key) {
		return wrappedMap().containsKey(key);
	}

	@Override
	public Stream<Entry<K, V>> entryStream() {
		return wrappedMap().asMap().entrySet().stream();
	}
}
