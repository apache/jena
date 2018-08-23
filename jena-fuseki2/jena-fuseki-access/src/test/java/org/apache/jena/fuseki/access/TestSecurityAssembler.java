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

package org.apache.jena.fuseki.access;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.fuseki.FusekiLib;
import org.apache.jena.fuseki.embedded.FusekiServer;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.assembler.AssemblerUtils;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.system.Txn;
import org.junit.Test;

/**
 * Test on the assembler for data access control.
 * <ul>
 * <li>assem-security.ttl - two services "/database" and "/plain" each with their own dataset. 
 * <li>assem-security-shared.ttl - two services "/database" and "/plain" with a shared dataset.
 * </ul>
 */
public class TestSecurityAssembler {
    static final String DIR = "testing/Access/";
    
    // Check the main test inputs. 
    @Test public void assembler1() { 
        Dataset ds = (Dataset)AssemblerUtils.build(DIR+"assem-security.ttl", VocabSecurity.tAccessControlledDataset);
    }
    
    @Test public void assembler2() { 
        Dataset ds = (Dataset)AssemblerUtils.build(DIR+"assem-security-shared.ttl", VocabSecurity.tAccessControlledDataset);
        SecurityRegistry securityRegistry = ds.getContext().get(DataAccessCtl.symSecurityRegistry);
    }
    
    private static FusekiServer setup(String assembler, AtomicReference<String> user) {
        int port = FusekiLib.choosePort();
        FusekiServer server = DataAccessCtl.fusekiBuilder((a)->user.get())
            .port(port)
            .parseConfigFile(assembler)
            .build();
                
        return server;
    }
    
    // Two separate datasets  
    @Test public void assembler3() {
        AtomicReference<String> user = new AtomicReference<>();
        FusekiServer server = setup(DIR+"assem-security.ttl", user);
        // Add data directly to the datasets.
        DatasetGraph dsg = server.getDataAccessPointRegistry().get("/database").getDataService().getDataset();
        //System.out.println(dsg.getContext());
        Txn.executeWrite(dsg,  ()->{
            dsg.add(SSE.parseQuad("(<http://host/graphname1> :s1 :p :o)"));
            dsg.add(SSE.parseQuad("(<http://host/graphname3> :s3 :p :o)"));
            dsg.add(SSE.parseQuad("(<http://host/graphname9> :s9 :p :o)"));
        });
        server.start();
        try {
            testAssembler(server.getPort(), user);

            // Access the uncontrolled dataset.
            user.set(null);
            String plainUrl = "http://localhost:"+server.getPort()+"/plain";
            try(RDFConnection conn = RDFConnectionFactory.connect(plainUrl)) {
                conn.update("INSERT DATA { <x:s> <x:p> 123 , 456 }");
                conn.queryResultSet("SELECT * { ?s ?p ?o }",
                    rs->{
                        int x = ResultSetFormatter.consume(rs);
                        assertEquals(2, x);
                    });
            }
        } finally { server.stop(); }
    }
    
    // Shared dataset
    @Test public void assembler4() {
        AtomicReference<String> user = new AtomicReference<>();
        FusekiServer server = setup(DIR+"assem-security-shared.ttl", user);
    
        String x = StrUtils.strjoinNL
            ("PREFIX : <http://example/>"
            ,"INSERT DATA {"
            ,"   GRAPH <http://host/graphname1> {:s1 :p :o}"
            ,"   GRAPH <http://host/graphname3> {:s3 :p :o}"
            ,"   GRAPH <http://host/graphname9> {:s9 :p :o}"
            ,"}"
            );
        
        server.start();
        try {
            user.set(null);
            String plainUrl = "http://localhost:"+server.getPort()+"/plain";
            try(RDFConnection conn = RDFConnectionFactory.connect(plainUrl)) {
                conn.update(x);
                conn.queryResultSet("SELECT * { GRAPH ?g { ?s ?p ?o } }",
                    rs->{
                        int c = ResultSetFormatter.consume(rs);
                        assertEquals(3, c);
                    });
            }
            testAssembler(server.getPort(), user);
        } finally { server.stop(); }
    }

    private void testAssembler(int port, AtomicReference<String> user) {
        // The access controlled dataset.
        String url = "http://localhost:"+port+"/database";

        Node s1 = SSE.parseNode(":s1"); 
        Node s3 = SSE.parseNode(":s3");
        Node s9 = SSE.parseNode(":s9"); 

        user.set("user1");
        try(RDFConnection conn = RDFConnectionFactory.connect(url)) {
            Set<Node> visible = query(conn, "SELECT * { GRAPH ?g { ?s ?p ?o }}");
            assertSeen(visible, s1, s3);
        }

        user.set("userX"); // No such user in the registry
        try(RDFConnection conn = RDFConnectionFactory.connect(url)) {
            Set<Node> visible = query(conn, "SELECT * { GRAPH ?g { ?s ?p ?o }}");
            assertSeen(visible);
        }
        user.set(null); // No user.
        try(RDFConnection conn = RDFConnectionFactory.connect(url)) {
            Set<Node> visible = query(conn, "SELECT * { GRAPH ?g { ?s ?p ?o }}");
            assertSeen(visible);
        }

        user.set("user2");
        try(RDFConnection conn = RDFConnectionFactory.connect(url)) {
            Set<Node> visible = query(conn, "SELECT * { GRAPH ?g { ?s ?p ?o }}");
            assertSeen(visible, s9);
        }

        user.set("userZ"); // No graphs with data.
        try(RDFConnection conn = RDFConnectionFactory.connect(url)) {
            Set<Node> visible = query(conn, "SELECT * { GRAPH ?g { ?s ?p ?o }}");
            assertSeen(visible);
        }

    }

    private static void assertSeen(Set<Node> visible, Node ... expected) {
        Set<Node> expectedNodes = new HashSet<>(Arrays.asList(expected));
        assertEquals(expectedNodes, visible);
    }
    
    private Set<Node> query(RDFConnection conn, String queryString) {
        Set<Node> results = new HashSet<>();
        conn.queryResultSet(queryString, rs->{
            List<QuerySolution> list = Iter.toList(rs);
            list.stream()
            .map(qs->qs.get("s"))
            .filter(Objects::nonNull)
            .map(RDFNode::asNode)
            .forEach(n->results.add(n));
        });
        return results;
    }
}
