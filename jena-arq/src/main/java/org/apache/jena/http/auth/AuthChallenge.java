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
    public static String SCHEME = "SCHEME";

    public final AuthScheme authScheme;
    public final String realm;
    public final String nonce;
    public final String opaque;
    public final String qop;
    public final Map<String, String> parsed;

    /** Parse "WWW-Authenticate:" challenge message */
    static public AuthChallenge parse(String authHeaderStr) {
        Map<String, String> authHeader = null;
        try {
            authHeader = AuthStringTokenizer.parse(authHeaderStr);
        } catch (Throwable ex) {
            return null;
        }
        try {
            if ( ! authHeader.containsKey(SCHEME) )
                return null;
            AuthScheme authScheme = AuthScheme.scheme(authHeader.get(SCHEME));
            switch(authScheme) {
                case DIGEST :
                    nonNull(RFC2617.strNonce, authHeader.get(RFC2617.strNonce));
                    nonNull(RFC2617.strRealm, authHeader.get(RFC2617.strRealm));
                    break;
                case BASIC :
                    nonNull(RFC2617.strRealm, authHeader.get(RFC2617.strRealm));
                    break;
            }

            return new AuthChallenge(authScheme,
                                     authHeader.get(RFC2617.strRealm),
                                     authHeader.get(RFC2617.strNonce), // Required for digest, not for basic.
                                     authHeader.get(RFC2617.strOpaque),
                                     authHeader.get(RFC2617.strQop),
                                     authHeader);
        } catch (NullPointerException ex) {
            return null;
        }
    }

    private AuthChallenge(AuthScheme authScheme, String realm, String nonce, String opaque, String qop, Map<String, String> parsed) {
        this.authScheme = authScheme;
        this.realm = realm;
        this.nonce = nonce;
        this.opaque = opaque;
        this.qop = qop;
        this.parsed = parsed;
    }

    private static String nonNull(String field, String s) {
        return Objects.requireNonNull(s, "Field="+field);
    }
}
