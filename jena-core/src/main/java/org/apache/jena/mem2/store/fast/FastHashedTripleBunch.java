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
package org.apache.jena.mem2.store.fast;

import org.apache.jena.graph.Triple;
import org.apache.jena.mem2.collection.FastHashSet;
import org.apache.jena.mem2.collection.JenaSet;

/**
 * A set of triples - backed by {@link FastHashSet}.
 */
public class FastHashedTripleBunch extends FastHashSet<Triple> implements FastTripleBunch {
    /**
     * Create a new triple bunch from the given set of triples.
     *
     * @param set the set of triples
     */
    public FastHashedTripleBunch(final JenaSet<Triple> set) {
        super((set.size() >> 1) + set.size()); //it should not only fit but also have some space for growth
        set.keyIterator().forEachRemaining(this::addUnchecked);
    }

    public FastHashedTripleBunch() {
        super();
    }

    @Override
    protected Triple[] newKeysArray(int size) {
        return new Triple[size];
    }

    @Override
    public boolean isArray() {
        return false;
    }
}
