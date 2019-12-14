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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.atlas.web.WebLib;
import org.apache.jena.graph.Graph;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.web.HttpNames;
import org.apache.jena.riot.web.HttpOp;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.sse.SSE;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/** Test HTTP level details */
public class TestHTTP {
    private static FusekiServer server = null;
    private static int port;

    private static Model data;
    private static Dataset dataset;

    private static String URL;

    @BeforeClass
    public static void beforeClass() {
        port = WebLib.choosePort();
        URL = "http://localhost:" + port + "/ds";
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
            .port(port)
            .build();
        server.start();
    }

    @AfterClass
    public static void afterClass() {
        if ( server != null )
            server.stop();
    }

    // GET

    @Test public void gspGet_dataset_1() {
        // Base URL, default content type => N-Quads (dump format)
        HttpOp.execHttpGet(URL, null, (base, response)->{
            String h = response.getFirstHeader(HttpNames.hContentType).getValue();
            assertNotNull(h);
            assertEquals(Lang.NQUADS.getHeaderString(), h);
        });
    }


    @Test public void gspGet_dataset_2() {
        String ct = Lang.TRIG.getHeaderString();
        HttpOp.execHttpGet(URL, ct, (base, response)->{
            String h = response.getFirstHeader(HttpNames.hContentType).getValue();
            assertNotNull(h);
            assertEquals(ct, h);
        });
    }

    @Test public void gspGet_graph_1() {
        String target = URL+"?default";
        HttpOp.execHttpGet(target, null, (base, response)->{
            String h = response.getFirstHeader(HttpNames.hContentType).getValue();
            assertNotNull(h);
            // "Traditional default".
            assertEquals(Lang.RDFXML.getHeaderString(), h);
        });
    }

    @Test public void gspGet_graph_2() {
        String target = URL+"?default";
        String ct = Lang.TTL.getHeaderString();
        HttpOp.execHttpGet(target, ct, (base, response)->{
            String h = response.getFirstHeader(HttpNames.hContentType).getValue();
            assertNotNull(h);
            assertEquals(ct, h);
        });
    }

    // HEAD

    @Test public void gspHead_dataset_1() {
        // Base URL, default content type => N-Quads (dump format)
        HttpOp.execHttpHead(URL, null, (base, response)->{
            String h = response.getFirstHeader(HttpNames.hContentType).getValue();
            assertNotNull(h);
            assertEquals(Lang.NQUADS.getHeaderString(), h);
        });
    }


    @Test public void gspHead_dataset_2() {
        String ct = Lang.TRIG.getHeaderString();
        HttpOp.execHttpHead(URL, ct, (base, response)->{
            String h = response.getFirstHeader(HttpNames.hContentType).getValue();
            assertNotNull(h);
            assertEquals(ct, h);
        });
    }

    @Test public void gspHead_graph_1() {
        String target = URL+"?default";
        HttpOp.execHttpHead(target, null, (base, response)->{
            String h = response.getFirstHeader(HttpNames.hContentType).getValue();
            assertNotNull(h);
            // "Traditional default".
            assertEquals(Lang.RDFXML.getHeaderString(), h);
        });
    }

    @Test public void gspHead_graph_2() {
        String target = URL+"?default";
        String ct = Lang.TTL.getHeaderString();
        HttpOp.execHttpHead(target, ct, (base, response)->{
            String h = response.getFirstHeader(HttpNames.hContentType).getValue();
            assertNotNull(h);
            assertEquals(ct, h);
        });
    }
}
