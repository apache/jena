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
package org.apache.jena.mem2.store.legacy;

import org.apache.jena.graph.Triple;
import org.apache.jena.mem2.collection.HashCommonSet;
import org.apache.jena.mem2.collection.JenaSet;

/**
 * A bunch of triples, implemented as a set of triples.
 */
public class HashedTripleBunch extends HashCommonSet<Triple> implements TripleBunch {
    protected HashedTripleBunch(final JenaSet<Triple> b) {
        super(nextSize((int) (b.size() / LOAD_FACTOR)));
        b.keyIterator().forEachRemaining(this::addUnchecked);
    }

    public HashedTripleBunch() {
        super(8);
    }

    @Override
    protected Triple[] newKeysArray(int size) {
        return new Triple[size];
    }

    @Override
    public void clear() {
        super.clear(8);
    }

    @Override
    public boolean isArray() {
        return false;
    }
}
