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

package org.apache.jena.atlas.iterator;

import java.util.Iterator;
import java.util.function.Predicate;

/** Iterate drop while a condition is true, then emit the remaining items. */
final
public class IteratorDropWhile<T> extends IteratorSlotted<T> {
    private final Predicate<T> predicate;
    private Iterator<T> iter;
    private boolean isInitialized = false;

    public IteratorDropWhile(Iterator<T> iter, Predicate<T> predicate) {
        this.iter = iter;
        this.predicate = predicate;
    }

    @Override
    protected boolean hasMore() {
        return true;
    }

    @Override
    protected T moveToNext() {
        // During initialize we drop items as long as the predicate evaluates to true.
        if (!isInitialized) {
            isInitialized = true;
            while (iter.hasNext()) {
                T item = iter.next();
                if (!predicate.test(item)) {
                    return item;
                }
            }
            return null;
        }

        if (!iter.hasNext())
            return null;
        T item = iter.next();
        return item;
    }

    @Override
    protected void closeIterator() {
        Iter.close(iter);
    }
}
