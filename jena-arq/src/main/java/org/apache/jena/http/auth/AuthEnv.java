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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthEnv {
    public static Logger LOG =  LoggerFactory.getLogger(AuthEnv.class);

    private static AuthCredentials passwordRegistry = new AuthCredentials();

    /** Register (username, password) information for a URI endpoint. */
    public static void registerUsernamePassword(URI uri, String user, String password) {
        AuthDomain domain = new AuthDomain(uri, null);
        AuthEnv.passwordRegistry.put(domain, new PasswordRecord(user, password));
    }

    /** Register (username, password) information for a URI endpoint. */
    public static void unregisterUsernamePassword(URI uri) {
        AuthDomain location = new AuthDomain(uri, null);
        AuthEnv.passwordRegistry.remove(location);
    }

    /**
     * Return the (username, password) for a {@code URI}.
     * <p>
     * If there is no exact match for the URI, then the information mapped from the
     * longest prefix entry is returned.
     */
    public static PasswordRecord getUsernamePassword(URI uri) {
        AuthDomain domain = new AuthDomain(uri, null);
        return AuthEnv.passwordRegistry.get(domain);
    }

    public static void clearAuthRequestModifiers() {
        AuthLib.authModifiers.clear();
    }
}
