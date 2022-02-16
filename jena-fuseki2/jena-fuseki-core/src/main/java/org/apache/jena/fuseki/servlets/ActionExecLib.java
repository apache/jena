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

import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jena.atlas.RuntimeIOException;
import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.server.*;
import org.apache.jena.fuseki.system.ActionCategory;
import org.apache.jena.query.QueryCancelledException;
import org.apache.jena.riot.web.HttpNames;
import org.apache.jena.shared.OperationDeniedException;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.jena.web.HttpSC;
import org.slf4j.Logger;

/**
 * Functions relating to {@link HttpAction} objects, including the standard execute with logging process ({@link #execAction})
 */
public class ActionExecLib {

    /**
     * Returns a fresh HTTP Action for this request.
     * @param dap
     * @param request HTTP request
     * @param response HTTP response
     * @return a new HTTP Action
     */
    public static HttpAction allocHttpAction(DataAccessPoint dap, Logger log, ActionCategory category, HttpServletRequest request, HttpServletResponse response) {
        long id = allocRequestId(request, response);
        // Need a way to set verbose logging on a per servlet and per request basis.
        HttpAction action = new HttpAction(id, log, category, request, response);
        if ( dap != null ) {
            // TODO remove setRequest?
            DataService dataService = dap.getDataService();
            action.setRequest(dap, dataService);
        }
        return action;
    }

    /**
     * Standard execution lifecycle for a SPARQL Request.
     * <ul>
     * <li>{@link #startRequest(HttpAction)}</li>
     * <li>initial statistics,</li>
     * <li>{@link ActionLifecycle#validate(HttpAction)} request,</li>
     * <li>{@link ActionLifecycle#execute(HttpAction)} request,</li>
     * <li>completion/error statistics,</li>
     * <li>{@link #finishRequest(HttpAction)}
     * </ul>
     * Common process for handling HTTP requests with logging and Java error
     * handling. This is the case where the ActionProcessor is defined by or is the
     * servlet directly outside the Fuseki dispatch process ({@link ServletAction}
     * for special case like {@link SPARQL_QueryGeneral} which directly holds the {@link ActionProcessor}
     * and {@link ServletProcessor} for administration actions.
     * <p>
     * Return false if the ActionProcessor is null.
     *
     * @param action
     * @param processor
     */
    public static void execAction(HttpAction action, ActionProcessor processor) {
        boolean b = execAction(action, ()->processor);
        if ( !b )
            ServletOps.errorNotFound("Not found: "+action.getActionURI());
    }

    /**
     * execAction, allowing for a choice of {@link ActionProcessor} within the logging and error handling.
     * Return false if there was no ActionProcessor to handle the action.
     */
    public static boolean execAction(HttpAction action, Supplier<ActionProcessor> processor) {
        try {
            return execActionSub(action, processor);
        } catch (Throwable th) {
            // This really should not catch anything.
            FmtLog.error(action.log, th, "Internal error");
            return true;
        }
    }

