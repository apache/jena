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

import java.util.Iterator ;
import java.util.List ;

import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.apache.jena.atlas.iterator.Filter ;
import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.web.MediaType ;
import org.apache.jena.fuseki.DEF ;
import org.apache.jena.fuseki.HttpNames ;
import org.apache.jena.fuseki.conneg.ConNeg ;
import org.apache.jena.fuseki.server.DatasetRef ;
import org.apache.jena.fuseki.server.DatasetRegistry ;
import org.apache.jena.riot.WebContent ;
import org.apache.jena.web.HttpSC ;

/** This servlet can be attached to a dataset location
 *  and acts as a falserouter for all SPARQL operations
 *  (query, update, graph store, both direct and indirect naming). 
 */
public abstract class SPARQL_UberServlet extends SPARQL_ServletBase
{
    protected abstract boolean allowQuery(HttpAction action) ;
    protected abstract boolean allowUpdate(HttpAction action) ;
    protected abstract boolean allowREST_R(HttpAction action) ;
    protected abstract boolean allowREST_W(HttpAction action) ;
    protected abstract boolean allowQuadsR(HttpAction action) ;
    protected abstract boolean allowQuadsW(HttpAction action) ;
    
    public static class ReadOnly extends SPARQL_UberServlet
    {
        public ReadOnly(boolean verbose_debug) { super(verbose_debug) ; }
        @Override protected boolean allowQuery(HttpAction action)    { return true ; }
        @Override protected boolean allowUpdate(HttpAction action)   { return false ; }
        @Override protected boolean allowREST_R(HttpAction action)   { return true ; }
        @Override protected boolean allowREST_W(HttpAction action)   { return false ; }
        @Override protected boolean allowQuadsR(HttpAction action)   { return true ; }
        @Override protected boolean allowQuadsW(HttpAction action)   { return false ; }
    }
    
    public static class ReadWrite extends SPARQL_UberServlet
    {
        public ReadWrite(boolean verbose_debug) { super(verbose_debug) ; }
        @Override protected boolean allowQuery(HttpAction action)    { return true ; }
        @Override protected boolean allowUpdate(HttpAction action)   { return true ; }
        @Override protected boolean allowREST_R(HttpAction action)   { return true ; }
        @Override protected boolean allowREST_W(HttpAction action)   { return true ; }
        @Override protected boolean allowQuadsR(HttpAction action)   { return true ; }
        @Override protected boolean allowQuadsW(HttpAction action)   { return true ; }
    }
    
    public static class AccessByConfig extends SPARQL_UberServlet
    {
        public AccessByConfig(boolean verbose_debug) { super(verbose_debug) ; }
        @Override protected boolean allowQuery(HttpAction action)    { return isEnabled(action.getDatasetRef().queryEP) ; }
        @Override protected boolean allowUpdate(HttpAction action)   { return isEnabled(action.getDatasetRef().updateEP) ; }
        @Override protected boolean allowREST_R(HttpAction action)   { return isEnabled(action.getDatasetRef().readGraphStoreEP) || allowREST_W(action); }
        @Override protected boolean allowREST_W(HttpAction action)   { return isEnabled(action.getDatasetRef().readWriteGraphStoreEP) ; }
        // Quad operations tied to presence/absence of GSP.
        @Override protected boolean allowQuadsR(HttpAction action)   { return isEnabled(action.getDatasetRef().readGraphStoreEP) ; }
        @Override protected boolean allowQuadsW(HttpAction action)   { return isEnabled(action.getDatasetRef().readWriteGraphStoreEP) ; }
        
        private boolean isEnabled(List<String> ep) { return ep.size() > 0 ; } 
    }
        
    
    /*  This can be used for a single servlet for everything (über-servlet)
     *  
     *  It can check for a request that looks like a service request and passes it on.
     * This takes precedence over direct naming.
     */
    
    // Refactor? Extract the direct naming handling.
    // To test: enable in SPARQLServer.configureOneDataset
    
    private final SPARQL_ServletBase queryServlet    = new SPARQL_QueryDataset(verbose_debug) ;
    private final SPARQL_ServletBase updateServlet   = new SPARQL_Update(verbose_debug) ;
    private final SPARQL_ServletBase uploadServlet   = new SPARQL_Upload(verbose_debug) ;
    private final SPARQL_REST        restServlet_RW  = new SPARQL_REST_RW(verbose_debug) ;
    private final SPARQL_REST        restServlet_R   = new SPARQL_REST_R(verbose_debug) ;
    private final SPARQL_ServletBase restQuads       = new REST_Quads(verbose_debug) ;
    
