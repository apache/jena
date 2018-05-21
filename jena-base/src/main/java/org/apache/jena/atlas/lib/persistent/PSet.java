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

import java.util.stream.Stream;

import com.github.andrewoma.dexx.collection.Set;
import com.github.andrewoma.dexx.collection.Sets;

/**
 * A implementation of {@link PersistentSet} using {@link Set}.
 *
 * @param <E> the type of element in this set
 */
public class PSet<E> implements PersistentSet<E> {

	private final Set<E> wrappedSet;

	/**
	 * @param wrappedSet
	 */
	private PSet(final Set<E> w) {
		this.wrappedSet = w;
	}

	/**
	 * @return an empty set
	 */
	public static <E> PSet<E> empty() {
		return new PSet<>(Sets.of());
	}

	@Override
	public PersistentSet<E> plus(final E e) {
		return new PSet<>(wrappedSet.add(e));
	}

	@Override
	public PersistentSet<E> minus(final E e) {
		return new PSet<>(wrappedSet.remove(e));
	}

	@Override
	public boolean contains(final E e) {
		return wrappedSet.contains(e);
	}

	@Override
	public Stream<E> stream() {
		return wrappedSet.asSet().stream();
	}

    @Override
    public java.util.Set<E> asSet() {
        return wrappedSet.asSet();
    }
}
