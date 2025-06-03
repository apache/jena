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
import org.apache.jena.util.iterator.ExtendedIterator;

import java.util.stream.Stream;

/**
 * A manual store strategy that does not maintain an index.
 * This strategy is used when no indexing is required, and all operations are no-ops.
 * It throws an exception if any match operation is attempted before the index is initialized.
 */
public class ManualStoreStrategy implements StoreStrategy {
    @Override
    public void addToIndex(final Triple triple, final int index) {
        // No-op, as there is no index to add to.
    }

    @Override
    public void removeFromIndex(final Triple triple, final int index) {
        // No-op, as there is no index to add to.
    }

    @Override
    public void clearIndex() {
        // No-op, as there is no index to add to.
    }

    @Override
    public boolean containsMatch(final Triple tripleMatch, final MatchPattern pattern) {
        throw new UnsupportedOperationException("Index has not been initialized yet. Please initialize the index before using it.");
    }

    @Override
    public Stream<Triple> streamMatch(final Triple tripleMatch, final MatchPattern pattern) {
        throw new UnsupportedOperationException("Index has not been initialized yet. Please initialize the index before using it.");
    }

    @Override
    public ExtendedIterator<Triple> findMatch(final Triple tripleMatch, final MatchPattern pattern) {
        throw new UnsupportedOperationException("Index has not been initialized yet. Please initialize the index before using it.");
    }
}
