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

package org.apache.jena.fuseki.servlets.prefixes;

import com.google.gson.JsonArray;
import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.fuseki.servlets.*;
import org.apache.jena.query.TxnType;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sparql.core.Transactional;

import java.io.IOException;
import java.util.*;


//validate and unpack the HTTP Get request,
//call the storage's fetch URI by the prefix function from the HTTP request

public class ActionProcPrefixes extends BaseActionREST {

    private final PrefixesAccess storage;

    // The response to a prefix lookup when there is no such prefix.
    // This value is not a proper namespace because it is not an absolute URI.
    private static final String NO_PREFIX_NS = "";

    public ActionProcPrefixes(PrefixesAccess storage) {
        this.storage = storage;
    }

    public void validateGet (HttpAction action) {

        // sanitize parameters
        Iterator<String> paramNames = action.getRequestParameterNames().asIterator();
        while(paramNames.hasNext()) {
            String check = paramNames.next();
            if(!check.equals("prefix") && !check.equals("uri") && !check.equals("removeprefix")) {
                ServletOps.errorBadRequest("Unrecognized parameter");
                return;
            }
        }
        // check if the combination of parameters is legal
        String prefix = action.getRequestParameter("prefix");
        String uri = action.getRequestParameter("uri");
        if(prefix != null && uri != null) {
            ServletOps.errorBadRequest("Provide only one of the prefix or uri parameters!");
            return;
        }
    }

    public void validatePost (HttpAction action) {
        String prefix = action.getRequestParameter("prefix");
        String uri = action.getRequestParameter("uri");
        String prefixToRemove = action.getRequestParameter("removeprefix");

        if (prefix.isEmpty() && uri.isEmpty() && prefixToRemove.isEmpty()) {
            ServletOps.errorBadRequest("Empty operation - unsuccessful!");
            return;
        }
        if (prefixToRemove.isEmpty()) {
            if (prefix.isEmpty() || uri.isEmpty() || !PrefixUtils.prefixIsValid(prefix) || !PrefixUtils.uriIsValid(uri)) {
                ServletOps.errorBadRequest("Update operation unsuccessful!");
                return;
            }
        }
        if (prefix.isEmpty() && uri.isEmpty()) {
            if (!PrefixUtils.prefixIsValid(prefixToRemove)) {
                ServletOps.errorBadRequest("Remove operation unsuccessful!");
                return;
            }
        }
    }

    @Override
    public void doGet (HttpAction action) {

        validateGet(action);

        Transactional transactional = storage.transactional();
        transactional.begin(TxnType.READ);

        try {
            // Not null (valid request)
            String prefix = action.getRequestParameter("prefix");
            String uri = action.getRequestParameter("uri");

            if (prefix == null && uri == null) {
                //getAll
                Map<String, String> allPairs = storage.getAll();
                JsonArray allJsonPairs = new JsonArray();
                allPairs.entrySet().stream()
                        .forEach(entry -> {
                            com.google.gson.JsonObject jsonObject = new com.google.gson.JsonObject();
                            jsonObject.addProperty("prefix", entry.getKey());
                            jsonObject.addProperty("uri", entry.getValue());
                            allJsonPairs.add(jsonObject);
                            FmtLog.info(action.log, "[%d] - %s", action.id, new JsonObject(entry.getKey(), entry.getValue()));
                        });
                action.setResponseContentType(WebContent.contentTypeJSON);
                action.getResponseOutputStream().print(String.valueOf(allJsonPairs));

                transactional.commit();
                ServletOps.success(action);
                transactional.end();
                return;
            }
            if (prefix != null && uri == null) {

                if (prefix.isEmpty()) {
                    ServletOps.errorBadRequest("Empty prefix!");
                    return;
                }
                else if (!PrefixUtils.prefixIsValid(prefix)) {
                    ServletOps.errorBadRequest("Prefix contains illegal characters!");
                    return;
                }
                else {
                    //fetchURI
                    Optional<String> x = storage.fetchURI(prefix);
                    String namespace = x.orElse(NO_PREFIX_NS);

                    JsonObject jsonObject = new JsonObject(prefix, namespace);

                    // Build the response.
                    action.setResponseContentType(WebContent.contentTypeJSON);
                    action.getResponseOutputStream().print(namespace);
                    // Indicate success
                    FmtLog.info(action.log, "[%d] %s -> %s", action.id, prefix, jsonObject.toString());
                    transactional.commit();
                    ServletOps.success(action);
                    return;
                }
            }
            if (prefix == null && uri != null) {
                if (uri.isEmpty()) {
                    ServletOps.errorBadRequest("Empty URI!");
                    return;
                }
                else if (!PrefixUtils.uriIsValid(uri)) {
                    ServletOps.errorBadRequest("URI contains illegal characters!");
                    return;
                }
                else {
                    //fetchPrefix
                    List<String> prefixList =storage.fetchPrefix(uri);
                    JsonArray prefixJsonArray = new JsonArray();
                    for (String p : prefixList) {
                        com.google.gson.JsonObject jsonObject2 = new com.google.gson.JsonObject();
                        jsonObject2.addProperty("prefix", p);
                        jsonObject2.addProperty("uri", uri);
                        prefixJsonArray.add(jsonObject2);
                        FmtLog.info(action.log, "[%d] - %s", action.id, new JsonObject(p, uri));
                    }
                    // Build the response.
                    action.setResponseContentType(WebContent.contentTypeJSON);
                    action.getResponseOutputStream().print(String.valueOf(prefixJsonArray));
                    // Indicate success
                    transactional.commit();
                    ServletOps.success(action);
                    transactional.end();
                    return;
                }
            }

        } catch (RuntimeException | IOException ex) {
            try { transactional.abort(); }
            catch (Throwable th ) {
                FmtLog.warn(action.log, th, "[%d] GET prefix = %s", action.id);
            }
            ServletOps.errorOccurred(ex);
        } finally {
            transactional.end();
        }
    }

    @Override
    public void doPost (HttpAction action) {
        validatePost(action);
        Transactional transactional = storage.transactional();

        transactional.begin(TxnType.WRITE);
        try {
            String prefix = action.getRequestParameter("prefix");
            String uri = action.getRequestParameter("uri");

            if(prefix.isEmpty()) {
                String prefixToRemove = action.getRequestParameter("removeprefix");
                storage.removePrefix(prefixToRemove);
                FmtLog.info(action.log, "[%d] Remove %s:", action.id, prefix);
            }
            else {
                storage.updatePrefix(prefix, uri);
                FmtLog.info(action.log, "[%d] Set %s: <%s>", action.id, prefix, uri);
            }
            transactional.commit();

            // Build the response.
            action.setResponseContentType(WebContent.contentTypeJSON);
            action.getResponseOutputStream().print("");
            // Indicate success
            ServletOps.success(action);
        } catch (RuntimeException | IOException ex) {
            try { transactional.abort(); }
            catch (Throwable th ) {
                FmtLog.warn(action.log, th, "[%d] POST prefix = %s", action.id);
            }
            ServletOps.errorOccurred(ex);
        } finally {
            transactional.end();
        }
    }
}
