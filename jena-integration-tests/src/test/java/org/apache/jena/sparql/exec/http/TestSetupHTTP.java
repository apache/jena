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

package org.apache.jena.sparql.exec.http;

import java.net.http.HttpClient;
import java.time.Duration;

import org.apache.jena.fuseki.main.FusekiServer ;
import org.apache.jena.graph.Graph;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.rdflink.RDFLinkHTTP;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.DatasetGraphFactory ;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.system.Txn ;
import org.junit.AfterClass ;
import org.junit.Before ;
import org.junit.BeforeClass ;
import org.junit.Test;

public class TestSetupHTTP  {
    private static FusekiServer server ;
    private static DatasetGraph serverdsg = DatasetGraphFactory.createTxnMem() ;
    private static HttpClient httpClient;
    private static String URL;
    // ---- Test data.
    private static Graph g = SSE.parseGraph("(graph (:s :p :o))");
    private static DatasetGraph dsg = SSE.parseDatasetGraph("(dataset (:g :s :p 123))");

    @BeforeClass
    public static void beforeClass() {
        server = FusekiServer.create().loopback(true)
            .port(0)
            .add("/ds", serverdsg)
            .start() ;
        URL = "http://localhost:"+server.getPort()+"/ds";
        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Before
    public void beforeTest() {
        // Clear server
        Txn.executeWrite(serverdsg, ()->serverdsg.clear()) ;
    }

    @AfterClass
    public static void afterClass() {
        server.stop();
    }

    @Test public void setup_GSP() {
        GSP.service(URL).httpClient(httpClient).contentType(RDFFormat.NT).defaultGraph().PUT(g);
        GSP.service(URL).httpClient(httpClient).acceptHeader("application/n-triples").defaultGraph().GET();
    }

    @SuppressWarnings("deprecation")
    @Test public void setup_GSP_dataset() {
        GSP.service(URL).httpClient(httpClient).dataset().contentType(RDFFormat.NQ).putDataset(dsg);
        GSP.service(URL).httpClient(httpClient).acceptHeader("application/n-triples").dataset().getDataset();
    }

    @Test public void setup_DSP() {
        DSP.service(URL).httpClient(httpClient).contentType(RDFFormat.NQ).PUT(dsg);
        DSP.service(URL).httpClient(httpClient).acceptHeader("application/n-quads").GET();
    }

    @Test public void setup_RDFLink() {
        try ( RDFLink link = RDFLinkHTTP.service(URL)
                .httpClient(httpClient)
                .acceptHeaderGraph("application/rdf+xml")
                .acceptHeaderDataset("application/n-quads")
                .quadsFormat(Lang.NQ)
                .triplesFormat(Lang.RDFXML)
                .build() ) {
            link.load(g);
            link.get();

            link.loadDataset(dsg);
            link.getDataset();
        }
    }
}
