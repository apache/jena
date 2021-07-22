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

package org.apache.jena.test.general;

import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.FusekiTestLib;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.web.HttpSC;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for operation run end-to-end.
 */
public class TestRemoteEndToEnd {
    // JENA-2074, JENA-2061 - bad RDF/XML causes HTTP problems if streamed directly.

    private static FusekiServer server = null;
    private static String LOG_LEVEL = null;
    private static String ActionLogName = Fuseki.actionLogName;

    @BeforeClass public static void beforeClass() {
        // Suppress warnings.
        LOG_LEVEL = LogCtl.getLevel(ActionLogName);
        LogCtl.setLevel(ActionLogName, "ERROR");

        DatasetGraph dsg1 = DatasetGraphFactory.createTxnMem();
        Quad q1 = SSE.parseQuad("(_ :s :p '\u0018')");
        dsg1.add(q1);

        DatasetGraph dsg2 = DatasetGraphFactory.createTxnMem();
        Quad q2 = SSE.parseQuad("(_ :s :p <rel>)");
        dsg2.add(q2);

        server = FusekiServer.create()
                    .add("/ds1", dsg1)
                    .add("/ds2", dsg2)
                    .port(0).build().start();
    }

    @AfterClass public static void afterClass() {
        try { server.stop(); } catch (Throwable th) {}
        LogCtl.setLevel(ActionLogName, LOG_LEVEL);
    }

    @Test public void badChar_RDFXML() {
        runTest("/ds1");
    }

    @Test public void relURI_RDFXML() {
        runTest("/ds2");
    }

    private static void runTest(String dataset) {
        int port = server.getPort();
        String URL = server.datasetURL(dataset);

        try ( RDFConnection conn = RDFConnectionRemote.newBuilder()
                .acceptHeaderGraph("application/rdf+xml")
                .destination(URL)
                .build() ) {
            FusekiTestLib.expectFail(()->conn.fetch(), HttpSC.Code.NOT_ACCEPTABLE);
            FusekiTestLib.expectQueryFail(()->conn.queryConstruct("CONSTRUCT WHERE { ?s ?p ?o }"),HttpSC.Code.NOT_ACCEPTABLE);
        }
    }
}
