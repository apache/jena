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

package org.apache.jena.sparql.service.enhancer.impl.util.iterator;

import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;

import org.apache.jena.atlas.lib.Closeable;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;

public class AbortableIterators {
    public static <T> AbortableIterator<T> empty() {
        return wrap(Collections.<T>emptyIterator());
    }

    /** Bridge between {@code QueryIterator} and {@code AbortableIterator}. */
    public static AbortableIterator<Binding> adapt(QueryIterator qIter) {
        return new AbortableIteratorOverQueryIterator(qIter);
    }

    public static <T> AbortableIterator<T> wrap(Iterator<T> it) {
        return it instanceof AbortableIterator<T> ait
            ? ait
            : new AbortableIteratorOverIterator<>(it);
    }

    public static QueryIterator asQueryIterator(AbortableIterator<Binding> it) {
        return it instanceof AbortableIteratorOverQueryIterator x
            ? x.delegate()
            : new QueryIteratorOverAbortableIterator(it);
    }

    public static <T> AbortableIterator<T> concat(AbortableIterator<T> a, AbortableIterator<T> b) {
        AbortableIteratorConcat<T> result = new AbortableIteratorConcat<>();
        result.add(a);
        result.add(b);
        return result;
    }

    /** Wrap an {@link AbortableIterator} with an additional close action. */
    public static <T> AbortableIterator<T> onClose(AbortableIterator<T> qIter, Closeable action) {
        Objects.requireNonNull(qIter);
        AbortableIterator<T> result = action == null
            ? qIter
            : new AbortableIteratorWrapper<>(qIter) {
                @Override
                protected void closeIterator() {
                    try {
                        action.close();
                    } finally {
                        super.closeIterator();
                    }
                }
            };
        return result;
    }
}
