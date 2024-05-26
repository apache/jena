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

package org.apache.jena.sparql.service.enhancer.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.stream.IntStream;

import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.PeekingIterator;
import com.google.common.collect.SetMultimap;
import com.google.common.primitives.Ints;
import org.apache.jena.sparql.service.enhancer.impl.util.SinglePrefetchIterator;

/**
 * Buffering iterator. Can buffer an arbitrary amount ahead.
 *
 * Single producer multi consumer style.
 *
 * @param <T> The item type of this iterator
 * @param <I> The type of the underlying iterator
 */
public class IteratorFactoryWithBuffer<T, I extends Iterator<T>>
{
    protected final Object lock = new Object();

    protected I delegate;
    protected List<T> buffer = null;
    protected long absBufferOffset = 0;
    protected SetMultimap<Long, Iterator<T>> offsetToChild = MultimapBuilder.treeKeys().hashSetValues().build();

    public IteratorFactoryWithBuffer(I delegate) {
        this.delegate = delegate;
    }

    public static <T, I extends Iterator<T>> SubIterator<T, I> wrap(I delegate) {
        SubIterator<T, I> result = new IteratorFactoryWithBuffer<>(delegate).createSubIterator(false);
        return result;
    }

    protected SubIterator<T, I> createSubIterator(long offset) {
        SubIterator<T, I> result;
        synchronized (lock) {
            result = new SubIteratorImpl(offset);
            offsetToChild.put(offset, result);
        }
        return result;
    }

    public SubIterator<T, I> createSubIterator(boolean startAtUnbuffered) {
        SubIterator<T, I> result;
        synchronized (lock) {
            long offset;
            offset = absBufferOffset;
            if (buffer != null && startAtUnbuffered) {
                offset += buffer.size();
            }

            // If the delegate is exhausted then position at the next element
//            if (!delegate.hasNext()) {
//                ++offset;
//            }

            result = createSubIterator(offset);
        }
        return result;
    }

    public interface SubIterator<T, I extends Iterator<T>>
        extends PeekingIterator<T>, AutoCloseable {

        I getDelegate();

        /**
         * Should return the absolute offset (starting at 0) of the next item being returned by a call to next();
         * ISSUE Guava AbstractIterator doesn't allow for checking whether hasNext has been called... - so
         * if hasNext was been called then the offset will point to the next element!
         */
        long getOffset();

        /**
         * Return how far this iterator is ahead of the iterator with the lowest offset.
         * If there is no other iterator than the distance is 0.
         *
         * This is also the amount of buffering used.
         */
        long getDistanceToLowestOffset();

        @Override
        void close();

        /** Create an iterator with the same next item as this one. Items are buffered as long as there exists an
         *  open iterator with a lower offset */
        SubIterator<T, I> createSubIterator(boolean startAtUnbuffered);

        default SubIterator<T, I> subIteratorAtStartOfBuffer() { return createSubIterator(false); }
        default SubIterator<T, I> subIteratorAtEndOfBuffer() { return createSubIterator(true); }
    }

    protected class SubIteratorImpl
        extends SinglePrefetchIterator<T>
        implements SubIterator<T, I>
    {
        protected long absOffset;

        @Override
        public I getDelegate() {
            return delegate;
        }

        public SubIteratorImpl(long absOffset) {
            super();
            this.absOffset = absOffset;
        }

        @Override
        public SubIterator<T, I> createSubIterator(boolean startAtUnbuffered) {
            // The result must be closed by the caller
            SubIterator<T, I> result = isOpen()
                    ? startAtUnbuffered
                            ? IteratorFactoryWithBuffer.this.createSubIterator(startAtUnbuffered)
                            : IteratorFactoryWithBuffer.this.createSubIterator(absOffset)
                    : new SubIteratorImpl(absOffset); // Create an iterator without registration => considered closed

            return result;
        }

        @Override
        public long getOffset() {
            // If an item was prefetched but not picked up via next() then return the previous index
            long d = wasHasNextCalled() ? 1 : 0;
            return absOffset - d;
        }

        protected boolean isOpen() {
            return offsetToChild.containsEntry(absOffset, this);
        }

        @Override
        protected T prefetch() {

            boolean isEndOfData = false;
            T result = null;
            synchronized (lock) {
                // If closed
                if (!isOpen()) {
                    return finish();
                }

                if (absOffset < absBufferOffset) {
                    throw new IllegalStateException();
                }

                int relOffset = Ints.checkedCast(absOffset - absBufferOffset);

                long bufferSize = buffer == null ? 0 : buffer.size();

                if (relOffset < bufferSize) {
                    // Serve item from the buffer
                    result = buffer.get(relOffset);
                    ++absOffset;
                } else if (relOffset == bufferSize) {
                    if (delegate.hasNext()) {
                        result = delegate.next();

                        // If there is another child iterator then buffer the item
                        if (offsetToChild.size() > 1) {
                            if (buffer == null) {
                                buffer = new ArrayList<>();
                                absBufferOffset = absOffset;
                            }
                            buffer.add(result);
                        } else {
                            // Buffer exhausted - clear it
                            buffer = null;
                        }

                        ++absOffset;
                    } else {
                        isEndOfData = true;
                    }
                } else {
                    // If sub iterator is created from a finished iterator it comes here
                    isEndOfData = true;
                    // throw new IllegalStateException();
                }

                if (isEndOfData) {
                    result = finish();
                    close();
                } else {
                    offsetToChild.remove(absOffset - 1, this);
                    offsetToChild.put(absOffset, this);

                    if (buffer == null) {
                        absBufferOffset = absOffset;
                    }
                    checkShrink();
                }
            }

            return result;
        }

        @Override
        public void close() {
            synchronized (lock) {
                offsetToChild.remove(absOffset, this);
                checkShrink();
            }
        }

        @Override
        public long getDistanceToLowestOffset() {
            SortedSet<Long> keys = (SortedSet<Long>)offsetToChild.asMap().keySet();

            long first = keys.isEmpty()
                ? absOffset
                : keys.first();

            long result = absOffset - first;
            return result;
        }

        @Override
        public T peek() {
            return current();
        }
    }

    protected void checkShrink() {
        // TODO For completeness shrink e.g. if the needed size of the buffer has halved
        // SortedSet<Long> keys = (SortedSet<Long>)offsetToChild.asMap().keySet();
        // if (keys.first()) {
        // }
    }

    public static void main(String[] args) {
        Iterator<Integer> base = IntStream.range(0, 5).iterator();
        IteratorFactoryWithBuffer<Integer, ?> factory = new IteratorFactoryWithBuffer<>(base);

        try (SubIterator<Integer, ?> primary = factory.createSubIterator(false)) {

            for (int i = 0; i < 2; ++i) {
                System.out.println("primary: " + primary.next());
            }

            try (SubIterator<Integer, ?> secondary = primary.createSubIterator(false)) {
                for (int i = 0; i < 2; ++i) {
                    System.out.println("secondary: " + secondary.next());
                }

                // secondary.close();
                try (SubIterator<Integer, ?> ternary = secondary.createSubIterator(false)) {
                    while (ternary.hasNext()) {
                        System.out.println("ternary: " + ternary.next());
                    }

                    while (primary.hasNext()) {
                        System.out.println("primary: " + primary.next());
                    }

                    while (secondary.hasNext()) {
                        System.out.println("secondary: " + secondary.next());
                    }

                    System.out.println(primary.getOffset());
                    System.out.println(secondary.getOffset());
                    System.out.println(ternary.getOffset());
                }
            }
        }

    }
}