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

import static org.apache.jena.fuseki.test.HttpTest.expect400;
import static org.apache.jena.fuseki.test.HttpTest.expect404;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.fuseki.main.FusekiServer;
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
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.IsoMatcher;

public class TestGSP {

    static String DIR = "testing/RDFLink/";

    private FusekiServer server = null;
    private final boolean verbose = false;

    private final String dsName = "/data";

    @BeforeEach
    public void makeServer() {
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        server = FusekiServer.create()
                .verbose(verbose)
                .enablePing(true)
                //.addServlet(data, holder)
                .add(dsName, dsg)
                .build()
                .start();
    }

    @AfterEach
    public void releaseServer() {
        if ( server != null )
            server.stop();
    }


    private String url(String path) {
        return server.datasetURL(path);
    }

    // GSP endpoint
    private String gspServiceURL() {
        return url(dsName);
    }

    private String defaultGraphURL() {
        return gspServiceURL() + "?default";
    }

    private String namedGraphURL() {
        return gspServiceURL() + "?graph=http://example/g";
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
        GSP.service(gspServiceURL()).defaultGraph().PUT(graph);
        Graph g = GSP.service(gspServiceURL()).defaultGraph().GET();
        assertNotNull(g);
        assertTrue(IsoMatcher.isomorphic(graph, g));
    }

    @Test
    public void gsp_bad_put_01() {
        // No .defaultGraph
        assertThrows(HttpException.class, ()->GSP.service(gspServiceURL()).PUT(graph));
    }

    @Test
    public void gsp_bad_get_err_02() {
        // No .defaultGraph
        assertThrows(HttpException.class, ()->GSP.service(gspServiceURL()).GET());
    }

    @Test
    public void gsp_post_get_ct_01() {
        String graphName = "http://example/graph";
        GSP.service(gspServiceURL()).graphName(graphName).POST(graph);
        Graph g1 = GSP.service(gspServiceURL()).defaultGraph().acceptHeader("application/rdf+xml").GET();
        assertNotNull(g1);
        assertTrue(g1.isEmpty());

        Graph g2 = GSP.service(gspServiceURL()).graphName(graphName).acceptHeader("application/rdf+xml").GET();
        assertNotNull(g2);
        assertFalse(g2.isEmpty());
        assertTrue(IsoMatcher.isomorphic(graph, g2));
    }

    @Test
    public void gsp_put_get_ct_02() {
        GSP.service(gspServiceURL()).defaultGraph().contentType(RDFFormat.NTRIPLES).PUT(graph);
        Graph g1 = GSP.service(gspServiceURL()).defaultGraph().accept(Lang.RDFXML).GET();
        assertNotNull(g1);
        assertFalse(g1.isEmpty());
        assertTrue(IsoMatcher.isomorphic(graph, g1));
    }

    @Test
    public void gsp_put_delete_01() {
        GSP.service(gspServiceURL()).defaultGraph().PUT(graph);
        Graph g1 = GSP.service(gspServiceURL()).defaultGraph().GET();
        assertFalse(g1.isEmpty());

        GSP.service(gspServiceURL()).defaultGraph().DELETE();
        Graph g2 = GSP.service(gspServiceURL()).defaultGraph().GET();
        assertTrue(g2.isEmpty());

        // And just to make sure ...
        String s2 = HttpOp.httpGetString(defaultGraphURL(), WebContent.contentTypeNTriples);
        // Default always exists so this is the empty graph in N-triples.
        assertTrue(s2.isEmpty());
    }

    @Test
    public void gsp_dft_ct_1() {
        GSP.service(gspServiceURL()).defaultGraph().contentType(RDFFormat.RDFXML).PUT(DIR + "data-rdfxml");
    }

    @Test
    public void gsp_dft_ct_2() {
        GSP.service(gspServiceURL()).defaultGraph().contentTypeHeader(WebContent.contentTypeRDFXML).PUT(DIR + "data-rdfxml");
    }

    // ----------------------------------------

    @Test
    public void gspHead_dataset_1() {
        // Base URL, default content type => N-Quads (dump format)
        String h = HttpOp.httpHead(gspServiceURL(), null);
        assertNotNull(h);
        assertEquals(Lang.NQUADS.getHeaderString(), h);
    }

    @Test
    public void gspHead_dataset_2() {
        String ct = Lang.TRIG.getHeaderString();
        String h = HttpOp.httpHead(gspServiceURL(), ct);
        assertNotNull(h);
        assertEquals(ct, h);
    }

    @Test
    public void gspHead_graph_1() {
        String target = defaultGraphURL();
        String h = HttpOp.httpHead(target, null);
        assertNotNull(h);
        // "Traditional default".
        assertEquals(Lang.RDFXML.getHeaderString(), h);
    }

    @Test
    public void gspHead_graph_2() {
        String target = defaultGraphURL();
        String ct = Lang.TTL.getHeaderString();
        String h = HttpOp.httpHead(target, ct);
        assertNotNull(h);
        assertEquals(ct, h);
    }

    @Test
    public void gsp_union_get() {
        Node gn1 = NodeFactory.createURI("http://example/graph1");
        Node gn2 = NodeFactory.createURI("http://example/graph2");
        GSP.service(gspServiceURL()).graphName(gn1).PUT(graph1);
        GSP.service(gspServiceURL()).graphName(gn2).PUT(graph2);
        // get union

        Graph g = GSP.service(gspServiceURL()).graphName("union").GET();
        assertEquals(3, g.size());
    }

    @Test
    public void gsp_union_post() {
        expect400(() -> {
            GSP.service(gspServiceURL()).graphName("union").POST(graph1);
        });
    }

    // 404

    @Test
    public void gsp_404_put_delete_get() {
        String graphName = "http://example/graph2";
        Node gn = NodeFactory.createURI("http://example/graph2");
        GSP.service(gspServiceURL()).graphName(gn).PUT(graph);
        Graph g = GSP.service(gspServiceURL()).graphName(graphName).GET();
        assertFalse(g.isEmpty());
        GSP.service(gspServiceURL()).graphName(gn).DELETE();
        expect404(() -> GSP.service(gspServiceURL()).graphName(graphName).GET());
    }

    @Test
    public void gsp_404_graph() {
        String graphName = "http://example/graph404";
        expect404(() -> GSP.service(gspServiceURL()).graphName(graphName).GET());
    }
}
