/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.sparql.exec.http;

import static org.apache.jena.fuseki.test.HttpTest.expect400;
import static org.apache.jena.fuseki.test.HttpTest.expect404;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.FusekiTestLib;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.http.HttpOp;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.IsoMatcher;

public class TestGSP {

    static String DIR = "testing/RDFLink/";

    private final boolean verbose = false;

    private final String dsName = "/data";

    public FusekiServer createServer() {
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        FusekiServer server = FusekiServer.create()
                .port(0)
                .verbose(verbose)
                .enablePing(true)
                .add(dsName, dsg)
                .build()
                .start();
        return server;
    }

    private String url(FusekiServer server, String path) {
        return server.datasetURL(path);
    }

    // GSP endpoint
    private String gspServiceURL(String datasetURL) {
        return datasetURL;
    }

    private String defaultGraphURL(String datasetURL) {
        return gspServiceURL(datasetURL) + "?default";
    }

    private String namedGraphURL(String datasetURL) {
        return gspServiceURL(datasetURL) + "?graph=http://example/g";
    }

    @FunctionalInterface
    interface Action { void run(String datasetURL); }

    private void withServerURL(Action action) {
        FusekiServer server = createServer().start();
        try {
            String datasetURL = server.datasetURL(dsName);
            action.run(datasetURL);
        } finally {
            server.stop();
        }
    }

    private static Graph graph1 = SSE.parseGraph("(graph (:s :p :x) (:s :p 1))");
    private static Graph graph2 = SSE.parseGraph("(graph (:s :p :x) (:s :p 2))");

    // Graph, with one triple in it.
    static Graph graph = makeGraph();
    static Graph makeGraph() {
        Graph graph = GraphFactory.createDefaultGraph();
        RDFParser.fromString("PREFIX : <http://example/> :s :p :o .", Lang.TTL).parse(graph);
        return graph;
    }

    static DatasetGraph dataset = makeDatasetGraph();
    static DatasetGraph makeDatasetGraph() {
        DatasetGraph dataset = DatasetGraphFactory.createTxnMem();
        RDFParser.fromString("PREFIX : <http://example/> :s :p :o . :g { :sg :pg :og }", Lang.TRIG).parse(dataset);
        return dataset;
    }

    @Test
    public void gsp_put_get_01() {
        withServerURL(datasetURL->{
            GSP.service(datasetURL).defaultGraph().PUT(graph);
            Graph g = GSP.service(gspServiceURL(datasetURL)).defaultGraph().GET();
            assertNotNull(g);
            assertTrue(IsoMatcher.isomorphic(graph, g));
        });
    }

    @Test
    public void gsp_bad_put_01() {
        withServerURL(datasetURL->{
            assertThrows(HttpException.class, ()->GSP.service(gspServiceURL(datasetURL)).PUT(graph));
        });
    }

    @Test
    public void gsp_bad_get_err_02() {
        withServerURL(datasetURL -> {
            assertThrows(HttpException.class, () -> GSP.service(gspServiceURL(datasetURL)).GET());
        });
    }

    @Test
    public void gsp_post_get_ct_01() {
        withServerURL(datasetURL -> {
            String graphName = "http://example/graph";
            GSP.service(gspServiceURL(datasetURL)).graphName(graphName).POST(graph);
            Graph g1 = GSP.service(gspServiceURL(datasetURL)).defaultGraph().acceptHeader("application/rdf+xml").GET();
            assertNotNull(g1);
            assertTrue(g1.isEmpty());

            Graph g2 = GSP.service(gspServiceURL(datasetURL)).graphName(graphName).acceptHeader("application/rdf+xml").GET();
            assertNotNull(g2);
            assertFalse(g2.isEmpty());
            assertTrue(IsoMatcher.isomorphic(graph, g2));
        });
    }

    @Test
    public void gsp_put_get_ct_02() {
        withServerURL(datasetURL->{
            GSP.service(gspServiceURL(datasetURL)).defaultGraph().contentType(RDFFormat.NTRIPLES).PUT(graph);
            Graph g1 = GSP.service(gspServiceURL(datasetURL)).defaultGraph().accept(Lang.RDFXML).GET();
            assertNotNull(g1);
            assertFalse(g1.isEmpty());
            assertTrue(IsoMatcher.isomorphic(graph, g1));
        });
    }

    @Test
    public void gsp_put_delete_01() {
        withServerURL(datasetURL->{
            GSP.service(gspServiceURL(datasetURL)).defaultGraph().PUT(graph);
            Graph g1 = GSP.service(gspServiceURL(datasetURL)).defaultGraph().GET();
            assertFalse(g1.isEmpty());

            GSP.service(gspServiceURL(datasetURL)).defaultGraph().DELETE();
            Graph g2 = GSP.service(gspServiceURL(datasetURL)).defaultGraph().GET();
            assertTrue(g2.isEmpty());

            // And just to make sure ...
            String s2 = HttpOp.httpGetString(defaultGraphURL(datasetURL), WebContent.contentTypeNTriples);
            // Default always exists so this is the empty graph in N-triples.
            assertTrue(s2.isEmpty());
        });
    }

