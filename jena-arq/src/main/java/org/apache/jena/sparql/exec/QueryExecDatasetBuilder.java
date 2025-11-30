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

package org.apache.jena.sparql.exec;

import java.util.concurrent.TimeUnit;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.tracker.QueryExecTransform;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;

/** Interface for dataset-centric query exec builders. */
public interface QueryExecDatasetBuilder
    extends QueryExecBuilder
{
    /** Create an uninitialized {@link QueryExecDatasetBuilderDeferred}. */
    public static QueryExecDatasetBuilder newBuilder() {
        return QueryExecDatasetBuilderDeferred.create();
    }

    public static QueryExecDatasetBuilder create() {
        return QueryExecDatasetBuilderDeferred.create();
    }

    // TODO SparqlAdapter binds QueryExecBuilder to a dsg - must not set it afterwards.
    //   However, QueryExecDatasetBuilder{Deferred, Impl} would both allow for changing the dataset.
    QueryExecDatasetBuilder dataset(DatasetGraph dsg);

    @Override public QueryExecDatasetBuilder query(Query query);
    @Override public QueryExecDatasetBuilder query(String queryString);
    @Override public QueryExecDatasetBuilder query(String queryString, Syntax syntax);
    @Override public QueryExecDatasetBuilder parseCheck(boolean parseCheck);
    @Override public QueryExecDatasetBuilder set(Symbol symbol, Object value);
    @Override public QueryExecDatasetBuilder set(Symbol symbol, boolean value);
    @Override public QueryExecDatasetBuilder context(Context context);
    @Override public QueryExecDatasetBuilder substitution(Binding binding);

    @Override public QueryExecDatasetBuilder substitution(Var var, Node value);

    /** Provide a (var name, Node) for substitution in the query when QueryExec is built. */
    @Override public default QueryExecDatasetBuilder substitution(String var, Node value) {
        return substitution(Var.alloc(var), value);
    }

    @Override
    public QueryExecDatasetBuilder transformExec(QueryExecTransform queryExecTransform);

    /** Set the overall query execution timeout. */
    @Override
    public QueryExecDatasetBuilder timeout(long value, TimeUnit timeUnit);

    @Override
    public QueryExecDatasetBuilder timeout(long timeout);

    @Override
    public QueryExecDatasetBuilder initialTimeout(long timeout, TimeUnit timeUnit);

    @Override
    public QueryExecDatasetBuilder overallTimeout(long timeout, TimeUnit timeUnit);

    Query getQuery();
    String getQueryString();
}

