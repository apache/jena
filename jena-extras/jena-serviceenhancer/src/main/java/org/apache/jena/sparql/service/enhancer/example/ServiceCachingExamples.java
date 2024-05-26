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

package org.apache.jena.sparql.service.enhancer.example;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;

import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.ResultSetMgr;
import org.apache.jena.riot.resultset.ResultSetLang;
import org.apache.jena.sparql.exec.http.QueryExecutionHTTP;
import org.apache.jena.sparql.service.enhancer.init.ServiceEnhancerConstants;
import org.apache.jena.sparql.service.enhancer.init.ServiceEnhancerInit;

/** Examples for setting up and using SERVICE caching */
public class ServiceCachingExamples {

    static { LogCtl.setLogging(); }

    public static void main(String[] args) {
        basicCachingExample();

        moreExamples();
    }

    public static void basicCachingExample() {
        Model model = ModelFactory.createDefaultModel();

        try (QueryExecution qe = QueryExecutionFactory.create(String.join("\n"
                , "SELECT * {"
                + "  SERVICE <loop:cache:bulk+3:http://dbpedia.org/sparql> {"
                + "    SELECT DISTINCT ?p { ?s a <http://dbpedia.org/ontology/MusicalArtist> ; ?p ?o }"
                + "  }"
                + "}"),
                model)) {

            ServiceEnhancerInit.wrapOptimizer(qe.getContext());
            benchmark(() -> ResultSetFormatter.consume(qe.execSelect()));
        }

        // The query below makes use of the cache and performs additional filtering
        // It's execution time should be significantly lower then the prior query
        try (QueryExecution qe = QueryExecutionFactory.create(String.join("\n"
                , "SELECT * {"
                + "  SERVICE <loop:cache:http://dbpedia.org/sparql> {"
                + "    SELECT DISTINCT ?p { ?s a <http://dbpedia.org/ontology/MusicalArtist> ; ?p ?o }"
                + "  }"
                + "  FILTER(CONTAINS(STR(?p), 'tim'))"
                + "}"),
                model)) {
            ServiceEnhancerInit.wrapOptimizer(qe.getContext());
            benchmark(() -> ResultSetFormatter.consume(qe.execSelect()));
        }
    }

    // TODO needs cleanup


    public static void testDbpedia() {
        String queryStr = String.join("\n",
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
            "PREFIX dbo: <http://dbpedia.org/ontology/>",
            "SELECT * WHERE {",
            "  SERVICE <cache:https://dbpedia.org/sparql> {",
            "    SELECT * {",
            "      ?s a dbo:MusicalArtist",
            "    } ORDER BY ?s LIMIT 10 OFFSET 20",
            "  }",
            "  SERVICE <cache:loop:bulk+10:https://dbpedia.org/sparql> {",
            "    ?s rdfs:label ?l",
            "  }",
            "}");

        // Model model = ModelFactory.createDefaultModel();
        QueryFactory.create(queryStr);
        // TS_ResultSetLimits.testWithCleanCaches(model, queryStr, 10000);
    }

