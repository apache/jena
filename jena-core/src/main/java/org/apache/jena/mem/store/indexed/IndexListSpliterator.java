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
 * Spliterator counterpart to {@link IndexListIterator}: walks an
 * {@link IndexList} in ascending order and dereferences each integer index
 * against a {@link IndexedTripleSource} to yield triples. Supports recursive
 * splitting for parallel traversal.
 * <p>
 * Detects concurrent modifications by snapshotting {@code triples.size()} at
 * construction time and rechecking it at each advance/forEach boundary;
 * throws {@link ConcurrentModificationException} if the size has changed.
 */
public class IndexListSpliterator implements Spliterator<Triple> {

    private final IndexedTripleSource source;
    private final Triple[] triples;
    private final int sizeOfSetAtStart;
    private final int[] indices;
    private final int toPositionExclusive;
    private int pos;

    /**
     * Creates a spliterator over every triple referenced by the given
     * index list.
     *
     * @param source   the canonical triple set
     * @param indexList the list of triple indices to walk
     */
    public IndexListSpliterator(final IndexedTripleSource source, final IndexList indexList) {
        this(source,
                indexList.getIndices(),
                0, indexList.size());
    }

    /**
     * Internal constructor used to produce sub-spliterators from
     * {@link #trySplit()}.
     *
     * @param source    the canonical triple set
     * @param indices    the raw indices array
     * @param from       inclusive lower bound on the slice to walk
     * @param toExclusive exclusive upper bound on the slice to walk
     */
    public IndexListSpliterator(final IndexedTripleSource source, final int[] indices, final int from, final int toExclusive) {
        this.source = source;
        this.triples = source.getTriples();
        this.sizeOfSetAtStart = source.size();
        this.indices = indices;
        this.pos = from;
        this.toPositionExclusive = toExclusive;
    }

    @Override
    public boolean tryAdvance(Consumer<? super Triple> action) {
        if (sizeOfSetAtStart != source.size()) throw new ConcurrentModificationException();
        if (pos < toPositionExclusive) {
            action.accept(triples[indices[pos++]]);
            return true;
        }
        return false;
    }

    @Override
    public void forEachRemaining(Consumer<? super Triple> action) {
        while (pos < toPositionExclusive) {
            action.accept(triples[indices[pos++]]);
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
        return new IndexListSpliterator(source, indices,
                oldPos, this.pos);
    }

    @Override
    public long estimateSize() {
        return toPositionExclusive - pos;
    }

    @Override
    public long getExactSizeIfKnown() {
        return toPositionExclusive - pos;
    }

    @Override
    public int characteristics() {
        return DISTINCT | SIZED | SUBSIZED | NONNULL ;
    }
}