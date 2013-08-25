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

import java.io.IOException ;
import java.io.PrintWriter ;
import java.util.concurrent.atomic.AtomicLong ;

import javax.servlet.http.HttpServlet ;
import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.fuseki.Fuseki ;
import org.apache.jena.fuseki.HttpNames ;
import org.apache.jena.web.HttpSC ;
import org.slf4j.Logger ;

public abstract class ServletBase extends HttpServlet
{
    protected static final Logger log = Fuseki.requestLog ;
    public final boolean verboseLogging = Fuseki.verboseLogging ;
    private static AtomicLong requestIdAlloc = new AtomicLong(0) ;

    protected ServletBase()     { }
    
    /**
     * Helper method which gets a unique request ID and appends it as a header to the response
     * @param request  HTTP Request
     * @param response HTTP Response
     * @return Request ID
     */
    protected long allocRequestId(HttpServletRequest request, HttpServletResponse response) {
        long id = requestIdAlloc.incrementAndGet();
        addRequestId(response, id);
        return id;
    }
    
    /**
     * Helper method for attaching a request ID to a response as a header
     * @param response Response
     * @param id Request ID
     */
    protected void addRequestId(HttpServletResponse response, long id) {
        response.addHeader("Fuseki-Request-ID", Long.toString(id));
    }
    
    protected void responseSendError(HttpServletResponse response, int statusCode, String message)
    {
        try { response.sendError(statusCode, message) ; }
        catch (IOException ex) { errorOccurred(ex) ; }
        catch (IllegalStateException ex) { }
    }
    
    protected void responseSendError(HttpServletResponse response, int statusCode)
    {
        try { response.sendError(statusCode) ; }
        catch (IOException ex) { errorOccurred(ex) ; }
    }

    protected static String wholeRequestURL(HttpServletRequest request)
    {
        StringBuffer sb = request.getRequestURL() ;
        String queryString = request.getQueryString() ;
        if ( queryString != null )
        {
            sb.append("?") ;
            sb.append(queryString) ;
        }
        return sb.toString() ;
    }
    
    protected static void successNoContent(HttpAction action)
    {
        success(action, HttpSC.NO_CONTENT_204);
    }
    
    protected static void success(HttpAction action)
    {
        success(action, HttpSC.OK_200);
    }

    protected static void successCreated(HttpAction action)
    {
        success(action, HttpSC.CREATED_201);
    }
    
    // When 404 is no big deal e.g. HEAD
    protected static void successNotFound(HttpAction action) 
    {
        success(action, HttpSC.NOT_FOUND_404) ;
    }

    //
    protected static void success(HttpAction action, int httpStatusCode)
    {
        action.response.setStatus(httpStatusCode);
    }
    
    protected static void successPage(HttpAction action, String message)
    {
        try {
            action.response.setContentType("text/html");
            action.response.setStatus(HttpSC.OK_200);
            PrintWriter out = action.response.getWriter() ;
            out.println("<html>") ;
            out.println("<head>") ;
            out.println("</head>") ;
            out.println("<body>") ;
            out.println("<h1>Success</h1>");
            if ( message != null )
            {
                out.println("<p>") ;
                out.println(message) ;
                out.println("</p>") ;
            }
            out.println("</body>") ;
            out.println("</html>") ;
            out.flush() ;
        } catch (IOException ex) { errorOccurred(ex) ; }
    }
    
    protected static void warning(String string)
    {
        log.warn(string) ;
    }
    
    protected static void warning(String string, Throwable thorwable)
    {
        log.warn(string, thorwable) ;
    }
    
    protected static void errorBadRequest(String string)
    {
        error(HttpSC.BAD_REQUEST_400, string) ;
    }

    protected static void errorNotFound(String string)
    {
        error(HttpSC.NOT_FOUND_404, string) ;
    }

    protected static void errorNotImplemented(String msg)
    {
        error(HttpSC.NOT_IMPLEMENTED_501, msg) ;
    }
    
    protected static void errorMethodNotAllowed(String method)
    {
        error(HttpSC.METHOD_NOT_ALLOWED_405, "HTTP method not allowed: "+method) ;
    }

    protected static void errorForbidden(String msg)
    {
        if ( msg != null )
            error(HttpSC.FORBIDDEN_403, msg) ;
        else
            error(HttpSC.FORBIDDEN_403, "Forbidden") ;
    }
    
    protected static void error(int statusCode)
    {
        throw new ActionErrorException(null, null, statusCode) ;
    }
    

    protected static void error(int statusCode, String string)
    {
        throw new ActionErrorException(null, string, statusCode) ;
    }
    
    protected static void errorOccurred(String message)
    {
        errorOccurred(message, null) ;
    }

    protected static void errorOccurred(Throwable ex)
    {
        errorOccurred(null, ex) ;
    }

    protected static void errorOccurred(String message, Throwable ex)
    {
        throw new ActionErrorException(ex, message, HttpSC.INTERNAL_SERVER_ERROR_500) ;
    }
    
    protected static String formatForLog(String string)
    {
        string = string.replace('\n', ' ') ;
        string = string.replace('\r', ' ') ;
        return string ; 
    }

    static String varyHeaderSetting = 
        StrUtils.strjoin(",", 
                         HttpNames.hAccept, 
                         HttpNames.hAcceptEncoding, 
                         HttpNames.hAcceptCharset ) ;
    
    public static void setVaryHeader(HttpServletResponse httpResponse)
    {
        httpResponse.setHeader(HttpNames.hVary, varyHeaderSetting) ;
    }

    
    public static void setCommonHeaders(HttpServletResponse httpResponse)
    {
        httpResponse.setHeader(HttpNames.hAccessControlAllowOrigin, "*") ;
        httpResponse.setHeader(HttpNames.hServer, Fuseki.serverHttpName) ;
    }
}
