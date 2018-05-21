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

import static org.apache.jena.query.ReadWrite.READ ;
import static org.apache.jena.query.ReadWrite.WRITE ;

import java.util.HashMap ;
import java.util.Map ;

import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.fuseki.Fuseki ;
import org.apache.jena.fuseki.FusekiException ;
import org.apache.jena.fuseki.server.* ;
import org.apache.jena.query.ReadWrite ;
import org.apache.jena.sparql.SystemARQ ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.DatasetGraphWrapper ;
import org.apache.jena.sparql.core.Transactional ;
import org.apache.jena.sparql.core.TransactionalLock ;
import org.apache.jena.sparql.util.Context;
import org.slf4j.Logger ;

/**
 * HTTP action that represents the user request lifecycle. Its state is handled in the
 * {@link ActionService#executeAction(HttpAction)} method.
 */
public class HttpAction
{
    public final long id ;
    public final boolean verbose ;
    public final Logger log ;
    
    // ----
    // Worth subclassing? Given this is allocated in the general lifecycle
    // it would mean there are downcasts to the specific type.
    
    // -- Valid only for operational actions (e.g. SPARQL).
    
    public  String          endpointName    = null ;        // Endpoint name srv was found under 
    public  Endpoint        endpoint        = null ;
    private Transactional   transactional   = null ;
    private boolean         isTransactional = false ;
    private DatasetGraph    activeDSG       = null ;        // Set when inside begin/end.
    private ReadWrite       activeMode      = null ;        // Set when inside begin/end.
    
    // -- Valid only for administration actions.
    
    // -- Shared items (but exact meaning may differ)
    /** Handle to dataset+services being acted on (maybe null) */
    private DataAccessPoint dataAccessPoint = null ;
    private DataService dataService         = null ;
    private String datasetName              = null ;        // Dataset URI used (e.g. registry)
    private DatasetGraph dsg                = null ;
    private Context context                 = null ;

    // ----
    
    private boolean startTimeIsSet = false ;
    private boolean finishTimeIsSet = false ;

    private long startTime = -2 ;
    private long finishTime = -2 ;
    
    // Outcome.
    public int statusCode = -1 ;
    public String message = null ;
    public int responseContentLength = -1 ;
    public String responseContentType = null ;
    
    // Cleared to archive:
    public Map <String, String> headers = new HashMap<>() ;
    public HttpServletRequest request;
    public HttpServletResponseTracker response ;
    private final String actionURI ;
    private final String contextPath ;

    private final ServiceDispatchRegistry serviceDispatchRegistry;
    private final DataAccessPointRegistry dataAccessPointRegistry ;
    
    /**
     * Creates a new HTTP Action, using the HTTP request and response, and a given ID.
     *
     * @param id given ID
     * @param log Logger for this action 
     * @param request HTTP request
     * @param response HTTP response
     */
    public HttpAction(long id, Logger log, HttpServletRequest request, HttpServletResponse response) {
        this.id = id ;
        this.log = log ;
        this.request = request ;
        this.response = new HttpServletResponseTracker(this, response) ;
        // Should this be set when setDataset is called from the dataset context?
        // Currently server-wide, e.g. from the command line.
        this.verbose = Fuseki.getVerbose(request.getServletContext()); 
        this.contextPath = request.getServletContext().getContextPath() ;
        this.actionURI = ActionLib.actionURI(request) ;
        this.serviceDispatchRegistry = ServiceDispatchRegistry.get(request.getServletContext()) ;
        this.dataAccessPointRegistry = DataAccessPointRegistry.get(request.getServletContext()) ;
    }

    /** Initialization after action creation during lifecycle setup.
     * <p>Sets the action dataset. Setting will replace any existing {@link DataAccessPoint} and {@link DataService},
     * as the {@link DatasetGraph} of the current HTTP Action.</p>
     *
     * <p>Once it has updated its members, the HTTP Action will change its transactional state and
     * {@link Transactional} instance according to its base dataset graph.</p>
     *
     * @param dataAccessPoint {@link DataAccessPoint}
     * @param dService {@link DataService}
     * @see Transactional
     */
    
