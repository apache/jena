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

import java.util.function.Consumer;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.fuseki.test.HttpTest;
import org.apache.jena.graph.Graph;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.jena.sparql.sse.SSE;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/** Tests for .add("/ds", dsg, false) */
public class TestFusekiStdReadOnlySetup {
    // This test suite is TestFusekiStdSetup, modified for read-only.

    private static FusekiServer server = null;
    private static int port;

    private static Model data;
    private static Dataset dataset;

    private static String URL;

    @BeforeClass
    public static void beforeClass() {
        Graph graph = SSE.parseGraph(StrUtils.strjoinNL
            ("(graph"
            ,"   (:s :p 1)"
            ,")"));
        data = ModelFactory.createModelForGraph(graph);

        DatasetGraph dsgData = DatasetGraphFactory.create();
        dsgData.add(SSE.parseQuad("(:g :s :p 2 )"));
        dataset = DatasetFactory.wrap(dsgData);

        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();

        FusekiServer server = FusekiServer.create()
            .add("/ds", dsg, false)
            .port(0)
            .build();
        server.start();
        URL = server.datasetURL("/ds");
    }

    @AfterClass
    public static void afterClass() {
        if ( server != null )
            server.stop();
    }

    @Test
    public void stdSetup_endpoint_1() {
        exec(URL, "/query", conn -> conn.queryAsk("ASK{}"));
    }

    @Test
    public void stdSetup_endpoint_2() {
        exec(URL, "/sparql", conn -> conn.queryAsk("ASK{}"));
    }

    @Test
    public void stdSetup_endpoint_3() {
        // Read-only : No "/update" endpoint.
        HttpTest.expect404(() -> exec(URL, "/update", conn -> conn.update("INSERT DATA { <x:s> <x:p> 123 }")) );
    }

    @Test
    public void stdSetup_endpoint_4() {
        exec(URL, "/get", conn -> conn.fetch());
    }

    @Test
    public void stdSetup_endpoint_5() {
        exec(URL, "/data", conn -> conn.fetch());
    }

    @Test
    public void stdSetup_endpoint_6() {
        // Read-only : PUT not allowed.
        HttpTest.expect405(() -> exec(URL, "/data", conn -> conn.put(data)) );
    }

    @Test
    public void stdSetup_endpoint_7() {
        // Read-only : PUT not allowed.
        HttpTest.expect405(() -> exec(URL, "/data", conn -> conn.putDataset(dataset)) );
    }

    @Test
    public void stdSetup_endpoint_8() {
        HttpTest.expect404( () -> exec(URL, "/nonsense", conn -> conn.putDataset(dataset)) );
    }

    @Test
    public void stdSetup_dataset_1() {
        exec(URL, conn -> conn.queryAsk("ASK{}"));
    }

    @Test
    public void stdSetup_dataset_2() {
        // Read-only : POST of an update not allowed. Multiple endpoints on the dataset URL (query, gsp-r) so general error.
        HttpTest.expect400(() -> exec(URL, conn -> conn.update("INSERT DATA { <x:s> <x:p> 123 }")) );
    }

    @Test
    public void stdSetup_dataset_3() {
        exec(URL, conn -> conn.fetch());
    }

    @Test
    public void stdSetup_dataset_4() {
        exec(URL, conn -> conn.fetchDataset());
    }

    @Test
    public void stdSetup_dataset_5() {
        HttpTest.expect405(() -> exec(URL, conn -> conn.put("http://example", data)) );
    }

    @Test
    public void stdSetup_dataset_6() {
        // Read-only: Bad POST - only queies can be POST'ed => bad media type.
        HttpTest.expect405(() -> exec(URL, conn -> conn.putDataset(dataset)) );
    }

    @Test public void stdSetup_endpoint_bad_1() {
        HttpTest.expect405( () -> exec(URL, "/get", conn->conn.put(data)) );
    }

    @Test public void stdSetup_endpoint_bad_2() {
        HttpTest.expect415( () -> exec(URL, "/query",  conn->conn.update("INSERT DATA { <x:s> <x:p> 123 }")) );
    }

    @Test public void stdSetup_endpoint_bad_3() {
        // Read-only. It is now 404 whereas in a setup with update it is 405.
        HttpTest.expect404( () -> exec(URL, "/update", conn->conn.queryAsk("ASK{}")) );
    }

    @Test public void stdSetup_endpoint_bad_4() {
        HttpTest.expect404( () -> exec(URL, "/doesNotExist", conn->conn.queryAsk("ASK{}")) );
    }

    @Test public void stdSetup_endpoint_bad_5() {
        HttpTest.expect404( () -> exec(URL+"2", "", (RDFConnection conn)->conn.queryAsk("ASK{}")) );
    }

    private static void exec(String url, Consumer<RDFConnection> action) {
        execEx(url, null, action);
    }

    private static void exec(String url, String ep, Consumer<RDFConnection> action) {
        try {
            execEx(url, ep, action);
        } catch (HttpException ex) {
            handleException(ex, ex.getStatusCode(), ex.getMessage());
        } catch (QueryExceptionHTTP ex) {
            handleException(ex, ex.getStatusCode(), ex.getMessage());
        }
    }

    private static void execEx(String url, String ep, Consumer<RDFConnection> action) {
        String dest;
        if ( ep == null || ep.isEmpty() ) {
            dest = url;
        } else {
            if ( ! url.endsWith("/") )
                url = url+"/";
            if ( ep.startsWith("/") )
                ep = ep.substring(1);
            dest = url+ep;
        }
        try ( RDFConnection conn = RDFConnection.connect(dest) ) {
            action.accept(conn);
        }
    }

    private static void handleException(RuntimeException ex, int responseCode, String message) {
//        System.out.flush();
//        System.err.println("**** "+message);
        throw ex;
    }
}
