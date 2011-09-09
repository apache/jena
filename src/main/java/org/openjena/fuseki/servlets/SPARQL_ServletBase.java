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

package org.openjena.fuseki.servlets;

import static java.lang.String.format ;

import java.io.IOException ;
import java.io.PrintWriter ;
import java.util.Enumeration ;
import java.util.Map ;
import java.util.concurrent.atomic.AtomicLong ;

import javax.servlet.ServletException ;
import javax.servlet.http.HttpServlet ;
import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.openjena.atlas.lib.Lib ;
import org.openjena.fuseki.Fuseki ;
import org.openjena.fuseki.HttpNames ;
import org.openjena.fuseki.http.HttpSC ;
import org.openjena.fuseki.server.DatasetRegistry ;
import org.openjena.fuseki.server.DatasetRef ;
import org.slf4j.Logger ;

import com.hp.hpl.jena.query.QueryParseException ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;

public abstract class SPARQL_ServletBase extends HttpServlet
{
    protected static final Logger log = Fuseki.requestLog ;
    protected static AtomicLong requestIdAlloc = new AtomicLong(0) ;
    private final PlainRequestFlag noQueryString ;
    protected final boolean verbose_debug ;

    // Flag for whether a request (no query string) is handled as a regular operation or
    // routed to special handler.
    protected enum PlainRequestFlag { REGULAR, DIFFERENT }
    
    protected SPARQL_ServletBase(PlainRequestFlag noQueryStringIsOK, boolean verbose_debug)
    {
        this.noQueryString = noQueryStringIsOK ;
        this.verbose_debug = verbose_debug ;
    }
    
    // Common framework for handling HTTP requests
    protected void doCommon(HttpServletRequest request, HttpServletResponse response)
    //throws ServletException, IOException
    {
        long id = requestIdAlloc.incrementAndGet() ;
        printRequest(id, request) ;
        
        HttpServletResponseTracker responseTracked = new HttpServletResponseTracker(response) ;
        response = responseTracked ;
        
        String uri = request.getRequestURI() ;
        setCommonHeaders(response) ;
        
        try {
            if ( request.getQueryString() == null && noQueryString == PlainRequestFlag.DIFFERENT )
            {
                boolean requestContinue = requestNoQueryString(request, response) ;
                if ( ! requestContinue ) 
                    return ;
            }

            uri = mapRequestToDataset(uri) ;
            DatasetGraph dsg = null ;
            if ( uri != null )
            {
                DatasetRef desc = DatasetRegistry.get().get(uri) ;
                if ( desc == null )
                {
                    errorNotFound("No dataset for URI: "+uri) ;
                    return ;
                }
                dsg = desc.dataset ;
            }
            perform(id, dsg, request, response) ;
            //serverlog.info(String.format("[%d] 200 Success", id)) ;
        } catch (ActionErrorException ex)
        {
            if ( ex.exception != null )
                ex.exception.printStackTrace(System.err) ;
            // Log mesage done by printResponse in a moment.
            if ( ex.message != null )
                responseSendError(response, ex.rc, ex.message) ;
            else
                responseSendError(response, ex.rc) ;
        }
        catch (Throwable ex)
        {   // This should not happen.
            //ex.printStackTrace(System.err) ;
            log.warn(format("[%d] RC = %d : %s", id, HttpSC.INTERNAL_SERVER_ERROR_500, ex.getMessage(), ex)) ;
            responseSendError(response, HttpSC.INTERNAL_SERVER_ERROR_500, ex.getMessage()) ;
        }
        
        printResponse(id, responseTracked) ;
    }

    //@Override
    @SuppressWarnings("unused")
    protected void doPatch(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "HTTP PATCH not supported");
    }
    
    private void printRequest(long id, HttpServletRequest request)
    {
        String url = wholeRequestURL(request) ;
        String method = request.getMethod() ;

        log.info(format("[%d] %s %s", id, method, url)) ; 
        if ( verbose_debug )
        {
            @SuppressWarnings("unchecked")
            Enumeration<String> en = request.getHeaderNames() ;
            for ( ; en.hasMoreElements() ; )
            {
                String h = en.nextElement() ;
                @SuppressWarnings("unchecked")
                Enumeration<String> vals = request.getHeaders(h) ;
                if ( ! vals.hasMoreElements() )
                    log.info(format("[%d]   ",id, h)) ;
                else
                {
                    for ( ; vals.hasMoreElements() ; )
                        log.info(format("[%d]   %-20s %s", id, h, vals.nextElement())) ;
                }
            }
        }
    }

    
    private void printResponse(long id, HttpServletResponseTracker response)
    {
        if ( verbose_debug )
        {
            if ( response.contentType != null )
                log.info(format("[%d]   %-20s %s", id, HttpNames.hContentType, response.contentType)) ;
            if ( response.contentLength != -1 )
                log.info(format("[%d]   %-20s %d", id, HttpNames.hContentLengh, response.contentLength)) ;
            for ( Map.Entry<String, String> e: response.headers.entrySet() )
                log.info(format("[%d]   %-20s %s", id, e.getKey(), e.getValue())) ;
        }
        
        if ( response.message == null )
            log.info(String.format("[%d] %d %s", id, response.statusCode, HttpSC.getMessage(response.statusCode))) ;
        else
            log.info(String.format("[%d] %d %s", id, response.statusCode, response.message)) ;
    }
    
    private void responseSendError(HttpServletResponse response, int statusCode, String message)
    {
        try { response.sendError(statusCode, message) ; }
        catch (IOException ex) { errorOccurred(ex) ; }
    }
    
    private void responseSendError(HttpServletResponse response, int statusCode)
    {
        try { response.sendError(statusCode) ; }
        catch (IOException ex) { errorOccurred(ex) ; }
    }

    /** Map request to uri in the registry.
     *  null means no mapping done (passthrough). 
     */
    protected abstract String mapRequestToDataset(String uri) ;
    
    /** A possible implementation for mapRequestToDataset(String) */
    protected String mapRequestToDataset(String uri, String tail)
    {
        if ( uri.endsWith(tail) )
            return uri.substring(0, uri.length()-tail.length()) ;
        return null ;
    }

    protected abstract void perform(long id, DatasetGraph dsg, HttpServletRequest request, HttpServletResponse response) ;

    /** Request had no query string.
     *  Either: (1) handle the request in this opeation - throw an error or send response
     *  and return "false" (don't continue) 
     *  Or: (2) return true for continue.
     */
    protected abstract boolean requestNoQueryString(HttpServletRequest request, HttpServletResponse response) ;
    
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
    
    protected static String messageForQPE(QueryParseException ex)
    {
        if ( ex.getMessage() != null )
            return ex.getMessage() ;
        if ( ex.getCause() != null )
            return Lib.classShortName(ex.getCause().getClass()) ;
        return null ;
    }

    public static void setCommonHeaders(HttpServletResponse httpResponse)
    {
        httpResponse.setHeader(HttpNames.hAccessControlAllowOrigin, "*") ;
        httpResponse.setHeader(HttpNames.hServer, Fuseki.serverHttpName) ;
    }
}
