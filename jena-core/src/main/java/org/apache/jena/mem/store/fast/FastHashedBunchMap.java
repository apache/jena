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
package org.apache.jena.mem.store.fast;

import org.apache.jena.atlas.lib.Copyable;
import org.apache.jena.graph.Node;
import org.apache.jena.mem.collection.FastHashMap;

/**
 * Map from nodes to triple bunches.
 */
public class FastHashedBunchMap
        extends FastHashMap<Node, FastTripleBunch>
        implements Copyable<FastHashedBunchMap> {

    public FastHashedBunchMap() {
        super();
    }

    /**
     * Copy constructor.
     * The new map will contain all the same nodes as keys of the map to copy, but copies of the bunches as values .
     *
     * @param mapToCopy
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
