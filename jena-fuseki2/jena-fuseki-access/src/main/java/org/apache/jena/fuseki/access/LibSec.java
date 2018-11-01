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

package org.apache.jena.fuseki.access;

import java.util.Objects;
import java.util.function.Consumer;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
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
import org.apache.jena.fuseki.jetty.AuthMode;
import org.apache.jena.fuseki.jetty.JettyLib;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.riot.web.HttpOp;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.UserStore;

/** Library for data access security */ 
public class LibSec {
    // See also DataAccessLib (package lib)
    
    /** Create a {@link FusekiServer} - setup with one dataset. The returned server has not been started. */
    public static FusekiServer fuseki(int port, String dsName, Dataset ds, AuthorizationService reg, UserStore userStore) {
        Objects.requireNonNull(dsName); 
        Objects.requireNonNull(ds); 
        Objects.requireNonNull(reg);
        Objects.requireNonNull(userStore);
        
        Dataset dsx = DataAccessCtl.controlledDataset(ds, reg);
        FusekiServer.Builder builder = DataAccessCtl.fusekiBuilder(DataAccessCtl.requestUserServlet)
            .port(port)
            .add(dsName, dsx, false);
        
        if ( dsName.startsWith("/") )
            dsName = dsName.substring(1);
        ConstraintSecurityHandler sh = JettyLib.makeSecurityHandler("Dataset:"+dsName, userStore);
        JettyLib.addPathConstraint(sh, "/"+dsName);
        builder.securityHandler(sh);
        return builder.build();
    }

    // HttpClientLibSec.
    
    // [AuthScheme] default
    public static AuthMode authMode = AuthMode.DIGEST;
    
    public static void withAuth(String urlStr, AuthSetup auth, Consumer<RDFConnection> action) {
        CredentialsProvider credsProvider = credsProvider(auth);
        HttpHost target = new HttpHost(auth.host, auth.port, "http");
        // --- AuthCache : not necessary
        // Create AuthCache instance - necessary for non-repeatable request entity. (i.e. streaming)
        
        // [AuthScheme]
        AuthCache authCache = new BasicAuthCache();
        if ( LibSec.authMode == AuthMode.BASIC ) {
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
    public static DigestScheme authDigestScheme(String realm) {
        //Objects.requireNonNull(realm);
        DigestScheme authScheme = new DigestScheme();
        authScheme.overrideParamter("realm", realm);
        authScheme.overrideParamter("nonce", "whatever");
        return authScheme;
    }

    /** Create basic auth {@link BasicScheme} */
    public static BasicScheme authBasicScheme(String realm) {
        BasicScheme authScheme = new BasicScheme();
        return authScheme;
    }

    public static HttpClient httpClient(AuthSetup auth) {
        // HttpClient with password.
        CredentialsProvider credsProvider = credsProvider(auth);
        Credentials credentials = new UsernamePasswordCredentials(auth.user, auth.password);
        
        String schemeAuthScope = authMode == AuthMode.BASIC ? "basic" : "digest";  
        AuthScope authScope = new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM, schemeAuthScope);
        //AuthScope authScope = AuthScope.ANY;
        
        credsProvider.setCredentials(AuthScope.ANY, credentials);
        HttpClient client = HttpOp.createPoolingHttpClientBuilder()
            .setDefaultCredentialsProvider(credsProvider)
            .build();
        return client;
    }
    
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
