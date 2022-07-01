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

import static org.apache.jena.fuseki.servlets.GraphTarget.determineTargetGSP;

import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.fuseki.FusekiConfigException;
import org.apache.jena.fuseki.server.Validators;
import org.apache.jena.fuseki.system.DataUploader;
import org.apache.jena.fuseki.system.FusekiNetLib;
import org.apache.jena.fuseki.system.UploadDetails;
import org.apache.jena.graph.Graph;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.RiotParseException;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.riot.web.HttpNames;
import org.apache.jena.shared.OperationDeniedException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.graph.GraphFactory;

public class GSP_RW extends GSP_R {

    public GSP_RW() {}

    @Override
    protected void doOptions(HttpAction action) {
        ActionLib.setCommonHeadersForOptions(action);
        if ( GSPLib.hasGSPParams(action) )
            action.setResponseHeader(HttpNames.hAllow, "GET,HEAD,OPTIONS,PUT,DELETE,POST");
        else
            action.setResponseHeader(HttpNames.hAllow, "GET,HEAD,OPTIONS,PUT,POST");
        ServletOps.success(action);
    }

    @Override
    protected void doDelete(HttpAction action) {
        if ( isQuads(action) )
            execDeleteQuads(action);
        else
            execDeleteGSP(action);
   }

    @Override
    protected void doPut(HttpAction action) {
        if ( isQuads(action) )
            execPutQuads(action);
        else
            execPutGSP(action);
    }

    @Override
    protected void doPost(HttpAction action) {
        if ( isQuads(action) )
            execPostQuads(action);
        else
            execPostGSP(action);
    }

    protected void execPostGSP(HttpAction action) { doPutPostGSP(action, false); }

    protected void execPostQuads(HttpAction action) { doPutPostQuads(action, false); }

    protected void execPutGSP(HttpAction action) { doPutPostGSP(action, true); }

    protected void execPutQuads(HttpAction action) { doPutPostQuads(action, true); }

    protected void execDeleteGSP(HttpAction action) {
        action.beginWrite();
        boolean haveCommited = false;
        try {
            DatasetGraph dsg = decideDataset(action);
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
            deleteGraph(dsg, action);
            action.commit();
            haveCommited = true;
        }
        catch (ActionErrorException ex) { throw ex; }
        catch (Exception ex) { action.abortSilent(); }
        finally { action.end(); }
        ServletOps.successNoContent(action);
    }

    protected void execDeleteQuads(HttpAction action) {
        // Don't allow whole-database DELETE.
        ServletOps.errorMethodNotAllowed("DELETE");
    }

    /** Delete a graph. This removes the storage choice and looses the setup.
     * The default graph is cleared, not removed.
     */
    protected static void deleteGraph(DatasetGraph dsg, HttpAction action) {
        GraphTarget target = determineTargetGSP(dsg, action);
        if ( target.isDefault() )
            clearGraph(target);
        else
            target.dataset().removeGraph(target.graphName());
    }

    /** Clear a graph - this leaves the storage choice and setup in-place */
    protected static void clearGraph(GraphTarget target) {
        Graph g = target.graph();
        g.getPrefixMapping().clearNsPrefixMap();
        g.clear();
    }

    protected void doPutPostQuads(HttpAction action, boolean replaceOperation) {
        ContentType ct = ActionLib.getContentType(action);
        if ( ct == null )
            ServletOps.errorBadRequest("No Content-Type:");

        if ( !action.getDataService().allowUpdate() )
            ServletOps.errorMethodNotAllowed(action.getMethod());

        UploadDetails details;
        if ( action.isTransactional() )
            details = UploadRDF.quadsPutPostTxn(action, a->decideDataset(a), replaceOperation);
        else
            details = UploadRDF.quadsPutPostNonTxn(action, a->decideDataset(a), replaceOperation);
        ServletOps.uploadResponse(action, details);
    }

    // ---- Triples to target graph

    protected void doPutPostGSP(HttpAction action, boolean overwrite) {
        ContentType ct = ActionLib.getContentType(action);
        if ( ct == null )
            ServletOps.errorBadRequest("No Content-Type:");

        UploadDetails details;
        if ( action.isTransactional() )
            details = triplesPutPostTxn(action, overwrite);
        else
            details = triplesPutPostNonTxn(action, overwrite);
        ServletOps.uploadResponse(action, details);
    }

    /** Directly add data in a transaction.
     * Assumes recovery from parse errors by transaction abort.
     * Return whether the target existed before.
     */
    private UploadDetails triplesPutPostTxn(HttpAction action, boolean replaceOperation) {
        action.beginWrite();
        try {
            DatasetGraph dsg = decideDataset(action);
            GraphTarget target = determineTargetGSP(dsg, action);
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
            action.end();
        }
    }

    /** Add data where the destination does not support full transactions.
     *  In particular, with no abort, and actions probably going to the real storage
     *  parse errors can lead to partial updates.  Instead, parse to a temporary
     *  graph, then insert that data.
     */
    private UploadDetails triplesPutPostNonTxn(HttpAction action, boolean replaceOperation) {
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
            DatasetGraph dsg = decideDataset(action);
            GraphTarget target = determineTargetGSP(dsg, action);
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
        } finally { action.end(); }
    }
}
