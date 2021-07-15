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

import static org.apache.jena.sparql.sse.SSE.parseQuad;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.exec.RowSet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for {@link QueryExecHTTP} with no authentication.
 * See also {@link TestQueryExecCleanServer}.
 */
public class TestQueryExecHTTP {
    private static FusekiServer server = null;
    private static String URL;
    private static String dsName = "/ds";
    private static String dsURL;
    private static Quad q0 = parseQuad("(_ :s :p :o)");
    private static Quad q1 = parseQuad("(:g1 :s :p 1)");
    private static Quad q2 = parseQuad("(:g2 :s :p 2)");

    static {
        if ( false )
            LogCtl.enable(Fuseki.actionLog);
        }

    @BeforeClass public static void beforeClass() {
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        dsg.add(q0);
        dsg.add(q1);
        dsg.add(q2);
        server = FusekiServer.create()
            .port(0)
            .verbose(true)
            .add(dsName, dsg)
            .build();
        server.start();
        int port = server.getPort();
        URL = "http://localhost:"+port+"/";
        dsURL = "http://localhost:"+port+dsName;
    }

    @AfterClass public static void afterClass() {
        server.stop();
    }

    private static String serviceQuery() { return dsURL; }

    @Test
    public void query_select_01() {
        try ( QueryExecHTTP qExec = QueryExecHTTP.newBuilder()
                    .service(dsURL).queryString("SELECT * { ?s ?p ?o }").build() ) {
            RowSet rs = qExec.select();
            assertTrue(rs.hasNext());
            rs.next();
            assertFalse(rs.hasNext());
            assertTrue(qExec.getHttpResponseContentType().startsWith("application/sparql-results+json"));
        }
    }

    @Test
    public void query_select_post_form_1() {
        try ( QueryExecHTTP qExec = QueryExecHTTP.newBuilder().sendMode(QuerySendMode.asPostForm)
                    .service(dsURL).queryString("SELECT * { ?s ?p ?o }").build() ) {
            RowSet rs = qExec.select();
            assertTrue(rs.hasNext());
            rs.next();
            assertFalse(rs.hasNext());
            assertTrue(qExec.getHttpResponseContentType().startsWith("application/sparql-results+json"));
        }
    }

    @Test
    public void query_select_post_body() {
        try ( QueryExecHTTP qExec = QueryExecHTTP.newBuilder().postQuery()
                    .service(dsURL).queryString("SELECT * { ?s ?p ?o }").build() ) {
            RowSet rs = qExec.select();
            assertTrue(rs.hasNext());
            rs.next();
            assertFalse(rs.hasNext());
            assertTrue(qExec.getHttpResponseContentType().startsWith("application/sparql-results+json"));
        }
    }

    @Test
    public void query_select_accept_1() {
        // Explicitly set the Accept header
        try ( QueryExecHTTP qExec = QueryExecHTTP.newBuilder()
                    .service(dsURL)
                    .queryString("SELECT * { ?s ?p ?o }")
                    .acceptHeader("application/sparql-results+xml")
                    .build() ) {
            RowSet rs = qExec.select();
            assertTrue(rs.hasNext());
            rs.next();
            assertFalse(rs.hasNext());
            assertEquals("application/sparql-results+xml", qExec.getHttpResponseContentType());
        }
    }

    // Not supported because of the interactions of "chunked" (for streaming) and "gzip".
//    @Test
//    public void query_select_compress_1() {
//        try ( QueryExecHTTP qExec = QueryExecHTTP.newBuilder()
//                    .service(dsURL)
//                    .allowCompression(true)
//                    .queryString("SELECT * { ?s ?p ?o }")
//                    .acceptHeader("application/sparql-results+xml")
//                    .build() ) {
//            RowSet rs = qExec.select();
//            assertTrue(rs.hasNext());
//            rs.next();
//            assertFalse(rs.hasNext());
//            assertEquals("application/sparql-results+xml", qExec.getHttpResponseContentType());
//        }
//    }

    // ASK, as Query

    @Test
    public void query_ask_01() {
        Query query = QueryFactory.create("ASK{}");
        try ( QueryExecHTTP qExec = QueryExecHTTP.newBuilder()
                    .service(dsURL).query(query).build() ) {
            boolean result = qExec.ask();
        }
    }

    // CONSTRUCT

    @Test
    public void query_construct_01()
    {
        String queryString = "CONSTRUCT { ?s ?p ?o } WHERE { GRAPH <"+Quad.unionGraph.getURI()+"> { ?s ?p ?o } }";
        // Check syntax
        Query query = QueryFactory.create(queryString, Syntax.syntaxARQ);

        try ( QueryExecHTTP qExec = QueryExecHTTP.newBuilder()
            .service(dsURL)
            .queryString(queryString)
            .build() ) {
            Graph graph = qExec.construct();
            assertEquals(2, graph.size());
        }
    }

    @Test
    public void query_construct_02()
    {
        String queryString = "CONSTRUCT { ?s ?p ?o } WHERE { GRAPH <"+Quad.unionGraph.getURI()+"> { ?s ?p ?o } }";
        // Check syntax
        Query query = QueryFactory.create(queryString, Syntax.syntaxARQ);

        try ( QueryExecHTTP qExec = QueryExecHTTP.newBuilder()
            .service(dsURL)
            .queryString(queryString)
            .build() ) {
            Iterator<Triple> iter = qExec.constructTriples();
            assertEquals(2, Iter.count(iter));
        }
    }

