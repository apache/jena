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

import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.servlets.ServletOps;
import org.apache.jena.http.auth.AuthHeader;
import org.apache.jena.riot.web.HttpNames;
import org.apache.jena.web.HttpSC;
import org.slf4j.Logger;

/**
 * Process an "Authorization: Bearer" header.
 * If present, extract as JWT
 */
public class AuthBearerFilter implements Filter {
    private static Logger log = Fuseki.serverLog;
    private final Function<String, String> verifiedUser;

    @Override
    public void init(FilterConfig filterConfig) {}

//    public AuthBearerFilter() {
//        this(null);
//    }

    public AuthBearerFilter(Function<String, String> verifiedUser) {
        this.verifiedUser = verifiedUser;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
        throws IOException, ServletException {
        HttpServletRequest withUser;
        try {
            HttpServletRequest request = (HttpServletRequest)servletRequest;
            HttpServletResponse response = (HttpServletResponse)servletResponse;
            withUser = request;

            // Authorization: required.
            String auth = request.getHeader(HttpNames.hAuthorization);
            if ( auth == null ) {
                // No auth header - reject and ask for Authorization:
                response.setHeader(HttpNames.hWWWAuthenticate, "Bearer");   // No realm, no scope.
                response.sendError(HttpSC.UNAUTHORIZED_401);
                return;
            }

            AuthHeader authParser = AuthHeader.parse(auth);
            String bearerToken = authParser.getBearerToken();
            String x = authParser.getUnknown();

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
                    withUser = new HttpServletRequestWithPrincipal(request, user);
                    break;
                }
                case UNKNOWN :
                    break;
                case BASIC :
                case DIGEST :
                default :
                    break;
            }
        } catch (Throwable ex) {
            log.info("Filter: unexpected exception: "+ex.getMessage(),ex);
            ServletOps.error(500);
            return;
        }
        // Continue.
        chain.doFilter(withUser, servletResponse);
    }

    /** Add a value for "getUserPrincipal" */
    private static class HttpServletRequestWithPrincipal extends HttpServletRequestWrapper {

        final String user ;
        HttpServletRequestWithPrincipal(HttpServletRequest req, String user) {
            super(req);
            this.user = user;
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
