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
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.jena.fuseki.server.Operation.*;
import static org.apache.jena.fuseki.servlets.ActionExecLib.allocHttpAction;

import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.auth.Auth;
import org.apache.jena.fuseki.server.*;
import org.apache.jena.riot.web.HttpNames;
import org.apache.jena.web.HttpSC;
import org.slf4j.Logger;

/**
 * Dispatch on registered datasets. This is the entry point into Fuseki for dataset
 * operations.
 *
 * Administration operations, and directly registered servlets and static content are
 * called through the usual web server process.
 *
 * HTTP Request URLs, after servlet context removed, take the form {@code /dataset} or {@code /dataset/service}.
 * The most general URL is {@code /context/dataset/service}.
 * The {@link DataAccessPointRegistry} maps {@code /dataset} to a {@link DataAccessPoint}.
 */
public class Dispatcher {

    // Development debugging only. Excessive for normal operation.
    private static final boolean LogDispatch = false;
    private static Logger        LOG         = Fuseki.serverLog;

    /**
     * Handle an HTTP request if it is sent to a registered dataset.
     *
     * Fuseki uses dynamic dispatch, the set of registered datasets can change while
     * the server is running, so dispatch is driven off Fuseki system registries.
     *
     * If the request URL matches a registered dataset, process the request, and send
     * the response.
     * 
     * This function is called by {@link FusekiFilter#doFilter}.
     *
     * @param request
     *            HttpServletRequest
     * @param response
     *            HttpServletResponse
     * @return Returns {@code true} if the request has been handled, else false (no
     *         response sent).
     */
    public static boolean dispatch(HttpServletRequest request, HttpServletResponse response) {
        // Path component of the URI, without context path
        String uri = ActionLib.actionURI(request);
        String datasetUri = ActionLib.mapActionRequestToDataset(uri);

        if ( LogDispatch ) {
            LOG.info("Filter: Request URI = " + request.getRequestURI());
            LOG.info("Filter: Action URI  = " + uri);
            LOG.info("Filter: Dataset URI = " + datasetUri);
        }

        if ( datasetUri == null )
            return false;

        DataAccessPointRegistry registry = DataAccessPointRegistry.get(request.getServletContext());
        if ( !registry.isRegistered(datasetUri) ) {
            if ( LogDispatch )
                LOG.debug("No dispatch for '"+datasetUri+"'");
            return false;
        }
        DataAccessPoint dap = registry.get(datasetUri);
        process(dap, request, response);
        return true;
    }

    /** Set up and handle a HTTP request for a dataset. */
    private static void process(DataAccessPoint dap, HttpServletRequest request, HttpServletResponse response) {
        HttpAction action = allocHttpAction(dap, Fuseki.actionLog, request, response);
        dispatchAction(action);
    }

    /**
     * Determine and call the {@link ActionProcessor} to handle this
     * {@link HttpAction}, including access control at the dataset and service levels.
     */
    public static void dispatchAction(HttpAction action) {
        ActionExecLib.execAction(action, ()->chooseProcessor(action));
    }

    /**
     * Find the ActionProcessor or return null if there can't determine one. This
     * function does NOT return null; it throws ActionErrorException after sending an
     * HTTP error response.
     * 
     * Returning null indicates an error, and the HTTP response
     * has been done.
     */
    private static ActionProcessor chooseProcessor(HttpAction action) {
        // "return null" indicates that processing failed to find a ActionProcessor
        DataAccessPoint dataAccessPoint = action.getDataAccessPoint();
        DataService dataService = action.getDataService();

        if ( !dataService.isAcceptingRequests() ) {
            ServletOps.error(HttpSC.SERVICE_UNAVAILABLE_503, "Dataset not currently active");
            return null;
        }

        // ---- Determine Endpoint.
        String endpointName = mapRequestToOperation(action, dataAccessPoint);

        Endpoint ep = chooseEndpoint(action, dataService, endpointName);
        if ( ep == null ) {
            if ( isEmpty(endpointName) )
                ServletOps.errorBadRequest("No operation for request: "+action.getActionURI());
            else
                ServletOps.errorNotFound("No endpoint: "+action.getActionURI());
            return null;
        }

        Operation operation = ep.getOperation();
        if ( operation == null ) {
            ServletOps.errorNotFound("No operation: "+action.getActionURI());
            return null;
        }

        action.setEndpoint(ep);
        
        // ---- Authorization
        // -- Server-level authorization.
        // Checking was carried out by servlet filter AuthFilter.
        // Need to check Data service and endpoint authorization policies.
        String user = action.getUser();

        // -- Data service level authorization
        if ( dataService.authPolicy() != null ) {
            if ( ! dataService.authPolicy().isAllowed(user) )
                ServletOps.errorForbidden();
        }

        // -- Endpoint level authorization
        // Make sure all contribute authentication.
        Auth.allow(user, action.getEndpoint().getAuthPolicy(), ServletOps::errorForbidden);
        if ( isEmpty(endpointName) && ! ep.isUnnamed() ) {
            // [DISPATCH LEGACY]
            // If choice was by looking in all named endpoints for a unnamed endpoint
            // request, ensure all choices allow access.
            // There may be several endpoints for the operation.
            // Authorization is the AND of all endpoints.
            Collection<Endpoint> x = getEndpoints(dataService, operation);
            if ( x.isEmpty() )
                throw new InternalErrorException("Inconsistent: no endpoints for "+operation);
            x.forEach(ept->
                Auth.allow(user, ept.getAuthPolicy(), ServletOps::errorForbidden));
        }
        // ---- Authorization checking.

        // ---- Handler.
        // Decide the code to execute the request.
        // ActionProcessor handler = target(action, operation);
        ActionProcessor processor = target(action, operation);
        if ( processor == null )
            ServletOps.errorBadRequest(format("dataset=%s: op=%s", dataAccessPoint.getName(), operation.getName()));
        return processor;
    }