    @Test
    public void query_construct_quad_01()
    {
        String queryString = "CONSTRUCT WHERE { GRAPH ?g { ?s ?p ?o } }";
        // Check syntax
        Query query = QueryFactory.create(queryString, Syntax.syntaxARQ);

        try ( QueryExecHTTP qExec = QueryExecHTTP.newBuilder()
                .service(dsURL).queryString(queryString).build() ) {
            DatasetGraph dataset = qExec.constructDataset();
            assertEquals(2, Iter.count(dataset.find()));
        }
    }

    @Test
    public void query_construct_quad_02()
    {
        String queryString = "CONSTRUCT WHERE { GRAPH ?g { ?s ?p ?o } }";
        // Check syntax
        Query query = QueryFactory.create(queryString, Syntax.syntaxARQ);

        try ( QueryExecHTTP qExec = QueryExecHTTP.newBuilder()
                .service(dsURL).queryString(queryString).build() ) {
            Iterator<Quad> iter= qExec.constructQuads();
            assertEquals(2, Iter.count(iter));
        }
    }

    // DESCRIBE

    @Test
    public void query_describe_1()
    {
        String queryString = "DESCRIBE <http://example/s> { }";
        // Check syntax
        Query query = QueryFactory.create(queryString, Syntax.syntaxARQ);
        try ( QueryExecHTTP qExec = QueryExecHTTP.newBuilder()
                .service(dsURL).queryString(queryString).build() ) {
            Graph graph = qExec.describe();
            assertEquals(3, graph.size());
        }
    }

    @Test
    public void query_describe_2()
    {
        String queryString = "DESCRIBE <http://example/s> { }";
        // Check syntax
        Query query = QueryFactory.create(queryString, Syntax.syntaxARQ);
        try ( QueryExecHTTP qExec = QueryExecHTTP.newBuilder()
                .service(dsURL).queryString(queryString).build() ) {
            Iterator<Triple> iter = qExec.describeTriples();
            assertEquals(3, Iter.count(iter));
        }
    }

    // JSON

    @Test
    public void query_json_1() {
        String queryString = "JSON { \"s\": ?s , \"p\": ?p , \"o\" : ?o } " + "WHERE { ?s ?p ?o }";
        // Check syntax
        Query query = QueryFactory.create(queryString, Syntax.syntaxARQ);

        try ( QueryExecHTTP qExec = QueryExecHTTP.newBuilder()
                .service(dsURL).queryString(queryString).build() ) {
            JsonArray jsonArray = qExec.execJson();
            assertEquals(1,jsonArray.size());
        }
    }

    @Test
    public void query_graph_uri_1() {
        String queryString = "SELECT * { ?s ?p ?o }";
        try ( QueryExecHTTP qExec = QueryExecHTTP.newBuilder()
                    .service(dsURL)
                    .queryString(queryString)
                    .addDefaultGraphURI("http://example/g1")
                    .build() ) {
            long x = Iter.count(qExec.select());
            assertEquals(1, x);
        }
    }

    @Test
    public void query_graph_uri_2() {
        String queryString = "SELECT * { ?s ?p ?o }";
        try ( QueryExecHTTP qExec = QueryExecHTTP.newBuilder()
                    .service(dsURL)
                    .queryString(queryString)
                    .addDefaultGraphURI("http://example/g1")
                    .addDefaultGraphURI("http://example/g2")
                    .build() ) {
            long x = Iter.count(qExec.select());
            assertEquals(2, x);
        }
    }


    @Test
    public void query_graph_uri_3() {
        String queryString = "SELECT * { ?s ?p ?o }";
        try ( QueryExecHTTP qExec = QueryExecHTTP.newBuilder()
                    .service(dsURL)
                    .queryString(queryString)
                    .addNamedGraphURI("http://example/g1")
                    .build() ) {
            long x = Iter.count(qExec.select());
            assertEquals(0, x);
        }
    }

    @Test
    public void query_graph_uri_4() {
        String queryString = "SELECT * { GRAPH <urn:x-arq:UnionGraph> { ?s ?p ?o } }";
        try ( QueryExecHTTP qExec = QueryExecHTTP.newBuilder()
                    .service(dsURL)
                    .queryString(queryString)
                    .addNamedGraphURI("http://example/g1")
                    .build() ) {
            long x = Iter.count(qExec.select());
            assertEquals(1, x);
        }
    }

    @Test
    public void query_graph_uri_5() {
        String queryString = "SELECT * { GRAPH <urn:x-arq:UnionGraph> { ?s ?p ?o } }";
        try ( QueryExecHTTP qExec = QueryExecHTTP.newBuilder()
                    .service(dsURL)
                    .queryString(queryString)
                    .addNamedGraphURI("http://example/g2")
                    .addNamedGraphURI("http://example/g1")
                    .build() ) {
            long x = Iter.count(qExec.select());
            assertEquals(2, x);
        }
    }
}
