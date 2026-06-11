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

import static org.apache.jena.junit.GraphHelper.triple;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.mem.pattern.PatternClassifier;
import org.mockito.Mockito;

@ParameterizedClass
@MethodSource("provideArgs")
public class GraphMemIndexedSetTest extends AbstractGraphMemTest {

    public static Stream<Arguments> provideArgs() {
        List<Arguments> args = Arrays.stream(IndexingStrategy.values())
                .map(strategy -> Arguments.of(strategy))
                .toList();
        return args.stream();
    }

    public IndexingStrategy indexingStrategy;

    public GraphMemIndexedSetTest(IndexingStrategy indexingStrategy) { this.indexingStrategy = indexingStrategy; }

    @Override
    public void testDeleteAll() {
        // the delete test takes almost a minute with MANUAL indexing
        if(indexingStrategy == IndexingStrategy.MANUAL)
            return;
        super.deleteAllWorker();
    }

    @Override
    protected GraphMem createGraph() {
        return switch (indexingStrategy) {
            case EAGER, LAZY, LAZY_PARALLEL, MINIMAL -> new GraphMemIndexedSet(indexingStrategy);
            case MANUAL -> setupGraphWithSpyForSpecialManualStrategy();
        };
    }

    private static boolean isPatternRequiringIndexing(final Triple tripleMatch) {
        return switch (PatternClassifier.classify(tripleMatch)) {
            case SUB_PRE_ANY, SUB_ANY_OBJ, SUB_ANY_ANY, ANY_PRE_OBJ, ANY_PRE_ANY, ANY_ANY_OBJ -> true;
            case ANY_ANY_ANY, SUB_PRE_OBJ -> false;
        };
    }

    /**
     * Builds a {@link GraphMemIndexedSet} with the {@link IndexingStrategy#MANUAL}
     * strategy, wrapped in a Mockito spy that exercises the documented
     * "fail-then-initialize-then-clear" cycle on every pattern lookup. Without
     * the wrapper the inherited tests in {@link AbstractGraphMemTest} would
     * fail outright, because a freshly-created MANUAL graph throws
     * {@link UnsupportedOperationException} on any pattern operation until
     * {@link GraphMemIndexedSet#initializeIndex()} has been called.
     */
    private GraphMemIndexedSet setupGraphWithSpyForSpecialManualStrategy() {
        final var realGraph = new GraphMemIndexedSet(IndexingStrategy.MANUAL);
        final var spyGraph = Mockito.spy(realGraph);

        // Mock {@link Graph#contains(Triple)}
        Mockito.doAnswer(invocation -> {
            final Triple tripleMatch = invocation.getArgument(0);
            // If the triple match is a pattern that requires indexing, throw an exception
            assertThrows(UnsupportedOperationException.class, () -> realGraph.contains(tripleMatch));

            // now initialize the index
            realGraph.initializeIndex();
            // determine the result with the index
            final var result = realGraph.contains(tripleMatch);
            // Reset the indexing strategy for the next call
            realGraph.resetIndexingStrategy();
            // Return the result of the store with the index
            return result;
        }).when(spyGraph).contains(Mockito.argThat(GraphMemIndexedSetTest::isPatternRequiringIndexing));

        // Mock {@link Graph#find(Triple)}
        Mockito.doAnswer(invocation -> {
            final Triple tripleMatch = invocation.getArgument(0);
            // If the triple match is a pattern that requires indexing, throw an exception
            assertThrows(UnsupportedOperationException.class, () -> realGraph.find(tripleMatch));

            // now initialize the index
            realGraph.initializeIndex();
            // determine the result with the index
            final var result = realGraph.find(tripleMatch);
            // Reset the indexing strategy for the next call
            realGraph.resetIndexingStrategy();
            // Return the result of the store with the index
            return result;
        }).when(spyGraph).find(Mockito.argThat(GraphMemIndexedSetTest::isPatternRequiringIndexing));

        var triplePatternMatcher = new TriplePatternArgumentCollectMatcher();

        // Mock {@link Graph#stream(Node, Node, Node)}
        Mockito.doAnswer(invocation -> {
                    final Node sm = invocation.getArgument(0);
                    final Node pm = invocation.getArgument(1);
                    final Node om = invocation.getArgument(2);

                    // If the triple match is a pattern that requires indexing, throw an exception
                    assertThrows(UnsupportedOperationException.class, () -> realGraph.stream(sm, pm, om));

                    // now initialize the index
                    realGraph.initializeIndex();
                    // determine the result with the index
                    final var result = realGraph.stream(sm, pm, om);
                    // Reset the indexing strategy for the next call
                    realGraph.resetIndexingStrategy();
                    // Return the result of the store with the index
                    return result;
                }).when(spyGraph)
                .stream(Mockito.argThat(triplePatternMatcher),
                        Mockito.argThat(triplePatternMatcher),
                        Mockito.argThat(triplePatternMatcher));

        return spyGraph;
    }

    /**
     * Matcher to collect the arguments of a triple pattern (Node, Node, Node)
     * and check if the pattern requires indexing.
     * This matcher is used to mock the behavior of methods that take a triple pattern as argument.
     */
    private static class TriplePatternArgumentCollectMatcher implements org.mockito.ArgumentMatcher<Node> {
        final Node[] nodes = new Node[3];
        int index = 0;

        @Override
        public boolean matches(Node node) {
            switch (index) {
                case 0:
                    nodes[0] = node;
                    index++;
                    return true;
                case 1:
                    nodes[1] = node;
                    index++;
                    return true;
                case 2:
                    nodes[2] = node;
                    index = 0; // Reset for next match
                    return isPatternRequiringIndexing(Triple.createMatch(nodes[0], nodes[1], nodes[2]));
                default:
                    return false; // Should not happen
            }
        }
    }

    private GraphMemIndexedSet getSutAsGraphMemIndexedSet() {
        return (GraphMemIndexedSet) super.sut;
    }

    @Test
    public void testGetIndexingStrategy() {
        // Given
        final var sut = getSutAsGraphMemIndexedSet();

        // Then
        assertEquals(indexingStrategy, sut.getIndexingStrategy());
    }

    @Test
    public void testIsIndexInitialized() {
        // Given
        final var sut = getSutAsGraphMemIndexedSet();

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
        final var sut = getSutAsGraphMemIndexedSet();
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
        final var sut = getSutAsGraphMemIndexedSet();

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
        final var sut = getSutAsGraphMemIndexedSet();

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
        final var sut = getSutAsGraphMemIndexedSet();
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

    @Test
    public void testCopyPreservesStrategyAndType() {
        // The MANUAL case here is the spy-wrapped graph, not a plain
        // GraphMemIndexedSet; copy() doesn't go through the spy so we limit
        // this test to the strategies that build their graph directly.
        if (indexingStrategy == IndexingStrategy.MANUAL) return;

        final var sut = getSutAsGraphMemIndexedSet();
        sut.add(triple("s p o"));

        final var copy = sut.copy();
        assertNotNull(copy);
        // The override on GraphMemIndexedSet must preserve the runtime type
        // and the indexing strategy.
        assertEquals(indexingStrategy, copy.getIndexingStrategy());
        assertTrue(copy.contains(triple("s p o")));

        // Mutations in the copy must not affect the source
        copy.add(triple("s2 p2 o2"));
        assertFalse(sut.contains(triple("s2 p2 o2")));
        assertTrue(copy.contains(triple("s2 p2 o2")));
    }
}