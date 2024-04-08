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
package org.apache.jena.sparql.engine.join;

import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;

/**
 * An immutable list without duplicates.
 *
 * For lists of size {@value ImmutableUniqueList#INDEX_THRESHOLD} or larger,
 * the {@link #indexOf(Object)} method will on first use index the elements for faster access.
 */
public class ImmutableUniqueList<T> extends AbstractList<T> {
    /** Threshold in the number of variables for when to use additional indexing structures
     *  in order to improve scalability */
    static final int INDEX_THRESHOLD = 5;

    /** The builder can emit a key every time build() is called
     * and it can be continued to be used.
     */
    public static final class Builder<T> {
        private Class<T> itemClass;

        /**
         * The keys collection upgrades itself from ArrayList to
         * LinkedHashSet upon adding a sufficient number of items.
         */
        private Collection<T> items;

        Builder(Class<T> itemClass) {
            super();
            this.itemClass = itemClass;
        }

        private void alloc(int n) {
            if (items == null) {
                items = n < INDEX_THRESHOLD ? new ArrayList<>(INDEX_THRESHOLD) : new LinkedHashSet<>();
            } else if (!(items instanceof Set) && (items.size() + n >= INDEX_THRESHOLD)) {
                Set<T> tmp = new LinkedHashSet<>(items);
                items = tmp;
            }
        }

        public Builder<T> add(T item) {
            if (!(items instanceof Set)) {
                if ( items == null || ! items.contains(item) ) {
                    alloc(1);
                    items.add(item) ;
                }
            } else {
                items.add(item);
            }
            return this ;
        }

        public Builder<T> addAll(Collection<T> items) {
            alloc(items.size());
            for (T item : items) {
                add(item);
            }
            return this;
        }

        public Builder<T> addAll(T[] arr) {
            alloc(arr.length);
            for (T item : arr) {
                add(item);
            }
            return this;
        }

        public Builder<T> remove(Object o) {
            if (items != null) {
                items.remove(o) ;
            }
            return this ;
        }

        public Builder<T> clear() {
            items = null;
            return this ;
        }

        public boolean isEmpty() {
            return items == null || items.isEmpty();
        }

        @SuppressWarnings("unchecked")
        public ImmutableUniqueList<T> build() {
            T[] finalItems;
            if (items == null) {
                finalItems = (T[])Array.newInstance(itemClass, 0);
            } else {
                finalItems = (T[])Array.newInstance(itemClass, items.size());
                items.toArray(finalItems);
            }
            return new ImmutableUniqueList<>(INDEX_THRESHOLD, finalItems);
        }
    }

    public static <T> Builder<T> newUniqueListBuilder(Class<T> itemClass) {
        return new Builder<>(itemClass);
    }

    public static <T> ImmutableUniqueList<T> createUniqueList(Class<T> itemClass, Collection<T> items) {
        return ImmutableUniqueList.<T>newUniqueListBuilder(itemClass).addAll(items).build();
    }

    public static <T> ImmutableUniqueList<T> createUniqueList(Class<T> itemClass, T[] items) {
        return ImmutableUniqueList.<T>newUniqueListBuilder(itemClass).addAll(items).build();
    }

    /** Subclasses may access the keys array but must never modify it! */
    protected final T[] elementData;
    protected final int indexThreshold;

    /** keyToIdx mapping is initialized lazily in {@link #indexOf(Object)} */
    private transient Map<T, Integer> elementToIndex;

    protected ImmutableUniqueList(T[] elementData) {
        this(INDEX_THRESHOLD, elementData);
    }

    protected ImmutableUniqueList(int indexThreshold, T[] elementData) {
        super();
        this.indexThreshold = indexThreshold;
        this.elementData = elementData ;
    }

    @Override
    public int size()                 { return elementData.length; }

    public int length()               { return size(); }

    @Override
    public T get(int i)               { return elementData[i]; }

    @Override
    public boolean contains(Object o) { return indexOf(o) != -1; }

    @Override
    public int indexOf(Object o) {
        int result;
        if (elementData.length < indexThreshold) {
            result = ArrayUtils.indexOf(elementData, o);
        } else {
            if (elementToIndex != null) {
                result = elementToIndex.getOrDefault(o, -1);
            } else {
                // Compute the map from element to its index
                Map<T, Integer> map = new HashMap<>();
                for (int i = 0; i < elementData.length; ++i) {
                    T key = elementData[i];
                    map.put(key, i);
                }
                result = map.getOrDefault(o, -1);
                elementToIndex = map;
            }
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        return super.equals(obj);
    }
}
