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

package org.apache.jena.sparql.service.enhancer.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.base.StandardSystemProperty;

import org.junit.jupiter.api.Test;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.algebra.TableFactory;
import org.apache.jena.sparql.algebra.Transform;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.optimize.Optimize;
import org.apache.jena.sparql.core.Substitute;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecDataset;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.service.enhancer.algebra.TransformSE_JoinStrategy;
import org.apache.jena.sparql.service.enhancer.init.ServiceEnhancerConstants;
import org.apache.jena.sparql.service.enhancer.init.ServiceEnhancerInit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Miscellaneous tests for many aspects of the service enhancer plugin. */
public class TestServiceEnhancerMisc {
    private static final Logger logger = LoggerFactory.getLogger(TestServiceEnhancerMisc.class);

    @Test
    public void testLargeCache01() {
        String queryStr = String.join("\n",
                "SELECT * {",
                "  SERVICE <cache:> { ?s ?p ?o }",
                "}");

        Model model = AbstractTestServiceEnhancerResultSetLimits.createModel(1000);
        int evalRowCount = AbstractTestServiceEnhancerResultSetLimits.testWithCleanCaches(model, queryStr, 1000000000);
        int cachedRowCount = AbstractTestServiceEnhancerResultSetLimits.testCore(model, queryStr, 1000000000);

        assertEquals(evalRowCount, cachedRowCount);
    }

    /** A query where it's whole graph pattern is subject to caching */
    @Test
    public void testCacheFullQuery() {
        // TODO We need to clean up caches after testing!
        ServiceResultSizeCache.get().invalidateAll();
        ServiceResponseCache.get().invalidateAll();

        Model model = AbstractTestServiceEnhancerResultSetLimits.createModel(1000);
        int rows = AbstractTestServiceEnhancerResultSetLimits.testCore(model, "SELECT * { SERVICE <cache:> { SELECT DISTINCT ?p { ?s ?p ?o } } }", 100);
        assertEquals(3, rows);

        // TODO We need to ensure that no backend request is made
        // We could register a custom service executor that does the counting
        // And/Or the test runner could return a stats object which includes the number of backend requests

        int cachedRows = AbstractTestServiceEnhancerResultSetLimits.testCore(model, "SELECT * { SERVICE <cache:> { SELECT DISTINCT ?p { ?s ?p ?o } } }", 100);
        assertEquals(3, cachedRows);
    }

    @Test
    public void testNestedLoopWithPropertyFunction() {
        String queryStr = String.join("\n",
                "PREFIX apf: <http://jena.apache.org/ARQ/property#>",
                "SELECT * {",
                "  SERVICE <loop:> { ?x1 ?p1 ?x2 }",
                "  SERVICE <loop:> { ?x2 ?p2 ?x3 }",
                "  SERVICE <loop:> { ?x3 apf:assign ?x4 }",
                "}");

        Model model = AbstractTestServiceEnhancerResultSetLimits.createModel(10);
        int rows = AbstractTestServiceEnhancerResultSetLimits.testWithCleanCaches(model, queryStr, 1000);
        assertEquals(110, rows);
    }

    /** Tests that a loop join where the scoped visible variables on either side are disjoint
     *  results in the right substitution */
    @Test
    public void testLoopJoinWithScope() {
        String queryStr = String.join("\n",
                "SELECT * {",
                "  { SELECT DISTINCT ?s { ?s a <urn:Department> ; ?p ?o } ORDER BY ?s }",
                "  SERVICE <loop:> { SELECT ?o { ?s <urn:hasEmployee> ?o } ORDER BY DESC(?o) LIMIT 1 }",
                "}");

        Model model = AbstractTestServiceEnhancerResultSetLimits.createModel(9);
        int rows = AbstractTestServiceEnhancerResultSetLimits.testWithCleanCaches(model, queryStr, 1000);
        assertEquals(9, rows);
    }

