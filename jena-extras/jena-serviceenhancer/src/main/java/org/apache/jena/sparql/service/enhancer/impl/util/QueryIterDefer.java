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

package org.apache.jena.sparql.service.enhancer.impl.util;

import java.util.Objects;
import java.util.function.Supplier;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIteratorWrapper;
import org.apache.jena.sparql.serializer.SerializationContext;

/** Deferred (lazy) iterator which initializes a delegate from a supplier only when needed */
public class QueryIterDefer
    extends QueryIteratorWrapper
{
    protected Supplier<QueryIterator> supplier;

    public QueryIterDefer(Supplier<QueryIterator> supplier) {
        super(null);
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
        return super.hasNextBinding();
    }

    @Override
    protected Binding moveToNextBinding() {
        ensureInitialized();
        return super.moveToNextBinding();
    }

    @Override
    public void output(IndentedWriter out) {
        ensureInitialized();
        super.output(out);
    }

    @Override
    protected void closeIterator() {
        super.closeIterator();
    }

    @Override
    public void output(IndentedWriter out, SerializationContext sCxt) {
        ensureInitialized();
        super.output(out, sCxt);
    }
}
