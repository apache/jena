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
import java.util.Iterator ;

import javax.servlet.ServletOutputStream ;

import org.apache.jena.atlas.web.MediaType ;
import org.apache.jena.atlas.web.TypedOutputStream ;
import org.apache.jena.fuseki.FusekiLib ;
import org.apache.jena.fuseki.HttpNames ;
import org.apache.jena.fuseki.server.DatasetRegistry ;
import org.openjena.riot.Lang ;
import org.openjena.riot.WebContent ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.rdf.model.RDFWriter ;

/** Only the READ operations */
public class SPARQL_REST_R extends SPARQL_REST
{
    public SPARQL_REST_R(boolean verbose)
    { super(verbose) ; }

    public SPARQL_REST_R()
    { this(false) ; }
    
    
    @Override
    protected String mapRequestToDataset(String uri) 
    {
        if ( uri == null )
            return null ;
        
        // Mapping a request for GSP needs to find the "best"
        // (shortest matching) unlike service matching, 
        // which is a matter of removing the service component.

        String ds = null ;
        Iterator<String> iter = DatasetRegistry.get().keys() ;
        while(iter.hasNext())
        {
            String ds2 = iter.next();
            if ( ! uri.startsWith(ds2) )
                continue ;

            if ( ds == null )
            {
                ds = ds2 ;
                continue ; 
            }
            if ( ds.length() > ds2.length() )
            {
                ds = ds2 ;
                continue ;
            }
        }
        return ds ;
    }

    @Override
    protected void doGet(HttpActionREST action)
    {
        // Assume success - do the set up before grabbing the lock.
        // Sets content type.
        MediaType mediaType = HttpAction.contentNegotationRDF(action) ;
        
        ServletOutputStream output ;
        try { output = action.response.getOutputStream() ; }
        catch (IOException ex) { errorOccurred(ex) ; output = null ; }
        
        TypedOutputStream out = new TypedOutputStream(output, mediaType) ;
        Lang lang = FusekiLib.langFromContentType(mediaType.getContentType()) ;

        if ( action.verbose )
            log.info(format("[%d]   Get: Content-Type=%s, Charset=%s => %s", 
                                  action.id, mediaType.getContentType(), mediaType.getCharset(), lang.getName())) ;

        action.beginRead() ;
        try {
            if ( log.isDebugEnabled() )
                log.debug("GET->"+action.getTarget()) ;
            boolean exists = action.getTarget().exists() ;
            if ( ! exists )
                errorNotFound("No such graph: <"+action.getTarget().name+">") ;
            // If we want to set the Content-Length, we need to buffer.
            //response.setContentLength(??) ;
            RDFWriter writer = FusekiLib.chooseWriter(lang) ;
            String ct = WebContent.mapLangToContentType(lang) ;
            action.response.setContentType(ct) ;
            Graph g = action.getTarget().graph() ;
            Model model = ModelFactory.createModelForGraph(g) ;
            writer.write(model, out, null) ;
            success(action) ;
        } finally { action.endRead() ; }
    }
    
    @Override
    protected void doOptions(HttpActionREST action)
    {
        action.response.setHeader(HttpNames.hAllow, "GET,HEAD,OPTIONS") ;
        action.response.setHeader(HttpNames.hContentLengh, "0") ;
        success(action) ;
    }

    @Override
    protected void doHead(HttpActionREST action)
    {
        action.beginRead() ;
        try { 
            if ( log.isDebugEnabled() )
                log.debug("HEAD->"+action.getTarget()) ;
            if ( ! action.getTarget().exists() )
            {
                successNotFound(action) ;
                return ;
            }
            MediaType mediaType = HttpAction.contentNegotationRDF(action) ;
            success(action) ;
        } finally { action.endRead() ; }
    }

    @Override
    protected void doPost(HttpActionREST action)
    { errorMethodNotAllowed("POST") ; }

    @Override
    protected void doDelete(HttpActionREST action)
    { errorMethodNotAllowed("DELETE") ; }

    @Override
    protected void doPut(HttpActionREST action)
    { errorMethodNotAllowed("PUT") ; }

    @Override
    protected void doPatch(HttpActionREST action)
    { errorMethodNotAllowed("PATCH") ; }
}
