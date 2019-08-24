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

import java.util.Objects;

import org.apache.jena.atlas.logging.Log;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.QueryEngineFactory;
import org.apache.jena.sparql.engine.QueryEngineRegistry;
import org.apache.jena.sparql.engine.QueryExecutionBase;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.util.Context;

/** Query Execution builder. */
public class QueryExecutionBuilder {

    private DatasetGraph dataset = null;
    private Query        query   = null;
    private Context      context = null;
    private Binding      binding = null;    

    public static QueryExecutionBuilder create() {
        return new QueryExecutionBuilder();
    }

    private QueryExecutionBuilder() {}

    public QueryExecutionBuilder query(Query query) {
        this.query = query;
        return this;
    }

    public QueryExecutionBuilder dataset(DatasetGraph dsg) {
        this.dataset = dsg;
        return this;
    }

    public QueryExecutionBuilder context(Context context) {
        this.context = context;
        return this;
    }

    public QueryExecutionBuilder initialBinding(Binding binding) {
        this.binding = binding;
        return this;
    }

    public QueryExecution build() {
        Objects.requireNonNull(query, "Query for QueryExecution");

        query.setResultVars();
        if ( context == null )
            context = ARQ.getContext();

        Context cxt = Context.setupContextForDataset(context, dataset) ;
        QueryEngineFactory f = QueryEngineRegistry.get().find(query, dataset, cxt);
        if ( f == null ) {
            Log.warn(QueryExecutionBuilder.class, "Failed to find a QueryEngineFactory");
            return null;
        }

        // QueryExecutionBase set up the final context, merging in the dataset context and setting the current time.
        QueryExecution qExec = new QueryExecutionBase(query, dataset, cxt, f);
        if ( binding != null )
            qExec.setInitialBinding(binding);
        return qExec;
    }
}
