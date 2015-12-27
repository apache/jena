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

package org.apache.jena.fuseki ;

import static org.apache.jena.fuseki.ServerCtl.serviceGSP ;
import static org.apache.jena.fuseki.ServerCtl.serviceQuery ;
import static org.apache.jena.fuseki.ServerTest.* ;
import static org.apache.jena.fuseki.ServerTest.model1 ;
import static org.apache.jena.fuseki.ServerTest.model2 ;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException ;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection ;
import java.net.URL ;
import java.util.Iterator ;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.lib.NotImplemented;
import org.apache.jena.atlas.web.AcceptList ;
import org.apache.jena.atlas.web.MediaType;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.query.* ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.riot.web.HttpOp;
import org.apache.jena.sparql.core.Quad ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP ;
import org.apache.jena.sparql.resultset.ResultSetCompare ;
import org.apache.jena.sparql.sse.SSE ;
import org.apache.jena.sparql.util.Convert ;
import org.junit.Assert ;
import org.junit.Before ;
import org.junit.Test ;

public class TestQuery extends AbstractFusekiTest {
    
    @Before
    public void before() {
        DatasetAccessor du = DatasetAccessorFactory.createHTTP(serviceGSP()) ;
        du.putModel(model1) ;
        du.putModel(gn1, model2) ;
    }
    
    private static final AcceptList quadsOfferTest = DEF.quadsOffer ;
    private static final AcceptList rdfOfferTest   = DEF.rdfOffer ;    
    
    @Test
    public void query_01() {
        execQuery("SELECT * {?s ?p ?o}", 1) ;
    }

    @Test
    public void query_recursive_01() {
        String query = "SELECT * WHERE { SERVICE <" + serviceQuery() + "> { ?s ?p ?o . BIND(?o AS ?x) } }" ;
        try (QueryExecution qExec = QueryExecutionFactory.sparqlService(serviceQuery(), query)) {
            ResultSet rs = qExec.execSelect() ;
            Var x = Var.alloc("x") ;
            while (rs.hasNext()) {
                Binding b = rs.nextBinding() ;
                Assert.assertNotNull(b.get(x)) ;
            }
        }
    }

    @Test
    public void query_with_params_01() {
        String query = "ASK { }" ;
        try (QueryExecution qExec = QueryExecutionFactory.sparqlService(serviceQuery() + "?output=json", query)) {
            boolean result = qExec.execAsk() ;
            Assert.assertTrue(result) ;
        }
    }

    @Test
    public void request_id_header_01() throws IOException {
        String qs = Convert.encWWWForm("ASK{}") ;
        URL u = new URL(serviceQuery() + "?query=" + qs) ;
        HttpURLConnection conn = (HttpURLConnection)u.openConnection() ;
        Assert.assertTrue(conn.getHeaderField("Fuseki-Request-ID") != null) ;
    }

    @Test
    public void query_dynamic_dataset_01() {
        DatasetAccessor du = DatasetAccessorFactory.createHTTP(serviceGSP()) ;
        du.putModel(model1);
        du.putModel(gn1, model2);
        {
            String query = "SELECT * { ?s ?p ?o }" ;
            try (QueryExecution qExec = QueryExecutionFactory.sparqlService(serviceQuery() + "?output=json", query)) {
                ResultSet rs = qExec.execSelect() ;
                Node o = rs.next().getLiteral("o").asNode() ;
                Node n = SSE.parseNode("1") ;
                assertEquals(n, o) ;
            }
        }
        {
            String query = "SELECT * FROM <" + gn1 + "> { ?s ?p ?o }" ;
            try (QueryExecution qExec = QueryExecutionFactory.sparqlService(serviceQuery() + "?output=json", query)) {
                ResultSet rs = qExec.execSelect() ;
                Node o = rs.next().getLiteral("o").asNode() ;
                Node n = SSE.parseNode("2") ;
                assertEquals(n, o) ;
            }
        }
    }

