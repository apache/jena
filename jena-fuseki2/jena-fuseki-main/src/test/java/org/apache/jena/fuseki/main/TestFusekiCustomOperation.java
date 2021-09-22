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

import static org.apache.jena.fuseki.main.FusekiTestLib.expectFail;
import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.atlas.web.WebLib;
import org.apache.jena.fuseki.FusekiConfigException;
import org.apache.jena.fuseki.build.FusekiExt;
import org.apache.jena.fuseki.server.DataService;
import org.apache.jena.fuseki.server.Endpoint;
import org.apache.jena.fuseki.server.Operation;
import org.apache.jena.fuseki.server.OperationRegistry;
import org.apache.jena.fuseki.servlets.ActionService;
import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.riot.WebContent;
import org.apache.jena.riot.web.HttpOp;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.web.HttpSC;
import org.junit.Test;

/** Test for adding a new operation */
public class TestFusekiCustomOperation {
    private static final Operation newOp                 = Operation.alloc("http://example/special", "special", "Custom operation");
    private static final String    contentType           = "application/special";
    private static final String    endpointName          = "special";
    private static final String    customHandlerBodyGet  = "    ** Hello world (GET) ** custom handler **";
    private static final String    customHandlerBodyPost = "    ** Hello world (POST) ** custom handler **";

