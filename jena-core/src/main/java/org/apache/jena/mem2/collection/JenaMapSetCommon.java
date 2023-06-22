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
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Common interface for {@link JenaMap} and {@link JenaSet}. *
 *
 * @param <E> the type of the keys/elements in the collection
 */
public interface JenaMapSetCommon<E> {

    /**
     * Clear the collection.
     */
    void clear();

    /**
     * @return the number of elements in the collection
     */
    int size();

    /**
     * @return true if the collection is empty
     */
    boolean isEmpty();

    /**
     * Check whether the collection contains a given key.
     *
     * @param key the key to look for
     * @return true if the collection contains the key
     */
    boolean containsKey(E key);

    /**
     * Check whether the collection contains any element matching the predicate.
     *
     * @param predicate the predicate to match
     * @return true if the collection contains any element matching the predicate
     */
    boolean anyMatch(Predicate<E> predicate);

    /**
     * Tries to remove a key from the collection.
     *
     * @param key the key to remove
     * @return true if the key was removed. If the key was not present, false is returned.
     */
    boolean tryRemove(E key);

    /**
     * Removes a key from the collection.
     * Attention: Implementations may assume that the key is present.
     *
     * @param key the key to remove
     */
    void removeUnchecked(E key);

    /**
     * Get an iterator over the keys in the collection.
     *
     * @return an iterator over the keys in the collection
     */
    ExtendedIterator<E> keyIterator();

    /**
     * Get a spliterator over the keys in the collection.
     *
     * @return a spliterator over the keys in the collection
     */
    Spliterator<E> keySpliterator();

    /**
     * Get a stream over the keys in the collection.
     *
     * @return a stream over the keys in the collection
     */
    default Stream<E> keyStream() {
        return StreamSupport.stream(keySpliterator(), false);
    }

    /**
     * Get a parallel stream over the keys in the collection.
     *
     * @return a parallel stream over the keys in the collection
     */
    default Stream<E> keyStreamParallel() {
        return StreamSupport.stream(keySpliterator(), true);
    }

}
