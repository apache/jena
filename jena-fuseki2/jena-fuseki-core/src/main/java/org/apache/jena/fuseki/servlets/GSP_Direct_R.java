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

import java.util.function.Function;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.jena.atlas.web.MediaType;
import org.apache.jena.riot.web.HttpNames;
import org.apache.jena.sparql.core.DatasetGraph;

public class GSP_Direct_R extends ActionREST {

    private static String defaultDirect = "$default$" ;

    // triples only!
    public GSP_Direct_R() {}

    @Override
    public void validate(HttpAction action) {
        // No query string allowed
        HttpServletRequest request = action.getRequest();
        if ( request.getQueryString() != null )
            ServletOps.errorBadRequest("Query string not allowed");
    }

    @Override
    protected void doGet(HttpAction action) {
        GSPLib.execGetGSP(action, this::decideDataset);
    }

    private void notSupported(HttpAction action) {
        ServletOps.errorMethodNotAllowed(action.getRequestMethod()+" "+action.getDatasetName());
    }

    protected void readOnly(HttpAction action) {
        ServletOps.errorMethodNotAllowed(action.getRequestMethod()+" : Read-only");
    }

    // Share?
    @Override
    protected void doHead(HttpAction action) {
        execHeadGSP(action, this::decideDataset);
    }

    // Share with GSP_R via GSP_Ops
    protected static void execHeadGSP(HttpAction action, Function<HttpAction, DatasetGraph> decideDataset) {
        ActionLib.setCommonHeaders(action);
        MediaType mediaType = ActionLib.contentNegotationRDF(action);
        if ( action.verbose )
            action.log.info(format("[%d]   Head: Content-Type=%s", action.id, mediaType.getContentTypeStr()));
        // Check graph not 404.
        action.beginRead();
        try {
            DatasetGraph dsg = decideDataset.apply(action);
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
    protected void doOptions(HttpAction action) {
        ActionLib.setCommonHeadersForOptions(action);
        action.setResponseHeader(HttpNames.hAllow, "GET,HEAD,OPTIONS");
        ServletOps.success(action);
    }

    @Override
    protected void doPost(HttpAction action)
    { readOnly(action); }

    @Override
    protected void doPut(HttpAction action)
    { readOnly(action); }

    @Override
    protected void doDelete(HttpAction action)
    { readOnly(action); }

    @Override
    protected void doPatch(HttpAction action)
    { notSupported(action); }
}
