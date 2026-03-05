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


package org.apache.jena.sparql.service.enhancer.util;

import java.util.TreeSet;

/**
 * A synchronized pool of integer ids.
 * Acquired ids must be eventually free'd using {@link #giveBack(int)}.
 */
public class IdPool {
    private volatile int nextId = 0;
    private final TreeSet<Integer> recycledIds = new TreeSet<>();

    /** Allocate and return an unused id. */
    public synchronized int acquire() {
        Integer first = recycledIds.pollFirst();
        int result = (first != null) ? first : nextId++;
        return result;
    }

    /** Return the size of recycled ids. */
    public synchronized int getRecycledIdsPoolSize() {
        return recycledIds.size();
    }

    /**
     * Return an id to the pool of available ids.
     *
     * @implNote If the highest acquired is returned then all consecutive
     *           trailing ids up to (excluding) the returned id are removed from the pool.
     *           In the worst case the will be overhead for completely emptying the pool.
     */
    public synchronized void giveBack(int v) {
        if (v < 0) {
            throw new IllegalArgumentException("Negative ids are not valid: " + v);
        }
        if (v >= nextId) {
            throw new IllegalArgumentException("Attempt to give back a value " + v + " which is greater than the largest generated one " + nextId + ".");
        }

        if (v + 1 == nextId) {
            // Case where the highest acquired value is returned.
            --nextId;
            // Keep compacting the tail while the new last id is also recycled.
            while (!recycledIds.isEmpty() && recycledIds.last() == nextId - 1) {
                recycledIds.removeLast();
                nextId--;
            }
        } else {
            if (!recycledIds.add(v)) {
                throw new IllegalArgumentException("The value has already been given back: " + v);
            }
        }
    }
}
