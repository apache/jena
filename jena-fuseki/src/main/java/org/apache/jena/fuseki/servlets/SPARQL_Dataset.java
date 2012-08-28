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

    private SPARQL_ServletBase queryServlet    = new SPARQL_QueryDataset(verbose_debug) ;
    private SPARQL_ServletBase updateServlet   = new SPARQL_Update(verbose_debug) ;
    // No upload support on the dataset itself - use service (or REST!) 
    //private SPARQL_ServletBase uploadServlet   = new SPARQL_Upload(verbose_debug) ;
    private SPARQL_REST_RW     restServlet_RW  = new SPARQL_REST_RW(verbose_debug) ;
    private SPARQL_REST_R      restServlet_R   = new SPARQL_REST_R(verbose_debug) ;
    private SPARQL_ServletBase restQuads       = new REST_Quads(verbose_debug) ;

    
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
    }

    // Development : calls to other servlets marked ****
    // This will need to do a proper servlet dispatch if they are going to be filterd (security, compression).
    // TODO Handling content-type: application/sparql-query and application/sparql-update  
    // If forwarding, id is added again.
    // For an über-servlet, directly call the other servlets.   
    
    @Override
    protected void doCommonWorker(long id, HttpServletRequest request, HttpServletResponse response)
    {
        String uri = request.getRequestURI() ;
        String method = request.getMethod() ;
        String dsname = findDataset(uri) ;
        String trailing = uri.substring(dsname.length()) ;
        String qs = request.getQueryString() ;
        
        String ct = request.getContentType() ;
        String charset = request.getCharacterEncoding() ;
        
        MediaType mt = null ;
        if ( ct != null )
            mt = MediaType.create(ct, charset) ;
        
        DatasetRef desc = DatasetRegistry.get().get(dsname) ;
        
        log.info(format("[%d] All: %S %s :: %s :: %s ? %s", id, method, dsname, trailing, (mt==null?"<none>":mt), (qs==null?"":qs))) ;
                       
        boolean hasTrailing = ( trailing.length() != 0 ) ;
        boolean hasQueryString = ( qs != null ) ;
        
        /* Better:
         *   Is it a query? => dispatch 
         *   Is it an update? => dispatch
         *   Trailing? =>  direct naming
         *   Dataset REST operation.
         */
        
        if ( hasTrailing )
        {
            // Is it a registered service?
            if ( checkDispatch(desc.queryEP, trailing, queryServlet, desc, id, request, response) ) return ; 
            if ( checkDispatch(desc.updateEP, trailing, updateServlet, desc, id, request, response) ) return ; 
            if ( checkDispatch(desc.uploadEP, trailing, restServlet_RW, desc, id, request, response) ) return ; 
            if ( checkDispatch(desc.readGraphStoreEP, trailing, restServlet_R, desc, id, request, response) ) return ; 
            if ( checkDispatch(desc.readWriteGraphStoreEP, trailing, restServlet_RW, desc, id, request, response) ) return ; 
        }
        
        if ( hasTrailing && hasQueryString )
            // Revisit
            errorBadRequest("Can't invoke a query-string service on a direct named graph") ; 
        
        /* Sort out:
         * No trailing.
         * query
         *   GET query string, ?query
         *   POST form, ?query
         *   POST content-type application/sparql-query
         * update
         *   GET form, ?update
         *   POST content-type application/sparql-update
         * REST:
         *   content-type != application/sparql-query, application/sparql-update
         *   GET, POST, PUT, (DELETE)
         *   
         * Trailing:
         *   GET, POST, PUT, (DELETE), direct naming
         *   ?? Query or update on a graph only.
         */
        
        // if no query string => direct naming or REST on the dataset itself.
        if ( ! hasQueryString )
        {
            // what about Content-type: application/sparql-query and application/sparql-update   
            
            if ( hasTrailing )
            {
                // Direct naming to indirect naming.
                String absURI = request.getRequestURL().toString() ;
                HttpActionREST a = new HttpActionREST(id, desc, absURI, request, response, verbose_debug) ;
                
                if ( desc.readWriteGraphStoreEP.size() > 0 )
                    // ****
                    restServlet_RW.dispatch(a) ;
                else if ( desc.readGraphStoreEP.size() > 0 )
                    // ****
                    restServlet_R.dispatch(a) ;
                else
                    errorMethodNotAllowed(method) ;
                return ;
            }
            else
            {
                // No trailing name, no query string => 
                //    REST on dataset
                //    POST-query
                //    POST-update
                // Direct action on the dataset itself.
                restQuads.doCommonWorker(id, request, response) ;
                return ;
            }
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
        if ( hasParamQuery )   c++ ;
        if ( hasParamRequest ) c++ ;
        if ( hasParamGraph )   c++ ;
        if ( c > 1 )
            errorBadRequest("Multiple possible actions") ;
        if ( c == 0 )
            errorBadRequest("Query string does not contain a specific action") ;

        // ****
        // Check an endpoint is registered.
        if ( hasParamQuery )
        {
//            // ---- Call by forwarding
//            if ( false )
//            {
//                // Call by dispatch - follows the servlet chain. 
//                String x = getEPName(desc.name, desc.queryEP) ;
//                if ( x == null )
//                    errorMethodNotAllowed(method) ;
//                else
//                {
//                    //request.setAttribute("org.apache.jena.fuseki.id", id) ;
//                    forwardServlet(x, request, response) ;
//                }
//            }
            // ---- Call direct.
            if ( desc.queryEP.size() > 0 )
                queryServlet.doCommonWorker(id, request, response) ;
            else
                errorMethodNotAllowed(method) ;
        }

        if ( hasParamRequest )
        {
            if ( desc.updateEP.size() > 0 )
                // ****
                updateServlet.doCommonWorker(id, request, response) ;
            else
                errorMethodNotAllowed(method) ;
        }

        if ( hasParamGraph )
        {
            if ( desc.readWriteGraphStoreEP.size() > 0 )
                // ****
                restServlet_RW.doCommonWorker(id, request, response) ;
            else if ( desc.readGraphStoreEP.size() > 0 )
                // ****
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

    private void forwardServlet(String target, HttpServletRequest request, HttpServletResponse response)
    {
        try
        {
            // relative
            request.getRequestDispatcher(target).forward(request, response) ;
            
            // Absolute
            // getServletContext().getRequestDispatcher(target)
        } catch (Exception e) { errorOccurred(e) ; }
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

