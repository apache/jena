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
 * Iterator that resolves the integer indices stored in an {@link IndexList}
 * back to {@link Triple} instances by looking them up in a {@link IndexedTripleSource}.
 * Walks the list from its last index to position {@code 0}.
 * <p>
 * Detects concurrent modifications by snapshotting {@code triples.size()} at
 * construction time and rechecking it on each call to {@link #next()} /
 * {@link #forEachRemaining(Consumer)}; throws
 * {@link ConcurrentModificationException} if the size has changed.
 */
public class IndexListIterator extends NiceIterator<Triple> {

    private final IndexedTripleSource source;
    private final int sizeOfSetAtStart;
    private final int[] indices;
    private final Triple[] triples;
    private int pos;

    /**
     * Creates an iterator over the triples whose indices are stored in
     * {@code indexList}.
     *
     * @param source   the canonical set of triples to dereference indices against
     * @param indexList the list of triple indices to walk
     */
    public IndexListIterator(final IndexedTripleSource source, final IndexList indexList) {
        this.source = source;
        this.triples = source.getTriples();
        indices = indexList.getIndices();
        pos = indexList.lastPos();
        this.sizeOfSetAtStart = source.size();
    }

    @Override
    public boolean hasNext() {
        return -1 < pos;
    }

    @Override
    public Triple next() {
        if (sizeOfSetAtStart != source.size()) throw new ConcurrentModificationException();
        if(!hasNext()) {
            throw new NoSuchElementException();
        }
        return triples[indices[pos--]];
    }

    @Override
    public void forEachRemaining(Consumer<? super Triple> action) {
        while (-1 < pos) {
            action.accept(triples[indices[pos--]]);
        }
        if (sizeOfSetAtStart != source.size()) throw new ConcurrentModificationException();
    }
}