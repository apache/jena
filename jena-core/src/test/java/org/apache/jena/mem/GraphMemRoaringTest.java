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

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.mem.pattern.PatternClassifier;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collection;

import static org.apache.jena.testing_framework.GraphHelper.triple;
import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class GraphMemRoaringTest extends AbstractGraphMemTest {

    @Parameterized.Parameter
    public IndexingStrategy indexingStrategy;

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.stream(IndexingStrategy.values())
                .map(strategy -> new Object[]{strategy})
                .toList();
    }

    @Override
    protected GraphMem createGraph() {
        switch (indexingStrategy) {
            case EAGER, LAZY, LAZY_PARALLEL, MINIMAL:
                return new GraphMemRoaring(indexingStrategy);
            case MANUAL:
                return setupGraphWithSpyForSpecialManualStrategy();
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

    private GraphMemRoaring setupGraphWithSpyForSpecialManualStrategy() {
        final var realGraph = new GraphMemRoaring(IndexingStrategy.MANUAL);
        // Spy setup for the minimal strategy
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
            // Clear the index to reset the state for the next call
            realGraph.clearIndex();
            // Return the result of the store with the index
            return result;
        }).when(spyGraph).contains(Mockito.argThat(t -> isPatternRequiringIndexing(t)));

        // Mock {@link Graph#find(Triple)}
        Mockito.doAnswer(invocation -> {
            final Triple tripleMatch = invocation.getArgument(0);
            // If the triple match is a pattern that requires indexing, throw an exception
            assertThrows(UnsupportedOperationException.class, () -> realGraph.find(tripleMatch));

            // now initialize the index
            realGraph.initializeIndex();
            // determine the result with the index
            final var result = realGraph.find(tripleMatch);
            // Clear the index to reset the state for the next call
            realGraph.clearIndex();
            // Return the result of the store with the index
            return result;
        }).when(spyGraph).find(Mockito.argThat(t -> isPatternRequiringIndexing(t)));

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
            // Clear the index to reset the state for the next call
            realGraph.clearIndex();
            // Return the result of the store with the index
            return result;
        }).when(spyGraph)
                .stream(Mockito.argThat(triplePatternMatcher::matches),
                        Mockito.argThat(triplePatternMatcher::matches),
                        Mockito.argThat(triplePatternMatcher::matches));

        return spyGraph;
    }

    /**
     * Matcher to collect the arguments of a triple pattern (Node, Node, Node)
     * and check if the pattern requires indexing.
     * This matcher is used to mock the behavior of methods that take a triple pattern as argument.
     */
    private class TriplePatternArgumentCollectMatcher implements org.mockito.ArgumentMatcher<Node> {
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

    private GraphMemRoaring getSutAsGraphMem2Roaring() {
        return (GraphMemRoaring) super.sut;
    }

    @Test
    public void testGetIndexingStrategy() {
        // Given
        final var sut = getSutAsGraphMem2Roaring();

        // Then
        assertEquals(indexingStrategy, sut.getIndexingStrategy());
    }

    @Test
    public void testIsIndexInitialized() {
        // Given
        final var sut = getSutAsGraphMem2Roaring();

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
        final var sut = getSutAsGraphMem2Roaring();
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
        final var sut = getSutAsGraphMem2Roaring();

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
        final var sut = getSutAsGraphMem2Roaring();

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
        final var sut = getSutAsGraphMem2Roaring();
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