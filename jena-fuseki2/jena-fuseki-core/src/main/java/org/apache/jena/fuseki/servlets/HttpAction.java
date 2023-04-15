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

import static org.apache.jena.query.TxnType.READ;
import static org.apache.jena.query.TxnType.READ_PROMOTE;
import static org.apache.jena.query.TxnType.WRITE;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.DeflaterInputStream;
import java.util.zip.GZIPInputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.lib.NotImplemented;
import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.FusekiException;
import org.apache.jena.fuseki.server.*;
import org.apache.jena.fuseki.system.ActionCategory;
import org.apache.jena.query.TxnType;
import org.apache.jena.riot.WebContent;
import org.apache.jena.riot.web.HttpNames;
import org.apache.jena.sparql.SystemARQ;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.sparql.core.TransactionalLock;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.web.HttpSC;
import org.slf4j.Logger;

/**
 * HTTP action that represents the user request lifecycle. It is is handled in the
 * {@link ActionBase#executeLifecycle(HttpAction)} method.
 */
public class HttpAction
{
    public final long id;
    public final boolean verbose;
    public final ActionCategory category;
    public final Logger log;

    // -- Valid only for operational actions (e.g. SPARQL).

    public  Endpoint        endpoint        = null;
    private Transactional   transactional   = null;
    private boolean         isTransactional = false;
    private DatasetGraph    activeDSG       = null;        // Set when inside begin/end.

    // -- Valid only for administration actions.

    // -- Shared items (but exact meaning may differ)
    /** Handle to dataset+services being acted on (maybe null) */
    private DataAccessPoint dataAccessPoint = null;
    private DataService dataService         = null;
    private String datasetName              = null;        // Dataset URI used (e.g. registry)
    private DatasetGraph dsg                = null;
    private Context context                 = null;

    // ----

    private boolean startTimeIsSet = false;
    private boolean finishTimeIsSet = false;

    private long startTime = -2;
    private long finishTime = -2;

    // Outcome.
    public int statusCode = -1;
    public String message = null;
    public int responseContentLength = -1;
    public String responseContentType = null;

    // Cleared to archive:
    /*package*/ Map <String, String> headers = new HashMap<>();
    private HttpServletRequest request;
    private HttpServletResponseTracker response;

    private final String actionURI;
    private final String contextPath;

    private final OperationRegistry serviceDispatchRegistry;
    private final DataAccessPointRegistry dataAccessPointRegistry;

    /**
     * Creates a new HTTP Action, using the HTTP request and response, and a given ID.
     *
     * @param id given ID
     * @param log Logger for this action
     * @param request HTTP request
     * @param response HTTP response
     */
    public HttpAction(long id, Logger log, ActionCategory category, HttpServletRequest request, HttpServletResponse response) {
        this.id = id;
        // Currently server-wide, e.g. from the command line.
        this.verbose = Fuseki.getVerbose(request.getServletContext());
        this.log = log;
        this.category = category;
        this.request = request;
        this.response = new HttpServletResponseTracker(this, response);
        this.contextPath = request.getServletContext().getContextPath();
        this.actionURI = ActionLib.actionURI(request);
        this.serviceDispatchRegistry = OperationRegistry.get(request.getServletContext());
        this.dataAccessPointRegistry = DataAccessPointRegistry.get(request.getServletContext());
    }

    /**
     * Initialization after action creation, during lifecycle setup. This is "set
     * once" (in other words, constructor-like but delayed because the information is
     * not yet available at the point we want to create the HttpAction).
     * <p>
     * This method sets the action dataset for service requests. Does not apply to "admin" and
     * "ctl" servlets. Setting will replace any existing {@link DataAccessPoint} and
     * {@link DataService}, as the {@link DatasetGraph} of the current HTTP Action.
     * <p>
     * Once it has updated its members, the HTTP Action will change its transactional
     * state and {@link Transactional} instance according to its base dataset graph.
     * </p>
     *
     * @param dataAccessPoint {@link DataAccessPoint}
     * @param dService {@link DataService}
     * @see Transactional
     */

    public void setRequest(DataAccessPoint dataAccessPoint, DataService dService) {
        if ( this.dataAccessPoint != null )
            throw new FusekiException("Redefinition of DataAccessPoint in the request action");
        this.dataAccessPoint = dataAccessPoint;
        if ( dataAccessPoint != null )
            this.datasetName = dataAccessPoint.getName();
        if ( this.dataService != null )
            throw new FusekiException("Redefinition of DatasetRef in the request action");
        if ( dService == null || dService.getDataset() == null )
            throw new FusekiException("Null DataService in the request action");
        this.dataService = dService;
        setDataset(dService.getDataset());
    }