    public SPARQL_UberServlet(boolean verbose_debug)
    {
        super(verbose_debug) ;
    }

    private String getEPName(String dsname, List<String> endpoints)
    {
        if (endpoints == null || endpoints.size() == 0) return null ;
        String x = endpoints.get(0) ;
        if ( ! dsname.endsWith("/") )
            x = dsname+"/"+x ;
        else
            x = dsname+x ;
        return x ;
    }
    
    @Override
    protected void validate(HttpServletRequest request)
    { 
        // Left to the underlying implementations.
    }

    @Override
    protected void doCommonWorker(long id, HttpServletRequest request, HttpServletResponse response)
    {
        String uri = request.getRequestURI() ;
        String method = request.getMethod() ;
        String dsname = findDataset(uri) ;
        String trailing = findTrailing(uri, dsname) ;
        String qs = request.getQueryString() ;

        boolean hasParams = request.getParameterMap().size() > 0 ;
        
        // Test for parameters - includes HTML forms.
        boolean hasParamQuery           = request.getParameter(HttpNames.paramQuery) != null ;
        // Include old name "request="
        boolean hasParamUpdate          = request.getParameter(HttpNames.paramUpdate) != null || request.getParameter(HttpNames.paramRequest) != null ;
        boolean hasParamGraph           = request.getParameter(HttpNames.paramGraph) != null ;
        boolean hasParamGraphDefault    = request.getParameter(HttpNames.paramGraphDefault) != null ;
        boolean isForm                  = WebContent.contentTypeForm.equalsIgnoreCase(request.getContentType()) ;

        String ct = request.getContentType() ;
        String charset = request.getCharacterEncoding() ;
        
        MediaType mt = null ;
        if ( ct != null )
            mt = MediaType.create(ct, charset) ;
        
        DatasetRef desc = DatasetRegistry.get().get(dsname) ;
        // The servlets create their own, subclass, action, but it's 
        // convenient to collect everything together. 
        HttpAction action = new HttpAction(id, desc, request, response, verbose_debug) ;
        
        log.info(format("[%d] All: %s %s :: '%s' :: %s ? %s", id, method, dsname, trailing, (mt==null?"<none>":mt), (qs==null?"":qs))) ;
                       
        boolean hasTrailing = ( trailing.length() != 0 ) ;
        
        if ( ! hasTrailing && ! hasParams )
        {
            restQuads.doCommonWorker(id, request, response) ;
            return ;
        }
        
        if ( ! hasTrailing )
        {
            // Has params of some kind.
            if ( hasParamQuery || WebContent.contentTypeSPARQLQuery.equalsIgnoreCase(ct) )
            {
                // SPARQL Query
                if ( ! allowQuery(action))
                    errorForbidden("Forbidden: SPARQL query") ; 
                executeRequest(desc, queryServlet, desc.queryEP, id, request, response) ;
                return ;
            }
                 
            if ( hasParamUpdate || WebContent.contentTypeSPARQLUpdate.equalsIgnoreCase(ct) )
            {
                // SPARQL Update
                if ( ! allowQuery(action))
                    errorForbidden("Forbidden: SPARQL query") ; 
                executeRequest(desc, updateServlet, desc.updateEP, id, request, response) ;
                return ;
            }
            
            if ( hasParamGraph || hasParamGraphDefault )
            {
                doGraphStoreProtocol(action) ;
                return ;
            }
            
            errorBadRequest("Malformed request") ;
        }
        
        final boolean checkForPossibleService = true ;
        if ( checkForPossibleService )
        {
            // There is a trailing part.
            // Check it's not the same name as a registered service.
            // If so, dispatch to that service.
            if ( checkDispatch(desc.queryEP, trailing, queryServlet, desc, id, request, response) ) return ; 
            if ( checkDispatch(desc.updateEP, trailing, updateServlet, desc, id, request, response) ) return ; 
            if ( checkDispatch(desc.uploadEP, trailing, uploadServlet, desc, id, request, response) ) return ; 
            if ( checkDispatch(desc.readGraphStoreEP, trailing, restServlet_R, desc, id, request, response) ) return ; 
            if ( checkDispatch(desc.readWriteGraphStoreEP, trailing, restServlet_RW, desc, id, request, response) ) return ; 
        }       
        // There is a trailing part - params are illegal by this point.
        if ( hasParams )
            // Revisit to include query-on-one-graph 
            errorBadRequest("Can't invoke a query-string service on a direct named graph") ; 

        // There is a trailing part - not a service, no params ==> GSP direct naming.
        doGraphStoreProtocol(action) ;
    }
    
