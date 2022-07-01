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

/** Tests for .add("/ds", dsg) */
public class TestStdSetup {

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
            .add("/ds", dsg)
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

    @Test public void stdSetup_1() {

        svcExec(URL, "/query",   conn->conn.queryAsk("ASK{}") );
        svcExec(URL, "/sparql",  conn->conn.queryAsk("ASK{}") );
        svcExec(URL, "/update",  conn->conn.update("INSERT DATA { <x:s> <x:p> 123 }") );
        svcExec(URL, "/get",     conn->conn.fetch());
        svcExec(URL, "/data",    conn->conn.fetch());
        svcExec(URL, "/data",    conn->conn.put(data));
        svcExec(URL, "/data",    conn->conn.putDataset(dataset));

    }

    @Test public void stdSetup_2() {
        execDataset(URL, conn->conn.queryAsk("ASK{}") );
        execDataset(URL, conn->conn.update("INSERT DATA { <x:s> <x:p> 123 }") );
        execDataset(URL, conn->conn.fetch());
        execDataset(URL, conn->conn.fetchDataset());
        execDataset(URL, conn->conn.put("http://example", data));
        execDataset(URL, conn->conn.putDataset(dataset));
    }

    @Test public void stdSetup_3() {
        svcExecFail(URL, "/get",    (RDFConnection conn)->conn.put(data));
        svcExecFail(URL, "/query",  (RDFConnection conn)->conn.update("INSERT DATA { <x:s> <x:p> 123 }") );
        svcExecFail(URL, "/update", (RDFConnection conn)->conn.queryAsk("ASK{}") );
        svcExecFail(URL, "/doesNotExist", (RDFConnection conn)->conn.queryAsk("ASK{}") );
        svcExecFail(URL+"2", "", (RDFConnection conn)->conn.queryAsk("ASK{}") );

    }


    private static void svcExec(String url, String ep, Consumer<RDFConnection> action) {
        exec(url, ep, action);
    }

    private static void svcExecFail(String url, String ep, Consumer<RDFConnection> action) {
        execFail(url, ep, action);
    }

    private static void execDataset(String url, Consumer<RDFConnection> action) {
        exec(url, null, action);
    }

    private static void execDatasetFail(String url, Consumer<RDFConnection> action) {
        execFail(url, null, action);
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

    private static void execFail(String url, String ep, Consumer<RDFConnection> action) {
        try {
            execEx(url, ep, action);
            System.err.println("Expected an exception");
        }
        catch (HttpException ex) {}
        catch (QueryExceptionHTTP ex) {}
    }

}
