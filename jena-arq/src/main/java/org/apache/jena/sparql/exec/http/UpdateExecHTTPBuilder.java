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
import java.util.*;

import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.http.HttpEnv;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.QueryException;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;

public class UpdateExecHTTPBuilder {

    static { JenaSystem.init(); }

    private String serviceURL;
    private String updateString;
    private Params params = Params.create();
    private boolean allowCompression;
    private Map<String, String> httpHeaders = new HashMap<>();
    private HttpClient httpClient;
    private UpdateSendMode sendMode = UpdateSendMode.systemtDefault;
    private UpdateRequest updateOperations = null;
    private List<String> usingGraphURIs = null;
    private List<String> usingNamedGraphURIs = null;
    private Context context = null;

    public UpdateExecHTTPBuilder() {}

    public UpdateExecHTTPBuilder service(String serviceURL) {
        this.serviceURL = serviceURL;
        return this;
    }

    public UpdateExecHTTPBuilder update(UpdateRequest updateRequest) {
        Objects.requireNonNull(updateRequest);
        ensureUpdateRequest();
        updateRequest.getOperations().forEach(this::update);
        this.updateString = null;
        return this;
    }

    public UpdateExecHTTPBuilder update(String updateRequestString) {
        ensureUpdateRequest();
        UpdateRequest more = UpdateFactory.create(updateRequestString);
        more.getOperations().forEach(this::update);
        this.updateString = null;
        return this;
    }

    /** Add the update. */
    public UpdateExecHTTPBuilder update(Update update) {
        Objects.requireNonNull(update);
        ensureUpdateRequest();
        this.updateOperations.add(update);
        this.updateString = null;
        return this;
    }

    /**
     * Set the update - this replaces any previous updates added. The update string
     * is used as given including nonstandard syntax features offered by the remote
     * SPARQL system.
     */
    public UpdateExecHTTPBuilder updateString(String updateString) {
        Objects.requireNonNull(updateString);
        this.updateOperations = null;
        this.updateString = updateString;
        return this;
    }

    public UpdateExecHTTPBuilder httpClient(HttpClient httpClient) {
        this.httpClient = Objects.requireNonNull(httpClient);
        return this;
    }

//    /**
//     * Whether to send the update request using POST and an HTML form, content type
//     * "application/x-www-form-urlencoded".
//     *
//     * If false (the default), send as "application/sparql-query" (default).
//     */
//    public UpdateExecutionHTTPBuilder sendHtmlForm(boolean htmlForm) {
//        this.sendMode =  htmlForm ? UpdateSendMode.asPostForm : UpdateSendMode.asPostBody;
//        return this;
//    }

    /**
     * Choose whether to send using POST as "application/sparql-update" (preferred) or
     * as an HTML form, content type "application/x-www-form-urlencoded".
     */
    public UpdateExecHTTPBuilder sendMode(UpdateSendMode mode) {
        this.sendMode = mode;
        return this;
    }


    // The old code, UpdateProcessRemote, didn't support this so may be not
    // provide it as its not being used.

    public UpdateExecHTTPBuilder addUsingGraphURI(String uri) {
        if (this.usingGraphURIs == null)
            this.usingGraphURIs = new ArrayList<>();
        this.usingGraphURIs.add(uri);
        return this;
    }

    public UpdateExecHTTPBuilder addUsingNamedGraphURI(String uri) {
        if (this.usingNamedGraphURIs == null)
            this.usingNamedGraphURIs = new ArrayList<>();
        this.usingNamedGraphURIs.add(uri);
        return this;
    }

    public UpdateExecHTTPBuilder param(String name) {
        Objects.requireNonNull(name);
        this.params.add(name);
        return this;
    }

    public UpdateExecHTTPBuilder param(String name, String value) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(value);
        this.params.add(name, value);
        return this;
    }

    public UpdateExecHTTPBuilder httpHeader(String headerName, String headerValue) {
        Objects.requireNonNull(headerName);
        Objects.requireNonNull(headerValue);
        this.httpHeaders.put(headerName, headerValue);
        return this;
    }

    /** Set the {@link Context}.
     *  This defaults to the global settings of {@code ARQ.getContext()}.
     *  If there was a previous call of {@code context} the multiple contexts are merged.
     * */
    public UpdateExecHTTPBuilder context(Context context) {
        if ( context == null )
            return this;
        ensureContext();
        this.context.putAll(context);
        return this;
    }

    private void ensureContext() {
        if ( context == null )
            context = new Context();
    }

    private void ensureUpdateRequest() {
        if ( updateOperations == null )
            updateOperations = new UpdateRequest();
    }

    public UpdateExecHTTP build() {
        Objects.requireNonNull(serviceURL, "No service URL");
        if ( updateOperations == null && updateString == null )
            throw new QueryException("No update for UpdateExecutionHTTP");
        if ( updateOperations != null && updateString != null )
            throw new InternalErrorException("UpdateRequest and update string");
        HttpClient hClient = HttpEnv.getHttpClient(serviceURL, httpClient);
        Context cxt = (context!=null) ? context : ARQ.getContext().copy();
        return new UpdateExecHTTP(serviceURL, updateOperations, updateString, hClient, params,
                                  copyArray(usingGraphURIs),
                                  copyArray(usingNamedGraphURIs),
                                  new HashMap<>(httpHeaders),
                                  sendMode, cxt);
    }
}
