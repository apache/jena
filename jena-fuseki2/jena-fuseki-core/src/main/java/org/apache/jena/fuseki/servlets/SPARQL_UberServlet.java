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

import static java.lang.String.format ;
import static org.apache.jena.riot.WebContent.contentTypeSPARQLQuery ;
import static org.apache.jena.riot.WebContent.contentTypeSPARQLUpdate ;

import java.util.List ;

import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.apache.jena.atlas.web.MediaType ;
import org.apache.jena.fuseki.DEF ;
import org.apache.jena.fuseki.FusekiException ;
import org.apache.jena.fuseki.conneg.ConNeg ;
import org.apache.jena.fuseki.server.DataAccessPoint ;
import org.apache.jena.fuseki.server.DataService ;
import org.apache.jena.fuseki.server.Endpoint ;
import org.apache.jena.fuseki.server.OperationName ;
import org.apache.jena.riot.web.HttpNames ;

/** This servlet can be attached to a dataset location
 *  and acts as a router for all SPARQL operations
 *  (query, update, graph store, both direct and 
 *  indirect naming, quads operations on a dataset and
 *  ?query and ?update directly on a dataset.) 
 */
public abstract class SPARQL_UberServlet extends ActionSPARQL
{
    protected abstract boolean allowQuery(HttpAction action) ;
    protected abstract boolean allowUpdate(HttpAction action) ;
    protected abstract boolean allowREST_R(HttpAction action) ;
    protected abstract boolean allowREST_W(HttpAction action) ;
    protected abstract boolean allowQuadsR(HttpAction action) ;
    protected abstract boolean allowQuadsW(HttpAction action) ;
    
    public static class ReadOnly extends SPARQL_UberServlet
    {
        public ReadOnly()    { super() ; }
        @Override protected boolean allowQuery(HttpAction action)    { return true ; }
        @Override protected boolean allowUpdate(HttpAction action)   { return false ; }
        @Override protected boolean allowREST_R(HttpAction action)   { return true ; }
        @Override protected boolean allowREST_W(HttpAction action)   { return false ; }
        @Override protected boolean allowQuadsR(HttpAction action)   { return true ; }
        @Override protected boolean allowQuadsW(HttpAction action)   { return false ; }
    }

    public static class ReadWrite extends SPARQL_UberServlet
    {
        public ReadWrite()    { super() ; }
        @Override protected boolean allowQuery(HttpAction action)    { return true ; }
        @Override protected boolean allowUpdate(HttpAction action)   { return true ; }
        @Override protected boolean allowREST_R(HttpAction action)   { return true ; }
        @Override protected boolean allowREST_W(HttpAction action)   { return true ; }
        @Override protected boolean allowQuadsR(HttpAction action)   { return true ; }
        @Override protected boolean allowQuadsW(HttpAction action)   { return true ; }
    }

    public static class AccessByConfig extends SPARQL_UberServlet
    {
        public AccessByConfig()    { super() ; }
        @Override protected boolean allowQuery(HttpAction action)    { return isEnabled(action, OperationName.Query) ; }
        @Override protected boolean allowUpdate(HttpAction action)   { return isEnabled(action, OperationName.Update) ; }
        @Override protected boolean allowREST_R(HttpAction action)   { return isEnabled(action, OperationName.GSP_R) || isEnabled(action, OperationName.GSP) ; }
        @Override protected boolean allowREST_W(HttpAction action)   { return isEnabled(action, OperationName.GSP) ; }
        // Quad operations tied to presence/absence of GSP.
        @Override protected boolean allowQuadsR(HttpAction action)   { return isEnabled(action, OperationName.GSP_R) || isEnabled(action, OperationName.GSP) ; }
        @Override protected boolean allowQuadsW(HttpAction action)   { return isEnabled(action, OperationName.GSP) ; }

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
    
    /*  This can be used for a single servlet for everything (über-servlet)
     *  
     *  It can check for a request that looks like a service request and passes it on.
     * This takes precedence over direct naming.
     */
    
    // Refactor? Extract the direct naming handling.
    // To test: enable in SPARQLServer.configureOneDataset
    
    private final ActionSPARQL queryServlet    = new SPARQL_QueryDataset() ;
    private final ActionSPARQL updateServlet   = new SPARQL_Update() ;
    private final ActionSPARQL uploadServlet   = new SPARQL_Upload() ;
    private final ActionSPARQL gspServlet_R    = new SPARQL_GSP_R() ;
    private final ActionSPARQL gspServlet_RW   = new SPARQL_GSP_RW() ;
    private final ActionSPARQL restQuads_R     = new REST_Quads_R() ;
    private final ActionSPARQL restQuads_RW    = new REST_Quads_RW() ;
    
    public SPARQL_UberServlet() { super(); }

    private String getEPName(String dsname, List<String> endpoints) {
        if (endpoints == null || endpoints.size() == 0) 
            return null ;
        String x = endpoints.get(0) ;
        if ( ! dsname.endsWith("/") )
            x = dsname+"/"+x ;
        else
            x = dsname+x ;
        return x ;
    }
    
