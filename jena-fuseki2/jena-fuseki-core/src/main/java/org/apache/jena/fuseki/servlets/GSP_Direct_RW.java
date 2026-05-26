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

import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.fuseki.system.UploadDetails;
import org.apache.jena.riot.web.HttpNames;

public class GSP_Direct_RW extends GSP_Direct_R {

    // triples only!
    public GSP_Direct_RW() {}

    @Override
    protected void doPost(HttpAction action) {
        doPutPostDirect(action, false);
    }

    @Override
    protected void doPut(HttpAction action) {
        doPutPostDirect(action, true);
    }

   private void doPutPostDirect(HttpAction action, boolean replace) {
        ContentType ct = ActionLib.getContentType(action);
        if ( ct == null )
            ServletOps.errorBadRequest("No Content-Type:");

        UploadDetails details;
        if ( action.isTransactional() )
            details = GSPLib.triplesPutPostTxn(action, replace, this::decideDataset);
        else
            details = GSPLib.triplesPutPostNonTxn(action, replace, this::decideDataset);
        ServletOps.uploadResponse(action, details);
    }

    @Override
    protected void doDelete(HttpAction action) {
        GSPLib.execDeleteGSP(action, this::decideDataset);
    }

    @Override
    protected void doHead(HttpAction action) {
        GSPLib.execHeadGSP(action, this::decideDataset);
    }

    @Override
    protected void doOptions(HttpAction action) {
        ActionLib.setCommonHeadersForOptions(action);
        action.setResponseHeader(HttpNames.hAllow, "GET,HEAD,OPTIONS,PUT,DELETE,POST");
        ServletOps.success(action);
    }

    @Override
    protected void doPatch(HttpAction action)
    { notSupported(action); }

    private void notSupported(HttpAction action) {
        ServletOps.errorMethodNotAllowed(action.getRequestMethod()+" "+action.getDatasetName());
    }
}
