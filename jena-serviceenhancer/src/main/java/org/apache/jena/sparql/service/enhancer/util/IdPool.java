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

import java.util.Iterator;
import java.util.TreeSet;

/**
 * A synchronized pool of integer ids.
 * Acquired ids must be eventually free'd using {@link #giveBack(int)}.
 */
public class IdPool {
    private volatile int i = 0;
    private final TreeSet<Integer> ids = new TreeSet<>();

    /** Allocate and return an unused id. */
    public synchronized int acquire() {
        int result;
        if (!ids.isEmpty()) {
            Iterator<Integer> it = ids.iterator();
            result = it.next();
            it.remove();
        } else {
            result = i++;
        }
        return result;
    }

    /** Return the size of recycled ids. */
    public int getRecyclePoolSize() {
        return ids.size();
    }

    /**
     * Return an id to the pool of available ids.
     *
     * @implNote If the highest acquired is returned then all consecutive
     *           trailing ids up to (excluding) the returned id are removed from the pool.
     *           In the worst case the will be overhead for completely emptying the pool.
     */
    public synchronized void giveBack(int v) {
        if (v >= i) {
            throw new IllegalArgumentException("Attempt to give back a value " + v + " which is greater than the largest generated one " + i + ".");
        }

        if (v + 1 == i) {
            // Case where the highest acquired value is returned
            --i;
            Iterator<Integer> it = ids.descendingIterator();
            while (it.hasNext()) {
                int id = it.next();
                if (id + 1 == i) {
                    it.remove();
                    --i;
                } else {
                    break;
                }
            }
        } else {
            if (ids.contains(v)) {
                throw new IllegalArgumentException("The value has already been given back: " + v);
            }

            ids.add(v);
        }
    }
}
