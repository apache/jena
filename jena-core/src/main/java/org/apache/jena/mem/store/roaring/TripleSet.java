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

package org.apache.jena.mem.store.roaring;

import org.apache.jena.atlas.lib.Copyable;
import org.apache.jena.graph.Triple;
import org.apache.jena.mem.collection.FastHashSet;

/**
 * Set of triples that is backed by a {@link TripleSet}.
 */
public class TripleSet
        extends FastHashSet<Triple>
        implements Copyable<TripleSet> {

    public TripleSet() {
        super();
    }

    private TripleSet(final FastHashSet<Triple> setToCopy) {
        super(setToCopy);
    }

    @Override
    protected Triple[] newKeysArray(int size) {
        return new Triple[size];
    }

    /**
     * Create a copy of this set.
     *
     * @return TripleSet
     */
    @Override
    public TripleSet copy() {
        return new TripleSet(this);
    }
}
