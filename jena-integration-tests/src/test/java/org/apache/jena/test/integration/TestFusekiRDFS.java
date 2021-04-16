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

package org.apache.jena.test.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.graph.Graph;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.rdfs.RDFSFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.sse.SSE;
import org.junit.Test;

public class TestFusekiRDFS {

    // Test building a Fuseki server with an RDFS dataset.
    @Test public void fuseki_rdfs_1() {
        Graph schema = SSE.parseGraph("(graph (:p rdfs:range :T))");
        DatasetGraph dsg0 = DatasetGraphFactory.createTxnMem();
        DatasetGraph dsg = RDFSFactory.datasetRDFS(dsg0, schema);
        dsg.add(SSE.parseQuad("(_ :s :p :o)"));
        FusekiServer server = FusekiServer.create()
            .port(0)
            .add("/ds", dsg)
            .build();
        server.start();
        int port = server.getHttpPort();
        String URL = "http://localhost:"+port+"/ds";
        try ( RDFConnection conn = RDFConnectionFactory.connect(URL) ) {
            conn.queryResultSet("PREFIX :<http://example/> SELECT ?t { :o a ?t }",
                                rs->{
                                    assertTrue(rs.hasNext());
                                    String type = rs.next().getResource("t").getURI();
                                    assertFalse(rs.hasNext());
                                    assertEquals(type, "http://example/T");
                                });
        }
    }
}
