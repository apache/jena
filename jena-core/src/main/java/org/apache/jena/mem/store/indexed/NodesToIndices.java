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

package org.apache.jena.mem.store.indexed;

import org.apache.jena.atlas.lib.Copyable;
import org.apache.jena.graph.Node;
import org.apache.jena.mem.collection.FastHashMap;

/**
 * {@link FastHashMap} from {@link Node} to {@link IndexList}, used by the
 * eager indexing strategy as one of the three subject/predicate/object
 * indices ("for this node, here are the indices of all triples that mention
 * it in the corresponding slot").
 */
public class NodesToIndices
        extends FastHashMap<Node, IndexList>
        implements Copyable<NodesToIndices> {

    /**
     * Creates an empty map with the default initial capacity.
     */
    public NodesToIndices() {
        super();
    }

    /**
     * Copy constructor. Each value in the new map is an independent clone
     * of the corresponding {@link IndexList} in {@code mapToCopy}.
     *
     * @param mapToCopy the source map
     */
    public NodesToIndices(final NodesToIndices mapToCopy) {
        super(mapToCopy, IndexList::copy);
    }

    @Override
    protected Node[] newKeysArray(int size) {
        return new Node[size];
    }

    @Override
    protected IndexList[] newValuesArray(int size) {
        return new IndexList[size];
    }

    /**
     * Returns an independent copy of this map. Keys are shared (nodes are
     * immutable), values are cloned.
     *
     * @return a deep copy of this map
     */
    @Override
    public NodesToIndices copy() {
        return new NodesToIndices(this);
    }

    public IndexList getOrNew(Node key) {
        final var hashCode = key.hashCode();
        var pIndex = findPosition(key, hashCode);
        if (pIndex < 0) {
            if (tryGrowPositionsArrayIfNeeded()) {
                pIndex = ~findEmptySlotWithoutEqualityCheck(hashCode);
            }
            final var value = new IndexList();
            final var eIndex = getFreeKeyIndex();
            keys[eIndex] = key;
            hashCodesOrDeletedIndices[eIndex] = hashCode;
            values[eIndex] = value;
            positions[~pIndex] = ~eIndex;
            return value;
        } else {
            return values[~positions[pIndex]];
        }
    }
}