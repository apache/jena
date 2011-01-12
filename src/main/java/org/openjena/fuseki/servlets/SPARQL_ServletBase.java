/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.fuseki.servlets;

import static java.lang.String.format ;
import static org.openjena.fuseki.Fuseki.serverlog ;

import java.io.IOException ;
import java.io.PrintWriter ;
import java.util.Enumeration ;
import java.util.Map ;
import java.util.concurrent.atomic.AtomicLong ;

import javax.servlet.ServletException ;
import javax.servlet.http.HttpServlet ;
import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.openjena.fuseki.Fuseki ;
import org.openjena.fuseki.HttpNames ;
import org.openjena.fuseki.http.HttpSC ;
import org.openjena.fuseki.server.DatasetRegistry ;

import com.hp.hpl.jena.sparql.core.DatasetGraph ;

public abstract class SPARQL_ServletBase extends HttpServlet
{
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
            // Read only action.
            DatasetGraph dsg = DatasetRegistry.get().get(uri) ;
            if ( dsg == null )
            {
                errorNotFound("No dataset for URI: "+uri) ;
                return ;
            }

            perform(id, dsg, request, response) ;
            //serverlog.info(String.format("[%d] 200 Success", id)) ;
        } catch (ActionErrorException ex)
        {
            if ( ex.exception != null )
                ex.exception.printStackTrace(System.err) ;

            if ( ex.message != null )
            {
                responseSendError(response, ex.rc, ex.message) ;
                //serverlog.info(format("[%d] RC = %d : %s",id, ex.rc, ex.message)) ;
            }
            else
            {
                responseSendError(response, ex.rc) ;
                //serverlog.info(format("[%d] RC = %d : %s",id, ex.rc)) ;
            }
        }
        catch (Exception ex)
        {   // This should not happen.
            ex.printStackTrace(System.err) ;
            responseSendError(response, HttpSC.INTERNAL_SERVER_ERROR_500, ex.getMessage()) ;
            //serverlog.info(format("[%d] RC = %d : %s", id, HttpSC.INTERNAL_SERVER_ERROR_500, ex.getMessage())) ;
        }
        
        printResponse(id, responseTracked) ;
    }

    //@Override
    protected void doPatch(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "HTTP PATCH not supported");
    }
    
    private void printRequest(long id, HttpServletRequest request)
    {
        String url = wholeRequestURL(request) ;
        String method = request.getMethod() ;

        serverlog.info(format("[%d] %s %s", id, method, url)) ; 
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
                    serverlog.info(format("[%d]   ",id, h)) ;
                else
                {
                    for ( ; vals.hasMoreElements() ; )
                        serverlog.info(format("[%d]   %-20s %s", id, h, vals.nextElement())) ;
                }
            }
        }
    }

    
    private void printResponse(long id, HttpServletResponseTracker response)
    {
        if ( verbose_debug )
        {
            if ( response.contentType != null )
                serverlog.info(format("[%d]   %-20s %s", id, HttpNames.hContentType, response.contentType)) ;
            if ( response.contentLength != -1 )
                serverlog.info(format("[%d]   %-20s %d", id, HttpNames.hContentLengh, response.contentLength)) ;
            for ( Map.Entry<String, String> e: response.headers.entrySet() )
                serverlog.info(format("[%d]   %-20s %s", id, e.getKey(), e.getValue())) ;
        }
        
        if ( response.message == null )
            serverlog.info(String.format("[%d] %d %s", id, response.statusCode, HttpSC.getMessage(response.statusCode))) ;
        else
            serverlog.info(String.format("[%d] %d %s", id, response.statusCode, response.message)) ;
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

    protected abstract String mapRequestToDataset(String uri) ;
    
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

    public static void setCommonHeaders(HttpServletResponse httpResponse)
    {
        httpResponse.setHeader(HttpNames.hAccessControlAllowOrigin, "*") ;
        httpResponse.setHeader(HttpNames.hServer, Fuseki.serverHttpName) ;
    }
}

/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */