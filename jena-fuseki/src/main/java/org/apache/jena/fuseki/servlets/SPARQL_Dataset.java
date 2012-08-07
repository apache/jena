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

/** This servlet can be attached to a dataset location
 *  and acts as a router for all SPARQL operations
 *  (query, update, graph store, both direct and indirect naming). 
 */
public class SPARQL_Dataset extends SPARQL_ServletBase
{
    // To test: enable in SPARQLServer.configureOneDataset

    // Restructure: 
    //    Remove PlainRequestFlag - just use the query string override.
    //    doCommonWorker without perform.
    // pull 
    // or allow validate(HttpServletRequest) override.
    
    public SPARQL_Dataset(boolean verbose_debug)
    {
        // Split SPARQL_ServletBase or move doCommon stub to ServletBase
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

    private SPARQL_ServletBase queryServlet    = new SPARQL_QueryDataset(verbose_debug) ;
    private SPARQL_ServletBase updateServlet   = new SPARQL_Update(verbose_debug) ;
    // No upload support on the dataset itself - use service (or REST!) 
    //private SPARQL_ServletBase uploadServlet   = new SPARQL_Upload(verbose_debug) ;
    private SPARQL_ServletBase restServlet_RW  = new SPARQL_REST_RW(verbose_debug) ;
    private SPARQL_ServletBase restServlet_R   = new SPARQL_REST_R(verbose_debug) ;
    private SPARQL_ServletBase restQuads       = new REST_Quads(verbose_debug) ;
    
    @Override
    protected void validate(HttpServletRequest request)
    {
        // already checked in SPARQ_Dataset?
    }

    @Override
    protected void doCommonWorker(long id, HttpServletRequest request, HttpServletResponse response)
    {
        String uri = request.getRequestURI() ;
        String method = request.getMethod() ;
        String dsname = findDataset(uri) ;
        String trailing = uri.substring(dsname.length()) ;
        String qs = request.getQueryString() ;
        
        DatasetRef desc = DatasetRegistry.get().get(dsname) ;
        
        //log.info(format("[%d] All: %S %s :: %s ? %s", id, method, dsname, trailing, qs==null?"":qs)) ;
        
        if ( trailing.length() != 0 )
        {
            // Is it a registered service?
            if ( checkDispatch(desc.queryEP, trailing, queryServlet, desc, id, request, response) ) return ; 
            if ( checkDispatch(desc.updateEP, trailing, updateServlet, desc, id, request, response) ) return ; 
            if ( checkDispatch(desc.uploadEP, trailing, restServlet_RW, desc, id, request, response) ) return ; 
            if ( checkDispatch(desc.readGraphStoreEP, trailing, restServlet_R, desc, id, request, response) ) return ; 
            if ( checkDispatch(desc.readWriteGraphStoreEP, trailing, restServlet_RW, desc, id, request, response) ) return ; 
        }
        
        // if no query string => direct naming or REST on the dataset itself.
        if ( qs == null )
        {
            if ( trailing.length() != 0 )
            {
                // Direct naming    
                String absURI = request.getRequestURL().toString() ;
                HttpActionREST a = new HttpActionREST(id, desc, absURI, request, response, verbose_debug) ;
                // Conneg.
                // Check access.
                new SPARQL_REST_RW(verbose_debug).dispatch(a) ;
            }
            else
            {
                restQuads.doCommonWorker(id, request, response) ;
                return ;
            }
        }
        
        if ( trailing.length() != 0 )
        {
            errorBadRequest("Can't invoke a query-string service on a direct named graph") ;
            return ;
        }
        
        datasetQueryString(id, desc, request, response) ;
    }
    
    
    // It's an ?operation on the dataset 
    private void datasetQueryString(long id, DatasetRef desc, HttpServletRequest request, HttpServletResponse response)
    {
        String method = request.getMethod() ;
        // Query string.
        boolean hasParamQuery    = request.getParameter(HttpNames.paramQuery) != null ;
        boolean hasParamRequest  = request.getParameter(HttpNames.paramRequest) != null ;
        boolean hasParamGraph    = request.getParameter(HttpNames.paramGraph) != null ;

        int c = 0 ;
        if ( hasParamQuery ) c++ ;
        if ( hasParamRequest ) c++ ;
        if ( hasParamGraph ) c++ ;
        if ( c > 1 )
            errorBadRequest("Multiple possible actions") ;
        if ( c == 0 )
            errorBadRequest("Query string does not contain a speific action") ;

        // Check an endpoint is registered.
        if ( hasParamQuery )
        {
            if ( desc.queryEP.size() > 0 )
                queryServlet.doCommonWorker(id, request, response) ;
            else
                errorMethodNotAllowed(method) ;
        }

        if ( hasParamRequest )
        {
            if ( desc.updateEP.size() > 0 )
                updateServlet.doCommonWorker(id, request, response) ;
            else
                errorMethodNotAllowed(method) ;
        }

        if ( hasParamGraph )
        {
            if ( desc.readWriteGraphStoreEP.size() > 0 )
                restServlet_RW.doCommonWorker(id, request, response) ;
            else if ( desc.readGraphStoreEP.size() > 0 )
                restServlet_R.doCommonWorker(id, request, response) ;
            else
                errorMethodNotAllowed(method) ;
        }
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
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
    { doCommon(request, response) ; }
    
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
    { doCommon(request, response) ; }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
    { doCommon(request, response) ; }

}

