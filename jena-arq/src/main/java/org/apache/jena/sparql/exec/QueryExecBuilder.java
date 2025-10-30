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

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Query;
import org.apache.jena.query.Syntax;
import org.apache.jena.riot.rowset.RowSetOnClose;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.algebra.TableFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.tracker.QueryExecTransform;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;

/** The common elements of a {@link QueryExec} builder. */
public interface QueryExecBuilder extends QueryExecMod {

    /** Set the query. */
    public QueryExecBuilder query(Query query);

    /** Set the query. */
    public QueryExecBuilder query(String queryString);

    /** Set the query. */
    public QueryExecBuilder query(String queryString, Syntax syntax);

    /** Hint whether to immediately parse query strings passed to {@link #query(String)} */
    public QueryExecBuilder parseCheck(boolean parseCheck);

    /** Set a context entry. */
    public QueryExecBuilder set(Symbol symbol, Object value);

    /** Set a context entry. */
    public QueryExecBuilder set(Symbol symbol, boolean value);

    /**
     * Set the context. If not set, defaults to the system context
     * ({@link ARQ#getContext}).
     */
    public QueryExecBuilder context(Context context);

    /** Provide a set of (Var, Node) for substitution in the query when QueryExec is built. */
    public QueryExecBuilder substitution(Binding binding);

    /** Provide a (Var, Node) for substitution in the query when QueryExec is built. */
    public QueryExecBuilder substitution(Var var, Node value);

    /** Provide a (var name, Node) for substitution in the query when QueryExec is built. */
    public default QueryExecBuilder substitution(String var, Node value) {
        return substitution(Var.alloc(var), value);
    }

    public QueryExecBuilder transformExec(QueryExecTransform queryExecTransform);

    /** Set the overall query execution timeout. */
    @Override
    public QueryExecBuilder timeout(long value, TimeUnit timeUnit);

    @Override
    public QueryExecBuilder timeout(long timeout);

    @Override
    public QueryExecBuilder initialTimeout(long timeout, TimeUnit timeUnit);

    @Override
    public QueryExecBuilder overallTimeout(long timeout, TimeUnit timeUnit);

    /**
     * Build the {@link QueryExec}. Further changes to he builder do not affect this
     * {@link QueryExec}.
     */
    @Override
    public QueryExec build();

    // build-and-use short cuts

    /**
     * Build and execute as a SELECT query.
     * The caller must eventually close the returned RowSet
     * in order to free any associated resources.
     * Use {@link #table()} to obtain an independent in-memory copy of the row set.
     */
    public default RowSet select() {
        QueryExec qExec = build();
        RowSet core = qExec.select();
        return new RowSetOnClose(core, qExec::close);
    }

    /** Build and execute as a CONSTRUCT query. */
    public default Graph construct() {
        try ( QueryExec qExec = build() ) {
            return qExec.construct();
        }
    }

    /** Build and execute as a CONSTRUCT query. */
    public default Graph describe() {
        try ( QueryExec qExec = build() ) {
            return qExec.describe();
        }
    }

    /** Build and execute as an ASK query. */
    public default boolean ask() {
        try ( QueryExec qExec = build() ) {
            return qExec.ask();
        }
    }

    /**
     * Build and execute as a SELECT query.
     * Creates and returns an independent in-memory table by materializing the underlying row set.
     * Subsequently, {@link Table#toRowSet()} can be used to obtain a fresh row set view over the table.
     */
    public default Table table() {
        Table result;
        try (QueryExec qExec = build()) {
            RowSet rowSet = qExec.select();
            result = TableFactory.create(rowSet);
        }
        return result;
    }
}
