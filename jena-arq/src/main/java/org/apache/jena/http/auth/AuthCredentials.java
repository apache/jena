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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.jena.atlas.lib.Trie;
import org.apache.jena.atlas.web.HttpException;

/**
 * Registry of (username, password) for a remote location (endpoint URI and optional realm.
 */
public class AuthCredentials {
    private Map<AuthDomain, PasswordRecord> authRegistry = new ConcurrentHashMap<>();
    private Trie<AuthDomain> prefixes = new Trie<>();
    public AuthCredentials() {}

    public void put(AuthDomain location, PasswordRecord pwRecord) {
        // Checks.
        URI uri = location.uri;
        if ( uri.getRawQuery() != null || uri.getRawFragment() != null )
            throw new HttpException("Endpoint URI must not have query string or fragment: "+uri);
        authRegistry.put(location, pwRecord);
        prefixes.add(uri.toString(), location);
    }

    public boolean contains(AuthDomain location) {
        return prefixes.contains(location.uri.toString());
    }

    public List<AuthDomain> registered() {
        return new ArrayList<>(authRegistry.keySet());
    }

    public PasswordRecord get(AuthDomain location) {
        PasswordRecord pwRecord = authRegistry.get(location);
        if ( pwRecord != null )
            return pwRecord;

        prefixes.partialSearch(location.uri.toString());

        AuthDomain match = prefixes.longestMatch(location.uri.toString());
        if ( match == null )
            return null;
        if ( match.getRealm() != null ) {
            if ( location.getRealm() != null && ! Objects.equals(location.getRealm(), match.getRealm()) )
            return null;
        }
        return authRegistry.get(match);
    }

    public void remove(AuthDomain location) {
        prefixes.remove(location.uri.toString());
        authRegistry.remove(location);
    }

    public void clearAll() {
        authRegistry.clear();
    }
}
