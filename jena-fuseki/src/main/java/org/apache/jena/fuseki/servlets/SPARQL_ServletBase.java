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

import static java.lang.String.format ;

import java.io.IOException ;
import java.util.Enumeration ;
import java.util.Map ;

import javax.servlet.ServletException ;
import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.apache.jena.fuseki.HttpNames ;
import org.apache.jena.fuseki.server.DatasetRef ;
import org.apache.jena.fuseki.server.DatasetRegistry ;
import org.apache.jena.web.HttpSC ;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.query.QueryCancelledException ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory ;
import com.hp.hpl.jena.sparql.core.DatasetGraphReadOnly ;
import com.hp.hpl.jena.sparql.util.Context ;

public abstract class SPARQL_ServletBase extends ServletBase
{
    private static DatasetGraph dummyDSG = new DatasetGraphReadOnly(DatasetGraphFactory.createMemFixed()) ;
    
    protected SPARQL_ServletBase(boolean verbose_debug)
    {
        super(verbose_debug) ;
        //this.queryStringHandling = noQueryStringIsOK ;
    }
    
    // Common framework for handling HTTP requests
    protected void doCommon(HttpServletRequest request, HttpServletResponse response)
    //throws ServletException, IOException
    {
        try {
            long id = allocRequestId(request, response);
            
            // Lifecycle
            WebRequest wRequest = allocWebAction(id, request, response) ;
            // then add to doCommonWorker
            // work with HttpServletResponseTracker
            
            printRequest(wRequest) ;
            wRequest.setStartTime() ;
            
            response = wRequest.getResponse() ;
            initResponse(request, response) ;
            Context cxt = ARQ.getContext() ;
    
            try {
                validate(request) ;
                doCommonWorker(id, request, response) ;
            } catch (QueryCancelledException ex) {
                // Also need the per query info ...
                String message = String.format("The query timed out (restricted to %s ms)", cxt.get(ARQ.queryTimeout));
                // Possibility :: response.setHeader("Retry-after", "600") ;    // 5 minutes
                responseSendError(response, HttpSC.SERVICE_UNAVAILABLE_503, message);
            } catch (ActionErrorException ex) {
                if ( ex.exception != null )
                    ex.exception.printStackTrace(System.err) ;
                // Log message done by printResponse in a moment.
                if ( ex.message != null )
                    responseSendError(response, ex.rc, ex.message) ;
                else
                    responseSendError(response, ex.rc) ;
            } catch (Throwable ex) {
                // This should not happen.
                //ex.printStackTrace(System.err) ;
                log.warn(format("[%d] RC = %d : %s", id, HttpSC.INTERNAL_SERVER_ERROR_500, ex.getMessage()), ex) ;
                responseSendError(response, HttpSC.INTERNAL_SERVER_ERROR_500, ex.getMessage()) ;
            }
    
            wRequest.setFinishTime() ;
            printResponse(wRequest) ;
        } catch (Throwable th) {
            log.error("Internal error", th) ;
        }
    }

    /** Return a fresh WebAction for this request */
    protected WebRequest allocWebAction(long id, HttpServletRequest request, HttpServletResponse response) {
        return new WebRequest(id, request, response) ;
    }

    protected abstract void validate(HttpServletRequest request) ;
    
    protected void doCommonWorker(long id, HttpServletRequest request, HttpServletResponse response)
    {
        DatasetRef desc = null ;
        String uri = request.getRequestURI() ;

        uri = mapRequestToDataset(uri) ;

        if ( uri != null )
        {
            desc = DatasetRegistry.get().get(uri) ;
            if ( desc == null )
            {
                errorNotFound("No dataset for URI: "+uri) ;
                return ;
            }
            //cxt = desc.dataset.getContext() ;
        }
        else {
            desc = new DatasetRef();
            desc.dataset = dummyDSG;
        }
        perform(id, desc, request, response) ;
    }

    @SuppressWarnings("unused") // ServletException
    protected void doPatch(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "HTTP PATCH not supported");
    }
    
    private void printRequest(WebRequest wAction)
    {
        String url = wholeRequestURL(wAction.getRequest()) ;
        String method = wAction.getRequest().getMethod() ;

        log.info(format("[%d] %s %s", wAction.id, method, url)) ; 
        if ( verbose_debug )
        {
            Enumeration<String> en = wAction.getRequest().getHeaderNames() ;
            for ( ; en.hasMoreElements() ; )
            {
                String h = en.nextElement() ;
                Enumeration<String> vals = wAction.getRequest().getHeaders(h) ;
                if ( ! vals.hasMoreElements() )
                    log.info(format("[%d]   ", wAction.id, h)) ;
                else
                {
                    for ( ; vals.hasMoreElements() ; )
                        log.info(format("[%d]   %-20s %s", wAction.id, h, vals.nextElement())) ;
                }
            }
        }
    }
    
    private void initResponse(HttpServletRequest request, HttpServletResponse response)
    {
        setCommonHeaders(response) ;
        String method = request.getMethod() ;
        // All GET and HEAD operations are sensitive to conneg so ...
        if ( HttpNames.METHOD_GET.equalsIgnoreCase(method) || HttpNames.METHOD_HEAD.equalsIgnoreCase(method) )
            setVaryHeader(response) ;
    }
    
    private void printResponse(WebRequest wRequest)
    {
        long time = wRequest.getTime() ;
        
        HttpServletResponseTracker response = wRequest.getResponse() ;
        if ( verbose_debug )
        {
            if ( wRequest.contentType != null )
                log.info(format("[%d]   %-20s %s", wRequest.id, HttpNames.hContentType, wRequest.contentType)) ;
            if ( wRequest.contentLength != -1 )
                log.info(format("[%d]   %-20s %d", wRequest.id, HttpNames.hContentLengh, wRequest.contentLength)) ;
            for ( Map.Entry<String, String> e: wRequest.getHeaders().entrySet() )
                log.info(format("[%d]   %-20s %s", wRequest.id, e.getKey(), e.getValue())) ;
        }

        String timeStr = fmtMillis(time) ;

        if ( wRequest.message == null )
            log.info(String.format("[%d] %d %s (%s) ", wRequest.id, wRequest.statusCode, HttpSC.getMessage(wRequest.statusCode), timeStr)) ;
        else
            log.info(String.format("[%d] %d %s (%s) ", wRequest.id, wRequest.statusCode, wRequest.message, timeStr)) ;
    }
    
    private static String fmtMillis(long time)
    {
        // Millis only? seconds only?
        if ( time < 1000 )
            return String.format("%,d ms", time) ;
        return String.format("%,.3f s", time/1000.0) ;
    }

    /** Map request to uri in the registry.
     *  null means no mapping done (passthrough). 
     */
    protected String mapRequestToDataset(String uri) 
    {
        return mapRequestToDataset$(uri) ;
    }
    
    /** A possible implementation for mapRequestToDataset(String)
     *  that assums the form /dataset/service 
     */
    
    protected static String mapRequestToDataset$(String uri)
    {
        // Chop off trailing part - the service selector
        // e.f. /dataset/sparql => /dataset 
        int i = uri.lastIndexOf('/') ;
        if ( i == -1 )
            return null ;
        if ( i == 0 )
        {
            // started with '/' - leave.
            return uri ;
        }
        
        return uri.substring(0, i) ;
    }
    
    protected abstract void perform(long id, DatasetRef desc, HttpServletRequest request, HttpServletResponse response) ;
}
