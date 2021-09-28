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
import java.net.http.HttpRequest.Builder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.jena.http.HttpLib;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthEnv {
    public static Logger LOG =  LoggerFactory.getLogger(AuthEnv.class);

    private AuthCredentials passwordRegistry = new AuthCredentials();
    // Challenge setups that are active.
    private Map<String, AuthRequestModifier> authModifiers = new ConcurrentHashMap<>();

    private static AuthEnv singleton = new AuthEnv();
    public static AuthEnv get() { return singleton; }

    private AuthEnv() { }

    /** Register (username, password) information for a URI endpoint. */
    public void registerUsernamePassword(URI uri, String user, String password) {
        AuthDomain domain = new AuthDomain(uri, null);
        passwordRegistry.put(domain, new PasswordRecord(user, password));
    }

    /** Check whether there is a registration. */
    public boolean hasRegistation(URI uri) {
        AuthDomain location = new AuthDomain(uri, null);
        return passwordRegistry.contains(location);
    }

    /** Register (username, password) information for a URI endpoint. */
    public void unregisterUsernamePassword(URI uri) {
        AuthDomain location = new AuthDomain(uri, null);
        passwordRegistry.remove(location);
        // and remove any active modifiers.
        authModifiers.remove(uri.toString());
    }

    /**
     * Return the (username, password) for a {@code URI}.
     * <p>
     * If there is no exact match for the URI, then the information mapped from the
     * longest prefix entry is returned.
     */
    public PasswordRecord getUsernamePassword(URI uri) {
        AuthDomain domain = new AuthDomain(uri, null);
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
    public Builder addAuth(Builder requestBuilder, String uri) {
        if ( authModifiers.isEmpty() )
            return requestBuilder;
        // Convert to the key for authentication handlers.
        String endpointURL = HttpLib.endpoint(uri);
        AuthRequestModifier mod = authModifiers.get(endpointURL);
        if ( mod == null )
            return requestBuilder;
        return mod.addAuth(requestBuilder);
    }

    /** Register the users/password which is to be used with basic auth at the given URL */
    public void registerBasicAuthModifier(String url, String user, String password) {
        AuthRequestModifier basicAuthModifier = AuthLib.basicAuthModifier(user, password);
        String serviceEndpoint = HttpLib.endpoint(url);
        authModifiers.put(serviceEndpoint, basicAuthModifier);
    }

    void registerAuthModifier(String requestTarget, AuthRequestModifier authModifier) {
        // Without query string or fragment.
        String serviceEndpoint = HttpLib.endpoint(requestTarget);
        //AuthEnv.LOG.info("Setup authentication for "+serviceEndpoint);
        authModifiers.put(serviceEndpoint, authModifier);
    }


    // Development - do not provide in production systems.
//    public void state() {
//        org.apache.jena.atlas.io.IndentedWriter out = org.apache.jena.atlas.io.IndentedWriter.stdout.clone();
//        out.setFlushOnNewline(true);
//
//        out.println("Password Registry");
//        out.incIndent();
//        passwordRegistry.registered().forEach(ad->out.println(ad.uri));
//        out.decIndent();
//
//        out.println("Auth Modifiers");
//        out.incIndent();
//        authModifiers.forEach((String uriStr, AuthRequestModifier m)->{out.println(uriStr);});
//        out.decIndent();
//    }
}
