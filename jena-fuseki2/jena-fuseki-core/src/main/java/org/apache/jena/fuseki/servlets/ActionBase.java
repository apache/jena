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

import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.apache.jena.atlas.RuntimeIOException ;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.query.QueryCancelledException ;
import org.apache.jena.riot.web.HttpNames ;
import org.apache.jena.web.HttpSC ;
import org.slf4j.Logger ;

/** General request lifecycle */
public abstract class ActionBase extends ServletBase
{
    protected final Logger log ;

    protected ActionBase(Logger log) {
        super() ;
        this.log = log ;
    }
    
    @Override 
    public void init() {
//        log.info("["+Utils.className(this)+"] ServletContextName = "+getServletContext().getServletContextName()) ;
//        log.info("["+Utils.className(this)+"] ContextPath        = "+getServletContext().getContextPath()) ;
    }
    
    /**
     * Common framework for handling HTTP requests.
     * @param request
     * @param response
     */
    protected void doCommon(HttpServletRequest request, HttpServletResponse response)
    {
        try {
            long id = allocRequestId(request, response);
            
            // Lifecycle
            HttpAction action = allocHttpAction(id, request, response) ;

            printRequest(action) ;
            action.setStartTime() ;
            
            // The response may be changed to a HttpServletResponseTracker
            response = action.response ;
            initResponse(request, response) ;
            try {
                action.startRequest();
                execCommonWorker(action) ;
            } catch (QueryCancelledException ex) {
                // To put in the action timeout, need (1) global, (2) dataset and (3) protocol settings.
                // See
                //    global -- cxt.get(ARQ.queryTimeout) 
                //    dataset -- dataset.getContect(ARQ.queryTimeout)
                //    protocol -- SPARQL_Query.setAnyTimeouts
                
                String message = String.format("Query timed out");
                // Possibility :: response.setHeader("Retry-after", "600") ;    // 5 minutes
                ServletOps.responseSendError(response, HttpSC.SERVICE_UNAVAILABLE_503, message);
            } catch (ActionErrorException ex) {
                if ( ex.getCause() != null )
                    ex.getCause().printStackTrace(System.err) ;
                // Log message done by printResponse in a moment.
                if ( ex.getMessage() != null )
                    ServletOps.responseSendError(response, ex.getRC(), ex.getMessage()) ;
                else
                    ServletOps.responseSendError(response, ex.getRC()) ;
            } catch (HttpException ex) {
                // Some code is passing up its own HttpException.
                if ( ex.getMessage() == null )
                    ServletOps.responseSendError(response, ex.getResponseCode());
                else
                    ServletOps.responseSendError(response, ex.getResponseCode(), ex.getMessage());
            } catch (RuntimeIOException ex) {
                log.warn(format("[%d] Runtime IO Exception (client left?) RC = %d : %s", id, HttpSC.INTERNAL_SERVER_ERROR_500, ex.getMessage()), ex) ;
                ServletOps.responseSendError(response, HttpSC.INTERNAL_SERVER_ERROR_500, ex.getMessage()) ;
            } catch (Throwable ex) {
                // This should not happen.
                //ex.printStackTrace(System.err) ;
                log.warn(format("[%d] RC = %d : %s", id, HttpSC.INTERNAL_SERVER_ERROR_500, ex.getMessage()), ex) ;
                ServletOps.responseSendError(response, HttpSC.INTERNAL_SERVER_ERROR_500, ex.getMessage()) ;
            } finally {
                action.setFinishTime() ;
                finishRequest(action);
            }
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
        return new HttpAction(id, log, request, response);
    }

    /**
     * Begin handling an {@link HttpAction}  
     * @param action
     */
    protected final void startRequest(HttpAction action) {
        action.startRequest() ;
    }
    
    /**
     * Stop handling an {@link HttpAction}  
     */
    protected final void finishRequest(HttpAction action) {
        action.finishRequest() ;
    }
    
    /**
     * Archives the HTTP Action.
     * @param action HTTP Action
     * @see HttpAction#minimize()
     */
    private void archiveHttpAction(HttpAction action) {
        action.minimize() ;
    }

    /**
     * Execute this request, which maybe a admin operation or a client request. 
     * @param action HTTP Action
     */
    protected abstract void execCommonWorker(HttpAction action) ;
    
    /**
     * Extract the name after the container name (servlet name).
     * @param action an HTTP action
     * @return item name as "/name" or {@code null}
     */
    protected static String extractItemName(HttpAction action) {
//      action.log.info("context path  = "+action.request.getContextPath()) ;
//      action.log.info("pathinfo      = "+action.request.getPathInfo()) ;
//      action.log.info("servlet path  = "+action.request.getServletPath()) ;
      // if /name
      //    request.getServletPath() otherwise it's null
      // if /*
      //    request.getPathInfo() ; otherwise it's null.
      
      // PathInfo is after the servlet name. 
      String x1 = action.request.getServletPath() ;
      String x2 = action.request.getPathInfo() ;
      
      String pathInfo = action.request.getPathInfo() ;
      if ( pathInfo == null || pathInfo.isEmpty() || pathInfo.equals("/") )
          // Includes calling as a container. 
          return null ;
      String name = pathInfo ;
      // pathInfo starts with a "/"
      int idx = pathInfo.lastIndexOf('/') ;
      if ( idx > 0 )
          name = name.substring(idx) ;
      // Returns "/name"
      return name ; 
  }

    protected void doPatch(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "HTTP PATCH not supported");
    }
    
