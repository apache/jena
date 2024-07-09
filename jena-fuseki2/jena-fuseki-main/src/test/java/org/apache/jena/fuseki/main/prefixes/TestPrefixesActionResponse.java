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

import static org.apache.jena.http.HttpOp.httpGet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.fuseki.main.FusekiServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Test the prefixes service when used as Fuseki operations
 * on a Fuseki service with a database.
 */
public class TestPrefixesActionResponse {

    private static FusekiServer server = null;
    private static String serviceR = null;

    @BeforeAll
    public static void beforeAll() {
        String DATASET = "dataset";
        server = FusekiServer.create()
                .port(0)
                // With data. read-only.
                .parseConfigFile("src/test/files/config-prefixes.ttl")
                .start();
        int port = server.getHttpPort();
        serviceR = "http://localhost:"+port+"/"+DATASET+"/prefixes";
    }

    @AfterAll
    public static void afterAll() {
        if ( server != null )
            server.stop();
    }

    private String testReadURL() {
        return serviceR;
    }

    // Test JSON responses

    private record PrefixesEntry(String prefix, String namespace) {}

    @Test
    public void getAllJson() {
        TypedInputStream x = httpGet(testReadURL());
        assertEquals("application/json", x.getContentType(), "Expected application/json");
        String response = IO.readWholeFileAsUTF8(x);
        // JSON array
        JsonElement elt = JsonParser.parseString(response);
        assertTrue(elt.isJsonArray());
        JsonArray array = elt.getAsJsonArray();
        assertEquals(2, array.size());

        JsonObject x1 = array.get(0).getAsJsonObject();
        JsonObject x2 = array.get(1).getAsJsonObject();

        String fPrefix0 = x1.get("prefix").getAsString();
        if ( fPrefix0.equals("prefix2") ) {
            JsonObject tmp = x1;
            x1 = x2;
            x2 = tmp;
        }

        testJSON(x1, "prefix1", "http://example/ns#");
        testJSON(x2, "prefix2", "http://example/namespace/");
    }

    private void testJSON(JsonObject jsonObj, String prefixValue, String uriValue) {
        String fPrefix = jsonObj.get("prefix").getAsString();
        assertEquals(prefixValue, fPrefix);
        String fURI = jsonObj.get("uri").getAsString();
        assertEquals(uriValue, fURI);
    }

    @Test
    public void getPrefixJson() {
        TypedInputStream x = httpGet(testReadURL()+"?prefix=prefix1");
        //assertEquals("application/json", x.getContentType(), "Expected application/json");
        String response = IO.readWholeFileAsUTF8(x);
        assertEquals("http://example/ns#", response);
    }

    @Test
    public void getPrefixNone() {
        // check content type
        TypedInputStream x = execStream(testReadURL(), "?prefix=noSuchPrefix");
        //assertEquals("application/json", x.getContentType(), "Expected application/json");
        String response = IO.readWholeFileAsUTF8(x);
        assertTrue(response.isEmpty());
    }

    private static TypedInputStream execStream(String url, String queryString) {
        String urlExec = queryString.startsWith("?")
                ? url + queryString
                : url + "?" + queryString;
        return httpGet(urlExec);
    }
}
