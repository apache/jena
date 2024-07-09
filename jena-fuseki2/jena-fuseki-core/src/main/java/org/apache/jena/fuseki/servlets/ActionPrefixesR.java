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

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.atlas.web.AcceptList;
import org.apache.jena.atlas.web.MediaType;
import org.apache.jena.fuseki.servlets.prefixes.ActionPrefixesBase;
import org.apache.jena.fuseki.servlets.prefixes.PrefixUtils;
import org.apache.jena.fuseki.servlets.prefixes.PrefixesAccess;
import org.apache.jena.fuseki.system.ConNeg;
import org.apache.jena.riot.WebContent;
import org.apache.jena.riot.web.HttpNames;
import org.apache.jena.web.HttpSC;

public class ActionPrefixesR extends ActionPrefixesBase {

    private static final String NO_PREFIX_NS = "";

    public ActionPrefixesR() {}

    @Override
    protected PrefixesAccess prefixes(HttpAction action) {
        return ActionPrefixesBase.prefixesFromAction(action);
    }

    @Override
    protected void doOptions(HttpAction action) {
        ActionLib.setCommonHeadersForOptions(action);
        action.setResponseHeader(HttpNames.hAllow, "GET,OPTIONS");
        ServletOps.success(action);
    }

    enum ResponseTypes {
        GET_ALL,
        FETCH_URI,
        FETCH_PREFIX,
        BAD_REQUEST
    }

    protected ResponseTypes chooseResponseType (String prefix, String uri) {
        if (prefix == null && uri == null)
            return ResponseTypes.GET_ALL;
        else if (prefix != null && uri == null) {
            if (prefix.isEmpty()) {
                ServletOps.errorBadRequest("Empty prefix!");
                return ResponseTypes.BAD_REQUEST;
            }
            else if (!PrefixUtils.prefixIsValid(prefix)) {
                ServletOps.errorBadRequest("Prefix contains illegal characters!");
                return ResponseTypes.BAD_REQUEST;
            }
            else
                return ResponseTypes.FETCH_URI;
        }
        else if (prefix == null && uri != null) {
            if (uri.isEmpty()) {
                ServletOps.errorBadRequest("Empty URI!");
                return ResponseTypes.BAD_REQUEST;
            }
            else if (!PrefixUtils.uriIsValid(uri)) {
                ServletOps.errorBadRequest("URI contains illegal characters!");
                return ResponseTypes.BAD_REQUEST;
            }
            else
                return ResponseTypes.FETCH_PREFIX;
        }
        return ResponseTypes.BAD_REQUEST;
    }

    @Override
    protected void validatePrefixesGET(HttpAction action) {
        // Only need to check for presence of expected parameters.
        // Values have been checked.
        String prefix = action.getRequestParameter(PrefixUtils.PREFIX);
        String uri = action.getRequestParameter(PrefixUtils.URI);
        if ( prefix != null && uri != null ) {
            ServletOps.errorBadRequest("Provide only no paremetrs, or one of the prefix or uri!");
            return;
        }
    }

    @Override
    protected void doGet(HttpAction action) {
        ActionLib.setCommonHeaders(action);
        action.beginRead();
        try {
            String prefix = action.getRequestParameter(PrefixUtils.PREFIX);
            String uri = action.getRequestParameter(PrefixUtils.URI);
            PrefixesAccess prefixes = prefixes(action);

            switch(chooseResponseType(prefix, uri)) {
                case GET_ALL -> execGetAll(action, prefixes);
                case FETCH_URI -> execFetchURIByPrefix(action, prefixes, prefix);
                case FETCH_PREFIX -> execFetchPrefixForURI(action, prefixes, uri);
                default ->  {
                    ServletOps.errorBadRequest("Bad request");
                    return;
                }
            }
        } catch (ActionErrorException ex) {
            // pass through
        } catch (RuntimeException ex) {
            action.abortSilent();
            ServletOps.errorOccurred(ex);
        } finally {
            action.endRead();
        }
    }

