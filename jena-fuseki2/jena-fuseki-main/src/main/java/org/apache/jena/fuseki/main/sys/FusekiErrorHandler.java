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

import static java.lang.String.format;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jena.fuseki.servlets.ServletOps;
import org.apache.jena.web.HttpSC;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.handler.ErrorHandler;

/**
 * Fuseki error handler (used with ServletAPI HttpServletResponse.sendError).
 * Typically ServletOps.responseSendError is used which directly sends the error and a message.
 */
public class FusekiErrorHandler extends ErrorHandler
{
    // Only used if ServletOps.responseSendError calls Servlet API response.sendError
    // or a non-Fuseki error occurs.
    public FusekiErrorHandler() {}

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String method = request.getMethod();

        if ( !method.equals(HttpMethod.GET.asString())
            && !method.equals(HttpMethod.POST.asString())
            && !method.equals(HttpMethod.HEAD.asString()) )
            return;

        ServletOps.setNoCache(response);
        int code = response.getStatus();
        String message = (response instanceof Response) ? ((Response)response).getReason() : HttpSC.getMessage(code);
        if ( message == null )
            message = HttpSC.getMessage(code);
        String msg = format("Error %d: %s\n", code, message);
        ServletOps.writeMessagePlainTextError(response, msg);
    }
}
