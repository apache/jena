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

package org.apache.jena.sparql.service.enhancer.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * A task that buffers items from an iterator in a list upon calling {@link #run()}.
 * The task runs until {@link #stop()} is called or the thread is interrupted.
 */
public class PrefetchTaskBase<T, X extends Iterator<T>>
    implements Runnable
{
    public enum State {
        CREATED,
        STARTING,
        RUNNING,
        TERMINATED
    }

    protected volatile X iterator;
    protected UnaryOperator<T> itemCopyFn;
    protected volatile List<T> bufferedItems;
    protected long maxBufferedItemsCount;

    protected volatile boolean isStopRequested;

    protected volatile State state = State.CREATED;

    /** When a tasks terminates in failure then this field is set.
     *  Consequently, {@link #run()} will never fail with an exception. */
    protected volatile Throwable throwable = null;

    public PrefetchTaskBase(X iterator, long maxBufferedItemsCount, UnaryOperator<T> copyFn) {
        this(iterator, new ArrayList<>(1024), maxBufferedItemsCount, copyFn);
    }

    /**
     *
     * @param iterator
     * @param bufferedItems
     * @param maxBufferedItemsCount
     * @param copyFn A function to copy items before buffering them.
     *               Can be used to detach items from resources.
     *               The copyFn be null.
     */
    public PrefetchTaskBase(X iterator, List<T> bufferedItems, long maxBufferedItemsCount, UnaryOperator<T> copyFn) {
        super();
        this.maxBufferedItemsCount = maxBufferedItemsCount;
        this.iterator = iterator;
        this.bufferedItems = bufferedItems;
        this.itemCopyFn = copyFn;
    }

    public List<T> getBufferedItems() {
        return bufferedItems;
    }

    public X getIterator() {
        return iterator;
    }

    public State getState() {
        return state;
    }

    public UnaryOperator<T> getCopyFn() {
        return itemCopyFn;
    }

    @Override
    public final void run() {
        // Before the first item has been processed the state remains in STARTING.
        state = State.STARTING;
        try {
            runActual();
        } catch (Throwable t) {
            this.throwable = t;
        } finally {
            try {
                afterRun();
            } finally {
                state = State.TERMINATED;
            }
        }
    }

    protected void afterRun() {}

    protected void runActual() {
        state = State.RUNNING;
        while (!isStopRequested && !Thread.interrupted() && iterator.hasNext() && bufferedItems.size() < maxBufferedItemsCount) {
            T item = iterator.next();
            T copy = itemCopyFn == null ? item : itemCopyFn.apply(item);
            bufferedItems.add(copy);
        }
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void stop() {
        isStopRequested = true;
    }

    public static <T, I extends Iterator<T>> PrefetchTaskBase<T, I> of(I iterator, long maxBufferedItemsCount, UnaryOperator<T> copyFn) {
        return new PrefetchTaskBase<>(iterator, maxBufferedItemsCount, copyFn);
    }
}
