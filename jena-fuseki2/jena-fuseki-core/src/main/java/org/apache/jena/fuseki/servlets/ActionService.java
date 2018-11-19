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
import static org.apache.jena.fuseki.server.CounterName.Requests;
import static org.apache.jena.fuseki.server.CounterName.RequestsBad;
import static org.apache.jena.fuseki.server.CounterName.RequestsGood;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.ServletException;

import org.apache.jena.atlas.RuntimeIOException;
import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.auth.Auth;
import org.apache.jena.fuseki.server.*;
import org.apache.jena.query.QueryCancelledException;
import org.apache.jena.web.HttpSC;

/** Service request lifecycle */
public abstract class ActionService extends ActionBase {
    protected ActionService() {
        super(Fuseki.actionLog);
    }

    protected abstract void validate(HttpAction action);

    protected abstract void perform(HttpAction action);

    /**
     * Executes common tasks, including mapping the request to the right dataset, setting
     * the dataset into the HTTP action, and retrieving the service for the dataset
     * requested. Finally, it calls the {@link #executeAction(HttpAction)} method, which
     * executes the HTTP Action life cycle.
     */
    @Override
    final
    protected void execCommonWorker(HttpAction action) {
        DataAccessPoint dataAccessPoint;
        DataService dSrv;

        String datasetUri = mapRequestToDataset(action);
        if ( datasetUri != null ) {
            dataAccessPoint = action.getDataAccessPointRegistry().get(datasetUri);
            
            if ( dataAccessPoint == null ) {
                ServletOps.errorNotFound("No dataset for URI: " + datasetUri);
                return;
            }
            dSrv = dataAccessPoint.getDataService();

            if ( !dSrv.isAcceptingRequests() ) {
                ServletOps.error(HttpSC.SERVICE_UNAVAILABLE_503, "Dataset not currently active");
                return;
            }
        } else {
            // Routed to this URL; no registered dataset on this URL.
            // e.g. General query servlet
            dSrv = ServiceOnly.dataService();
            dataAccessPoint = ServiceOnly.dataAccessPoint();
        }

        action.setRequest(dataAccessPoint, dSrv);
        // Endpoint Name is "" for GSP or quads.
        // Endpoint name is not "", but unknown for GSP direct naming (which is usually disabled).
        String endpointName = mapRequestToOperation(action, dataAccessPoint);
        
        // ServiceRouter dispatch
        Operation operation = null;
        if ( !endpointName.isEmpty() ) {
            operation = chooseOperation(action, dSrv, endpointName);
            if ( operation == null )
                if ( ! Fuseki.GSP_DIRECT_NAMING ) 
                    ServletOps.errorBadRequest(format("dataset=%s, service=%s", dataAccessPoint.getName(), endpointName));
                else
                    throw new InternalErrorException("Inconsistent: GSP_DIRECT_NAMING but no operation");
        } else {
            // Endpoint ""
            operation = chooseOperation(action, dSrv);
            if ( operation == null )
                ServletOps.errorBadRequest(format("dataset=%s", dataAccessPoint.getName()));
        }

        // ---- Auth checking.
        // -- Server-level auhtorization.
        // Checking was carried out by servlet filter AuthFilter.
        // Need to check Data service and endpoint authorization policies.
        String user = action.getUser();
        // -- Data service level authorization
        if ( dSrv.authPolicy() != null ) {
            if ( ! dSrv.authPolicy().isAllowed(user) )
                ServletOps.errorForbidden();
        }
        
        // -- Endpoint level authorization
        // Make sure all contribute authentication.
        if ( action.getEndpoint() != null ) {
            // Specific endpoint chosen.
            Auth.allow(user, action.getEndpoint().getAuthPolicy(), ServletOps::errorForbidden);
        } else {
            // No Endpoint name given; there may be several endpoints for the operation.
            // authorization is the AND of all endpoints.
            Collection<Endpoint> x = getEndpoints(dSrv, operation);
            if ( x.isEmpty() )
                throw new InternalErrorException("Inconsistent: no endpoints for "+operation);
            x.forEach(ep->{
                Auth.allow(user, ep.getAuthPolicy(), ServletOps::errorForbidden);
            });
        }
        // ---- End auth checking.

        ActionService handler = action.getServiceDispatchRegistry().findHandler(operation);
        if ( handler == null )
            ServletOps.errorBadRequest(format("dataset=%s: op=%s", dataAccessPoint.getName(), operation.getName()));
        handler.executeLifecycle(action);
        return;
    }

