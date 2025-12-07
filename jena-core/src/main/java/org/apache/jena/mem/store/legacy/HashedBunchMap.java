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
package org.apache.jena.mem.store.legacy;

import org.apache.jena.graph.Node;
import org.apache.jena.mem.collection.HashCommonMap;

/**
 * A map from nodes to bunches of triples.
 */
public class HashedBunchMap extends HashCommonMap<Node, TripleBunch> {

    public HashedBunchMap() {
        super(10);
    }

    /**
     * Copy constructor.
     * The new map will contain all the same nodes as keys of the map to copy, but copies of the bunches as values .
     *
     * @param mapToCopy
     */
    private HashedBunchMap(final HashedBunchMap mapToCopy) {
        super(mapToCopy, TripleBunch::copy);
    }


    @Override
    protected Node[] newKeysArray(int size) {
        return new Node[size];
    }

    @Override
    protected TripleBunch[] newValuesArray(int size) {
        return new TripleBunch[size];
    }

    @Override
    public void clear() {
        super.clear(10);
    }

    /**
     * Create a copy of this map.
     * The new map will contain all the same nodes as keys of this map, but copies of the bunches as values.
     *
     * @return an independent copy of this map
     */
    public HashedBunchMap copy() {
        return new HashedBunchMap(this);
    }
}
