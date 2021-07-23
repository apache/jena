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

import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.graph.Node;
import org.apache.jena.http.HttpEnv;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.QueryException;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.http.Params;
import org.apache.jena.sparql.exec.http.UpdateSendMode;
import org.apache.jena.sparql.syntax.syntaxtransform.UpdateTransformOps;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateException;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;

public abstract class ExecUpdateHTTPBuilder<X, Y> {

    static { JenaSystem.init(); }

    protected String serviceURL;
    protected String updateString;
    private UpdateRequest updateOperations = null;
    protected Params params = Params.create();
    protected boolean allowCompression;
    protected Map<String, String> httpHeaders = new HashMap<>();
    protected HttpClient httpClient;
    protected UpdateSendMode sendMode = UpdateSendMode.systemDefault;
    protected List<String> usingGraphURIs = null;
    protected List<String> usingNamedGraphURIs = null;
    protected Context context = null;
    // Uses query rewrite to replace variables by values.
    protected Map<Var, Node> substitutionMap     = new HashMap<>();


    protected ExecUpdateHTTPBuilder() {}

    protected abstract Y thisBuilder();

    public Y endpoint(String serviceURL) {
        this.serviceURL = serviceURL;
        return thisBuilder();
    }

    public Y update(UpdateRequest updateRequest) {
        Objects.requireNonNull(updateRequest);
        ensureUpdateRequest();
        updateRequest.getOperations().forEach(this::update);
        this.updateString = null;
        return thisBuilder();
    }

    public Y update(String updateRequestString) {
        ensureUpdateRequest();
        UpdateRequest more = UpdateFactory.create(updateRequestString);
        more.getOperations().forEach(this::update);
        this.updateString = null;
        return thisBuilder();
    }

    /** Add the update. */
    public Y update(Update update) {
        Objects.requireNonNull(update);
        ensureUpdateRequest();
        this.updateOperations.add(update);
        this.updateString = null;
        return thisBuilder();
    }

    /**
     * Set the update - this replaces any previous updates added. The update string
     * is used as given including nonstandard syntax features offered by the remote
     * SPARQL system.
     */
    public Y updateString(String updateString) {
        Objects.requireNonNull(updateString);
        this.updateOperations = null;
        this.updateString = updateString;
        return thisBuilder();
    }

    public Y substitution(Binding binding) {
        binding.forEach(this.substitutionMap::put);
        return thisBuilder();
    }

    public Y substitution(Var var, Node value) {
        this.substitutionMap.put(var, value);
        return thisBuilder();
    }

    public Y httpClient(HttpClient httpClient) {
        this.httpClient = Objects.requireNonNull(httpClient);
        return thisBuilder();
    }

//    /**
//     * Whether to send the update request using POST and an HTML form, content type
//     * "application/x-www-form-urlencoded".
//     *
//     * If false (the default), send as "application/sparql-query" (default).
//     */
//    public UpdateExecutionHTTPBuilder sendHtmlForm(boolean htmlForm) {
//        this.sendMode =  htmlForm ? UpdateSendMode.asPostForm : UpdateSendMode.asPostBody;
//        return thisBuilder();
//    }

    /**
     * Choose whether to send using POST as "application/sparql-update" (preferred) or
     * as an HTML form, content type "application/x-www-form-urlencoded".
     */
    public Y sendMode(UpdateSendMode mode) {
        this.sendMode = mode;
        return thisBuilder();
    }


    // The old code, UpdateProcessRemote, didn't support this so may be not
    // provide it as its not being used.

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

    private void ensureUpdateRequest() {
        if ( updateOperations == null )
            updateOperations = new UpdateRequest();
    }

    public X build() {
        Objects.requireNonNull(serviceURL, "No service URL");
        if ( updateOperations == null && updateString == null )
            throw new QueryException("No update for UpdateExecutionHTTP");
        if ( updateOperations != null && updateString != null )
            throw new InternalErrorException("UpdateRequest and update string");
        HttpClient hClient = HttpEnv.getHttpClient(serviceURL, httpClient);

        UpdateRequest updateActual = updateOperations;
        if ( substitutionMap != null && ! substitutionMap.isEmpty() ) {
            if ( updateActual == null )
                throw new UpdateException("Substitution only supported if an UpdateRequest object was provided");
            updateActual = UpdateTransformOps.transform(updateActual, substitutionMap);
        }
        Context cxt = (context!=null) ? context : ARQ.getContext().copy();
        return buildX(hClient, updateActual, cxt);
    }

    protected abstract X buildX(HttpClient hClient, UpdateRequest updateActual, Context cxt);
}
