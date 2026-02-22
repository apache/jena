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

package org.apache.jena.http.sys;

import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.apache.jena.graph.Node;
import org.apache.jena.http.HttpEnv;
import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.adapter.ParseCheckUtils;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.http.Params;
import org.apache.jena.sparql.exec.http.UpdateSendMode;
import org.apache.jena.sparql.syntax.syntaxtransform.UpdateTransformOps;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.ContextAccumulator;
import org.apache.jena.sparql.util.Symbol;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateException;
import org.apache.jena.update.UpdateRequest;

public abstract class ExecUpdateHTTPBuilder<X, Y> {

    static { JenaSystem.init(); }

    protected String serviceURL;
    protected Boolean parseCheck = null;
    private UpdateEltAcc updateEltAcc = new UpdateEltAcc();

    protected Params params = Params.create();
    protected boolean allowCompression;
    protected Map<String, String> httpHeaders = new HashMap<>();
    protected HttpClient httpClient;
    protected UpdateSendMode sendMode = UpdateSendMode.systemDefault;
    protected List<String> usingGraphURIs = null;
    protected List<String> usingNamedGraphURIs = null;
    private ContextAccumulator contextAcc = ContextAccumulator.newBuilder(()->ARQ.getContext());
    // Uses query rewrite to replace variables by values.
    protected Map<Var, Node> substitutionMap     = new HashMap<>();
    protected long timeout = -1;
    protected TimeUnit timeoutUnit = null;

    protected ExecUpdateHTTPBuilder() {}

    protected abstract Y thisBuilder();

    public Y endpoint(String serviceURL) {
        this.serviceURL = serviceURL;
        return thisBuilder();
    }

    public Y update(UpdateRequest updateRequest) {
        Objects.requireNonNull(updateRequest);
        updateEltAcc.add(updateRequest);
        return thisBuilder();
    }

    public Y update(String updateRequestString) {
        Objects.requireNonNull(updateRequestString);
        if (effectiveParseCheck()) {
            updateEltAcc.add(updateRequestString);
        } else {
            updateEltAcc.addString(updateRequestString);
        }
        return thisBuilder();
    }

    /** Add the update. */
    public Y update(Update update) {
        Objects.requireNonNull(update);
        updateEltAcc.add(update);
        return thisBuilder();
    }

    /**
     * Set the update - this replaces any previous updates added. The update string
     * is used as given including nonstandard syntax features offered by the remote
     * SPARQL system.
     */
    public Y updateString(String updateString) {
        Objects.requireNonNull(updateString);
        updateEltAcc.clear();
        updateEltAcc.addString(updateString);
        return thisBuilder();
    }

    public Y parseCheck(boolean parseCheck) {
        this.parseCheck = parseCheck;
        return thisBuilder();
    }

    protected boolean effectiveParseCheck() {
        return ParseCheckUtils.effectiveParseCheck(parseCheck, contextAcc);
    }

    public Y substitution(Binding binding) {
        binding.forEach(this.substitutionMap::put);
        return thisBuilder();
    }

    public Y substitution(String var, Node value) {
        return substitution(Var.alloc(var), value);
    }

    public Y substitution(Var var, Node value) {
        this.substitutionMap.put(var, value);
        return thisBuilder();
    }

    public Y timeout(long timeout, TimeUnit timeoutUnit) {
        this.timeout = timeout;
        this.timeoutUnit = timeoutUnit;
        return thisBuilder();
    }

    public Y httpClient(HttpClient httpClient) {
        this.httpClient = Objects.requireNonNull(httpClient);
        return thisBuilder();
    }

    /**
     * Choose whether to send using POST as "application/sparql-update" (preferred) or
     * as an HTML form, content type "application/x-www-form-urlencoded".
     */
    public Y sendMode(UpdateSendMode mode) {
        this.sendMode = mode;
        return thisBuilder();
    }

    public Y addUsingGraphURI(String uri) {
        if (this.usingGraphURIs == null)
            this.usingGraphURIs = new ArrayList<>();
        this.usingGraphURIs.add(uri);
        return thisBuilder();
    }

    public Y addUsingNamedGraphURI(String uri) {
        if (this.usingNamedGraphURIs == null)
            this.usingNamedGraphURIs = new ArrayList<>();
        this.usingNamedGraphURIs.add(uri);
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

    public Y httpHeader(String headerName, String headerValue) {
        Objects.requireNonNull(headerName);
        Objects.requireNonNull(headerValue);
        this.httpHeaders.put(headerName, headerValue);
        return thisBuilder();
    }

    public Y httpHeaders(Map<String, String> headers) {
        Objects.requireNonNull(headers);
        this.httpHeaders.putAll(headers);
        return thisBuilder();
    }

    /** Set the {@link Context}.
     *  This defaults to the global settings of {@code ARQ.getContext()}.
     *  If there was a previous call of {@code context} the multiple contexts are merged.
     * */
    public Y context(Context context) {
        if ( context == null )
            return thisBuilder();
        this.contextAcc.context(context);
        return thisBuilder();
    }

    public Y set(Symbol symbol, Object value) {
        this.contextAcc.set(symbol, value);
        return thisBuilder();
    }

    public Y set(Symbol symbol, boolean value) {
        this.contextAcc.set(symbol, value);
        return thisBuilder();
    }

    public X build() {
        Objects.requireNonNull(serviceURL, "No service URL");
        if ( updateEltAcc.isEmpty() )
            throw new UpdateException("No update for UpdateExecutionHTTP");

        HttpClient hClient = HttpEnv.getHttpClient(serviceURL, httpClient);

        // If all elements are objects then immediately build the UpdateRequest.
        UpdateRequest updateActual = updateEltAcc.isParsed() ? updateEltAcc.buildUpdateRequest() : null;

        if ( substitutionMap != null && ! substitutionMap.isEmpty() ) {
            // If updateActual is null it means that some elements are strings
            // and we need to parse those now.
            if ( updateActual == null ) {
                try {
                    updateActual = updateEltAcc.buildUpdateRequest();
                } catch (Exception e) {
                    throw new UpdateException("Substitution only supported for UpdateRequest objects. Failed to parse a given string as an UpdateRequest object.", e);
                }
            }
            updateActual = UpdateTransformOps.transform(updateActual, substitutionMap);
        }

        // If the UpdateRequest object wasn't built until now then build the string instead.
        String updateStringActual = updateActual == null ? updateEltAcc.buildString() : null;

        Context cxt = contextAcc.context();
        return buildX(hClient, updateActual, updateStringActual, cxt);
    }

    protected abstract X buildX(HttpClient hClient, UpdateRequest updateActual, String updateStringActual, Context cxt);
}
