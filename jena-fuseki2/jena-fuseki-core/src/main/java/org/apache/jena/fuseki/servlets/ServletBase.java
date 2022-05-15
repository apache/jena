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

import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.riot.web.HttpNames;

/**
 * Addition HTTP Servlet operations.
 */
public abstract class ServletBase extends HttpServlet {

    public static final String METHOD_DELETE    = "DELETE";
    public static final String METHOD_HEAD      = "HEAD";
    public static final String METHOD_GET       = "GET";
    public static final String METHOD_OPTIONS   = "OPTIONS";
    public static final String METHOD_POST      = "POST";
    public static final String METHOD_PUT       = "PUT";
    public static final String METHOD_TRACE     = "TRACE";
    public static final String METHOD_PATCH     = "PATCH";

    private static AtomicLong     requestIdAlloc = new AtomicLong(0);

    protected ServletBase() {}

    /**
     * Helper method which gets a unique request id and appends it as a header
     * to the response
     *
     * @param request
     *            HTTP Request
     * @param response
     *            HTTP Response
     * @return Request Id
     */
    protected static long allocRequestId(HttpServletRequest request, HttpServletResponse response) {
        long id = requestIdAlloc.incrementAndGet();
        addRequestId(response, id);
        return id;
    }

    /**
     * Helper method for attaching a request ID to a response as a header
     *
     * @param response
     *            Response
     * @param id
     *            Request ID
     */
    protected static void addRequestId(HttpServletResponse response, long id) {
        response.addHeader(Fuseki.FusekiRequestIdHeader, Long.toString(id));
    }

    static final String varyHeaderSetting = String.join(",",
         HttpNames.hAccept,
         HttpNames.hAcceptEncoding,
         HttpNames.hAcceptCharset,
         HttpNames.hOrigin,
         HttpNames.hAccessControlRequestMethod,
         HttpNames.hAccessControlRequestHeaders
            );

    public static void setVaryHeader(HttpServletResponse httpResponse) {
        httpResponse.setHeader(HttpNames.hVary, varyHeaderSetting);
    }

    /** Done via web.xml */
    public static boolean CORS_ENABLED = false;

    public static void setCommonHeadersForOptions(HttpServletResponse httpResponse) {
        if ( CORS_ENABLED )
            httpResponse.setHeader(HttpNames.hAccessControlAllowHeaders, "X-Requested-With, Content-Type, Authorization");
        httpResponse.setHeader(HttpNames.hContentLength, "0");
        setCommonHeaders(httpResponse);
    }

    public static void setCommonHeaders(HttpServletResponse httpResponse) {
        if ( CORS_ENABLED )
            httpResponse.setHeader(HttpNames.hAccessControlAllowOrigin, "*");
        if ( Fuseki.outputFusekiServerHeader )
            httpResponse.setHeader(HttpNames.hServer, Fuseki.serverHttpName);
    }
}
