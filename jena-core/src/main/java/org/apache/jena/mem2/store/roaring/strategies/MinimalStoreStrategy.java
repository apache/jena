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

package org.apache.jena.mem2.store.roaring.strategies;

import org.apache.jena.graph.Triple;
import org.apache.jena.mem2.pattern.MatchPattern;
import org.apache.jena.mem2.store.roaring.TripleSet;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.util.stream.Stream;

/**
 * A minimal store strategy that does not maintain any bitmaps or indexes.
 * This strategy is used when no indexing is required.
 * The matching operations are performed directly on the set of triples.
 * This strategy is useful for scenarios where the overhead of maintaining an index is not justified,
 * such as when the dataset is small or when the performance of match operations is not critical.
 */
public class MinimalStoreStrategy implements StoreStrategy {
    private final TripleSet triples;

    public MinimalStoreStrategy(final TripleSet triples) {
        this.triples = triples;
    }

    @Override
    public void addToIndex(final Triple triple, final int index) {
        // No-op, as we do not store any bitmaps
    }

    @Override
    public void removeFromIndex(final Triple triple, final int index) {
        // No-op, as we do not store any bitmaps
    }

    @Override
    public void clearIndex() {
        // No-op, as we do not store any bitmaps
    }

    @Override
    public boolean containsMatch(final Triple tripleMatch, final MatchPattern pattern) {
        return this.triples.anyMatch(tripleMatch::matches);
    }

    @Override
    public Stream<Triple> streamMatch(final Triple tripleMatch, final MatchPattern pattern) {
        return this.triples.keyStream().filter(tripleMatch::matches);
    }

    @Override
    public ExtendedIterator<Triple> findMatch(final Triple tripleMatch, final MatchPattern pattern) {
        return this.triples.keyIterator().filterKeep(tripleMatch::matches);
    }
}
