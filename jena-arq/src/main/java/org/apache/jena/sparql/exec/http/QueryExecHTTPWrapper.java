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

import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecWrapper;
import org.apache.jena.sparql.exec.tracker.QueryExecTransform;

/**
 * Wrapper for QueryExecHTTP instances.
 * Uses one delegate for execution and another for information.
 */
public class QueryExecHTTPWrapper
    extends QueryExecWrapper<QueryExec>
    implements QueryExecHTTP
{
    private final QueryExecHTTP httpDelegate;

    // Closing the exec delegate is assumed to close the http delegate.
    public static QueryExecHTTP transform(QueryExecHTTP qExec, QueryExecTransform transform) {
        QueryExecHTTP httpDelegate = qExec;
        QueryExec execDelegate = qExec;

        // Unwrap an existing wrapper.
        if (qExec instanceof QueryExecHTTPWrapper wrapper) {
            httpDelegate = wrapper.getDelegateHttp();
            execDelegate = wrapper.getDelegate();
        }

        QueryExec qe = transform.transform(execDelegate);
        if (qe instanceof QueryExecHTTP qeh) {
            return qeh;
        }

        return new QueryExecHTTPWrapper(httpDelegate, qe);
    }

    public QueryExecHTTPWrapper(QueryExecHTTP delegate) {
        this(delegate, delegate);
    }

    public QueryExecHTTPWrapper(QueryExecHTTP httpDelegate, QueryExec execDelegate) {
        super(execDelegate);
        this.httpDelegate = httpDelegate;
    }

    /** Delegate for HTTP metadata. May be different from the execution delegate obtained from {@link #getDelegate()}. */
    protected QueryExecHTTP getDelegateHttp() {
        return httpDelegate;
    }

    @Override
    public String getAcceptHeaderSelect() {
        return getDelegateHttp().getAcceptHeaderSelect();
    }

    @Override
    public String getAcceptHeaderAsk() {
        return getDelegateHttp().getAcceptHeaderAsk();
    }

    @Override
    public String getAcceptHeaderDescribe() {
        return getDelegateHttp().getAcceptHeaderDescribe();
    }

    @Override
    public String getAcceptHeaderConstructGraph() {
        return getDelegateHttp().getAcceptHeaderConstructGraph();
    }

    @Override
    public String getAcceptHeaderConstructDataset() {
        return getDelegateHttp().getAcceptHeaderConstructDataset();
    }

    @Override
    public String getHttpResponseContentType() {
        return getDelegateHttp().getHttpResponseContentType();
    }
}