    public static void moreExamples() {
        Model model;

//    	ServiceExecutorRegistryBulk.get().prepend(new ChainingServiceExecutorBulkSpecial());
        //ServiceExecutorRegistryBulk.get().chain(new ChainingServiceExecutorBulkCache());

//    	ServiceResponseCache serviceCache = new ServiceResponseCache();
//    	ARQ.getContext().set(ServicePlugin.serviceCache, serviceCache);


        try (QueryExecution qe = QueryExecutionHTTP.newBuilder()
            .endpoint("https://dbpedia.org/sparql")
            .query("CONSTRUCT { ?s ?p ?o } WHERE { { SELECT DISTINCT ?s { ?s a <http://dbpedia.org/ontology/Person> } LIMIT 10 } ?s ?p ?o }")
            .build()) {
            model = qe.execConstruct();
        }

        System.out.println("Backend request spo all");
        if (true) {
            try (QueryExecution qe = QueryExecutionFactory.create(
                    "SELECT * { { SELECT DISTINCT ?s { ?s a <http://dbpedia.org/ontology/Person> } LIMIT 3 } SERVICE <loop:cache:bulk+3> { { SELECT * { ?s ?p ?o } } } }",
                    model)) {
                ServiceEnhancerInit.wrapOptimizer(qe.getContext());
                ResultSetMgr.write(System.out, qe.execSelect(), ResultSetLang.RS_JSON);
            }
        }

        System.out.println("Backend request spo 1");
        if (true) {
            try (QueryExecution qe = QueryExecutionFactory.create(
                    "SELECT * { { SELECT DISTINCT ?s { ?s a <http://dbpedia.org/ontology/Person> } LIMIT 3 } SERVICE <loop:cache:bulk+3> { { SELECT * { ?s ?p ?o } LIMIT 1 } } }",
                    model)) {
                ServiceEnhancerInit.wrapOptimizer(qe.getContext());
                ResultSetMgr.write(System.out, qe.execSelect(), ResultSetLang.RS_JSON);
            }
        }

        System.out.println("Backend request:");
        if (true) {
            try (QueryExecution qe = QueryExecutionFactory.create(
                    "SELECT * { { SELECT DISTINCT ?s { ?s a <http://dbpedia.org/ontology/Person> } LIMIT 3 } SERVICE <loop:cache:bulk+3> { { SELECT * { ?s ?p ?o . FILTER(?p = <http://www.w3.org/2000/01/rdf-schema#label>) } ORDER BY ?p LIMIT 1 } } }",
                    model)) {
                ServiceEnhancerInit.wrapOptimizer(qe.getContext());
                ResultSetMgr.write(System.out, qe.execSelect(), ResultSetLang.RS_JSON);
            }
        }

        System.out.println("Serving from cache:");
        if (true) {
            try (QueryExecution qe = QueryExecutionFactory.create(
                    "SELECT * { { SELECT DISTINCT ?s { ?s a <http://dbpedia.org/ontology/Person> } LIMIT 3 } SERVICE <loop:cache:bulk+3> { { SELECT * { ?s ?p ?o . FILTER(?p = <http://www.w3.org/2000/01/rdf-schema#label>) } ORDER BY ?p LIMIT 1 } } }",
                    model)) {
                ServiceEnhancerInit.wrapOptimizer(qe.getContext());
                ResultSetMgr.write(System.out, qe.execSelect(), ResultSetLang.RS_JSON);
            }
        }

        System.out.println("Fetching one more binding per item:");
        if (true) {
            try (QueryExecution qe = QueryExecutionFactory.create(
                    "SELECT * { { SELECT DISTINCT ?s { ?s a <http://dbpedia.org/ontology/Person> } LIMIT 3 } SERVICE <loop:cache:bulk+3> { { SELECT * { ?s ?p ?o . FILTER(?p = <http://www.w3.org/2000/01/rdf-schema#label>) } ORDER BY ?p LIMIT 2 } } }",
                    model)) {
                ServiceEnhancerInit.wrapOptimizer(qe.getContext());
                ResultSetMgr.write(System.out, qe.execSelect(), ResultSetLang.RS_JSON);
            }
        }

        if (true) {
            // Test for nested loop
            // - Special emphasis of this test: Injected idxVars (references to lhs input bindings) must not clash
            try (QueryExecution qe = QueryExecutionFactory.create(
                    "SELECT * { BIND('x' AS ?x) SERVICE <loop:> { BIND(?x AS ?y) SERVICE <loop:> { BIND(?y AS ?z) } } }",
                    model)) {
                ServiceEnhancerInit.wrapOptimizer(qe.getContext());
                ResultSetMgr.write(System.out, qe.execSelect(), ResultSetLang.RS_JSON);
            }
        }

        // System.out.println(Algebra.compile(QueryFactory.create("SELECT * { ?s a <http://dbpedia.org/ontology/Person> SERVICE <https://dbpedia.org/sparql> { { SELECT ?s ?p { ?s ?p ?o . FILTER(?p = <http://www.w3.org/2000/01/rdf-schema#label>) } ORDER BY ?p } } }")));
        // System.out.println(Algebra.compile(QueryFactory.create("SELECT * { ?s a <http://dbpedia.org/ontology/Person> SERVICE <https://dbpedia.org/sparql> { BIND(?s AS ?x) } }")));

//        if (false) {
//            try (QueryExecution qe = QueryExecutionFactory.create(
//                    //"SELECT * { ?s a <http://dbpedia.org/ontology/Person> SERVICE <http://dbpedia.org/sparql> { { SELECT * { { BIND(?s AS ?x) } UNION { BIND(?s AS ?y) } UNION { ?s <urn:dummy> ?s } } } } }",
//                    "SELECT * { ?s a <http://dbpedia.org/ontology/Person> SERVICE <https://dbpedia.org/sparql> { { SELECT ?x ?y { { BIND(?s AS ?x) } UNION { BIND(?s AS ?y) } } } } }",
//                    //"SELECT * { ?s a <http://dbpedia.org/ontology/Person> SERVICE <https://dbpedia.org/sparql> { { BIND(?s AS ?x) } UNION { BIND(?s AS ?y) } } }",
//                    model)) {
//                 qe.getContext().set(InitServiceEnhancer.serviceBulkMaxBindingCount, 10);
//                qe.getContext().set(InitServiceEnhancer.serviceBulkRequestMaxByteSize, 1500);
//                ResultSetMgr.write(System.out, qe.execSelect(), ResultSetLang.RS_JSON);
//            }
//        }

        //		"SELECT * { ?s a <http://dbpedia.org/ontology/Person> SERVICE <https://dbpedia.org/sparql> { { SELECT ?s (COUNT(*) AS ?c) { ?s ?p ?o } GROUP BY ?s } } }",

        if (true) {
            try (QueryExecution qe = QueryExecutionFactory.create(
                    "SELECT * { { SELECT ?s { ?s a <http://dbpedia.org/ontology/Person> } OFFSET 1 LIMIT 1 } SERVICE <cache:bulk+20:https://dbpedia.org/sparql> { { SELECT ?s ?p ?o { ?s ?p ?o . FILTER(?p = <http://www.w3.org/2000/01/rdf-schema#label>) } ORDER BY ?p } } }",
                    model)) {
                ServiceEnhancerInit.wrapOptimizer(qe.getContext());
                qe.getContext().set(ServiceEnhancerConstants.serviceBulkMaxBindingCount, 10);
                // qe.getContext().set(ServiceEnhancerConstants.serviceBulkRequestMaxByteSize, 1500);
                ResultSetMgr.write(System.out, qe.execSelect(), ResultSetLang.RS_JSON);
            }
        }

        if (true) {
            try (QueryExecution qe = QueryExecutionFactory.create(
                    "SELECT * { { SELECT ?s { ?s a <http://dbpedia.org/ontology/Person> } OFFSET 0 LIMIT 3 } SERVICE <https://dbpedia.org/sparql> { { SELECT ?s ?p ?o { ?s ?p ?o . FILTER(?p = <http://www.w3.org/2000/01/rdf-schema#label>) } ORDER BY ?p } } }",
                    model)) {
                ServiceEnhancerInit.wrapOptimizer(qe.getContext());
                qe.getContext().set(ServiceEnhancerConstants.serviceBulkMaxBindingCount, 10);
                // qe.getContext().set(ServiceEnhancerConstants.serviceBulkRequestMaxByteSize, 1500);
                ResultSetMgr.write(System.out, qe.execSelect(), ResultSetLang.RS_JSON);
            }
        }

        if (true) {
            try (QueryExecution qe = QueryExecutionFactory.create(
                    "SELECT * { ?s a <http://dbpedia.org/ontology/Person> SERVICE <https://dbpedia.org/sparql> { { SELECT * { ?s ?p ?o } LIMIT 3 OFFSET 5 } } }",
                    model)) {
                // qe.getContext().set(ServicePlugin.serviceBulkRequestMaxItemCount, 1);
                // qe.getContext().set(ServiceEnhancerConstants.serviceBulkRequestMaxByteSize, 1500);
                ResultSetMgr.write(System.out, qe.execSelect(), ResultSetLang.RS_JSON);
            }
        }

        if (true) {
            try (QueryExecution qe = QueryExecutionFactory.create(
                    "SELECT * { SERVICE <https://dbpedia.org/sparql> { { SELECT DISTINCT ?p { ?s a <http://dbpedia.org/ontology/Company> ; ?p ?o } ORDER BY ?p } } }",
                    model)) {
                ServiceEnhancerInit.wrapOptimizer(qe.getContext());
                qe.getContext().set(ServiceEnhancerConstants.serviceBulkMaxBindingCount, 10);
                // qe.getContext().set(ServiceEnhancerConstants.serviceBulkRequestMaxByteSize, 1500);
                ResultSetMgr.write(System.out, qe.execSelect(), ResultSetLang.RS_JSON);
            }
        }

        if (true) {
            try (QueryExecution qe = QueryExecutionFactory.create(
                    "SELECT * { SERVICE <https://dbpedia.org/sparql> { { SELECT DISTINCT ?p { ?s a <http://dbpedia.org/ontology/Company> ; ?p ?o } ORDER BY ?p } } FILTER (CONTAINS(STR(?p), 'rdf'))}",
                    model)) {
                ServiceEnhancerInit.wrapOptimizer(qe.getContext());
                qe.getContext().set(ServiceEnhancerConstants.serviceBulkMaxBindingCount, 10);
                // qe.getContext().set(ServiceEnhancerConstants.serviceBulkRequestMaxByteSize, 1500);
                ResultSetMgr.write(System.out, qe.execSelect(), ResultSetLang.RS_JSON);
            }
        }
    }

    /** Utility method to measure the given callable's execution time and display a message on stdout */
    public static void benchmark(Callable<?> callable) {
        Stopwatch sw = Stopwatch.createStarted();
        Object result;
        try {
            result = callable.call();
        } catch (Exception e) {
            System.out.println("Failed in " + sw.elapsed(TimeUnit.MILLISECONDS) + "ms");
            throw new RuntimeException(e);
        }
        System.out.println("Obtained value [" + result + "] in " + sw.elapsed(TimeUnit.MILLISECONDS) + "ms");
    }
}