    private static boolean execActionSub(HttpAction action, Supplier<ActionProcessor> processor) {
        logRequest(action);
        action.setStartTime();
        initResponse(action);
        HttpServletResponse response = action.getResponse();

        startRequest(action);
        try {
            // Get the processor inside the startRequest - error handling - finishRequest sequence.
            ActionProcessor proc = processor.get();
            if ( proc == null ) {
                // Only for the logging.
                finishRequest(action);
                logNoResponse(action);
                archiveHttpAction(action);
                // Can't find the URL (the /dataset/service case) - not handled here.
                return false;
            }
            proc.process(action);
        } catch (QueryCancelledException ex) {
            // To put in the action timeout, need (1) global, (2) dataset and (3) protocol settings.
            // See
            //    global -- cxt.get(ARQ.queryTimeout)
            //    dataset -- dataset.getContect(ARQ.queryTimeout)
            //    protocol -- SPARQL_Query.setAnyTimeouts
            String message = "Query timed out";
            ServletOps.responseSendError(response, HttpSC.SERVICE_UNAVAILABLE_503, message);
        } catch (OperationDeniedException ex) {
            if ( ex.getMessage() == null )
                FmtLog.info(action.log, "[%d] OperationDeniedException", action.id);
            else
                FmtLog.info(action.log, "[%d] OperationDeniedException: %s", action.id, ex.getMessage());
            ServletOps.responseSendError(response, HttpSC.FORBIDDEN_403);
        } catch (ActionErrorException ex) {
            if ( ex.getCause() != null )
                FmtLog.warn(action.log, ex, "[%d] ActionErrorException with cause", action.id);
            // Log message done by printResponse in a moment.
            if ( ex.getMessage() != null )
                ServletOps.responseSendError(response, ex.getRC(), ex.getMessage());
            else
                ServletOps.responseSendError(response, ex.getRC());
        } catch (HttpException ex) {
            int sc = ex.getStatusCode();
            if ( sc <= 0 )
                // -1: Connection problem.
                sc = 400;
            // Some code is passing up its own HttpException.
            if ( ex.getMessage() == null )
                ServletOps.responseSendError(response, sc);
            else
                ServletOps.responseSendError(response, sc, ex.getMessage());
        } catch (QueryExceptionHTTP ex) {
            // SERVICE failure.
            int sc = ex.getStatusCode();
            if ( sc <= 0 )
                // -1: Connection problem. "Bad Gateway"
                sc = 502;
            if ( ex.getMessage() == null )
                ServletOps.responseSendError(response, sc);
            else
                ServletOps.responseSendError(response, sc, ex.getMessage());
        } catch (RuntimeIOException ex) {
            FmtLog.warn(action.log, /*ex,*/ "[%d] Runtime IO Exception (client left?) RC = %d : %s", action.id, HttpSC.INTERNAL_SERVER_ERROR_500, ex.getMessage());
            ServletOps.responseSendError(response, HttpSC.INTERNAL_SERVER_ERROR_500, ex.getMessage());
        } catch (Throwable ex) {
            // This should not happen.
            //ex.printStackTrace(System.err);
            FmtLog.warn(action.log, ex, "[%d] RC = %d : %s", action.id, HttpSC.INTERNAL_SERVER_ERROR_500, ex.getMessage());
            ServletOps.responseSendError(response, HttpSC.INTERNAL_SERVER_ERROR_500, ex.getMessage());
        } finally {
            action.setFinishTime();
            finishRequest(action);
        }
        // Handled - including sending back errors.
        logResponse(action);
        archiveHttpAction(action);
        return true;
    }

    /**
     * Helper method which gets a unique request ID and appends it as a header to the
     * response
     *
     * @param request   HTTP Request
     * @param response  HTTP Response
     * @return Request ID
     */
    public static long allocRequestId(HttpServletRequest request, HttpServletResponse response) {
        long id = requestIdAlloc.incrementAndGet();
        addRequestId(response, id);
        return id;
    }

    private static AtomicLong     requestIdAlloc = new AtomicLong(0);

    /**
     * Helper method for attaching a request ID to a response as a header
     *
     * @param response
     *            Response
     * @param id
     *            Request ID
     */
    public static void addRequestId(HttpServletResponse response, long id) {
        response.setHeader(Fuseki.FusekiRequestIdHeader, Long.toString(id));
    }

    /**
     * Begin handling an {@link HttpAction}
     * @param action
     */
    private static void startRequest(HttpAction action) {
        action.startRequest();
    }

    /**
     * Stop handling an {@link HttpAction}
     */
    private static void finishRequest(HttpAction action) {
        action.finishRequest();
    }

    private static boolean logLifecycle(HttpAction action) {
        return action.verbose || action.category != ActionCategory.ADMIN;
    }

    /** Log an {@link HttpAction} request. */
    public static void logRequest(HttpAction action) {
        String url = ActionLib.wholeRequestURL(action.getRequest());
        String method = action.getRequestMethod();

        if ( logLifecycle(action) )
            FmtLog.info(action.log, "[%d] %s %s", action.id, method, url);
        if ( action.verbose ) {
            Enumeration<String> en = action.getRequestHeaderNames();
            for (; en.hasMoreElements();) {
                String h = en.nextElement();
                Enumeration<String> vals = action.getRequestHeaders(h);
                if ( !vals.hasMoreElements() )
                    FmtLog.info(action.log, "[%d]   => %s", action.id, h+":");
                else {
                    for (; vals.hasMoreElements();)
                        FmtLog.info(action.log, "[%d]   => %-20s %s", action.id, h+":", vals.nextElement());
                }
            }
        }
    }

