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

package org.apache.jena.test.rdfconnection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.jena.atlas.web.WebLib;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.sse.SSE;
import org.junit.Test;

/* Tests that blanknodes work over RDFConnectionFuseki
 * This consists of testing each of the necessary components,
 * and then a test of a connection itself.
 */

public class TestRDFConnectionFusekiBinary {
    private static Node n(String str) { return SSE.parseNode(str) ; }

    @Test public void rdfconnection_fuseki_1() {
        // Tests all run, in order, on one connection.
        Triple triple = SSE.parseTriple("(:s :p <_:b3456>)");
        // Goes in as URI! (pre this PR)
        Model model = ModelFactory.createDefaultModel();
        model.getGraph().add(triple);

        int PORT = WebLib.choosePort();
        FusekiServer server = createFusekiServer(PORT).build().start();
        try {
            String dsURL = "http://localhost:"+PORT+"/ds" ;
            assertTrue(Fuseki.isFuseki(dsURL));

            RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination(dsURL);

            try (RDFConnectionFuseki conn = (RDFConnectionFuseki)builder.build()) {
                assertTrue(Fuseki.isFuseki(dsURL));
                // GSP
                conn.put(model);
                checkModel(conn, "b3456");

                // Query forms.
                conn.querySelect("SELECT * {?s ?p ?o}", x-> {
                    Node obj = x.get("o").asNode();
                    assertTrue(obj.isBlank());
                    assertEquals("b3456", obj.getBlankNodeLabel());
                });

                try(QueryExecution qExec = conn.query("ASK {?s ?p <_:b3456>}")){
                    boolean bool = qExec.execAsk();
                    assertTrue(bool);
                }

                try (QueryExecution qExec = conn.query("CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o . FILTER (sameTerm(?o, <_:b3456>)) }")){
                    Model model2 = qExec.execConstruct() ;
                    checkModel(model2, "b3456");
                }

                try(QueryExecution qExec = conn.query("DESCRIBE ?s WHERE { ?s ?p <_:b3456>}")){
                    Model model2 = qExec.execConstruct() ;
                    checkModel(model2, "b3456");
                }

                // Update
                conn.update("CLEAR DEFAULT" );
                conn.update("INSERT DATA { <x:s> <x:p> <_:b789> }" );
                checkModel(conn, "b789");
                conn.update("CLEAR DEFAULT" );
                conn.update("INSERT DATA { <x:s> <x:p> <_:6789> }" );
                checkModel(conn, "6789");
            }
        } finally { server.stop(); }
    }

    private void checkModel(RDFConnectionFuseki conn, String label) {
        Model model2 = conn.fetch();
        checkModel(model2, label);
    }

    private void checkModel(Model model2, String label) {
        assertEquals(1, model2.size());
        Node n = model2.listStatements().next().getObject().asNode();
        assertTrue(n.isBlank());
        assertEquals(label, n.getBlankNodeLabel());
    }


    private static FusekiServer.Builder createFusekiServer(int PORT) {
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        return
            FusekiServer.create().loopback(true)
                .port(PORT)
                //.setStaticFileBase("/home/afs/ASF/jena-fuseki-cmds/sparqler")
                .add("/ds", dsg)
                //.setVerbose(true)
                ;
    }
}
