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

package org.apache.jena.fuseki.mod.metrics;

import static org.apache.jena.http.HttpLib.handleResponseRtnString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.sys.FusekiModules;
import org.apache.jena.fuseki.mod.prometheus.FMod_Prometheus;
import org.apache.jena.http.HttpEnv;
import org.apache.jena.http.HttpLib;
import org.apache.jena.riot.WebContent;
import org.apache.jena.riot.web.HttpNames;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;

public class TestModPrometheus {

    private FusekiServer testServer = null;

    @BeforeEach void setupServer() {
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        FusekiModules fusekiModules = FusekiModules.create(FMod_Prometheus.get());
        testServer = FusekiServer.create()
                .add("/ds", dsg)
                .enableMetrics(false)       // N.B. false. Instead, use module to setup.
                .fusekiModules(fusekiModules)
                .build();
        testServer.start();
    }

    @AfterEach void teardownServer() {
        if ( testServer != null )
            testServer.stop();
    }

    @Test
    public void can_retrieve_metrics() {
        String metricsURL = testServer.serverURL()+"$/metrics";
        HttpRequest request = HttpRequest.newBuilder().uri(HttpLib.toRequestURI(metricsURL)).build();
        HttpResponse<InputStream> response = HttpLib.executeJDK(HttpEnv.getDftHttpClient(), request, BodyHandlers.ofInputStream());
        String body = handleResponseRtnString(response);

        String ct = response.headers().firstValue(HttpNames.hContentType).orElse(null);
        assertNotNull(ct, "No Content-Type");
        assertTrue(ct.contains(WebContent.contentTypeTextPlain));
        assertTrue(ct.contains(WebContent.charsetUTF8));
        assertTrue(body.contains("fuseki_requests_good"));
    }
}
