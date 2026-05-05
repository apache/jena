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
 * Extension of {@link JenaSetHashOptimized} that exposes index-based access to elements.
 * Indices are stable handles to entries (returned by {@link #addAndGetIndex(Object)}) and remain
 * valid until the corresponding entry is removed.
 *
 * @param <E> the element type of the set
 */
public interface JenaSetIndexed<E> extends JenaSetHashOptimized<E> {

    /**
     * Add an element and return the index it was stored at. If the element
     * is already present, returns a negative value (typically the bitwise
     * complement of the existing index).
     *
     * @param key      the element to add
     * @return the index of the inserted element, or a negative value if the
     *         element was already present
     */
    int addAndGetIndex(final E key);

    /**
     * Returns the element stored at the given index.
     *
     * @param index the index to read
     * @return the element at that index
     */
    E getKeyAt(int index);

    /**
     * Returns the index of the given element, or a negative value if it is
     * not in the set.
     *
     * @param key the element to look up
     * @return the index of {@code key}, or a negative value if absent
     */
    int indexOf(E key);
}
