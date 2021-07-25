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

package org.apache.jena.fuseki;

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
import org.junit.Test;

/**
 * Tests for multi-part file upload.
 */
public class TestWebappFileUpload extends AbstractFusekiWebappTest {
    @Test
    public void upload_gsp_01() {
        FileSender x = new FileSender(ServerCtl.serviceGSP() + "?default");
        x.add("D.ttl", "<http://example/s> <http://example/p> <http://example/o> .", "text/turtle");
        x.send("POST");

        // Ways to get the content, from highest to lowest level APIs.

        Graph graph1 = GSP.service(ServerCtl.serviceGSP()).acceptHeader("text/turtle").defaultGraph().GET();
        assertEquals(1, graph1.size());

        Graph graph2 = HttpRDF.httpGetGraph(ServerCtl.serviceGSP(), "text/turtle");
        assertEquals(1, graph2.size());

        Graph graph3 = GraphFactory.createDefaultGraph();
        StreamRDF stream = StreamRDFLib.graph(graph3);
        HttpRDF.httpGetToStream(ServerCtl.serviceGSP(), "text/turtle", stream);

        Graph graph4 = GraphFactory.createDefaultGraph();
        TypedInputStream in = HttpOp.httpGet(ServerCtl.serviceGSP(), "text/turtle");
        RDFDataMgr.read(graph4, in, Lang.TTL);

        assertEquals(1, graph4.size());
    }

    @Test
    public void upload_gsp_02() {
        FileSender x = new FileSender(ServerCtl.serviceGSP() + "?default");
        x.add("D.ttl", "<http://example/s> <http://example/p> 123 .", "text/turtle");
        x.add("D.nt", "<http://example/s> <http://example/p> <http://example/o-456> .", "application/n-triples");
        x.send("PUT");

        Graph graph = GSP.service(ServerCtl.serviceGSP()).defaultGraph().GET();
        assertEquals(2, graph.size());
    }

    // Extension of GSP - no graph selector => dataset
    @Test
    public void upload_gsp_03() {
        FileSender x = new FileSender(ServerCtl.serviceGSP());
        x.add("D.ttl", "<http://example/s> <http://example/p> <http://example/o> .", "text/turtle");
        x.add("D.trig", "<http://example/g> { <http://example/s> <http://example/p> 123,456 }", "text/trig");
        x.send("POST");

        DatasetGraph dsg = GSP.service(ServerCtl.serviceGSP()).getDataset();
        long c = Iter.count(dsg.find());
        assertEquals(3, c);
    }

    @Test
    public void upload_gsp_04() {
        FileSender x = new FileSender(ServerCtl.urlDataset());
        x.add("D.ttl", "<http://example/s> <http://example/p> <http://example/o> .", "text/plain");
        x.add("D.trig", "<http://example/g> { <http://example/s> <http://example/p> 123,456 }", "text/plain");
        x.send("POST");

        DatasetGraph dsg = GSP.service(ServerCtl.serviceGSP()).getDataset();
        assertEquals(1, dsg.getDefaultGraph().size());
        assertEquals(2, dsg.getUnionGraph().size());
    }
}
