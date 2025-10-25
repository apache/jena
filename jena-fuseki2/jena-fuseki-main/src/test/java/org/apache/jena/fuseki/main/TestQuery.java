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

import static org.apache.jena.http.HttpLib.newGetRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Iterator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.web.AcceptList;
import org.apache.jena.atlas.web.MediaType;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.fuseki.DEF;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.http.HttpEnv;
import org.apache.jena.http.HttpLib;
import org.apache.jena.http.HttpOp;
import org.apache.jena.query.*;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.http.*;
import org.apache.jena.sparql.resultset.ResultsCompare;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.Convert;

public class TestQuery extends AbstractFusekiTest {

    private static final String  graphName1    = "http://graph/1";
    private static final String  graphName2    = "http://graph/2";

    private static final Node    gn1           = NodeFactory.createURI(graphName1);
    private static final Node    gn2           = NodeFactory.createURI(graphName2);
    private static final Graph   graph1        = SSE.parseGraph("(base <http://example/> (graph (<x> <p> 1)))");
    private static final Graph   graph2        = SSE.parseGraph("(base <http://example/> (graph (<x> <p> 2)))");

    @BeforeEach
    public void before() {
        GSP.service(serviceGSP()).defaultGraph().PUT(graph1);
        GSP.service(serviceGSP()).graphName(gn1).PUT(graph2);
    }

    private static final AcceptList quadsOfferTest = DEF.quadsOffer;
    private static final AcceptList rdfOfferTest   = DEF.rdfOffer;

    @Test
    public void query_01() {
        execQuery("SELECT * {?s ?p ?o}", 1);
    }

    @Test
    public void query_recursive_01() {
        String query = "SELECT * WHERE { SERVICE <" + serviceQuery() + "> { ?s ?p ?o . BIND(?o AS ?x) } }";

        // Set in server!
        Object serverSetting = Fuseki.getContext().get(Service.httpServiceAllowed);
        Fuseki.getContext().set(Service.httpServiceAllowed, true);

        try (QueryExecution qExec = QueryExecution.service(serviceQuery(), query)) {
            ResultSet rs = qExec.execSelect();
            Var x = Var.alloc("x");
            while (rs.hasNext()) {
                Binding b = rs.nextBinding();
                assertNotNull(b.get(x));
            }
        } finally {
            Fuseki.getContext().set(Service.httpServiceAllowed, serverSetting);
        }
    }

    @Test
    public void query_with_params_01() {
        String query = "ASK { }";
        try (QueryExecution qExec = QueryExecution.service(serviceQuery() + "?output=json", query)) {
            boolean result = qExec.execAsk();
            assertTrue(result);
        }
    }

    @Test
    public void request_id_header_01() throws IOException {
        String qs = Convert.encWWWForm("ASK{}");
        String url = serviceQuery() + "?query=" + qs;
        HttpRequest request = newGetRequest(url, null);
        HttpResponse<InputStream> response = HttpLib.execute(HttpEnv.getDftHttpClient(), request);
        try (InputStream body = response.body()) {
            assertTrue(response.headers().firstValue(Fuseki.FusekiRequestIdHeader) != null);
            HttpLib.finishInputStream(body);
        }
    }

    @Test
    public void query_dynamic_dataset_01() {
        {
            String query = "SELECT * { ?s ?p ?o }";
            try (QueryExecution qExec = QueryExecution.service(serviceQuery() + "?output=json", query)) {
                ResultSet rs = qExec.execSelect();
                Node o = rs.next().getLiteral("o").asNode();
                Node n = SSE.parseNode("1");
                assertEquals(n, o);
            }
        }
        {
            String query = "SELECT * FROM <" + graphName1 + "> { ?s ?p ?o }";
            try (QueryExecution qExec = QueryExecution.service(serviceQuery() + "?output=json", query)) {
                ResultSet rs = qExec.execSelect();
                Node o = rs.next().getLiteral("o").asNode();
                Node n = SSE.parseNode("2");
                assertEquals(n, o);
            }
        }
    }

