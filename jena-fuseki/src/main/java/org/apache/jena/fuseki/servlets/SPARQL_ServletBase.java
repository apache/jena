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
        long id = allocRequestId(request, response);
        printRequest(id, request) ;
        
        HttpServletResponseTracker responseTracked = new HttpServletResponseTracker(response) ;
        response = responseTracked ;
        initResponse(request, response) ;
        Context cxt = ARQ.getContext() ;

        try {
            validate(request) ;
            doCommonWorker(id, request, response) ;
        } catch (QueryCancelledException ex)
        {
            // Also need the per query info ...
            String message = String.format("The query timed out (restricted to %s ms)", cxt.get(ARQ.queryTimeout));
            responseSendError(response, HttpSC.REQUEST_TIMEOUT_408, message);
            // Log message done by printResponse in a moment.
        } catch (ActionErrorException ex)
        {
            if ( ex.exception != null )
                ex.exception.printStackTrace(System.err) ;
            // Log message done by printResponse in a moment.
            if ( ex.message != null )
                responseSendError(response, ex.rc, ex.message) ;
            else
                responseSendError(response, ex.rc) ;
        }
        catch (Throwable ex)
        {   // This should not happen.
            //ex.printStackTrace(System.err) ;
            log.warn(format("[%d] RC = %d : %s", id, HttpSC.INTERNAL_SERVER_ERROR_500, ex.getMessage()), ex) ;
            responseSendError(response, HttpSC.INTERNAL_SERVER_ERROR_500, ex.getMessage()) ;
        }

        printResponse(id, responseTracked) ;
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
    
    private void printRequest(long id, HttpServletRequest request)
    {
        String url = wholeRequestURL(request) ;
        String method = request.getMethod() ;

        log.info(format("[%d] %s %s", id, method, url)) ; 
        if ( verbose_debug )
        {
            Enumeration<String> en = request.getHeaderNames() ;
            for ( ; en.hasMoreElements() ; )
            {
                String h = en.nextElement() ;
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
    
    private void initResponse(HttpServletRequest request, HttpServletResponse response)
    {
        setCommonHeaders(response) ;
        String method = request.getMethod().toUpperCase() ;
        // All GET and HEAD operations are sensitive to conneg so ...
        if ( HttpNames.METHOD_GET.equals(method) || HttpNames.METHOD_HEAD.equals(method) )
            setVaryHeader(response) ;
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
