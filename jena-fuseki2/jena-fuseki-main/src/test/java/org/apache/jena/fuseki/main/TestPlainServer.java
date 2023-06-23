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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.jena.http.HttpOp;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/** Test Fuseki with plain servlet and handling a file area */
public class TestPlainServer {

    private static FusekiServer server = null;
    private static int port;

    private static Model data;
    private static Dataset dataset;

    private static String serverURL;

    @BeforeClass
    public static void beforeClass() {
        FusekiServer server = FusekiServer.create()
            .port(0)
            .add("/ds", DatasetGraphFactory.createTxnMem())
            // Named like a dataset service
            .addServlet("/ds/myServlet1", new MyServlet("1"))
            // Named like a dataset
            .addServlet("/myServlet2", new MyServlet("2"))
            // File area.
            .staticFileBase("testing/Files")
            .build();
        server.start();
        port = server.getPort();
        serverURL = "http://localhost:" + port;
    }

    // Test : responds to GET
    private static class MyServlet extends HttpServlet {
        private final String label;

        public MyServlet(String label) {
            this.label = label;
        }

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            resp.setContentType("text/plain");
            resp.getOutputStream().print("SERVLET: "+label);
            resp.setStatus(200);
        }
    }

    @AfterClass
    public static void afterClass() {
        if ( server != null )
            server.stop();
    }
    // Servlet - mounted at /ds/myServlet, but not a service that Fuseki dispatches.
    @Test public void plainServlet1() {
        String x = HttpOp.httpGetString(serverURL+"/ds/myServlet1");
        assertEquals("SERVLET: 1",x);
    }

    @Test public void plainServlet2() {
        String x = HttpOp.httpGetString(serverURL+"/myServlet2");
        assertEquals("SERVLET: 2",x);
    }

    // Files - the naming can overlay the dataset naming space;
    // any dataset and its services take precedence.

    @Test public void plainFile1() {
        String x = HttpOp.httpGetString(serverURL+"/ds/file-ds.txt");
        assertNotNull(x);
        assertTrue(x.contains("CONTENT"));
    }

    // Files - a static file not visible via non-existent dataset.
    @Test public void plainFile2() {
        String x = HttpOp.httpGetString(serverURL+"/other/file-other.txt");
        assertNotNull(x);
        assertTrue(x.contains("CONTENT"));
    }

    @Test public void plainFile3() {
        String x = HttpOp.httpGetString(serverURL+"/file-top.txt");
        assertNotNull(x);
        assertTrue(x.contains("CONTENT"));
    }

    @Test public void plainFile4() {
        String x = HttpOp.httpGetString(serverURL+"/non-existent.txt");
        assertNull(x);
    }

    @Test public void plainFile5() {
        String x = HttpOp.httpGetString(serverURL+"/ds/non-existent.txt");
        assertNull(x);
    }
}
