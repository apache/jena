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
package org.apache.jena.mem2.store.roaring;

import org.apache.jena.graph.Triple;
import org.apache.jena.mem2.collection.FastHashSet;
import org.apache.jena.util.iterator.NiceIterator;
import org.roaringbitmap.BatchIterator;
import org.roaringbitmap.ImmutableBitmapDataProvider;

import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

/**
 * A triple iterator that iterates over triple indices in a RoaringBitmap {@link BatchIterator}.
 * Only the #forEachRemaining method uses {@link ImmutableBitmapDataProvider#forEach} if there has been no
 * #next or #hasNext call before.
 * All triples are stored in a {@link FastHashSet} and each triple is retrieved from the set by its index.
 * The bitmap  typically is a subset of the triple indices in the set.
 */
public class RoaringBitmapTripleIterator extends NiceIterator<Triple> {
    protected static final int BUFFER_SIZE = 64;
    private final ImmutableBitmapDataProvider bitmap;
    private final FastHashSet<Triple> triples;
    private final int initialSize;
    private final BatchIterator batchIterator;
    private final int[] buffer = new int[BUFFER_SIZE];
    private int bufferIndex = -1;
    private boolean batchIteratorHasBeenUsed = false;

    public RoaringBitmapTripleIterator(final ImmutableBitmapDataProvider bitmap, final FastHashSet<Triple> triples) {
        this.bitmap = bitmap;
        this.batchIterator = bitmap.getBatchIterator();
        this.triples = triples;
        this.initialSize = triples.size();
    }

    @Override
    public boolean hasNext() {
        if (bufferIndex > 0)
            return true;
        if (this.batchIterator.hasNext()) {
            if (!batchIteratorHasBeenUsed) {
                batchIteratorHasBeenUsed = true;
            }
            bufferIndex = batchIterator.nextBatch(buffer);
        }
        return bufferIndex > 0;
    }

    @Override
    public Triple next() {
        if (triples.size() != initialSize) throw new ConcurrentModificationException();

        if (this.hasNext())
            return triples.getKeyAt(buffer[--bufferIndex]);

        throw new NoSuchElementException();
    }

    @Override
    public void forEachRemaining(Consumer<? super Triple> action) {
        if (batchIteratorHasBeenUsed) {
            while (-1 < --bufferIndex) {
                action.accept(triples.getKeyAt(buffer[bufferIndex]));
            }
            while (batchIterator.hasNext()) {
                bufferIndex = batchIterator.nextBatch(buffer);
                while (-1 < --bufferIndex) {
                    action.accept(triples.getKeyAt(buffer[bufferIndex]));
                }
            }
        } else {
            bitmap.forEach((int index) -> action.accept(triples.getKeyAt(index)));
        }
        if (triples.size() != initialSize) throw new ConcurrentModificationException();
    }
}
