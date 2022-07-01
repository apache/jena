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
import java.util.Objects;

/** URI, and optional realm, as a value-equality pair. */
public class AuthDomain {
    private URI uri;
    private String realm;
    public static final String noRealm = "";

    public AuthDomain(URI uri) {
        this(uri, null);
    }

    private AuthDomain(URI uri, String realm) {
        Objects.requireNonNull(uri);
        this.uri = uri;
        if ( realm == null )
            realm = noRealm;
        this.realm = realm;
    }

    public URI getURI() {
        return uri;
    }

    public String getRealm() {
        return realm;
    }

    @Override
    public int hashCode() {
        return Objects.hash(realm, uri);
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        AuthDomain other = (AuthDomain)obj;
        return Objects.equals(realm, other.realm) && Objects.equals(uri, other.uri);
    }
}
