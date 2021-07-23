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

import static org.apache.jena.fuseki.test.FusekiTest.expect400;
import static org.apache.jena.fuseki.test.FusekiTest.expect404;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;

import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.http.HttpOp2;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.IsoMatcher;
import org.apache.jena.test.conn.EnvTest;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestGSP {

    static String DIR = "testing/RDFLink/";

    private static EnvTest env;
    @BeforeClass public static void beforeClass() {
        env = EnvTest.create("/ds");
    }

    @Before public void before() {
        env.clear();
    }

    @AfterClass public static void afterClass() {
        EnvTest.stop(env);
    }

    private static Graph graph1 = SSE.parseGraph("(graph (:s :p :x) (:s :p 1))");
    private static Graph graph2 = SSE.parseGraph("(graph (:s :p :x) (:s :p 2))");

    private String url(String path) { return env.datasetPath(path); }

    static String gspServiceURL()   { return env.datasetPath("/data"); }

    static String defaultGraphURL() { return gspServiceURL()+"?default"; }
    static String namedGraphURL()   { return gspServiceURL()+"?graph=http://example/g"; }

    // Graph, with one triple in it.
    static Graph graph = makeGraph();
    static Graph makeGraph() {
        Graph graph = GraphFactory.createDefaultGraph();
        RDFDataMgr.read(graph, new StringReader("PREFIX : <http://example/> :s :p :o ."), null, Lang.TTL);
        return graph;
    }

    static DatasetGraph dataset = makeDatasetGraph();
    static DatasetGraph makeDatasetGraph() {
        DatasetGraph dataset = DatasetGraphFactory.createTxnMem();
        RDFDataMgr.read(dataset, new StringReader("PREFIX : <http://example/> :s :p :o . :g { :sg :pg :og }"), null, Lang.TRIG);
        return dataset;
    }

    @Test public void gsp_put_get_01() {
        GSP.service(gspServiceURL())
            .defaultGraph()
            .PUT(graph);
        Graph g = GSP.service(gspServiceURL())
            .defaultGraph()
            .GET();
        assertNotNull(g);
        assertTrue(graph.isIsomorphicWith(g));
    }

    @Test(expected=HttpException.class)
    public void gsp_bad_put_01() {
        // No .defaultGraph
        GSP.service(gspServiceURL()).PUT(graph);
    }

    @Test(expected=HttpException.class)
    public void gsp_bad_get_err_02() {
        // No .defaultGraph
        GSP.service(gspServiceURL()).GET();
    }

    @Test public void gsp_post_get_ct_01() {
        String graphName = "http://example/graph";
        GSP.service(gspServiceURL())
            .graphName(graphName)
            .POST(graph);
        Graph g1 = GSP.service(gspServiceURL())
            .defaultGraph()
            .acceptHeader("application/rdf+xml")
            .GET();
        assertNotNull(g1);
        assertTrue(g1.isEmpty());

        Graph g2 = GSP.service(gspServiceURL())
            .graphName(graphName)
            .acceptHeader("application/rdf+xml")
            .GET();
        assertNotNull(g2);
        assertFalse(g2.isEmpty());
        assertTrue(graph.isIsomorphicWith(g2));
    }

    @Test public void gsp_put_get_ct_02() {
        GSP.service(gspServiceURL())
            .defaultGraph()
            .contentType(RDFFormat.NTRIPLES)
            .PUT(graph);
        Graph g1 = GSP.service(gspServiceURL())
            .defaultGraph()
            .accept(Lang.RDFXML)
            .GET();
        assertNotNull(g1);
        assertFalse(g1.isEmpty());
        assertTrue(graph.isIsomorphicWith(g1));
    }

    @Test public void gsp_put_delete_01() {
        GSP.service(gspServiceURL())
            .defaultGraph()
            .PUT(graph);
        Graph g1 = GSP.service(gspServiceURL())
             .defaultGraph()
             .GET();
        assertFalse(g1.isEmpty());

        GSP.service(gspServiceURL())
            .defaultGraph()
            .DELETE();
        Graph g2 = GSP.service(gspServiceURL())
            .defaultGraph()
            .GET();
        assertTrue(g2.isEmpty());

        // And just to make sure ...
        String s2 = HttpOp2.httpGetString(defaultGraphURL(), WebContent.contentTypeNTriples);
        // Default always exists so this is the empty graph in N-triples.
        assertTrue(s2.isEmpty());
    }

    @Test public void gsp_dft_ct_1() {
        GSP.service(url("/ds")).defaultGraph().contentType(RDFFormat.RDFXML).PUT(DIR+"data-rdfxml");
    }

    @Test public void gsp_dft_ct_2() {
        GSP.service(url("/ds")).defaultGraph().contentTypeHeader(WebContent.contentTypeRDFXML).PUT(DIR+"data-rdfxml");
    }

    // ----------------------------------------

    @Test public void gsp_ds_put_get_01() {
        GSP.service(gspServiceURL()).putDataset(dataset);
        DatasetGraph dsg = GSP.service(gspServiceURL()).getDataset();
        assertNotNull(dsg);
        assertTrue(IsoMatcher.isomorphic(dataset, dsg));
    }

    @Test public void gsp_ds_post_get_02() {
        GSP.service(gspServiceURL()).postDataset(dataset);
        DatasetGraph dsg = GSP.service(gspServiceURL()).getDataset();
        assertNotNull(dsg);
        assertTrue(IsoMatcher.isomorphic(dataset, dsg));
    }

    @Test(expected=HttpException.class)
    public void gsp_ds_err_01() {
        GSP.service(gspServiceURL()).defaultGraph().putDataset(dataset);
    }

    @Test
    public void gsp_ds_clear_01() {
        // DELETE on the GSP endpoint would be the default graph.
        // DELETE on the dataset endpoint is not supported by Fuseki - this does "CLER ALL"
        GSP.service(env.datasetURL()).clearDataset();
    }

    @Test
    public void gsp_ds_clear_02() {
        GSP.service(gspServiceURL()).postDataset(dataset);
        GSP.service(env.datasetURL()).clearDataset();
        DatasetGraph dsg = GSP.service(gspServiceURL()).getDataset();
        assertFalse(dsg.find().hasNext());
    }


//    @Test public void gsp_ds_put_delete_01() {
//        GSP.request(gspServiceURL()).putDataset(dataset);
//        GSP.request(gspServiceURL()).clearDataset();
//        DatasetGraph dsg = GSP.request(gspServiceURL()).getDataset();
//        assertTrue(dsg.isEmpty());
//    }

    @Test public void gsp_union_get() {
        Node gn1 = NodeFactory.createURI("http://example/graph1");
        Node gn2 = NodeFactory.createURI("http://example/graph2");
        GSP.service(gspServiceURL())
           .graphName(gn1)
           .PUT(graph1);
        GSP.service(gspServiceURL())
           .graphName(gn2)
            .PUT(graph2);
        // get union

        Graph g = GSP.service(gspServiceURL()).graphName("union").GET();
        assertEquals(3, g.size());
    }

    @Test public void gsp_union_post() {
        expect400(()->{
            GSP.service(gspServiceURL()).graphName("union").POST(graph1);
        });
    }

    // 404

    @Test public void gsp_404_01() {
        String graphName = "http://example/graph2";
        Node gn = NodeFactory.createURI("http://example/graph2");
        GSP.service(gspServiceURL())
            .graphName(gn)
            .PUT(graph);
        Graph g = GSP.service(gspServiceURL())
            .graphName(graphName)
            .GET();
        assertFalse(g.isEmpty());
        GSP.service(gspServiceURL())
            .graphName(gn)
            .DELETE();

        expect404(()->
            GSP.service(gspServiceURL())
                .graphName(graphName)
                .GET()
        );
    }

    @Test public void gsp_404_1() {
        String graphName = "http://example/graph404";
        expect404(
            ()->GSP.service(gspServiceURL()).graphName(graphName).GET()
        );
    }

    @Test public void gsp_404_2() {
        expect404(
            ()->GSP.service(gspServiceURL()+"junk").getDataset()
        );
    }
}