    private void execGetAll(HttpAction action, PrefixesAccess prefixes) {
        Map<String, String> allPairs = prefixes.getAll();
        JsonArray allJsonPairs = new JsonArray();
        allPairs.entrySet().stream().forEach(entry -> {
            JsonObject jsonObject = jsonObject(entry.getKey(), entry.getValue());
            allJsonPairs.add(jsonObject);
            FmtLog.debug(action.log, "[%d] Entry: %s: <%s>", action.id, entry.getKey(), entry.getValue());
        });
        FmtLog.info(action.log, "[%d] - Get all prefix mappings", action.id);
        ServletOps.success(action);
        try {
            action.setResponseContentType(WebContent.contentTypeJSON);
            action.getResponseOutputStream().print(String.valueOf(allJsonPairs));
            ServletOps.success(action);
        } catch (IOException ex) {
            FmtLog.warn(action.log, "[%d] Get all prefixes: Failed to send response: %s", action.id, ex.getMessage());
            ServletOps.errorOccurred(ex);
        }
    }

    private static JsonObject jsonObject(String prefix, String uri) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(PrefixUtils.PREFIX, prefix);
        jsonObject.addProperty(PrefixUtils.URI, uri);
        return jsonObject;
    }

    private static AcceptList acceptGET = AcceptList.create(WebContent.contentTypeTextPlain, WebContent.contentTypeJSON);
    private static MediaType dftMediaType = MediaType.create(WebContent.contentTypeJSON);

    private void execFetchURIByPrefix(HttpAction action, PrefixesAccess prefixes, String prefix) {
        Optional<String> x = prefixes.fetchURI(prefix);
        String namespace = x.orElse(NO_PREFIX_NS);

        try {
            MediaType mt = ConNeg.chooseContentType(action.getRequest(), acceptGET, dftMediaType);
            String ctString = mt.getContentTypeStr();
            switch (ctString) {
                case WebContent.contentTypeTextPlain -> responseText(action, prefix, namespace);
                case WebContent.contentTypeJSON -> responseJSON(action, prefix, namespace);
                default ->
                    ServletOps.error(HttpSC.UNSUPPORTED_MEDIA_TYPE_415);
            }
            FmtLog.info(action.log, "[%d] %s -> %s", action.id, prefix, namespace);
            ServletOps.success(action);
        } catch (IOException ex) {
            FmtLog.warn(action.log, "[%d] Fetch URI by prefix: Failed to send response: %s", action.id, ex.getMessage());
            ServletOps.errorOccurred(ex);
        }
        FmtLog.info(action.log, "[%d] %s -> %s", action.id, prefix, namespace);
        action.commit();
        ServletOps.success(action);
    }

    private static void responseJSON(HttpAction action, String prefix, String uri)  throws IOException {
        action.setResponseContentType(WebContent.contentTypeJSON);
        JsonObject jObj = jsonObject(prefix, uri);
        action.getResponseOutputStream().print(String.valueOf(jObj));
    }

    private static void responseText(HttpAction action, String prefix, String uri) throws IOException {
        action.setResponseContentType(WebContent.contentTypeTextPlain);
        action.getResponseOutputStream().print(uri);
    }

    private void execFetchPrefixForURI(HttpAction action, PrefixesAccess prefixes, String uri) {
        List<String> prefixList = prefixes.fetchPrefix(uri);
        JsonArray prefixJsonArray = new JsonArray();
        for (String p : prefixList) {
            JsonObject jsonObject2 = jsonObject(p, uri);
            prefixJsonArray.add(jsonObject2);
        }
        FmtLog.info(action.log, "[%d] PrefixForURI: %s: %s", action.id, String.valueOf(prefixJsonArray));

        try {
            action.setResponseContentType(WebContent.contentTypeJSON);
            action.getResponseOutputStream().print(String.valueOf(prefixJsonArray));
        } catch (IOException ex) {
            FmtLog.warn(action.log, "[%d] Fetch prefixes for URI: Failed to send response: %s", action.id, ex.getMessage());
            ServletOps.errorOccurred(ex);
        }
        action.commit();
        ServletOps.success(action);
        action.endRead();
    }
}
