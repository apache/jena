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

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.apache.jena.riot.rowset.RowSetWrapper;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.RowSet;

/** RowSetWrapper that tracks any encountered exceptions in the provided tracker. */
public class RowSetTracked
    extends RowSetWrapper
{
    protected ThrowableTracker tracker;

    public RowSetTracked(RowSet other, ThrowableTracker tracker) {
        super(other);
        this.tracker = Objects.requireNonNull(tracker);
    }

    public ThrowableTracker getTracker() {
        return tracker;
    }

    @Override
    public boolean hasNext() {
        return IteratorTracked.trackBoolean(tracker, get()::hasNext);
    }

    @Override
    public Binding next() {
        return IteratorTracked.track(tracker, get()::next);
    }

    @Override
    public List<Var> getResultVars() {
        return IteratorTracked.track(tracker, get()::getResultVars);
    }

    @Override
    public void forEachRemaining(Consumer<? super Binding> action) {
        IteratorTracked.trackForEachRemaining(tracker, get(), action);
    }
}