    @Test
    public void gsp_dft_ct_1() {
        withServerURL(datasetURL->{
            GSP.service(gspServiceURL(datasetURL)).defaultGraph().contentType(RDFFormat.RDFXML).PUT(DIR + "data-rdfxml");
        });
    }

    @Test
    public void gsp_dft_ct_2() {
        withServerURL(datasetURL->{
            GSP.service(gspServiceURL(datasetURL)).defaultGraph().contentTypeHeader(WebContent.contentTypeRDFXML).PUT(DIR + "data-rdfxml");
        });
    }

    // ----------------------------------------

    @Test
    public void gspHead_dataset_1() {
        withServerURL(datasetURL->{
// Base URL, default content type => N-Quads (dump format)
            String h = HttpOp.httpHead(gspServiceURL(datasetURL), null);
            assertNotNull(h);
            assertEquals(Lang.NQUADS.getHeaderString(), h);
        });
    }

    @Test
    public void gspHead_dataset_2() {
        withServerURL(datasetURL->{
            String ct = Lang.TRIG.getHeaderString();
            String h = HttpOp.httpHead(gspServiceURL(datasetURL), ct);
            assertNotNull(h);
            assertEquals(ct, h);
        });
    }

    @Test
    public void gspHead_graph_1() {
        withServerURL(datasetURL->{
            String target = defaultGraphURL(datasetURL);
            String h = HttpOp.httpHead(target, null);
            assertNotNull(h);
            // "Traditional default".
            assertEquals(Lang.RDFXML.getHeaderString(), h);
        });
    }

    @Test
    public void gspHead_graph_2() {
        withServerURL(datasetURL->{
            String target = defaultGraphURL(datasetURL);
            String ct = Lang.TTL.getHeaderString();
            String h = HttpOp.httpHead(target, ct);
            assertNotNull(h);
            assertEquals(ct, h);
        });
    }

    @Test
    public void gsp_union_get() {
        withServerURL(datasetURL->{
            Node gn1 = NodeFactory.createURI("http://example/graph1");
            Node gn2 = NodeFactory.createURI("http://example/graph2");
            GSP.service(gspServiceURL(datasetURL)).graphName(gn1).PUT(graph1);
            GSP.service(gspServiceURL(datasetURL)).graphName(gn2).PUT(graph2);
            // get union

            Graph g = GSP.service(gspServiceURL(datasetURL)).graphName("union").GET();
            assertEquals(3, g.size());
        });
    }

    @Test
    public void gsp_union_post() {
        withServerURL(datasetURL->{
            expect400(() -> {
                GSP.service(gspServiceURL(datasetURL)).graphName("union").POST(graph1);
            });
        });
    }

    // 404

    @Test
    public void gsp_404_put_delete_get() {
        withServerURL(datasetURL->{
            String graphName = "http://example/graph2";
            Node gn = NodeFactory.createURI("http://example/graph2");
            GSP.service(gspServiceURL(datasetURL)).graphName(gn).PUT(graph);
            Graph g = GSP.service(gspServiceURL(datasetURL)).graphName(graphName).GET();
            assertFalse(g.isEmpty());
            GSP.service(gspServiceURL(datasetURL)).graphName(gn).DELETE();
            expect404(() -> GSP.service(gspServiceURL(datasetURL)).graphName(graphName).GET());
        });
    }

    @Test
    public void gsp_404_graph() {
        withServerURL(datasetURL->{
            String graphName = "http://example/graph404";
            expect404(() -> GSP.service(gspServiceURL(datasetURL)).graphName(graphName).GET());
        });
    }

    @Test public void gsp_config_general_dataset() {
        // Issue GH-4292
        // DatasetGraphMapLink
        DatasetGraph dsg = DatasetGraphFactory.createGeneral();
        FusekiServer server = FusekiServer.create().port(0).add(dsName, dsg).build();
        server.start();

        String URL = server.datasetURL(dsName);
        Node gn = NodeFactory.createURI("urn:ns:missing");
        try {
            // Expect 404
            FusekiTestLib.expect404(()->
                GSP.service(URL+"/data").graphName(gn).DELETE()
                );
            // GET 400
            //GSP.service(URL+"/data").graphName(gn).GET();

            QueryExec qExec = QueryExecHTTP.service(URL).query("SELECT * { GRAPH ?g { } }").build();
            RowSet rowSet = qExec.select();
            // No named graphs expected
            assertFalse(rowSet.hasNext(), "No named graphs expected");
        } finally {
            server.stop();
        }
    }
}
