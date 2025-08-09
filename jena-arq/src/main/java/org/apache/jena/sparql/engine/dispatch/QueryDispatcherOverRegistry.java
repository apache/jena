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

package org.apache.jena.sparql.engine.dispatch;

import java.util.List;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryException;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.util.Context;

/** Abstraction of a registry's single chain as a service executor */
public class QueryDispatcherOverRegistry
    implements QueryDispatcher
{
    protected SparqlDispatcherRegistry registry;

    /** Position in the chain */
    protected int pos;

    public QueryDispatcherOverRegistry(SparqlDispatcherRegistry registry) {
        this(registry, 0);
    }

    public QueryDispatcherOverRegistry(SparqlDispatcherRegistry registry, int pos) {
        super();
        this.registry = registry;
        this.pos = pos;
    }

    protected ChainingQueryDispatcher getDispatcher() {
        List<ChainingQueryDispatcher> queryDispatchers = registry.getQueryDispatchers();
        int n = queryDispatchers.size();
        if (pos >= n) {
            throw new QueryException("No more elements in query dispatcher chain (pos=" + pos + ", chain size=" + n + ")");
        }
        ChainingQueryDispatcher dispatcher = queryDispatchers.get(pos);
        return dispatcher;
    }

    @Override
    public QueryExec create(Query query, DatasetGraph dsg, Binding initialBinding, Context context) {
        ChainingQueryDispatcher dispatcher = getDispatcher();
        QueryDispatcher next = new QueryDispatcherOverRegistry(registry, pos + 1);
        QueryExec result = dispatcher.create(query, dsg, initialBinding, context, next);
        return result;
    }

    @Override
    public QueryExec create(String queryString, Syntax syntax, DatasetGraph dsg, Binding initialBinding, Context context) {
        ChainingQueryDispatcher dispatcher = getDispatcher();
        QueryDispatcher next = new QueryDispatcherOverRegistry(registry, pos + 1);
        QueryExec result = dispatcher.create(queryString, syntax, dsg, initialBinding, context, next);
        return result;
    }
}