    // These calls should not happen because we hook in at executeAction
    @Override protected void validate(HttpAction action) { throw new FusekiException("Call to SPARQL_UberServlet.validate") ; }
    @Override protected void perform(HttpAction action)  { throw new FusekiException("Call to SPARQL_UberServlet.perform") ; }

    /** Map request to uri in the registry.
     *  null means no mapping done 
     */
    @Override
    protected String mapRequestToDataset(HttpAction action) {
        String uri = ActionLib.removeContextPath(action) ;
        return ActionLib.mapRequestToDatasetLongest$(uri) ;
    }
    
    /** Intercept the processing cycle at the point where the action has been set up,
     *  the dataset target decided but no validation or execution has been done, 
     *  nor any stats have been done.
     */
    @Override
    protected void executeAction(HttpAction action) {
        long id = action.id ;
        HttpServletRequest request = action.request ;
        HttpServletResponse response = action.response ;
        String actionURI = action.getActionURI() ;            // No context path
        String method = request.getMethod() ;
        
        DataAccessPoint desc = action.getDataAccessPoint() ;
        DataService dSrv = action.getDataService() ;

//        if ( ! dSrv.isActive() )
//            ServletOps.error(HttpSC.SERVICE_UNAVAILABLE_503, "Dataset not currently active");
        
        // Part after the DataAccessPoint (dataset) name.
        String trailing = findTrailing(actionURI, desc.getName()) ;
        String qs = request.getQueryString() ;

        boolean hasParams = request.getParameterMap().size() > 0 ;
        
        // Test for parameters - includes HTML forms.
        boolean hasParamQuery           = request.getParameter(HttpNames.paramQuery) != null ;
        // Include old name "request="
        boolean hasParamUpdate          = request.getParameter(HttpNames.paramUpdate) != null || request.getParameter(HttpNames.paramRequest) != null ;
        boolean hasParamGraph           = request.getParameter(HttpNames.paramGraph) != null ;
        boolean hasParamGraphDefault    = request.getParameter(HttpNames.paramGraphDefault) != null ;

        String ct = request.getContentType() ;
        String charset = request.getCharacterEncoding() ;
        
        MediaType mt = null ;
        if ( ct != null )
            mt = MediaType.create(ct, charset) ;
        
        if (action.log.isInfoEnabled() ) {
            //String cxt = action.getContextPath() ;
            action.log.info(format("[%d] %s %s :: '%s' :: %s ? %s", id, method, desc.getName(), trailing, (mt==null?"<none>":mt), (qs==null?"":qs))) ;
        }
                       
        boolean hasTrailing = ( trailing.length() != 0 ) ;
        
        if ( !hasTrailing && !hasParams ) {
            // REST quads operations.
//            if ( serviceDispatch(action, OperationName.GSP_R, restQuads_R) ) return ;
//            if ( serviceDispatch(action, OperationName.GSP, restQuads_RW) ) return ;
            
//            boolean isPOST = method.equals(HttpNames.METHOD_POST) ;
//            if ( isPOST ) {
//                // Differentiate SPARQL query, SPARQL update by content type.
//            }
            
            // REST dataset.
            boolean isGET = method.equals(HttpNames.METHOD_GET) ;
            boolean isHEAD = method.equals(HttpNames.METHOD_HEAD) ;
            
            // Check enabled.
            if ( isGET || isHEAD ) {
                if ( allowREST_R(action) )
                    restQuads_R.executeLifecycle(action) ;
                else
                    ServletOps.errorForbidden("Forbidden: "+method+" on dataset") ;
            }
            if ( allowREST_W(action) )
                restQuads_RW.executeLifecycle(action) ;
            else
                ServletOps.errorForbidden("Forbidden: "+method+" on dataset") ;
            return ;
        }
        
        if ( !hasTrailing ) {
            boolean isPOST = action.getRequest().getMethod().equals(HttpNames.METHOD_POST) ;
            // Nothing after the DataAccessPoint i.e Dataset by name.
            // e.g.  http://localhost:3030/ds?query=
            // Query - GET or POST.
            // Query - ?query= or body of application/sparql-query
            if ( hasParamQuery || ( isPOST && contentTypeSPARQLQuery.equalsIgnoreCase(ct) ) ) {
                // SPARQL Query
                if ( !allowQuery(action) )
                    ServletOps.errorForbidden("Forbidden: SPARQL query") ;
                executeRequest(action, queryServlet) ;
                return ;
            }

            // Insist on POST for update.
            // Update - ?update= or body of application/sparql-update
            if ( isPOST && ( hasParamUpdate || contentTypeSPARQLUpdate.equalsIgnoreCase(ct) ) ) {
                // SPARQL Update
                if ( !allowUpdate(action) )
                    ServletOps.errorForbidden("Forbidden: SPARQL update") ;
                executeRequest(action, updateServlet) ;
                return ;
            }
            
            // ?graph=, ?default
            if ( hasParamGraph || hasParamGraphDefault ) {
                doGraphStoreProtocol(action) ;
                return ;
            }
            
            ServletOps.errorBadRequest("Malformed request") ;
            ServletOps.errorForbidden("Forbidden: SPARQL Graph Store Protocol : Read operation : "+method) ;
        }
        
        final boolean checkForPossibleService = true ;
        if ( checkForPossibleService && action.getEndpoint() != null ) {
            // There is a trailing part.
            // Check it's not the same name as a registered service.
            // If so, dispatch to that service.
            if ( serviceDispatch(action, OperationName.Query, queryServlet) ) return ; 
            if ( serviceDispatch(action, OperationName.Update, updateServlet) ) return ; 
            if ( serviceDispatch(action, OperationName.Upload, uploadServlet) ) return ;
            if ( hasParams ) {
                if ( serviceDispatch(action, OperationName.GSP_R, gspServlet_R) ) return ; 
                if ( serviceDispatch(action, OperationName.GSP, gspServlet_RW) ) return ;
            } else {
                // No parameters - do as a quads operation on the dataset.
                if ( serviceDispatch(action, OperationName.GSP_R, restQuads_R) ) return ;
                if ( serviceDispatch(action, OperationName.GSP, restQuads_RW) ) return ;
            }
        }
        // There is a trailing part - params are illegal by this point.
        if ( hasParams )
            // ?? Revisit to include query-on-one-graph 
            //errorBadRequest("Can't invoke a query-string service on a direct named graph") ;
            ServletOps.errorNotFound("Not found: dataset='"+printName(desc.getName())+"' service='"+printName(trailing)+"'");

        // There is a trailing part - not a service, no params ==> GSP direct naming.
        doGraphStoreProtocol(action) ;
    }
    
