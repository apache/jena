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

import org.apache.jena.atlas.web.MediaType ;
import org.apache.jena.atlas.web.TypedOutputStream ;
import org.apache.jena.fuseki.Fuseki ;
import org.apache.jena.fuseki.HttpNames ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.RDFLanguages ;
import org.apache.jena.riot.ReaderRIOT ;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.system.StreamRDFLib ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;

/** 
 * Servlet that serves up quads for a dataset.
 */

public class REST_Quads extends SPARQL_REST
{
    public REST_Quads()     { super(); }
    
    @Override
    protected void validate(HttpAction action)
    {
        // already checked?
    }
    
    @Override
    protected void doGet(HttpAction action)
    {
        MediaType mediaType = HttpAction.contentNegotationQuads(action) ;
        ServletOutputStream output ;
        try { output = action.response.getOutputStream() ; }
        catch (IOException ex) { errorOccurred(ex) ; output = null ; }
        
        TypedOutputStream out = new TypedOutputStream(output, mediaType) ;
        Lang lang = RDFLanguages.contentTypeToLang(mediaType.getContentType()) ;
        if ( lang == null )
            lang = RDFLanguages.TRIG ;

        if ( action.verbose )
            log.info(format("[%d]   Get: Content-Type=%s, Charset=%s => %s", 
                                  action.id, mediaType.getContentType(), mediaType.getCharset(), lang.getName())) ;
        if ( ! RDFLanguages.isQuads(lang) )
            errorBadRequest("Not a quads format: "+mediaType) ;
        
        action.beginRead() ;
        try {
            DatasetGraph dsg = action.getActiveDSG() ;
            RDFDataMgr.write(out, dsg, lang) ;
            success(action) ;
        } finally { action.endRead() ; }
    }
    
    @Override
    protected void doOptions(HttpAction action)
    {
        action.response.setHeader(HttpNames.hAllow, "GET, HEAD, OPTIONS") ;
        action.response.setHeader(HttpNames.hContentLengh, "0") ;
        success(action) ;
    }

    @Override
    protected void doHead(HttpAction action)
    {
        action.beginRead() ;
        try { 
            MediaType mediaType = HttpAction.contentNegotationQuads(action) ;
            success(action) ;
        } finally { action.endRead() ; }
    }

    static int counter = 0 ;
    @Override
    protected void doPost(HttpAction action)
    { 
        if ( ! action.getDatasetRef().allowDatasetUpdate )
            errorMethodNotAllowed("POST") ;

        // Graph Store Protocol mode - POST triples to dataset causes
        // a new graph to be created and the new URI returned via Location.
        // Normally off.  
        // When off, POST of triples goes to default graph.
        boolean gspMode = Fuseki.graphStoreProtocolPostCreate ;
        
        // Code to pass the GSP test suite.
        // Not necessarily good code.
        String x = action.request.getContentType() ;
        if ( x == null )
            errorBadRequest("Content-type required for data format") ;
        
        MediaType mediaType = MediaType.create(x) ;
        Lang lang = RDFLanguages.contentTypeToLang(mediaType.getContentType()) ;
        if ( lang == null )
            lang = RDFLanguages.TRIG ;

        if ( action.verbose )
            log.info(format("[%d]   Post: Content-Type=%s, Charset=%s => %s", 
                                  action.id, mediaType.getContentType(), mediaType.getCharset(), lang.getName())) ;
        
        if ( RDFLanguages.isQuads(lang) )
            doPostQuads(action, lang) ;
        else if ( gspMode && RDFLanguages.isTriples(lang) )
            doPostTriplesGSP(action, lang) ;
        else if ( RDFLanguages.isTriples(lang) )
            doPostTriples(action, lang) ;
        else
            errorBadRequest("Not a triples or quads format: "+mediaType) ;
    }
        
    protected void doPostQuads(HttpAction action, Lang lang)
    {
        action.beginWrite() ;
        try {
            String name = action.request.getRequestURL().toString() ;
            DatasetGraph dsg = action.getActiveDSG() ;
            StreamRDF dest = StreamRDFLib.dataset(dsg) ;
            ReaderRIOT reader = RDFDataMgr.createReader(lang) ;
            reader.read(action.request.getInputStream(), name, null, dest, null);
            action.commit();
            success(action) ;
        } catch (IOException ex) { action.abort() ; } 
        finally { action.endWrite() ; }
    }
    
  
    // POST triples to dataset -- send to default graph.  
    protected void doPostTriples(HttpAction action, Lang lang) 
    {
        action.beginWrite() ;
        try {
            DatasetGraph dsg = action.getActiveDSG() ;
            // This should not be anythign other than the datasets name via this route.  
            String name = action.request.getRequestURL().toString() ;
            //log.info(format("[%d] ** Content-length: %d", action.id, action.request.getContentLength())) ;  
            Graph g = dsg.getDefaultGraph() ;
            StreamRDF dest = StreamRDFLib.graph(g) ;
            ReaderRIOT reader = RDFDataMgr.createReader(lang) ;
            reader.read(action.request.getInputStream(), name, null, dest, null);
            action.commit();
            success(action) ;
        } catch (IOException ex) { action.abort() ; } 
        finally { action.endWrite() ; }
    }
    
    protected void doPostTriplesGSP(HttpAction action, Lang lang) 
    {
        action.beginWrite() ;
        try {
            DatasetGraph dsg = action.getActiveDSG() ;
            //log.info(format("[%d] ** Content-length: %d", action.id, action.request.getContentLength())) ;  
            
            String name = action.request.getRequestURL().toString() ;
            if ( ! name.endsWith("/") )
                name = name+ "/"  ;
            name = name+(++counter) ;
            Node gn = NodeFactory.createURI(name) ;
            Graph g = dsg.getGraph(gn) ;
            StreamRDF dest = StreamRDFLib.graph(g) ;
            ReaderRIOT reader = RDFDataMgr.createReader(lang) ;
            reader.read(action.request.getInputStream(), name, null, dest, null);
            log.info(format("[%d] Location: %s", action.id, name)) ;
            action.response.setHeader("Location",  name) ;
            action.commit();
            successCreated(action) ;
        } catch (IOException ex) { action.abort() ; } 
        finally { action.endWrite() ; }
    }

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