    /** Minimum initialization using just a dataset.
     * <p>
     * the HTTP Action will change its transactional state and
     * {@link Transactional} instance according to its base dataset graph.
     * </p>
     * <p>There is no associated DataAccessPoint or DataService set by this operation.</p>
     *
     * @param dsg DatasetGraph
     */
    private void setDataset(DatasetGraph dsg) {
        this.dsg = dsg;
        this.context = Context.mergeCopy(Fuseki.getContext(), dsg.getContext());
        if ( dsg == null )
            return;
        setTransactionalPolicy(dsg);
    }

    private void setTransactionalPolicy(DatasetGraph dsg) {
        if ( dsg.supportsTransactionAbort() ) {
            // Use transactional if it looks safe - abort is necessary.
            transactional = dsg;
            isTransactional = true;
        } else if ( dsg.supportsTransactions() ) {
            // No abort - e.g. loading data needs buffering against syntax errors.
            transactional = dsg;
            isTransactional = false;
        } else {
            // Nothing to build on.  Be safe.
            transactional = TransactionalLock.createMutex();
            isTransactional = false;
        }
    }

    /** Return the dataset, if any (may be null) */
    public DatasetGraph getDataset() {
        return dsg;
    }

    /** Return the Context for this {@code HttpAction}. */
    public Context getContext() {
        return context;
    }

    /**
     * Return the authenticated user for this {@code HttpAction}.
     * Return null for no authenticated user.
     */
    public String getUser() {
        if ( request == null )
            return null;
        return request.getRemoteUser();
        //Same as: return request.getUserPrincipal().getName();
    }

    /**
     * Return the "Transactional" for this HttpAction.
     */

    public Transactional getTransactional() {
        return transactional;
    }

    /**
     * This is the requestURI with the context path removed.
     * It should be used internally for dispatch.
     */
    public String getActionURI() {
        return actionURI;
    }

    /** Get the context path.
     */
    public String getContextPath() {
        return contextPath;
    }

    /**
     * Get the {@link OperationRegistry} for this action.
     */
    public OperationRegistry getOperationRegistry() {
        return serviceDispatchRegistry;
    }

    /**
     * Get the {@link DataAccessPointRegistry} for this action.
     */
    public DataAccessPointRegistry getDataAccessPointRegistry() {
        return dataAccessPointRegistry;
    }

    /** Set the endpoint and endpoint name that this is an action for.
     * @param endpoint {@link Endpoint}
     */
    public void setEndpoint(Endpoint endpoint) {
        if ( endpoint != null )
            this.context = Context.mergeCopy(getContext(), endpoint.getContext());
        this.endpoint = endpoint;
    }

    /** Get the endpoint for the action (may be null) . */
    public Endpoint getEndpoint() {
        return endpoint;
    }

    /**
     * Returns whether or not the underlying DatasetGraph is fully transactional (supports rollback)
     */
    public boolean isTransactional() {
        return isTransactional;
    }

    public void begin(TxnType txnType) {
        if ( transactional != null )
            transactional.begin(txnType);
        activeDSG = dsg;
        if ( dataService != null )
            dataService.startTxn(txnType);
    }

    public void begin() {
        begin(READ_PROMOTE);
    }

    public void beginWrite() {
        begin(WRITE);
    }

    public void beginRead() {
        begin(READ);
    }

    public void endRead() {
        if ( dataService != null )
            dataService.finishTxn();
        if ( transactional != null ) {
            try { transactional.commit(); } catch (RuntimeException ex) {}
            try { transactional.end(); } catch (RuntimeException ex) {}
        }
    }

    public void end() {
        dataService.finishTxn();
        if ( transactional.isInTransaction() ) {
            FmtLog.warn(log, "[%d] Transaction still active - no commit or abort seen (forced abort)", this.id);
            try {
                transactional.abort();
            } catch (RuntimeException ex) {
                FmtLog.warn(log, "[%d] Exception in forced abort (trying to continue)", this.id, ex);
            }
        }
        if ( transactional.isInTransaction() ) {
            try { transactional.end(); }
            catch (RuntimeException ex) {}
        }
        endOfAction();
    }

    private void endOfAction() {
        activeDSG = null;
        // Should be handled where necessary in the request handling.
//        if ( inputStream != null ) {
//            ActionLib.consumeBody(this);
//        }
//        if ( outputStream != null ) {
//            IO.flush(outputStream);
//            IO.close(outputStream);
//        }
    }

