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

package org.apache.jena.sparql.exec.tracker;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.jena.atlas.iterator.IteratorWrapper;

/**
 * Iterator wrapper that forwards an encountered exception
 * to a configured destination.
 */
public class IteratorTracked<T>
    extends IteratorWrapper<T>
{
    protected ThrowableTracker tracker;

    public IteratorTracked(Iterator<T> iterator, ThrowableTracker tracker) {
        super(iterator);
        this.tracker = Objects.requireNonNull(tracker);
    }

    @Override
    public boolean hasNext() {
        return trackBoolean(tracker, get()::hasNext);
    }

    @Override
    public T next() {
        return track(tracker, get()::next);
    }

    @Override
    public void forEachRemaining(Consumer<? super T> action) {
        trackForEachRemaining(tracker, get(), action);
    }

    public static boolean trackBoolean(ThrowableTracker tracker, BooleanSupplier action) {
        try {
            boolean result = action.getAsBoolean();
            return result;
        } catch (Throwable t) {
            tracker.report(t);
            t.addSuppressed(new RuntimeException("Error during hasNext."));
            throw t;
        }
    }

    public static <T> T track(ThrowableTracker tracker, Supplier<T> action) {
        try {
            T result = action.get();
            return result;
        } catch (Throwable t) {
            tracker.report(t);
            t.addSuppressed(new RuntimeException("Error during hasNext."));
            throw t;
        }
    }

    public static <T> void trackForEachRemaining(ThrowableTracker tracker, Iterator<T> it, Consumer<? super T> action) {
        try {
            it.forEachRemaining(action);
        } catch (Throwable t) {
            tracker.report(t);
            t.addSuppressed(new RuntimeException("Error during forEachRemaining."));
            throw t;
        }
    }
}
