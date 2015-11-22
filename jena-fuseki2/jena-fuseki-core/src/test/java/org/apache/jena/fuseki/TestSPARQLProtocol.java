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

import static org.apache.jena.fuseki.ServerTest.*;
import org.apache.jena.atlas.junit.BaseTest;
import org.apache.jena.atlas.lib.Cache;
import org.apache.jena.fuseki.cache.CacheEntry;
import org.apache.jena.fuseki.servlets.SPARQL_Query_Cache;
import org.apache.jena.query.*;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.apache.jena.sparql.util.Convert;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Iterator;

public class TestSPARQLProtocol extends BaseTest
{
    @BeforeClass
    public static void beforeClass() {
        ServerTest.allocServer();
        // Load some data.
        DatasetAccessor du = DatasetAccessorFactory.createHTTP(serviceGSP);
        du.putModel(model1);
        du.putModel(gn1, model2);
    }

    @AfterClass
    public static void afterClass() {
        ServerTest.resetServer();
        ServerTest.freeServer();
    }

    static String query(String base, String queryString) {
        return base + "?query=" + Convert.encWWWForm(queryString);
    }

    @Test
    public void query_01() {
        Query query = QueryFactory.create("SELECT * { ?s ?p ?o }");
        QueryExecution qexec = QueryExecutionFactory.sparqlService(serviceQuery, query);
        ResultSet rs = qexec.execSelect();
        int x = ResultSetFormatter.consume(rs);
        assertTrue(x != 0);
    }

    @Test
    public void cache_query_01() {
        Query query = QueryFactory.create("SELECT * { ?s ?p ?o }");
        QueryExecution qexec = QueryExecutionFactory.sparqlService(serviceQuery, query);
        ResultSet rs = qexec.execSelect();
        int x = ResultSetFormatter.consume(rs);
        assertTrue(x != 0);
        Cache cache = SPARQL_Query_Cache.getCache();
        assertTrue(cache.size() == 1);
        Iterator keysIter = cache.keys();
        String key = (String) keysIter.next();
        assertTrue(key.contains("?s  ?p  ?o"));
        CacheEntry e = (CacheEntry) cache.getIfPresent(key);
        assertTrue(e.isInitialized());
        assertTrue(e.getData().length != 0);
    }

    @Test
    public void query_02() {
        Query query = QueryFactory.create("SELECT * { ?s ?p ?o }");
        QueryEngineHTTP engine = QueryExecutionFactory.createServiceRequest(serviceQuery, query);
        engine.setSelectContentType(WebContent.contentTypeResultsJSON);
        ResultSet rs = engine.execSelect();
        int x = ResultSetFormatter.consume(rs);
        assertTrue(x != 0);
    }

    @Test
    public void cache_query_02() {
        Query query = QueryFactory.create("SELECT * { ?s ?p ?o }");
        QueryEngineHTTP engine = QueryExecutionFactory.createServiceRequest(serviceQuery, query);
        engine.setSelectContentType(WebContent.contentTypeResultsJSON);
        ResultSet rs = engine.execSelect();
        int x = ResultSetFormatter.consume(rs);
        assertTrue(x != 0);
        Cache cache = SPARQL_Query_Cache.getCache();
        assertTrue(cache.size() == 1);
        Iterator keysIter = cache.keys();
        String key = (String) keysIter.next();
        System.out.println("key " + key);
        assertTrue(key.contains("?s  ?p  ?o"));
        assertTrue(key.contains("application/sparql-results+json"));
        CacheEntry e = (CacheEntry) cache.getIfPresent(key);
        assertTrue(e.isInitialized());
        assertTrue(e.getData().length != 0);
    }

    @Test
    public void cache_query_03() {
        Query query = QueryFactory.create("SELECT * { ?s ?p ?o }");
        QueryEngineHTTP engine = QueryExecutionFactory.createServiceRequest(serviceQuery, query);
        engine.setSelectContentType(WebContent.contentTypeResultsXML);
        ResultSet rs = engine.execSelect();
        String result = ResultSetFormatter.asXMLString(rs);
        Cache cache = SPARQL_Query_Cache.getCache();
        assertTrue(cache.size() == 1);
        ResultSet rs2 = engine.execSelect();
        String result2 = ResultSetFormatter.asXMLString(rs2);
        assertEqualsIgnoreCase(result, result2);
    }

    @Test
    public void update_01() {
        UpdateRequest update = UpdateFactory.create("INSERT DATA {}");
        UpdateProcessor proc = UpdateExecutionFactory.createRemote(update, serviceUpdate);
        proc.execute();
    }

    @Test
    public void update_02() {
        UpdateRequest update = UpdateFactory.create("INSERT DATA {}");
        UpdateProcessor proc = UpdateExecutionFactory.createRemoteForm(update, serviceUpdate);
        proc.execute();
    }
}
