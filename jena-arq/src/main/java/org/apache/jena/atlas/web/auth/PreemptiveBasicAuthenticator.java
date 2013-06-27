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
package org.apache.jena.atlas.web.auth;

import java.net.URI;

import org.apache.http.HttpHost;
import org.apache.http.client.AuthCache;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.protocol.HttpContext;

/**
 * A decorator for other authenticators that may be used to enable preemptive
 * basic authentication. Note that preemptive basic authentication is less
 * secure because it can expose credentials to servers that do not require them.
 * 
 */
public class PreemptiveBasicAuthenticator implements HttpAuthenticator {
    
    private HttpAuthenticator authenticator;
    
    /**
     * Creates a new decorator over the given authenticator
     * @param authenticator Authenticator to decorate
     */
    public PreemptiveBasicAuthenticator(HttpAuthenticator authenticator) {
        if (authenticator == null) throw new IllegalArgumentException("Must provide an authenticator to decorate");
        this.authenticator = authenticator;
    }

    @Override
    public void apply(AbstractHttpClient client, HttpContext httpContext, URI target) {
        this.authenticator.apply(client, httpContext, target);
        
        // Enable preemptive basic authentication
        AuthCache authCache = new BasicAuthCache();
        BasicScheme basicAuth = new BasicScheme();
        authCache.put(new HttpHost(target.getHost(), target.getPort()), basicAuth);
        httpContext.setAttribute(ClientContext.AUTH_CACHE, authCache);   
    }

}