    // Find the endpoints for an operation.
    // This is GSP_R/GSP_RW and Quads_R/Quads_RW aware.
    // If asked for GSP_R and there are no endpoints for GSP_R, try GSP_RW.
    // Ditto Quads_R -> Quads_RW.
    private Collection<Endpoint> getEndpoints(DataService dSrv, Operation operation) {
        Collection<Endpoint> x = dSrv.getEndpoints(operation);
        if ( x == null || x.isEmpty() ) {
            if ( operation == Operation.GSP_R )
                x = dSrv.getEndpoints(Operation.GSP_RW);
            else if ( operation == Operation.Quads_R )
                x = dSrv.getEndpoints(Operation.Quads_RW);
        }
        return x;
    }
    
    /**
     * Return the operation that corresponds to the endpoint name for a given data service.
     * Side effect: This operation should set the selected endpoint in the HttpAction
     * if this operation is determined to be a specific endpoint.
     */
    protected Operation chooseOperation(HttpAction action, DataService dataService, String endpointName) {
        // Overridden by the ServiceRouter.
        // This default implementation is plain service name to operation based on the
        // DataService as would be used by operation servlets bound by web.xml
        // except Fuseki can add and delete mapping while running.
        Endpoint ep = dataService.getEndpoint(endpointName);
        Operation operation = ep.getOperation();
        action.setEndpoint(ep);
        return operation;
    }

    /**
     * Return the operation that corresponds to the request when there is no endpoint name. 
     * This operation does not set the selected endpoint in the HttpAction.
     */
    protected Operation chooseOperation(HttpAction action, DataService dataService) {
        // No default implementation for directly bound services operation servlets.
        return null;
    }

    private void executeRequest(HttpAction action, ActionService servlet) {
        if ( true ) {
            // Execute an ActionService.
            // Bypasses HttpServlet.service to doMethod dispatch.
            servlet.executeLifecycle(action);
            return;
        }
        if ( false ) {
            // Execute by calling the whole servlet mechanism.
            // This causes HttpServlet.service to call the appropriate doMethod.
            // but the action, and the id, are not passed on and a ne one is created.
            try {
                servlet.service(action.request, action.response);
            }
            catch (ServletException | IOException e) {
                ServletOps.errorOccurred(e);
            }
        }
    }

    /**
     * Execute a SPARQL request. Statistics have not been adjusted at this point.
     * 
     * @param action
     */
    protected void executeAction(HttpAction action) {
        executeLifecycle(action);
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
    final protected void executeLifecycle(HttpAction action) {
        // And also HTTP counter
        CounterSet csService = action.getDataService().getCounters();
        CounterSet csOperation = null;
        if ( action.getEndpoint() != null )
            // Direct naming GSP does not have an "endpoint".
            csOperation = action.getEndpoint().getCounters();

        incCounter(csService, Requests);
        incCounter(csOperation, Requests);
        // Either exit this via "bad request" on validation
        // or in execution in perform.
        try {
            validate(action);
        }
        catch (ActionErrorException ex) {
            incCounter(csOperation, RequestsBad);
            incCounter(csService, RequestsBad);
            throw ex;
        }

        try {
            perform(action);
            // Success
            incCounter(csOperation, RequestsGood);
            incCounter(csService, RequestsGood);
        }
        catch (ActionErrorException | QueryCancelledException | RuntimeIOException ex) {
            incCounter(csOperation, RequestsBad);
            incCounter(csService, RequestsBad);
            throw ex;
        }
    }

    /**
     * Map request {@link HttpAction} to uri in the registry. A return of {@code null}
     * means no mapping done (passthrough).
     * 
     * @param uri
     *            the URI
     * @return the dataset
     */
    protected String mapRequestToDataset(HttpAction action) {
        return ActionLib.mapRequestToDataset(action);
    }

    /**
     * Map request to uri in the registry. {@code null} means no mapping done
     * (passthrough).
     */
    protected String mapRequestToOperation(HttpAction action, DataAccessPoint dataAccessPoint) {
        return ActionLib.mapRequestToOperation(action, dataAccessPoint);
    }

    /** Increment counter */
    protected static void incCounter(Counters counters, CounterName name) {
        if ( counters == null )
            return;
        incCounter(counters.getCounters(), name);
    }

    /** Decrement counter */
    protected static void decCounter(Counters counters, CounterName name) {
        if ( counters == null )
            return;
        decCounter(counters.getCounters(), name);
    }

    protected static void incCounter(CounterSet counters, CounterName name) {
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

    protected static void decCounter(CounterSet counters, CounterName name) {
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