    @Test
    public void testLookupJoinWithScopeAndCache() {
        String queryStr = String.join("\n",
                "SELECT * {",
                "  { SELECT DISTINCT ?s { ?s a <urn:Department> ; ?p ?o } ORDER BY ?s }",
                "  SERVICE <cache:loop:> { SELECT ?o { ?s <urn:hasEmployee> ?o } ORDER BY DESC(?o) LIMIT 1 }",
                "}");

        Model model = AbstractTestServiceEnhancerResultSetLimits.createModel(9);
        int referenceRowCount = AbstractTestServiceEnhancerResultSetLimits.testWithCleanCaches(model, queryStr, 1000);
        assertEquals(9, referenceRowCount);
        int actualRowCount = AbstractTestServiceEnhancerResultSetLimits.testCore(model, queryStr, 1000);
        assertEquals(referenceRowCount, actualRowCount);

/*
-------------------------------
| s           | o             |
===============================
| <urn:dept1> | <urn:person9> |
| <urn:dept2> | <urn:person8> |
| <urn:dept3> | <urn:person7> |
 */
    }

    @Test
    public void testStdJoinWithScope() {
        String queryStr = String.join("\n",
                "SELECT * {",
                "  { SELECT DISTINCT ?s { ?s a <urn:Department> ; ?p ?o } ORDER BY ?s }",
                "  SERVICE <urn:x-arq:self> { SELECT ?o { ?s <urn:hasEmployee> ?o } ORDER BY DESC(?o) LIMIT 1 }",
                "}");

        /*
-------------------------------
| s           | o             |
===============================
| <urn:dept1> | <urn:person9> |
| <urn:dept2> | <urn:person9> |
| <urn:dept3> | <urn:person9> |
         */

        Model model = AbstractTestServiceEnhancerResultSetLimits.createModel(9);
        int actualRowCount = AbstractTestServiceEnhancerResultSetLimits.testWithCleanCaches(model, queryStr, 1000);
        assertEquals(9, actualRowCount);
    }

    @Test
    public void testNestedCache() {
        String queryStr = String.join("\n",
                "SELECT * {",
                "  SERVICE <cache:> {",
                "    SERVICE <xcache:> { SELECT ?s { ?s a <urn:Department> } ORDER BY ?s OFFSET 7 LIMIT 2 }",
                "    SERVICE <loop:xcache:> { ?s <urn:hasEmployee> ?o }",
                "  }",
                "}").replace("xcache:", "urn:x-arq:self");

        Model model = AbstractTestServiceEnhancerResultSetLimits.createModel(9);
        int referenceRowCount = AbstractTestServiceEnhancerResultSetLimits.testWithCleanCaches(model, queryStr, 1000);
        int actualRowCount = AbstractTestServiceEnhancerResultSetLimits.testCore(model, queryStr, 1000);

        assertEquals(3, referenceRowCount);
        assertEquals(referenceRowCount, actualRowCount);
    }

    @Test
    public void testCacheRefresh_01a() {
        String queryStr = String.join("\n",
                "SELECT * {",
                "  SERVICE <cache+clear:> { SELECT ?s { ?s a <urn:Department> } ORDER BY ?s OFFSET 7 LIMIT 2 }",
                "}");

        Model model = AbstractTestServiceEnhancerResultSetLimits.createModel(9);
        int referenceRowCount = AbstractTestServiceEnhancerResultSetLimits.testWithCleanCaches(model, queryStr, 1000);
        int actualRowCount = AbstractTestServiceEnhancerResultSetLimits.testCore(model, queryStr, 1000);

        assertEquals(2, referenceRowCount);
        assertEquals(referenceRowCount, actualRowCount);
    }