    @Test
    public void query_dynamic_dataset_02() {
        GSP.service(serviceGSP()).graphName(gn1).PUT(graph1);
        GSP.service(serviceGSP()).graphName(gn2).PUT(graph2);
        String query = "SELECT * FROM <"+graphName1+"> FROM <"+graphName2+"> { ?s ?p ?o }";
        try (QueryExecution qExec = QueryExecution.service(serviceQuery() + "?output=json", query)) {
            ResultSet rs = qExec.execSelect();
            int n = ResultSetFormatter.consume(rs);
            assertEquals(2, n);
        }
    }

    @Test
    public void query_construct_quad_01()
    {
        String queryString = "CONSTRUCT { GRAPH <http://eg/g> {?s ?p ?oq} } WHERE {?s ?p ?oq}";
        Query query = QueryFactory.create(queryString, Syntax.syntaxARQ);

        try ( QueryExecutionHTTP qExec = QueryExecutionHTTP.service(serviceQuery(), query) ) {
            Iterator<Quad> result = qExec.execConstructQuads();
            assertTrue(result.hasNext());
            assertEquals( "http://eg/g", result.next().getGraph().getURI());

        }
    }

    @Test
    public void query_construct_quad_02()
    {
        String queryString = "CONSTRUCT { GRAPH <http://eg/g> {?s ?p ?oq} } WHERE {?s ?p ?oq}";
        Query query = QueryFactory.create(queryString, Syntax.syntaxARQ);

        try ( QueryExecution qExec = QueryExecution.service(serviceQuery(), query) ) {
            Dataset result = qExec.execConstructDataset();
            assertTrue(result.asDatasetGraph().find().hasNext());
            assertEquals( "http://eg/g", result.asDatasetGraph().find().next().getGraph().getURI());
        }
    }

    @Test
    public void query_construct_01()
    {
        String query = "CONSTRUCT {?s ?p ?o} WHERE {?s ?p ?o}";
        try ( QueryExecution qExec = QueryExecution.service(serviceQuery(), query) ) {
            Iterator<Triple> result = qExec.execConstructTriples();
            assertTrue(result.hasNext());
        }
    }

    @Test
    public void query_construct_02()
    {
        String query = "CONSTRUCT {?s ?p ?o} WHERE {?s ?p ?o}";
        try ( QueryExec qExec = QueryExec.service(serviceQuery()).query(query).build() ) {
            Graph result = qExec.construct();
            assertEquals(1, result.size());
        }
    }

    @Test
    public void query_describe_01() {
        String query = "DESCRIBE ?s WHERE {?s ?p ?o}";
        try ( QueryExec qExec = QueryExec.service(serviceQuery()).query(query).build() ) {
            Graph result = qExec.describe();
            assertFalse(result.isEmpty());
        }
    }

    @Test
    public void query_describe_02() {
        String query = "DESCRIBE <http://example/somethingelse> WHERE { }";
        try ( QueryExec qExec = QueryExec.service(serviceQuery()).query(query).build() ) {
            Graph result = qExec.describe();
            assertTrue(result.isEmpty());
        }
    }

    // Conneg tests:

    @Test
    public void query_construct_conneg() throws IOException {
        String query = "CONSTRUCT {?s ?p ?o} WHERE {?s ?p ?o}";
        for (MediaType type : rdfOfferTest.entries()) {
            // Includes text/plain - the old MIME type for N-triples

            String contentType = type.toHeaderString();
            try (QueryExecutionHTTP qExec =
                    QueryExecutionHTTPBuilder.create()
                    .endpoint(serviceQuery())
                    .queryString(query)
                    .acceptHeader(contentType)
                    .build() ) {
                Iterator<Triple> iter = qExec.execConstructTriples();
                assertTrue(iter.hasNext());
                String x = qExec.getHttpResponseContentType();
                x = removeHttpParameters(x);
                assertEquals(contentType, x);
            }
        }
    }

