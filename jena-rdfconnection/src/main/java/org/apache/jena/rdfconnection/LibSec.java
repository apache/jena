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

package org.apache.jena.rdfconnection;

import java.util.function.Consumer;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.auth.DigestScheme;
import org.apache.http.impl.auth.RFC2617Scheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.protocol.HttpContext;
import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.atlas.web.AuthScheme;
import org.apache.jena.riot.web.HttpOp;
import org.apache.jena.web.AuthSetup;

/** Library for client side use of access control. */ 
public class LibSec {
    // See also DataAccessLib (package lib)
    
    // [AuthScheme] default
    public static AuthScheme authMode = AuthScheme.DIGEST;
    
    public static void withAuth(String urlStr, AuthSetup auth, Consumer<RDFConnection> action) {
        CredentialsProvider credsProvider = credsProvider(auth);
        HttpHost target = new HttpHost(auth.host, auth.port, "http");
        // --- AuthCache : not necessary
        // Create AuthCache instance - necessary for non-repeatable request entity. (i.e. streaming)
        
        // [AuthScheme]
        AuthCache authCache = new BasicAuthCache();
        if ( LibSec.authMode == AuthScheme.BASIC ) {
            RFC2617Scheme authScheme = authScheme(auth.realm);
            // Can force the client to use basic first time by setting authCache.
            // This does not work for digest because the nonce's will be wrong.
            authCache.put(target, authScheme);
        }
        
        HttpContext httpContext = httpContext(authCache, credsProvider);
        HttpClient httpClient = httpClient(auth); 
        
        // Needs retryable mods to RDFConnectionRemote??
        try ( RDFConnection conn = RDFConnectionRemote.create()
                .destination(urlStr)
                .httpClient(httpClient)
                .httpContext(httpContext)
                .build() ) {
            action.accept(conn);
        }
    }

    /** Create digest auth {@link DigestScheme} */
    private static RFC2617Scheme authScheme(String realm) {
        switch (authMode) {
            case BASIC: return authBasicScheme(realm);
            case DIGEST : return authDigestScheme(realm);
            default:
                throw new InternalErrorException("RFC2617 auth scheme not reocgnized: "+authMode);
        }
    }
    
    /** Create digest auth {@link DigestScheme} */
    private static DigestScheme authDigestScheme(String realm) {
        //Objects.requireNonNull(realm);
        DigestScheme authScheme = new DigestScheme();
        authScheme.overrideParamter("realm", realm);
        authScheme.overrideParamter("nonce", "whatever");
        return authScheme;
    }

    /** Create basic auth {@link BasicScheme} */
    private static BasicScheme authBasicScheme(String realm) {
        BasicScheme authScheme = new BasicScheme();
        return authScheme;
    }

    /**
     * Create an {@link HttpClient} with authentication as given by
     * the {@link AuthSetup} for a particular host and port.
     */
    public static HttpClient httpClient(AuthSetup auth) {
        // HttpClient with password.
        CredentialsProvider credsProvider = credsProvider(auth);
        HttpClient client = HttpOp.createPoolingHttpClientBuilder()
            .setDefaultCredentialsProvider(credsProvider)
            .build();
        return client;
    }
    
    /**
     * Create an {@link HttpClient} with authentication by user/password
     * a particular host and port.
     */
    public static HttpClient httpClient(String host, int port, String user, String password, String realm) {
        AuthSetup auth = new AuthSetup(host, port, user, password, realm);
        return httpClient(auth);
    }

    public static HttpClientContext httpContext(AuthCache authCache, CredentialsProvider provider) {
        // Add AuthCache to the execution context
        HttpClientContext localContext = HttpClientContext.create();
        return httpContext(localContext, authCache, provider);
    }

    public static HttpClientContext httpContext(HttpClientContext localContext, AuthCache authCache, CredentialsProvider provider) {
        // Add AuthCache to the execution context
        if ( authCache != null )
            localContext.setAuthCache(authCache);
        localContext.setCredentialsProvider(provider);
        return localContext;
    }

    public static CredentialsProvider credsProvider(AuthSetup auth) {
        return credsProvider(auth.host, auth.port, auth.user, auth.password);
    }
    
    private static CredentialsProvider credsProvider(String host, int port, String user, String password) {
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
            new AuthScope(host, port),
            new UsernamePasswordCredentials(user, password));
        return credsProvider;
    }
}
