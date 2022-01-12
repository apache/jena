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

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.graph.Graph;
import org.apache.jena.http.HttpOp;
import org.apache.jena.http.HttpRDF;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.exec.http.GSP;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.web.FileSender;
import org.apache.jena.web.HttpSC;
import org.junit.Test;

/**
 * Tests for multi-part file upload.
 * Fuseki supports multifile upload for GSP.
 */
public class TestFileUpload  extends AbstractFusekiTest {

    @Test
    public void upload_gsp_01() {
        FileSender x = new FileSender(databaseURL() + "?default");
        x.add("D.ttl", "<http://example/s> <http://example/p> <http://example/o> .", "text/turtle");
        int sc = x.send("POST");
        assertEquals(HttpSC.OK_200, sc);

        // Ways to get the content, from highest to lowest level APIs.

        Graph graph1 = GSP.service(databaseURL()).acceptHeader("text/turtle").defaultGraph().GET();
        assertEquals(1, graph1.size());

        Graph graph2 = HttpRDF.httpGetGraph(databaseURL(), "text/turtle");
        assertEquals(1, graph2.size());

        Graph graph3 = GraphFactory.createDefaultGraph();
        StreamRDF stream = StreamRDFLib.graph(graph3);
        HttpRDF.httpGetToStream(databaseURL(), "text/turtle", stream);

        Graph graph4 = GraphFactory.createDefaultGraph();
        TypedInputStream in = HttpOp.httpGet(databaseURL(), "text/turtle");
        RDFDataMgr.read(graph4, in, Lang.TTL);

        assertEquals(1, graph4.size());
    }

    @Test
    public void upload_gsp_02() {
        FileSender x = new FileSender(databaseURL() + "?graph=http://example/g");
        x.add("D.ttl", "<http://example/s> <http://example/p> 123 .", "text/turtle");
        x.add("D.nt", "<http://example/s> <http://example/p> <http://example/o-456> .", "application/n-triples");
        int sc = x.send("PUT");
        assertEquals(HttpSC.CREATED_201, sc);

        Graph graph = GSP.service(databaseURL()).graphName("http://example/g").GET();
        assertEquals(2, graph.size());
    }

    // Extension of GSP - no graph selector => dataset
    @Test
    public void upload_gsp_03() {
        FileSender x = new FileSender(databaseURL());
        x.add("D.ttl", "<http://example/s> <http://example/p> <http://example/o> .", "text/turtle");
        x.add("D.trig", "<http://example/g> { <http://example/s> <http://example/p> 123,456 }", "text/trig");
        int sc = x.send("POST");
        assertEquals(HttpSC.OK_200, sc);

        DatasetGraph dsg = GSP.service(databaseURL()).getDataset();
        long c = Iter.count(dsg.find());
        assertEquals(3, c);
    }

    @Test
    public void upload_gsp_04() {
        FileSender x = new FileSender(databaseURL());
        x.add("D.ttl", "<http://example/s> <http://example/p> <http://example/o> .", "text/plain");
        x.add("D.trig", "<http://example/g> { <http://example/s> <http://example/p> 123,456 }", "text/plain");
        int sc = x.send("POST");
        assertEquals(HttpSC.OK_200, sc);

        DatasetGraph dsg = GSP.service(databaseURL()).getDataset();
        assertEquals(1, dsg.getDefaultGraph().size());
        assertEquals(2, dsg.getUnionGraph().size());
    }
}
