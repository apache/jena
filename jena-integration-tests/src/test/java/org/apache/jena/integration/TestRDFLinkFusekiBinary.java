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

package org.apache.jena.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.http.HttpLib;
import org.apache.jena.rdflink.RDFLinkFuseki;
import org.apache.jena.rdflink.RDFLinkHTTPBuilder;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.sse.SSE;
import org.junit.Test;

/* Tests that blank nodes work over RDFLinkFuseki.
 */

public class TestRDFLinkFusekiBinary {
    private static Node n(String str) { return SSE.parseNode(str) ; }

    @Test public void rdflink_fuseki_1() {
        // Tests all run, in order, on one connection.
        Triple triple = SSE.parseTriple("(:s :p <_:b3456>)");
        Graph graph = GraphFactory.createDefaultGraph();
        graph.add(triple);

        FusekiServer server = createFusekiServer().build().start();
        int port = server.getPort();
        try {
            String dsURL = "http://localhost:"+port+"/ds" ;
            assertTrue(HttpLib.isFuseki(dsURL));

            RDFLinkHTTPBuilder builder = RDFLinkFuseki.newBuilder().destination(dsURL);

            try (RDFLinkFuseki link = (RDFLinkFuseki)builder.build()) {
                //assertTrue(isFuseki(link));
                // GSP
                link.put(graph);
                checkGraph(link, "b3456");

                // Query forms.
                link.querySelect("SELECT * {?s ?p ?o}", x-> {
                    Node obj = x.get(Var.alloc("o"));
                    assertTrue(obj.isBlank());
                    assertEquals("b3456", obj.getBlankNodeLabel());
                });

                try(QueryExec qExec = link.query("ASK {?s ?p <_:b3456>}")){
                    boolean bool = qExec.ask();
                    assertTrue(bool);
                }

                try (QueryExec qExec = link.query("CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o . FILTER (sameTerm(?o, <_:b3456>)) }")){
                    Graph graph2 = qExec.construct();
                    checkGraph(graph2, "b3456");
                }

                try(QueryExec qExec = link.query("DESCRIBE ?s WHERE { ?s ?p <_:b3456>}")){
                    Graph graph2 = qExec.describe();
                    checkGraph(graph2, "b3456");
                }

                // Update
                link.update("CLEAR DEFAULT" );
                link.update("INSERT DATA { <x:s> <x:p> <_:b789> }" );
                checkGraph(link, "b789");
                link.update("CLEAR DEFAULT" );
                link.update("INSERT DATA { <x:s> <x:p> <_:6789> }" );
                checkGraph(link, "6789");
            }
        } finally { server.stop(); }
    }

    private void checkGraph(RDFLinkFuseki link, String label) {
        Graph graph2 = link.get();
        checkGraph(graph2, label);
    }

    private void checkGraph(Graph graph2, String label) {
        assertEquals(1, graph2.size());
        Node n = graph2.find().next().getObject();
        assertTrue(n.isBlank());
        assertEquals(label, n.getBlankNodeLabel());
    }


    private static FusekiServer.Builder createFusekiServer() {
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        return
            FusekiServer.create().loopback(true)
                .port(0)
                //.setStaticFileBase("/home/afs/ASF/jena-fuseki-cmds/sparqler")
                .add("/ds", dsg)
                //.setVerbose(true)
                ;
    }
}
