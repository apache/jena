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
import java.io.OutputStream;

import jakarta.servlet.ServletOutputStream;

import org.apache.jena.atlas.web.MediaType;
import org.apache.jena.graph.Graph;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.web.HttpNames;
import org.apache.jena.shared.JenaException;
import org.apache.jena.shared.OperationDeniedException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.web.HttpSC;

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
        ActionLib.setCommonHeaders(action);
        // If this asks for triples, get N-Quads. Don't want the named graphs hidden.
        MediaType mediaType = ActionLib.contentNegotationQuads(action);
        ServletOutputStream output;
        try { output = action.getResponseOutputStream(); }
        catch (IOException ex) { ServletOps.errorOccurred(ex); output = null; }

        Lang lang = RDFLanguages.contentTypeToLang(mediaType.getContentTypeStr());
        if ( lang == null )
            lang = RDFLanguages.TRIG;

        if ( action.verbose )
            action.log.info(format("[%d]   Get: Content-Type=%s, Charset=%s => %s", action.id,
                mediaType.getContentTypeStr(), mediaType.getCharset(), lang.getName()));
        if ( !RDFLanguages.isQuads(lang) )
            ServletOps.errorBadRequest("Not a quads format: " + mediaType);

        action.beginRead();
        try {
            DatasetGraph dsg = decideDataset(action);
            try {
                // Use the preferred MIME type.
                ActionLib.datasetResponse(action, dsg, lang);
                ServletOps.success(action);
            } catch (OperationDeniedException ex) {
                throw ex;
            } catch (JenaException ex) {
                // Attempt to send an error - which may not work.
                // "406 Not Acceptable" - Accept header issue; target is fine.
                ServletOps.error(HttpSC.NOT_ACCEPTABLE_406, "Failed to write output: "+ex.getMessage());
            }
        } finally {
            action.endRead();
        }
    }

    protected void execGetGSP(HttpAction action) {
        ActionLib.setCommonHeaders(action);
        MediaType mediaType = ActionLib.contentNegotationRDF(action);

        OutputStream output;
        try { output = action.getResponseOutputStream(); }
        catch (IOException ex) { ServletOps.errorOccurred(ex); output = null; }

        Lang lang = RDFLanguages.contentTypeToLang(mediaType.getContentTypeStr());
        if ( lang == null )
            lang = RDFLanguages.TURTLE;

        action.beginRead();
        if ( action.verbose )
            action.log.info(format("[%d]   Get: Content-Type=%s, Charset=%s => %s",
                            action.id, mediaType.getContentTypeStr(), mediaType.getCharset(), lang.getName()));
        try {
            DatasetGraph dsg = decideDataset(action);
            GraphTarget target = determineTargetGSP(dsg, action);
            if ( action.log.isDebugEnabled() )
                action.log.debug("GET->"+target);
            boolean exists = target.exists();
            if ( ! exists )
                ServletOps.errorNotFound("No such graph: "+target.label());
            Graph graph = target.graph();
            // Special case RDF/XML to be the plain (faster, less readable) form
            try {
                // Use the preferred MIME type.
                ActionLib.graphResponse(action, graph, lang);
                ServletOps.success(action);
            } catch (OperationDeniedException ex) {
                throw ex;
            } catch (JenaException ex) {
                // ActionLib.graphResponse has special handling for RDF/XML but for
                // other syntax forms unexpected errors mean we may or may not have
                // written some output because of output buffering.
                // Attempt to send an error - which may not work.
                // "406 Not Acceptable" - Accept header issue; target is fine.
                ServletOps.error(HttpSC.NOT_ACCEPTABLE_406, "Failed to write output: "+ex.getMessage());
            }
        } finally { action.endRead(); }
    }

    @Override
    protected void doOptions(HttpAction action) {
        ActionLib.setCommonHeadersForOptions(action);
        action.setResponseHeader(HttpNames.hAllow, "GET,HEAD,OPTIONS");
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
        ActionLib.setCommonHeaders(action);
        MediaType mediaType = ActionLib.contentNegotationQuads(action);
        if ( action.verbose )
            action.log.info(format("[%d]   Head: Content-Type=%s", action.id, mediaType.getContentTypeStr()));
        ServletOps.success(action);
    }

    protected void execHeadGSP(HttpAction action) {
        ActionLib.setCommonHeaders(action);
        MediaType mediaType = ActionLib.contentNegotationRDF(action);
        if ( action.verbose )
            action.log.info(format("[%d]   Head: Content-Type=%s", action.id, mediaType.getContentTypeStr()));
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
