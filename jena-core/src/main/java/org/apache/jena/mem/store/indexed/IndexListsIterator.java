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
import org.apache.jena.util.iterator.NiceIterator;

import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

/**
 * Iterator over the intersection of two {@link IndexList}s, used by the
 * eager indexing strategy to answer two-key patterns
 * (e.g. subject-and-predicate or predicate-and-object).
 * <p>
 * The intersection is computed lazily: the iterator walks the shorter of
 * the two lists and probes each candidate triple index against the larger
 * list's reverse-index array. This gives expected
 * {@code O(min(|A|, |B|))} cost without any explicit set-allocation.
 * <p>
 * Detects concurrent modifications by snapshotting {@code triples.size()} at
 * construction time and rechecking it on each call to {@link #next()} /
 * {@link #forEachRemaining(Consumer)}; throws
 * {@link ConcurrentModificationException} if the size has changed.
 */
public class IndexListsIterator extends NiceIterator<Triple> {

    private final IndexedTripleSource source;
    private final Triple[] triples;
    private final int sizeOfSetAtStart;
    private final int[] indicesSmaller;
    private final int[] indicesLarger;
    private final int[] reverseIndicesLarger;
    private int pos;
    private int tripleIndex;
    final int indicesLargerSize;
    private boolean hasNext = false;

    /**
     * Creates an iterator over the triples whose indices appear in both
     * {@code indexListA} and {@code indexListB}.
     *
     * @param source         the canonical triple set to dereference indices against
     * @param indexListA      one of the index lists to intersect
     * @param reverseIndicesA reverse-index array for {@code indexListA}
     * @param indexListB      the other index list to intersect
     * @param reverseIndicesB reverse-index array for {@code indexListB}
     */
    public IndexListsIterator(final IndexedTripleSource source,
                              final IndexList indexListA, final int[] reverseIndicesA,
                              final IndexList indexListB, final int[] reverseIndicesB) {
        this.source = source;
        this.triples = source.getTriples();
        this.sizeOfSetAtStart = source.size();
        if(indexListA.size() < indexListB.size()) {
            indicesSmaller = indexListA.getIndices();
            indicesLarger = indexListB.getIndices();
            reverseIndicesLarger = reverseIndicesB;
            pos = indexListA.lastPos();
            indicesLargerSize = indexListB.size();
        } else {
            indicesSmaller = indexListB.getIndices();
            indicesLarger = indexListA.getIndices();
            reverseIndicesLarger = reverseIndicesA;
            pos = indexListB.lastPos();
            indicesLargerSize = indexListA.size();
        }
    }

    @Override
    public boolean hasNext() {
        if(hasNext)
            return true;

        while(-1 < pos)  {
            tripleIndex = indicesSmaller[pos--];
            final var posLarger = reverseIndicesLarger[tripleIndex];

            if(posLarger < indicesLargerSize
                    && indicesLarger[posLarger] == tripleIndex) {
                return hasNext = true;
            }
        }
        return false;
    }

    @Override
    public Triple next() {
        if (sizeOfSetAtStart != source.size()) throw new ConcurrentModificationException();
        if(hasNext || hasNext()) {
            hasNext = false;
            return triples[tripleIndex];
        }
        throw new NoSuchElementException();
    }

    @Override
    public void forEachRemaining(Consumer<? super Triple> action) {
        if(hasNext) {
            action.accept(next());
            hasNext = false;
        }
        while (-1 < pos) {
            tripleIndex = indicesSmaller[pos--];
            final var posLarger = reverseIndicesLarger[tripleIndex];
            if(posLarger < indicesLargerSize
                    && indicesLarger[posLarger] == tripleIndex) {
                action.accept(triples[tripleIndex]);
            }
        }
        if (sizeOfSetAtStart != source.size()) throw new ConcurrentModificationException();
    }
}