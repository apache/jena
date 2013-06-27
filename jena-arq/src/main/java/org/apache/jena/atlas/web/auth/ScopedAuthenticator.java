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
import java.util.HashMap;
import java.util.Map;

import org.apache.jena.atlas.lib.Pair;

/**
 * A credentials based authenticator where credentials are scoped to URIs. This
 * allows for a single authenticator to present different credentials to
 * different URIs as appropriate.
 * 
 */
public class ScopedAuthenticator extends AbstractScopedAuthenticator {

    private Map<URI, Pair<String, char[]>> credentials = new HashMap<URI, Pair<String, char[]>>();

    /**
     * Creates an authenticator with credentials for the given URI
     * @param target URI
     * @param username User name
     * @param password Password
     */
    public ScopedAuthenticator(URI target, String username, char[] password) {
        if (target == null) throw new IllegalArgumentException("Target URI cannot be null");
        this.credentials.put(target, Pair.create(username, password));
    }

    /**
     * Creates an authenticator with a set of credentials for URIs
     * @param credentials Credentials
     */
    public ScopedAuthenticator(Map<URI, Pair<String, char[]>> credentials) {
        this.credentials.putAll(credentials);
    }
    
    /**
     * Adds/Overwrites credentials for a given URI
     * @param target Target
     * @param username User name
     * @param password Password
     */
    public void addCredentials(URI target, String username, char[] password) {
        if (target == null) throw new IllegalArgumentException("Target URI cannot be null");
        this.credentials.put(target, Pair.create(username, password));
    }

    @Override
    protected String getUserName(URI target) {
        Pair<String, char[]> p = this.credentials.get(target);
        return p != null ? p.getLeft() : null;
    }

    @Override
    protected char[] getPassword(URI target) {
        Pair<String, char[]> p = this.credentials.get(target);
        return p != null ? p.getRight() : null;
    }

}
