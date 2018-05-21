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

/**
 * A persistent set data structure.
 *
 * @param <E> the type of element in this set
 */
public interface PersistentSet<E> {

	/**
	 * @return an empty set
	 */
	static <T> PersistentSet<T> empty() {
		return PSet.empty();
	}

	/**
	 * @param e an element
	 * @return a new set with the elements of this set and {@code e}
	 */
	PersistentSet<E> plus(E e);

	/**
	 * @param e an element
	 * @return a new set with the elements of this set except {@code e}
	 */
	PersistentSet<E> minus(E e);

	/**
	 * @param e an element
	 * @return whether this set contains {@code e}
	 */
	boolean contains(E e);

	/**
	 * @return a {@link Stream} of the elements in this set
	 */
	Stream<E> stream();

    /**
     * An immutable view of this as a {@code java.util.Set}.
     */
    java.util.Set<E> asSet();
}
