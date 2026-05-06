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

import org.apache.jena.graph.Triple;

import java.util.ConcurrentModificationException;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * Spliterator counterpart to {@link IndexListsIterator}: walks the
 * intersection of two {@link IndexList}s, dereferencing each surviving
 * triple index against a {@link IndexedTripleSource}. Supports recursive splitting
 * for parallel traversal; the split happens on the (smaller) list being
 * scanned, never on the larger list (which is only probed via its
 * reverse-index array).
 * <p>
 * Detects concurrent modifications by snapshotting {@code triples.size()} at
 * construction time and rechecking it at each advance/forEach boundary;
 * throws {@link ConcurrentModificationException} if the size has changed.
 */
public class IndexListsSpliterator implements Spliterator<Triple> {

    private final IndexedTripleSource source;
    private final Triple[] triples;
    private final int sizeOfSetAtStart;
    private final int[] indicesSmaller;
    private final int[] indicesLarger;
    private final int[] reverseIndicesLarger;
    private final int toPositionExclusive;
    private int pos;
    final int indicesLargerSize;

    /**
     * Creates a spliterator over the triples whose indices appear in both
     * {@code indexListA} and {@code indexListB}.
     *
     * @param source         the canonical triple set
     * @param indexListA      one of the index lists to intersect
     * @param reverseIndicesA reverse-index array for {@code indexListA}
     * @param indexListB      the other index list to intersect
     * @param reverseIndicesB reverse-index array for {@code indexListB}
     */
    public IndexListsSpliterator(final IndexedTripleSource source,
                                 final IndexList indexListA, final int[] reverseIndicesA,
                                 final IndexList indexListB, final int[] reverseIndicesB) {
        this.source = source;
        this.triples = source.getTriples();
        this.sizeOfSetAtStart = source.size();
        if(indexListA.size() < indexListB.size()) {
            indicesSmaller = indexListA.getIndices();
            indicesLarger = indexListB.getIndices();
            reverseIndicesLarger = reverseIndicesB;
            toPositionExclusive = indexListA.size();
            pos = 0;
            indicesLargerSize = indexListB.size();
        } else {
            indicesSmaller = indexListB.getIndices();
            indicesLarger = indexListA.getIndices();
            reverseIndicesLarger = reverseIndicesA;
            toPositionExclusive = indexListB.size();
            pos = 0;
            indicesLargerSize = indexListA.size();
        }
    }

    private IndexListsSpliterator(final IndexedTripleSource source,
                                  final int sizeOfSetAtStart,
                                  final int[] indicesSmaller,
                                  final int[] indicesLarger, final int indicesLargerSize,
                                  final int[] reverseIndicesLarger,
                                  final int from, final int toExclusive) {
        this.source = source;
        this.triples = source.getTriples();
        this.sizeOfSetAtStart = sizeOfSetAtStart;
        this.indicesSmaller = indicesSmaller;
        this.indicesLarger = indicesLarger;
        this.reverseIndicesLarger = reverseIndicesLarger;
        this.pos = from;
        this.toPositionExclusive = toExclusive;
        this.indicesLargerSize = indicesLargerSize;
    }


    @Override
    public boolean tryAdvance(Consumer<? super Triple> action) {
        if (sizeOfSetAtStart != source.size()) throw new ConcurrentModificationException();
        while (pos < toPositionExclusive) {
            final var tripleIndex = indicesSmaller[pos++];
            final var posLarger = reverseIndicesLarger[tripleIndex];
            if(posLarger < indicesLargerSize
                    && indicesLarger[posLarger] == tripleIndex) {
                action.accept(triples[tripleIndex]);
                return true;
            }
        }
        return false;
    }

    @Override
    public void forEachRemaining(Consumer<? super Triple> action) {
        while (pos < toPositionExclusive) {
            final var tripleIndex = indicesSmaller[pos++];
            final var posLarger = reverseIndicesLarger[tripleIndex];
            if(posLarger < indicesLargerSize
                    && indicesLarger[posLarger] == tripleIndex) {
                action.accept(triples[tripleIndex]);
            }
        }
        if (sizeOfSetAtStart != source.size()) throw new ConcurrentModificationException();
    }

    @Override
    public Spliterator<Triple> trySplit() {
        final var remaining = toPositionExclusive - pos;
        if (remaining < 2) {
            return null;
        }
        final var oldPos = pos;
        this.pos = pos + (remaining >>> 1);
        return new IndexListsSpliterator(source, sizeOfSetAtStart,
                indicesSmaller, indicesLarger, indicesLargerSize,
                reverseIndicesLarger,
                oldPos, this.pos);
    }

    @Override
    public long estimateSize() {
        return toPositionExclusive - pos;
    }

    @Override
    public long getExactSizeIfKnown() {
        return -1;
    }

    @Override
    public int characteristics() {
        return DISTINCT | NONNULL ;
    }
}