    @Test
    public void testCacheRefresh_01b() {
        String queryStr = String.join("\n",
                "SELECT * {",
                "  SERVICE <cache+clear:> {",
                "    SERVICE <cache+clear:> { SELECT ?s { ?s a <urn:Department> } ORDER BY ?s OFFSET 7 LIMIT 2 }",
                "    SERVICE <loop:cache+clear:> { ?s <urn:hasEmployee> ?o }",
                "  }",
                "}"); // .replace("xcache:", "urn:x-arq:self");

        Model model = AbstractTestServiceEnhancerResultSetLimits.createModel(9);
        int referenceRowCount = AbstractTestServiceEnhancerResultSetLimits.testWithCleanCaches(model, queryStr, 1000);
        int actualRowCount = AbstractTestServiceEnhancerResultSetLimits.testCore(model, queryStr, 1000);

        assertEquals(3, referenceRowCount);
        assertEquals(referenceRowCount, actualRowCount);
    }

    @Test
    public void testCacheRefreshWithOffsetOutside() {
        String queryStr = String.join("\n",
                "SELECT * {",
                "  SERVICE <cache:> {",
                "    SELECT ?s { ?s a <urn:Department> } ORDER BY ?s",
                "  }",
                "} LIMIT 5 OFFSET 5"); // .replace("xcache:", "urn:x-arq:self");

        Model model = AbstractTestServiceEnhancerResultSetLimits.createModel(9);
        int referenceRowCount = AbstractTestServiceEnhancerResultSetLimits.testWithCleanCaches(model, queryStr, 1000);

        queryStr = queryStr.replace("cache:", "cache+clear:");
        int actualRowCount = AbstractTestServiceEnhancerResultSetLimits.testCore(model, queryStr, 1000);

        assertEquals(4, referenceRowCount);
        assertEquals(referenceRowCount, actualRowCount);
    }

    /** Test case where LIMIT/OFFSET is used within the SERVICE <cache:> block. */
    @Test
    public void testCacheRefreshWithOffsetInside() {
        String queryStr = String.join("\n",
                "SELECT * {",
                "  SERVICE <cache:> {",
                "    SELECT ?s { ?s a <urn:Department> } ORDER BY ?s OFFSET 5 LIMIT 10",
                "  }",
                "}"); // .replace("xcache:", "urn:x-arq:self");

        Model model = AbstractTestServiceEnhancerResultSetLimits.createModel(9);
        int rows = AbstractTestServiceEnhancerResultSetLimits.testWithCleanCaches(model, queryStr, 1000);
        int rows2 = AbstractTestServiceEnhancerResultSetLimits.testCore(model, queryStr, 1000);

        queryStr = queryStr.replace("cache:", "cache+clear:");
        int rows3 = AbstractTestServiceEnhancerResultSetLimits.testCore(model, queryStr, 1000);

        assertEquals(4, rows);
        assertEquals(rows, rows2);
        assertEquals(rows, rows3);
    }

    @Test
    public void testSubstitute() {
        String queryStr = String.join("\n",
                "SELECT ?s {",
                "  ?s ?p ?o ",
                "  { SELECT ?p {",
                "    ?s ?p ?o ",
                "  } }",
                "}");

        String expectedQueryStr = String.join("\n",
                "SELECT  ?s",
                "WHERE",
                "  { <urn:s>  ?p  ?o",
                "    { SELECT  ?p",
                "      WHERE",
                "        { <urn:s>  ?p  ?o }",
                "    }",
                "  }");

        Query expectedQuery = QueryFactory.create(expectedQueryStr);
        Op op = Algebra.compile(QueryFactory.create(queryStr));
        Op op2 = Substitute.substitute(op, BindingFactory.binding(Var.alloc("s"), NodeFactory.createURI("urn:s")));
        Query actualQuery = OpAsQuery.asQuery(op2);
        assertEquals(expectedQuery, actualQuery);
    }

    /** Tests for the presence of the function cacheInvalidate and expects it to return one binding
     * with the number of invalidated entries */
    @Test
    public void testCacheMgmtInvalidate() {
        String queryStr = String.join("\n",
                "PREFIX se: <http://jena.apache.org/service-enhancer#>",
                "SELECT (se:cacheRm() AS ?count) WHERE {",
                "}");

        Dataset dataset = DatasetFactory.create();
        dataset.getContext().set(ServiceEnhancerConstants.enableMgmt, true);
        int actualRowCount = AbstractTestServiceEnhancerResultSetLimits.testWithCleanCaches(dataset, queryStr, 1000);
        assertEquals(1, actualRowCount);
    }

