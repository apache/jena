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

import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * A spliterator for sparse arrays. This spliterator will iterate over the array
 * skipping null entries.
 *
 * This spliterator supports splitting into sub-spliterators.
 *
 * The spliterator will check for concurrent modifications by invoking a {@link Runnable}
 * before each action.
 *
 * @param <E>
 */
public class SparseArraySubSpliterator<E> implements Spliterator<E> {

    private final E[] entries;
    private final int fromIndex;
    private int pos;
    private final float fillRatio;
    private final Runnable checkForConcurrentModification;

    /**
     * Create a spliterator for the given array, with the given size.
     *
     * @param entries                        the array
     * @param fromIndex                      the index of the first element, inclusive
     * @param toIndex                        the index of the last element, exclusive
     * @param fillRatio                      the ratio of elements containing null
     * @param checkForConcurrentModification
     */
    public SparseArraySubSpliterator(final E[] entries, final int fromIndex, final int toIndex, final float fillRatio, final Runnable checkForConcurrentModification) {
        this.entries = entries;
        this.fromIndex = fromIndex;
        this.pos = toIndex;
        this.fillRatio = fillRatio;
        this.checkForConcurrentModification = checkForConcurrentModification;
    }

    /**
     * Create a spliterator for the given array, with the given size.
     * @param entries   the array
     * @param estimatedElementsCount the estimated size
     */
    public SparseArraySubSpliterator(final E[] entries, final int estimatedElementsCount, final Runnable checkForConcurrentModification) {
       this(entries, 0, entries.length, ((float)estimatedElementsCount / (float)entries.length), checkForConcurrentModification);
    }


    /**
     * If a remaining element exists, performs the given action on it,
     * returning {@code true}; else returns {@code false}.  If this
     * Spliterator is {@link #ORDERED} the action is performed on the
     * next element in encounter order.  Exceptions thrown by the
     * action are relayed to the caller.
     *
     * @param action The action
     * @return {@code false} if no remaining elements existed
     * upon entry to this method, else {@code true}.
     * @throws NullPointerException if the specified action is null
     */
    @Override
    public boolean tryAdvance(Consumer<? super E> action) {
        this.checkForConcurrentModification.run();
        while(fromIndex <= --pos) {
            if(null != entries[pos]) {
                action.accept(entries[pos]);
                return true;
            }
        }
        return false;
    }

    /**
     * Performs the given action for each remaining element, sequentially in
     * the current thread, until all elements have been processed or the action
     * throws an exception.  If this Spliterator is {@link #ORDERED}, actions
     * are performed in encounter order.  Exceptions thrown by the action
     * are relayed to the caller.
     *
     * @param action The action
     * @throws NullPointerException if the specified action is null
     * @implSpec The default implementation repeatedly invokes {@link #tryAdvance} until
     * it returns {@code false}.  It should be overridden whenever possible.
     */
    @Override
    public void forEachRemaining(Consumer<? super E> action) {
        pos--;
        while (fromIndex <= pos) {
            if(null != entries[pos]) {
                action.accept(entries[pos]);
            }
            pos--;
        }
        this.checkForConcurrentModification.run();
    }

    /**
     * If this spliterator can be partitioned, returns a Spliterator
     * covering elements, that will, upon return from this method, not
     * be covered by this Spliterator.
     *
     * <p>If this Spliterator is {@link #ORDERED}, the returned Spliterator
     * must cover a strict prefix of the elements.
     *
     * <p>Unless this Spliterator covers an infinite number of elements,
     * repeated calls to {@code trySplit()} must eventually return {@code null}.
     * Upon non-null return:
     * <ul>
     * <li>the value reported for {@code estimateSize()} before splitting,
     * must, after splitting, be greater than or equal to {@code estimateSize()}
     * for this and the returned Spliterator; and</li>
     * <li>if this Spliterator is {@code SUBSIZED}, then {@code estimateSize()}
     * for this spliterator before splitting must be equal to the sum of
     * {@code estimateSize()} for this and the returned Spliterator after
     * splitting.</li>
     * </ul>
     *
     * <p>This method may return {@code null} for any reason,
     * including emptiness, inability to split after traversal has
     * commenced, data structure constraints, and efficiency
     * considerations.
     *
     * @return a {@code Spliterator} covering some portion of the
     * elements, or {@code null} if this spliterator cannot be split
     * @apiNote An ideal {@code trySplit} method efficiently (without
     * traversal) divides its elements exactly in half, allowing
     * balanced parallel computation.  Many departures from this ideal
     * remain highly effective; for example, only approximately
     * splitting an approximately balanced tree, or for a tree in
     * which leaf nodes may contain either one or two elements,
     * failing to further split these nodes.  However, large
     * deviations in balance and/or overly inefficient {@code
     * trySplit} mechanics typically result in poor parallel
     * performance.
     */
    @Override
    public Spliterator<E> trySplit() {
        final int entriesCount = pos - fromIndex;
        if (entriesCount < 2) {
            return null;
        }
        if (this.estimateSize() < 2L) {
            return null;
        }
        final int toIndexOfSubIterator = this.pos;
        this.pos = fromIndex + (entriesCount >>> 1);
        return new SparseArraySubSpliterator<>(entries, this.pos, toIndexOfSubIterator, fillRatio, checkForConcurrentModification);
    }

    /**
     * Returns an estimate of the number of elements that would be
     * encountered by a {@link #forEachRemaining} traversal, or returns {@link
     * Long#MAX_VALUE} if infinite, unknown, or too expensive to compute.
     *
     * <p>If this Spliterator is {@link #SIZED} and has not yet been partially
     * traversed or split, or this Spliterator is {@link #SUBSIZED} and has
     * not yet been partially traversed, this estimate must be an accurate
     * count of elements that would be encountered by a complete traversal.
     * Otherwise, this estimate may be arbitrarily inaccurate, but must decrease
     * as specified across invocations of {@link #trySplit}.
     *
     * @return the estimated size, or {@code Long.MAX_VALUE} if infinite,
     * unknown, or too expensive to compute.
     * @apiNote Even an inexact estimate is often useful and inexpensive to compute.
     * For example, a sub-spliterator of an approximately balanced binary tree
     * may return a value that estimates the number of elements to be half of
     * that of its parent; if the root Spliterator does not maintain an
     * accurate count, it could estimate size to be the power of two
     * corresponding to its maximum depth.
     */
    @Override
    public long estimateSize() { return ((long) (fillRatio * (pos - fromIndex))) + 1L; }


    /**
     * Returns a set of characteristics of this Spliterator and its
     * elements. The result is represented as ORed values from {@link
     * #ORDERED}, {@link #DISTINCT}, {@link #SORTED}, {@link #SIZED},
     * {@link #NONNULL}, {@link #IMMUTABLE}, {@link #CONCURRENT},
     * {@link #SUBSIZED}.  Repeated calls to {@code characteristics()} on
     * a given spliterator, prior to or in-between calls to {@code trySplit},
     * should always return the same result.
     *
     * <p>If a Spliterator reports an inconsistent set of
     * characteristics (either those returned from a single invocation
     * or across multiple invocations), no guarantees can be made
     * about any computation using this Spliterator.
     *
     * @return a representation of characteristics
     * @apiNote The characteristics of a given spliterator before splitting
     * may differ from the characteristics after splitting.  For specific
     * examples see the characteristic values {@link #SIZED}, {@link #SUBSIZED}
     * and {@link #CONCURRENT}.
     */
    @Override
    public int characteristics() {
        return DISTINCT | NONNULL | IMMUTABLE;
    }
}
