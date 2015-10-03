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
import static org.apache.jena.fuseki.server.CounterName.Requests ;
import static org.apache.jena.fuseki.server.CounterName.RequestsBad ;
import static org.apache.jena.fuseki.server.CounterName.RequestsGood ;

import java.io.IOException ;
import java.util.Enumeration ;
import java.util.Map ;
import java.util.concurrent.atomic.AtomicLong ;

import javax.servlet.ServletException ;
import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.apache.jena.atlas.RuntimeIOException ;
import org.apache.jena.fuseki.Fuseki ;
import org.apache.jena.fuseki.HttpNames ;
import org.apache.jena.fuseki.server.* ;
import org.apache.jena.query.ARQ ;
import org.apache.jena.query.QueryCancelledException ;
import org.apache.jena.sparql.util.Context ;
import org.apache.jena.web.HttpSC ;

/**
 * Base servlet for SPARQL requests.
 */
public abstract class SPARQL_ServletBase extends ServletBase
{
    /**
     * Creates a new SPARQL base Servlet.
     */
    protected SPARQL_ServletBase()      {   super() ; }
    
    // Common framework for handling HTTP requests
    /**
     * Handles GET and POST requests.
     * @param request HTTP request
     * @param response HTTP response
     */
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
            } catch (RuntimeIOException ex) {
                log.warn(format("[%d] Runtime IO Exception (client left?) RC = %d", id, HttpSC.INTERNAL_SERVER_ERROR_500)) ;
                responseSendError(response, HttpSC.INTERNAL_SERVER_ERROR_500, ex.getMessage()) ;
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

    /**
     * Returns a fresh HTTP Action for this request.
     * @param id the Request ID
     * @param request HTTP request
     * @param response HTTP response
     * @return a new HTTP Action
     */
    protected HttpAction allocHttpAction(long id, HttpServletRequest request, HttpServletResponse response) {
        // Need a way to set verbose logging on a per servlet and per request basis. 
        return new HttpAction(id, request, response, verboseLogging) ;
    }

    /**
     * Validates a HTTP Action.
     * @param action HTTP Action
     */
    protected abstract void validate(HttpAction action) ;

    /**
     * Performs the HTTP Action.
     * @param action HTTP Action
     */
    protected abstract void perform(HttpAction action) ;

    /**
     * Default start step.
     * @param action HTTP Action
     */
    protected void startRequest(HttpAction action) {
    }

    /**
     * Default finish step.
     * @param action HTTP Action
     */
    protected void finishRequest(HttpAction action) { }

    /**
     * Archives the HTTP Action.
     * @param action HTTP Action
     * @see HttpAction#minimize()
     */
    private void archiveHttpAction(HttpAction action)
    {
        action.minimize() ;
    }

    /**
     * Executes common tasks, including mapping the request to the right dataset, setting the dataset into the HTTP
     * action, and retrieving the service for the dataset requested. Finally, it calls the
     * {@link #executeAction(HttpAction)} method, which executes the HTTP Action life cycle.
     * @param action HTTP Action
     */
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
        } else
            dsRef = FusekiConfig.serviceOnlyDatasetRef() ;

        action.setDataset(dsRef) ;
        String serviceName = mapRequestToService(dsRef, uri, datasetUri) ;
        ServiceRef srvRef = dsRef.getServiceRef(serviceName) ;
        action.setService(srvRef) ;
        executeAction(action) ;
    }

    /**
     * Utility method, that increments and returns the AtomicLong value.
     * @param x AtomicLong
     */
    protected void inc(AtomicLong x)
    {
        x.incrementAndGet() ;
    }

    /**
     * Executes the HTTP Action. Serves as intercept point for the UberServlet.
     * @param action HTTP Action
     */
    protected void executeAction(HttpAction action)
    {
        executeLifecycle(action) ;
    }
    
    /**
     * Handles the service request lifecycle. Called directly by the UberServlet,
     * which has not done any stats by this point.
     * @param action {@link HttpAction}
     * @see HttpAction
     */
    protected void executeLifecycle(HttpAction action)
    {
        incCounter(action.dsRef, Requests) ;
        incCounter(action.srvRef, Requests) ;

        startRequest(action) ;
        try {
            validate(action) ;
        } catch (ActionErrorException ex) {
            incCounter(action.dsRef,RequestsBad) ;
            throw ex ;
        }

        try {
            perform(action) ;
            // Success
            incCounter(action.srvRef, RequestsGood) ;
            incCounter(action.dsRef, RequestsGood) ;
        } catch (ActionErrorException ex) {
            incCounter(action.srvRef, RequestsBad) ;
            incCounter(action.dsRef, RequestsBad) ;
            throw ex ;
        } catch (QueryCancelledException ex) {
            incCounter(action.srvRef, RequestsBad) ;
            incCounter(action.dsRef, RequestsBad) ;
            throw ex ;
        } finally {
            finishRequest(action) ;
        }
    }

    /**
     * Increments a counter.
     * @param counters a {@link Counter}
     * @param name a {@link CounterName}
     */
    protected static void incCounter(Counters counters, CounterName name) {
        try {
            if ( counters.getCounters().contains(name) )
                counters.getCounters().inc(name) ;
        } catch (Exception ex) {
            Fuseki.serverLog.warn("Exception on counter inc", ex) ;
        }
    }

    /**
     * Decrements a counter.
     * @param counters a {@link Counter}
     * @param name a {@link CounterName}
     */
    protected static void decCounter(Counters counters, CounterName name) {
        try {
            if ( counters.getCounters().contains(name) )
                counters.getCounters().dec(name) ;
        } catch (Exception ex) {
            Fuseki.serverLog.warn("Exception on counter dec", ex) ;
        }
    }

    /**
     * <p>Sends an <strong>error</strong> when the PATCH method is called.</p>
     * <p>Throws ServletException or IOException as per overloaded method signature.</p>
     * @param request HTTP request
     * @param response HTTP response
     * @throws ServletException from overloaded method signature
     * @throws IOException from overloaded method signature
     */
    protected void doPatch(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "HTTP PATCH not supported");
    }

    /**
     * Prints the HTTP Action request to the program log, using the INFO level.
     * @param action {@link HttpAction}
     */
    private void printRequest(HttpAction action)
    {
        String url = wholeRequestURL(action.request) ;
        String method = action.request.getMethod() ;

        log.info(format("[%d] %s %s", action.id, method, url)) ;
        if ( action.verbose ) {
            Enumeration<String> en = action.request.getHeaderNames() ;
            for (; en.hasMoreElements();) {
                String h = en.nextElement() ;
                Enumeration<String> vals = action.request.getHeaders(h) ;
                if (!vals.hasMoreElements())
                    log.info(format("[%d]   %s", action.id, h)) ;
                else {
                    for (; vals.hasMoreElements();)
                        log.info(format("[%d]   %-20s %s", action.id, h, vals.nextElement())) ;
                }
            }
        }
    }

    /**
     * Initiates the response, by setting common headers such as Access-Control-Allow-Origin and Server, and
     * the Vary header if the request method used was a GET.
     * @param request HTTP request
     * @param response HTTP response
     */
    private void initResponse(HttpServletRequest request, HttpServletResponse response)
    {
        setCommonHeaders(response) ;
        String method = request.getMethod() ;
        // All GET and HEAD operations are sensitive to conneg so ...
        if ( HttpNames.METHOD_GET.equalsIgnoreCase(method) || HttpNames.METHOD_HEAD.equalsIgnoreCase(method) )
            setVaryHeader(response) ;
    }

    /**
     * Prints the HTTP Action response to the program log, using the INFO level.
     * @param action {@link HttpAction}
     */
    private void printResponse(HttpAction action)
    {
        long time = action.getTime() ;
        
        HttpServletResponseTracker response = action.response ;
        if ( action.verbose )
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

    /**
     * <p>Given a time epoch, it will return the time in milli seconds if it is less than 1000,
     * otherwise it will normalize it to display as second.</p>
     * <p>It appends a 'ms' suffix when using milli seconds, and ditto <i>s</i> for seconds.</p>
     * <p>For instance: </p>
     * <ul>
     * <li>10 emits 10 ms</li>
     * <li>999 emits 999 ms</li>
     * <li>1000 emits 1.000000 s</li>
     * <li>10000 emits 10.000000 s</li>
     * </ul>
     * @param time the time epoch
     * @return the time in milli seconds or in seconds
     */
    private static String fmtMillis(long time)
    {
        // Millis only? seconds only?
        if ( time < 1000 )
            return String.format("%,d ms", time) ;
        return String.format("%,.3f s", time/1000.0) ;
    }

    /**
     * Map request to uri in the registry. null means no mapping done (passthrough).
     * @param uri the URI
     * @return the dataset
     */
    protected String mapRequestToDataset(String uri) 
    {
        return mapRequestToDataset$(uri) ;
    }
    
    /**
     * A possible implementation for mapRequestToDataset(String) that assumes the form /dataset/service.
     * @param uri the URI
     * @return the dataset
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
    }

    /**
     * Maps a request to a service (e.g. Query, Update).
     * @param dsRef a {@link DatasetRef}
     * @param uri the URI
     * @param datasetURI the dataset URI
     * @return an empty String (i.e. "") if the DatasetRef is null, or if its name is longer than the URI's name.
     * Otherwise will return the service name.
     */
    protected String mapRequestToService(DatasetRef dsRef, String uri, String datasetURI)
    {
        if ( dsRef == null )
            return "" ;
        if ( dsRef.name.length() >= uri.length() )
            return "" ;
        return uri.substring(dsRef.name.length()+1) ;   // Skip the separating "/"
        
    }
    
    /**
     * Implementation of mapRequestToDataset(String) that looks for the longest match in the registry.
     * This includes use in direct naming GSP.
     * @param uri the URI
     * @return <code>null</code> if the URI is null, otherwise will return the longest match in the registry.
     */
    protected static String mapRequestToDatasetLongest$(String uri) 
    {
        if ( uri == null )
            return null ;
        
        // This covers local, using the URI as a direct name for
        // a graph, not just using the indirect ?graph= or ?default 
        // forms.

        String ds = null ;
        for ( String ds2 : DatasetRegistry.get().keys() ) {
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
