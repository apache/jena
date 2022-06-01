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
import java.util.function.Function;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.jena.atlas.web.AuthScheme;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.servlets.ServletOps;
import org.apache.jena.http.auth.AuthHeader;
import org.apache.jena.riot.web.HttpNames;
import org.apache.jena.web.HttpSC;
import org.slf4j.Logger;

/**
 * Process an "Authorization: Bearer" header.
 * If present, extract as JWT
 * <p>
 * This has two modes:
 * <ul>
 * <li>{@code requireBearer=true} : Only accept requests with a bearer
 * authorization header. If missing, respond with a 401 challenge asking for a
 * bearer token.</li>
 * <li>{@code requireBearer=false} : Verify any bearer token but otherwise pass
 * through the request as-is. This will pass through requests to an unsecured
 * ("public") dataset but will cause a 403 on a secured dataset, not a 401
 * challenge.
 * </ul>
 * <p>
 * A more flexible approach for mixing authentication methods is to setup Fuseki
 * with multiple {@code AuthBearerFilter} filters installed in a Fuseki
 * server, with different path specs.</li>
 */
public class AuthBearerFilter implements Filter {
    private static Logger log = Fuseki.serverLog;
    private final Function<String, String> verifiedUser;
    private final boolean requireBearer;

    /**
     * Create a servlet filter that verifies a JWT as bearer authentication.
     *
     * @param verifiedUser Function to take the encoded bearer token and return the
     *     user name of a verified user.
     * @param requireBearer Mode
     */
    public AuthBearerFilter(Function<String, String> verifiedUser, boolean requireBearer) {
        this.verifiedUser = verifiedUser;
        this.requireBearer = requireBearer;
    }

    /**
     * Create a servlet filter that verifies a JWT as bearer authentication. Only
     * requests with a verifiable bearer authorization header are accepted. If there
     * is no "Authentication" header, or it does not specify "Bearer", respond with a
     * 401 challenge asking for a bearer token.
     * <p>
     * This is equivalent to calling the 2-argument constructor with
     * "requireBearer=true".
     */
    public AuthBearerFilter(Function<String, String> verifiedUser) {
        this(verifiedUser, true);
    }

    @Override
    public void init(FilterConfig filterConfig) {}

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
        throws IOException, ServletException {
        try {
            HttpServletRequest request = (HttpServletRequest)servletRequest;
            HttpServletResponse response = (HttpServletResponse)servletResponse;
            // The request to pass along the filter chain.
            HttpServletRequest chainRequest = request;

            // Authorization
            String auth = request.getHeader(HttpNames.hAuthorization);

            // If auth required.
            if ( auth == null && requireBearer ) {
                send401bearer(response);
                return;
            }

            // 401 challenge if auth but not bearer.
            if ( auth != null ) {
                AuthHeader authParser = AuthHeader.parse(auth);
                String bearerToken = authParser.getBearerToken();
                String x = authParser.getUnknown();
                if ( requireBearer ) {
                    // Not the required bearer.
                    if ( ! AuthScheme.BEARER.equals(authParser.getAuthScheme())) {
                        send401bearer(response);
                        return;
                    }
                }

                switch(authParser.getAuthScheme()) {
                    case BEARER : {
                        if ( bearerToken == null ) {
                            log.warn("Not a legal bearer token: "+authParser.getAuthArgs());
                            response.sendError(HttpSC.BAD_REQUEST_400);
                            return;
                        }
                        if ( verifiedUser == null ) {
                            // No function to verify the token and extract the user.
                            response.sendError(HttpSC.BAD_REQUEST_400);
                            return;
                        }
                        String user = verifiedUser.apply(bearerToken);
                        if ( user == null ) {
                            response.sendError(HttpSC.FORBIDDEN_403);
                            return;
                        }
                        chainRequest = new HttpServletRequestWithPrincipal(request, user);
                        break;
                    }
                    case UNKNOWN :
                    case BASIC :
                    case DIGEST :
                    default :
                        break;
                }
            }
            // Continue.
            chain.doFilter(chainRequest, servletResponse);

        } catch (Throwable ex) {
            log.info("Filter: unexpected exception: "+ex.getMessage(),ex);
            ServletOps.error(500);
            return;
        }
    }

    private void send401bearer(HttpServletResponse response) throws IOException {
        response.setHeader(HttpNames.hWWWAuthenticate, "Bearer");   // No realm, no scope.
        response.sendError(HttpSC.UNAUTHORIZED_401);
    }

    /** Add a value for "getUserPrincipal" */
    private static class HttpServletRequestWithPrincipal extends HttpServletRequestWrapper {

        final String user ;
        HttpServletRequestWithPrincipal(HttpServletRequest req, String user) {
            super(req);
            this.user = user;
        }

        @Override
        public String getRemoteUser() {
            return user;
        }

        @Override public java.security.Principal getUserPrincipal() {
            return new java.security.Principal() {
                @Override public String getName() { return user; }
            };
        }
    }

    @Override
    public void destroy() {}

}
