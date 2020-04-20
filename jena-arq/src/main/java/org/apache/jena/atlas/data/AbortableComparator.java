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
package org.apache.jena.atlas.data;

import java.util.Arrays;
import java.util.Comparator;

public final class AbortableComparator<E> implements Comparator<E> {
    public AbortableComparator(Comparator<? super E> comparator) {
        this.baseComparator = comparator;
    }

    /**
     * <code>AbandonSort</code> is the exception thrown from
     * <code>AbortableComparator</code> to abandon a sort.
     */
    public static class AbandonSort extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }

    public static enum Finish {
        COMPLETED, ABORTED
    }

    protected volatile boolean cancelled;

    final Comparator<? super E> baseComparator;

    @Override
    public int compare(E o1, E o2) {
        if (cancelled)
            throw new AbandonSort();
        return baseComparator.compare(o1, o2);
    }

    /**
     * Sort the array <code>e</code> using this comparator with the additional
     * ability to abort the sort.
     */
    public Finish abortableSort(E[] e) {
        try {
            Arrays.sort(e, this);
        } catch (AbandonSort s) {
            return Finish.ABORTED;
        }
        return Finish.COMPLETED;
    }

    /**
     * Arrange that the next on-frequency cancellation test in compare will
     * succeed, aborting the sort.
     */
    public void cancel() {
        cancelled = true;
    }
}
