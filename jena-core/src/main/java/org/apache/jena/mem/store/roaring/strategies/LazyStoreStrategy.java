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

package org.apache.jena.mem.store.roaring.strategies;

import org.apache.jena.graph.Triple;
import org.apache.jena.mem.pattern.MatchPattern;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A lazy store strategy that defers the initialization of the index until it is needed.
 * This strategy is useful when the index is not always required, allowing for more efficient memory usage.
 * It uses a supplier to create a new instance of {@link EagerStoreStrategy} when needed.
 */
public class LazyStoreStrategy implements StoreStrategy {

    private final Supplier<EagerStoreStrategy> setCurrentStrategyToNewEagerStoreStrategy;

    public LazyStoreStrategy(final Supplier<EagerStoreStrategy> setCurrentStrategyToNewEagerStoreStrategy) {
        this.setCurrentStrategyToNewEagerStoreStrategy = setCurrentStrategyToNewEagerStoreStrategy;
    }

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
        return setCurrentStrategyToNewEagerStoreStrategy.get()
                .containsMatch(tripleMatch, pattern);
    }

    @Override
    public Stream<Triple> streamMatch(final Triple tripleMatch, final MatchPattern pattern) {
        return setCurrentStrategyToNewEagerStoreStrategy.get()
                .streamMatch(tripleMatch, pattern);
    }

    @Override
    public ExtendedIterator<Triple> findMatch(final Triple tripleMatch, final MatchPattern pattern) {
        return setCurrentStrategyToNewEagerStoreStrategy.get()
                .findMatch(tripleMatch, pattern);
    }
}