    private void printRequest(HttpAction action) {
        String url = ActionLib.wholeRequestURL(action.request) ;
        String method = action.request.getMethod() ;

        log.info(format("[%d] %s %s", action.id, method, url)) ;
        if ( action.verbose ) {
            Enumeration<String> en = action.request.getHeaderNames() ;
            for (; en.hasMoreElements();) {
                String h = en.nextElement() ;
                Enumeration<String> vals = action.request.getHeaders(h) ;
                if ( !vals.hasMoreElements() )
                    log.info(format("[%d]   => %s", action.id, h+":")) ;
                else {
                    for (; vals.hasMoreElements();)
                        log.info(format("[%d]   => %-20s %s", action.id, h+":", vals.nextElement())) ;
                }
            }
        }
    }

    private void initResponse(HttpServletRequest request, HttpServletResponse response) {
        setCommonHeaders(response) ;
        String method = request.getMethod() ;
        // All GET and HEAD operations are sensitive to conneg so ...
        if ( HttpNames.METHOD_GET.equalsIgnoreCase(method) || HttpNames.METHOD_HEAD.equalsIgnoreCase(method) )
            setVaryHeader(response) ;
    }

    private void printResponse(HttpAction action) {
        long time = action.getTime() ;

        HttpServletResponseTracker response = action.response ;
        if ( action.verbose ) {
            if ( action.responseContentType != null )
                log.info(format("[%d]   <= %-20s %s", action.id, HttpNames.hContentType+":", action.responseContentType)) ;
            if ( action.responseContentLength != -1 )
                log.info(format("[%d]   <= %-20s %d", action.id, HttpNames.hContentLengh+":", action.responseContentLength)) ;
            for (Map.Entry<String, String> e : action.headers.entrySet()) {
                // Skip already printed.
                if ( e.getKey().equalsIgnoreCase(HttpNames.hContentType) && action.responseContentType != null)
                    continue;
                if ( e.getKey().equalsIgnoreCase(HttpNames.hContentLengh) && action.responseContentLength != -1)
                    continue;
                log.info(format("[%d]   <= %-20s %s", action.id, e.getKey()+":", e.getValue())) ;
            }
        }

        String timeStr = fmtMillis(time) ;

        if ( action.message == null )
            log.info(String.format("[%d] %d %s (%s)", action.id, action.statusCode,
                                   HttpSC.getMessage(action.statusCode), timeStr)) ;
        else
            log.info(String.format("[%d] %d %s (%s)", action.id, action.statusCode, action.message, timeStr)) ;
        
        // See also HttpAction.finishRequest - request logging happens there.
    }

    /**
     * <p>Given a time point, return the time as a milli second string if it is less than 1000,
     * otherwise return a seconds string.</p>
     * <p>It appends a 'ms' suffix when using milli seconds,
     *  and 's' for seconds.</p>
     * <p>For instance: </p>
     * <ul>
     * <li>10 emits 10 ms</li>
     * <li>999 emits 999 ms</li>
     * <li>1000 emits 1.000 s</li>
     * <li>10000 emits 10.000 s</li>
     * </ul>
     * @param time the time in milliseconds
     * @return the time as a display string
     */

    private static String fmtMillis(long time) {
        // Millis only? seconds only?
        if ( time < 1000 )
            return String.format("%,d ms", time) ;
        return String.format("%,.3f s", time / 1000.0) ;
    }
}
