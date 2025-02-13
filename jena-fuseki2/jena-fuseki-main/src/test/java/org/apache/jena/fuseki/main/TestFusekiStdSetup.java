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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.fuseki.test.HttpTest;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.jena.sparql.sse.SSE;

/** Tests for .add("/ds", dsg) */
public class TestFusekiStdSetup {

    private static FusekiServer server = null;
    private static int port;

    private static Graph data;
    private static DatasetGraph dataset;

    private static String URL;

    @BeforeAll
    public static void beforeClass() {
        data = SSE.parseGraph(StrUtils.strjoinNL
            ("(graph"
            ,"   (:s :p 1)"
            ,")"));

        dataset = DatasetGraphFactory.create();
        dataset.add(SSE.parseQuad("(:g :s :p 2 )"));

        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();

        server = FusekiServer.create()
            .add("/ds", dsg)
            .port(0)
            .build();
        server.start();
        URL = server.datasetURL("/ds");
    }

    @AfterAll
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
        exec(URL, "/update", conn -> conn.update("INSERT DATA { <x:s> <x:p> 123 }"));
    }

    @Test
    public void stdSetup_endpoint_4() {
        exec(URL, "/get", conn -> conn.get());
    }

    @Test
    public void stdSetup_endpoint_5() {
        exec(URL, "/data", conn -> conn.get());
    }

    @Test
    public void stdSetup_endpoint_6() {
        exec(URL, "/data", conn -> conn.put(data));
    }

    @Test
    public void stdSetup_endpoint_7() {
        exec(URL, "/data", conn -> conn.putDataset(dataset));
    }

    @Test
    public void stdSetup_dataset_1() {
        exec(URL, conn -> conn.queryAsk("ASK{}"));
    }

    @Test
    public void stdSetup_dataset_2() {
        exec(URL, conn -> conn.update("INSERT DATA { <x:s> <x:p> 123 }"));
    }

    @Test
    public void stdSetup_dataset_3() {
        exec(URL, conn -> conn.get());
    }

    @Test
    public void stdSetup_dataset_4() {
        exec(URL, conn -> conn.getDataset());
    }

    @Test
    public void stdSetup_dataset_5() {
        Node gn = NodeFactory.createURI("http://example");
        exec(URL, conn -> conn.put(gn, data));
    }

    @Test
    public void stdSetup_dataset_6() {
        exec(URL, conn -> conn.putDataset(dataset));
    }

    @Test public void stdSetup_endpoint_bad_1() {
        HttpTest.expect405( () -> exec(URL, "/get", conn->conn.put(data)) );
    }

    @Test public void stdSetup_endpoint_bad_2() {
        HttpTest.expect415( () -> exec(URL, "/query",  conn->conn.update("INSERT DATA { <x:s> <x:p> 123 }")) );
    }

    @Test public void stdSetup_endpoint_bad_3() {
        HttpTest.expect405( () -> exec(URL, "/update", conn->conn.queryAsk("ASK{}")) );
    }

    @Test public void stdSetup_endpoint_bad_4() {
        HttpTest.expect404( () -> exec(URL, "/doesNotExist", conn->conn.queryAsk("ASK{}")) );
    }

    @Test public void stdSetup_endpoint_bad_5() {
        HttpTest.expect404( () -> exec(URL+"2", "", conn->conn.queryAsk("ASK{}")) );
    }

    @Test public void stdSetup_endpoint_bad_6() {
        HttpTest.expect404( () -> exec(URL, "/nonsense", conn -> conn.putDataset(dataset)) );
    }

    private static void exec(String url, Consumer<RDFLink> action) {
        execEx(url, null, action);
    }

    private static void exec(String url, String ep, Consumer<RDFLink> action) {
        try {
            execEx(url, ep, action);
        } catch (HttpException ex) {
            handleException(ex, ex.getStatusCode(), ex.getMessage());
        } catch (QueryExceptionHTTP ex) {
            handleException(ex, ex.getStatusCode(), ex.getMessage());
        }
    }

    private static void execEx(String url, String ep, Consumer<RDFLink> action) {
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
        try ( RDFLink conn = RDFLink.connect(dest) ) {
            action.accept(conn);
        }
    }

    private static void handleException(RuntimeException ex, int responseCode, String message) {
//        System.out.flush();
//        System.err.println("**** "+message);
        throw ex;
    }
}
