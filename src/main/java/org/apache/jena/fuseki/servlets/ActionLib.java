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

import org.apache.jena.atlas.web.AcceptList ;
import org.apache.jena.atlas.web.MediaType ;
import org.apache.jena.fuseki.DEF ;
import org.apache.jena.fuseki.conneg.ConNeg ;
import org.apache.jena.fuseki.server.DatasetRef ;
import org.apache.jena.fuseki.server.DatasetRegistry ;

/** Operations related to servlets */

public class ActionLib {

    
    // TODO Combine ServletOps?
    
    /** Map request to uri in the registry.
     * A possible implementation for mapRequestToDataset(String)
     *  that assumes the form /dataset/service 
     *  Returning null means no mapping found. 
     */
    
    public static String mapRequestToDataset(String uri)
    {
        // Chop off trailing part - the service selector
        // e.g. /dataset/sparql => /dataset 
        int i = uri.lastIndexOf('/') ;
        if ( i == -1 )
            return null ;
        if ( i == 0 )
        {
            // started with '/' - leave.
            return uri ;
        }
        
        return uri.substring(0, i) ;
    }

    public static String mapRequestToService(DatasetRef dsRef, String uri, String datasetURI)
    {
        if ( dsRef == null )
            return "" ;
        if ( dsRef.name.length() >= uri.length() )
            return "" ;
        return uri.substring(dsRef.name.length()+1) ;   // Skip the separating "/"
        
    }
    
    /** Implementation of mapRequestToDataset(String) that looks for
     * the longest match in the registry.
     * This includes use in direct naming GSP. 
     */
    public static String mapRequestToDatasetLongest$(String uri) 
    {
        if ( uri == null )
            return null ;
        
        // This covers local, using the URI as a direct name for
        // a graph, not just using the indirect ?graph= or ?default 
        // forms.

        String ds = null ;
        for ( String ds2 : DatasetRegistry.get().keys() ) {
            if ( ! uri.startsWith(ds2) )
                continue ;

            if ( ds == null )
            {
                ds = ds2 ;
                continue ; 
            }
            if ( ds.length() < ds2.length() )
            {
                ds = ds2 ;
                continue ;
            }
        }
        return ds ;
    }

    public static String wholeRequestURL(HttpServletRequest request) {
        StringBuffer sb = request.getRequestURL() ;
        String queryString = request.getQueryString() ;
        if ( queryString != null ) {
            sb.append("?") ;
            sb.append(queryString) ;
        }
        return sb.toString() ;
    }

    /* 
     * The context path can be:
     * "" for the root context
     * "/webapp" for named contexts
     * so:
     * "/dataset/server" becomes "/dataset/server"
     * "/webapp/dataset/server" becomes "/dataset/server"
     */
    public static String removeContextPath(HttpAction action) {
//        Log.info(this, "URI                     = '"+action.request.getRequestURI()) ;
//        Log.info(this, "Context path            = '"+action.request.getContextPath()+"'") ;
//        Log.info(this, "Servlet path            = '"+action.request.getServletPath()+"'") ;
//        ServletContext cxt = this.getServletContext() ;
//        Log.info(this, "ServletContext path     = '"+cxt.getContextPath()+"'") ;

        String uri = action.request.getRequestURI() ;
        String contextPath = action.request.getServletContext().getContextPath() ;
        if ( contextPath == null )
            return uri ;
        if ( contextPath.isEmpty())
            return uri ;
        String x = uri ;
        if ( uri.startsWith(contextPath) )
            x = uri.substring(contextPath.length()) ;
        //log.info("removeContext: uri = "+uri+" contextPath="+contextPath+ "--> x="+x) ;
        return x ;
    }

    /** Negotiate the content-type and set the response headers */ 
    public static MediaType contentNegotation(HttpAction action, AcceptList myPrefs,
                                              MediaType defaultMediaType) {
        MediaType mt = ConNeg.chooseContentType(action.request, myPrefs, defaultMediaType) ;
        if ( mt == null )
            return null ;
        if ( mt.getContentType() != null )
            action.response.setContentType(mt.getContentType()) ;
        if ( mt.getCharset() != null )
            action.response.setCharacterEncoding(mt.getCharset()) ;
        return mt ;
    }
    
    /** Negotiate the content-type for an RDF triples syntax and set the response headers */ 
    public static MediaType contentNegotationRDF(HttpAction action) {
        return contentNegotation(action, DEF.rdfOffer, DEF.acceptRDFXML) ;
    }

    /** Negotiate the content-type for an RDF quads syntax and set the response headers */ 
    public static MediaType contentNegotationQuads(HttpAction action) {
        return contentNegotation(action, DEF.quadsOffer, DEF.acceptNQuads) ;
    }
}

