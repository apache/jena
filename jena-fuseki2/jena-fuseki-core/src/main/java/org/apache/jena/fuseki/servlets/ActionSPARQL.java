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

import static java.lang.String.format;
import static org.apache.jena.fuseki.server.CounterName.Requests ;
import static org.apache.jena.fuseki.server.CounterName.RequestsBad ;
import static org.apache.jena.fuseki.server.CounterName.RequestsGood ;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.jena.atlas.RuntimeIOException ;
import org.apache.jena.fuseki.Fuseki ;
import org.apache.jena.fuseki.server.* ;
import org.apache.jena.query.QueryCancelledException ;
import org.apache.jena.web.HttpSC;

/** SPARQL request lifecycle */
public abstract class ActionSPARQL extends ActionBase
{
    private static final long serialVersionUID = 8655400764034493574L;

    protected ActionSPARQL() { super(Fuseki.actionLog) ; }
    
    protected abstract void validate(HttpAction action) ;
    protected abstract void perform(HttpAction action) ;

    /**
     * Executes common tasks, including mapping the request to the right dataset, setting the dataset into the HTTP
     * action, and retrieving the service for the dataset requested. Finally, it calls the
     * {@link #executeAction(HttpAction)} method, which executes the HTTP Action life cycle.
     * @param action HTTP Action
     */
    @Override
    protected void execCommonWorker(HttpAction action) {
        DataAccessPoint dataAccessPoint ;
        DataService dSrv ;
        
        String datasetUri = mapRequestToDataset(action) ;
        if ( datasetUri != null ) {
            dataAccessPoint = action.getDataAccessPointRegistry().get(datasetUri) ;
            if ( dataAccessPoint == null ) {
                ServletOps.errorNotFound("No dataset for URI: "+datasetUri) ;
                return ;
            }
            dSrv = dataAccessPoint.getDataService() ;
            if ( ! dSrv.isAcceptingRequests() ) {
                ServletOps.error(HttpSC.SERVICE_UNAVAILABLE_503, "Dataset not currently active");
                return ;
            }
        } else {
            dataAccessPoint = null ;
            dSrv = DataService.serviceOnlyDataService() ;
        }

        action.setRequest(dataAccessPoint, dSrv) ;
        String endpointName = mapRequestToOperation(action, dataAccessPoint) ;
        
        if ( true ) {
            // New dispatch
            OperationName opName = null;
            if ( ! endpointName.isEmpty() ) {
                 opName = chooseEndpoint(action, dSrv, endpointName);
                if ( opName == null )
                    ServletOps.errorNotFound(format("dataset=%s, service=%s", dataAccessPoint.getName(), endpointName));
                
            } else {
                opName = chooseEndpoint(action, dSrv);
                if ( opName == null )
                    ServletOps.errorBadRequest(format("dataset=%s",  dataAccessPoint.getName()));
            }
            
            ActionSPARQL handler = Dispatch.OpNameToHandler.get(opName);
            // XXX -- replace action.setEndpoint
            Endpoint ep = dSrv.getEndpoint(endpointName) ;
            //List<Endpoint> endpoints = dSrv.getOperation(opName);
            action.setEndpoint(ep, endpointName);
            handler.executeLifecycle(action);
            return ;
        }
        
        // Old dispatch via SPARQL_UberServlet.executeAction override.
        Endpoint op = dSrv.getEndpoint(endpointName) ;
        action.setEndpoint(op, endpointName);
        executeAction(action) ;
    }

    // These are overridden by the ServiceRouterServlet.
    protected OperationName chooseEndpoint(HttpAction action, DataService dataService, String serviceName) {
        Endpoint ep = dataService.getEndpoint(serviceName) ;
        OperationName opName = ep.getOperationName();
        return opName;
    }
    
    protected OperationName chooseEndpoint(HttpAction action, DataService dataService) {
        // No default implementation for directly bound services operation servlets.
        return null;
    }

