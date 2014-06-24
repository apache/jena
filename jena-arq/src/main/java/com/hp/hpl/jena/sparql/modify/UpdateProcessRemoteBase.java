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

package com.hp.hpl.jena.sparql.modify;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.protocol.HttpContext;
import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.jena.atlas.web.auth.SimpleAuthenticator;
import org.apache.jena.riot.web.HttpOp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sparql.engine.http.HttpParams;
import com.hp.hpl.jena.sparql.engine.http.Params;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import com.hp.hpl.jena.sparql.engine.http.Service;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.sparql.util.Symbol;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;

/**
 * Abstract base class for update processors that perform remote updates over
 * HTTP
 * 
 */
public abstract class UpdateProcessRemoteBase implements UpdateProcessor {
    private static Logger log = LoggerFactory.getLogger(UpdateProcessRemoteBase.class);

    /**
     * Symbol used to set a {@link HttpContext} which will be used for HTTP
     * requests
     */
    public static final Symbol HTTP_CONTEXT = Symbol.create("httpContext");

    private final UpdateRequest request;
    private final String endpoint;
    private final Context context;
    private HttpAuthenticator authenticator;
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
        this.context = Context.setupContext(context, null);

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
     * {@code configureQuery()} method of the {@link Service} class though it
     * omits parameter merging since that will be done automatically whenever
     * the {@link QueryEngineHTTP} instance makes a query for remote submission.
     * </p>
     * 
     * @param serviceURI
     *            Service URI
     */
    private static void applyServiceConfig(String serviceURI, UpdateProcessRemoteBase engine) {
        @SuppressWarnings("unchecked")
        Map<String, Context> serviceContextMap = (Map<String, Context>) engine.context.get(Service.serviceContext);
        if (serviceContextMap != null && serviceContextMap.containsKey(serviceURI)) {
            Context serviceContext = serviceContextMap.get(serviceURI);
            if (log.isDebugEnabled())
                log.debug("Endpoint URI {} has SERVICE Context: {} ", serviceURI, serviceContext);

            // Apply authentication settings
            String user = serviceContext.getAsString(Service.queryAuthUser);
            String pwd = serviceContext.getAsString(Service.queryAuthPwd);

            if (user != null || pwd != null) {
                user = user == null ? "" : user;
                pwd = pwd == null ? "" : pwd;
                if (log.isDebugEnabled())
                    log.debug("Setting basic HTTP authentication for endpoint URI {} with username: {} ", serviceURI, user);

                engine.setAuthentication(user, pwd.toCharArray());
            }
        }
    }

    @Override
    public GraphStore getGraphStore() {
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
     * Gets the generated query string portion of the endpoint URL if applicable
     * <p>
     * Generated string will not include leading ? so that consuming code can
     * decide whether to add this themselves since the generated query string
     * may be being used in addition to an existing query string.
     * </p>
     * 
     * @return Generated query string
     */
    public String getQueryString() {
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
     * Sets authentication credentials for remote updates
     * <p>
     * May be better to use {@link #setAuthenticator(HttpAuthenticator)} as that
     * allows for more complex authentication to be used
     * </p>
     * 
     * @param username
     *            User name
     * @param password
     *            Password
     */
    public void setAuthentication(String username, char[] password) {
        this.setAuthenticator(new SimpleAuthenticator(username, password));
    }

    /**
     * Sets the authenticator to use
     * <p>
     * Note that you can globally set an authenticator via
     * {@link HttpOp#setDefaultAuthenticator(HttpAuthenticator)} to avoid the
     * need to set authentication on a per-request basis
     * </p>
     * 
     * @param authenticator
     *            HTTP Authenticator
     */
    public void setAuthenticator(HttpAuthenticator authenticator) {
        this.authenticator = authenticator;
    }

    /**
     * Gets the authenticator that has been set (if any)
     * <p>
     * If no authenticator is used then the default authenticator will be used,
     * this can be configured via the
     * {@link HttpOp#setDefaultAuthenticator(HttpAuthenticator)} method.
     * </p>
     * 
     * @return HTTP Authenticator if set, null otherwise
     */
    public HttpAuthenticator getAuthenticator() {
        return this.authenticator;
    }

    /**
     * Gets whether any authenticator has been set
     * 
     * @return True if an authenticator has been set, false otherwise
     */
    public boolean isUsingAuthentication() {
        return this.authenticator != null;
    }
}