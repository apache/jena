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

import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.apache.jena.fuseki.Fuseki ;
import org.apache.jena.fuseki.FusekiException ;
import org.apache.jena.fuseki.server.DataService;
import org.apache.jena.fuseki.server.Endpoint;
import org.apache.jena.fuseki.server.OperationName;
import org.apache.jena.riot.web.HttpNames ;

/**
 * This servlet makes the routing decisions for all service operations (not admin, not
 * task handling).
 * <p>
 * The two routing operations are {@link #chooseEndpoint(HttpAction, DataService)} for
 * operation on the dataset and {@link #chooseEndpoint(HttpAction, DataService, String)}
 * for operations by service endpoint.
 * <p>
 * Normal use is to route all service operations to this servlet via {@link ActionSPARQL}.
 * It wil route for operations on teh dataset and the
 * <p>
 * It be attached to a dataset location and acts as a router for all SPARQL operations
 * (query, update, graph store, both direct and indirect naming, quads operations on a
 * dataset and ?query and ?update directly on a dataset.). Then specific service servlets
 * attached to each service endpoint.
 */
public abstract class ServiceRouterServlet extends ActionSPARQL
{
    protected abstract boolean allowQuery(HttpAction action) ;
    protected abstract boolean allowUpdate(HttpAction action) ;
    protected abstract boolean allowGSP_R(HttpAction action) ;
    protected abstract boolean allowGSP_RW(HttpAction action) ;
    protected abstract boolean allowQuads_R(HttpAction action) ;
    protected abstract boolean allowQuads_RW(HttpAction action) ;

//    public static class ReadOnly extends ServiceRouterServlet
//    {
//        public ReadOnly()    { super() ; }
//        @Override protected boolean allowQuery(HttpAction action)    { return true ; }
//        @Override protected boolean allowUpdate(HttpAction action)   { return false ; }
//        @Override protected boolean allowGSP_R(HttpAction action)   { return true ; }
//        @Override protected boolean allowGSP_RW(HttpAction action)   { return false ; }
//        @Override protected boolean allowQuads_R(HttpAction action)   { return true ; }
//        @Override protected boolean allowQuads_RW(HttpAction action)   { return false ; }
//    }
//
//    public static class ReadWrite extends ServiceRouterServlet
//    {
//        public ReadWrite()    { super() ; }
//        @Override protected boolean allowQuery(HttpAction action)    { return true ; }
//        @Override protected boolean allowUpdate(HttpAction action)   { return true ; }
//        @Override protected boolean allowGSP_R(HttpAction action)   { return true ; }
//        @Override protected boolean allowGSP_RW(HttpAction action)   { return true ; }
//        @Override protected boolean allowQuads_R(HttpAction action)   { return true ; }
//        @Override protected boolean allowQuads_RW(HttpAction action)   { return true ; }
//    }

    public static class AccessByConfig extends ServiceRouterServlet
    {
        public AccessByConfig()    { super() ; }
        @Override protected boolean allowQuery(HttpAction action)    { return isEnabled(action, OperationName.Query) ; }
        @Override protected boolean allowUpdate(HttpAction action)   { return isEnabled(action, OperationName.Update) ; }
        @Override protected boolean allowGSP_R(HttpAction action)   { return isEnabled(action, OperationName.GSP_R) || isEnabled(action, OperationName.GSP_RW) ; }
        @Override protected boolean allowGSP_RW(HttpAction action)   { return isEnabled(action, OperationName.GSP_RW) ; }
        @Override protected boolean allowQuads_R(HttpAction action)   { return isEnabled(action, OperationName.Quads_R) || isEnabled(action, OperationName.Quads_RW) ; }
        @Override protected boolean allowQuads_RW(HttpAction action)   { return isEnabled(action, OperationName.Quads_RW) ; }

        // Test whether there is a configuration that allows this action as the operation given.
        // Ignores the operation in the action (set due to parsing - it might be "quads"
        // which is the generic operation when just the dataset is specificed.
        private boolean isEnabled(HttpAction action, OperationName opName) {
            // Disregard the operation name of the action
            DataService dSrv = action.getDataService() ;
            if ( dSrv == null )
                return false;
            return ! dSrv.getOperation(opName).isEmpty() ;
        }
    }

    public ServiceRouterServlet() { super(); }

    // These calls should not happen because we hook in at executeAction
    @Override protected void validate(HttpAction action) { throw new FusekiException("Call to ServiceRouterServlet.validate") ; }
    @Override protected void perform(HttpAction action)  { throw new FusekiException("Call to ServiceRouterServlet.perform") ; }

