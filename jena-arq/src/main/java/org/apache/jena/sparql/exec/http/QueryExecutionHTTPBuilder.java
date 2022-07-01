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

package org.apache.jena.sparql.exec.http;

import static org.apache.jena.http.HttpLib.copyArray;

import java.net.http.HttpClient;
import java.util.HashMap;

import org.apache.jena.http.sys.ExecHTTPBuilder;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionBuilder;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingLib;
import org.apache.jena.sparql.util.Context;

public class QueryExecutionHTTPBuilder
    extends ExecHTTPBuilder<QueryExecutionHTTP, QueryExecutionHTTPBuilder>
    implements QueryExecutionBuilder {

    public static QueryExecutionHTTPBuilder create() { return new QueryExecutionHTTPBuilder(); }

    public static QueryExecutionHTTPBuilder service(String serviceURL) { return create().endpoint(serviceURL); }

    private QueryExecutionHTTPBuilder() {}

    @Override
    protected QueryExecutionHTTPBuilder thisBuilder() {
        return this;
    }

    @Override
    protected QueryExecutionHTTP buildX(HttpClient hClient, Query queryActual, String queryStringActual, Context cxt) {
        QueryExecHTTP qExec = new QueryExecHTTP(serviceURL, queryActual, queryStringActual, urlLimit,
                                                hClient, new HashMap<>(httpHeaders), Params.create(params), cxt,
                                                copyArray(defaultGraphURIs), copyArray(namedGraphURIs),
                                                sendMode, appAcceptHeader,
                                                timeout, timeoutUnit);
        return new QueryExecutionHTTP(qExec);
    }

    @Override
    public QueryExecutionBuilder substitution(QuerySolution querySolution) {
        Binding binding = BindingLib.toBinding(querySolution);
        super.substitution(binding);
        return thisBuilder();
    }

    @Override
    public QueryExecutionBuilder substitution(String varName, RDFNode value) {
        super.substitution(Var.alloc(varName), value.asNode());
        return thisBuilder();
    }
}
