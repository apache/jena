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

import static org.apache.jena.http.HttpLib.*;

import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.jena.atlas.logging.Log;
import org.apache.jena.http.HttpEnv;
import org.apache.jena.http.HttpLib;
import org.apache.jena.riot.WebContent;
import org.apache.jena.riot.web.HttpNames;
import org.apache.jena.sparql.engine.http.HttpParams;
import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.update.UpdateRequest;

public class UpdateExecHTTP implements UpdateExec {

    public static UpdateExecHTTPBuilder newBuilder() {
        return UpdateExecHTTPBuilder.create();
    }

    public static UpdateExecHTTPBuilder service(String endpointURL) {
        return UpdateExecHTTPBuilder.create().endpoint(endpointURL);
    }

    private final Context context;
    private final String service;

    // UpdateRequest as an object - may be null.
    private final UpdateRequest update;
    private final String updateString;
    private final Map<String, String> httpHeaders;
    private final HttpClient httpClient;
    private final UpdateSendMode sendMode;
    private final Params params;
    private final List<String> usingGraphURIs;
    private final List<String> usingNamedGraphURIs;
    private final long timeout;
    private final TimeUnit timeoutUnit;

    private AtomicBoolean cancelSignal = new AtomicBoolean(false);
    private volatile InputStream retainedConnection = null;

    /*package*/ UpdateExecHTTP(String serviceURL, UpdateRequest update, String updateString,
                               HttpClient httpClient, Params params,
                               List<String> usingGraphURIs,
                               List<String> usingNamedGraphURIs,
                               Map<String, String> httpHeaders, UpdateSendMode sendMode,
                               Context context,
                               long timeout, TimeUnit timeoutUnit) {
        this.context = context;
        this.service = serviceURL;
        this.update = update;
        // Builder ensures one or the other is set.
        this.updateString = ( updateString != null ) ? updateString : update.toString();
        this.httpClient = dft(httpClient, HttpEnv.getDftHttpClient());
        this.params = params;
        this.usingGraphURIs = usingGraphURIs;
        this.usingNamedGraphURIs = usingNamedGraphURIs;
        this.httpHeaders = httpHeaders;
        this.sendMode = sendMode;
        this.timeout = timeout;
        this.timeoutUnit = timeoutUnit;
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public UpdateRequest getUpdateRequest() {
        return update;
    }

    @Override
    public String getUpdateRequestString() {
        return updateString;
    }

    @Override
    public void execute() {
        Params thisParams = Params.create(params);
        if ( usingGraphURIs != null ) {
            for ( String uri : usingGraphURIs )
                thisParams.add(HttpNames.paramUsingGraphURI, uri);
        }
        if ( usingNamedGraphURIs != null ) {
            for ( String uri : usingNamedGraphURIs )
                thisParams.add(HttpNames.paramUsingNamedGraphURI, uri);
        }

        modifyByService(service, context, thisParams, httpHeaders);

        switch(sendMode) {
            case asPost :
                executePostBody(thisParams); break;
            case asPostForm :
                executePostForm(thisParams); break;
        }
    }

    private void executePostBody(Params thisParams) {
        String str = updateString;
        String requestURL = service;
        if ( thisParams.count() > 0 ) {
            String qs = thisParams.httpString();
            requestURL = requestURL(requestURL, qs);
        }
        executeUpdate(requestURL, BodyPublishers.ofString(str), WebContent.contentTypeSPARQLUpdate);
    }

    private void executePostForm(Params thisParams) {
        String requestURL = service;
        thisParams.add(HttpParams.pUpdate, updateString);
        String formString = thisParams.httpString();
        // Everything goes into the form body, no use of the request URI query string.
        executeUpdate(requestURL, BodyPublishers.ofString(formString, StandardCharsets.US_ASCII), WebContent.contentTypeHTMLForm);
    }

    private String executeUpdate(String requestURL, BodyPublisher body, String contentType) {
        HttpRequest.Builder builder = HttpLib.requestBuilder(requestURL, httpHeaders, timeout, timeoutUnit);
        builder = contentTypeHeader(builder, contentType);
        HttpRequest request = builder.POST(body).build();
        logUpdate(updateString, request);
        HttpResponse<InputStream> response = HttpLib.execute(httpClient, request);
        // Consumes and closes the input stream.
        return HttpLib.handleResponseRtnString(response, this::setRetainedConnection);
    }

    // abort() may be called while waiting for the remote update to complete.
    // Capture the input stream (from HttpLib.handleResponseRtnString)
    private void setRetainedConnection(InputStream in) {
        synchronized (cancelSignal) {
            retainedConnection = in;
            if (cancelSignal.get()) {
                abort();
            }
        }
    }

    private static void logUpdate(String updateString, HttpRequest request) {}

    /**
     * Best effort that tries to close an underlying HTTP connection.
     * May still hang waiting for the HTTP request to complete.
     */
    @Override
    public void abort() {
        cancelSignal.set(true);
        synchronized (cancelSignal) {
            try {
                InputStream in = retainedConnection;
                if (in != null) {
                    HttpLib.finishInputStream(in);
                    retainedConnection = null;
                }
            } catch (Exception ex) {
                Log.warn(this, "Error during abort", ex);
            }
        }
    }
}
