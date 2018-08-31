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
import org.apache.jena.graph.Graph ;
import org.apache.jena.riot.* ;
import org.apache.jena.riot.web.HttpNames ;
import org.apache.jena.shared.JenaException ;
import org.apache.jena.sparql.core.DatasetGraph;

/** Only the READ operations */
public class SPARQL_GSP_R extends SPARQL_GSP
{
    public SPARQL_GSP_R()
    { super() ; }
    
    @Override
    protected String mapRequestToDataset(HttpAction action) {
        return ActionLib.mapRequestToDatasetLongest$(action.request.getRequestURI(), action.getDataAccessPointRegistry()) ;
    }

    @Override
    protected void doGet(HttpAction action) {
        // Assume success - do the set up before grabbing the lock.
        // Sets content type.
        MediaType mediaType = ActionLib.contentNegotationRDF(action) ;
        
        ServletOutputStream output ;
        try { output = action.response.getOutputStream() ; }
        catch (IOException ex) { ServletOps.errorOccurred(ex) ; output = null ; }
        
        TypedOutputStream out = new TypedOutputStream(output, mediaType) ;
        Lang lang = RDFLanguages.contentTypeToLang(mediaType.getContentType()) ;

        if ( action.verbose )
            action.log.info(format("[%d]   Get: Content-Type=%s, Charset=%s => %s", 
                            action.id, mediaType.getContentType(), mediaType.getCharset(), lang.getName())) ;

        action.beginRead() ;
        setCommonHeaders(action.response) ;
        try {
            DatasetGraph dsg = decideDataset(action);
            Target target = determineTarget(dsg, action) ;
            if ( action.log.isDebugEnabled() )
                action.log.debug("GET->"+target) ;
            boolean exists = target.exists() ;
            if ( ! exists )
                ServletOps.errorNotFound("No such graph: <"+target.name+">") ;
            Graph g = target.graph() ;
            if ( ! target.isDefault && g.isEmpty() )
                ServletOps.errorNotFound("No such graph: <"+target.name+">") ;
            // If we want to set the Content-Length, we need to buffer.
            //response.setContentLength(??) ;
            String ct = lang.getContentType().toHeaderString() ;
            action.response.setContentType(ct) ;
            //Special case RDF/XML to be the plain (faster, less readable) form
            RDFFormat fmt = 
                ( lang == Lang.RDFXML ) ? RDFFormat.RDFXML_PLAIN : RDFWriterRegistry.defaultSerialization(lang) ;
            try { 
                RDFDataMgr.write(out, g, fmt) ;
            } catch (JenaException ex) { 
                // Some RDF/XML data is unwritable. All we can do is pretend it's a bad
                // request (inappropriate content type).
                // Good news - this happens before any output for RDF/XML-ABBREV. 
                if ( fmt.getLang().equals(Lang.RDFXML) )
                    ServletOps.errorBadRequest("Failed to write output in RDF/XML: "+ex.getMessage()) ;
                else
                    ServletOps.errorOccurred("Failed to write output: "+ex.getMessage(), ex) ;
            }
            ServletOps.success(action) ;
        } finally { action.endRead() ; }
    }
    
    @Override
    protected void doOptions(HttpAction action) {
        setCommonHeadersForOptions(action.response) ;
        action.response.setHeader(HttpNames.hAllow, "GET,HEAD,OPTIONS") ;
        action.response.setHeader(HttpNames.hContentLengh, "0") ;
        ServletOps.success(action) ;
    }

    @Override
    protected void doHead(HttpAction action) {
        action.beginRead() ;
        setCommonHeaders(action.response) ;
        try {
            DatasetGraph dsg = decideDataset(action);
            Target target = determineTarget(dsg, action) ;
            if ( action.log.isDebugEnabled() )
                action.log.debug("HEAD->"+target) ;
            if ( ! target.exists() )
            {
                ServletOps.successNotFound(action) ;
                return ;
            }
            MediaType mediaType = ActionLib.contentNegotationRDF(action) ;
            ServletOps.success(action) ;
        } finally { action.endRead() ; }
    }

    @Override
    protected void doPost(HttpAction action)
    { ServletOps.errorMethodNotAllowed("POST : Read-only") ; }

    @Override
    protected void doDelete(HttpAction action)
    { ServletOps.errorMethodNotAllowed("DELETE : Read-only") ; }

    @Override
    protected void doPut(HttpAction action)
    { ServletOps.errorMethodNotAllowed("PUT : Read-only") ; }

    @Override
    protected void doPatch(HttpAction action)
    { ServletOps.errorMethodNotAllowed("PATCH : Read-only") ; }
}