    /** See if the operation is enabled for this setup.
     * Return true if dispatched 
     */
    private boolean serviceDispatch(HttpAction action, OperationName opName, ActionSPARQL servlet) {
        Endpoint operation = action.getEndpoint() ;
        if ( operation == null )
            return false ;
        if ( ! operation.isType(opName) ) 
            return false ;
        // Handle OPTIONS specially.
//        if ( action.getRequest().getMethod().equals(HttpNames.METHOD_OPTIONS) ) {
//            // See also ServletBase.CORS_ENABLED
//            //action.log.info(format("[%d] %s", action.id, action.getMethod())) ;
//            setCommonHeadersForOptions(action.getResponse()) ;
//            ServletOps.success(action);
//            return true ;
//        }
        executeRequest(action, servlet) ;
        return true ;
    }
    private String printName(String x) {
        if ( x.startsWith("/") )
            return x.substring(1) ;
        return x ;
    }
    
    private void doGraphStoreProtocol(HttpAction action) {
        // The GSP servlets handle direct and indirect naming. 
        Endpoint operation = action.getEndpoint() ;
        String method = action.request.getMethod() ;

        // Try to route to read service.

        if ( HttpNames.METHOD_GET.equalsIgnoreCase(method) ||
            HttpNames.METHOD_HEAD.equalsIgnoreCase(method) ) 
        {
            // Graphs Store Protocol, indirect naming, read operations
            // Try to send to the R service, else drop through to RW service dispatch.
            if ( allowREST_R(action)) 
                ServletOps.errorForbidden("Forbidden: SPARQL Graph Store Protocol : Read operation : "+method) ;
            executeRequest(action, gspServlet_R) ;
            return ;
        }

        // Graphs Store Protocol, indirect naming, write (or read, though actually handled above)
        // operations on the RW service.
        if ( ! allowREST_W(action))
            ServletOps.errorForbidden("Forbidden: SPARQL Graph Store Protocol : "+method) ;
        executeRequest(action, gspServlet_RW) ;
        return ;
    }

    private void executeRequest(HttpAction action, ActionSPARQL servlet) {
        servlet.executeLifecycle(action) ;
        // A call to "doCommon" or a forwarded dispatch looses "action".
//      try
//      {
//          String target = getEPName(desc.name, endpointList) ;
//          if ( target == null )
//              errorMethodNotAllowed(request.getMethod()) ;
//          // ** relative servlet forward
//          request.getRequestDispatcher(target).forward(request, response) ;    
//          // ** absolute srvlet forward
//          // getServletContext().getRequestDispatcher(target) ;
//      } catch (Exception e) { errorOccurred(e) ; }        
    }

    protected static MediaType contentNegotationQuads(HttpAction action) {
        MediaType mt = ConNeg.chooseContentType(action.request, DEF.quadsOffer, DEF.acceptNQuads) ;
        if ( mt == null )
            return null ;
        if ( mt.getContentType() != null )
            action.response.setContentType(mt.getContentType());
        if ( mt.getCharset() != null )
        action.response.setCharacterEncoding(mt.getCharset()) ;
        return mt ;
    }

    /** Find part after the dataset name: service name or the graph (direct naming) */ 
    protected String findTrailing(String uri, String dsname) {
        if ( dsname.length() >= uri.length() )
            return "" ;
        return uri.substring(dsname.length()+1) ;   // Skip the separating "/"
    }

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
