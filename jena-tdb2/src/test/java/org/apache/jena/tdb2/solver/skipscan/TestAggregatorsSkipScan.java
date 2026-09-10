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

package org.apache.jena.tdb2.solver.skipscan;

import org.junit.jupiter.api.Test;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.tdb2.TDB2Factory;
import org.apache.jena.tdb2.sys.CopyDSG;
import org.apache.jena.tdb2.sys.TDBInternal;

/**
 * Base class for testing multi-aggregator queries with skip scan.
 *
 * <p>Tests verify that skip scan is used for queries with multiple aggregators
 * that operate on compatible variables (same variable with compatible distinct semantics).</p>
 */
public class TestAggregatorsSkipScan {
    protected DatasetGraph referenceDsg;
    protected DatasetGraph systemUnderTestDsg;

    public TestAggregatorsSkipScan() {
        setupDataset();
    }

    // @BeforeAll
    public void setupDataset() {
        referenceDsg = SSE.parseDatasetGraph(
        """
        (dataset
            (graph
              (:s :p  5)
              (:s :p  7)
            )
            (graph :g1
              (:s :p  7)
              (:s :p 11)
            )
            (graph :g2
              (:s :p  7)
              (:s :p 17)
            )
        )
        """);

        // Set up the test TDB2 dataset
        systemUnderTestDsg = TDBInternal.getDatasetGraphTDB(TDB2Factory.createDataset());
        CopyDSG.copy(referenceDsg, systemUnderTestDsg);
    }

    /** Min/max on the same variable should use skip scan. */
    @Test
    public void test_skipScan_01_spo() {
        expectSkipScan("SELECT (min(?o) AS ?min) (max(?o) AS ?max) { ?s ?p ?o }");
    }

    /** Min/max on the same variable should use skip scan. */
    @Test
    public void test_skipScan_01_gspo() {
        expectSkipScan("SELECT (min(?o) AS ?min) (max(?o) AS ?max) { GRAPH ?g { ?s ?p ?o } }");
    }

    /** Min/max on the same variable should use skip scan. */
    @Test
    public void test_skipScan_01_union() {
        expectSkipScan("SELECT (min(?o) AS ?min) (max(?o) AS ?max) { GRAPH <urn:x-arq:UnionGraph> { ?s ?p ?o } }");
    }

    /** distinct sum/avg */
    @Test
    public void test_skipScan_03() {
        expectSkipScan("SELECT (avg(DISTINCT ?o) AS ?avg) (sum(DISTINCT ?o) AS ?sum) { GRAPH ?g { ?s ?p ?o } }");
    }

    /** Sample should prevent skip scan in order to preserve value distribution. */
    @Test
    public void test_no_skipScan_02() {
        expectNoSkipScan("SELECT (min(?o) AS ?min) (sample(?o) AS ?max) { ?s ?p ?o }");
    }

    /** Mix of distinct / non-distinct aggregator - should not use skip scan. */
    @Test
    public void test_no_skipScan_03() {
        expectNoSkipScan("SELECT (min(?o) AS ?min) (avg(?o) AS ?avg) { ?s ?p ?o }");
    }

    /** Aggregation by different variables - should not use skip scan. */
    @Test
    public void test_no_skipScan_04() {
        expectNoSkipScan("SELECT (count(DISTINCT ?p) AS ?countP) (count(DISTINCT ?o) AS ?countO) { ?s ?p ?o }");
    }

    /** Count of all bindings (not a restricted to a specific variable) - should not use skip scan. */
    @Test
    public void test_no_skipScan_05() {
        expectNoSkipScan("SELECT (count(DISTINCT *) AS ?c) { GRAPH ?g { ?s ?p ?o } }");
    }

    /** Count distinct of a single variable - should use skip scan. */
    @Test
    public void test_skipScan_06() {
        expectSkipScan("SELECT (count(DISTINCT ?p) AS ?c) { GRAPH ?g { ?s ?p ?o } }");
    }

    public void expectSkipScan(String queryString) { test(queryString, true); }
    public void expectNoSkipScan(String queryString) { test(queryString, false); }

    private void test(String queryString, boolean expectSkipScan) {
        Query query = QueryFactory.create(queryString);
        GraphCompareSelectResultExecutableSkipScan executable =
                new GraphCompareSelectResultExecutableSkipScan("test", query, referenceDsg, systemUnderTestDsg, expectSkipScan);
        executable.execute();
    }
}
