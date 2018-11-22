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

package org.apache.jena.fuseki.servlets;

import java.io.IOException;
import java.util.function.Predicate;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.web.HttpSC;
import org.eclipse.jetty.security.SecurityHandler;

/**
 * Servlet filter that applies a predicate to incoming requests and rejects with with 403
 * "Forbidden" if the predicate returns false, otherwise it passes the request down the
 * filter chain.
 * <p>
 * Either the user from {@link HttpServletRequest#getRemoteUser() getRemoteUser} is null,
 * no authentication, or it has been validated. Failed authentication will have been
 * handled and rejected by the {@link SecurityHandler security handler} before they get to
 * the filter chain.
 */
public class AuthFilter implements Filter {

    private final Predicate<String> predicate;

    public AuthFilter(Predicate<String> allowAccess) {
        predicate = allowAccess;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filter) throws IOException, ServletException {
        try {
            HttpServletRequest httpRequest = (HttpServletRequest)request;
            HttpServletResponse httpResponse = (HttpServletResponse)response;
            String user = httpRequest.getRemoteUser();
            boolean allowed = predicate.test(user);
            if ( !allowed ) {
                // No action id allocated this early.
                // Fuseki.actionLog.info("Response 403: "+httpRequest.getRequestURI());
                httpResponse.sendError(HttpSC.FORBIDDEN_403);
                return;
            }
            // HTTP only.
            filter.doFilter(httpRequest, httpResponse);
        } catch (ClassCastException ex) {
            Fuseki.actionLog.error(ex.getMessage());
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void destroy() {}
}