    // operation to code for operation.
    private static ActionProcessor target(HttpAction action, Operation operation) {
        return action.getOperationRegistry().findHandler(operation);
    }

    /**
     * Map request to operation name.
     * Returns the service name (the part after the "/" of the dataset part) or "".
     */
    protected static String mapRequestToOperation(HttpAction action, DataAccessPoint dataAccessPoint) {
        return ActionLib.mapRequestToOperation(action, dataAccessPoint);
    }

    // Find the endpoints for an operation.
    // This is GSP_R/GSP_RW and Quads_R/Quads_RW aware.
    // If asked for GSP_R and there are no endpoints for GSP_R, try GSP_RW.
    // Ditto Quads_R -> Quads_RW.
    private static Collection<Endpoint> getEndpoints(DataService dataService, Operation operation) {
        Collection<Endpoint> x = dataService.getEndpoints(operation);
        if ( x == null || x.isEmpty() ) {
            if ( operation == GSP_R )
                x = dataService.getEndpoints(GSP_RW);
        }
        return x;
    }

    /**
     * Choose an endpoint. This can be with or without endpointName.
     * If there is no endpoint and the action is on the data service itself (unnamed endpoint)
     * look for a named endpoint that supplies the operation.  
     */ 
    private static Endpoint chooseEndpoint(HttpAction action, DataService dataService, String endpointName) {
        Endpoint ep = chooseEndpointNoLegacy(action, dataService, endpointName);
        if ( ep != null )
            return ep;
        if ( ! isEmpty(endpointName) )
            return ep;
        // [DISPATCH LEGACY]
        Operation operation = chooseOperation(action);
        // Search for an endpoint that provides the operation.
        // No guarantee it has the access controls for the operation
        // but in this case, access control will validate against all possible endpoints.
        ep = findEndpointForOperation(action, dataService, operation, true);
        return ep;
    }

    /** Choose an endpoint. */
    private static Endpoint chooseEndpointNoLegacy(HttpAction action, DataService dataService, String endpointName) {
        EndpointSet epSet = isEmpty(endpointName) ? dataService.getEndpointSet() : dataService.getEndpointSet(endpointName);
        if ( epSet == null || epSet.isEmpty() )
            return null;
        // If there is one endpoint, dispatch there directly.
        Endpoint ep = epSet.getOnly();
        if ( ep != null )
            return ep;

//        if ( ep != null ) {
//            if ( ! isGSP(ep.getOperation()) )
//                return ep;
//            // GSP but if not valid, let it upgrade to quads.
//            if ( hasGSPParams(action) )
//                return ep;
//            ep = null;
//        }
        // No single direct dispatch.
        // Work out which operation we are looking for.
        Operation operation = chooseOperation(action);
        ep = epSet.get(operation);
        if ( ep != null )
            return ep;
        return null;
    }

   private static boolean isGSP(Operation operation) {
        return operation.equals(GSP_R) || operation.equals(GSP_RW);
    }

    private static boolean hasGSPParams(HttpAction action) {
        boolean hasParamGraphDefault = action.request.getParameter(HttpNames.paramGraphDefault) != null;
        if ( hasParamGraphDefault )
            return true;
        boolean hasParamGraph = action.request.getParameter(HttpNames.paramGraph) != null;
        if ( hasParamGraph )
            return true;
        return false;
    }

    /** Find an endpoint for an operation.
     *  This returns all endpoints of a {@link DataService} that provide the {@link Operation}. 
     *  This understands that Quads_RW can service Quads_R and GSP_RW can service GSP_R.
     */
    private static Endpoint findEndpointForOperation(HttpAction action, DataService dataService, Operation operation, boolean preferUnnamed) {
        Endpoint ep = findEndpointForOperationExact(dataService, operation, preferUnnamed);
        if ( ep != null )
            return ep;
        // [DISPATCH LEGACY]
        // Try to find "R" functionality from an RW. 
        if ( GSP_R.equals(operation) ) 
            return findEndpointForOperationExact(dataService, GSP_RW, preferUnnamed);
        // Instead of 404, return 405 if asked for RW but only R available.
        if ( GSP_RW.equals(operation) && dataService.hasOperation(GSP_R) )
            ServletOps.errorMethodNotAllowed(action.getMethod());
        return null;
    }

