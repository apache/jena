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

package org.apache.jena.fuseki.main;

import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.atlas.web.WebLib;
import org.apache.jena.fuseki.FusekiConfigException;
import org.apache.jena.fuseki.build.FusekiConfig;
import org.apache.jena.fuseki.build.FusekiExt;
import org.apache.jena.fuseki.server.DataService;
import org.apache.jena.fuseki.server.Operation;
import org.apache.jena.fuseki.servlets.ActionService;
import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.riot.WebContent;
import org.apache.jena.riot.web.HttpOp;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.web.HttpSC;
import org.junit.Test;

/** Test for adding a new operation */
public class TestFusekiCustomOperation {
    private static final Operation newOp = Operation.alloc("http://example/special", "special", "Custom operation");
    private static final String contentType = "application/special";
    private static final String endpointName = "special";

    private final ActionService customHandler = new CustomTestService() {
        @Override
        protected void doGet(HttpAction action) {
            action.response.setStatus(HttpSC.OK_200);
            try {
                action.response.setContentType(WebContent.contentTypeTextPlain);
                action.response.getOutputStream().println("    ** Hello world (GET) **");
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void doHead(HttpAction action) {
            action.response.setStatus(HttpSC.OK_200);
            action.response.setContentType(WebContent.contentTypeTextPlain);
        }

        @Override
        protected void doPost(HttpAction action) {
            action.response.setStatus(HttpSC.OK_200);
            try {
                action.response.setContentType(WebContent.contentTypeTextPlain);
                action.response.getOutputStream().println("    ** Hello world (POST) **");
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    private final int port = WebLib.choosePort();
    private final String url = "http://localhost:"+port;

    @Test
    public void cfg_dataservice() {
        // Create a DataService and add the endpoint -> operation association.
        // This still needs the server to have the operation registered.
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        DataService dataService = new DataService(dsg);
        FusekiConfig.populateStdServices(dataService, true);
        FusekiExt.registerOperation(newOp, customHandler);
        FusekiConfig.addServiceEP(dataService, newOp, endpointName);

        FusekiServer server =
            FusekiServer.create()
                .port(port)
                .registerOperation(newOp, contentType, customHandler)
                .add("/ds", dataService)
                .build();
        testServer(server, true, true);
    }

    @Test
    public void cfg_builder_CT() {
        FusekiServer server =
            FusekiServer.create()
                .port(port)
                .registerOperation(newOp, contentType, customHandler)
                .add("/ds", DatasetGraphFactory.createTxnMem(), true)
                .addEndpoint("/ds", endpointName, newOp)
                .build();
        testServer(server, true, true);
    }

    @Test
    public void cfg_builder_noCT() {
        FusekiServer server =
            FusekiServer.create()
                .port(port)
                .registerOperation(newOp, null, customHandler)
                .add("/ds", DatasetGraphFactory.createTxnMem(), true)
                .addEndpoint("/ds", endpointName, newOp)
                .build();
        testServer(server, true, false);
    }

    @Test(expected=FusekiConfigException.class)
    public void cfg_bad_01() {
        FusekiServer.create()
        .port(port)
        .registerOperation(newOp, null, customHandler)
        .addEndpoint("/UNKNOWN", endpointName, newOp);
        //.build();
    }

    @Test(expected=FusekiConfigException.class)
    public void cfg_bad_02() {
        FusekiServer.create()
        .port(port)
        //.registerOperation(newOp, null, customHandler)
        .add("/ds", DatasetGraphFactory.createTxnMem(), true)
        // Unregistered.
        .addEndpoint("/ds", endpointName, newOp);
        //.build();
    }

    public void cfg_bad_ct_not_enabkled_here() {
        FusekiServer server = FusekiServer.create()
            .port(port)
            .registerOperation(newOp, "app/special", customHandler)
            .add("/ds", DatasetGraphFactory.createTxnMem(), true)
            // Unregistered.
            .addEndpoint("/ds", endpointName, newOp)
            .build();
        testServer(server, false, false);
    }


    private void testServer(FusekiServer server, boolean withEndpoint, boolean withContentType) {
        try {
            server.start();
            // Try query (no extension required)
            try(RDFConnection rconn = RDFConnectionFactory.connect(url+"/ds")) {
                try(QueryExecution qExec = rconn.query("ASK {}")) {
                    qExec.execAsk();
                }
            }

            if ( withEndpoint ) {
                // Service endpoint name : GET
                String s1 = HttpOp.execHttpGetString(url+"/ds/"+endpointName);

                // Service endpoint name : POST
                try ( TypedInputStream stream = HttpOp.execHttpPostStream(url+"/ds/"+endpointName, "ignored", "", "text/plain") ) {
                    String x = IOUtils.toString(stream, StandardCharsets.UTF_8);
                    assertNotNull(x);
                } catch (IOException ex) {
                    IO.exception(ex);
                }
            } else {
                // No endpoint so we expect a 404.
                try {
                    // Service endpoint name : GET
                    HttpOp.execHttpGet(url+"/ds/"+endpointName);
                    fail("Expected to fail HTTP GET");
                } catch (HttpException ex) {
                    assertEquals(404, ex.getStatusCode());
                }
            }

            if ( withContentType ) {
                // Content-type
                try ( TypedInputStream stream = HttpOp.execHttpPostStream(url+"/ds", contentType, "", "text/plain") ) {
                    String x = IOUtils.toString(stream, StandardCharsets.UTF_8);
                    assertNotNull(x);
                } catch (IOException ex) {
                    IO.exception(ex);
                }
            } else {
                // No Content-Type
                try ( TypedInputStream stream = HttpOp.execHttpPostStream(url+"/ds", contentType, "", "text/plain") ) {
                    fail("Expected to fail HTTP POST using Content-Type");
                } catch (HttpException ex) {}

                // Service endpoint name. DELETE -> fails 405
                try {
                    HttpOp.execHttpDelete(url+"/ds/"+endpointName);
                    throw new IllegalStateException("DELETE succeeded");
                } catch (HttpException ex) {
                    assertEquals(405, ex.getStatusCode());
                }
            }
        } finally {
            server.stop();
        }
    }
}
