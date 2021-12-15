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

package org.apache.jena.jdbc.remote.http;

import java.util.ArrayList ;
import java.util.List ;

import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.engine.http.HttpParams ;
import org.apache.jena.sparql.engine.http.Params ;
import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.sparql.exec.http.Service;
import org.apache.jena.sparql.util.Context ;
import org.apache.jena.sparql.util.Symbol ;
import org.apache.jena.update.UpdateRequest ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

/**
 * Abstract base class for update processors that perform remote updates over
 * HTTP
 * @deprecated Use {@code UpdateExecutionHTTP} created with {@code UpdateExecutionHTTPBuilder}.
 */
@Deprecated
public abstract class UpdateProcessRemoteBase implements UpdateExec {
    private static Logger log = LoggerFactory.getLogger(UpdateProcessRemoteBase.class);

    /**
     * Symbol used to set a {@link HttpContext} which will be used for HTTP
     * requests
     */
    public static final Symbol HTTP_CONTEXT = Symbol.create("httpContext");

    private final UpdateRequest request;
    private final String endpoint;
    private final Context context;
    private HttpClient client;
    private Params params;

    protected List<String> defaultGraphURIs = new ArrayList<>();
    protected List<String> namedGraphURIs = new ArrayList<>();

    /**
     * Creates a new remote update processor
     *
     * @param request
     *            Update request
     * @param endpoint
     *            Update endpoint
     * @param context
     *            Context
     */
    public UpdateProcessRemoteBase(UpdateRequest request, String endpoint, Context context) {
        super();

        this.request = request;
        this.endpoint = endpoint;
        this.context = Context.setupContextForDataset(context, null);

        // Apply service configuration if applicable
        UpdateProcessRemoteBase.applyServiceConfig(endpoint, this);
    }

    /**
     * <p>
     * Helper method which applies configuration from the Context to the query
     * engine if a service context exists for the given URI
     * </p>
     * <p>
     * Based off proposed patch for JENA-405 but modified to apply all relevant
     * configuration, this is in part also based off of the private
     * {@code configureQuery()} method of the {@link Service_AHC} class though it
     * omits parameter merging since that will be done automatically whenever
     * the {@link QueryEngineHTTP} instance makes a query for remote submission.
     * </p>
     *
     * @param serviceURI
     *            Service URI
     */
    private static void applyServiceConfig(String serviceURI, UpdateProcessRemoteBase engine) {
        if ( engine.context.isDefined(Service.oldServiceContext) )
            System.err.println("************ UpdateProcessRemoteBase.applyServiceConfig NOT IMPLEMENTED *************");

//        @SuppressWarnings("unchecked")
//        Map<String, Context> serviceContextMap = (Map<String, Context>) engine.context.get(Service_AHC.serviceContext);
//        if (serviceContextMap != null && serviceContextMap.containsKey(serviceURI)) {
//            Context serviceContext = serviceContextMap.get(serviceURI);
//            if (log.isDebugEnabled())
//                log.debug("Endpoint URI {} has SERVICE Context: {} ", serviceURI, serviceContext);
//
//            // Apply client settings
//            HttpClient client = serviceContext.get(Service_AHC.queryClient);
//
//            if (client != null) {
//                if (log.isDebugEnabled())
//                    log.debug("Using context-supplied client for endpoint URI {}", serviceURI);
//                engine.setClient(client);
//            }
//        }
    }

    @Override
    public DatasetGraph getDatasetGraph() {
        return null;
    }

    /**
     * Gets the endpoint
     *
     * @return Endpoint URI
     */
    public String getEndpoint() {
        return this.endpoint;
    }

    /**
     * Gets the generated HTTP query string portion of the endpoint URL if applicable
     * <p>
     * Generated string will not include leading ? so that consuming code can
     * decide whether to add this themselves since the generated query string
     * may be being used in addition to an existing query string.
     * </p>
     *
     * @return Generated query string
     */
    public String getUpdateString() {
        return this.getParams().httpString();
    }

    /**
     * Gets the parameters for the execution
     *
     * @return Parameters
     */
    public Params getParams() {
        Params ps = this.params != null ? new Params(this.params) : new Params();
        if (this.defaultGraphURIs != null) {
            for (String defaultGraph : this.defaultGraphURIs) {
                ps.addParam(HttpParams.pUsingGraph, defaultGraph);
            }
        }
        if (this.namedGraphURIs != null) {
            for (String namedGraph : this.namedGraphURIs) {
                ps.addParam(HttpParams.pUsingNamedGraph, namedGraph);
            }
        }
        return ps;
    }

    /**
     * Gets the update request
     *
     * @return Update request
     */
    public UpdateRequest getUpdateRequest() {
        return this.request;
    }

    /**
     * Adds a default graph
     *
     * @param defaultGraph
     *            Default Graph URI
     */
    public void addDefaultGraph(String defaultGraph) {
        if (this.defaultGraphURIs == null) {
            this.defaultGraphURIs = new ArrayList<>();
        }
        this.defaultGraphURIs.add(defaultGraph);
    }

    /**
     * Adds a named graph
     *
     * @param namedGraph
     *            Named Graph URi
     */
    public void addNamedGraph(String namedGraph) {
        if (this.namedGraphURIs == null) {
            this.namedGraphURIs = new ArrayList<>();
        }
        this.namedGraphURIs.add(namedGraph);
    }

    /**
     * Adds a custom parameter to the request
     *
     * @param field
     *            Field
     * @param value
     *            Value
     */
    public void addParam(String field, String value) {
        if (this.params == null)
            this.params = new Params();
        this.params.addParam(field, value);
    }

    /**
     * Sets the default graphs
     *
     * @param defaultGraphs
     *            Default Graphs
     */
    public void setDefaultGraphs(List<String> defaultGraphs) {
        this.defaultGraphURIs = defaultGraphs;
    }

    /**
     * Sets the named graphs
     *
     * @param namedGraphs
     *            Named Graphs
     */
    public void setNamedGraphs(List<String> namedGraphs) {
        this.namedGraphURIs = namedGraphs;
    }

    /**
     * Convenience method to set the {@link HttpContext}
     *
     * @param httpContext
     *            HTTP Context
     */
    public void setHttpContext(HttpContext httpContext) {
        getContext().put(HTTP_CONTEXT, httpContext);
    }

    /**
     * Convenience method to get the {@link HttpContext}
     *
     * @return HttpContext
     */
    public HttpContext getHttpContext() {
        return (HttpContext) getContext().get(HTTP_CONTEXT);
    }

    @Override
    public Context getContext() {
        return context;
    }

    /**
     * Sets the client to use
     * <p>
     * Note that you can globally set an client via
     * {@link HttpOp1#setDefaultHttpClient(HttpClient)} to avoid the
     * need to set client on a per-request basis
     * </p>
     *
     * @param client
     *            HTTP client
     */
    public void setClient(HttpClient client) {
        this.client = client;
    }

    /**
     * Gets the client that has been set (if any)
     * <p>
     * If no client is used then the default client will be used,
     * this can be configured via the
     * {@link HttpOp1#setDefaultHttpClient(HttpClient)} method.
     * </p>
     *
     * @return HTTP client if set, null otherwise
     */
    public HttpClient getClient() {
        return this.client;
    }
}
