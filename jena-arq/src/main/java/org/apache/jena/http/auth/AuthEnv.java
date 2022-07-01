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

package org.apache.jena.http.auth;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

import org.apache.jena.http.HttpLib;
import org.apache.jena.riot.web.HttpNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An authentication environment. Multiple {@code AuthEnv} would exists for a
 * multi-tenant environment. This is not currently supported but the use of
 * an object, obtained with {@link #get}, allows for that in teh future,
 */
public class AuthEnv {
    public static Logger LOG =  LoggerFactory.getLogger(AuthEnv.class);

    private AuthCredentials passwordRegistry = new AuthCredentials();
    // Challenge setups that are active.
    private Map<String, AuthRequestModifier> authModifiers = new ConcurrentHashMap<>();
    // Token fetcher for bearer authentication
    private BiFunction<String, AuthChallenge, String> tokenSupplier = null;

    private static AuthEnv singleton = new AuthEnv();

    /**
     * Get the AuthEnv appropriate to the caller.
     */
    public static AuthEnv get() { return singleton; }

    private AuthEnv() { }

    /** Register (username, password) information for a URI endpoint. */
    public void registerUsernamePassword(URI uri, String user, String password) {
        AuthDomain domain = new AuthDomain(uri);
        passwordRegistry.put(domain, new PasswordRecord(user, password));
    }

    /** Check whether there is a registration. */
    public boolean hasRegistation(URI uri) {
        AuthDomain location = new AuthDomain(uri);
        return passwordRegistry.contains(location);
    }

    /**
     * Remove the registration for a URI endpoint.
     */
    public void unregisterUsernamePassword(URI uri) {
        AuthDomain location = new AuthDomain(uri);
        passwordRegistry.remove(location);
        // ... and remove any active modifiers.
        // This must clear any potential AuthRequestModifier setup because the uri is
        // being used as a prefix for multiple endpoints.

        String uristr = uri.toString();
        List<String> removes = new ArrayList<>();
        for ( Entry<String, AuthRequestModifier> e : authModifiers.entrySet() ) {
            String key = e.getKey();
            if ( key.startsWith(uristr) )
                removes.add(key);
        }
        removes.forEach(k->authModifiers.remove(k));
    }

    /**
     * Return the (username, password) for a {@code URI}.
     * <p>
     * If there is no exact match for the URI, then the information mapped from the
     * longest prefix entry is returned.
     */
    public PasswordRecord getUsernamePassword(URI uri) {
        AuthDomain domain = new AuthDomain(uri);
        return passwordRegistry.get(domain);
    }

    public void clearActiveAuthentication() {
        authModifiers.clear();
    }

    /** Clear all registrations. */
    public void clearAuthEnv() {
        passwordRegistry.clearAll();
        authModifiers.clear();
    }

    /**
     * Add authentication, if in the authModifiers registry. That is, add the right
     * headers, and for digest, calculate the new digest string.
     */
    public HttpRequest.Builder addAuth(HttpRequest.Builder requestBuilder, String uri) {
        if ( authModifiers.isEmpty() )
            return requestBuilder;
        // Convert to the key for authentication handlers.
        String endpointURL = HttpLib.endpoint(uri);
        AuthRequestModifier mod = authModifiers.get(endpointURL);
        if ( mod == null )
            return requestBuilder;
        return mod.addAuth(requestBuilder);
    }

    /** Register the user/password which is to be used with basic auth at the given URL */
    public void registerBasicAuthModifier(String url, String user, String password) {
        AuthRequestModifier basicAuthModifier = AuthLib.basicAuthModifier(user, password);
        String serviceEndpoint = HttpLib.endpoint(url);
        authModifiers.put(serviceEndpoint, basicAuthModifier);
    }

    /** Register an AuthRequestModifier for a specific request target */
    public void registerAuthModifier(String requestTarget, AuthRequestModifier authModifier) {
        // Without query string or fragment.
        String serviceEndpoint = HttpLib.endpoint(requestTarget);
        //AuthEnv.LOG.info("Setup authentication for "+serviceEndpoint);
        authModifiers.put(serviceEndpoint, authModifier);
    }

    /** Remove any AuthRequestModifier for a specific request target */
    public void unregisterAuthModifier(String requestTarget) {
        String endpointURL = HttpLib.endpoint(requestTarget);
        AuthRequestModifier oldMod = authModifiers.remove(endpointURL);
    }

    /**
     * Set the creator of tokens for bearer authentication. The function must return
     * null (reject a 401 challenge) or a valid token including encoding (e.g.
     * Base64). It must not contain spaces. Requests will fail when the token becomes
     * out-of-date and the application will need to set a new token.
     * <p>
     * The string supplied will be used as-is with no further processing.
     * <p>
     * Supply a null argument for the tokenSupplier to clear any previous token
     * supplier.
     */
    public void setBearerTokenProvider(BiFunction<String, AuthChallenge, String> tokenSupplier) {
        this.tokenSupplier = tokenSupplier;
    }

    /**
     * Set the tokens for bearer authentication at an specific endpoint. This is
     * added to all requests sent to this same request target. Requests will fail
     * when the token becomes out-of-date and the application will need to set a new
     * token.
     * <p>
     * The string supplied will be used as-is with no further processing. Supply a
     * null argument to clear any previous token supplier.
     */
    public void setBearerToken(String requestTarget, String token) {
        if ( token == null ) {
            unregisterAuthModifier(requestTarget);
            return;
        }
        String endpointURL = HttpLib.endpoint(requestTarget);
        AuthRequestModifier authModifier = builder->builder.setHeader(HttpNames.hAuthorization, "Bearer "+token);
        registerAuthModifier(requestTarget, authModifier);
    }

    /**
     * Return a bearer auth token to use when responding to a 401 challenge. The
     * token must be in the form required for the "Authorization" header, including
     * encoding (typically base64 URL encoding with no padding). The string supplied is used
     * as-is with no further processing.
     * <p>
     * Return null for "no token" in which case a 401 response is passed back to the
     * application.
     */
    public String getBearerToken(String uri, AuthChallenge aHeader) {
        if ( tokenSupplier == null )
            return null;
        return tokenSupplier.apply(uri, aHeader);
    }

    // Development - do not provide in production systems.
  public void state() {
      org.apache.jena.atlas.io.IndentedWriter out = org.apache.jena.atlas.io.IndentedWriter.stdout.clone();
      out.setFlushOnNewline(true);

      out.println("Password Registry");
      out.incIndent();
      passwordRegistry.registered().forEach(ad->out.println(ad.getURI()));
      out.decIndent();

      out.println("Auth Modifiers");
      out.incIndent();
      authModifiers.forEach((String uriStr, AuthRequestModifier m)->{out.println(uriStr);});
      out.decIndent();
  }
}
