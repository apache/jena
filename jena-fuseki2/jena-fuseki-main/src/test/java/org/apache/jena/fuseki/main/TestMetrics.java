/**
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
package org.apache.jena.fuseki.main;

import static org.apache.jena.http.HttpLib.handleResponseRtnString;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import org.apache.jena.http.HttpEnv;
import org.apache.jena.http.HttpLib;
import org.apache.jena.riot.WebContent;
import org.apache.jena.riot.web.HttpNames;
import org.junit.Test;

public class TestMetrics extends AbstractFusekiTest {

    @Test
    public void can_retrieve_metrics() {
        String r = urlRoot() + "$/metrics";
        HttpRequest request = HttpRequest.newBuilder().uri(HttpLib.toRequestURI(r)).build();
        HttpResponse<InputStream> response = HttpLib.executeJDK(HttpEnv.getDftHttpClient(), request, BodyHandlers.ofInputStream());
        String body = handleResponseRtnString(response);

        String ct = response.headers().firstValue(HttpNames.hContentType).orElse(null);
        assertTrue(ct.contains(WebContent.contentTypeTextPlain));
        assertTrue(ct.contains(WebContent.charsetUTF8));
        assertTrue(body.contains("fuseki_requests_good"));
    }
}