    /**
     * Choose dispatch when {@code serviceName} is not the empty string.
     * <p>
     * Example {@code /dataset/sparql} has dataset URI {@code /dataset} and service name 
     * {@code sparql}. 
     * The {@code serviceName} is the empty string which is handled by {@link #chooseEndpoint(HttpAction, DataService)}.
     * <p>
     * If the service name isn't recognized, drops through to GSP Direct Naming (the graph name is the whole URI).
     * This is not usually enabled; it is controlled by {@link Fuseki#GSP_DIRECT_NAMING}.
     */
    @Override
    protected OperationName chooseEndpoint(HttpAction action, DataService dataService, String serviceName) {
        // Check enabled happens here. Must be enabled by configuration to be in the lookup.
        Endpoint ep = dataService.getEndpoint(serviceName) ;
        if ( ep != null ) {
            OperationName opName = ep.getOperationName();
            if ( opName != null ) {
                // If GSP, no params means Quads operation.
                if ( opName.equals(OperationName.GSP_R) || opName.equals(OperationName.GSP_RW)  ) {
                    // Look for special case. Quads on the GSP service endpoint.
                    boolean hasParamGraph           = action.request.getParameter(HttpNames.paramGraph) != null ;
                    boolean hasParamGraphDefault    = action.request.getParameter(HttpNames.paramGraphDefault) != null ;
                    if ( ! hasParamGraph && ! hasParamGraphDefault ) {
                        if ( opName.equals(OperationName.GSP_RW) )
                            return OperationName.Quads_RW;
                        else
                            return OperationName.Quads_R;
                    }
                }
                return opName;
            }
        }
        
        // There is a trailing part - unrecognized service name ==> GSP direct naming.
        if ( ! Fuseki.GSP_DIRECT_NAMING )
            ServletOps.errorNotFound("Not found: dataset='"+printName(action.getDataAccessPoint().getName())+"' service='"+printName(serviceName)+"'");
        // GSP Direct naming - the servlets handle direct and indirct naming. 
        return gspOpName(action, action.request);
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
     *  
     */
    @Override
    protected OperationName chooseEndpoint(HttpAction action, DataService dataService) {
        Endpoint ep = dataService.getEndpoint("") ;
        HttpServletRequest request = action.getRequest();
        
        // ---- Dispatch based on HttpParams : Query, Update, GSP. 
        //-- Query
        boolean isQuery = request.getParameter(HttpNames.paramQuery) != null ;
        if ( isQuery ) {
            if ( !allowQuery(action) )
                ServletOps.errorMethodNotAllowed("SPARQL query : "+action.getMethod()) ;
            return OperationName.Query; 
        }
        //-- Update
        // Standards name "update", non-standard name "request" (old use by Fuseki)
        boolean isUpdate = request.getParameter(HttpNames.paramUpdate) != null || request.getParameter(HttpNames.paramRequest) != null;
        if ( isUpdate ) {
            if ( !allowUpdate(action) )
                ServletOps.errorMethodNotAllowed("SPARQL update : "+action.getMethod()) ;
            // The SPARQL_Update servlet will deal with using GET.
            return OperationName.Update; 
        }
        
        // -- SPARQL Graph Store Protocol
        boolean hasParamGraph           = request.getParameter(HttpNames.paramGraph) != null ;
        boolean hasParamGraphDefault    = request.getParameter(HttpNames.paramGraphDefault) != null ;
        if ( hasParamGraph || hasParamGraphDefault )
            return gspOpName(action, request);
        
        // -- Anything else.
        // Place for an extension point.
        boolean hasParams = request.getParameterMap().size() > 0 ;
        if ( hasParams ) {
            // Unrecognized ?key=value
            ServletOps.errorBadRequest("Malformed request: unrecognized query string parameters: "+request.getQueryString()) ;
        }
        
        // ---- Content-type
        
        // This does no have the ";charset=" 
        String ct = request.getContentType();
        if ( ct != null ) {
            OperationName opName = Dispatch.contentTypeToOpName.get(ct);
            if ( opName != null )
                return opName;
        }
        
        // ---- Default: quads on dataset (maybe).
        return quadsOpName(action, request);
    }

    /** Determine the {@link OperationName} for a SPARQL Graph Store Protocol (GSP) action.
     * <p>
     * Assumes, and does not check, that the action is a GSP action.
     * 
     * @throws ActionErrorException (which causes a servlet 4xx response) if the operaton is not permitted.
     */
    private OperationName gspOpName(HttpAction action, HttpServletRequest request) throws ActionErrorException {
        // Check enabled.
        if ( isReadMethod(request) )
            return operationGSP_R(action);
        else
            return operationGSP_RW(action);
    }
    
    /** Determine the {@link OperationName} for a Quads operation.
     * (GSP, except on the whole dataset). 
     * <p>
     * Assumes, and does not check, that the action is a Quads action.
     * 
     * @throws ActionErrorException (which causes a servlet 405 response) if the operaton is not permitted.
     */
    private OperationName quadsOpName(HttpAction action, HttpServletRequest request) throws ActionErrorException {
    
        // Check enabled.
        if ( isReadMethod(request) )
            return operationQuads_R(action);
        else
            return operationQuads_RW(action);
    }

    private OperationName operationGSP_R(HttpAction action) {
        if ( allowGSP_R(action) )
            return OperationName.GSP_R;
        else
            ServletOps.errorMethodNotAllowed(action.request.getMethod());
        return null;
    }
    
    private OperationName operationGSP_RW(HttpAction action) {
        if ( allowGSP_RW(action) )
            return OperationName.GSP_RW;
        else
            ServletOps.errorMethodNotAllowed("Read-only dataset : "+action.request.getMethod());
        return null;
    }

    private OperationName operationQuads_R(HttpAction action) {
        if ( allowQuads_R(action) )
            return OperationName.Quads_R;
        else
            ServletOps.errorMethodNotAllowed(action.request.getMethod());
        return null;
    }
    
    private OperationName operationQuads_RW(HttpAction action) {
        if ( allowQuads_RW(action) )
            return OperationName.Quads_RW;
        else
            ServletOps.errorMethodNotAllowed("Read-only dataset : "+action.request.getMethod());
        return null;
    }

    // XXX -------------------
    
private boolean isReadMethod(HttpServletRequest request) {
        String method = request.getMethod();
        // REST dataset.
        boolean isGET = method.equals(HttpNames.METHOD_GET) ;
        boolean isHEAD = method.equals(HttpNames.METHOD_HEAD) ;
        return isGET || isHEAD ;
    }
private String printName(String x) {
        if ( x.startsWith("/") )
            return x.substring(1) ;
        return x ;
    }
    //    /** Intercept the processing cycle at the point where the action has been set up,
//     *  the dataset target decided but no validation or execution has been done,
//     *  nor any stats have been done.
//     */
//    @Override
//    protected void executeAction(HttpAction action) {
//        if ( true )
//            logAction(action);
//        // DEBUG: DataAccessPointRegistry.print("UberServlet ");
//        
//        long id = action.id ;
//        HttpServletRequest request = action.request ;
//        HttpServletResponse response = action.response ;
//        String actionURI = action.getActionURI() ;            // No context path
//        String method = request.getMethod() ;
//
//        DataAccessPoint desc = action.getDataAccessPoint() ;
//        DataService dSrv = action.getDataService() ;
//
//        if ( ! dSrv.isAcceptingRequests() )
//            ServletOps.error(HttpSC.SERVICE_UNAVAILABLE_503, "Dataset not currently active");
//
//        // Part after the DataAccessPoint (dataset) name.
//        String trailing = findTrailing(actionURI, desc.getName()) ;
//        String qs = request.getQueryString() ;
//
//        boolean hasParams = request.getParameterMap().size() > 0 ;
//
//        // Is it a query or update because of a ?query= , ?request= parameter? 
//        // Test for parameters - includes HTML forms.
//        boolean isQuery           = request.getParameter(HttpNames.paramQuery) != null ;
//        // Include old name "request="
//        boolean isUpdate          = request.getParameter(HttpNames.paramUpdate) != null || request.getParameter(HttpNames.paramRequest) != null ;
//
//        boolean hasParamGraph           = request.getParameter(HttpNames.paramGraph) != null ;
//        boolean hasParamGraphDefault    = request.getParameter(HttpNames.paramGraphDefault) != null ;
//
//        boolean hasTrailing = ( trailing.length() != 0 ) ;
//        
//        String ct = request.getContentType() ;
//        String charset = request.getCharacterEncoding() ;
//
//        MediaType mt = null ;
//        if ( ct != null ) {
//            // Parse it.
//            mt = MediaType.create(ct, charset) ;
//            // Another way to send queries and updates is with the content-type. 
//            if ( contentTypeSPARQLQuery.equalsIgnoreCase(ct) )
//                isQuery = true ;
//            else if ( contentTypeSPARQLUpdate.equalsIgnoreCase(ct) )
//                isUpdate = true ;
//        }
//            
//        if (action.log.isInfoEnabled() ) {
//            //String cxt = action.getContextPath() ;
//            action.log.info(format("[%d] %s %s :: '%s' :: %s ? %s", id, method, desc.getName(), trailing, (mt==null?"<none>":mt), (qs==null?"":qs))) ;
//        }
//        
//        if ( !hasTrailing ) {
//            // Nothing after the DataAccessPoint i.e. Dataset by name.
//            // Action on the dataset itself. This can be:
//            //
//            //   http://localhost:3030/ds?query=
//            //   http://localhost:3030/ds and a content type of "applicatiopn/sparql-query"
//            //
//            //   http://localhost:3030/ds?update=
//            //   http://localhost:3030/ds and a content type of "applicatiopn/sparql-update"
//            //
//            //   http://localhost:3030/ds?default ?graph=  GSP
//            //
//            //   http://localhost:3030/ds   REST quads action on the dataset itself.
//            if ( isQuery ) {
//                if ( !allowQuery(action) )
//                    ServletOps.errorMethodNotAllowed("SPARQL query : "+method) ;
//                executeRequest(action, queryServlet) ;
//                return ;
//            }
//
//            if ( isUpdate ) {
//                // SPARQL Update
//                if ( !allowUpdate(action) )
//                    ServletOps.errorMethodNotAllowed("SPARQL update : "+method) ;
//                // This will deal with using GET.
//                executeRequest(action, updateServlet) ;
//                return ;
//            }
//
//            // ?graph=, ?default
//            if ( hasParamGraph || hasParamGraphDefault ) {
//                doGraphStoreProtocol(action) ;
//                return ;
//            }
//
//            if ( hasParams ) {
//                // Unrecognized ?key=value
//                ServletOps.errorBadRequest("Malformed request") ;
//            }
//            
//            // REST dataset.
//            boolean isGET = method.equals(HttpNames.METHOD_GET) ;
//            boolean isHEAD = method.equals(HttpNames.METHOD_HEAD) ;
//
//            // Check enabled.
//            if ( isGET || isHEAD ) {
//                if ( allowQuads_R(action) )
//                    restQuads_R.executeLifecycle(action) ;
//                else
//                    ServletOps.errorMethodNotAllowed(method) ;
//                return ;
//            }
//            
//            if ( allowQuads_RW(action) )
//                restQuads_RW.executeLifecycle(action) ;
//            else
//                ServletOps.errorMethodNotAllowed("Read-only dataset : "+method) ;
//            return ;
//        }
//
//        // Has trailing path name => service or direct naming GSP.
//        
//        final boolean checkForPossibleService = true ;
//        if ( checkForPossibleService && action.getEndpoint() != null ) {
//            
//            // Small steps.
//            {
//                Endpoint operation = action.getEndpoint() ;
//                OperationName op = operation.getOperationName();
//                
//                // GSP to quads.
//                if ( ! hasParams ) {
//                    
//                    if ( op.equals(OperationName.GSP_RW) )
//                        op = OperationName.Quads_RW;
//                    else if ( op.equals(OperationName.GSP_R) )
//                        op  = OperationName.Quads_R;
//                }
//                
//                ActionSPARQL handler = OpNameToHandler.get(op);
//                if ( handler != null ) {
//                    executeRequest(action, handler) ;
//                    return ;
//                }
//            }
//            
//                System.err.println("No new dispatch");
//            
//            // There is a trailing part.
//            // Check it's not the same name as a registered service.
//            // If so, dispatch to that service.
//            if ( serviceDispatch(action, OperationName.Query, queryServlet) ) return ;
//            if ( serviceDispatch(action, OperationName.Update, updateServlet) ) return ;
//            if ( serviceDispatch(action, OperationName.Upload, uploadServlet) ) return ;
//            if ( hasParams ) {
//                if ( serviceDispatch(action, OperationName.GSP_R, gspServlet_R) ) return ;
//                if ( serviceDispatch(action, OperationName.GSP_RW, gspServlet_RW) ) return ;
//            } else {
//                // No parameters - do as a quads operation on the dataset.
//                if ( serviceDispatch(action, OperationName.GSP_R, restQuads_R) ) return ;
//                if ( serviceDispatch(action, OperationName.GSP_RW, restQuads_RW) ) return ;
//            }
//            if ( serviceDispatch(action, OperationName.Quads_RW, restQuads_RW) ) return ;
//            if ( serviceDispatch(action, OperationName.Quads_R, restQuads_R) ) return ;
//        }
//        // There is a trailing part - params are illegal by this point.
//        if ( hasParams )
//            // ?? Revisit to include query-on-one-graph
//            //errorBadRequest("Can't invoke a query-string service on a direct named graph") ;
//            ServletOps.errorNotFound("Not found: dataset='"+printName(desc.getName())+
//                                     "' service='"+printName(trailing)+
//                                     "' query string=?"+qs);
//
//        // There is a trailing part - not a service, no params ==> GSP direct naming.
//        if ( ! Fuseki.GSP_DIRECT_NAMING )
//            ServletOps.errorNotFound("Not found: dataset='"+printName(desc.getName())+"' service='"+printName(trailing)+"'");
//
//        doGraphStoreProtocol(action);
//    }
//
//    private void logAction(HttpAction action) {
//        Endpoint ep = action.endpoint; 
//        if ( ep != null )
//            action.log.info(format("[%d] ep=('%s',%s) ct=%s", action.id, ep.getEndpoint(), ep.getOperationName(), action.responseContentType));
//        else
//            action.log.info(format("[%d] ep=('--',--) ct=%s", action.id, action.responseContentType));
//    }
//    
//    /**
//     * See if the operation is enabled for this setup.
//     * Return true if dispatched
//     */
//    private boolean serviceDispatch(HttpAction action, OperationName opName, ActionSPARQL servlet) {
//        Endpoint operation = action.getEndpoint() ;
//        if ( operation == null )
//            return false ;
//        if ( ! operation.isType(opName) )
//            return false ;
//        // Handle OPTIONS specially.
////        if ( action.getRequest().getMethod().equals(HttpNames.METHOD_OPTIONS) ) {
////            // See also ServletBase.CORS_ENABLED
////            //action.log.info(format("[%d] %s", action.id, action.getMethod())) ;
////            setCommonHeadersForOptions(action.getResponse()) ;
////            ServletOps.success(action);
////            return true ;
////        }
//        executeRequest(action, servlet) ;
//        return true ;
//    }
//    private String printName(String x) {
//        if ( x.startsWith("/") )
//            return x.substring(1) ;
//        return x ;
//    }
//
//    private void doGraphStoreProtocol(HttpAction action) {
//        // The GSP servlets handle direct and indirect naming.
//        Endpoint operation = action.getEndpoint() ;
//        String method = action.request.getMethod() ;
//
//        // Try to route to read service.
//
//        if ( HttpNames.METHOD_GET.equalsIgnoreCase(method) ||
//            HttpNames.METHOD_HEAD.equalsIgnoreCase(method) )
//        {
//            // Graphs Store Protocol, indirect naming, read operations
//            // Try to send to the R service, else drop through to RW service dispatch.
//            if ( ! allowGSP_R(action))
//                ServletOps.errorForbidden("Forbidden: SPARQL Graph Store Protocol : Read operation : "+method) ;
//            executeRequest(action, gspServlet_R) ;
//            return ;
//        }
//
//        // Graphs Store Protocol, indirect naming, write (or read, though actually handled above)
//        // operations on the RW service.
//        if ( ! allowGSP_RW(action))
//            ServletOps.errorForbidden("Forbidden: SPARQL Graph Store Protocol : "+method) ;
//        executeRequest(action, gspServlet_RW) ;
//        return ;
//    }
//
//    private void executeRequest(HttpAction action, ActionSPARQL servlet) {
//        if ( true ) {
//            // Execute an ActionSPARQL.
//            // Bypasses HttpServlet.service to doMethod dispatch.
//            servlet.executeLifecycle(action) ;
//            return ;
//        }
//        if ( false )  {
//            // Execute by calling the whole servlet mechanism.
//            // This causes HttpServlet.service to call the appropriate doMethod.
//            // but the action, and the id, are not passed on and a ne one is created.
//            try { servlet.service(action.request, action.response) ; }
//            catch (ServletException | IOException e) {
//                ServletOps.errorOccurred(e);
//            }
//        }
//    }
//
//    /** Find part after the dataset name: service name or the graph (direct naming) */
//    protected String findTrailing(String uri, String dsname) {
//        if ( dsname.length() >= uri.length() )
//            return "" ;
//        return uri.substring(dsname.length()+1) ;   // Skip the separating "/"
//    }
//
    // Route everything to "doCommon"
    @Override
    protected void doHead(HttpServletRequest request, HttpServletResponse response)
    { doCommon(request, response) ; }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    { doCommon(request, response) ; }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    { doCommon(request, response) ; }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
    { doCommon(request, response) ; }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
    { doCommon(request, response) ; }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
    { doCommon(request, response) ; }
}
