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

import java.io.IOException;
import java.util.Objects;
import java.util.function.Function;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.jena.atlas.web.AuthScheme;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.servlets.ServletOps;
import org.apache.jena.http.auth.AuthHeader;
import org.apache.jena.riot.web.HttpNames;
import org.apache.jena.web.HttpSC;
import org.slf4j.Logger;

/**
 * Process an "Authorization: Bearer" header.
 * <p>
 * This has two modes:
 * <ul>
 * <li>{@code requireBearer=true} : Only accept requests with a bearer authorization
 * header. If missing, respond with a 401 challenge asking for a bearer token.</li>
 * <li>{@code requireBearer=false} : Verify any bearer token but otherwise pass
 * through the request as-is. This will pass through requests to an unsecured
 * ("public") dataset but will cause a 403 on a secured dataset, not a 401
 * challenge.</li>
 * </ul>
 * <p>
 * Handling the bearer token is delegated to a handler function, passing the token as
 * seen in the HTTP request. Normally, this will be base64 encoded. It is the
 * responsibility of the handler function to decode the token.
 * <p>
 * This class has some extension points for customizing the handling of bearer
 * authentication for
 * <ul>
 * <li>getting the token from the HTTP request (e.g. from a different HTTP field)</li>
 * <li>handling the challenge case (no authentication provided)</li>
 * <li>handling the case of authentication provided, but it is not "bearer" and bearer is required</li>
 * </ul>
 *
 * A more flexible approach for mixing authentication methods is to setup Fuseki with
 * multiple {@code AuthBearerFilter} filters installed in a Fuseki server, with
 * different path specs.
 */
public class AuthBearerFilter implements Filter {
    private static Logger log = Fuseki.serverLog;
    private final Function<String, String> verifiedUser;
    private final boolean requireBearer;

    public enum BearerMode { REQUIRED, OPTIONAL/*, NONE*/ }

    /**
     * Create a servlet filter that verifies a JWT as bearer authentication. Only
     * requests with a verifiable bearer authorization header are accepted. If there
     * is no "Authentication" header, or it does not specify "Bearer", respond with a
     * 401 challenge asking for a bearer token (customisable behaviour via {@link #sendResponseNoAuthPresent(HttpServletResponse)}).
     * <p>
     * This is equivalent to calling the 2-argument constructor with
     * "{@code requireBearer=true}".
     */
    public AuthBearerFilter(Function<String, String> verifiedUser) {
        this(verifiedUser, BearerMode.REQUIRED);
    }

    /**
     * Create a servlet filter that verifies a JWT as bearer authentication.
     *
     * @param verifiedUser Function to take the encoded bearer token and return the
     *     user name of a verified user.
     * @param bearerMode Whether bearer required or not.
     *     If set OPTIONAL, no auth, Basic and Digest requests will pass through.
     *     If set REQUIRED, Bearer must be present, and no auth causes a challenge.
     */
    public AuthBearerFilter(Function<String, String> verifiedUser, BearerMode bearerMode) {
        Objects.requireNonNull(bearerMode);
        Objects.requireNonNull(verifiedUser);
        this.verifiedUser = verifiedUser;
        this.requireBearer = (bearerMode == BearerMode.REQUIRED);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
        throws IOException, ServletException {
        try {
            HttpServletRequest request = (HttpServletRequest)servletRequest;
            HttpServletResponse response = (HttpServletResponse)servletResponse;

            // Authorization
            String auth = getHttpAuthField(request);

            // If auth required.
            if ( auth == null && requireBearer ) {
                sendResponseNoAuthPresent(response);
                return;
            }

            if ( auth == null && ! requireBearer ) {
                // No auth - not mandatory.
                // Simply continue.
                chain.doFilter(request, response);
                return;
            }

            // ---- Authenticate present.

            // The request to pass along the filter chain.
            // Test for Auth but not bearer.
            AuthHeader authHeader = getAuthToken(request, auth);
            if ( requireBearer ) {
                // Not the required bearer authenticate scheme.
                if ( ! AuthScheme.BEARER.equals(authHeader.getAuthScheme())) {
                    sendResponseBearerRequired(response);
                    return;
                }
            }
            // Not required and not bearer auth.
            switch(authHeader.getAuthScheme()) {
                case BEARER :
                    break;
                case UNKNOWN :
                case BASIC :
                case DIGEST :
                default :
                    chain.doFilter(request, response);
                    return;
            }
            // Bearer auth.
            String bearerToken = authHeader.getBearerToken();

            if ( bearerToken == null ) {
                log.warn("Not a legal bearer token: "+authHeader.getAuthArgs());
                response.sendError(HttpSC.BAD_REQUEST_400);
                return;
            }
            if ( verifiedUser == null ) {
                // No function to verify the token and extract the user.
                response.sendError(HttpSC.BAD_REQUEST_400);
                return;
            }

            // Extract user from the (still encoded) token.
            String user = verifiedUser.apply(bearerToken);
            if ( user == null ) {
                response.sendError(HttpSC.FORBIDDEN_403);
                return;
            }
            HttpServletRequest chainRequest = new HttpServletRequestWithPrincipal(request, user);
            chain.doFilter(chainRequest, servletResponse);

        } catch (Throwable ex) {
            log.info("Filter: unexpected exception: "+ex.getMessage(),ex);
            ServletOps.error(500);
            return;
        }
    }

    @Override
    public void destroy() {}

    /**
     * The HTTP field (header)
     * Usually "Authenticate" ... although AWS Cognito is different.
     */
    protected String getHttpAuthField(HttpServletRequest request) {

        return request.getHeader(HttpNames.hAuthorization);
    }

    /**
     * Send the response when the authentication information is missing.
     * Either 401 (Challenge, expecting the client to send the information)
     * or 403 (no challenge step).
     */
    protected void sendResponseNoAuthPresent(HttpServletResponse response) throws IOException {
        response.setHeader(HttpNames.hWWWAuthenticate, "Bearer");   // No realm, no scope.
        response.sendError(HttpSC.UNAUTHORIZED_401);
    }

    /**
     * Send the response when the authentication required is "Bearer"
     * and it was something else ("Basic", "Digest").
     * Either 401 (Challenge, expecting the client to send the right information)
     * or 403 (no challenge, reject now).
     * <p>
     * Note: 403 is safer to avoid repeated attempts with the same non-bearer authentication
     * when bearer authentication is being used for machine-to-machine services.
     */
    protected void sendResponseBearerRequired(HttpServletResponse response) throws IOException {
        //sendResponseChallenge(response);
        response.sendError(HttpSC.FORBIDDEN_403);
    }

    /**
     * Create a AuthHeader
     * Usually, this reads the "Authenticate" and parses it (RFC 7230, RFC 9112)
     * ... although AWS Cognito is different.
     * The header to access is controlled by {@link #getHttpAuthField(HttpServletRequest)}
     * and the value of the header is passed as {@code authHeaderValue}.
     */
    protected AuthHeader getAuthToken(HttpServletRequest request, String authHeaderValue) {
        return AuthHeader.parseAuth(authHeaderValue);
    }

    /** Wrapper to add the value for "getUserPrincipal"/"getRemoteUser". */
    private static class HttpServletRequestWithPrincipal extends HttpServletRequestWrapper {

        private final String username ;
        HttpServletRequestWithPrincipal(HttpServletRequest req, String username) {
            super(req);
            this.username = username;
        }

        @Override
        public String getRemoteUser() {
            return username;
        }

        @Override public java.security.Principal getUserPrincipal() {
            return () -> username;
        }
    }
}
