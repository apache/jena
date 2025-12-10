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

package org.apache.jena.mem;

import org.apache.jena.mem.store.TripleStore;
import org.apache.jena.mem.store.fast.FastTripleStore;

/**
 * A graph that stores triples in memory. This class is not thread-safe.
 * <p>
 * Purpose: GraphMem2Fast is a strong candidate for becoming the new default in-memory graph in the upcoming Jena 5,
 * thanks to its improved performance and relatively minor increase in memory usage.
 * <p>
 * Faster than {@link GraphMemLegacy} (specially Graph#add, Graph#find and Graph#stream)
 * Removing triples is a bit slower than {@link GraphMemLegacy}.
 * Memory consumption is about 6-35% higher than {@link GraphMemLegacy}
 * Maps and sets are based on {@link org.apache.jena.mem.collection.FastHashBase}
 * Benefits from multiple small optimizations. (see: {@link FastTripleStore})
 * <p>
 * The heritage of GraphMem:
 * <ul>
 * <li>Also uses 3 hash-maps indexed by subjects, predicates, and objects
 * <li>Values of the maps also switch from arrays to hash sets for the triples
 * </ul>
 */
public class GraphMemFast extends GraphMem {

    public GraphMemFast() {
        super(new FastTripleStore());
    }

    private GraphMemFast(final TripleStore tripleStore) {
        super(tripleStore);
    }

    @Override
    public GraphMemFast copy() {
        return new GraphMemFast(this.tripleStore.copy());
    }
}
