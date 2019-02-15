/**
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

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jena.atlas.web.AcceptList;
import org.apache.jena.atlas.web.MediaType;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.FusekiException;
import org.apache.jena.fuseki.server.DataService;
import org.apache.jena.fuseki.server.Endpoint;
import org.apache.jena.fuseki.server.Operation;
import org.apache.jena.fuseki.system.ConNeg;
import org.apache.jena.riot.web.HttpNames;

/**
 * This servlet makes the routing decisions for all service operations (not admin, not
 * task handling).
 * <p>
 * The two routing operations are {@link #chooseOperation(HttpAction, DataService)} for
 * operation on the dataset and {@link #chooseOperation(HttpAction, DataService, String)}
 * for operations by service endpoint.
 * <p>
 * Normal use is to route all service operations to this servlet via {@link ActionService}.
 * It will route for operations on the dataset.
 * <p>
 * It be attached to a dataset location and acts as a router for all SPARQL operations
 * (query, update, graph store, both direct and indirect naming, quads operations on a
 * dataset and ?query and ?update directly on a dataset.). Then specific service servlets
 * attached to each service endpoint.
 * <p>
 * It work in conjunction with {@link ActionService#execCommonWorker} to decide where to
 * route requests.
 */
public class ServiceRouter extends ActionService {
    
    public ServiceRouter() {
        super((action, operation)->action.getServiceDispatchRegistry().findHandler(operation));
    }

    // These calls should not happen because ActionService calls chooseOperation(),
    // looks that up in the ServiceDispatchRegistry for the servlet context,
    // then calls executeLifecycle() on that servlet.
    // These exceptions catch any loops. 
    @Override
    protected void validate(HttpAction action) {
        throw new FusekiException("Call to ServiceRouterServlet.validate");
    }

    @Override
    protected void perform(HttpAction action) {
        throw new FusekiException("Call to ServiceRouterServlet.perform");
    }

    /**
     * Choose dispatch when {@code serviceName} is not the empty string.
     * <p>
     * Example {@code /dataset/sparql} has dataset URI {@code /dataset} and service name
     * {@code sparql}. The {@code serviceName} is the empty string which is handled by
     * {@link #chooseOperation(HttpAction, DataService)}.
     * <p>
     * If the service name isn't recognized, drops through to GSP Direct Naming (the graph
     * name is the whole URI). This is not usually enabled; it is controlled by
     * {@link Fuseki#GSP_DIRECT_NAMING}.
     */
    @Override
    protected Operation chooseOperation(HttpAction action, DataService dataService, String endpointName) {
        // Default implementation in ActionService:
//        Endpoint ep = dataService.getEndpoint(endpointName);
//        Operation operation = ep.getOperation();
//        action.setEndpoint(ep);
        
        Endpoint ep = dataService.getEndpoint(endpointName);
        if ( ep != null ) {
            Operation operation = ep.getOperation();
            action.setEndpoint(ep);
            if ( operation != null ) { 
                // Can this be null?
                // If a GSP operation, then no params means Quads operation.
                if ( operation.equals(Operation.GSP_R) || operation.equals(Operation.GSP_RW) ) {
                    // Look for special case. Quads on the GSP service endpoint.
                    boolean hasParamGraph = action.request.getParameter(HttpNames.paramGraph) != null;
                    boolean hasParamGraphDefault = action.request.getParameter(HttpNames.paramGraphDefault) != null;
                    if ( !hasParamGraph && !hasParamGraphDefault ) {
                        if ( operation.equals(Operation.GSP_RW) )
                            return Operation.Quads_RW;
                        else
                            return Operation.Quads_R;
                    }
                }
                return operation;
            }
            System.err.printf("Notice: endpoint %s but no operation", endpointName);
        }

        // No endpoint.
        // There is a trailing part - unrecognized service name ==> GSP direct naming.
        if ( !Fuseki.GSP_DIRECT_NAMING )
            ServletOps.errorNotFound(
                "Not found: dataset='" + printName(action.getDataAccessPoint().getName()) + "' endpoint='" + printName(endpointName) + "'");
        // GSP Direct naming - the servlets handle direct and indirct naming.
        return gspOperation(action, action.request);
    }