    private void doGraphStoreProtocol(HttpAction action)
    {
        // The GSP servlets handle direct and indirect naming. 
        DatasetRef desc = action.getDatasetRef();
        String method = action.request.getMethod() ;
        
        if ( HttpNames.METHOD_GET.equalsIgnoreCase(method) ||
             HttpNames.METHOD_HEAD.equalsIgnoreCase(method) ) 
       {
           if ( ! allowREST_R(action))
               errorForbidden("Forbidden: SPARQL Graph Store Protocol : Read operation : "+method) ;
           // Graphs Store Protocol, indirect naming, read
           // Indirect naming. Prefer the R service if available.
           if ( desc.readGraphStoreEP.size() > 0 )
               executeRequest(desc, restServlet_R, desc.readGraphStoreEP, action.id, action.request, action.response) ;
           else if ( desc.readWriteGraphStoreEP.size() > 0 )
               executeRequest(desc, restServlet_RW, desc.readWriteGraphStoreEP, action.id, action.request, action.response) ;
           else
               errorMethodNotAllowed(method) ;
           return ;
       }
       
       // Graphs Store Protocol, indirect naming, write
       if ( ! allowREST_W(action))
           errorForbidden("Forbidden: SPARQL Graph Store Protocol : Write operation : "+method) ;
       executeRequest(desc, restServlet_RW, desc.readWriteGraphStoreEP, action.id, action.request, action.response) ;
       return ;
    }

    private void executeRequest(DatasetRef desc, SPARQL_ServletBase servlet, List<String> endpointList, long id,
                                HttpServletRequest request, HttpServletResponse response)
    {
        if ( endpointList == null || endpointList.size() == 0 )
            errorMethodNotAllowed(request.getMethod()) ;
        servlet.doCommonWorker(id, request, response) ;
    }

    private void executeRequest(DatasetRef desc, SPARQL_ServletBase servlet, long id,
                                HttpServletRequest request, HttpServletResponse response)
    {
        servlet.doCommonWorker(id, request, response) ;
//      // Forwarded dispatch.
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

    
    
    protected static MediaType contentNegotationQuads(HttpAction action)
    {
        MediaType mt = ConNeg.chooseContentType(action.request, DEF.quadsOffer, DEF.acceptNQuads) ;
        if ( mt == null )
            return null ;
        if ( mt.getContentType() != null )
            action.response.setContentType(mt.getContentType());
        if ( mt.getCharset() != null )
        action.response.setCharacterEncoding(mt.getCharset()) ;
        return mt ;
    }

    private boolean checkDispatch(List<String> endpointNames, String srvName , SPARQL_ServletBase servlet , DatasetRef desc, long id, 
                                  HttpServletRequest request, HttpServletResponse response)
    {
        if ( ! endpointNames.contains(srvName) )
            return false ;
        servlet.doCommonWorker(id, request, response) ;
        return true ;
    }

    @Override
    protected void perform(long id, DatasetRef desc, HttpServletRequest request, HttpServletResponse response)
    {
        // Dummy - restructure SPARQL_ServletBase?
        error(HttpSC.INTERNAL_SERVER_ERROR_500, "Operation directed to general indirection servlet") ;
    }
    
    /** Find the dataset name even if direct naming */ 
    protected static String findDataset(final String uri) 
    {
        // Find the dataset.
        Iterator<String> datasets = DatasetRegistry.get().keys() ;
        Filter<String> matchDS = new Filter<String>()
            {
                @Override
                public boolean accept(String datasetname)
                {
                    return uri.startsWith(datasetname) ;
                }
            } ;
        String ds = Iter.first(datasets, matchDS) ;
        return ds ;
    }

    /** Find the dataset name even if direct naming */ 
    protected static String findTrailing(String uri, String dsname) 
    {
        if ( dsname.length() >= uri.length() )
            return "" ;
        return uri.substring(dsname.length()+1) ;   // Skip the separating "/"
    }

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

