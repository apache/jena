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
import org.apache.jena.atlas.logging.FmtLog;
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
 * <p>
 * Administration operations, and directly registered servlets and static content,
 * are called through the usual web server process.
 * <p>
 * HTTP Request URLs, after servlet context removed, take the form {@code /dataset}
 * or {@code /dataset/service}. The most general URL is
 * {@code /context/dataset/service}. The {@link DataAccessPointRegistry} maps
 * {@code /dataset} to a {@link DataAccessPoint} which is a name and a
 * {@linkplain DataService}.
 * <p>
 * The dispatch process is:
 * </p>
 * <p>
 * 1. {@linkplain Dispatcher#dispatch} calls
 * {@linkplain Dispatcher#locateDataAccessPoint} to get the
 * {@linkplain DataAccessPoint} and calls {@linkplain Dispatcher#process}.
 * </p>
 * <p>
 * 2. {@linkplain Dispatcher#process} create the {@linkplain HttpAction} then calls
 * {@linkplain Dispatcher#dispatchAction} which calls through to
 * {@linkplain Dispatcher#chooseProcessor}.
 * </p>
 * <p>
 * 3. {@linkplain Dispatcher#chooseProcessor} does some checking and calls
 * {@linkplain Dispatcher#chooseEndpoint}.
 * </p>
 * <p>
 * 4. {@linkplain Dispatcher#chooseEndpoint} looks at request, and determines the
 * {@linkplain EndpointSet}.
 * </p>
 * <p>
 * 5. If there isn't an {@linkplain EndpointSet}, the dispatch process returns.
 * </p>
 * <p>
 * 6. If there is exactly one entry, this is the outcome.
 * </p>
 * <p>
 * 7. If there are multiple choices , {@linkplain Dispatcher#chooseOperation} looks
 * at request and decides which {@linkplain Operation} is being requested based on
 * SPARQL operations signatures and Content-Type.
 * </p>
 * <p>
 * 8.There is a default for dispatches with multiple choices that can't be separated
 * by SPARQL signature. These have no registered Content-type and no query string.
 * </p>
 * <p>
 * A choice by dispatch does not necessarily mean an operation is valid and will be
 * executed. It may fail authentication or not have a registered handler.
 * </p>
 */
public class Dispatcher {

    // Development debugging only. Excessive for normal operation.
    private static final boolean LogDispatch = false;
    private static Logger        LOG         = Fuseki.serverLog;

    // Development support
    private static final boolean DEBUG       = false;

    /**
     * Handle an HTTP request if it is sent to a registered dataset.
     * <p>
     * Fuseki uses dynamic dispatch, the set of registered datasets can change while
     * the server is running, so dispatch is driven off Fuseki system registries.
     * <p>
     * If the request URL matches a registered dataset, process the request, and send
     * the response.
     * <p>
     * This function is called by {@link FusekiFilter#doFilter}.
     * <p>
     * Returns {@code true} if the request has been handled, including an error response sent,
     * and returns false (no error or response sent) if the request has not been handled.
     * <p>
     * This function does not throw exceptions.
     * <p>
     * The dispatch process is:
     * <ul>
     * <li>Decide the data, based on the request URI ({@link #locateDataAccessPoint(HttpServletRequest, DataAccessPointRegistry)}
     * <li>Allocate HttpAction ({@link #process}, {@link #dispatchAction}).
     * <li>Decide service endpoint name ({@link #chooseProcessor})
     * <li>Decide operation {@link #chooseEndpoint}
     *   <ul>
     *   <li>Request parameters - query string (fixed for SPARQL query and SPARQL update) ({@link #chooseOperation})
     *   <li>Content type  ({@link #chooseOperation})
     *   <li>Default - quads operation
     *   </ul>
     * <li>Allow authentication for the dispatch choice.
     * </ul>
     *
     */
    public static boolean dispatch(HttpServletRequest request, HttpServletResponse response) {
        DataAccessPointRegistry registry = DataAccessPointRegistry.get(request.getServletContext());
        // Use the name to choose a DataAccessPoint.
        // A DataAccessPoint is a pair of dataset name and DataService.
        // The DataService may have multiple endpoints.
        DataAccessPoint dap = locateDataAccessPoint(request, registry);
        if ( dap == null ) {
            if ( LogDispatch )
                LOG.debug("No dispatch for '"+request.getRequestURI()+"'");
            return false;
        }
        // The execution code is in ActionExecLib.execAction
        return process(dap, request, response);
    }

    /**
     * The request may be /path/dataset/sparql or /path/dataset, or even /.
     * <p>
     * If the servlet context path is the "/path", then that was removed in ActionLib.actionURI.
     * But the dataset name may have a path within the servlet context.
     * <p>
     * The second form looks like dataset="path" and service="dataset"
     * We don't know the service until we find the DataAccessPoint.
     * For /dataset/sparql or /dataset, there is not a problem. The latter is too short to be a named service.
     * <p>
     * This function chooses the DataAccessPoint.
     * There may not be an endpoint and operation to handle the request.
     */
    private static DataAccessPoint locateDataAccessPoint(HttpServletRequest request, DataAccessPointRegistry registry) {
        // Path component of the URI, without context path
        String uri = ActionLib.actionURI(request);
        if ( LogDispatch ) {
            LOG.info("Filter: Request URI = " + request.getRequestURI());
            LOG.info("Filter: Action URI  = " + uri);
        }
        DataAccessPoint dap = locateDataAccessPoint(uri, registry);
        // At this point, we are going to dispatch to the DataAccessPoint.
        // It still may not have a handler for the service on this dataset.
        // See #chooseProcessor(HttpAction) for locating the endpoint.
        return dap;
    }

    /*package:testing*/ static DataAccessPoint locateDataAccessPoint(String uri, DataAccessPointRegistry registry) {
        // Direct match.
        if ( registry.isRegistered(uri) )
            // Cases: /, /dataset and /path/dataset where /path is not the servlet context path.
            return registry.get(uri);

        // Remove possible service endpoint name.
        String datasetUri = removeFinalComponent(uri);

        // Requests should at least have "/".
        if ( datasetUri == null )
            return null;

        if ( registry.isRegistered(datasetUri) )
            // Cases: /dataset/sparql and /path/dataset/sparql
            return registry.get(datasetUri);

        return null;
    }

    /** Remove the final component of a path - return a valid URI path. */
    private static String removeFinalComponent(String uri) {
        int i = uri.lastIndexOf('/');
        if ( i == -1 )
            return null;
        if ( i == 0 ) {
            // /pathComponent - return a valid URI path.
            return "/";
        }
        return uri.substring(0, i);
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
        // Pass as a Supplier so that chooseProcessor is inside
        // the error handling and after the start of the request.
        return ActionExecLib.execAction(action, ()->chooseProcessor(action));
    }

    /**
     * Find the ActionProcessor or return null if there can't determine one.
     *
     * This function sends the appropriate HTTP error response on failure to choose an endpoint.
     *
     * Returning null indicates an HTTP error response, and the HTTP response has been done.
     *
     * Process:
     * <ul>
     * <li> mapRequestToEndpointName -> endpoint name
     * <li> chooseEndpoint(action, dataService, endpointName) -> Endpoint.
     * <li> Endpoint to Operation (endpoint carries Operation).
     * <li> target(action, operation) -> ActionProcess.
     * </ul>
     *
     * @return ActionProcessor or null if the request URI can not be dealt with.
     */
    private static ActionProcessor chooseProcessor(HttpAction action) {
        // "return null" indicates that processing failed to find an ActionProcessor
        DataAccessPoint dataAccessPoint = action.getDataAccessPoint();
        DataService dataService = action.getDataService();

        if ( DEBUG )
            FmtLog.info(LOG, "Dispatch: "+ dataAccessPoint.getName());

        if ( !dataService.isAcceptingRequests() ) {
            ServletOps.error(HttpSC.SERVICE_UNAVAILABLE_503, "Dataset not currently active");
            return null;
        }

        // ---- Determine Endpoint.
        String endpointName = mapRequestToEndpointName(action, dataAccessPoint);
        if ( DEBUG )
            FmtLog.info(LOG, "Dispatch: endpointName: "+ endpointName);

        // Main step of choosing the endpoint for the dispatch of the request.
        // An endpoint is a (name, operation).
        // There may be multiple operations for an endpointName of this data service.

        Endpoint endpoint = chooseEndpoint(action, dataService, endpointName);
        if ( endpoint == null ) {
            if ( DEBUG )
                FmtLog.info(LOG, "Dispatch: no endpoint");
            // Includes named service, no such endpoint.
            // Allows for resources under /dataset/
            // The request will pass on down the filter/servlet chain.
            return null;
        }

        Operation operation = endpoint.getOperation();
        if ( operation == null ) {
            ServletOps.errorNotFound("No operation: "+action.getActionURI());
            return null;
        }
        if ( DEBUG )
            FmtLog.info(LOG, "Dispatch: endpoint operation: "+ operation);

        action.setEndpoint(endpoint);

        applyAuthentication(action.getUser(), dataService, endpoint);

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
        // Work out which operation we are looking for based on SPARQL characteristics.
        Operation operation = chooseOperation(action, epSet);

        // Special case for GSP_R/GSP_RW
        if ( operation != null )
            // May cause 405.
            operation = mapGSP(action, operation, epSet);
        if ( operation == null )
            ServletOps.errorBadRequest("No operation for request: "+action.getActionURI());
        ep = epSet.get(operation);
        // There are multiple endpoints; if none are suitable, then 400.
        if ( ep == null )
            ServletOps.errorBadRequest("No operation for request: "+action.getActionURI());
        return ep;
    }

    /**
     * Special case : GSP.
     * <p>
     * It is special because the two forms, GSP_R and GSP_RW overlap. GSP_RW can
     * service GSP_R methods (GET and HEAD). It is also special because there is a
     * different sattus code for attempting to write to GSP_R.
     * <p>
     * If this EndpointSet does not have exactly right operation:
     * <ul>
     * <li>If request is GSP_R, then if the EndpointSet has GSP_RW use that,
     *     else no operation.
     * <li>If request is GSP_RW, and the method is OPTIONS, redirect to
     *     GSP_R if available.
     * <li>If request is GSP_RW, and it is POST, PUT, DELETE, when only
     *     GSP_R is available, reject 405 "method not allowed".
     * </ul>
     */
    private static Operation mapGSP(HttpAction action, Operation operation, EndpointSet epSet) {
        // Get the endpoint
        Endpoint ep = epSet.get(operation);
        if ( ep == null ) {
            if ( GSP_R.equals(operation) ) {
                // If asking for GSP_R, and GSP_RW is available, use GSP_RW.
                if ( epSet.contains(GSP_RW) )
                    return GSP_RW;
            } else if ( GSP_RW.equals(operation) ) {
                // If asking for GSP_RW, but only GSP_R is available ...
                // ... if OPTIONS, use GSP_R.
                if ( action.getMethod().equals(HttpNames.METHOD_OPTIONS) && epSet.contains(GSP_R) )
                        return GSP_R;
                // ... else 405
                if ( epSet.contains(GSP_R) )
                    ServletOps.errorMethodNotAllowed(action.getMethod());
            }
        }
        return operation;
    }

    /**
     * Identify the operation being requested. It is analysing the HTTP request using
     * global configuration. The decision is based on
     * <ul>
     * <li>HTTP query string parameters (URL query string or HTML form)</li>
     * <li>Registered Content-Type header</li>
     * <li>Otherwise it is a plain REST (quads)</li>
     * </ul>
     * The configured endpoints is not considered.
     * This affects GSP. For read methods return GSP_R, and for changes return GSP_RW.
     * The dispatcher will map GSP_R to GSP_RW when only GSP_RW is available.
     * <p>
     * The operation is not guaranteed to be supported on every {@link DataService}
     * nor that access control will allow it to be performed.
     */
    private static Operation chooseOperation(HttpAction action, EndpointSet epSet) {
        // which is a DispatchFunction.
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
        String ct = request.getContentType();
        // Without ";charset="
        if ( ct != null ) {
            int idx = ct.indexOf(';');
            if ( idx > 0 )
                ct = ct.substring(0, idx);
        }
        // -- Any registration?
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
        // Plain HTTP operation on the dataset.
        DispatchFunction selectOperation = action.getDataService().getDefaultOperationChooser();
        if ( selectOperation == null )
            // Default is a quads operation.
            selectOperation = Dispatcher::selectPlainOperation;
        return selectOperation.selectOperation(action, epSet);
    }

    /**
     * This is a system default {@link DispatchFunction}.
     */
    public static Operation selectPlainOperation(HttpAction action, EndpointSet epSet) {
        return gspOperation(action, action.getRequest());
    }

    /**
     * Having chosen the data service and the endpoint, check the user request is permitted.
     * {@link AuthFilter} has already checked the server-level authorization.
     */
    private static void applyAuthentication(String user, DataService dataService, Endpoint endpoint) {
        // -- Server-level authorization.
        // AuthFilter.

        // -- Data service level authorization
        Auth.allow(user, dataService.authPolicy(), ServletOps::errorForbidden);

        // -- Endpoint level authorization
        Auth.allow(user, endpoint.getAuthPolicy(), ServletOps::errorForbidden);
    }

    /**
     * Determine the {@link Operation} for a SPARQL Graph Store Protocol (GSP) action.
     * <p>
     * Assumes, and does not check, that the action is a GSP action.
     */
    private static Operation gspOperation(HttpAction action, HttpServletRequest request) {
        return isReadMethod(request) ? GSP_R : GSP_RW;
    }

    /**
     * Return whether request method is a "read" (GET or HEAD) or "write", modifying
     * (POST, PUT, DELETE, PATCH)
     */
    private static boolean isReadMethod(HttpServletRequest request) {
        String method = request.getMethod();
        // REST dataset.
        boolean isGET = method.equals(HttpNames.METHOD_GET);
        boolean isHEAD = method.equals(HttpNames.METHOD_HEAD);
        return isGET || isHEAD;
    }
}
