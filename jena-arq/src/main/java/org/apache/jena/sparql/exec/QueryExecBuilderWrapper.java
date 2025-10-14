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
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Query;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.tracker.QueryExecTransform;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;

public class QueryExecBuilderWrapper<X extends QueryExecBuilder, T extends QueryExecBuilder>
    extends QueryExecModWrapper<X, T>
    implements QueryExecBuilder
{
    public QueryExecBuilderWrapper(T delegate) {
        super(delegate);
    }

    @Override
    public X query(Query query) {
        getDelegate().query(query);
        return self();
    }

    /** Set the query. */
    @Override
    public X query(String queryString) {
        getDelegate().query(queryString);
        return self();
    }

    @Override
    public QueryExecBuilder parseCheck(boolean parseCheck) {
        getDelegate().parseCheck(parseCheck);
        return self();
    }

    /** Set the query. */
    @Override
    public X query(String queryString, Syntax syntax) {
        getDelegate().query(queryString, syntax);
        return self();
    }

    /** Set a context entry. */
    @Override
    public X set(Symbol symbol, Object value) {
        getDelegate().set(symbol, value);
        return self();
    }

    /** Set a context entry. */
    @Override
    public X set(Symbol symbol, boolean value) {
        getDelegate().set(symbol, value);
        return self();
    }

    /**
     * Set the context. If not set, publics to the system context
     * ({@link ARQ#getContext}).
     */
    @Override
    public X context(Context context) {
        getDelegate().context(context);
        return self();
    }

    /** Provide a set of (Var, Node) for substitution in the query when QueryExec is built. */
    @Override
    public X substitution(Binding binding) {
        getDelegate().substitution(binding);
        return self();
    }

    /** Provide a (Var, Node) for substitution in the query when QueryExec is built. */
    @Override
    public X substitution(Var var, Node value) {
        getDelegate().substitution(var, value);
        return self();
    }

    /** Provide a (var name, Node) for substitution in the query when QueryExec is built. */
    @Override
    public X substitution(String var, Node value) {
        getDelegate().substitution(var, value);
        return self();
    }

    /** Set the overall query execution timeout. */
    @Override
    public X timeout(long value, TimeUnit timeUnit) {
        getDelegate().timeout(value, timeUnit);
        return self();
    }

    @Override
    public QueryExecBuilder transformExec(QueryExecTransform queryExecTransform) {
        getDelegate().transformExec(queryExecTransform);
        return self();
    }

    /**
     * Build the {@link QueryExec}. Further changes to he builder do not affect this
     * {@link QueryExec}.
     */
    @Override
    public QueryExec build() {
        return getDelegate().build();
    }
}
