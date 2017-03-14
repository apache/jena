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

import static org.apache.jena.fuseki.ServerCtl.serviceQuery ;
import static org.apache.jena.fuseki.ServerTestData.gn1 ;
import static org.apache.jena.fuseki.ServerTestData.model1 ;
import static org.apache.jena.fuseki.ServerTestData.model2 ;

import java.io.IOException ;
import java.net.HttpURLConnection ;
import java.net.URL ;

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.query.* ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.util.Convert ;
import org.junit.* ;
import org.junit.Test ;

public class TestQuery extends BaseTest 
{
    @BeforeClass public static void ctlBeforeClass() { ServerCtl.ctlBeforeClass(); }
    @AfterClass  public static void ctlAfterClass()  { ServerCtl.ctlAfterClass(); }
    @Before      public void ctlBeforeTest()         { ServerCtl.ctlBeforeTest(); }
    @After       public void ctlAfterTest()          { ServerCtl.ctlAfterTest(); }
    
    @Before public void beforeClass() {
        DatasetAccessor du = DatasetAccessorFactory.createHTTP(ServerCtl.serviceGSP()) ;
        du.putModel(model1) ;
        du.putModel(gn1, model2) ;
    }
    
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

    private void execQuery(String queryString, int exceptedRowCount) {
        QueryExecution qExec = QueryExecutionFactory.sparqlService(serviceQuery(), queryString) ;
        ResultSet rs = qExec.execSelect() ;
        int x = ResultSetFormatter.consume(rs) ;
        assertEquals(exceptedRowCount, x) ;
    }
}