    @Test
    public void query_dynamic_dataset_02() {
        DatasetAccessor du = DatasetAccessorFactory.createHTTP(serviceGSP()) ;
        du.putModel(model1);
        du.putModel(gn1, model1);
        du.putModel(gn2, model2);
        String query = "SELECT * FROM <"+gn1+"> FROM <"+gn2+"> { ?s ?p ?o }" ;
        try (QueryExecution qExec = QueryExecutionFactory.sparqlService(serviceQuery() + "?output=json", query)) {
            ResultSet rs = qExec.execSelect() ;
            int n = ResultSetFormatter.consume(rs) ;
            assertEquals(2, n) ;
        }
    }

    @Test
    public void query_construct_quad_01()
    {
        String queryString = " CONSTRUCT { GRAPH <http://eg/g> {?s ?p ?oq} } WHERE {?s ?p ?oq}" ;
        Query query = QueryFactory.create(queryString, Syntax.syntaxARQ);

        try ( QueryExecution qExec = QueryExecutionFactory.sparqlService(serviceQuery(), query) ) {
            Iterator<Quad> result = qExec.execConstructQuads();
            Assert.assertTrue(result.hasNext());
            Assert.assertEquals( "http://eg/g", result.next().getGraph().getURI());

        }
    }

    @Test
    public void query_construct_quad_02()
    {
        String queryString = " CONSTRUCT { GRAPH <http://eg/g> {?s ?p ?oq} } WHERE {?s ?p ?oq}" ;
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
        String query = " CONSTRUCT {?s ?p ?o} WHERE {?s ?p ?o}" ;
        try ( QueryExecution qExec = QueryExecutionFactory.sparqlService(serviceQuery(), query) ) {
            Iterator<Triple> result = qExec.execConstructTriples();
            Assert.assertTrue(result.hasNext());
        }
    }

    @Test
    public void query_construct_02()
    {
        String query = " CONSTRUCT {?s ?p ?o} WHERE {?s ?p ?o}" ;
        try ( QueryExecution qExec = QueryExecutionFactory.sparqlService(serviceQuery(), query) ) {
            Model result = qExec.execConstruct();
            assertEquals(1, result.size());
        }
    }

    @Test
    public void query_describe_01() {
        String query = "DESCRIBE ?s WHERE {?s ?p ?o}" ;
        try ( QueryExecution qExec = QueryExecutionFactory.sparqlService(serviceQuery(), query) ) {
            Model result = qExec.execDescribe();
            assertFalse(result.isEmpty()) ;
        }
    }

    @Test
    public void query_describe_02() {
        String query = "DESCRIBE <http://example/somethingelse> WHERE { }" ;
        try ( QueryExecution qExec = QueryExecutionFactory.sparqlService(serviceQuery(), query) ) {
            Model result = qExec.execDescribe();
            assertTrue(result.isEmpty()) ;
        }
    }

    // Conneg tests:
    // These use independent connection pooling.
    // Sharing pooling too much leads to lock up if the list is long (contentTypeTriXxml seems significant)
    // Hence: try (CloseableHttpClient client = HttpOp.createPoolingHttpClient()) { ... qExec.setClient(client); ... } 
    
    
    @Test
    public void query_construct_conneg() throws IOException {
        try (CloseableHttpClient client = HttpOp.createPoolingHttpClient()) {
            String query = " CONSTRUCT {?s ?p ?o} WHERE {?s ?p ?o}";
            for (MediaType type : rdfOfferTest.entries()) {

                String contentType = type.toHeaderString();
                try (QueryEngineHTTP qExec = (QueryEngineHTTP) QueryExecutionFactory.sparqlService(serviceQuery(),
                        query)) {
                    qExec.setModelContentType(contentType);
                    qExec.setClient(client);
                    Iterator<Triple> iter = qExec.execConstructTriples();
                    assertTrue(iter.hasNext());
                    String x = qExec.getHttpResponseContentType();
                    assertEquals(contentType, x);
                }
            }
        }
    }