    /** Tests whether cacheLs with empty argument only lists the ids */
    @Test
    public void testCacheMgmtList01() {
        // This call creates one cache entry
        testCacheRefreshWithOffsetInside();

        String queryStr = String.join("\n",
                "PREFIX se: <http://jena.apache.org/service-enhancer#>",
                "SELECT * WHERE {",
                "  ?id se:cacheLs (?service ?queryStr)",
                "}");

        AbstractTestServiceEnhancerResultSetLimits.assertRowCount(1, ModelFactory.createDefaultModel(), queryStr, 1000);
    }

    /** Tests for the presence of the property function cacheLs */
    @Test
    public void testCacheMgmtList02() {

        // This call creates one cache entry
        testCacheRefreshWithOffsetInside();

        String queryStr = String.join("\n",
                "PREFIX se: <http://jena.apache.org/service-enhancer#>",
                "SELECT * WHERE {",
                "  ?id se:cacheLs (?op ?binding ?start ?end)",
                "}");

        int actualRowCount = AbstractTestServiceEnhancerResultSetLimits.testCore(ModelFactory.createDefaultModel(), queryStr, 1000);
        assertEquals(1, actualRowCount);
    }

    @Test
    public void testWikiData() {
        // This test case can be turned into an integration test by using an empty dataset
        // and adding the wikidata URL below to the service request
        Dataset dataset = RDFDataMgr.loadDataset("semweb.wikidata.sample.ttl");

        String queryStr = String.join("\n",
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
                "PREFIX wd: <http://www.wikidata.org/entity/>",
                "SELECT * { SERVICE <cache:> {",
            "SELECT ?s ?l {",
                "  # Apache Jena, Semantic Web, RDF, SPARQL, Andy Seaborne",
                "  VALUES ?s { wd:Q1686799 wd:Q54837 wd:Q54872 wd:Q54871 wd:Q108379795 }",
                "  SERVICE <cache:loop:bulk+5:> {", // https://query.wikidata.org/sparql
                "    SELECT ?l {",
                "      ?s rdfs:label ?l",
                "      FILTER(langMatches(lang(?l), 'en'))",
                "    } ORDER BY ?l LIMIT 1",
                "  }",
                "}",
            "} }");

        int referenceRowCount = AbstractTestServiceEnhancerResultSetLimits.testWithCleanCaches(dataset, queryStr, 1000);
        int actualRowCount = AbstractTestServiceEnhancerResultSetLimits.testCore(dataset, queryStr, 1000);
        assertEquals(referenceRowCount, actualRowCount);
    }

    @Test
    public void testSubSelectInService() {
        String queryStr = String.join("\n",
                "SELECT ?s ?o {",
                "  { SELECT ?s { ?s a <urn:Department> } ORDER BY ?s OFFSET 6 LIMIT 3 } ",
                "  SERVICE <cache:loop:> {",
                "    SELECT ?o {",
                "    ?s <urn:hasEmployee> ?o",
                "  } ORDER BY ?o }",
                "}");

         Model model = AbstractTestServiceEnhancerResultSetLimits.createModel(9);
         int actualRowCount = AbstractTestServiceEnhancerResultSetLimits.testWithCleanCaches(model, queryStr, 1000);

         assertEquals(6, actualRowCount);
    }

