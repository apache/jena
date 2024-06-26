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

package org.apache.jena.fuseki.main.prefixes;

import com.google.gson.Gson;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.servlets.prefixes.ActionProcPrefixes;
import org.apache.jena.fuseki.servlets.prefixes.JsonObject;
import org.apache.jena.fuseki.servlets.prefixes.PrefixesPlain;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.apache.jena.http.HttpOp.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonObjectTests {

    private static FusekiServer server = null;
    private static String serviceURL = null;

    @BeforeAll
    public static void beforeSuite() {
        PrefixesPlain s = new PrefixesPlain();
        server = FusekiServer.create()
                .port(0)
                .addProcessor("/prefixes", new ActionProcPrefixes(s))
                .build();
        server.start();
        serviceURL = String.format("http://localhost:%d/prefixes", server.getHttpPort());
    }

    @AfterAll
    public static void afterSuite() {
        if ( server != null )
            server.stop();
    }


    @Test
    public void fetchURIllegal0() {
        // check content type
        TypedInputStream x = exec(serviceURL, "?prefix=prefix2");
        assertEquals("application/json", x.getContentType(), "Expected application/json");
    }


    @Test
    public void stringJsonObject0() {
        JsonObject jsonObject = new JsonObject("prefix1", "http://www.localhost.org/uri1");
        Gson gson = new Gson();
        String x = gson.toJson(jsonObject);
        assertEquals("{\"prefix\":\"prefix1\",\"namespace\":\"http://www.localhost.org/uri1\"}", x, "Got " + x);
    }

    @Test
    public void fieldsJsonObject0() {
        JsonObject jsonObject = new JsonObject("prefix1", "http://www.localhost.org/uri1");
        assertEquals("prefix1", jsonObject.prefix());
        assertEquals("http://www.localhost.org/uri1", jsonObject.namespace());
    }

    private static TypedInputStream exec(String url, String queryString) {
        String urlExec = queryString.startsWith("?")
                ? url + queryString
                : url + "?" + queryString;
        return httpGet(urlExec);
    }

}
