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

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.fuseki.servlets.prefixes.PrefixUtils;
import org.apache.jena.fuseki.servlets.prefixes.PrefixesAccess;
import org.apache.jena.riot.web.HttpNames;

public class ActionPrefixesRW extends ActionPrefixesR {

    public ActionPrefixesRW() {}

    @Override
    protected void doOptions(HttpAction action) {
        ActionLib.setCommonHeadersForOptions(action);
        action.setResponseHeader(HttpNames.hAllow, "GET,OPTIONS,POST,DELETE");
        ServletOps.success(action);
    }

    @Override
    protected void validatePrefixesDELETE(HttpAction action) {
        String prefixToRemove = action.getRequestParameter(PrefixUtils.PREFIX);
        if (prefixToRemove == null || prefixToRemove.isEmpty() || !PrefixUtils.prefixIsValid(prefixToRemove))
            ServletOps.errorBadRequest("Remove operation unsuccessful!");
    }

    @Override
    protected void doDelete(HttpAction action) {
        ActionLib.setCommonHeaders(action);
        action.beginWrite();
        try {
            String prefixToRemove = action.getRequestParameter(PrefixUtils.PREFIX);
            PrefixesAccess prefixes = prefixes(action);

            prefixes.removePrefix(prefixToRemove);
            FmtLog.info(action.log, "[%d] Remove %s:", action.id, prefixToRemove);
            action.commit();
            ServletOps.success(action);
        } catch (RuntimeException ex) {
            action.abortSilent();
            ServletOps.errorOccurred(ex);
        } finally {
            action.end();
        }
    }

    @Override
    protected void validatePrefixesPOST(HttpAction action) {
        String prefix = action.getRequestParameter(PrefixUtils.PREFIX);
        String uri = action.getRequestParameter(PrefixUtils.URI);

        if (prefix.isEmpty() ) {
            ServletOps.errorBadRequest("Missing prefix parameter");
            return;
        }
        if ( StringUtils.isEmpty(uri) ) {
            ServletOps.errorBadRequest("Missing URI parameter");
            return;
        }

        else if (!PrefixUtils.prefixIsValid(prefix) || !PrefixUtils.uriIsValid(uri)) {
            ServletOps.errorBadRequest("Empty operation - unsuccessful!");
            return;
        }
    }

    @Override
    protected void doPost(HttpAction action) {
        ActionLib.setCommonHeaders(action);
        action.beginWrite();
        try {
            try {
                String prefix = action.getRequestParameter(PrefixUtils.PREFIX);
                String uri = action.getRequestParameter(PrefixUtils.URI);
                PrefixesAccess prefixes = prefixes(action);

                prefixes.updatePrefix(prefix, uri);
                FmtLog.info(action.log, "[%d] Set %s: <%s>", action.id, prefix, uri);
                action.commit();
                ServletOps.success(action);
            } catch (RuntimeException ex) {
                action.abortSilent();
                ServletOps.errorOccurred(ex);
            }
        } finally {
            action.end();
        }
    }
}