    public void setRequest(DataAccessPoint dataAccessPoint, DataService dService) {
        this.dataAccessPoint = dataAccessPoint ;
        if ( dataAccessPoint != null )
            this.datasetName = dataAccessPoint.getName() ; 

        if ( this.dataService != null )
            throw new FusekiException("Redefinition of DatasetRef in the request action") ;
        
        this.dataService = dService ;
        if ( dService == null || dService.getDataset() == null )
            // Null does not happens for service requests, (it does for admin requests - call setControlRequest) 
            throw new FusekiException("Null DataService in the request action") ;
        setDataset(dService.getDataset()) ;
    }
    
    public void setControlRequest(DataAccessPoint dataAccessPoint, String datasetUri) {
        this.dataAccessPoint = dataAccessPoint ;
        this.dataService = null ;
        if ( dataAccessPoint != null )
            this.dataService = dataAccessPoint.getDataService() ;
        this.datasetName = datasetUri ;
        if ( dataService != null )
            setDataset(dataAccessPoint.getDataService().getDataset()) ; 
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
        this.dsg = dsg ;
        this.context = Context.mergeCopy(Fuseki.getContext(), dsg.getContext());
        if ( dsg == null )
            return ;
        setTransactionalPolicy(dsg) ;
    }
    
    private void setTransactionalPolicy(DatasetGraph dsg) {
        if ( dsg.supportsTransactionAbort() ) {
            // Use transactional if it looks safe - abort is necessary.
            transactional = dsg ;
            isTransactional = true ;
        } else if ( dsg.supportsTransactions() ) {
            // No abort - e.g. loading data needs buffering against syntax errors.
            transactional = dsg ;
            isTransactional = false ;
        } else {
            // Nothing to build on.  Be safe. 
            transactional = TransactionalLock.createMutex() ;
            isTransactional = false ;
        }
    }
    
    /** Return the dataset, if any (may be null) */
    public DatasetGraph getDataset() {
        return dsg ;
    }

    /** Return the Context for this {@code HttpAction}. */
    public Context getContext() {
        return context ;
    }

    /**
     * Return the "Transactional" for this HttpAction.
     */
    
    public Transactional getTransactional() {
        return transactional ;
    }

    /**
     * A {@link DatasetGraph} may contain other <strong>wrapped DatasetGraph's</strong>. This method will return
     * the first instance (including the argument to this method) that <strong>is not</strong> an instance of
     * {@link DatasetGraphWrapper}.
     *
     * @param dsg a {@link DatasetGraph}
     * @return the first found {@link DatasetGraph} that is not an instance of {@link DatasetGraphWrapper}
     */
    // Unused currently.
   private static DatasetGraph x_unwrap(DatasetGraph dsg) {
       if ( dsg instanceof DatasetGraphWrapper)
            dsg = ((DatasetGraphWrapper)dsg).getBase() ;
        return dsg ;
    }
        
    /** This is the requestURI with the context path removed.
     *  It should be used internally for dispatch.
     */
    public String getActionURI() {
        return actionURI ;
    }
    
    /** Get the context path.
     */
    public String getContextPath() {
        return contextPath ;
    }
    
    /**
     * Get the ServiceRegistry for this action
     */
    public ServiceDispatchRegistry getServiceDispatchRegistry() {
        return serviceDispatchRegistry ;
    }

    /**
     * Get the DataAccessPointRegistry for this action
     */
    public DataAccessPointRegistry getDataAccessPointRegistry() {
        return dataAccessPointRegistry ;
    }
    
    /** Set the endpoint and endpoint name that this is an action for. 
     * @param srvRef {@link Endpoint}
     * @param endpointName
     */
    public void setEndpoint(Endpoint srvRef, String endpointName) {
        this.endpoint = srvRef ; 
        this.endpointName = endpointName ;
    }
    
    /** Get the endpoint for the action (may be null) . */
    public Endpoint getEndpoint() {
        return endpoint ; 
    }

    /**
     * Returns whether or not the underlying DatasetGraph is fully transactional (supports rollback)
     */
    public boolean isTransactional() {
        return isTransactional ;
    }