    @Test
    public void testLoopScope() {
        String queryStr = String.join("\n",
                "SELECT ?s {",
                "  ?s ?p ?o ",
                "  { SELECT ?p {",
                "    ?s ?p ?o ",
                // "    SERVICE <loop:> { SELECT ?x { ?o ?x ?y } LIMIT 1 }",
                "    SERVICE <loop:> { SELECT ?x { ?o ?x ?y } }",
                "  } }",
                "}");

        String expectedStr = String.join("\n",
                "(project (?s)",
                "  (sequence",
                "    (bgp (triple ?s ?p ?o))",
                "    (project (?p)",
                "      (sequence",
                "        (bgp (triple ?/s ?p ?/o))",
                "        (service <loop:>",
                "          (project (?/x)",
                "            (bgp (triple ?/o ?/x ?//y))))))))",
                "");

         Op op = Algebra.compile(QueryFactory.create(queryStr));
         Op op2 = Optimize.stdOptimizationFactory.create(ARQ.getContext()).rewrite(op);
         Op op3 = Transformer.transform(new TransformSE_JoinStrategy(), op2);

         assertEquals(expectedStr, op3.toString());
    }

    @Test
    public void testScope3() {
        Dataset dataset = DatasetFactory.create();
        dataset.getNamedModel("urn:g1").add(AbstractTestServiceEnhancerResultSetLimits.createModel(5));
        dataset.getNamedModel("urn:g2").add(AbstractTestServiceEnhancerResultSetLimits.createModel(4));
        dataset.getNamedModel("urn:g3").add(AbstractTestServiceEnhancerResultSetLimits.createModel(3));

        String queryStr = String.join("\n",
           "SELECT ?c {",
                    "SELECT ?c WHERE {\n"
                     // + "  VALUES ?p {geo:hasGeometry}\n"
                     + "    BIND(<urn:hasEmployee> AS ?p)\n"
                   + "SERVICE <loop:> {"
                    + "    { SERVICE <bulk+3:cache:> { SELECT ?g (count(*) AS ?c) { GRAPH ?g { ?s ?p ?o } } GROUP BY ?g } }\n"
                   + "}"
                    + "}",

             "}"); //.replace("loop:", "urn:x-arq:self");

        int referenceRowCount = AbstractTestServiceEnhancerResultSetLimits.testWithCleanCaches(dataset, queryStr, 1000);
        assertEquals(3, referenceRowCount);
        int actualRowCount = AbstractTestServiceEnhancerResultSetLimits.testCore(dataset, queryStr, 1000);
        assertEquals(referenceRowCount, actualRowCount);
    }

    @Test
    public void testScopeSimple() {
//        String queryStr2 = String.join("\n",
//                "SELECT ?p ?c {",
//                "  BIND(<http://www.w3.org/1999/02/22-rdf-syntax-ns#type> AS ?p)",
//                "  { SELECT (COUNT(*) AS ?c) { ?s ?p ?o } }",
//                "}");

        String queryStr = String.join("\n",
                "SELECT ?p ?c {",
                "  BIND(<http://www.w3.org/1999/02/22-rdf-syntax-ns#type> AS ?p)",
                "  SERVICE <loop:> { SELECT (COUNT(*) AS ?c) { ?s ?p ?o } }",
                "}");

        String expectedStr = String.join("\n",
                "(project (?p ?c)",
                "  (sequence",
                "    (extend ((?p <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>))",
                "      (table unit))",
                "    (service <loop:>",
                "      (project (?c)",
                "        (extend ((?c ?/.0))",
                "          (group () ((?/.0 (count)))",
                "            (bgp (triple ?/s ?p ?/o))))))))",
                "");


        Transform loopTransform = new TransformSE_JoinStrategy();
        Op op0 = Algebra.compile(QueryFactory.create(queryStr));
        Op op1 = Transformer.transform(loopTransform, op0);
        Op op2 = Optimize.stdOptimizationFactory.create(ARQ.getContext()).rewrite(op1);
        Op op3 = Transformer.transform(loopTransform, op2);

        assertEquals(expectedStr, op3.toString());
    }

