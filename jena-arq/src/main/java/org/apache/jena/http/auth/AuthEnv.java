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
import java.util.List;
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

    private AuthEnv() {
        //super(Function.identity());
    }

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
        // [QExec] authModifiers Move to AuthEnv.
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

    // Clear the setup active registrations.
    public void clearAuthEnv() {
        List<AuthDomain> items = passwordRegistry.registered();
        items.forEach(ad->{
            passwordRegistry.remove(ad);
            authModifiers.remove(ad.getURI().toString());
        });
    }

    // [QExec] Clean up. Pass in URI?
    public Builder addAuth(Builder requestBuilder, String uri) {
        if ( authModifiers.isEmpty() )
            return requestBuilder;
        // Covert to the key for authentication handlers.
        String endpointURL = HttpLib.endpoint(uri);
        AuthRequestModifier mod = authModifiers.get(endpointURL);
        if ( mod == null )
            return requestBuilder;
        return mod.addAuth(requestBuilder);
    }

    void registerAuthModifier(String requestTarget, AuthRequestModifier digestAuthModifier) {
        // Without query string or fragment.
        String serviceEndpoint = HttpLib.endpoint(requestTarget);
        //AuthEnv.LOG.info("Setup authentication for "+serviceEndpoint);
        authModifiers.put(serviceEndpoint, digestAuthModifier);
    }

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
