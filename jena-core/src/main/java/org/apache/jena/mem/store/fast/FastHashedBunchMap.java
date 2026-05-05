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
package org.apache.jena.mem.store.fast;

import org.apache.jena.atlas.lib.Copyable;
import org.apache.jena.graph.Node;
import org.apache.jena.mem.collection.FastHashMap;

/**
 * {@link FastHashMap} specialized to map a {@link Node} to its associated
 * {@link FastTripleBunch}. Used by {@link FastTripleStore} to maintain the
 * three subject/predicate/object indices.
 */
public class FastHashedBunchMap
        extends FastHashMap<Node, FastTripleBunch>
        implements Copyable<FastHashedBunchMap> {

    /**
     * Creates an empty bunch map with the default initial capacity.
     */
    public FastHashedBunchMap() {
        super();
    }

    /**
     * Copy constructor. The new map has the same node keys as
     * {@code mapToCopy}; each value is replaced by a deep copy of the
     * corresponding bunch (via {@link FastTripleBunch#copy()}) so that
     * mutations of either map cannot affect the other.
     *
     * @param mapToCopy the source map
     */
    private FastHashedBunchMap(final FastHashedBunchMap mapToCopy) {
        super(mapToCopy, FastTripleBunch::copy);
    }

    @Override
    protected Node[] newKeysArray(int size) {
        return new Node[size];
    }

    @Override
    protected FastTripleBunch[] newValuesArray(int size) {
        return new FastTripleBunch[size];
    }

    @Override
    public FastHashedBunchMap copy() {
        return new FastHashedBunchMap(this);
    }
}
