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

import org.apache.jena.graph.Triple;
import org.apache.jena.mem.IndexingStrategy;
import org.apache.jena.mem.pattern.PatternClassifier;
import org.apache.jena.mem.store.AbstractTripleStoreTest;
import org.apache.jena.mem.store.TripleStore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collection;

import static org.apache.jena.testing_framework.GraphHelper.triple;
import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class RoaringTripleStoreTest extends AbstractTripleStoreTest {

    @Parameterized.Parameter
    public IndexingStrategy indexingStrategy;

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.stream(IndexingStrategy.values())
                .map(strategy -> new Object[]{strategy})
                .toList();
    }

    @Override
    protected TripleStore createTripleStore() {
        switch (indexingStrategy) {
            case EAGER, LAZY, LAZY_PARALLEL, MINIMAL:
                return new RoaringTripleStore(indexingStrategy);
            case MANUAL:
                return setupStoreWithSpyForSpecialManualStrategy();
            default:
                throw new IllegalArgumentException("Unsupported indexing strategy: " + indexingStrategy);
        }
    }

    private static boolean isPatternRequiringIndexing(final Triple tripleMatch) {
        switch(PatternClassifier.classify(tripleMatch)) {
            case SUB_PRE_ANY, SUB_ANY_OBJ, SUB_ANY_ANY, ANY_PRE_OBJ, ANY_PRE_ANY, ANY_ANY_OBJ:
                return true;
            case ANY_ANY_ANY, SUB_PRE_OBJ:
                return false;
            default:
                throw new IllegalArgumentException("Unknown pattern classification: " + PatternClassifier.classify(tripleMatch));
        }
    }

    private RoaringTripleStore setupStoreWithSpyForSpecialManualStrategy() {
        final var realStore = new RoaringTripleStore(IndexingStrategy.MANUAL);
        // Spy setup for the minimal strategy
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
            // Clear the index to reset the state for the next call
            realStore.clearIndex();
            // Return the result of the store with the index
            return result;
        }).when(spyStore).contains(Mockito.argThat(t -> isPatternRequiringIndexing(t)));

        // Mock {@link TripleStore#find(Triple)}
        Mockito.doAnswer(invocation -> {
            final Triple tripleMatch = invocation.getArgument(0);
            // If the triple match is a pattern that requires indexing, throw an exception
            assertThrows(UnsupportedOperationException.class, () -> realStore.find(tripleMatch));

            // now initialize the index
            realStore.initializeIndex();
            // determine the result with the index
            final var result = realStore.find(tripleMatch);
            // Clear the index to reset the state for the next call
            realStore.clearIndex();
            // Return the result of the store with the index
            return result;
        }).when(spyStore).find(Mockito.argThat(t -> isPatternRequiringIndexing(t)));

        // Mock {@link TripleStore#stream(Triple)}
        Mockito.doAnswer(invocation -> {
            final Triple tripleMatch = invocation.getArgument(0);
            // If the triple match is a pattern that requires indexing, throw an exception
            assertThrows(UnsupportedOperationException.class, () -> realStore.stream(tripleMatch));

            // now initialize the index
            realStore.initializeIndex();
            // determine the result with the index
            final var result = realStore.stream(tripleMatch);
            // Clear the index to reset the state for the next call
            realStore.clearIndex();
            // Return the result of the store with the index
            return result;
        }).when(spyStore).stream(Mockito.argThat(t -> isPatternRequiringIndexing(t)));

        return spyStore;
    }

    private RoaringTripleStore getSutAsRoaringTripleStore() {
        return (RoaringTripleStore) super.sut;
    }

    @Test
    public void testGetIndexingStrategy() {
        // Given
        final var sut = getSutAsRoaringTripleStore();

        // Then
        assertEquals(indexingStrategy, sut.getIndexingStrategy());
    }

    @Test
    public void testIsIndexInitialized() {
        // Given
        final var sut = getSutAsRoaringTripleStore();

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
    public void testLazyInitiallization() {
        // Given
        final var sut = getSutAsRoaringTripleStore();
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
        final var sut = getSutAsRoaringTripleStore();

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
        final var sut = getSutAsRoaringTripleStore();

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
    public void testClearIndex() {
        // Given
        final var sut = getSutAsRoaringTripleStore();
        sut.initializeIndex();

        // When
        sut.clearIndex();

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