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

package org.apache.jena.sparql.exec;

import java.util.concurrent.TimeUnit;

import org.apache.jena.sparql.util.Context;

public class QueryExecModWrapper<X extends QueryExecMod, T extends QueryExecMod>
    implements QueryExecMod
{
    protected T delegate;

    public QueryExecModWrapper(T delegate) {
        super();
        this.delegate = delegate;
    }

    protected T getDelegate() {
        return delegate;
    }

    @SuppressWarnings("unchecked")
    public X self() {
        return (X)this;
    }

    @Override
    public X timeout(long timeout) {
        getDelegate().timeout(timeout, TimeUnit.MILLISECONDS);
        return self();
    }

    @Override
    public X timeout(long timeout, TimeUnit timeoutUnits) {
        getDelegate().timeout(timeout, TimeUnit.MILLISECONDS);
        return self();
    }

    @Override
    public X initialTimeout(long timeout, TimeUnit timeUnit) {
        getDelegate().initialTimeout(timeout, timeUnit);
        return self();
    }

    @Override
    public X overallTimeout(long timeout, TimeUnit timeUnit) {
        getDelegate().overallTimeout(timeout, timeUnit);
        return self();
    }

    @Override
    public Context getContext() {
        return getDelegate().getContext();
    }

    @Override
    public QueryExec build() {
        return getDelegate().build();
    }
}
