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
package org.apache.jena.mem;

import org.apache.jena.mem.store.indexed.IndexedSetTripleStore;
import org.junit.Test;

import static org.apache.jena.testing_framework.GraphHelper.triple;
import static org.junit.Assert.*;

/**
 * Unit tests for the {@link IndexingStrategy} enum: verifies the five
 * documented strategies are present and that an {@link IndexedSetTripleStore}
 * can be constructed with each of them.
 */
public class IndexingStrategyTest {

    @Test
    public void allDocumentedStrategiesArePresent() {
        // If any of these names changes, GraphMemIndexedSet, the strategy
        // factory and a number of switch statements in the codebase will
        // need to be updated in lock-step.
        assertNotNull(IndexingStrategy.valueOf("EAGER"));
        assertNotNull(IndexingStrategy.valueOf("LAZY"));
        assertNotNull(IndexingStrategy.valueOf("LAZY_PARALLEL"));
        assertNotNull(IndexingStrategy.valueOf("MANUAL"));
        assertNotNull(IndexingStrategy.valueOf("MINIMAL"));
    }

    @Test
    public void valueOfRoundTripsForEveryConstant() {
        for (IndexingStrategy s : IndexingStrategy.values()) {
            assertEquals(s, IndexingStrategy.valueOf(s.name()));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void valueOfRejectsUnknownNames() {
        IndexingStrategy.valueOf("NOT_A_STRATEGY");
    }

    @Test
    public void everyStrategyConstructsAValidIndexedSetTripleStore() {
        // Sanity check: the enum is the public configuration knob for
        // IndexedSetTripleStore. Every value must lead to a usable store.
        for (IndexingStrategy s : IndexingStrategy.values()) {
            final var store = new IndexedSetTripleStore(s);
            assertEquals(s, store.getIndexingStrategy());
            assertEquals(0, store.countTriples());
        }
    }

    @Test
    public void graphMemIndexedSetExposesTheSelectedStrategy() {
        for (IndexingStrategy s : IndexingStrategy.values()) {
            final var graph = new GraphMemIndexedSet(s);
            assertEquals(s, graph.getIndexingStrategy());
        }
    }

    @Test
    public void eagerStrategyMaintainsIndexAfterAdd() {
        final var graph = new GraphMemIndexedSet(IndexingStrategy.EAGER);
        graph.add(triple("s p o"));
        assertTrue(graph.isIndexInitialized());
    }

    @Test
    public void lazyStrategyDelaysIndexBuildUntilFirstPatternLookup() {
        final var graph = new GraphMemIndexedSet(IndexingStrategy.LAZY);
        graph.add(triple("s p o"));
        assertFalse(graph.isIndexInitialized());
        // Pattern lookup triggers the build
        graph.contains(triple("s ?? ??"));
        assertTrue(graph.isIndexInitialized());
    }

    @Test
    public void manualStrategyRefusesPatternLookupUntilIndexInitialized() {
        final var graph = new GraphMemIndexedSet(IndexingStrategy.MANUAL);
        graph.add(triple("s p o"));
        assertThrows(UnsupportedOperationException.class,
                () -> graph.contains(triple("s ?? ??")));
        graph.initializeIndex();
        // After explicit initialization, lookups succeed.
        assertTrue(graph.contains(triple("s ?? ??")));
    }

    @Test
    public void minimalStrategyAnswersPatternLookupsWithoutBuildingIndex() {
        final var graph = new GraphMemIndexedSet(IndexingStrategy.MINIMAL);
        graph.add(triple("s p o"));
        assertFalse(graph.isIndexInitialized());
        // MINIMAL filters linearly instead of building an index.
        assertTrue(graph.contains(triple("s ?? ??")));
        assertFalse(graph.isIndexInitialized());
    }
}
