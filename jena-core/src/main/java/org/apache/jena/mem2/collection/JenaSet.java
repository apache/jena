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

/**
 * Set interface specialized for the use cases in triple store implementations.
 *
 * @param <E>
 */
public interface JenaSet<E> extends JenaMapSetCommon<E> {

    /**
     * Add the key to the set if it is not already present.
     *
     * @param key the key to add
     * @return true if the key was added, false if it was already present
     */
    boolean tryAdd(E key);

    /**
     * Add the key to the set without checking if it is already present.
     * Attention: This method must only be used if it is guaranteed that the key is not already present.
     *
     * @param key the key to add
     */
    void addUnchecked(E key);
}
