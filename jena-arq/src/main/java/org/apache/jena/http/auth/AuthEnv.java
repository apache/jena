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

    private static AuthCredentials passwordRegistry = new AuthCredentials();

    // Challenge setups
    /*package*/ static Map<String, AuthRequestModifier> authModifiers = new ConcurrentHashMap<>();

    /** Register (username, password) information for a URI endpoint. */
    public static void registerUsernamePassword(URI uri, String user, String password) {
        AuthDomain domain = new AuthDomain(uri, null);
        AuthEnv.passwordRegistry.put(domain, new PasswordRecord(user, password));
    }

    /** Check whether there is a registration. */
    public static boolean hasRegistation(URI uri) {
        AuthDomain location = new AuthDomain(uri, null);
        return AuthEnv.passwordRegistry.contains(location);
    }

    /** Register (username, password) information for a URI endpoint. */
    public static void unregisterUsernamePassword(URI uri) {
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
    public static PasswordRecord getUsernamePassword(URI uri) {
        AuthDomain domain = new AuthDomain(uri, null);
        return passwordRegistry.get(domain);
    }

    // Clear the setup active registrations.
    public static void clearPasswordRegistry() {
        List<AuthDomain> items = passwordRegistry.registered();
        items.forEach(ad->{
            passwordRegistry.remove(ad);
            authModifiers.remove(ad.getURI().toString());
        });
    }

    // [QExec] Clean up. Pass in URI?
    public static Builder addAuth(Builder requestBuilder, String uri) {
        if ( authModifiers.isEmpty() )
            return requestBuilder;
        // Covert to the key for authentication handlers.
        String endpointURL = HttpLib.endpoint(uri);
        AuthRequestModifier mod = authModifiers.get(endpointURL);
        if ( mod == null )
            return requestBuilder;
        return mod.addAuth(requestBuilder);
    }

    static void registerAuthModifier(String requestTarget, AuthRequestModifier digestAuthModifier) {
        // Without query string or fragment.
        String serviceEndpoint = HttpLib.endpoint(requestTarget);
        //AuthEnv.LOG.info("Setup authentication for "+serviceEndpoint);
        authModifiers.put(serviceEndpoint, digestAuthModifier);
    }
}
