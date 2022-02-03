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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;

import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.http.HttpOp;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.ModelStore;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.IsoMatcher;
import org.apache.jena.test.conn.EnvTest;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestModelStore {

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

    private static Model model1 = ModelFactory.createModelForGraph(SSE.parseGraph("(graph (:s :p :x) (:s :p 1))"));
    private static Model model2 = ModelFactory.createModelForGraph(SSE.parseGraph("(graph (:s :p :x) (:s :p 2))"));

    private String url(String path) { return env.datasetPath(path); }

    static String gspServiceURL()   { return env.datasetPath("/data"); }

    static String defaultGraphURL() { return gspServiceURL()+"?default"; }
    static String namedGraphURL()   { return gspServiceURL()+"?graph=http://example/g"; }

    // Graph, with one triple in it.
    static Model graph = makeModel();
    static Model makeModel() {
        Model graph = ModelFactory.createDefaultModel();
        RDFDataMgr.read(graph, new StringReader("PREFIX : <http://example/> :s :p :o ."), null, Lang.TTL);
        return graph;
    }

    static Dataset dataset = makeDataset();
    static Dataset makeDataset() {
        Dataset dataset = DatasetFactory.createTxnMem();
        RDFDataMgr.read(dataset, new StringReader("PREFIX : <http://example/> :s :p :o . :g { :sg :pg :og }"), null, Lang.TRIG);
        return dataset;
    }

    @Test public void gsp_put_get_01() {
        ModelStore.service(gspServiceURL())
            .defaultModel()
            .PUT(graph);
        Model m = ModelStore.service(gspServiceURL())
            .defaultGraph()
            .GET();
        assertNotNull(m);
        assertTrue(graph.isIsomorphicWith(m));
    }

    @Test(expected=HttpException.class)
    public void gsp_bad_put_01() {
        // No .defaultGraph
        ModelStore.service(gspServiceURL()).PUT(graph);
    }

    @Test(expected=HttpException.class)
    public void gsp_bad_get_err_02() {
        // No .defaultGraph
        ModelStore.service(gspServiceURL()).GET();
    }

    @Test public void gsp_post_get_ct_01() {
        String graphName = "http://example/graph";
        ModelStore.service(gspServiceURL())
            .namedGraph(graphName)
            .POST(graph);
        Model m1 = ModelStore.service(gspServiceURL())
            .defaultGraph()
            .acceptHeader("application/rdf+xml")
            .GET();
        assertNotNull(m1);
        assertTrue(m1.isEmpty());

        Model m2 = ModelStore.service(gspServiceURL())
            .namedGraph(graphName)
            .acceptHeader("application/rdf+xml")
            .GET();
        assertNotNull(m2);
        assertFalse(m2.isEmpty());
        assertTrue(graph.isIsomorphicWith(m2));
    }

    @Test public void gsp_put_get_ct_02() {
        ModelStore.service(gspServiceURL())
            .defaultGraph()
            .contentType(RDFFormat.NTRIPLES)
            .PUT(graph);
        Model m1 = ModelStore.service(gspServiceURL())
            .defaultGraph()
            .accept(Lang.RDFXML)
            .GET();
        assertNotNull(m1);
        assertFalse(m1.isEmpty());
        assertTrue(graph.isIsomorphicWith(m1));
    }

    @Test public void gsp_put_delete_01() {
        ModelStore.service(gspServiceURL())
            .defaultGraph()
            .PUT(graph);
        Model m1 = ModelStore.service(gspServiceURL())
             .defaultGraph()
             .GET();
        assertFalse(m1.isEmpty());

        ModelStore.service(gspServiceURL())
            .defaultGraph()
            .DELETE();
        Model m2 = ModelStore.service(gspServiceURL())
            .defaultGraph()
            .GET();
        assertTrue(m2.isEmpty());

        // And just to make sure ...
        String s2 = HttpOp.httpGetString(defaultGraphURL(), WebContent.contentTypeNTriples);
        // Default always exists so this is the empty graph in N-triples.
        assertTrue(s2.isEmpty());
    }

    @Test public void gsp_dft_ct_1() {
        ModelStore.service(url("/ds")).defaultGraph().contentType(RDFFormat.RDFXML).PUT(DIR+"data-rdfxml");
    }

    @Test public void gsp_dft_ct_2() {
        ModelStore.service(url("/ds")).defaultGraph().contentTypeHeader(WebContent.contentTypeRDFXML).PUT(DIR+"data-rdfxml");
    }

    // ----------------------------------------

    @Test public void dsp_put_get_01() {
        ModelStore.service(gspServiceURL()).putDataset(dataset);
        Dataset ds = ModelStore.service(gspServiceURL()).getDataset();
        assertNotNull(ds);
        assertTrue(IsoMatcher.isomorphic(dataset.asDatasetGraph(), ds.asDatasetGraph()));
    }

    @Test public void dsp_post_get_02() {
        ModelStore.service(gspServiceURL()).postDataset(dataset);
        Dataset ds = ModelStore.service(gspServiceURL()).getDataset();
        assertNotNull(ds);
        assertTrue(IsoMatcher.isomorphic(dataset.asDatasetGraph(), ds.asDatasetGraph()));
    }

    // Not an error for ModelStore.
//    @Test(expected=HttpException.class)
//    public void dsp_err_01() {
//        ModelStore.service(gspServiceURL()).defaultGraph().putDataset(dataset);
//    }

    @Test
    public void gsp_head_01() {
        // HEAD on the GSP endpoint would be the default graph.
        // DELETE on the dataset endpoint is not supported by Fuseki - this does "CLER ALL"
        ModelStore.service(env.datasetURL()).clearDataset();
    }

    @Test
    public void dsp_clear_01() {
        // DELETE on the GSP endpoint would be the default graph.
        // DELETE on the dataset endpoint is not supported by Fuseki - this does "CLER ALL"
        ModelStore.service(env.datasetURL()).clearDataset();
    }

    @Test
    public void dsp_clear_02() {
        ModelStore.service(gspServiceURL()).postDataset(dataset);
        ModelStore.service(env.datasetURL()).clearDataset();
        Dataset ds = ModelStore.service(gspServiceURL()).getDataset();
        assertTrue(ds.isEmpty());
    }


    @Test public void dsp_put_delete_01() {
        ModelStore.service(gspServiceURL()).putDataset(dataset);
        ModelStore.service(gspServiceURL()).clearDataset();
        Dataset ds = ModelStore.service(gspServiceURL()).getDataset();
        assertTrue(ds.isEmpty());
    }

    @Test public void gsp_union_get() {
        String gn1 = "http://example/graph1";
        String gn2 = "http://example/graph2";
        ModelStore.service(gspServiceURL())
           .namedGraph(gn1)
           .PUT(model1);
        ModelStore.service(gspServiceURL())
           .namedGraph(gn2)
            .PUT(model2);
        // get union

        Model m = ModelStore.service(gspServiceURL()).namedGraph("union").GET();
        assertEquals(3, m.size());
    }

    @Test public void gsp_union_post() {
        expect400(()->{
            ModelStore.service(gspServiceURL()).namedGraph("union").POST(model1);
        });
    }

    // 404

    @Test public void gsp_404_put_delete_get() {
        String graphName = "http://example/graph2";
        ModelStore.service(gspServiceURL())
            .namedGraph(graphName)
            .PUT(graph);
        Model g = ModelStore.service(gspServiceURL())
            .namedGraph(graphName)
            .GET();
        assertFalse(g.isEmpty());
        ModelStore.service(gspServiceURL())
            .namedGraph(graphName)
            .DELETE();
        expect404(()->
            ModelStore.service(gspServiceURL())
                .namedGraph(graphName)
                .GET()
        );
    }

    @Test public void gsp_404_graph() {
        String graphName = "http://example/graph404";
        expect404(
            ()->ModelStore.service(gspServiceURL()).namedGraph(graphName).GET()
        );
    }

    @Test public void dsp_404_dataset() {
        expect404(
            ()->ModelStore.service(gspServiceURL()+"junk").getDataset()
        );
    }
}
