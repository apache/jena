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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
import org.apache.jena.test.conn.EnvTest;

// From TestModelStore - these tests are ones that occassionaly fail.
public class TestModelStore2 {

    static String DIR = "testing/RDFLink/";

    private static EnvTest env;
    @BeforeAll public static void beforeClass() {
        env = EnvTest.create("/ds");
    }

    @BeforeEach public void before() {
        env.clear();
    }

    @AfterAll public static void afterClass() {
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

    @Test public void gsp_dft_ct_1() {
        ModelStore.service(url("/ds")).defaultGraph().contentType(RDFFormat.RDFXML).PUT(DIR+"data-rdfxml");
    }

    @Test public void gsp_dft_ct_2() {
        ModelStore.service(url("/ds")).defaultGraph().contentTypeHeader(WebContent.contentTypeRDFXML).PUT(DIR+"data-rdfxml");
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
}