    @Test
    public void query_construct_quad_conneg() throws IOException {
        try (CloseableHttpClient client = HttpOp.createPoolingHttpClient()) {
            String queryString = " CONSTRUCT { GRAPH ?g {?s ?p ?o} } WHERE { GRAPH ?g {?s ?p ?o}}";
            Query query = QueryFactory.create(queryString, Syntax.syntaxARQ);
            for (MediaType type : quadsOfferTest.entries()) {
                String contentType = type.toHeaderString();
                try (QueryEngineHTTP qExec = (QueryEngineHTTP) QueryExecutionFactory.sparqlService(serviceQuery(),
                        query)) {
                    qExec.setDatasetContentType(contentType);
                    qExec.setClient(client);
                    Iterator<Quad> iter = qExec.execConstructQuads();
                    assertTrue(iter.hasNext());
                    String x = qExec.getHttpResponseContentType();
                    assertEquals(contentType, x);
                }
            }
        }
    }

    @Test
    public void query_describe_conneg() throws IOException {
        try (CloseableHttpClient client = HttpOp.createPoolingHttpClient()) {
            String query = "DESCRIBE ?s WHERE {?s ?p ?o}";
            for (MediaType type : rdfOfferTest.entries()) {
                String contentType = type.toHeaderString();
                try (QueryEngineHTTP qExec = (QueryEngineHTTP) QueryExecutionFactory.sparqlService(serviceQuery(),
                        query)) {
                    qExec.setModelContentType(contentType);
                    qExec.setClient(client);
                    Model m = qExec.execDescribe();
                    String x = qExec.getHttpResponseContentType();
                    assertEquals(contentType, x);
                    assertFalse(m.isEmpty());
                }
            }
        }
    }

    @Test(expected = NotImplemented.class)
    public void query_json_01() throws IOException {
        Query query = QueryFactory.create("JSON { \"s\": ?s , \"p\": ?p , \"o\" : ?o } "
                + "WHERE { ?s ?p ?o }", Syntax.syntaxARQ);
        query.toString();
        try ( QueryExecution qExec = QueryExecutionFactory.sparqlService(serviceQuery(), query) ) {
            JsonArray result = qExec.execJson();
            assertEquals(1, result.size());
        }
    }

    @Test
    public void query_json_02() throws IOException {
        String qs = Convert.encWWWForm("JSON { \"s\": ?s , \"p\": ?p , \"o\" : ?o } "
                + "WHERE { ?s ?p ?o }") ;
        URL u = new URL(serviceQuery() + "?query=" + qs) ;
        HttpURLConnection conn = (HttpURLConnection)u.openConnection() ;
        String result = null;
        StringBuffer sb = new StringBuffer();
        InputStream is = null;
        try {
            is = new BufferedInputStream(conn.getInputStream());
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String inputLine = "";
            while ((inputLine = br.readLine()) != null) {
                sb.append(inputLine);
            }
            result = sb.toString();
        }
        finally {
            if (is != null) {
                is.close(); 
            }   
        }
        Assert.assertNotNull(result);
        Assert.assertTrue(result.contains("http://example/x"));
    }

    private static void execQuery(String queryString, int exceptedRowCount) {
        try ( QueryExecution qExec = QueryExecutionFactory.sparqlService(serviceQuery(), queryString) ) {
            ResultSet rs = qExec.execSelect() ;
            int x = ResultSetFormatter.consume(rs) ;
            assertEquals(exceptedRowCount, x) ;
        }
    }

    private static void execQuery(String queryString, ResultSet expectedResultSet) {
        try ( QueryExecution qExec = QueryExecutionFactory.sparqlService(serviceQuery(), queryString) ) {
            ResultSet rs = qExec.execSelect() ;
            boolean b = ResultSetCompare.equalsByTerm(rs, expectedResultSet) ;
            assertTrue("Result sets different", b) ;
        }
    }

}
