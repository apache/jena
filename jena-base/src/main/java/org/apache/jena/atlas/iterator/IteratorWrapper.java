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

package org.apache.jena.atlas.iterator;

import java.util.Iterator;
import java.util.function.Consumer;

public class IteratorWrapper<T> implements IteratorCloseable<T> {
    protected final Iterator<T> iterator;

    protected Iterator<T> get() {
        return iterator;
    }

    public IteratorWrapper(Iterator<T> iterator) {
        this.iterator = iterator;
    }

    @Override
    public boolean hasNext() {
        return get().hasNext();
    }

    @Override
    public T next() {
        return get().next();
    }

    @Override
    public void remove() {
        get().remove();
    }

    @Override
    public void forEachRemaining(Consumer<? super T> action) {
        get().forEachRemaining(action);
    }

    @Override
    public void close() {
        Iter.close(iterator);
    }
}
