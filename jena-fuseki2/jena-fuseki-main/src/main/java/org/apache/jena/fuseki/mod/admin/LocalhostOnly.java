/**
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

package org.apache.jena.fuseki.mod.admin;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.web.HttpSC;
import org.slf4j.Logger;

/**
 * Responds with HTTP 403 on any denied request.
 */
public class LocalhostOnly implements Filter {

    private static String LOCALHOST_IpV6_a = "[0:0:0:0:0:0:0:1]";
    private static String LOCALHOST_IpV6_b = "0:0:0:0:0:0:0:1";
    // This is what appears in the Chrome developer tools client-side.
    // "[0:0:0:0:0:0:0:1]" by the time it arrives here, it is not clear which
    // software component is responsible for that.
    // To be safe we add "[::1]".
    private static String LOCALHOST_IpV6_c = "[::1]";
    private static String LOCALHOST_IpV4   = "127.0.0.1";   // Strictly, 127.*.*.*

    private static final Collection<String> localhosts = new HashSet<>(
            Arrays.asList(LOCALHOST_IpV4, LOCALHOST_IpV6_a, LOCALHOST_IpV6_b, LOCALHOST_IpV6_c));

    private static Logger log = Fuseki.serverLog;
    private static final String message = "Access denied : only localhost access allowed";

    public LocalhostOnly() { }

    // "permit" and "deny" lists
    private List<String> secured = Arrays.asList("/$/backup", "/$/compact", "/$/datasets");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            HttpServletRequest req = (HttpServletRequest)request;
            HttpServletResponse resp = (HttpServletResponse)response;
            /**
             * Check request from "localhost, else 403.
             */
            boolean accept = checkRequest(req, resp);
            if ( ! accept ) {
                // Log
                resp.sendError(HttpSC.FORBIDDEN_403);
                return;
            }
        } catch (Throwable ex) {
            log.info("Filter: unexpected exception: "+ex.getMessage(),ex);
        }
        // Continue.
        chain.doFilter(request, response);
    }

    public boolean checkRequest(HttpServletRequest req, HttpServletResponse resp) {
        String uri = req.getRequestURI();
        for ( String s : secured ) {
            if ( uri.startsWith(s) ) {
                if ( ! checkLocalhost(req) )
                    return false;
            }
        }
        return true;
    }

    public static boolean checkLocalhost(HttpServletRequest request) {
        return localhosts.contains(request.getRemoteAddr()) ;
    }
}