    /** Find a matching endpoint for exactly this operation. */ 
    private static Endpoint findEndpointForOperationExact(DataService dataService, Operation operation, boolean preferUnnamed) {
        List<Endpoint> eps = dataService.getEndpoints(operation);
        if ( eps == null || eps.isEmpty() )
            return null;
        // ==== Legacy compatibility.
        // Find a named service if an unnamed one is not available.
        Endpoint epAlt = null;
        for ( Endpoint ep : eps ) {
            if ( operation.equals(ep.getOperation()) ) {
                if ( ep.isUnnamed() && preferUnnamed )
                    return ep;
                if ( ! ep.isUnnamed() && ! preferUnnamed )
                    return ep;
                epAlt = ep;
            }
        }
        // Did not find a preferred one.
        return epAlt;
    }

    /**
     * Identify the operation being requested.
     * It is analysing the HTTP request using global configuration. 
     * The decision is is based on
     * <ul>
     * <li>Query parameters (URL query string or HTML form)</li>
     * <li>Content-Type header</li>
     * <li>Otherwise it is a plain REST (quads) operation.chooseOperation</li>
     * </ul>
     * The HTTP Method is not considered.
     * <p>
     * The operation is not guaranteed to be supported on every {@link DataService}
     * nor that access control will allow it to be performed. 
     */
    public static Operation chooseOperation(HttpAction action) {
        HttpServletRequest request = action.getRequest();
    
        // ---- Dispatch based on HttpParams : Query, Update, GSP.
        // -- Query
        boolean isQuery = request.getParameter(HttpNames.paramQuery) != null;
        if ( isQuery )
            return Query;
        // -- Update
        // Standards name "update", non-standard name "request" (old use by Fuseki)
        boolean isUpdate = request.getParameter(HttpNames.paramUpdate) != null || request.getParameter(HttpNames.paramRequest) != null;
        if ( isUpdate )
            // The SPARQL_Update servlet will deal with using GET.
            return Update;
    
        // -- SPARQL Graph Store Protocol
        boolean hasParamGraph = request.getParameter(HttpNames.paramGraph) != null;
        boolean hasParamGraphDefault = request.getParameter(HttpNames.paramGraphDefault) != null;
        if ( hasParamGraph || hasParamGraphDefault )
            return gspOperation(action, request);
    
        // -- Any other queryString
        // Place for an extension point.
        boolean hasParams = request.getParameterMap().size() > 0;
        if ( hasParams ) {
            // Unrecognized ?key=value
            ServletOps.errorBadRequest("Malformed request: unrecognized query string parameters: " + request.getQueryString());
        }
    
        // ---- Content-type
        // We don't wire in all the RDF syntaxes.
        // Instead, "Quads" drops through to the default operation.
    
        // This does not have the ";charset="
        String ct = request.getContentType();
        if ( ct != null ) {
            Operation operation = action.getOperationRegistry().findByContentType(ct);
            if ( operation != null )
                return operation;
        }
    
        // ---- No registered content type, no query parameters.
        // Plain HTTP operation on the dataset handled as quads or rejected.
        return quadsOperation(action, request);
    }

    /**
     * Determine the {@link Operation} for a SPARQL Graph Store Protocol (GSP) action.
     * <p>
     * Assumes, and does not check, that the action is a GSP action.
     *
     * @throws ActionErrorException
     *             (which causes a servlet 4xx response) if the operaton is not permitted.
     */
    private static Operation gspOperation(HttpAction action, HttpServletRequest request) throws ActionErrorException {
        // Check enabled.
        if ( isReadMethod(request) )
            return GSP_R;
        else
            return GSP_RW;
    }

    /**
     * Determine the {@link Operation} for a Quads operation. (GSP, except on the
     * whole dataset).
     * <p>
     * Assumes, and does not check, that the action is a Quads action.
     *
     * @throws ActionErrorException
     *             (which causes a servlet 405 response) if the operaton is not permitted.
     */
    private static Operation quadsOperation(HttpAction action, HttpServletRequest request) throws ActionErrorException {
        // Check enabled. Extends GSP.
        if ( isReadMethod(request) )
            return GSP_R;
        else
            return GSP_RW;
    }

    private static boolean isReadMethod(HttpServletRequest request) {
        String method = request.getMethod();
        // REST dataset.
        boolean isGET = method.equals(HttpNames.METHOD_GET);
        boolean isHEAD = method.equals(HttpNames.METHOD_HEAD);
        return isGET || isHEAD;
    }
}
