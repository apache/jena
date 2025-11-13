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

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;
import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateRequest;

public class UpdateExecBuilderWrapper<X extends UpdateExecBuilder, T extends UpdateExecBuilder>
    implements UpdateExecBuilder
{
    private T delegate;

    public UpdateExecBuilderWrapper(T delegate) {
        super();
        this.delegate = delegate;
    }

    protected T getDelegate() {
        return delegate;
    }

    @SuppressWarnings("unchecked")
    protected X self() {
        return (X)this;
    }

    @Override
    public X update(UpdateRequest request) {
        getDelegate().update(request);
        return self();
    }

    @Override
    public X update(Update update) {
        getDelegate().update(update);
        return self();
    }

    @Override
    public X update(String updateString) {
        getDelegate().update(updateString);
        return self();
    }

    @Override
    public X parseCheck(boolean parseCheck) {
        getDelegate().parseCheck(parseCheck);
        return self();
    }

    @Override
    public X set(Symbol symbol, Object value) {
        getDelegate().set(symbol, value);
        return self();
    }

    @Override
    public X set(Symbol symbol, boolean value) {
        getDelegate().set(symbol, value);
        return self();
    }

    @Override
    public X context(Context context) {
        getDelegate().context(context);
        return self();
    }

    @Override
    public X substitution(Binding binding) {
        getDelegate().substitution(binding);
        return self();
    }

    @Override
    public X substitution(Var var, Node value) {
        getDelegate().substitution(var, value);
        return self();
    }

    @Override
    public X substitution(String var, Node value) {
        getDelegate().substitution(var, value);
        return self();
    }

    @Override
    public X timeout(long value, TimeUnit timeUnit) {
        getDelegate().timeout(value, timeUnit);
        return self();
    }

    @Override
    public UpdateExec build() {
        UpdateExec result = getDelegate().build();
        return result;
    }

    @Override
    public void execute() {
        getDelegate().execute();
    }
}
