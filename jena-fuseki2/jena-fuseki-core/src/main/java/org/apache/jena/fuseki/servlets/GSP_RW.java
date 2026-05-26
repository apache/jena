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

import static org.apache.jena.fuseki.servlets.GraphTarget.determineTargetGSP;

import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.fuseki.system.UploadDetails;
import org.apache.jena.graph.Graph;
import org.apache.jena.riot.web.HttpNames;
import org.apache.jena.sparql.core.DatasetGraph;

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
            GSPLib.execDeleteGSP(action, this::decideDataset);
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

    // ---- Quads to dataset
    protected void doPutPostQuads(HttpAction action, boolean replaceOperation) {
        ContentType ct = ActionLib.getContentType(action);
        if ( ct == null )
            ServletOps.errorBadRequest("No Content-Type:");

        UploadDetails details;
        if ( action.isTransactional() )
            details = UploadRDF.quadsPutPostTxn(action, this::decideDataset, replaceOperation);
        else
            details = UploadRDF.quadsPutPostNonTxn(action, this::decideDataset, replaceOperation);
        ServletOps.uploadResponse(action, details);
    }

    // ---- Triples to target graph

    protected void doPutPostGSP(HttpAction action, boolean overwrite) {
        ContentType ct = ActionLib.getContentType(action);
        if ( ct == null )
            ServletOps.errorBadRequest("No Content-Type:");

        UploadDetails details;
        if ( action.isTransactional() )
            details = GSPLib.triplesPutPostTxn(action, overwrite, this::decideDataset);
        else
            details = GSPLib.triplesPutPostNonTxn(action, overwrite, this::decideDataset);
        ServletOps.uploadResponse(action, details);
    }
}
