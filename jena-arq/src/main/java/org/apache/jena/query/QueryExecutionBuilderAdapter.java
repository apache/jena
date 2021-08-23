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

package org.apache.jena.query;

import java.util.concurrent.TimeUnit;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingLib;
import org.apache.jena.sparql.exec.QueryExecBuilder;
import org.apache.jena.sparql.exec.QueryExecutionAdapter;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;

/**
 * Query Execution for local datasets - builder style.
 */
public class QueryExecutionBuilderAdapter implements QueryExecutionBuilder {

    // This only implements QueryExecutionBuilderCommon, the builder steps in common between
    // local (dataset) and remote (HTTP) query building. In particular, it does not
    // provide operations to change the target of the query.

    private final QueryExecBuilder builder;

    public QueryExecutionBuilderAdapter(QueryExecBuilder builder) {
        this.builder = builder;
    }

    public QueryExecBuilder getExecBuilder() { return builder; }

    @Override
    public QueryExecutionBuilderAdapter query(Query query) {
        builder.query(query);
        return this;
    }

    @Override
    public QueryExecutionBuilderAdapter query(String queryString) {
        builder.query(queryString);
        return this;
    }

    @Override
    public QueryExecutionBuilderAdapter query(String queryString, Syntax syntax) {
        builder.query(queryString, syntax);
        return this;
    }

    @Override
    public QueryExecutionBuilderAdapter set(Symbol symbol, Object value) {
        builder.set(symbol, value);
        return this;
    }

    @Override
    public QueryExecutionBuilderAdapter set(Symbol symbol, boolean value) {
        builder.set(symbol, value);
        return this;
    }

    @Override
    public QueryExecutionBuilderAdapter context(Context context) {
        builder.context(context);
        return this;
    }

    @Override
    public QueryExecutionBuilderAdapter substitution(QuerySolution querySolution) {
        if ( querySolution != null ) {
            Binding binding = BindingLib.toBinding(querySolution);
            builder.substitution(binding);
        }
        return this;
    }

    public QueryExecutionBuilderAdapter substitution(Binding binding) {
        builder.substitution(binding);
        return this;
    }

    @Override
    public QueryExecutionBuilderAdapter substitution(String varName, RDFNode value) {
        Var var = Var.alloc(varName);
        Node val = value.asNode();
        builder.substitution(var, val);
        return this;
    }

    @Override
    public QueryExecutionBuilderAdapter timeout(long value, TimeUnit timeUnit) {
        builder.timeout(value, timeUnit);
        return this;
    }

    @Override
    public QueryExecution build() {
        // No support for delayed setup.
        return QueryExecutionAdapter.adapt(builder.build());
    }
}

