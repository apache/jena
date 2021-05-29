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

import static java.lang.String.format;
import static org.apache.jena.fuseki.servlets.GraphTarget.determineTargetGSP;

import java.io.IOException;

import javax.servlet.ServletOutputStream;

import org.apache.jena.atlas.web.MediaType;
import org.apache.jena.atlas.web.TypedOutputStream;
import org.apache.jena.graph.Graph;
import org.apache.jena.riot.*;
import org.apache.jena.riot.web.HttpNames;
import org.apache.jena.shared.JenaException;
import org.apache.jena.shared.OperationDeniedException;
import org.apache.jena.sparql.core.DatasetGraph;

public class GSP_R extends GSP_Base {

    public GSP_R() {}

    @Override
    protected void doGet(HttpAction action) {
        if ( isQuads(action) )
            execGetQuads(action);
        else
            execGetGSP(action);
    }

    protected void execGetQuads(HttpAction action) {
        ActionLib.setCommonHeaders(action.response);
        MediaType mediaType = ActionLib.contentNegotationQuads(action);
        ServletOutputStream output;
        try { output = action.response.getOutputStream(); }
        catch (IOException ex) { ServletOps.errorOccurred(ex); output = null; }

        TypedOutputStream out = new TypedOutputStream(output, mediaType);
        Lang lang = RDFLanguages.contentTypeToLang(mediaType.getContentType());
        if ( lang == null )
            lang = RDFLanguages.TRIG;

        if ( action.verbose )
            action.log.info(format("[%d]   Get: Content-Type=%s, Charset=%s => %s", action.id,
                mediaType.getContentType(), mediaType.getCharset(), lang.getName()));
        if ( !RDFLanguages.isQuads(lang) )
            ServletOps.errorBadRequest("Not a quads format: " + mediaType);

        action.beginRead();
        try {
            DatasetGraph dsg = decideDataset(action);
            action.response.setHeader("Content-type", lang.getContentType().toHeaderString());
            // ActionLib.contentNegotationQuads above
            // RDF/XML is not a choice but this code is general.
            RDFFormat fmt =
                // Choose streaming.
                ( lang == Lang.RDFXML ) ? RDFFormat.RDFXML_PLAIN : RDFWriterRegistry.defaultSerialization(lang);
            try {
                RDFDataMgr.write(out, dsg, fmt);
            } catch (OperationDeniedException ex) {
                throw ex;
            } catch (JenaException ex) {
                if ( fmt.getLang().equals(Lang.RDFXML) )
                    ServletOps.errorBadRequest("Failed to write output in RDF/XML: "+ex.getMessage());
                else
                    ServletOps.errorOccurred("Failed to write output: "+ex.getMessage(), ex);
            }
            ServletOps.success(action);
        } finally {
            action.endRead();
        }
    }

    protected void execGetGSP(HttpAction action) {
        ActionLib.setCommonHeaders(action.response);
        MediaType mediaType = ActionLib.contentNegotationRDF(action);

        ServletOutputStream output;
        try { output = action.response.getOutputStream(); }
        catch (IOException ex) { ServletOps.errorOccurred(ex); output = null; }

        TypedOutputStream out = new TypedOutputStream(output, mediaType);
        Lang lang = RDFLanguages.contentTypeToLang(mediaType.getContentType());

        action.beginRead();
        if ( action.verbose )
            action.log.info(format("[%d]   Get: Content-Type=%s, Charset=%s => %s",
                            action.id, mediaType.getContentType(), mediaType.getCharset(), lang.getName()));
        try {
            DatasetGraph dsg = decideDataset(action);
            GraphTarget target = determineTargetGSP(dsg, action);
            if ( action.log.isDebugEnabled() )
                action.log.debug("GET->"+target);
            boolean exists = target.exists();
            if ( ! exists )
                ServletOps.errorNotFound("No such graph: "+target.label());
            Graph g = target.graph();
            // If we want to set the Content-Length, we need to buffer.
            //response.setContentLength(??);
            String ct = lang.getContentType().toHeaderString();
            action.response.setContentType(ct);
            //Special case RDF/XML to be the plain (faster, less readable) form
            RDFFormat fmt =
                ( lang == Lang.RDFXML ) ? RDFFormat.RDFXML_PLAIN : RDFWriterRegistry.defaultSerialization(lang);
            try {
                RDFDataMgr.write(out, g, fmt);
            } catch (OperationDeniedException ex) {
                throw ex;
            } catch (JenaException ex) {
                // Some RDF/XML data is unwritable. All we can do is pretend it's a bad
                // request (inappropriate content type).
                // Good news - this happens before any output for RDF/XML-ABBREV.
                if ( fmt.getLang().equals(Lang.RDFXML) )
                    ServletOps.errorBadRequest("Failed to write output in RDF/XML: "+ex.getMessage());
                else
                    ServletOps.errorOccurred("Failed to write output: "+ex.getMessage(), ex);
            }
            ServletOps.success(action);
        } finally { action.endRead(); }
    }

    @Override
    protected void doOptions(HttpAction action) {
        ActionLib.setCommonHeadersForOptions(action.response);
        action.response.setHeader(HttpNames.hAllow, "GET,HEAD,OPTIONS");
        ServletOps.success(action);
    }

    @Override
    protected void doHead(HttpAction action) {
        if ( isQuads(action) )
            execHeadQuads(action);
        else
            execHeadGSP(action);
    }

    protected void execHeadQuads(HttpAction action) {
        ActionLib.setCommonHeaders(action.response);
        MediaType mediaType = ActionLib.contentNegotationQuads(action);
        if ( action.verbose )
            action.log.info(format("[%d]   Head: Content-Type=%s", action.id, mediaType.getContentType()));
        ServletOps.success(action);
    }

    protected void execHeadGSP(HttpAction action) {
        ActionLib.setCommonHeaders(action.response);
        MediaType mediaType = ActionLib.contentNegotationRDF(action);
        if ( action.verbose )
            action.log.info(format("[%d]   Head: Content-Type=%s", action.id, mediaType.getContentType()));
        // Check graph not 404.
        action.beginRead();
        try {
            DatasetGraph dsg = decideDataset(action);
            GraphTarget target = determineTargetGSP(dsg, action);
            if ( action.log.isDebugEnabled() )
                action.log.debug("HEAD->"+target);
            boolean exists = target.exists();
            if ( ! exists )
                ServletOps.errorNotFound("No such graph: "+target.label());
            ServletOps.success(action);
        } finally { action.endRead(); }
    }

    @Override
    protected void doPost(HttpAction action)
    { ServletOps.errorMethodNotAllowed("POST : Read-only"); }

    @Override
    protected void doDelete(HttpAction action)
    { ServletOps.errorMethodNotAllowed("DELETE : Read-only"); }

    @Override
    protected void doPut(HttpAction action)
    { ServletOps.errorMethodNotAllowed("PUT : Read-only"); }

    @Override
    protected void doPatch(HttpAction action)
    { ServletOps.errorMethodNotAllowed("PATCH : Read-only"); }

}
