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
import java.util.Iterator ;
import java.util.Map ;
import java.util.concurrent.atomic.AtomicLong ;

import javax.servlet.ServletException ;
import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.apache.jena.fuseki.HttpNames ;
import static org.apache.jena.fuseki.server.CounterName.* ;
import org.apache.jena.fuseki.server.DatasetRef ;
import org.apache.jena.fuseki.server.DatasetRegistry ;
import org.apache.jena.fuseki.server.ServiceRef ;
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
            HttpAction action = allocHttpAction(id, request, response) ;
            // then add to doCommonWorker
            // work with HttpServletResponseTracker
            
            printRequest(action) ;
            action.setStartTime() ;
            
            response = action.response ;
            initResponse(request, response) ;
            Context cxt = ARQ.getContext() ;
            
            try {
                execCommonWorker(action) ;
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
    
            action.setFinishTime() ;
            printResponse(action) ;
            archiveHttpAction(action) ;
        } catch (Throwable th) {
            log.error("Internal error", th) ;
        }
    }


    // ---- Operation lifecycle

    /** Return a fresh WebAction for this request */
    protected HttpAction allocHttpAction(long id, HttpServletRequest request, HttpServletResponse response) {
        return new HttpAction(id, request, response, verbose_debug) ;
    }


    protected abstract void validate(HttpAction action) ;
    protected abstract void perform(HttpAction action) ;

    // Default start/finish steps. 
    protected void startRequest(HttpAction action) {
    }
    
    protected void finishRequest(HttpAction action) { }
    
    private void archiveHttpAction(HttpAction action)
    {
        action.minimize() ;
    }

    private void execCommonWorker(HttpAction action)
    {
        DatasetRef dsRef = null ;
        String uri = action.request.getRequestURI() ;

        String datasetUri = mapRequestToDataset(uri) ;
        
        if ( datasetUri != null ) {
            dsRef = DatasetRegistry.get().get(datasetUri) ;
            if ( dsRef == null ) {
                errorNotFound("No dataset for URI: "+datasetUri) ;
                return ;
            }
        } else {
            // General SPARQL processor
            // - is this the right way to do it?
            // = or make it a completely separate servlet. 
            dsRef = new DatasetRef();
            dsRef.dataset = dummyDSG;
            dsRef.init() ;
        }
        
        action.setDataset(dsRef) ;
        String serviceName = mapRequestToService(dsRef, uri, datasetUri) ;
        ServiceRef srvRef = dsRef.getServiceRef(serviceName) ;
        action.setService(srvRef) ;
        executeAction(action) ;
    }
        
    protected void inc(AtomicLong x)
    {
        x.incrementAndGet() ;
    }

    // Execute - no stats.
    // Intecept point for the UberServlet 
    protected void executeAction(HttpAction action)
    {
        executeLifecycle(action) ;
    }
    
    // This is the service request lifecycle.
    // Called directly by the UberServlet which has not done any stats by this point.
    protected void executeLifecycle(HttpAction action)
    {
        action.dsRef.counters.inc(Requests) ;
        action.srvRef.counters.inc(Requests) ;

        startRequest(action) ;
        try {
            validate(action) ;
        } catch (ActionErrorException ex) {
            action.dsRef.counters.inc(RequestsBad) ;
            throw ex ;
        }

        try {
            perform(action) ;
            // Success
            action.srvRef.counters.inc(RequestsGood) ;
            action.dsRef.counters.inc(RequestsGood) ;
        } catch (ActionErrorException ex) {
            action.srvRef.counters.inc(RequestsBad) ;
            action.dsRef.counters.inc(RequestsBad) ;
            throw ex ;
        } catch (QueryCancelledException ex) {
            action.srvRef.counters.inc(RequestsBad) ;
            action.dsRef.counters.inc(RequestsBad) ;
            throw ex ;
        } finally {
            finishRequest(action) ;
        }
    }
   
    @SuppressWarnings("unused") // ServletException
    protected void doPatch(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "HTTP PATCH not supported");
    }
    
    private void printRequest(HttpAction action)
    {
        String url = wholeRequestURL(action.request) ;
        String method = action.request.getMethod() ;

        log.info(format("[%d] %s %s", action.id, method, url)) ;
        if (verbose_debug) {
            Enumeration<String> en = action.request.getHeaderNames() ;
            for (; en.hasMoreElements();) {
                String h = en.nextElement() ;
                Enumeration<String> vals = action.request.getHeaders(h) ;
                if (!vals.hasMoreElements())
                    log.info(format("[%d]   ", action.id, h)) ;
                else {
                    for (; vals.hasMoreElements();)
                        log.info(format("[%d]   %-20s %s", action.id, h, vals.nextElement())) ;
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
    
    private void printResponse(HttpAction action)
    {
        long time = action.getTime() ;
        
        HttpServletResponseTracker response = action.response ;
        if ( verbose_debug )
        {
            if ( action.contentType != null )
                log.info(format("[%d]   %-20s %s", action.id, HttpNames.hContentType, action.contentType)) ;
            if ( action.contentLength != -1 )
                log.info(format("[%d]   %-20s %d", action.id, HttpNames.hContentLengh, action.contentLength)) ;
            for ( Map.Entry<String, String> e: action.headers.entrySet() )
                log.info(format("[%d]   %-20s %s", action.id, e.getKey(), e.getValue())) ;
        }

        String timeStr = fmtMillis(time) ;

        if ( action.message == null )
            log.info(String.format("[%d] %d %s (%s) ", action.id, action.statusCode, HttpSC.getMessage(action.statusCode), timeStr)) ;
        else
            log.info(String.format("[%d] %d %s (%s) ", action.id, action.statusCode, action.message, timeStr)) ;
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
        // e.g. /dataset/sparql => /dataset 
        int i = uri.lastIndexOf('/') ;
        if ( i == -1 )
            return null ;
        if ( i == 0 )
        {
            // started with '/' - leave.
            return uri ;
        }
        
        return uri.substring(0, i) ;
    }    /** Find the dataset name even if direct naming */ 
    protected static String findTrailing(String uri, String dsname) 
    {
        if ( dsname.length() >= uri.length() )
            return "" ;
        return uri.substring(dsname.length()+1) ;   // Skip the separating "/"
    }

    

    
    protected static String mapRequestToService(DatasetRef dsRef, String uri, String serviceName)
    {
        if ( dsRef.name.length() >= uri.length() )
            return "" ;
        return uri.substring(dsRef.name.length()+1) ;   // Skip the separating "/"
        
    }
    
    /** Implementation of mapRequestToDataset(String) that looks for the longest match.
     *  This includes use in direct naming GSP. 
     */
    protected static String mapRequestToDatasetLongest$(String uri) 
    {
        if ( uri == null )
            return null ;
        
        // Mapping a request for GSP needs to find the "best"
        // (longest matching) dataset URI.
        // This covers local, using the URI as a direct name for
        // a graph, not just using the indirect ?graph= or ?default 
        // forms.
        // Service matching, which is a matter of removing the service component,
        // would only work for indirect. 

        String ds = null ;
        Iterator<String> iter = DatasetRegistry.get().keys() ;
        while(iter.hasNext())
        {
            String ds2 = iter.next();
            if ( ! uri.startsWith(ds2) )
                continue ;

            if ( ds == null )
            {
                ds = ds2 ;
                continue ; 
            }
            if ( ds.length() < ds2.length() )
            {
                ds = ds2 ;
                continue ;
            }
        }
        return ds ;
    }
}
