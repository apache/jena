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

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionBuilder;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.core.ResultBinding;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.ModelUtils;
import org.apache.jena.sparql.util.Symbol;

/** QueryExecBuilder view over a QueryExecutionBuilder */
public class QueryExecBuilderAdapter
    implements QueryExecBuilder
{
    protected QueryExecutionBuilder builder;

    protected QueryExecBuilderAdapter(QueryExecutionBuilder builder) {
        super();
        this.builder = builder;
    }

    /** Adapter that attempts to unwrap a QueryExecutionBuilderAdapter's builder */
    public static QueryExecBuilder adapt(QueryExecutionBuilder builder) {
        Objects.requireNonNull(builder);

        QueryExecBuilder result = builder instanceof QueryExecutionBuilderAdapter
                ? ((QueryExecutionBuilderAdapter)builder).getExecBuilder()
                : new QueryExecBuilderAdapter(builder);

        return result;
    }

    public QueryExecutionBuilder getExecBuilder() {
        return builder;
    }

    @Override
    public QueryExecMod initialTimeout(long timeout, TimeUnit timeUnit) {
        // Gracefully ignore?
        // builder = builder.timeout(timeout, timeUnit);
        return this;
    }

    @Override
    public QueryExecMod overallTimeout(long timeout, TimeUnit timeUnit) {
        builder = builder.timeout(timeout, timeUnit);
        return this;
    }

    @Override
    public Context getContext() {
        throw new UnsupportedOperationException("QueryExecBuilderAdapter.getContext()");
    }

    @Override
    public QueryExecBuilder query(Query query) {
        builder = builder.query(query);
        return this;
    }

    @Override
    public QueryExecBuilder query(String queryString) {
        builder = builder.query(queryString);
        return this;
    }

    @Override
    public QueryExecBuilder query(String queryString, Syntax syntax) {
        builder = builder.query(queryString, syntax);
        return this;
    }

    @Override
    public QueryExecBuilder set(Symbol symbol, Object value) {
        builder = builder.set(symbol, value);
        return this;
    }

    @Override
    public QueryExecBuilder set(Symbol symbol, boolean value) {
        builder = builder.set(symbol, value);
        return this;
    }

    @Override
    public QueryExecBuilder context(Context context) {
        builder = builder.context(context);
        return this;
    }

    @Override
    public QueryExecBuilder substitution(Binding binding) {
        builder = builder.substitution(new ResultBinding(null, binding));
        return this;
    }

    @Override
    public QueryExecBuilder substitution(Var var, Node value) {
        builder = builder.substitution(var.getName(), ModelUtils.convertGraphNodeToRDFNode(value));
        return this;
    }

    @Override
    public QueryExecBuilder timeout(long value, TimeUnit timeUnit) {
        builder = builder.timeout(value, timeUnit);
        return this;
    }

    @Override
    public QueryExec build() {
        QueryExecution qExec = builder.build();
        QueryExec result = QueryExecAdapter.adapt(qExec);
        return result;
    }
}