    private void executeRequest(HttpAction action, ActionSPARQL servlet) {
        if ( true ) {
            // Execute an ActionSPARQL.
            // Bypasses HttpServlet.service to doMethod dispatch.
            servlet.executeLifecycle(action) ;
            return ;
        }
        if ( false )  {
            // Execute by calling the whole servlet mechanism.
            // This causes HttpServlet.service to call the appropriate doMethod.
            // but the action, and the id, are not passed on and a ne one is created.
            try { servlet.service(action.request, action.response) ; }
            catch (ServletException | IOException e) {
                ServletOps.errorOccurred(e);
            }
        }
    }

    
    /** Execute a SPARQL request. Statistics have not been adjusted at this point.
     * 
     * @param action
     */
    protected void executeAction(HttpAction action) {
        executeLifecycle(action) ;
    }
    
    /** 
     * Standard execution lifecycle for a SPARQL Request.
     * <ul>
     * <li>{@link #startRequest(HttpAction)}</li>
     * <li>initial statistics,</li>
     * <li>{@link #validate(HttpAction)} request,</li>
     * <li>{@link #perform(HttpAction)} request,</li>
     * <li>completion/error statistics,</li>
     * <li>{@link #finishRequest(HttpAction)}
     * </ul>
     * 
     * @param action
     */
    // This is the service request lifecycle.
    final
    protected void executeLifecycle(HttpAction action) {
        startRequest(action) ;
        // And also HTTP counter
        CounterSet csService = action.getDataService().getCounters() ;
        CounterSet csOperation = null ;
        if ( action.getEndpoint() != null )
            // Direct naming GSP does not have an "endpoint".
            csOperation = action.getEndpoint().getCounters() ;
        
        incCounter(csService, Requests) ;
        incCounter(csOperation, Requests) ;
        try {
            // Either exit this via "bad request" on validation
            // or in execution in perform. 
            try {
                validate(action) ;
            } catch (ActionErrorException ex) {
                incCounter(csOperation, RequestsBad) ;
                incCounter(csService, RequestsBad) ;
                throw ex ;
            }

            try {
                perform(action) ;
                // Success
                incCounter(csOperation, RequestsGood) ;
                incCounter(csService, RequestsGood) ;
            } catch (ActionErrorException | QueryCancelledException | RuntimeIOException ex) {
                incCounter(csOperation, RequestsBad) ;
                incCounter(csService, RequestsBad) ;
                throw ex ;
            }
        } finally {
            finishRequest(action) ;
        }
    }
    
    /**
     * Map request {@link HttpAction} to uri in the registry.
     * A return of {@code null} means no mapping done (passthrough).
     * @param uri the URI
     * @return the dataset
     */
    protected String mapRequestToDataset(HttpAction action) {
        return ActionLib.mapRequestToDataset(action) ;
    }

    /**
     * Map request to uri in the registry. {@code null} means no mapping done
     * (passthrough).
     */
    protected String mapRequestToOperation(HttpAction action, DataAccessPoint dataAccessPoint) {
        return ActionLib.mapRequestToOperation(action, dataAccessPoint) ;
    }

    /** Increment counter */
    protected static void incCounter(Counters counters, CounterName name) {
        if ( counters == null ) return ;
        incCounter(counters.getCounters(), name) ; 
    }
    
    /** Decrement counter */
    protected static void decCounter(Counters counters, CounterName name) {
        if ( counters == null ) return ;
        decCounter(counters.getCounters(), name) ; 
    }

    protected static void incCounter(CounterSet counters, CounterName name) {
        if ( counters == null )
            return ;
        try {
            if ( counters.contains(name) )
                counters.inc(name) ;
        } catch (Exception ex) {
            Fuseki.serverLog.warn("Exception on counter inc", ex) ;
        }
    }
    
    protected static void decCounter(CounterSet counters, CounterName name) {
        if ( counters == null )
            return ;
        try {
            if ( counters.contains(name) )
                counters.dec(name) ;
        } catch (Exception ex) {
            Fuseki.serverLog.warn("Exception on counter dec", ex) ;
        }
    }
}
