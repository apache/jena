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

package org.apache.jena.fuseki.server;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.jena.fuseki.server.Operation.GSP_R;
import static org.apache.jena.fuseki.server.Operation.GSP_RW;
import static org.apache.jena.fuseki.server.Operation.Query;
import static org.apache.jena.fuseki.server.Operation.Update;
import static org.apache.jena.fuseki.servlets.ActionExecLib.allocHttpAction;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.auth.Auth;
import org.apache.jena.fuseki.servlets.*;
import org.apache.jena.fuseki.system.ActionCategory;
import org.apache.jena.riot.WebContent;
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
     * Returns {@code true} if the request has been handled, including an error response sent,
     * and returns false (no error or response sent) if the request has not been handled.
     *
     * This function does not throw exceptions.
     */
    public static boolean dispatch(HttpServletRequest request, HttpServletResponse response) {
        // Path component of the URI, without context path
        String uri = ActionLib.actionURI(request);
        String datasetUri = ActionLib.mapRequestToDataset(uri);

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
        return process(dap, request, response);
    }

    /**
     * Map request to operation name.
     * Returns the service name (the part after the "/" of the dataset part) or "".
     */
    private static String mapRequestToEndpointName(HttpAction action, DataAccessPoint dataAccessPoint) {
        return ActionLib.mapRequestToEndpointName(action, dataAccessPoint);
    }

    /** Set up and handle a HTTP request for a dataset. */
    private static boolean process(DataAccessPoint dap, HttpServletRequest request, HttpServletResponse response) {
        HttpAction action = allocHttpAction(dap, Fuseki.actionLog, ActionCategory.ACTION, request, response);
        return dispatchAction(action);
    }

    /**
     * Determine and call the {@link ActionProcessor} to handle this
     * {@link HttpAction}, including access control at the dataset and service levels.
     */
    private static boolean dispatchAction(HttpAction action) {
        return ActionExecLib.execAction(action, ()->chooseProcessor(action));
    }

    /**
     * Find the ActionProcessor or return null if there can't determine one.
     *
     * This function sends the appropriate HTTP error response.
     *
     * Returning null indicates an HTTP error response, and the HTTP response has been done.
     *
     * Process
     * <li> mapRequestToEndpointName -> endpoint name
     * <li> chooseEndpoint(action, dataService, endpointName) -> Endpoint.
     * <li> Endpoint to Operation (endpoint carries Operation).
     * <li> target(action, operation) -> ActionProcess.
     *
     * @return ActionProcessor or null if the request URI can not be dealt with.
     * @throws ActionErrorException for dispatch errors
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
        String endpointName = mapRequestToEndpointName(action, dataAccessPoint);
        // Main step of choosing the endpoint for the dispatch of the request.
        // An endpoint is a (name, operation).
        // There may be multiple operations for an endpointName of this data service.

        Endpoint endpoint = chooseEndpoint(action, dataService, endpointName);
        if ( endpoint == null )
            // Includes named service, no such endpoint.
            // Allows for resources under /dataset/
            // The request will pass on down the filter/servlet chain.
            return null;

        Operation operation = endpoint.getOperation();
        if ( operation == null ) {
            ServletOps.errorNotFound("No operation: "+action.getActionURI());
            return null;
        }

        action.setEndpoint(endpoint);

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
        // ---- Authorization checking.

        // ---- Handler.
        // Decide the code to execute the request.
        ActionProcessor processor = endpoint.getProcessor();
        if ( processor == null )
            ServletOps.errorBadRequest(format("No processor: dataset=%s: op=%s", dataAccessPoint.getName(), operation.getName()));
        return processor;
    }

    /**
     * Choose an endpoint.
     * An endpoint is a name and an operation.
     * <ul>
     * <li>Look by service name to get the EndpointSet</li>
     * <li>If empty set, respond with error</li>
     * <li>If there is only one choice, return that (may even be the wrong operation
     *       - processor implementations must be defensive).</li>
     * <li>If multiple choices, classify the operation
     *     (includes custom content-type) and look up by operation.</li>
     * <li>If not suitable, respond with error
     * <li>Return an endpoint.
     * </ul>
     * The endpoint chosen may not be suitable, the operation must do checking.
     */
    private static Endpoint chooseEndpoint(HttpAction action, DataService dataService, String endpointName) {
        EndpointSet epSet = isEmpty(endpointName) ? dataService.getEndpointSet() : dataService.getEndpointSet(endpointName);
        if ( epSet == null || epSet.isEmpty() ) {
            // No matches by name.
            if ( ! StringUtils.isAnyEmpty(endpointName) )
                // There was a service name but it was not found.
                // It may be a URL for static resource.
                return null;
            // Dataset URL - "exists" (even if no services) so 404 is wrong.
            ServletOps.errorBadRequest("No endpoint for request");
            return null; // Unreachable.
        }

        // If there is one endpoint, dispatch there directly.
        Endpoint ep = epSet.getExactlyOne();
        if ( ep != null )
            // Single dispatch, may not be valid.
            return ep;
        // No single direct dispatch. Multiple choices (different operation, same endpoint name)
        // Work out which operation we are looking for.
        Operation operation = chooseOperation(action);
        ep = epSet.get(operation);
        if ( ep == null ) {
            if ( GSP_R.equals(operation) )
                // If asking for GSP_R, and GSP_RW available, pass that back.
                ep = epSet.get(GSP_RW); // [GSP Promote]
            else if ( GSP_RW.equals(operation) ) {
                // If asking for GSP_RW, only GSP_R available -> 405.
                if ( epSet.contains(GSP_R) )
                    ServletOps.errorMethodNotAllowed(action.getMethod());
            }
        }

        // There are multiple endpoints; if none are suitable, then 400.
        if ( ep == null )
            ServletOps.errorBadRequest("No operation for request: "+action.getActionURI());
        return ep;
    }

    /**
     * Identify the operation being requested.
     * It is analysing the HTTP request using global configuration.
     * The decision is based on
     * <ul>
     * <li>HTTP query string parameters (URL query string or HTML form)</li>
     * <li>Registered Content-Type header</li>
     * <li>Otherwise it is a plain REST (quads)</li>
     * </ul>
     * The HTTP Method is not considered.
     * <p>
     * The operation is not guaranteed to be supported on every {@link DataService}
     * nor that access control will allow it to be performed.
     */
    private static Operation chooseOperation(HttpAction action) {
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

        // ---- Content-type
        // This does not have the ";charset="
        String ct = request.getContentType();
        if ( ct != null ) {
            Operation operation = action.getOperationRegistry().findByContentType(ct);
            if ( operation != null )
                return operation;
        }
        // We don't wire in all the RDF syntaxes.
        // Instead, "Quads" drops through to the default operation.

        // -- SPARQL Graph Store Protocol
        boolean hasParamGraph = request.getParameter(HttpNames.paramGraph) != null;
        boolean hasParamGraphDefault = request.getParameter(HttpNames.paramGraphDefault) != null;
        if ( hasParamGraph || hasParamGraphDefault )
            return gspOperation(action, request);

        // -- Any other queryString
        // Query string now unexpected.

        // Place for an extension point.
        boolean hasParams = request.getParameterMap().size() > 0;
        if ( hasParams ) {
            // One nasty case:
            // Bad HTML form (content-type  application/x-www-form-urlencoded), but body is not an HTML form.
            //  map is one entry, and the key is all of the body,
            if ( WebContent.contentTypeHTMLForm.equals(request.getContentType()) ) {
                ServletOps.errorBadRequest("Malformed request: unrecognized HTML form request");
                return null;
            }
            // Unrecognized ?key=value
            String qs = request.getQueryString();
            if ( qs != null )
                ServletOps.errorBadRequest("Malformed request: unrecognized parameters: " + qs);
            else
                ServletOps.errorBadRequest(HttpSC.getMessage(HttpSC.BAD_REQUEST_400));
        }

        // ---- No registered content type, no query parameters.
        // Plain HTTP operation on the dataset handled as quads or rejected.
        return quadsOperation(action, request);
    }

    /**
     * Determine the {@link Operation} for a SPARQL Graph Store Protocol (GSP) action.
     * <p>
     * Assumes, and does not check, that the action is a GSP action.
     */
    private static Operation gspOperation(HttpAction action, HttpServletRequest request) {
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
     */
    private static Operation quadsOperation(HttpAction action, HttpServletRequest request) {
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
