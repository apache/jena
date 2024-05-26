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

package org.apache.jena.fuseki.cmd;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.jena.riot.WebContent;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.util.Callback;

/**
 * Fuseki error handler (used with ServletAPI HttpServletResponse.sendError).
 * Typically ServletOps.responseSendError is used which directly sends the error and a message.
 */
public class FusekiErrorHandler extends ErrorHandler
{
    private static List<Charset> utf8List = List.of(StandardCharsets.UTF_8);

    // Only used if ServletOps.responseSendError calls Servlet API response.sendError
    // or a non-Fuseki error occurs.
    public FusekiErrorHandler() {}

    @Override
    protected boolean generateAcceptableResponse(Request request, Response response, Callback callback, String contentType, List<Charset> charsets, int code, String message, Throwable cause) throws IOException {
        // Fix Jetty GH-10474 : (12.0.0, 12.0.1)
        // ContentType application/json cause illegal state exception
        if ( contentType != null && contentType.equals(WebContent.contentTypeJSON) ) {
            contentType = MimeTypes.Type.TEXT_PLAIN.asString();
            charsets = utf8List;
        }
        //ServletOps.setNoCache(response);
        return super.generateAcceptableResponse(request, response, callback, contentType, utf8List, code, message, cause);
    }
}
