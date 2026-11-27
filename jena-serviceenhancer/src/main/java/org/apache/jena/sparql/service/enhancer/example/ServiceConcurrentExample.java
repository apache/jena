/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.sparql.service.enhancer.example;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.resultset.ResultsFormat;
import org.apache.jena.sparql.service.enhancer.init.ServiceEnhancerInit;
import org.apache.jena.sparql.util.Context;

/**
 * This example demonstrates concurrent bulk retrieval from a remote endpoint.
 * <p>
 * The central construct is explained as follows:
 * <pre>
 * SERVICE &lt;loop+scoped:concurrent+20:bulk+50:cache:https://linkedgeodata.org/sparql&gt; { ... }
 * </pre>
 *
 * <ul>
 *   <li><code>loop</code> activates "for-each"-mode:
 *     Each binding produced by the graph pattern before the SERVICE clause becomes an <i>input binding</i>.
 *     Conceptually, the SERVICE clause is evaluated w.r.t. each input binding.
 *   </li>
 *   <li><code>concurrent+10-25</code> indicates to partition the input to 10 threads with 25 bindings assigned to each thread.</li>
 *   <li><code>bulk+25</code> indicates to group 25 input bindings into a single request.
 *     Note, that <code>concurrent</code> must appear before <code>bulk</code> so that <code>bulk</code> is executed
 *     within each partition created by <code>concurrent</code>.</li>
 *   <li><code>cache></code> instructs to cache the <i>output bindings</i> produced by the service pattern (including the service IRI) with each input binding.</li>
 * </ul>
 *
 * <p>
 *
 * Notes on <code>loop+scoped</code>:
 * <p>
 * The <code>scoped</code> option causes <code>loop</code> to only substitute in-scope variables of the SERVICE pattern.
 * Without <code>scoped</code>, <code>loop</code> would replace variables regardless of their scope - i.e. also variables nested in sub-queries.
 * The <code>scoped</code> option aligns <code>loop</code> closer with the semantics of SPARQL's <code>LATERAL</code> keyword.
 *
 */
public class ServiceConcurrentExample {
    static { LogCtl.setJavaLogging(); }

    public static void main(String[] args) {
        String endpointUrl = "https://data.aksw.org/coypu";

        // Number of threads in addition to the main thread.
        int numThreads = 10;

        // Bindings per batch.
        // If the number is too high the HTTP requests may fail because they become too large.
        int numBindingsPerBatch = 25;

        // How often to repeat the query
        int numRepeats = 3;
        boolean showTableOnlyOnFirstIteration = true;
        boolean showTable = true;
        boolean showFinalQuery = true;

        String queryStr = """
            PREFIX owl: <http://www.w3.org/2002/07/owl#>
            SELECT * {
              # Fetch 1000 classes from the remote endpoint
              SERVICE <{E}> {
                SELECT * {
                  ?t a owl:Class .
                  FILTER(isIRI(?t))
                } LIMIT 1000
              }

              # For each class fetch 2 instances. Use T threads each with a batch of B classes.
              SERVICE <loop+scoped:concurrent+{T}-{B}:bulk+{B}:cache:{E}> {
                # LATERAL { SERVICE <{E}> {
                  SELECT * { ?s a ?t } LIMIT 2
                # } }
              }
            }
            """
            .replace("{E}", endpointUrl)
            .replace("{T}", Integer.toString(numThreads))
            .replace("{B}", Integer.toString(numBindingsPerBatch))
            ;

        DatasetGraph dsg = DatasetGraphFactory.create();

        // Enable loop on the data set (registers a pre-processing step to Jena's default optimizer).
        Context cxt = dsg.getContext();
        ServiceEnhancerInit.wrapOptimizer(cxt);

        // Configure a bigger cache as needed.
        // ServiceResponseCache.set(cxt, new ServiceResponseCache(10000, 10000, 15));

        Query query = QueryFactory.create(queryStr);

        for (int i = 0; i < numRepeats; ++i) {
            StopWatch sw = StopWatch.createStarted();
            Table table = QueryExec.dataset(dsg).query(query).table();
            sw.stop();

            if (showTable && !(showTableOnlyOnFirstIteration && i != 0)) {
                ResultSetFormatter.output(System.out, ResultSet.adapt(table.toRowSet()), ResultsFormat.TEXT);
            }

            System.out.println("Fetched " + table.size() + " rows in " + sw + ".");
            System.out.println();

            if (showFinalQuery && i + 1 == numRepeats) {
                System.out.println("Query:");
                System.out.println(query);
            }
        }
    }
}
