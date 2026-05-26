/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.fuseki.servlets;

import static java.lang.String.format;
import static org.apache.jena.fuseki.servlets.GraphTarget.determineTargetGSP;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.function.Function;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.jena.atlas.web.MediaType;
import org.apache.jena.fuseki.FusekiConfigException;
import org.apache.jena.fuseki.server.Validators;
import org.apache.jena.fuseki.system.DataUploader;
import org.apache.jena.fuseki.system.FusekiNetLib;
import org.apache.jena.fuseki.system.UploadDetails;
import org.apache.jena.graph.Graph;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.RiotParseException;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.riot.web.HttpNames;
import org.apache.jena.shared.JenaException;
import org.apache.jena.shared.OperationDeniedException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.web.HttpSC;

/*package*/ class GSPLib {

    /** Test whether the operation has either of the GSP parameters. */
    /*package*/ static boolean hasGSPParams(HttpAction action) {
        if ( action.getRequestQueryString() == null )
            return false;
        boolean hasParamGraphDefault = action.getRequestParameter(HttpNames.paramGraphDefault) != null;
        if ( hasParamGraphDefault )
            return true;
        boolean hasParamGraph = action.getRequestParameter(HttpNames.paramGraph) != null;
        if ( hasParamGraph )
            return true;
        return false;
    }

    /** Test whether the operation has exactly one GSP parameter and no other parameters. */
    /*package*/ static boolean hasGSPParamsStrict(HttpAction action) {
        if ( action.getRequestQueryString() == null )
            return false;
        Map<String, String[]> params = action.getRequestParameterMap();
        if ( params.size() != 1 )
            return false;
        boolean hasParamGraphDefault = GSPLib.hasExactlyOneValue(action, HttpNames.paramGraphDefault);
        boolean hasParamGraph = GSPLib.hasExactlyOneValue(action, HttpNames.paramGraph);
        // Java XOR
        return hasParamGraph ^ hasParamGraphDefault;
    }

    /** Check whether there is exactly one HTTP header value */
    /*package*/ static boolean hasExactlyOneValue(HttpAction action, String name) {
        String[] values = action.getRequestParameterValues(name);
        if ( values == null )
            return false;
        if ( values.length == 0 )
            return false;
        if ( values.length > 1 )
            return false;
        return true;
    }

    /** Get one value where there may be several HTTP header values.
     * Multiple values causes an exception.
     * No value returns null.
     */
    /*package*/ static String getOneOnly(HttpServletRequest request, String name) {
        String[] values = request.getParameterValues(name);
        if ( values == null )
            return null;
        if ( values.length == 0 )
            return null;
        if ( values.length > 1 )
            ServletOps.errorBadRequest("Multiple occurrences of '" + name + "'");
        return values[0];
    }

    /*package*/ static String fullURI(HttpAction action) {
        return action.getRequest().getRequestURL().toString();
    }

    /*package*/ static void execGetGSP(HttpAction action, Function<HttpAction, DatasetGraph> selectDataset) {
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
            DatasetGraph dsg = selectDataset.apply(action);
            GraphTarget target = determineTargetGSP(dsg, action);
            if ( action.log.isDebugEnabled() )
                action.log.debug("GET->"+target);
            boolean exists = target.exists();
            if ( ! exists )
                ServletOps.errorNotFound("No such graph: "+target.label());
            Graph graph = target.graph();
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



    /** Directly add data in a transaction.
     * Assumes recovery from parse errors by transaction abort.
     * Return whether the target existed before.
     */
    /*package*/ static UploadDetails triplesPutPostTxn(HttpAction action, boolean replaceOperation, Function<HttpAction, DatasetGraph> selectDataset) {
        action.beginWrite();
        try {
            DatasetGraph dsg = selectDataset.apply(action);
            GraphTarget target = GraphTarget.determineTargetGSP(dsg, action);
            if ( action.log.isDebugEnabled() )
                action.log.debug(action.getRequestMethod().toUpperCase()+"->"+target);
            if ( target.isUnion() )
                ServletOps.errorBadRequest("Can't load into the union graph");
            // Check URI.
            if ( ! target.isDefault() && target.graphName() != null && ! target.graphName().isBlank()) {
                String uri = target.graphName().getURI();
                try {
                    Validators.graphName(uri);
                } catch (FusekiConfigException ex) {
                    ServletOps.errorBadRequest("Bad URI: "+uri);
                    return null;
                }
            }

            boolean existedBefore = target.exists();
            Graph g = target.graph();
            if ( replaceOperation && existedBefore )
                clearGraph(target);
            StreamRDF sink = StreamRDFLib.graph(g);
            UploadDetails upload = DataUploader.incomingData(action, sink);
            upload.setExistedBefore(existedBefore);
            action.commit();
            return upload;
        } catch (RiotParseException ex) {
            action.abortSilent();
            ServletOps.errorParseError(ex);
            return null;
        } catch (RiotException ex) {
            // Parse error
            action.abortSilent();
            ServletOps.errorBadRequest(ex.getMessage());
            return null;
        } catch (OperationDeniedException ex) {
            action.abortSilent();
            throw ex;
        } catch (ActionErrorException ex) {
            // Any ServletOps.error from calls in the try{} block.
            action.abortSilent();
            throw ex;
        } catch (Exception ex) {
            // Something unexpected.
            action.abortSilent();
            ServletOps.errorOccurred(ex.getMessage());
            return null;
        } finally {
            action.endWrite();
        }
    }

    /**
     * Add data where the destination does not support full transactions.
     * In particular, with no abort, and actions probably going to the real storage
     * parse errors can lead to partial updates.  Instead, parse to a temporary
     * graph, then insert that data.
     */
    /*package*/ static UploadDetails triplesPutPostNonTxn(HttpAction action, boolean replaceOperation, Function<HttpAction, DatasetGraph> selectDataset) {
        Graph graphTmp = GraphFactory.createGraphMem();
        StreamRDF dest = StreamRDFLib.graph(graphTmp);

        UploadDetails details;
        try { details = DataUploader.incomingData(action, dest); }
        catch (RiotParseException ex) {
            ServletOps.errorParseError(ex);
            return null;
        }
        // Now insert into dataset
        action.beginWrite();
        try {
            DatasetGraph dsg = selectDataset.apply(action);
            GraphTarget target = GraphTarget.determineTargetGSP(dsg, action);
            if ( action.log.isDebugEnabled() )
                action.log.debug("  ->"+target);
            if ( target.isUnion() )
                ServletOps.errorBadRequest("Can't load into the union graph");
            boolean existedBefore = target.exists();
            if ( replaceOperation && existedBefore )
                clearGraph(target);
            FusekiNetLib.addDataInto(graphTmp, target.dataset(), target.graphName());
            details.setExistedBefore(existedBefore);
            action.commit();
            return details;
        } catch (OperationDeniedException ex) {
            action.abortSilent();
            throw ex;
        } catch (Exception ex) {
            // We parsed into a temporary graph so an exception at this point
            // is not because of a parse error.
            // We're in the non-transactional branch, this probably will not work
            // but it might and there is no harm safely trying.
            action.abortSilent();
            ServletOps.errorOccurred(ex.getMessage());
            return null;
        } finally { action.endWrite(); }
    }

    /*package*/ static void execHeadGSP(HttpAction action, Function<HttpAction, DatasetGraph> selectDataset) {
        ActionLib.setCommonHeaders(action);
        MediaType mediaType = ActionLib.contentNegotationRDF(action);
        if ( action.verbose )
            action.log.info(format("[%d]   Head: Content-Type=%s", action.id, mediaType.getContentTypeStr()));
        // Check graph not 404.
        action.beginRead();
        try {
            DatasetGraph dsg = selectDataset.apply(action);
            GraphTarget target = determineTargetGSP(dsg, action);
            if ( action.log.isDebugEnabled() )
                action.log.debug("HEAD->"+target);
            boolean exists = target.exists();
            if ( ! exists )
                ServletOps.errorNotFound("No such graph: "+target.label());
            ServletOps.success(action);
        } finally { action.endRead(); }
    }

    /*package*/ static  void execDeleteGSP(HttpAction action, Function<HttpAction, DatasetGraph> selectDataset) {
        action.beginWrite();
        boolean haveCommited = false;
        try {
            DatasetGraph dsg = selectDataset.apply(action);
            GraphTarget target = determineTargetGSP(dsg, action);
            if ( action.log.isDebugEnabled() )
                action.log.debug("DELETE->"+target);
            if ( target.isUnion() )
                ServletOps.errorBadRequest("Can't delete the union graph");
            boolean existedBefore = target.exists();
            if ( !existedBefore ) {
                // Commit, not abort, because locking "transactions" don't support abort.
                action.commit();
                haveCommited = true;
                ServletOps.errorNotFound("No such graph: "+target.label());
            }
            deleteGraph(target);
            action.commit();
            haveCommited = true;
        }
        catch (ActionErrorException ex) { throw ex; }
        catch (Exception ex) { action.abortSilent(); }
        finally { action.endWrite(); }
        ServletOps.successNoContent(action);
    }

    /*package*/ static void deleteGraph(GraphTarget target) {
        if ( target.isDefault() )
            clearGraph(target);
        else
            target.dataset().removeGraph(target.graphName());
    }

    /** Clear a graph - this leaves the storage choice and setup in-place */
    /*package*/ static void clearGraph(GraphTarget target) {
        Graph g = target.graph();
        g.getPrefixMapping().clearNsPrefixMap();
        g.clear();
    }

}
