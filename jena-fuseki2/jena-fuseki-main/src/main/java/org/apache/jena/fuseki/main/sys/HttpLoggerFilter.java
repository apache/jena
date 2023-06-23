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

package org.apache.jena.fuseki.main.sys;

import java.io.IOException;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicLong;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.servlets.ActionLib;
import org.apache.jena.riot.web.HttpNames;
import org.slf4j.Logger;

/**
 * Standalone request-response logger.
 * Use with Fuseki main addFilter
 *
 */
public class HttpLoggerFilter implements Filter {
    private static final Logger log = Fuseki.serverLog;
    private static final boolean verbose = true;

    private static final AtomicLong counter =new AtomicLong(0);

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        // See also ActionExecLib
        String id = "{"+counter.incrementAndGet()+"}";
        logRequest(id, servletRequest);
        chain.doFilter(servletRequest, servletResponse);
        logResponse(id, servletResponse);
    }

    private void logRequest(String id, ServletRequest servletRequest) {
        try {
            HttpServletRequest request = (HttpServletRequest)servletRequest;
            String url = ActionLib.wholeRequestURL(request);
            String method = request.getMethod();
            FmtLog.info(log, "%s %s %s", id, method, url);

            if ( verbose ) {
                Enumeration<String> en = request.getHeaderNames();
                if ( en != null ) {
                    for (; en.hasMoreElements();) {
                        String h = en.nextElement();
                        Enumeration<String> vals = request.getHeaders(h);
                        if ( !vals.hasMoreElements() )
                            FmtLog.info(log, "%s    => %s", id, h+":");
                        else {
                            for (; vals.hasMoreElements();)
                                FmtLog.info(log, "%s    => %-20s %s", id, h+":", vals.nextElement());
                        }
                    }
                }
            }
        } catch (Throwable ex) {
            log.info("Filter (log request): unexpected exception: "+ex.getMessage(),ex);
        }
    }

    private void logResponse(String id, ServletResponse servletResponse) {
        try {
            HttpServletResponse response = (HttpServletResponse)servletResponse;
            if ( verbose ) {
                String responseContentType = response.getHeader(HttpNames.hContentType);
                String responseContentLength = response.getHeader(HttpNames.hContentLength);

                if ( responseContentType != null )
                    FmtLog.info(log, "%s    <= %-20s %s", id, HttpNames.hContentType+":", responseContentType);
                if ( responseContentLength != null )
                    FmtLog.info(log, "%s    <= %-20s %d", id, HttpNames.hContentLength+":", responseContentLength);

                for (String headerName : response.getHeaderNames() ) {
                    // Skip already printed.
                    if ( headerName.equalsIgnoreCase(HttpNames.hContentType) )
                        continue;
                    if ( headerName.equalsIgnoreCase(HttpNames.hContentLength))
                        continue;
                    String headerValue = response.getHeader(headerName);
                    FmtLog.info(log, "%s    <= %-20s %s", id, headerName+":", headerValue);
                }
            }
            FmtLog.info(log, "%s %s", id, response.getStatus());

        } catch (Throwable ex) {
            log.info("Filter (log response: unexpected exception: "+ex.getMessage(),ex);
        }
    }
}
