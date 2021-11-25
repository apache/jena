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
import java.util.concurrent.TimeUnit;

import org.apache.jena.http.sys.ExecHTTPBuilder;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.exec.QueryExecBuilder;
import org.apache.jena.sparql.exec.QueryExecMod;
import org.apache.jena.sparql.util.Context;

public class QueryExecHTTPBuilder extends ExecHTTPBuilder<QueryExecHTTP, QueryExecHTTPBuilder> implements QueryExecMod, QueryExecBuilder {

    public static QueryExecHTTPBuilder create() { return new QueryExecHTTPBuilder(); }

    public static QueryExecHTTPBuilder service(String serviceURL) { return create().endpoint(serviceURL); }

    private QueryExecHTTPBuilder() {}

    @Override
    protected QueryExecHTTPBuilder thisBuilder() {
        return this;
    }

    @Override
    protected QueryExecHTTP buildX(HttpClient hClient, Query queryActual, String queryStringActual, Context cxt) {
        return new QueryExecHTTP(serviceURL, queryActual, queryStringActual, urlLimit,
                                 hClient, new HashMap<>(httpHeaders), Params.create(params), cxt,
                                 copyArray(defaultGraphURIs),
                                 copyArray(namedGraphURIs),
                                 sendMode, appAcceptHeader,
                                 timeout, timeoutUnit);
    }

    @Override
    public QueryExecHTTPBuilder initialTimeout(long timeout, TimeUnit timeUnit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public QueryExecHTTPBuilder overallTimeout(long timeout, TimeUnit timeUnit) {
        super.timeout(timeout, timeUnit);
        return thisBuilder();
    }

    @Override
    public Context getContext() {
        return null;
    }
}
