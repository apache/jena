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

import java.io.IOException ;

import javax.servlet.ServletOutputStream ;
import javax.servlet.http.HttpServletRequest ;

import org.apache.jena.fuseki.Fuseki ;
import org.apache.jena.fuseki.FusekiLib ;
import org.apache.jena.fuseki.HttpNames ;
import org.openjena.atlas.lib.Sink ;
import org.openjena.atlas.web.MediaType ;
import org.openjena.atlas.web.TypedOutputStream ;
import org.openjena.riot.Lang ;
import org.openjena.riot.RiotReader ;
import org.openjena.riot.RiotWriter ;
import org.openjena.riot.lang.LangRIOT ;
import org.openjena.riot.lang.SinkTriplesToGraph ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;

/** 
 * Servlet that serves up quads for a dataset.
 */

public class REST_Quads extends SPARQL_REST
{
    public REST_Quads(boolean verbose)
    { super(verbose) ; }
    
    @Override
    protected void validate(HttpServletRequest request)
    {
        // already checked?
    }
    
    @Override
    protected void doGet(HttpActionREST action)
    {
        MediaType mediaType = HttpAction.contentNegotationQuads(action) ;
        ServletOutputStream output ;
        try { output = action.response.getOutputStream() ; }
        catch (IOException ex) { errorOccurred(ex) ; output = null ; }
        
        TypedOutputStream out = new TypedOutputStream(output, mediaType) ;
        Lang lang = FusekiLib.langFromContentType(mediaType.getContentType()) ;
        if ( lang == null )
            lang = Lang.TRIG ;

        if ( action.verbose )
            log.info(format("[%d]   Get: Content-Type=%s, Charset=%s => %s", 
                                  action.id, mediaType.getContentType(), mediaType.getCharset(), lang.getName())) ;
        if ( ! lang.isQuads() )
            errorBadRequest("Not a quads format: "+mediaType) ;
        
        action.beginRead() ;
        try {
            DatasetGraph dsg = action.getActiveDSG() ;
            
            if ( lang == Lang.NQUADS )
                RiotWriter.writeNQuads(out, dsg) ;
            else if ( lang == Lang.TRIG )
                errorBadRequest("TriG - Not implemented (yet) : "+mediaType) ;
            else
                errorBadRequest("No handled: "+mediaType) ;
            success(action) ;
        } finally { action.endRead() ; }
    }
    
    @Override
    protected void doOptions(HttpActionREST action)
    {
        action.response.setHeader(HttpNames.hAllow, "GET, HEAD, OPTIONS") ;
        action.response.setHeader(HttpNames.hContentLengh, "0") ;
        success(action) ;
    }

    @Override
    protected void doHead(HttpActionREST action)
    {
        action.beginRead() ;
        try { 
            MediaType mediaType = HttpAction.contentNegotationQuads(action) ;
            success(action) ;
        } finally { action.endRead() ; }
    }

    static int counter = 0 ;
    @Override
    protected void doPost(HttpActionREST action)
    { 
        if ( ! Fuseki.graphStoreProtocolMode )
            errorMethodNotAllowed("POST") ;
        
        // Code to pass the GSP test suite.
        // Not necessarily good code.
        String x = action.request.getContentType() ;
        
        MediaType mediaType = MediaType.create(x) ;
        Lang lang = FusekiLib.langFromContentType(mediaType.getContentType()) ;
        if ( lang == null )
            lang = Lang.TRIG ;

        if ( action.verbose )
            log.info(format("[%d]   Post: Content-Type=%s, Charset=%s => %s", 
                                  action.id, mediaType.getContentType(), mediaType.getCharset(), lang.getName())) ;
        if ( lang.isQuads() )
            errorBadRequest("Quads format: "+mediaType) ;
        
        action.beginWrite() ;
        try {
            DatasetGraph dsg = action.getActiveDSG() ;
            //log.info(format("[%d] ** Content-length: %d", action.id, action.request.getContentLength())) ;  
            
            String name = action.request.getRequestURL().toString() ;
            if ( ! name.endsWith("/") )
                name = name+ "/"  ;
            name = name+(++counter) ;
            Node gn = Node.createURI(name) ;
            Graph g = dsg.getGraph(gn) ;
            Sink<Triple> sink = new SinkTriplesToGraph(g) ;

            LangRIOT parser = RiotReader.createParserTriples(action.request.getInputStream(), lang, name , sink) ;
            parser.parse() ;
            log.info(format("[%d] Location: %s", action.id, name)) ;
            action.response.setHeader("Location",  name) ;
            action.commit();
            successCreated(action) ;
        } catch (IOException ex) { action.abort() ; } 
        finally { action.endWrite() ; }
        
    }
    

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

