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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.http.HttpClient;
import java.util.Iterator;

import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.web.AcceptList;
import org.apache.jena.atlas.web.MediaType;
import org.apache.jena.fuseki.DEF;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.http.HttpEnv;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.http.GSP;
import org.apache.jena.sparql.exec.http.QueryExecutionHTTP;
import org.apache.jena.sparql.exec.http.QueryExecutionHTTPBuilder;
import org.apache.jena.sparql.resultset.ResultSetCompare;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.Convert;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestQuery extends AbstractFusekiTest {

    private static final String  graphName1    = "http://graph/1";
    private static final String  graphName2    = "http://graph/2";

    private static final Node    gn1           = NodeFactory.createURI(graphName1);
    private static final Node    gn2           = NodeFactory.createURI(graphName2);
    private static final Graph   graph1        = SSE.parseGraph("(base <http://example/> (graph (<x> <p> 1)))");
    private static final Graph   graph2        = SSE.parseGraph("(base <http://example/> (graph (<x> <p> 2)))");

    @Before
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
        try (QueryExecution qExec = QueryExecutionFactory.sparqlService(serviceQuery(), query)) {
            ResultSet rs = qExec.execSelect();
            Var x = Var.alloc("x");
            while (rs.hasNext()) {
                Binding b = rs.nextBinding();
                Assert.assertNotNull(b.get(x));
            }
        }
    }

    @Test
    public void query_with_params_01() {
        String query = "ASK { }";
        try (QueryExecution qExec = QueryExecutionFactory.sparqlService(serviceQuery() + "?output=json", query)) {
            boolean result = qExec.execAsk();
            Assert.assertTrue(result);
        }
    }

    @Test
    public void request_id_header_01() throws IOException {
        String qs = Convert.encWWWForm("ASK{}");
        URL u = new URL(serviceQuery() + "?query=" + qs);
        HttpURLConnection conn = (HttpURLConnection)u.openConnection();
        Assert.assertTrue(conn.getHeaderField(Fuseki.FusekiRequestIdHeader) != null);
    }

    @Test
    public void query_dynamic_dataset_01() {
        {
            String query = "SELECT * { ?s ?p ?o }";
            try (QueryExecution qExec = QueryExecutionFactory.sparqlService(serviceQuery() + "?output=json", query)) {
                ResultSet rs = qExec.execSelect();
                Node o = rs.next().getLiteral("o").asNode();
                Node n = SSE.parseNode("1");
                assertEquals(n, o);
            }
        }
        {
            String query = "SELECT * FROM <" + graphName1 + "> { ?s ?p ?o }";
            try (QueryExecution qExec = QueryExecutionFactory.sparqlService(serviceQuery() + "?output=json", query)) {
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
        try (QueryExecution qExec = QueryExecutionFactory.sparqlService(serviceQuery() + "?output=json", query)) {
            ResultSet rs = qExec.execSelect();
            int n = ResultSetFormatter.consume(rs);
            assertEquals(2, n);
        }
    }

    @Test
    public void query_construct_quad_01()
    {
        String queryString = " CONSTRUCT { GRAPH <http://eg/g> {?s ?p ?oq} } WHERE {?s ?p ?oq}";
        Query query = QueryFactory.create(queryString, Syntax.syntaxARQ);

        try ( QueryExecutionHTTP qExec = QueryExecutionFactory.sparqlService(serviceQuery(), query) ) {
            Iterator<Quad> result = qExec.execConstructQuads();
            Assert.assertTrue(result.hasNext());
            Assert.assertEquals( "http://eg/g", result.next().getGraph().getURI());

        }
    }

    @Test
    public void query_construct_quad_02()
    {
        String queryString = " CONSTRUCT { GRAPH <http://eg/g> {?s ?p ?oq} } WHERE {?s ?p ?oq}";
        Query query = QueryFactory.create(queryString, Syntax.syntaxARQ);

        try ( QueryExecution qExec = QueryExecutionFactory.sparqlService(serviceQuery(), query) ) {
            Dataset result = qExec.execConstructDataset();
            Assert.assertTrue(result.asDatasetGraph().find().hasNext());
            Assert.assertEquals( "http://eg/g", result.asDatasetGraph().find().next().getGraph().getURI());
        }
    }

    @Test
    public void query_construct_01()
    {
        String query = " CONSTRUCT {?s ?p ?o} WHERE {?s ?p ?o}";
        try ( QueryExecution qExec = QueryExecutionFactory.sparqlService(serviceQuery(), query) ) {
            Iterator<Triple> result = qExec.execConstructTriples();
            Assert.assertTrue(result.hasNext());
        }
    }

    @Test
    public void query_construct_02()
    {
        String query = " CONSTRUCT {?s ?p ?o} WHERE {?s ?p ?o}";
        try ( QueryExecution qExec = QueryExecutionFactory.sparqlService(serviceQuery(), query) ) {
            Model result = qExec.execConstruct();
            assertEquals(1, result.size());
        }
    }

    @Test
    public void query_describe_01() {
        String query = "DESCRIBE ?s WHERE {?s ?p ?o}";
        try ( QueryExecution qExec = QueryExecutionFactory.sparqlService(serviceQuery(), query) ) {
            Model result = qExec.execDescribe();
            assertFalse(result.isEmpty());
        }
    }

    @Test
    public void query_describe_02() {
        String query = "DESCRIBE <http://example/somethingelse> WHERE { }";
        try ( QueryExecution qExec = QueryExecutionFactory.sparqlService(serviceQuery(), query) ) {
            Model result = qExec.execDescribe();
            assertTrue(result.isEmpty());
        }
    }

    // Conneg tests:

    @Test
    public void query_construct_conneg() throws IOException {
        String query = " CONSTRUCT {?s ?p ?o} WHERE {?s ?p ?o}";
        for (MediaType type : rdfOfferTest.entries()) {

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
                assertEquals(contentType, x);
            }
        }
    }

    @Test
    public void query_construct_quad_conneg() throws IOException {
        String queryString = " CONSTRUCT { GRAPH ?g {?s ?p ?o} } WHERE { GRAPH ?g {?s ?p ?o}}";
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
            QueryExecutionHTTP qExec =
                    QueryExecutionHTTPBuilder.create()
                    .httpClient(client)
                    .endpoint(serviceQuery())
                    .queryString(query)
                    .acceptHeader(contentType)
                    .build();

            try ( qExec ) {
                Model m = qExec.execDescribe();
                String x = qExec.getHttpResponseContentType();
                assertEquals(contentType, x);
                assertFalse(m.isEmpty());
            }
        }
    }

    public void query_json_01() throws IOException {
        Query query = QueryFactory.create("JSON { \"s\": ?s , \"p\": ?p , \"o\" : ?o } "
                + "WHERE { ?s ?p ?o }", Syntax.syntaxARQ);
        try ( QueryExecution qExec = QueryExecutionFactory.sparqlService(serviceQuery(), query) ) {
            JsonArray result = qExec.execJson();
            assertEquals(1, result.size());
        }
    }

    @Test
    public void query_json_02() throws IOException {
        String qs = Convert.encWWWForm("JSON { \"s\": ?s , \"p\": ?p , \"o\" : ?o } "
                + "WHERE { ?s ?p ?o }");
        URL u = new URL(serviceQuery() + "?query=" + qs);
        HttpURLConnection conn = (HttpURLConnection)u.openConnection();
        String result = null;
        StringBuffer sb = new StringBuffer();
        try ( InputStream is = new BufferedInputStream(conn.getInputStream());
              BufferedReader br = new BufferedReader(new InputStreamReader(is)) ) {
            String inputLine = "";
            while ((inputLine = br.readLine()) != null) {
                sb.append(inputLine);
            }
            result = sb.toString();
        }
        Assert.assertNotNull(result);
        Assert.assertTrue(result.contains("http://example/x"));
    }

    private static void execQuery(String queryString, int exceptedRowCount) {
        try ( QueryExecution qExec = QueryExecutionFactory.sparqlService(serviceQuery(), queryString) ) {
            ResultSet rs = qExec.execSelect();
            int x = ResultSetFormatter.consume(rs);
            assertEquals(exceptedRowCount, x);
        }
    }

    private static void execQuery(String queryString, ResultSet expectedResultSet) {
        try ( QueryExecution qExec = QueryExecutionFactory.sparqlService(serviceQuery(), queryString) ) {
            ResultSet rs = qExec.execSelect();
            boolean b = ResultSetCompare.equalsByTerm(rs, expectedResultSet);
            assertTrue("Result sets different", b);
        }
    }

}
