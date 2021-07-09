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

package org.apache.jena.http.sys;

import java.net.http.HttpClient;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.apache.jena.http.HttpEnv;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.exec.http.Params;
import org.apache.jena.sparql.exec.http.QuerySendMode;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sys.JenaSystem;

/** Execution builder for remote queries. */
public abstract class ExecHTTPBuilder<X, Y> {
    // neutral superclass.
    // [QExec] Collapse if RDFConnection is an adapter over RDFLink.
    static { JenaSystem.init(); }

    protected String serviceURL = null;
    protected Query query = null;
    protected String queryString = null;
    protected HttpClient httpClient = null;
    protected Map<String, String> httpHeaders = new HashMap<>();
    protected Params params = Params.create();
    protected Context context = null;
    // Accept: Handled as special case because the defaults varies by query type.
    protected String acceptHeader;
    protected long timeout = -1;
    protected TimeUnit timeoutUnit = null;

    protected int urlLimit = HttpEnv.urlLimit;
    protected QuerySendMode sendMode = QuerySendMode.systemtDefault;
    protected List<String> defaultGraphURIs = new ArrayList<>();
    protected List<String> namedGraphURIs = new ArrayList<>();

    public ExecHTTPBuilder() {}

    protected abstract Y thisBuilder();

    /** Set the URL of the query endpoint. */
    public Y service(String serviceURL) {
        this.serviceURL = Objects.requireNonNull(serviceURL);
        return thisBuilder();
    }

    /** Set the query - this also sets the query string to agree with the query argument. */
    public Y query(Query query) {
        this.query = Objects.requireNonNull(query);
        this.queryString = query.toString();
        return thisBuilder();
    }

    /** Set the query string - this also clears any Query already set. */
    public Y queryString(String queryString) {
        this.query = null;
        this.queryString = Objects.requireNonNull(queryString);
        return thisBuilder();
    }

    public Y addDefaultGraphURI(String uri) {
        if (this.defaultGraphURIs == null)
            this.defaultGraphURIs = new ArrayList<>();
        this.defaultGraphURIs.add(uri);
        return thisBuilder();
    }

    public Y addNamedGraphURI(String uri) {
        if (this.namedGraphURIs == null)
            this.namedGraphURIs = new ArrayList<>();
        this.namedGraphURIs.add(uri);
        return thisBuilder();
    }

    public Y httpClient(HttpClient httpClient) {
        this.httpClient = Objects.requireNonNull(httpClient);
        return thisBuilder();
    }

    // Prefer sendMode
//    /**
//     * Send the query using HTTP POST with HTML form-encoded data.
//     * If set false, the URL limit still applies.
//     */
//    public Y sendHtmlForm(boolean htmlForm) {
//        this.sendMode =  htmlForm ? QuerySendMode.asPostForm : QuerySendMode.asGetWithLimitBody;
//        return thisBuilder();
//    }

    /**
     * Choose how to send the query string over HTTP.
     * <p>
     * The default is {@code QuerySendMode.systemDefault} which is {@code QuerySendMode.asGetWithLimitBody} &ndash;
     * send by HTTP GET and a query string unless too long (see {@link #urlLimit}), when it switch to
     * POST and application/sparql-query".
     *
     * @see QuerySendMode
     */
    public Y sendMode(QuerySendMode mode) {
        this.sendMode = mode;
        return thisBuilder();
    }

    /**
     * Send the query using HTTP GET and the HTTP URL query string,
     * unless the request exceeds the {@link #urlGetLimit}
     * (system default {@link HttpEnv#urlLimit}).
     * <p>
     * If it exceeds the limit, switch to using a HTML form and POST request.
     * By default, queries with a log URL are sent in an HTTP form with POST.
     * <p>
     * This is the default setting.
     *
     * @see #urlGetLimit
     * @see #useGet
     * @see #postQuery
     */
    public Y useGetWithLimit() {
        this.sendMode = QuerySendMode.asGetWithLimitBody;
        return thisBuilder();
    }

    /**
     * Send the query using HTTP GET and the HTTP URL query string regardless of length.
     * By default, queries with a long URL are sent in an HTTP POST.
     */
    public Y useGet() {
        this.sendMode = QuerySendMode.asGetAlways;
        return thisBuilder();
    }

    /**
     * Send the query request using POST with a Content-Type of as a
     * "application/sparql-query"
     */
    public Y postQuery() {
        this.sendMode = QuerySendMode.asPostBody;
        return thisBuilder();
    }

    /**
     * Maximum length for a GET request URL, this includes the length of the
     * service endpoint URL - longer than this and the request will use
     * POST/Form.
     * <p>
     * Long URLs can be silently truncated by intermediate systems and proxies.
     * Use of the URL query string means that request are not cached.
     * <p>
     * See also {@link #postQuery} to send the request using HTTP POST with the
     * query in the POST body using {@code Content-Type} "application/sparql-query"
     * <p>
     * See also {@link #sendMode} to choose a specific "send" policy.
     */
    public Y urlGetLimit(int urlLimit) {
        this.urlLimit = urlLimit;
        return thisBuilder();
    }

    /** Merge in {@link Params} from another object. */
    public Y params(Params other) {
        if ( other != null )
            this.params.merge(other);
        return thisBuilder();
    }

    public Y param(String name) {
        Objects.requireNonNull(name);
        this.params.add(name);
        return thisBuilder();
    }

    public Y param(String name, String value) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(value);
        this.params.add(name, value);
        return thisBuilder();
    }

    public Y acceptHeader(String acceptHeader) {
        Objects.requireNonNull(acceptHeader);
        this.acceptHeader = acceptHeader;
        return thisBuilder();
    }

    public Y httpHeader(String headerName, String headerValue) {
        Objects.requireNonNull(headerName);
        Objects.requireNonNull(headerValue);
        this.httpHeaders.put(headerName, headerValue);
        return thisBuilder();
    }

    /** Set the {@link Context}.
     *  This defaults to the global settings of {@code ARQ.getContext()}.
     *  If there was a previous call of {@code context} the multiple contexts are merged.
     * */
    public Y context(Context context) {
        if ( context == null )
            return thisBuilder();
        ensureContext();
        this.context.putAll(context);
        return thisBuilder();
    }

    private void ensureContext() {
        if ( context == null )
            context = new Context();
    }

    /**
     * Set a timeout to the overall overall operation.
     * Time-to-connect can be set with a custom {@link HttpClient} - see {@link java.net.http.HttpClient.Builder#connectTimeout(java.time.Duration)}.
     */
    public Y timeout(long timeout, TimeUnit timeoutUnit) {
        if ( timeout < 0 ) {
            this.timeout = -1;
            this.timeoutUnit = null;
        } else {
            this.timeout = timeout;
            this.timeoutUnit = Objects.requireNonNull(timeoutUnit);
        }
        return thisBuilder();
    }

    public abstract X build();
}