    private final ActionService    customHandler =
        new CustomTestService() {
        @Override
        protected void doGet(HttpAction action) {
            action.setResponseStatus(HttpSC.OK_200);
            try {
                action.setResponseContentType(WebContent.contentTypeTextPlain);
                action.getResponseOutputStream().print(customHandlerBodyGet);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void doHead(HttpAction action) {
            action.setResponseStatus(HttpSC.OK_200);
            action.setResponseContentType(WebContent.contentTypeTextPlain);
        }

        @Override
        protected void doPost(HttpAction action) {
            action.setResponseStatus(HttpSC.OK_200);
            try {
                action.setResponseContentType(WebContent.contentTypeTextPlain);
                action.getResponseOutputStream().print(customHandlerBodyPost);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    private final int              port                  = WebLib.choosePort();
    // Without trailing "/"
    private final String           url                   = "http://localhost:" + port;

    @Test
    public void cfg_dataservice_named() {
        // Create a DataService and add the endpoint -> operation association.
        // This still needs the server to have the operation registered.
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        try {
            FusekiExt.registerOperation(newOp, customHandler);
            assertTrue(OperationRegistry.get().isRegistered(newOp));
            Endpoint endpoint = Endpoint.create(newOp, endpointName);
            DataService dataService = DataService.newBuilder(dsg)
                    .withStdServices(true)
                    .addEndpoint(endpoint)
                    .build();
            FusekiServer server = FusekiServer.create().port(port)
                    .registerOperation(newOp, contentType, customHandler)
                    .add("/ds", dataService)
                    .build();
            testServer(server, url, endpointName, true, false);
        }
        finally {
            FusekiExt.unregisterOperation(newOp, customHandler);
        }
        assertFalse(OperationRegistry.get().isRegistered(newOp));
    }

    @Test
    public void cfg_dataservice_unnamed() {
        // Create a DataService and add the endpoint -> operation association.
        // This still needs the server to have the operation registered.
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        try {
            FusekiExt.registerOperation(newOp, customHandler);
            assertTrue(OperationRegistry.get().isRegistered(newOp));

            Endpoint endpoint = Endpoint.create(newOp, null);
            DataService dataService = DataService.newBuilder(dsg)
                    .withStdServices(true)
                    .addEndpoint(endpoint)
                    .build();
            FusekiServer server = FusekiServer.create().port(port)
                    .registerOperation(newOp, contentType, customHandler)
                    .add("/ds", dataService)
                    .build();
            // No endpoint name dispatch - content-type required.
            testServer(server, url, "", false, true);
        }
        finally {
            FusekiExt.unregisterOperation(newOp, customHandler);
        }
        assertFalse(OperationRegistry.get().isRegistered(newOp));
    }

    @Test
    public void cfg_builder_noCT() {
        // Register operation in the builder. Dispatch by-name. CT not required.
        FusekiServer server = FusekiServer.create().port(port).registerOperation(newOp, null, customHandler)
            .add("/ds", DatasetGraphFactory.createTxnMem(), true).addEndpoint("/ds", endpointName, newOp).build();
        testServer(server, url, endpointName, true, false);
    }

    @Test
    public void cfg_builder_CT_named() {
        FusekiServer server = FusekiServer.create().port(port).registerOperation(newOp, contentType, customHandler)
            .add("/ds", DatasetGraphFactory.createTxnMem(), true).addEndpoint("/ds", endpointName, newOp).build();
        // Endpoint name dispatch - with content-type
        testServer(server, url, endpointName, true, true);
    }

    @Test
    public void cfg_builder_CT_noName() {
        // Register operation in the builder. Dispatch by-content-type on "".
        FusekiServer server = FusekiServer.create().port(port).registerOperation(newOp, contentType, customHandler)
            .add("/ds", DatasetGraphFactory.createTxnMem(), true).addEndpoint("/ds", "", newOp).build();
        testServer(server, url, "", false, true);
    }

    @Test(expected = FusekiConfigException.class)
    public void cfg_bad_01() {
        FusekiServer.create().port(port).registerOperation(newOp, null, customHandler).addEndpoint("/UNKNOWN", endpointName, newOp);
        // .build();
    }

    @Test(expected = FusekiConfigException.class)
    public void cfg_bad_02() {
        FusekiServer.create().port(port)
            // .registerOperation(newOp, null, customHandler)
            .add("/ds", DatasetGraphFactory.createTxnMem(), true)
            // Unregistered.
            .addEndpoint("/ds", endpointName, newOp);
        // .build();
    }

    // Bad test: no MIME type must match.
    @Test
    public void cfg_bad_ct_not_enabled_here_1() {
        FusekiServer server = FusekiServer.create().port(port)
            // Wrong MIME type.
            .registerOperation(newOp, "app/special", customHandler).add("/ds", DatasetGraphFactory.createTxnMem(), true)
            // Unregistered CT dispatch.
            .addEndpoint("/ds", "", newOp).build();

        // CT dispatch.
        expectFail(() -> testServer(server, url, "", false, true), HttpSC.Code.BAD_REQUEST);
    }

    /** Call the server to check that the endpoint can be contacted. */
    private static void testServer(FusekiServer server, String url, String epName, boolean withoutContentType, boolean withContentType) {
        try {
            server.start();
            Lib.sleep(100);

            String svcCall = StringUtils.isEmpty(epName) ? url + "/ds" : url + "/ds/" + epName;

            if ( withoutContentType )
                testServerNoCT(server, svcCall);

            if ( withContentType )
                testServerCT(server, svcCall);

            // DELETE -> fails
            int statusCode = expectFail(() -> HttpOp.execHttpDelete(svcCall));
            switch (statusCode) {
                // Acceptable.
                case HttpSC.BAD_REQUEST_400 :
                case HttpSC.METHOD_NOT_ALLOWED_405 :
                    break;
                default :
                    // Wrong
                    fail("Status code = " + statusCode);
            }
        }
        finally {
            server.stop();
        }
    }

    private static void testServerCT(FusekiServer server, String svcCall) {
        // Content-type
        try (TypedInputStream stream = HttpOp.execHttpPostStream(svcCall, contentType, "", "text/plain")) {
            assertNotNull(stream);
            String x = IOUtils.toString(stream, StandardCharsets.UTF_8);
            assertValidResponseBody(customHandlerBodyPost, x);
        } catch (IOException ex) {
            IO.exception(ex);
        }
    }

    private static void testServerNoCT(FusekiServer server, String svcCall) {
        // Service endpoint name : GET
        String s1 = HttpOp.execHttpGetString(svcCall);
        if ( s1 == null )
            throw new HttpException(HttpSC.NOT_FOUND_404, "Not Found", "");
        assertValidResponseBody(customHandlerBodyGet, s1);

        // Service endpoint name : POST
        try (TypedInputStream stream = HttpOp.execHttpPostStream(svcCall, "ignored", "", "text/plain")) {
            assertNotNull(stream);
            String x = IOUtils.toString(stream, StandardCharsets.UTF_8);
            assertValidResponseBody(customHandlerBodyPost, x);
        } catch (IOException ex) {
            IO.exception(ex);
        }
    }

    private static void assertValidResponseBody(String expectedResponseBody, String responseBody) {
        assertNotNull(responseBody);
        assertEquals(expectedResponseBody, responseBody);
    }
}