    public void commit() {
        dataService.finishTxn();
        transactional.commit();
        end();
    }

    /** Abort: ignore exceptions (for clearup code) */
    public void abortSilent() {
        try { transactional.abort(); }
        catch (Exception ex) {}
        finally {
            try { end(); } catch (Exception ex) {}
        }
    }

    public void abort() {
        try { transactional.abort(); }
        catch (Exception ex) {
            // Some datasets claim to be transactional but
            // don't provide a real abort. We tried to avoid
            // them earlier but even if they sneak through,
            // we try to continue server operation.
            Log.warn(this, "Exception during abort (operation attempts to continue): "+ex.getMessage());
        }
        end();
    }

    public final void startRequest() {
        if ( dataAccessPoint != null )
            dataAccessPoint.startRequest(this);
    }

    public final void finishRequest() {
        if ( dataAccessPoint != null )
            dataAccessPoint.finishRequest(this);
    }

    /** If inside the transaction for the action, return the active {@link DatasetGraph},
     *  otherwise return null.
     * @return Current active {@link DatasetGraph}
     */
    public final DatasetGraph getActiveDSG() {
        return activeDSG;
    }

    public final DataAccessPoint getDataAccessPoint() {
        return dataAccessPoint;
    }

//    public void setDataAccessPoint(DataAccessPoint dataAccessPoint) {
//        this.dataAccessPoint = dataAccessPoint;
//    }

    public final DataService getDataService() {
        return dataService;
    }

//    public final void setDataService(DataService dataService) {
//        this.dataService = dataService;
//    }

    public final String getDatasetName() {
        return datasetName;
    }

//    public void setDatasetName(String datasetName) {
//        this.datasetName = datasetName;
//    }

    /** Reduce to a size that can be kept around for sometime.
     */
    public void minimize() {
        this.request = null;
        this.response = null;
        this.dsg = null;
        this.dataService = null;
        this.activeDSG = null;
        this.endpoint = null;
    }

    public void setStartTime() {
        if ( startTimeIsSet )
            Log.warn(this,  "Start time reset");
        startTimeIsSet = true;
        this.startTime = System.nanoTime();
    }

    /** Start time, in system nanos */
    public long getStartTime() {
        if ( ! startTimeIsSet )
            Log.warn(this,  "Start time is not set");
        return startTime;
    }

    /** Start time, in system nanos */
    public long getFinishTime() {
        if ( ! finishTimeIsSet )
            Log.warn(this,  "Finish time is not set");
        return finishTime;
    }

    public void setFinishTime() {
        if ( finishTimeIsSet )
            Log.warn(this,  "Finish time reset");
        finishTimeIsSet = true;
        this.finishTime = System.nanoTime();
    }

    /**
     * Return the recorded time taken in milliseconds. {@link #setStartTime} and
     * {@link #setFinishTime} must have been called.
     */
    public long getTime() {
        if ( !startTimeIsSet )
            Log.warn(this,  "Start time not set");
        if ( ! finishTimeIsSet )
            Log.warn(this,  "Finish time not set");
        return (finishTime-startTime)/(1000*1000);
    }

    public void sync() {
        SystemARQ.sync(dsg);
    }

    @Override
    public String toString() {
        return request.getMethod()+" "+request.getRequestURL().toString();
    }

    // ---- Request - response abstraction.

    public String getMethod()                           { return request.getMethod(); }

    public HttpServletRequest getRequest()              { return request; }
    public HttpServletResponse getResponse()            { return response; }

    // ---- Request accessors

    public String getRequestParameter(String string) {
        return request.getParameter(string);
    }

    public Enumeration<String> getRequestParameterNames() {
        return request.getParameterNames();
    }

    public String[] getRequestParameterValues(String name) {
        return request.getParameterValues(name);
    }

    public Map<String, String[]> getRequestParameterMap() {
        return request.getParameterMap();
    }

    public String getRequestMethod() {
        return request.getMethod();
    }

    public Enumeration<String> getRequestHeaderNames() {
        return request.getHeaderNames();
    }

    public String getRequestHeader(String name) {
        return request.getHeader(name);
    }

    public Enumeration<String> getRequestHeaders(String name) {
        return request.getHeaders(name);
    }

    public String getRequestContentType() {
        return request.getContentType();
    }

    public String getRequestCharacterEncoding() {
        return request.getCharacterEncoding();
    }

    public int getRequestContentLength() {
        return request.getContentLength();
    }

    public long getRequestContentLengthLong() {
        return request.getContentLengthLong();
    }

    private InputStream inputStream = null;

