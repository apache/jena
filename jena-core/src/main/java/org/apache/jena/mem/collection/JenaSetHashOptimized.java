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
 * Extension of {@link JenaSet} that lets callers supply a precomputed hash
 * code.
 * <p>
 * Attention: any caller-supplied hash code MUST equal {@code E.hashCode()};
 * if it does not, the set will misbehave.
 *
 * @param <E> the element type of the set
 */
public interface JenaSetHashOptimized<E> extends JenaSet<E> {

    /**
     * Add an element with the given precomputed hash code if it is not
     * already present.
     *
     * @param key      the element to add
     * @param hashCode {@code key.hashCode()}
     * @return {@code true} if added, {@code false} if already present
     */
    boolean tryAdd(E key, int hashCode);

    /**
     * Add an element with the given precomputed hash code without checking
     * whether it is already present. The caller MUST ensure the key is absent.
     *
     * @param key      the element to add
     * @param hashCode {@code key.hashCode()}
     */
    void addUnchecked(E key, int hashCode);

    /**
     * Try to remove an element with the given precomputed hash code.
     *
     * @param key      the element to remove
     * @param hashCode {@code key.hashCode()}
     * @return {@code true} if removed, {@code false} if it was not present
     */
    boolean tryRemove(E key, int hashCode);

    /**
     * Remove an element assumed to be present, with the given precomputed
     * hash code. Behavior is undefined if the element is not in the set.
     *
     * @param key      the element to remove
     * @param hashCode {@code key.hashCode()}
     */
    void removeUnchecked(E key, int hashCode);
}
