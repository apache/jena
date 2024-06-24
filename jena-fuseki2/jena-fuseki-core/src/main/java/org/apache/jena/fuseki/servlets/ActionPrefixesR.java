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

import com.google.gson.JsonArray;
import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.riot.WebContent;
import org.apache.jena.riot.web.HttpNames;

import org.apache.jena.fuseki.servlets.prefixes.ActionPrefixesBase;
import org.apache.jena.fuseki.servlets.prefixes.PrefixUtils;
import org.apache.jena.fuseki.servlets.prefixes.JsonObject;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ActionPrefixesR extends ActionPrefixesBase {

    private static final String NO_PREFIX_NS = "";

    public ActionPrefixesR() {}

    @Override
    protected void doOptions(HttpAction action) {
        ActionLib.setCommonHeadersForOptions(action);
        action.setResponseHeader(HttpNames.hAllow, "GET,OPTIONS");
        ServletOps.success(action);
    }

    public void validateGet(HttpAction action) {
        validate(action);
        // check if the combination of parameters is legal
        String prefix = action.getRequestParameter(PrefixUtils.PREFIX);
        String uri = action.getRequestParameter(PrefixUtils.URI);
        if(prefix != null && uri != null) {
            ServletOps.errorBadRequest("Provide only one of the prefix or uri parameters!");
            return;
        }
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
    protected void doGet(HttpAction action) {
        ActionLib.setCommonHeaders(action);
        validateGet(action);

        action.beginRead();
        try {
            // Not null (valid request)
            String prefix = action.getRequestParameter(PrefixUtils.PREFIX);
            String uri = action.getRequestParameter(PrefixUtils.URI);

            switch(chooseResponseType(prefix, uri)) {
                case GET_ALL -> {
                    Map<String, String> allPairs = prefixes(action).getAll();
                    JsonArray allJsonPairs = new JsonArray();
                    allPairs.entrySet().stream()
                            .forEach(entry -> {
                                com.google.gson.JsonObject jsonObject = new com.google.gson.JsonObject();
                                jsonObject.addProperty(PrefixUtils.PREFIX, entry.getKey());
                                jsonObject.addProperty(PrefixUtils.URI, entry.getValue());
                                allJsonPairs.add(jsonObject);
                                FmtLog.info(action.log, "[%d] - %s", action.id, new JsonObject(entry.getKey(), entry.getValue()));
                            });
                    action.setResponseContentType(WebContent.contentTypeJSON);
                    action.getResponseOutputStream().print(String.valueOf(allJsonPairs));

                    ServletOps.success(action);
                    action.endRead();
                    return;
                }
                case FETCH_URI -> {
                    Optional<String> x = prefixes(action).fetchURI(prefix);
                    String namespace = x.orElse(NO_PREFIX_NS);

                    JsonObject jsonObject = new JsonObject(prefix, namespace);

                    // Build the response.
                    action.setResponseContentType(WebContent.contentTypeJSON);
                    action.getResponseOutputStream().print(namespace);
                    // Indicate success
                    FmtLog.info(action.log, "[%d] %s -> %s", action.id, prefix, jsonObject.toString());
                    action.commit();
                    ServletOps.success(action);
                    return;
                }
                case FETCH_PREFIX -> {
                    List<String> prefixList =prefixes(action).fetchPrefix(uri);
                    JsonArray prefixJsonArray = new JsonArray();
                    for (String p : prefixList) {
                        com.google.gson.JsonObject jsonObject2 = new com.google.gson.JsonObject();
                        jsonObject2.addProperty(PrefixUtils.PREFIX, p);
                        jsonObject2.addProperty(PrefixUtils.URI, uri);
                        prefixJsonArray.add(jsonObject2);
                        FmtLog.info(action.log, "[%d] - %s", action.id, new JsonObject(p, uri));
                    }
                    // Build the response.
                    action.setResponseContentType(WebContent.contentTypeJSON);
                    action.getResponseOutputStream().print(String.valueOf(prefixJsonArray));
                    // Indicate success
                    action.commit();
                    ServletOps.success(action);
                    action.endRead();
                    return;
                }
                default ->  {
                    ServletOps.errorBadRequest("Bad request");
                    return;
                }
            }
        } catch (RuntimeException | IOException ex) {
            try { action.abort(); }
            catch (Throwable th ) {
                FmtLog.warn(action.log, th, "[%d] GET prefix = %s", action.id);
            }
            ServletOps.errorOccurred(ex);
        } finally {
            action.endRead();
        }
    }
}
