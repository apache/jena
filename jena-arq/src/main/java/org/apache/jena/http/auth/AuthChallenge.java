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

import java.util.Map;
import java.util.Objects;

import org.apache.jena.atlas.web.AuthScheme;

/** Details of a "WWW-Authenticate" header. */
public class AuthChallenge {
    /**
     * Map key name used to record the authentication scheme (entries in the header
     * are lower case and never clash with this name).
     */
    public final AuthScheme authScheme;
    public final AuthHeader authHeader;
    public final String realm;
    public final String nonce;
    public final String opaque;
    public final String qop;
    // May be null;
    public final Map<String, String> authParams;

    /** Parse "WWW-Authenticate:" challenge message */
    static public AuthChallenge parse(String authHeaderStr) {
        AuthHeader auth;
        try {
            auth = AuthHeader.parse(authHeaderStr);
            if ( auth == null )
                return null;
            if ( auth.getAuthScheme() == null )
                return null;
        } catch (Throwable ex) {
            return null;
        }
        try {
            //Checking.
            AuthScheme authScheme = auth.getAuthScheme();
            switch(authScheme) {
                case DIGEST :
                    nonNull(auth, AuthHttp.strNonce);
                    nonNull(auth, AuthHttp.strRealm);
                    break;
                case BASIC :
                    // This is the challenge so realm is required.
                    nonNull(auth, AuthHttp.strRealm);
                    break;
                case BEARER:
                    // RFC 6750
                    break;
                case UNKNOWN :
                    break;
                default :
                    break;
            }

            return new AuthChallenge(authScheme,
                                     auth,
                                     get(auth, AuthHttp.strRealm),
                                     get(auth, AuthHttp.strNonce), // Required for digest, not for basic.
                                     get(auth, AuthHttp.strOpaque),
                                     get(auth, AuthHttp.strQop),
                                     auth.getAuthParams());
        } catch (NullPointerException ex) {
            return null;
        }
    }

    private AuthChallenge(AuthScheme authScheme, AuthHeader authHeader, String realm, String nonce, String opaque, String qop, Map<String, String> authParams) {
        Objects.requireNonNull(authScheme);
        Objects.requireNonNull(authHeader);
        this.authScheme = authScheme;
        this.authHeader = authHeader;
        this.realm = realm;
        this.nonce = nonce;
        this.opaque = opaque;
        this.qop = qop;
        this.authParams = authParams;
    }

    public String getRealm() {
        if ( authParams == null )
            return null;
        return authParams.get(AuthHttp.strRealm);
    }

    public String getToken() {
        if ( ! Objects.equals(authHeader.getAuthScheme(), AuthScheme.BEARER) )
            return null;
        return authHeader.getBearerToken();
    }

    private static String get(AuthHeader auth, String s) {
        Map<String, String> map = auth.getAuthParams();
        if ( map == null )
            return null;
        return map.get(s);
    }

    private static String nonNull(AuthHeader auth, String s) {
        Map<String, String> map = auth.getAuthParams();
        if ( map == null )
            throw new NullPointerException("No auth params");
        return Objects.requireNonNull(map.get(s), "Field="+auth);
    }
}
