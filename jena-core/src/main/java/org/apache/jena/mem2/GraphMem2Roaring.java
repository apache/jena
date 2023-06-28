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

package org.apache.jena.mem2;

import org.apache.jena.mem2.store.roaring.RoaringTripleStore;

/**
 * A graph that stores triples in memory. This class is not thread-safe.
 * <p>
 * Purpose: GraphMem2Roaring is ideal for handling extremely large graphs. If you frequently work with such massive
 * data structures, this implementation could be your top choice.
 * <p>
 * Graph#contains is faster than {@link GraphMem2Fast}.
 * Removing triples is a bit slower than {@link GraphMem2Legacy}.
 * Better performance than GraphMem2Fast for operations with triple matches for the pattern S_O, SP_, and _PO on
 * large graphs,due to bit-operations to find intersecting triples.
 * Memory consumption is about 7-99% higher than {@link GraphMem2Legacy}
 * Suitable for really large graphs like bsbm-5m.nt.gz, bsbm-25m.nt.gz, and possibly even larger.
 * Simple and straightforward implementation.
 * No heritage of GraphMem.
 * <p>
 * Internal structure:
 * - One indexed hash set (same as GraphMem2Fast uses) that holds all triples.
 * - Three hash maps indexed by subjects, predicates, and objects with RoaringBitmaps as values.
 * - The bitmaps contain the indices of the triples in the central hash set.
 */
public class GraphMem2Roaring extends GraphMem2 {

    public GraphMem2Roaring() {
        super(new RoaringTripleStore());
    }

}