    @Test
    public void testNormalization01() {
        Dataset dataset = DatasetFactory.create();
        dataset.getNamedModel("urn:g1").add(AbstractTestServiceEnhancerResultSetLimits.createModel(5));
        dataset.getNamedModel("urn:g2").add(AbstractTestServiceEnhancerResultSetLimits.createModel(4));
        dataset.getNamedModel("urn:g3").add(AbstractTestServiceEnhancerResultSetLimits.createModel(3));

        String queryStr = "SELECT * WHERE {\n"
                + "    { SERVICE <cache:> { SELECT ?g (count(*) AS ?c) { GRAPH ?g { ?s ?p ?o } } GROUP BY ?g } }\n"
                + "  UNION\n"
                + "    { SELECT ('total' AS ?g) (?x AS ?c)\n"
                + "      { SELECT (sum(?c) AS ?x) {\n"
                + "        { SERVICE <cache:> { SELECT (count(*) AS ?c) { GRAPH ?g { ?s ?p ?o } } GROUP BY ?g } }\n"
                + "      } } \n"
                + "    }\n"
                + "  # FILTER(CONTAINS(STR(?g), 'power')) # hier einfach aendern\n"
                + "}";

        int actualRowCount = AbstractTestServiceEnhancerResultSetLimits.testWithCleanCaches(dataset, queryStr, 1000);
        assertEquals(4, actualRowCount);
    }

    /**
     * Test for <a href="https://github.com/apache/jena/issues/1688">/issues/1688</a>.
     * <p>
     * This test checks that building an overall result set from a bulk request
     * that involves contributions of cached empty result sets works as expected.
     * Without the fix corresponding to this issues this test fails.
     *
     * @implNote This test case makes use of a dataset where only a few resources have labels.
     * The test query caches the labels of all resources which means that most cache entries
     * have empty results.
     */
    @Test
    public void testBulkRequestsOverCachedEmptyResultSets() {
        String dataStr = String.join(StandardSystemProperty.LINE_SEPARATOR.value(),
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
            "<urn:example:00> a rdfs:Resource .",
            "<urn:example:01> a rdfs:Resource .",
            "<urn:example:02> a rdfs:Resource .",
            "<urn:example:03> a rdfs:Resource .",
            "<urn:example:04> a rdfs:Resource ; rdfs:label '04' .",
            "<urn:example:05> a rdfs:Resource .",
            "<urn:example:06> a rdfs:Resource .",
            "<urn:example:07> a rdfs:Resource .",
            "<urn:example:08> a rdfs:Resource ; rdfs:label '08' .",
            "<urn:example:09> a rdfs:Resource ; rdfs:label '09' .");

        String queryStr = String.join(StandardSystemProperty.LINE_SEPARATOR.value(),
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
            "SELECT * {",
            "  { SELECT * { ?s a rdfs:Resource } ORDER BY ?s }",
            "   SERVICE <loop:cache:bulk+5> { ?s rdfs:label ?l }",
            "}");

        Dataset ds = RDFParser.fromString(dataStr, Lang.TURTLE).toDataset();
        Query query = QueryFactory.create(queryStr);

        int rsSize;
        ServiceResponseCache localCache = new ServiceResponseCache();
        // Execute twice: First populate the cache then read from it
        for (int i = 0; i < 2; ++i) {
            try (QueryExecution qe = QueryExecutionFactory.create(query, ds)) {
                ServiceResponseCache.set(qe.getContext(), localCache);
                ResultSet rs = qe.execSelect();
                rsSize = ResultSetFormatter.consume(rs);
            }
            // Expect the 3 labels of the test dataset
            assertEquals(3, rsSize);
        }
    }

    /** Test case where an attempt is made to cache slightly more items than the maximum cache size. */
    @Test
    public void testCacheEvictionCornerCase() {
        // Investigation of some some very rare race conditions due to cache thrashing
        // required around 100K+ tests.
        int numTests = 100;
        int maxCacheSize = 10;
        int numExcessItems = 1; // Number of items by which to exceed the maximum cache size.
        testCacheEvictionCornerCaseWorker(numTests, maxCacheSize, numExcessItems);
        // IntStream.range(0, 20).boxed().toList().parallelStream().forEach(i -> testCacheEvictionCornerCaseWorker());
    }

