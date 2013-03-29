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

import java.util.Map;

import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import com.hp.hpl.jena.sparql.engine.http.Service;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.sparql.util.Symbol;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;

/**
 * Abstract base class for update processors that perform remote updates over HTTP
 *
 */
public abstract class UpdateProcessRemoteBase implements UpdateProcessor {
    private static Logger log = LoggerFactory.getLogger(UpdateProcessRemoteBase.class) ;
    
    /**
     * Symbol used to set a {@link HttpContext} which will be used for HTTP requests
     */
    public static final Symbol HTTP_CONTEXT = Symbol.create("httpContext");
    
    private final UpdateRequest request ;
    private final String endpoint ;
    private final Context context ;

    /**
     * Creates a new remote update processor
     * @param request Update request
     * @param endpoint Update endpoint
     * @param context Context
     */
    public UpdateProcessRemoteBase(UpdateRequest request, String endpoint, Context context) {
        super();
        
        this.request = request;
        this.endpoint = endpoint ;
        this.context = Context.setupContext(context, null) ;
        
        // Apply service configuration if applicable
        UpdateProcessRemoteBase.applyServiceConfig(endpoint, this);
    }
    
    /**
     * <p>
     * Helper method which applies configuration from the Context to the query engine
     * if a service context exists for the given URI
     * </p>
     * <p>
     * Based off proposed patch for JENA-405 but modified to apply all relevant configuration, this is in part also
     * based off of the private {@code configureQuery()} method of the {@link Service} class though it omits
     * parameter merging since that will be done automatically whenever the {@link QueryEngineHTTP} instance
     * makes a query for remote submission.
     * </p>
     * @param serviceURI Service URI
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
                
                HttpContext httpContext = engine.getHttpContext();
                if (httpContext == null) {
                    engine.setHttpContext(new BasicHttpContext());
                    httpContext = engine.getHttpContext();
                }
                httpContext.setAttribute(Service.queryAuthUser.toString(), user);
                httpContext.setAttribute(Service.queryAuthPwd.toString(), pwd);
            }
        }
    }
    
    @Override
    public GraphStore getGraphStore()
    {
        return null ;
    }
    
    /**
     * Gets the endpoint
     * @return Endpoint URI
     */
    public String getEndpoint() {
        return this.endpoint;
    }
    
    /**
     * Gets the update request
     * @return Update request
     */
    public UpdateRequest getUpdateRequest() {
        return this.request;
    }

    /**
     * Convenience method to set the {@link HttpContext}
     * @param httpContext
     */
    public void setHttpContext(HttpContext httpContext) {
        getContext().put(HTTP_CONTEXT, httpContext) ;
    }

    /**
     * Convenience method to get the {@link HttpContext}
     * @return HttpContext
     */
    public HttpContext getHttpContext() {
        return (HttpContext) getContext().get(HTTP_CONTEXT);
    }

    @Override
    public Context getContext()
    {
        return context ;
    }
    
    /**
     * Sets authentication credentials for remote updates
     * @param username Username
     * @param password Password
     */
    public void setAuthentication(String username, char[] password) {
        HttpContext httpContext = this.getHttpContext();
        if (httpContext == null) {
            this.setHttpContext(new BasicHttpContext());
            httpContext = this.getHttpContext();
        }
        httpContext.setAttribute(Service.queryAuthUser.toString(), username);
        httpContext.setAttribute(Service.queryAuthPwd.toString(), new String(password));
    }
    
    /**
     * Gets whether any authentication credentials have been set
     * @return True if credentials have been set, false otherwise
     */
    public boolean isUsingAuthentication() {
        HttpContext httpContext = this.getHttpContext();
        if (httpContext == null) return false;
        return httpContext.getAttribute(Service.queryAuthUser.toString()) != null && httpContext.getAttribute(Service.queryAuthPwd.toString()) != null;
    }
}