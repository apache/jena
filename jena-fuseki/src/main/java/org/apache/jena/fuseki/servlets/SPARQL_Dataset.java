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

import org.apache.jena.fuseki.DEF ;
import org.apache.jena.fuseki.HttpNames ;
import org.apache.jena.fuseki.conneg.ConNeg ;
import org.apache.jena.fuseki.http.HttpSC ;
import org.apache.jena.fuseki.server.DatasetRef ;
import org.apache.jena.fuseki.server.DatasetRegistry ;
import org.apache.jena.fuseki.servlets.SPARQL_REST.HttpActionREST ;
import org.openjena.atlas.iterator.Filter ;
import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.web.MediaType ;
import org.openjena.riot.WebContent ;

/** This servlet can be attached to a dataset location
 *  and acts as a router for all SPARQL operations
 *  (query, update, graph store, both direct and indirect naming). 
 */
public class SPARQL_Dataset extends SPARQL_ServletBase
{
    /*  This can be used for
     *  1/ a single servlet for everything (über-servlet)
     *  2/ just direct naming for a dataset with other services  
     *  May need refactoring to separate those 2 functions. 
     */   
    // To test: enable in SPARQLServer.configureOneDataset
    
    private final SPARQL_ServletBase queryServlet    = new SPARQL_QueryDataset(verbose_debug) ;
    private final SPARQL_ServletBase updateServlet   = new SPARQL_Update(verbose_debug) ;
    private SPARQL_ServletBase       uploadServlet   = new SPARQL_Upload(verbose_debug) ;
    private final SPARQL_REST        restServlet_RW  = new SPARQL_REST_RW(verbose_debug) ;
    private final SPARQL_REST        restServlet_R   = new SPARQL_REST_R(verbose_debug) ;
    private SPARQL_ServletBase       restQuads       = new REST_Quads(verbose_debug) ;
    
    public SPARQL_Dataset(boolean verbose_debug)
    {
        super(verbose_debug) ;
    }

    protected String findDataset(final String uri) 
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
        String trailing = uri.substring(dsname.length()+1) ;    // Skip the "/"
        String qs = request.getQueryString() ;
        
        boolean hasParams               = request.getParameterMap().size() != 0 ;
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
        
        log.info(format("[%d] All: %S %s :: %s :: %s ? %s", id, method, dsname, trailing, (mt==null?"<none>":mt), (qs==null?"":qs))) ;
                       
        boolean hasTrailing = ( trailing.length() != 0 ) ;
        
        if ( ! hasTrailing && ! hasParams )
        {
            restQuads.doCommonWorker(id, request, response) ;
            return ;
        }
        
        if ( ! hasTrailing )
        {
            // Has params of some kind.
            if ( hasParamQuery || 
                WebContent.contentTypeSPARQLQuery.equalsIgnoreCase(ct) )
                
            {
                // query
                executeRequest(desc, queryServlet, desc.queryEP, id, request, response) ;
                return ;
            }
                 
            if ( hasParamUpdate ||
                WebContent.contentTypeSPARQLUpdate.equalsIgnoreCase(ct) )
            {
                // update
                executeRequest(desc, updateServlet, desc.updateEP, id, request, response) ;
                return ;

            }
            
            if ( hasParamGraph || hasParamGraphDefault )
            {
                // Direct naming.  Convert to indirect naming, Graph store protocol.
                // Prefer the RW service.
                if ( desc.readWriteGraphStoreEP.size() > 0 )
                    executeRequest(desc, restServlet_RW, desc.readWriteGraphStoreEP, id, request, response) ;
                else
                    executeRequest(desc, restServlet_R, desc.readGraphStoreEP, id, request, response) ;
                return ;
            }
            errorBadRequest("Malformed request") ;
        }

        // There is a trailing part.
        // Check it's not the same name as a registered service.
        // If so, dispatch to that service.
        if ( checkDispatch(desc.queryEP, trailing, queryServlet, desc, id, request, response) ) return ; 
        if ( checkDispatch(desc.updateEP, trailing, updateServlet, desc, id, request, response) ) return ; 
        if ( checkDispatch(desc.uploadEP, trailing, uploadServlet, desc, id, request, response) ) return ; 
        if ( checkDispatch(desc.readGraphStoreEP, trailing, restServlet_R, desc, id, request, response) ) return ; 
        if ( checkDispatch(desc.readWriteGraphStoreEP, trailing, restServlet_RW, desc, id, request, response) ) return ; 
       
        // There is a trailing part - params are illegal by this point.
        if ( hasParams )
            // Revisit to include query-on-one-graph 
            errorBadRequest("Can't invoke a query-string service on a direct named graph") ; 

        // There is a trailing part - not a service, no params ==> direct naming.
        // Direct naming to indirect naming.
        doCommonWorkerDirectNaming(id, request, response) ;
    }
    
    private void doCommonWorkerDirectNaming(long id, HttpServletRequest request, HttpServletResponse response)
    {
        // again ... but ready for a direct naming servlet. 
        String uri = request.getRequestURI() ;
        String dsname = findDataset(uri) ;
        DatasetRef desc = DatasetRegistry.get().get(dsname) ;
        
        String absURI = request.getRequestURL().toString() ;
        HttpActionREST a = new HttpActionREST(id, desc, absURI, request, response, verbose_debug) ;

        if ( desc.readWriteGraphStoreEP.size() > 0 )
            // ****
            restServlet_RW.dispatch(a) ;
        else if ( desc.readGraphStoreEP.size() > 0 )
            // ****
            restServlet_R.dispatch(a) ;
        else
            errorMethodNotAllowed(request.getMethod()) ;
    }

    private void executeRequest(DatasetRef desc, SPARQL_ServletBase servlet, List<String> endpointList, long id,
                                HttpServletRequest request, HttpServletResponse response)
    {
        // Direct dispatch
        if ( endpointList.size() > 0 )
            servlet.doCommonWorker(id, request, response) ;
        else
            errorMethodNotAllowed(request.getMethod()) ;
//        // Forwarded dispatch.
//        try
//        {
//            String target = getEPName(desc.name, endpointList) ;
//            if ( target == null )
//                errorMethodNotAllowed(request.getMethod()) ;
//            // ** relative servlet forward
//            request.getRequestDispatcher(target).forward(request, response) ;
//            // ** absolute srvlet forward
//            // getServletContext().getRequestDispatcher(target) ;
//        } catch (Exception e) { errorOccurred(e) ; }
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