    public InputStream getRequestInputStream() throws IOException {
        if ( inputStream == null )
            inputStream = getInputStreamOneTime(this);
        return inputStream;
    }

    /** Get the request input stream, bypassing any compression.
     * The state of the input stream is unknown.
     * Only useful for skipping a body on a connection.
     * @throws IOException
     */
    public InputStream getRequestInputStreamRaw() throws IOException {
        return request.getInputStream();
    }

    public String getRequestQueryString() {
        return request.getQueryString();
    }

    public String getRequestRequestURI() {
        return request.getRequestURI();
    }

    public StringBuffer getRequestRequestURL() {
        return request.getRequestURL();
    }

    public String getRequestPathInfo() {
        return request.getPathInfo();
    }

    public String getRequestServletPath() {
        return request.getServletPath();
    }

    public int getRequestLocalPort() {
        return request.getLocalPort();
    }

    // ---- Response setters

    public void setResponseCharacterEncoding(String charset) {
        response.setCharacterEncoding(charset);
    }

    /**
     * Get the output stream for the response, respecting "Accept-Encoding" in the
     * request. This function sets the "Content-Encoding" header of the response if
     * compression is added.
     * @implNote
     * This most be called only once per response is encoding
     * is used because GZIPOutputStream adds header information when created.
     */
    private static OutputStream __getOutputStreamOneTime(HttpAction action) {
        try {
            OutputStream output = action.response.getOutputStream();
            // Ignore requests to encode response.
            if ( true )
                return output;
            // This does not work.
            // It requires:
            // * better handling so that the Content-Encoding is "chunked, gzip", not "gzip", when
            //   combined with streaming. Jetty seems to lose this only sending "chunked".
            // * The client to cooperate.
            // It is of dubious value to gzip one-off responses.
            // Also needs to work with Content-Length
            throw new NotImplemented("Content-Encoding: chunked, gzip");

//            String encoding = action.request.getHeader(HttpNames.hAcceptEncoding);
//            if ( encoding == null )
//                return output;
//            String[] options = ActionLib.splitOnComma(encoding);
//            if ( ActionLib.splitContains(options, WebContent.encodingGzip) ) {
//                action.response.setHeader(HttpNames.hContentEncoding, WebContent.encodingGzip);
//                // This must be closed to finish the compression.
//                // flush is not enough.
//                return new GZIPOutputStream(output, 8192);
//            }
//            if ( ActionLib.splitContains(options, WebContent.encodingDeflate) ) {
//                action.response.setHeader(HttpNames.hContentEncoding, WebContent.encodingDeflate);
//                return new DeflaterOutputStream(output);
//            }
//            // "compress" is legacy - ignore.
//            // Bad. Log and continue with no added encoding.
//            String msg = HttpNames.hAcceptEncoding+" '"+encoding+"' encoding not supported";
//            action.log.warn(msg);
//            return output;
        } catch (IOException ex) { IO.exception(ex); return null; }
    }

    public void setResponseContentType(String ct) {
        response.setContentType(ct);
    }

    public void setResponseContentLength(int length) {
        response.setContentLength(length);
    }

    public void setResponseContentLengthLong(long length) {
        response.setContentLengthLong(length);
    }

    public void setResponseHeader(String name, String value) {
        response.setHeader(name, value);
    }

    public void setResponseStatus(int statusCode) {
        response.setStatus(statusCode);
    }

    public ServletOutputStream getResponseOutputStream() throws IOException {
        return response.getOutputStream();
    }

    public PrintWriter getResponseWriter() throws IOException {
        return response.getWriter();
    }

    /**
     * Get the InputStream for the request, adding a compression decoder if the
     * "Content-Encoding" is set in the request. Responds 400 if the encoding is not
     * one that the server understands.
     */
    private static InputStream getInputStreamOneTime(HttpAction action) throws IOException {
        InputStream input = action.request.getInputStream();
        String encoding = action.request.getHeader(HttpNames.hContentEncoding);
        if ( encoding == null )
            return input;
        switch (encoding) {
            case WebContent.encodingGzip :
                return new GZIPInputStream(input, 8192);
            case WebContent.encodingDeflate :
                return new DeflaterInputStream(input);
                // Not supported:
            case "br" :
                // From Apache Common Compress but needs extra org.brotli.dec.BrotliInputStream
            case "compress" :
                // Legacy - not supported
            default :
        }
        // Not supported or not understood.
        ServletOps.error(HttpSC.BAD_REQUEST_400, HttpNames.hContentEncoding+" '"+encoding+"' encoding not supported");
        return null;
    }
}
