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

package org.apache.jena.mem.store.indexed;

import static org.apache.jena.junit.GraphHelper.triple;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.apache.jena.graph.Triple;
import org.apache.jena.mem.IndexingStrategy;
import org.apache.jena.mem.pattern.PatternClassifier;
import org.apache.jena.mem.store.AbstractTripleStoreTest;
import org.apache.jena.mem.store.TripleStore;
import org.mockito.Mockito;

@ParameterizedClass
@MethodSource("provideArgs")
public class IndexedSetTripleStoreTest extends AbstractTripleStoreTest {

    public static Stream<Arguments> provideArgs() {
        List<Arguments> args = Arrays.stream(IndexingStrategy.values())
                .map(strategy -> Arguments.of(strategy))
                .toList();
        return args.stream();
    }

    public IndexingStrategy indexingStrategy;

    public IndexedSetTripleStoreTest(IndexingStrategy indexingStrategy) { this.indexingStrategy = indexingStrategy; }

    @Override
    protected TripleStore createTripleStore() {
        return switch (indexingStrategy) {
            case EAGER, LAZY, LAZY_PARALLEL, MINIMAL -> new IndexedSetTripleStore(indexingStrategy);
            case MANUAL -> setupStoreWithSpyForSpecialManualStrategy();
        };
    }

    private static boolean isPatternRequiringIndexing(final Triple tripleMatch) {
        return switch (PatternClassifier.classify(tripleMatch)) {
            case SUB_PRE_ANY, SUB_ANY_OBJ, SUB_ANY_ANY, ANY_PRE_OBJ, ANY_PRE_ANY, ANY_ANY_OBJ -> true;
            case ANY_ANY_ANY, SUB_PRE_OBJ -> false;
        };
    }

    private IndexedSetTripleStore setupStoreWithSpyForSpecialManualStrategy() {
        final var realStore = new IndexedSetTripleStore(IndexingStrategy.MANUAL);
        // Spy setup for the manual strategy
        final var spyStore = Mockito.spy(realStore);

        // Mock {@link TripleStore#contains(Triple)}
        Mockito.doAnswer(invocation -> {
            final Triple tripleMatch = invocation.getArgument(0);
            // If the triple match is a pattern that requires indexing, throw an exception
            assertThrows(UnsupportedOperationException.class, () -> realStore.contains(tripleMatch));

            // now initialize the index
            realStore.initializeIndex();
            // determine the result with the index
            final var result = realStore.contains(tripleMatch);
            // Reset the indexing strategy for the next call
            realStore.resetIndexingStrategy();
            // Return the result of the store with the index
            return result;
        }).when(spyStore).contains(Mockito.argThat(IndexedSetTripleStoreTest::isPatternRequiringIndexing));

        // Mock {@link TripleStore#find(Triple)}
        Mockito.doAnswer(invocation -> {
            final Triple tripleMatch = invocation.getArgument(0);
            // If the triple match is a pattern that requires indexing, throw an exception
            assertThrows(UnsupportedOperationException.class, () -> realStore.find(tripleMatch));

            // now initialize the index
            realStore.initializeIndex();
            // determine the result with the index
            final var result = realStore.find(tripleMatch);
            // Reset the indexing strategy for the next call
            realStore.resetIndexingStrategy();
            // Return the result of the store with the index
            return result;
        }).when(spyStore).find(Mockito.argThat(IndexedSetTripleStoreTest::isPatternRequiringIndexing));

        // Mock {@link TripleStore#stream(Triple)}
        Mockito.doAnswer(invocation -> {
            final Triple tripleMatch = invocation.getArgument(0);
            // If the triple match is a pattern that requires indexing, throw an exception
            assertThrows(UnsupportedOperationException.class, () -> realStore.stream(tripleMatch));

            // now initialize the index
            realStore.initializeIndex();
            // determine the result with the index
            final var result = realStore.stream(tripleMatch);
            // Reset the indexing strategy for the next call
            realStore.resetIndexingStrategy();
            // Return the result of the store with the index
            return result;
        }).when(spyStore).stream(Mockito.argThat(IndexedSetTripleStoreTest::isPatternRequiringIndexing));

        return spyStore;
    }

