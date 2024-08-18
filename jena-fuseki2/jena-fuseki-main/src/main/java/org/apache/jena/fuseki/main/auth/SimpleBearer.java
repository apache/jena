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

package org.apache.jena.fuseki.main.auth;

import java.util.Objects;

import org.apache.jena.atlas.logging.Log;
import org.apache.jena.http.HttpLib;
import org.apache.jena.http.auth.AuthLib;

/**
 * Support for bearer authentication token of the form
 * {@code Bearer: base64("user:NAME")}. This is unsafe if believed without further
 * checking but for tests and development it provides a mode whereby the deployment
 * authentication machinery isn't needed.
 * ,p>
 * Usage:
 * <pre>
 *     new AuthBearerFilter(SimpleBearer::getUserFromToken64);
 * </pre>
 *
 */
public class SimpleBearer {

    // Weak development and test form -- "Bearer base64(user:NAME)"
    public static String USER_PREFIX = "user:";

    /**
     * Create the header string setting for bearer auth in the "user:" scheme.
     * The result is for the form {@code "base64chars"}.
     */
    public static String requestAuthorizationToken(String user) {
        Objects.requireNonNull(user);
        String x = USER_PREFIX+user;
        String x64 = AuthLib.base64enc(x);
        return x64;
    }

    /**
     * Create the header string setting for bearer auth in the "user:" scheme.
     * The result is for the form {@code "Bearer base64chars"}.
     */
    public static String requestAuthorizationHeader(String user) {
        Objects.requireNonNull(user);
        String x64 = requestAuthorizationToken(user);
        return HttpLib.bearerAuthHeader(x64);
    }

    /**
     * Get the user name from a "user:" bearer token which not in base64 encoded plain text.
     * @see #getUserFromToken64(String) which decodes and extracts the user.
     */
    public static String getUserFromAuthHeader(String headerValue) {
        if ( ! headerValue.startsWith(HttpLib.BEARER_PREFIX) )
            throw new IllegalArgumentException("Expected a header stirng startsing \"Bearer \"");
        String base64token = headerValue.substring(HttpLib.BEARER_PREFIX.length());
        return getUserFromToken64(base64token);
    }

    /** Decode a base64 string and get the user name assuming the "user:" form. */
    public static String getUserFromToken64(String b64token) {
        String string = AuthLib.base64dec(b64token);
        if ( string == null ) {}
        return getUserFromString(string);
    }

    /**
     * Get the user name from a "user:" bearer token which not in base64 encoded plain text.
     * @see #getUserFromToken64(String) which decodes and extracts the user.
     */
    private static String getUserFromString(String decodedString) {
        if ( ! decodedString.startsWith(USER_PREFIX) ) {
            Log.info(SimpleBearer.class, "Bearer token does not start \"user:\"");
            return null;
        }
        String user = decodedString.substring(USER_PREFIX.length());
        return user;
    }
}
