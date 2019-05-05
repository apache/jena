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

package org.apache.jena.fuseki.jetty;

import static java.lang.String.format;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jena.fuseki.servlets.ServletOps;
import org.apache.jena.web.HttpSC;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.handler.ErrorHandler;

/** One line Fuseki error handler */
public class FusekiErrorHandler1 extends ErrorHandler
{
    public FusekiErrorHandler1() {}

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String method = request.getMethod();

        if ( !method.equals(HttpMethod.GET.asString()) 
            && !method.equals(HttpMethod.POST.asString()) 
            && !method.equals(HttpMethod.HEAD.asString()) )
            return;

        response.setContentType(MimeTypes.Type.TEXT_PLAIN_UTF_8.asString());
        ServletOps.setNoCache(response);
        int code = response.getStatus();
        String message = (response instanceof Response) ? ((Response)response).getReason() : HttpSC.getMessage(code);
        response.getOutputStream().print(format("Error %d: %s\n", code, message));
    }
}
