/**
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

package arq.examples.service;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.engine.iterator.QueryIterRoot;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecDatasetBuilder;
import org.apache.jena.sparql.service.ServiceExecutorRegistry;
import org.apache.jena.sparql.service.single.ChainingServiceExecutor;
import org.apache.jena.sparql.service.single.ServiceExecutor;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.QueryExecUtils;

public class CustomServiceExecutor {

    /** Query for resources having the label "Apache Jena"en */
    public static final String QUERY_STR = String.join("\n",
            "PREFIX wd: <http://www.wikidata.org/entity/>",
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
            "PREFIX dbr: <http://dbpedia.org/resource/>",
            "SELECT * {",
            "  SERVICE <http://query.wikidata.org/sparql> {",
            "    SELECT * {",
            "      ?s rdfs:label \"Apache Jena\"@en",
            "     } LIMIT 10",
            "  }",
            "}");

    public static final Node WIKIDATA = NodeFactory.createURI("http://query.wikidata.org/sparql");
    public static final Node DBPEDIA = NodeFactory.createURI("http://dbpedia.org/sparql");

    public static void main(String[] args) {

        Dataset dataset = DatasetFactory.empty();

        conventionalExec(dataset);
        relayWikidataToDBpediaChaining(dataset);
        relayWikidataToDBpedia(dataset);
        suppressRemoteRequests(dataset);
    }

    /** Conventionally execute a query against Wikidata as per request */
    public static void conventionalExec(Dataset dataset) {
        // Default: Send request to Wikidata
        execQueryAndShowResult(dataset, QUERY_STR, null);

        /* ---------------
         * | s           |
         * ===============
         * | wd:Q1686799 |
         * ---------------
         */
    }

    /** Relay requests for Wikidata to DBpedia instead. Modern variant using chaining. */
    public static void relayWikidataToDBpediaChaining(Dataset dataset) {
        ChainingServiceExecutor relaySef = (opExecute, original, binding, execCxt, chain) -> {
            if (opExecute.getService().equals(WIKIDATA)) {
                opExecute = new OpService(DBPEDIA, opExecute.getSubOp(), opExecute.getSilent());
            }
            return chain.createExecution(opExecute, original, binding, execCxt);
        };

        Context cxt = ARQ.getContext().copy();
        ServiceExecutorRegistry registry = ServiceExecutorRegistry.get(cxt).copy();
        registry.addSingleLink(relaySef);

        ServiceExecutorRegistry.set(cxt, registry);
        execQueryAndShowResult(dataset, QUERY_STR, cxt);

        /*
         * -------------------
         * | s               |
         * ===================
         * | dbr:Apache_Jena |
         * -------------------
         */
    }

    /** Relay requests for Wikidata to DBpedia instead. Non-chaining legacy variant. */
    public static void relayWikidataToDBpedia(Dataset dataset) {
        ServiceExecutor relaySef = (opExecute, original, binding, execCxt) -> {
                if (opExecute.getService().equals(WIKIDATA)) {
                    opExecute = new OpService(DBPEDIA, opExecute.getSubOp(), opExecute.getSilent());
                    return ServiceExecutorRegistry.httpService.createExecution(opExecute, original, binding, execCxt);
                }
                return null;
            };

        Context cxt = ARQ.getContext().copy();
        ServiceExecutorRegistry registry = ServiceExecutorRegistry.get(cxt).copy();
        registry.add(relaySef);

        ServiceExecutorRegistry.set(cxt, registry);
        execQueryAndShowResult(dataset, QUERY_STR, cxt);

        /*
         * -------------------
         * | s               |
         * ===================
         * | dbr:Apache_Jena |
         * -------------------
         */
    }

    /** Suppress remote requests - make any SERVICE request return the input binding */
    public static void suppressRemoteRequests(Dataset dataset) {
        ServiceExecutor noop = (opExecute, original, binding, execCxt) -> QueryIterRoot.create(execCxt);

        Context cxt = ARQ.getContext().copy();
        ServiceExecutorRegistry registry = new ServiceExecutorRegistry();
        registry.add(noop);

        ServiceExecutorRegistry.set(cxt, registry);
        execQueryAndShowResult(dataset, QUERY_STR, cxt);

        /*
         * -----
         * | s |
         * =====
         * |   |
         * -----
         */
    }

    public static void execQueryAndShowResult(
            Dataset dataset,
            String queryStr,
            Context cxt) {
        try {
            try (QueryExec exec = QueryExecDatasetBuilder.create()
                    .dataset(dataset.asDatasetGraph())
                    .query(queryStr)
                    .context(cxt)
                    .build()) {
                QueryExecUtils.exec(exec);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
