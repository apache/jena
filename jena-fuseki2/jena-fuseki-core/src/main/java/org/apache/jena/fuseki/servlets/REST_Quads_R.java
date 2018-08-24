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

package org.apache.jena.fuseki.servlets ;

import static java.lang.String.format ;

import java.io.IOException ;

import javax.servlet.ServletOutputStream ;

import org.apache.jena.atlas.web.MediaType ;
import org.apache.jena.atlas.web.TypedOutputStream ;
import org.apache.jena.riot.web.HttpNames ;
import org.apache.jena.shared.JenaException ;
import org.apache.jena.riot.* ;
import org.apache.jena.sparql.core.DatasetGraph ;

/**
 * Servlet for operations directly on a dataset - REST(ish) behaviour on the
 * dataset URI.
 */

public class REST_Quads_R extends REST_Quads {

    public REST_Quads_R() {
        super() ;
    }

    @Override
    protected void validate(HttpAction action) { 
        // Allowed methods controlled by ActionREST.dispatch
        String method = action.getRequest().getMethod() ;
        switch(method) {
            case HttpNames.METHOD_GET:
            case HttpNames.METHOD_HEAD:
            case HttpNames.METHOD_OPTIONS:
                break ;
            default:
                ServletOps.errorMethodNotAllowed(method+" : Read-only dataset");
        }
    }

    @Override
    protected void doGet(HttpAction action) {
        MediaType mediaType = ActionLib.contentNegotationQuads(action) ;
        ServletOutputStream output ;
        try {
            output = action.response.getOutputStream() ;
        } catch (IOException ex) {
            ServletOps.errorOccurred(ex) ;
            output = null ;
        }

        TypedOutputStream out = new TypedOutputStream(output, mediaType) ;
        Lang lang = RDFLanguages.contentTypeToLang(mediaType.getContentType()) ;
        if ( lang == null )
            lang = RDFLanguages.TRIG ;

        if ( action.verbose )
            action.log.info(format("[%d]   Get: Content-Type=%s, Charset=%s => %s", action.id,
                                   mediaType.getContentType(), mediaType.getCharset(), lang.getName())) ;
        if ( !RDFLanguages.isQuads(lang) )
            ServletOps.errorBadRequest("Not a quads format: " + mediaType) ;

        action.beginRead() ;
        try {
            DatasetGraph dsg = actOn(action); 
            action.response.setHeader("Content-type", lang.getContentType().toHeaderString());
            // ActionLib.contentNegotationQuads above
            // RDF/XML is not a choice but this code is general.
            RDFFormat fmt =
                // Choose streaming.
                ( lang == Lang.RDFXML ) ? RDFFormat.RDFXML_PLAIN : RDFWriterRegistry.defaultSerialization(lang) ;
            try {
                RDFDataMgr.write(out, dsg, fmt) ;
            } catch (JenaException ex) {
                if ( fmt.getLang().equals(Lang.RDFXML) )
                    ServletOps.errorBadRequest("Failed to write output in RDF/XML: "+ex.getMessage()) ;
                else
                    ServletOps.errorOccurred("Failed to write output: "+ex.getMessage(), ex) ;
            }
            ServletOps.success(action) ;
        } finally {
            action.endRead() ;
        }
    }

    /**
     * Decide on the dataset to use for the operation. Can be overrided by specialist
     * subclasses.
     */
    protected DatasetGraph actOn(HttpAction action) {
        return action.getActiveDSG() ;
    }

    @Override
    protected void doOptions(HttpAction action) {
        action.response.setHeader(HttpNames.hAllow, "GET, HEAD, OPTIONS") ;
        action.response.setHeader(HttpNames.hContentLengh, "0") ;
        ServletOps.success(action) ;
    }

    @Override
    protected void doHead(HttpAction action) {
        action.beginRead() ;
        try {
            MediaType mediaType = ActionLib.contentNegotationQuads(action) ;
            ServletOps.success(action) ;
        } finally {
            action.endRead() ;
        }
    }
}
