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

package org.apache.jena.sparql.engine.iterator;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.apache.jena.atlas.iterator.IteratorCloseable;
import org.apache.jena.atlas.iterator.IteratorWrapper;
import org.apache.jena.query.QueryCancelledException;

/**
 * Plain iterator version to add "abort" functionality.
 * Iterator that adds an abort operation which can be called at any time,
 * including from another thread, and causes the iterator to throw an exception
 * when next touched (hasNext, next).
 *
 * The abort signal may be shared across a set of components.
 * Hence, an abort on one component will be visible to all shared components.
 */
public class IterAbortable<T> extends IteratorWrapper<T> implements Abortable, IteratorCloseable<T> {
    /**
     * The cancel signal. Thread safe. Never null.
     * May be set by external callers or from this instance.
     */
    private AtomicBoolean cancelSignal;

    public IterAbortable(Iterator<T> iterator) {
        this(iterator, null);
    }

    protected IterAbortable(Iterator<T> iterator, AtomicBoolean cancelSignal) {
        super(iterator);
        this.cancelSignal = (cancelSignal != null) ? cancelSignal : new AtomicBoolean(false);
    }

    public static <T> IterAbortable<T> wrap(Iterator<T> it, AtomicBoolean cancelSignal) {
        return new IterAbortable<>(it, cancelSignal);
    }

    private boolean isAborted() {
        return cancelSignal.get() || Thread.currentThread().isInterrupted();
    }

    private void checkAbort() {
        if ( isAborted() ) {
            throw new QueryCancelledException();
        }
    }

    /** Can call asynchronously at any time */
    @Override
    public void abort() {
        cancelSignal.set(true);
    }

    @Override
    public boolean hasNext() {
        checkAbort();
        return get().hasNext();
    }

    @Override
    public T next() {
        checkAbort();
        return get().next();
    }

    @Override
    public void remove() {
        checkAbort();
        get().remove();
    }

    @Override
    public void forEachRemaining(Consumer<? super T> action) {
        get().forEachRemaining(item -> {
            checkAbort();
            action.accept(item);
        });
    }
}
