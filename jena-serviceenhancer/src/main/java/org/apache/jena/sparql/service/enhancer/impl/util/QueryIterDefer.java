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

package org.apache.jena.sparql.service.enhancer.impl.util;

import java.util.Objects;
import java.util.function.Supplier;

import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIter;

/** Deferred (lazy) iterator which initializes a delegate from a supplier only when needed */
public class QueryIterDefer
    extends QueryIter
{
    protected Supplier<QueryIterator> supplier;
    protected QueryIterator iterator;

    public QueryIterDefer(ExecutionContext execCxt, Supplier<QueryIterator> supplier) {
        super(execCxt);
        this.supplier = supplier;
    }

    protected void ensureInitialized() {
        if (iterator == null) {
            iterator = Objects.requireNonNull(supplier.get(), "Deferred iterator supplier yeld null");
        }
    }

    @Override
    protected boolean hasNextBinding() {
        ensureInitialized();
        return iterator.hasNext();
    }

    @Override
    protected Binding moveToNextBinding() {
        ensureInitialized();
        return iterator.hasNext() ? iterator.next() : null;
    }

    @Override
    protected void requestCancel() {
        if (iterator != null) {
            iterator.cancel();
        }
    }

    @Override
    protected void closeIterator() {
        if (iterator != null) {
            iterator.close();
        }
    }
}