    private IndexedSetTripleStore getSutAsIndexedSetTripleStore() {
        return (IndexedSetTripleStore) super.sut;
    }

    @Test
    public void testGetIndexingStrategy() {
        // Given
        final var sut = getSutAsIndexedSetTripleStore();

        // Then
        assertEquals(indexingStrategy, sut.getIndexingStrategy());
    }

    @Test
    public void testIsIndexInitialized() {
        // Given
        final var sut = getSutAsIndexedSetTripleStore();

        // Then
        switch (sut.getIndexingStrategy()) {
            case EAGER:
                assertTrue(sut.isIndexInitialized());
                break;
            case LAZY, LAZY_PARALLEL, MANUAL, MINIMAL:
                assertFalse(sut.isIndexInitialized());
                break;
            default:
                throw new IllegalArgumentException("Unsupported indexing strategy: " + sut.getIndexingStrategy());
        }

        // When
        sut.add(triple("s p o"));

        // Then
        switch (sut.getIndexingStrategy()) {
            case EAGER:
                assertTrue(sut.isIndexInitialized());
                break;
            case LAZY, LAZY_PARALLEL, MANUAL, MINIMAL:
                assertFalse(sut.isIndexInitialized());
                break;
            default:
                throw new IllegalArgumentException("Unsupported indexing strategy: " + sut.getIndexingStrategy());
        }
    }

    @Test
    public void testLazyInitialization() {
        // Given
        final var sut = getSutAsIndexedSetTripleStore();
        sut.add(triple("s p o"));

        // When
        sut.contains(triple("s ?? o"));

        // Then
        switch (sut.getIndexingStrategy()) {
            case EAGER, LAZY, LAZY_PARALLEL:
                assertTrue(sut.isIndexInitialized());
                break;
            case MANUAL, MINIMAL:
                assertFalse(sut.isIndexInitialized());
                break;
            default:
                throw new IllegalArgumentException("Unsupported indexing strategy: " + sut.getIndexingStrategy());
        }
    }

    @Test
    public void testManualInitialization() {
        // Given
        final var sut = getSutAsIndexedSetTripleStore();

        // When
        sut.initializeIndex();

        // Then
        switch (sut.getIndexingStrategy()) {
            case EAGER, LAZY, LAZY_PARALLEL, MANUAL, MINIMAL:
                assertTrue(sut.isIndexInitialized());
                break;
            default:
                throw new IllegalArgumentException("Unsupported indexing strategy: " + sut.getIndexingStrategy());
        }
    }

    @Test
    public void testManualInitializationParallel() {
        // Given
        final var sut = getSutAsIndexedSetTripleStore();

        // When
        sut.initializeIndexParallel();

        // Then
        switch (sut.getIndexingStrategy()) {
            case EAGER, LAZY, LAZY_PARALLEL, MANUAL, MINIMAL:
                assertTrue(sut.isIndexInitialized());
                break;
            default:
                throw new IllegalArgumentException("Unsupported indexing strategy: " + sut.getIndexingStrategy());
        }
    }

    @Test
    public void testResetIndexingStrategy() {
        // Given
        final var sut = getSutAsIndexedSetTripleStore();
        sut.initializeIndex();

        // When
        sut.resetIndexingStrategy();

        // Then
        switch (sut.getIndexingStrategy()) {
            case EAGER:
                assertTrue(sut.isIndexInitialized());
                break;
            case LAZY, LAZY_PARALLEL, MANUAL, MINIMAL:
                assertFalse(sut.isIndexInitialized());
                break;
            default:
                throw new IllegalArgumentException("Unsupported indexing strategy: " + sut.getIndexingStrategy());
        }
    }
}