    public void testCacheEvictionCornerCaseWorker(int numTests, int maxCacheSize, int numExcessItems) {
        Model model = AbstractTestServiceEnhancerResultSetLimits.createModel(maxCacheSize + numExcessItems);
        Dataset ds = DatasetFactory.wrap(model);
        ServiceResponseCache cache = new ServiceResponseCache(1, 1, maxCacheSize);
        ServiceResponseCache.set(ds.getContext(), cache);

        String queryStr = """
                SELECT * {
                    { SELECT ?dept { ?dept a <urn:Department> } ORDER BY ?dept LIMIT $N }
                    SERVICE <loop:cache:> { SELECT ?dept (COUNT(*) AS ?employees) { ?dept <urn:hasEmployee> ?emp } GROUP BY ?dept }
                }
                """.replace("$N", "" + (maxCacheSize + numExcessItems));
        Query query = QueryFactory.create(queryStr);

        Table prevTable = null;
        for (int i = 0; i < numTests; ++i) {
            Table thisTable;
            try (QueryExec qe = QueryExecDataset.newBuilder()
                .dataset(ds.asDatasetGraph())
                .query(query)
                .build()) {
                thisTable = TableFactory.create(qe.select());
            }

            if (prevTable != null) {
                if (!prevTable.equals(thisTable)) {
                    System.err.println("Test failure on iteration #" + i);
                }
                assertEquals(prevTable, thisTable);
            } else {
                prevTable = thisTable;
            }

            if (i % 10 == 0) {
                cache.invalidateAll();
            }
        }
    }

    @Test
    public void testCacheEvictionCornerCase2() {
        // Investigation of some some very rare race conditions due to cache thrashing.
        // There was a bug in QueryIterBulkAndCache.moveToNext when backend requests were
        // followed by cached ranges: In that case iterator ended too early not serving the cached data.
        // Reproduction required around 100K+ iterations.
        int numTests = 100;
        int maxCacheSize = 10;
        int numExcessItems = 1; // Number of items by which to exceed the maximum cache size.
        testCacheEvictionCornerCaseWorker2(numTests, maxCacheSize, numExcessItems);
        // IntStream.range(0, 20).boxed().toList().parallelStream().forEach(i -> testCacheEvictionCornerCaseWorker());
    }

    public void testCacheEvictionCornerCaseWorker2(int numTests, int maxCacheSize, int numExcessItems) {
        Dataset ds = RDFDataMgr.loadDataset("linkedgeodata.sample.ttl");
        ServiceResponseCache cache = new ServiceResponseCache(1, 1, maxCacheSize);
        ServiceResponseCache.set(ds.getContext(), cache);

        String queryStr = """
                PREFIX owl: <http://www.w3.org/2002/07/owl#>
                SELECT * {
                    { SELECT ?t { ?t a owl:Class } LIMIT $N }
                    SERVICE <loop:cache:bulk+2:> { SELECT ?s ?t { ?s a ?t } LIMIT 2 }
                }
                """.replace("$N", "" + (maxCacheSize + numExcessItems));
        Query query = QueryFactory.create(queryStr);

        Table prevTable = null;
        for (int i = 0; i < numTests; ++i) {
            Table thisTable;
            try (QueryExec qe = QueryExecDataset.newBuilder()
                .dataset(ds.asDatasetGraph())
                .query(query)
                .build()) {
                ServiceEnhancerInit.wrapOptimizer(qe.getContext());

                RowSet rs = qe.select();
                thisTable = TableFactory.create(rs);
            }

            if (logger.isDebugEnabled()) {
                logger.debug(ResultSetFormatter.asText(ResultSet.adapt(thisTable.toRowSet())));
            }

            if (prevTable != null) {
                if (!prevTable.equals(thisTable)) {
                    if (logger.isErrorEnabled()) {
                        logger.error("Test failure on iteration #" + i);
                    }
                }
                assertEquals(prevTable, thisTable);
            } else {
                prevTable = thisTable;
            }

//            if (i % 10 == 0) {
//                cache.invalidateAll();
//            }
        }
    }
}
