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

import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIter;

/** QueryIter-based wrapper which tracks open iterators in the execution context. */
public class QueryIterOverAbortableIterator
    extends QueryIter
{
    private AbortableIterator<Binding> delegate;

    public QueryIterOverAbortableIterator(ExecutionContext execCxt, AbortableIterator<Binding> delegate) {
        super(execCxt);
        this.delegate = delegate;
    }

    @Override
    protected boolean hasNextBinding() {
        return delegate.hasNext();
    }

    @Override
    protected Binding moveToNextBinding() {
        return delegate.next();
    }

    @Override
    protected void closeIterator() {
        delegate.close();
    }

    @Override
    protected void requestCancel() {
        delegate.cancel();
    }
}