    /** Remove any parameters e.g. charset=, version= profile= etc*/
    private String removeHttpParameters(String x) {
        return x.replaceAll(";.*$", "");
    }

    @Test
    public void query_construct_quad_conneg() throws IOException {
        String queryString = "CONSTRUCT { GRAPH ?g {?s ?p ?o} } WHERE { GRAPH ?g {?s ?p ?o}}";
        Query query = QueryFactory.create(queryString, Syntax.syntaxARQ);
        for (MediaType type : quadsOfferTest.entries()) {
            String contentType = type.toHeaderString();
            try (QueryExecutionHTTP qExec =
                    QueryExecutionHTTPBuilder.create()
                    .endpoint(serviceQuery())
                    .query(query)
                    .acceptHeader(contentType)
                    .build() ) {
                Iterator<Quad> iter = qExec.execConstructQuads();
                assertTrue(iter.hasNext());
                String x = qExec.getHttpResponseContentType();
                assertEquals(contentType, x);
            }
        }
    }

    @Test
    public void query_describe_conneg() throws IOException {
        HttpClient client = HttpEnv.httpClientBuilder().build();
        String query = "DESCRIBE ?s WHERE {?s ?p ?o}";
        for (MediaType type : rdfOfferTest.entries()) {
            String contentType = type.toHeaderString();
            QueryExecHTTP qExec =
                    QueryExecHTTPBuilder.create()
                    .httpClient(client)
                    .endpoint(serviceQuery())
                    .queryString(query)
                    .acceptHeader(contentType)
                    .build();
            try ( qExec ) {
                Graph graph = qExec.describe();
                String x = qExec.getHttpResponseContentType();
                x = removeHttpParameters(x);
                assertEquals(contentType, x);
                assertFalse(graph.isEmpty());
            }
        }
    }

    public void query_json_01() throws IOException {
        Query query = QueryFactory.create("""
                JSON { "s": ?s , "p": ?p , "o" : ?o }
                WHERE { ?s ?p ?o }
                """,
                Syntax.syntaxARQ);
        try ( QueryExecution qExec = QueryExecution.service(serviceQuery(), query) ) {
            JsonArray result = qExec.execJson();
            assertEquals(1, result.size());
        }
    }

    @Test
    public void query_json_02() throws IOException {
        String qs = Convert.encWWWForm("""
                JSON { "s": ?s , "p": ?p , "o" : ?o }
                WHERE { ?s ?p ?o }""");
        String url = serviceQuery() + "?query=" + qs;
        String result = null;
        try ( TypedInputStream in = HttpOp.httpGet(url) ) {
            StringBuilder sb = new StringBuilder();
            try ( InputStream is = new BufferedInputStream(in);
                  BufferedReader br = new BufferedReader(new InputStreamReader(is)) ) {
                String inputLine = "";
                while ((inputLine = br.readLine()) != null) {
                    sb.append(inputLine);
                }
                result = sb.toString();
            }
        }
        assertNotNull(result);
        assertTrue(result.contains("http://example/x"));
    }

    private void execQuery(String queryString, int exceptedRowCount) {
        try ( QueryExecution qExec = QueryExecution.service(serviceQuery(), queryString) ) {
            ResultSet rs = qExec.execSelect();
            int x = ResultSetFormatter.consume(rs);
            assertEquals(exceptedRowCount, x);
        }
    }

    private void execQuery(String queryString, ResultSet expectedResultSet) {
        try ( QueryExecution qExec = QueryExecution.service(serviceQuery(), queryString) ) {
            ResultSet rs = qExec.execSelect();
            boolean b = ResultsCompare.equalsByTerm(rs, expectedResultSet);
            assertTrue(b, "Result sets different");
        }
    }

}
