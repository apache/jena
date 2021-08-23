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

import org.apache.jena.sparql.exec.QueryExecutionAdapter;

/**
 * A query execution implementation where queries are executed
 * against a remote service over HTTP.
 */
public class QueryExecutionHTTP extends QueryExecutionAdapter {

    public static QueryExecutionHTTPBuilder create() { return QueryExecutionHTTPBuilder.create(); }
    public static QueryExecutionHTTPBuilder newBuilder() { return QueryExecutionHTTPBuilder.create(); }

    /** Create a new builder for the remote endpoint */
    public static QueryExecutionHTTPBuilder service(String endpointURL) { return QueryExecutionHTTPBuilder.create().endpoint(endpointURL); }

    public QueryExecutionHTTP(QueryExecHTTP qExecHTTP) {
        super(qExecHTTP);
    }

    /** Get the content-type of the response. Only valid after successful execution of the query. */
    public String getHttpResponseContentType() {
        return ((QueryExecHTTP)get()).getHttpResponseContentType();
    }
}