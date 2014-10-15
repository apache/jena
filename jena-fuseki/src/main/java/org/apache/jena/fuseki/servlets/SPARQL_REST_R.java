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

package org.apache.jena.fuseki.servlets;

import static java.lang.String.format ;

import java.io.IOException ;

import javax.servlet.ServletOutputStream ;

import org.apache.jena.atlas.web.MediaType ;
import org.apache.jena.atlas.web.TypedOutputStream ;
import org.apache.jena.fuseki.HttpNames ;
import org.apache.jena.riot.* ;

import com.hp.hpl.jena.graph.Graph ;

/** Only the READ operations */
public class SPARQL_REST_R extends SPARQL_REST
{
    public SPARQL_REST_R()
    { super() ; }
    
    
    @Override
    protected String mapRequestToDataset(String uri) { return mapRequestToDatasetLongest$(uri) ; } 

    @Override
    protected void doGet(HttpAction action)
    {
        // Assume success - do the set up before grabbing the lock.
        // Sets content type.
        MediaType mediaType = HttpAction.contentNegotationRDF(action) ;
        
        ServletOutputStream output ;
        try { output = action.response.getOutputStream() ; }
        catch (IOException ex) { errorOccurred(ex) ; output = null ; }
        
        TypedOutputStream out = new TypedOutputStream(output, mediaType) ;
        Lang lang = RDFLanguages.contentTypeToLang(mediaType.getContentType()) ;

        if ( action.verbose )
            log.info(format("[%d]   Get: Content-Type=%s, Charset=%s => %s", 
                            action.id, mediaType.getContentType(), mediaType.getCharset(), lang.getName())) ;

        action.beginRead() ;

        try {
            Target target = determineTarget(action) ;
            if ( log.isDebugEnabled() )
                log.debug("GET->"+target) ;
            boolean exists = target.exists() ;
            if ( ! exists )
                errorNotFound("No such graph: <"+target.name+">") ;
            // If we want to set the Content-Length, we need to buffer.
            //response.setContentLength(??) ;
            String ct = lang.getContentType().toHeaderString() ;
            action.response.setContentType(ct) ;
            Graph g = target.graph() ;
            //Special case RDF/XML to be the plain (faster, less readable) form
            RDFFormat fmt = 
                ( lang == Lang.RDFXML ) ? RDFFormat.RDFXML_PLAIN : RDFWriterRegistry.defaultSerialization(lang) ;  
            RDFDataMgr.write(out, g, fmt) ;
            success(action) ;
        } finally { action.endRead() ; }
    }
    
    @Override
    protected void doOptions(HttpAction action)
    {
        action.response.setHeader(HttpNames.hAllow, "GET,HEAD,OPTIONS") ;
        action.response.setHeader(HttpNames.hContentLengh, "0") ;
        success(action) ;
    }

    @Override
    protected void doHead(HttpAction action)
    {
        action.beginRead() ;
        try { 
            Target target = determineTarget(action) ;
            if ( log.isDebugEnabled() )
                log.debug("HEAD->"+target) ;
            if ( ! target.exists() )
            {
                successNotFound(action) ;
                return ;
            }
            MediaType mediaType = HttpAction.contentNegotationRDF(action) ;
            success(action) ;
        } finally { action.endRead() ; }
    }

    @Override
    protected void doPost(HttpAction action)
    { errorMethodNotAllowed("POST") ; }

    @Override
    protected void doDelete(HttpAction action)
    { errorMethodNotAllowed("DELETE") ; }

    @Override
    protected void doPut(HttpAction action)
    { errorMethodNotAllowed("PUT") ; }

    @Override
    protected void doPatch(HttpAction action)
    { errorMethodNotAllowed("PATCH") ; }
}
