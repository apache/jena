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

package org.apache.jena.fuseki.authz;

import java.io.IOException;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.jena.web.HttpSC;
import org.apache.shiro.web.filter.authz.AuthorizationFilter;
import org.apache.shiro.web.util.WebUtils;

/** Specialise AuthorizationFilter to yield HTTP 403 on access denied */
public abstract class AuthorizationFilter403 extends AuthorizationFilter
{
    private String message;

    protected AuthorizationFilter403(String text)   { setMessage(text); }
    protected AuthorizationFilter403()              { this(null); }

    /** Set the message used in HTTP 403 responses */
    public void setMessage(String msg) { message = msg; }

    public String getMessage() { return message; }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws IOException {
        HttpServletResponse httpResponse;
        try { httpResponse = WebUtils.toHttp(response); }
        catch (ClassCastException ex) {
            // Not a HTTP Servlet operation
            return super.onAccessDenied(request, response);
        }
        if ( message == null )
            httpResponse.sendError(HttpSC.FORBIDDEN_403);
        else
            httpResponse.sendError(HttpSC.FORBIDDEN_403, message);
        return false;  // No further processing.
    }
}

