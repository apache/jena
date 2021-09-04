/**
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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import org.apache.jena.atlas.AtlasException;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.iterator.IteratorCloseable;
import org.apache.jena.atlas.lib.FileOps;

/**
 * This class is like {@link DistinctDataBag} except that you are informed if the
 * item you just added was known to be distinct. This will normally only work until
 * the first spill. After that, the system may not be able to tell for sure, and will
 * thus return false. When you are finished adding items, you may call
 * {@link #netIterator()} to get any distinct items that are in the spill files but
 * were not indicated as distinct previously.
 */
public class DistinctDataNet<E> extends DistinctDataBag<E> {
    protected File firstSpillFile;

    public DistinctDataNet(ThresholdPolicy<E> policy, SerializationFactory<E> serializerFactory, Comparator<E> comparator) {
        super(policy, serializerFactory, comparator);
    }

    /**
     * @return true if the item added is known to be distinct.
     */
    public boolean netAdd(E item) {
        long s = size;
        super.add(item);
        return !spilled && size > s;
    }

    @Override
    protected void registerSpillFile(File spillFile) {
        // If this is the first time spilling, then keep this spill file separate
        if ( !spilled ) {
            firstSpillFile = spillFile;
        } else {
            super.registerSpillFile(spillFile);
        }
    }

    @Override
    protected void deleteSpillFiles() {
        super.deleteSpillFiles();
        if ( null != firstSpillFile ) {
            FileOps.delete(firstSpillFile, false);
            firstSpillFile = null;
        }
    }

    // Used by the .iterator() method
    @Override
    protected List<File> getSpillFiles() {
        List<File> toReturn = new ArrayList<>(super.getSpillFiles());
        if ( null != firstSpillFile ) {
            toReturn.add(firstSpillFile);
        }
        return toReturn;
    }

    // TODO: Will be used by the .netIterator() method
    protected List<File> getNetSpillFiles() {
        return super.getSpillFiles();
    }

    /**
     * Returns an iterator to all additional items that are distinct but were not
     * reported to be so at the time {@link #netAdd(Object)} was invoked.
     * <p/>
     * If you do not exhaust the iterator, you should call
     * {@link org.apache.jena.atlas.iterator.Iter#close(Iterator)} to be sure any
     * open file handles are closed.
     */
    public Iterator<E> netIterator() {
        // If we havn't spilled, then we have already indicated all distinct values
        // via .netAdd()
        if ( !spilled ) {
            return Iter.nullIterator();
        }

        Iterator<E> exclusionsIterator;
        try {
            exclusionsIterator = getInputIterator(firstSpillFile);
        } catch (FileNotFoundException e) {
            throw new AtlasException("Cannot find the first spill file", e);
        }

        // TODO: Improve performance by making the superclass .iterator() use
        // getNetSpillFiles()
        // instead of getSpillFiles() so it doesn't contain the contents of the first
        // file
        Iterator<E> rest = super.iterator();

        SortedDiffIterator<E> sdi = SortedDiffIterator.create(rest, exclusionsIterator, comparator);
        registerCloseableIterator(sdi);

        return sdi;
    }

    /**
     * Produces the set difference of two sorted set sequences.
     */
    protected static class SortedDiffIterator<T> implements IteratorCloseable<T> {
        private final Iterator<T> inputToBeFiltered;
        private final Iterator<T> exclusions;
        private final Comparator<? super T> comp;

        private boolean finished = false;
        private boolean exclusionSlotFull = false;
        private T inputElement;
        private T exclusionElement;

        /**
         * Produces the set difference of two sorted set sequences using the natural
         * ordering of the items (null items will always be considered less than any
         * other items).
         *
         * @param first An Iterator&lt;T&gt; whose elements that are not also in
         *     second will be returned.
         * @param exclusions An Iterator&lt;T&gt; whose elements that also occur in the
         *     first sequence will cause those elements to be removed from the
         *     returned sequence.
         */
        public static <S extends Comparable<? super S>> SortedDiffIterator<S> create(Iterator<S> first, Iterator<S> exclusions) {
            return create(first, exclusions, new Comparator<S>() {
                @Override
                public int compare(S o1, S o2) {
                    if ( null == o1 && null == o2 )
                        return 0;
                    if ( null == o1 )
                        return -1;
                    if ( null == o2 )
                        return 1;
                    return o1.compareTo(o2);
                }
            });
        }

        /**
         * Produces the set difference of two sorted set sequences using the
         * specified comparator.
         *
         * @param first An Iterator&lt;T&gt; whose elements that are not also in
         *     second will be returned.
         * @param second An Iterator&lt;T&gt; whose elements that also occur in the
         *     first sequence will cause those elements to be removed from the
         *     returned sequence.
         * @param comparator The comparator used to compare the elements from each
         *     iterator.
         */
        public static <S> SortedDiffIterator<S> create(Iterator<S> first, Iterator<S> second, Comparator<? super S> comparator) {
            return new SortedDiffIterator<>(first, second, comparator);
        }

        private SortedDiffIterator(Iterator<T> first, Iterator<T> second, Comparator<? super T> comparator) {
            this.inputToBeFiltered = first;
            this.exclusions = second;
            this.comp = comparator;

            // Prime the inputElement item
            fill();
        }

        private void fill() {
            if ( finished )
                return;

            if ( !inputToBeFiltered.hasNext() ) {
                close();
                return;
            }

            if ( !exclusionSlotFull ) {
                if ( !exclusions.hasNext() ) {
                    inputElement = inputToBeFiltered.next();
                    return;
                }

                exclusionElement = exclusions.next();
                exclusionSlotFull = true;
            }

            // Outer loop advances inputElement
            while (true) {
                if ( !inputToBeFiltered.hasNext() ) {
                    close();
                    return;
                }
                inputElement = inputToBeFiltered.next();

                int cmp = comp.compare(inputElement, exclusionElement);

                if ( cmp < 0 )
                    return;

                // Inner loop advances exclusionElement until inputElement is less than or equal to it
                while (cmp > 0) {
                    if ( !exclusions.hasNext() ) {
                        exclusionElement = null;
                        exclusionSlotFull = false;
                        return;
                    }
                    exclusionElement = exclusions.next();
                    cmp = comp.compare(inputElement, exclusionElement);

                    if ( cmp < 0 )
                        return;
                }
            }
        }

        @Override
        public boolean hasNext() {
            return !finished;
        }

        @Override
        public T next() {
            if ( finished )
                throw new NoSuchElementException();
            T toReturn = inputElement;
            fill();
            return toReturn;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("SortedDiffIterator.remove");
        }

        @Override
        public void close() {
            finished = true;
            inputElement = null;
            exclusionElement = null;
            Iter.close(inputToBeFiltered);
            Iter.close(exclusions);
        }
    }

}
