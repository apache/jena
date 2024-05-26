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
package org.apache.jena.mem2.map.node;

import org.apache.jena.graph.Node;
import org.apache.jena.mem2.collection.HashCommonMap;

public class HashCommonNodeMap extends HashCommonMap<Node, Object> {

    /**
     * Initialise this hashed thingy to have <code>initialCapacity</code> as its
     * capacity and the corresponding threshold. All the key elements start out
     * null.
     *
     * @param initialCapacity
     */
    public HashCommonNodeMap(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Initialise this hashed thingy to have <code>10</code> as its
     * capacity and the corresponding threshold. All the key elements start out
     * null.
     */
    public HashCommonNodeMap() {
        this(10);
    }

    @Override
    protected Node[] newKeysArray(int size) {
        return new Node[size];
    }

    @Override
    public void clear() {
        super.clear(10);
    }

    @Override
    protected Object[] newValuesArray(int size) {
        return new Object[size];
    }
}