    public void beginRead() {
        activeMode = READ ;
        transactional.begin(READ) ;
        activeDSG = dsg ;
        dataService.startTxn(READ) ;
    }

    public void endRead() {
        dataService.finishTxn(READ) ;
        activeMode = null ;
        transactional.end() ;
        activeDSG = null ;
    }

    public void beginWrite() {
        transactional.begin(WRITE) ;
        activeMode = WRITE ;
        activeDSG = dsg ;
        dataService.startTxn(WRITE) ;
    }

    public void commit() {
        transactional.commit() ;
        activeDSG = null ;
    }

    public void abort() {
        try { transactional.abort() ; } 
        catch (Exception ex) {
            // Some datasets claim to be transactional but
            // don't provide a real abort. We tried to avoid
            // them earlier but even if they sneek through,
            // we try to continue server operation.
            Log.warn(this, "Exception during abort (operation attempts to continue): "+ex.getMessage()) ; 
        }
        activeDSG = null ;
    }

    public void endWrite() {
        dataService.finishTxn(WRITE) ;
        activeMode = null ;

        if ( transactional.isInTransaction() ) {
            Log.warn(this, "Transaction still active in endWriter - no commit or abort seen (forced abort)") ;
            try {
                transactional.abort() ;
            } catch (RuntimeException ex) {
                Log.warn(this, "Exception in forced abort (trying to continue)", ex) ;
            }
        }
        transactional.end() ;
        activeDSG = null ;
    }

    public final void startRequest() { 
        if ( dataAccessPoint != null ) 
            dataAccessPoint.startRequest(this) ;
    }

    public final void finishRequest() { 
        if ( dataAccessPoint != null ) 
            dataAccessPoint.finishRequest(this) ;
        // Standard logging goes here.
        if ( Fuseki.requestLog != null && Fuseki.requestLog.isInfoEnabled() ) { 
            String s = RequestLog.combinedNCSA(this) ;
            Fuseki.requestLog.info(s);
        }
    }
    
    /** If inside the transaction for the action, return the active {@link DatasetGraph},
     *  otherwise return null.
     * @return Current active {@link DatasetGraph}
     */
    public final DatasetGraph getActiveDSG() {
        return activeDSG ;
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
     * Release resources like datasets that may be closed, reset etc.
     */
    public void minimize() {
        this.request = null ;
        this.response = null ;
        this.dsg = null ;
        this.dataService = null ;
        this.activeDSG = null ;
        this.endpoint = null ;
    }

    public void setStartTime() {
        if ( startTimeIsSet ) 
            Log.warn(this,  "Start time reset") ;
        startTimeIsSet = true ;
        this.startTime = System.nanoTime() ;
    }

    /** Start time, in system nanos */
    public long getStartTime() { 
        if ( ! startTimeIsSet ) 
            Log.warn(this,  "Start time is not set") ;
        return startTime ;
    }

    /** Start time, in system nanos */
    public long getFinishTime() { 
        if ( ! finishTimeIsSet ) 
            Log.warn(this,  "Finish time is not set") ;
        return finishTime ;
    }
    
    public void setFinishTime() {
        if ( finishTimeIsSet ) 
            Log.warn(this,  "Finish time reset") ;
        finishTimeIsSet = true ;
        this.finishTime = System.nanoTime() ;
    }

    public String getMethod()                           { return request.getMethod() ; }

    public HttpServletRequest getRequest()              { return request ; }

    public HttpServletResponseTracker getResponse()     { return response ; }
    
    /** Return the recorded time taken in milliseconds. 
     *  {@link #setStartTime} and {@link #setFinishTime}
     *  must have been called.
     */
    public long getTime()
    {
        if ( ! startTimeIsSet ) 
            Log.warn(this,  "Start time not set") ;
        if ( ! finishTimeIsSet ) 
            Log.warn(this,  "Finish time not set") ;
        return (finishTime-startTime)/(1000*1000) ;
    }

    public void sync() {
        SystemARQ.sync(dsg) ;
    }
}