    /**
     * Choose dispatch when {@code serviceName} is the empty string.
     * <p>
     * Example {@code /dataset} has dataset URI {@code /dataset}.
     * <p>
     * Dispatch is based on:
     * <ul>
     * <li>HTTP params (for ?query= and ?update=)</li>
     * <li>Content type</li>
     * </ul>
     */
    @Override final
    protected Operation chooseOperation(HttpAction action, DataService dataService) {
        HttpServletRequest request = action.getRequest();

        // ---- Dispatch based on HttpParams : Query, Update, GSP.
        // -- Query
        boolean isQuery = request.getParameter(HttpNames.paramQuery) != null;
        if ( isQuery ) {
            if ( !allowQuery(action) )
                ServletOps.errorMethodNotAllowed("SPARQL query : " + action.getMethod());
            return Operation.Query;
        }
        // -- Update
        // Standards name "update", non-standard name "request" (old use by Fuseki)
        boolean isUpdate = request.getParameter(HttpNames.paramUpdate) != null || request.getParameter(HttpNames.paramRequest) != null;
        if ( isUpdate ) {
            if ( !allowUpdate(action) )
                ServletOps.errorMethodNotAllowed("SPARQL update : " + action.getMethod());
            // The SPARQL_Update servlet will deal with using GET.
            return Operation.Update;
        }

        // -- SPARQL Graph Store Protocol
        boolean hasParamGraph = request.getParameter(HttpNames.paramGraph) != null;
        boolean hasParamGraphDefault = request.getParameter(HttpNames.paramGraphDefault) != null;
        if ( hasParamGraph || hasParamGraphDefault )
            return gspOperation(action, request);

        // -- Anything else.
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
            Operation operation = action.getServiceDispatchRegistry().findOperation(ct);
            if ( operation != null ) {
                // Check there is a service for this dataset.
                List<Endpoint> x = action.getDataService().getEndpoints(operation);
                if ( x.isEmpty() )
                    ServletOps.errorBadRequest("Malformed request: Content-Type not enabled by an endpoint for this dataset: " 
                            + action.getActionURI() + " : Content-Type: "+ct);
                return operation;
            }
            // operation == null : include drop-through for quads/triples on the dataset.
        }

        // ---- GET and Accept
        // Placeholder.
        // Done by default drop through currently.
        if ( false ) {
            // Registry of offers. 
            AcceptList offers = null;
            MediaType defAccept = null;
            MediaType mt = ConNeg.chooseContentType(action.request, offers, defAccept);
            // mt -> Operation 
        }

        // ---- Default
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
    private Operation gspOperation(HttpAction action, HttpServletRequest request) throws ActionErrorException {
        // Check enabled.
        if ( isReadMethod(request) )
            return operationGSP_R(action);
        else
            return operationGSP_RW(action);
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
    private Operation quadsOperation(HttpAction action, HttpServletRequest request) throws ActionErrorException {
        // Check enabled.
        if ( isReadMethod(request) )
            return operationQuads_R(action);
        else
            return operationQuads_RW(action);
    }

    private Operation operationGSP_R(HttpAction action) {
        if ( allowGSP_R(action) )
            return Operation.GSP_R;
        else
            ServletOps.errorMethodNotAllowed(action.request.getMethod());
        return null;
    }

    private Operation operationGSP_RW(HttpAction action) {
        if ( allowGSP_RW(action) )
            return Operation.GSP_RW;
        else
            ServletOps.errorMethodNotAllowed("Read-only dataset : " + action.request.getMethod());
        return null;
    }

    private Operation operationQuads_R(HttpAction action) {
        if ( allowQuads_R(action) )
            return Operation.Quads_R;
        else
            ServletOps.errorMethodNotAllowed(action.request.getMethod());
        return null;
    }

    private Operation operationQuads_RW(HttpAction action) {
        if ( allowQuads_RW(action) )
            return Operation.Quads_RW;
        else
            ServletOps.errorMethodNotAllowed("Read-only dataset : " + action.request.getMethod());
        return null;
    }

    private boolean isReadMethod(HttpServletRequest request) {
        String method = request.getMethod();
        // REST dataset.
        boolean isGET = method.equals(HttpNames.METHOD_GET);
        boolean isHEAD = method.equals(HttpNames.METHOD_HEAD);
        return isGET || isHEAD;
    }
    
    private static String printName(String x) {
        if ( x.startsWith("/") )
            return x.substring(1);
        return x;
    }
    
    // Route everything to "doCommon"
    @Override
    protected void doHead(HttpServletRequest request, HttpServletResponse response) {
        doCommon(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        doCommon(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        doCommon(request, response);
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) {
        doCommon(request, response);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) {
        doCommon(request, response);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) {
        doCommon(request, response);
    }

    // Check whether an operation is allowed by the setup.
    // This is used when the operation/endpoint is not named directly
    // e.g. http://host:port/dataset?query= 
    //   is implicitly a call on 
    // http://host:port/dataset/sparql?query=
    
    protected boolean allowQuery(HttpAction action) {
        return isEnabled(action, Operation.Query);
    }

    protected boolean allowUpdate(HttpAction action) {
        return isEnabled(action, Operation.Update);
    }

    protected boolean allowGSP_R(HttpAction action) {
        return isEnabled(action, Operation.GSP_R) || isEnabled(action, Operation.GSP_RW);
    }

    protected boolean allowGSP_RW(HttpAction action) {
        return isEnabled(action, Operation.GSP_RW);
    }

    protected boolean allowQuads_R(HttpAction action) {
        return isEnabled(action, Operation.Quads_R) || isEnabled(action, Operation.Quads_RW);
    }

    protected boolean allowQuads_RW(HttpAction action) {
        return isEnabled(action, Operation.Quads_RW);
    }

    /**
     * Test whether there is a configuration that allows this action as the operation
     * given. Ignores the operation in the action which is set due to parsing - it
     * might be "quads" which is the generic operation when just the dataset is
     * specified.
     */
    private boolean isEnabled(HttpAction action, Operation operation) {
        // Disregard the operation name of the action.
        DataService dSrv = action.getDataService();
        if ( dSrv == null )
            return false;
        return !dSrv.getEndpoints(operation).isEmpty();
    }
}