    /**
     * Log an {@link HttpAction} response.
     * This includes a message to the action log and also on to the standard format Combined NCSA log.
     */
    public static void logResponse(HttpAction action) {
        long time = action.getTime();

        if ( action.verbose ) {
            if ( action.responseContentType != null )
                FmtLog.info(action.log,"[%d]   <= %-20s %s", action.id, HttpNames.hContentType+":", action.responseContentType);
            if ( action.responseContentLength != -1 )
                FmtLog.info(action.log,"[%d]   <= %-20s %d", action.id, HttpNames.hContentLength+":", action.responseContentLength);
            for (Map.Entry<String, String> e : action.headers.entrySet()) {
                // Skip already printed.
                if ( e.getKey().equalsIgnoreCase(HttpNames.hContentType) && action.responseContentType != null)
                    continue;
                if ( e.getKey().equalsIgnoreCase(HttpNames.hContentLength) && action.responseContentLength != -1)
                    continue;
                FmtLog.info(action.log,"[%d]   <= %-20s %s", action.id, e.getKey()+":", e.getValue());
            }
        }

        String timeStr = fmtMillis(time);

        if ( logLifecycle(action) ) {
            if ( action.message == null )
                FmtLog.info(action.log, "[%d] %d %s (%s)",
                    action.id, action.statusCode, HttpSC.getMessage(action.statusCode), timeStr);
            else
                FmtLog.info(action.log,"[%d] %d %s (%s)", action.id, action.statusCode, action.message, timeStr);
        }
        // Standard format NCSA log.
        if ( action.category == ActionCategory.ACTION ) {
            if ( Fuseki.requestLog != null && Fuseki.requestLog.isInfoEnabled() ) {
                String s = RequestLog.combinedNCSA(action);
                Fuseki.requestLog.info(s);
            }
        }
    }

    /**
     * Log when we don't handle this request.
     */
    public static void logNoResponse(HttpAction action) {
        FmtLog.info(action.log,"[%d] No Fuseki dispatch %s", action.id, action.getActionURI());
    }

    /** Set headers for the response. */
    public static void initResponse(HttpAction action) {
        ActionLib.setCommonHeaders(action);
        String method = action.getRequestMethod();
        // All GET and HEAD operations are sensitive to conneg so ...
        if ( HttpNames.METHOD_GET.equalsIgnoreCase(method) || HttpNames.METHOD_HEAD.equalsIgnoreCase(method) )
            ServletBase.setVaryHeader(action.getResponse());
    }

    /**
     * <p>Given a time point, return the time as a milli second string if it is less than 1000,
     * otherwise return a seconds string.</p>
     * <p>It appends a 'ms' suffix when using milli secoOnds,
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
            return String.format("%,d ms", time);
        return String.format("%,.3f s", time / 1000.0);
    }

    /**
     * Archives the HTTP Action.
     * @param action HTTP Action
     * @see HttpAction#minimize()
     */
    private static void archiveHttpAction(HttpAction action) {
        action.minimize();
    }

    /** Increment counter */
    public static void incCounter(Counters counters, CounterName name) {
        if ( counters == null )
            return;
        incCounter(counters.getCounters(), name);
    }

    /** Decrement counter */
    public static void decCounter(Counters counters, CounterName name) {
        if ( counters == null )
            return;
        decCounter(counters.getCounters(), name);
    }

    public static void incCounter(CounterSet counters, CounterName name) {
        if ( counters == null )
            return;
        try {
            if ( counters.contains(name) )
                counters.inc(name);
        }
        catch (Exception ex) {
            Fuseki.serverLog.warn("Exception on counter inc", ex);
        }
    }

    public static void decCounter(CounterSet counters, CounterName name) {
        if ( counters == null )
            return;
        try {
            if ( counters.contains(name) )
                counters.dec(name);
        }
        catch (Exception ex) {
            Fuseki.serverLog.warn("Exception on counter dec", ex);
        }
    }

}
