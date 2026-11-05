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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Objects;

import org.junit.jupiter.api.Test;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb2.TDB2Factory;
import org.apache.jena.tdb2.sys.TDBInternal;

public class TestFindBestIndex {

    private static final String TRIPLE_DATASET = """
        (dataset
            (graph
              (:s :p :oa)
              (:s :p :ob)
            )
            (graph :g1
              (:s :p :oa)
              (:s :p :ob)
            )
            (graph :g2
              (:s :p :oa)
              (:s :p :ob)
            )
        )
        """;

    @Test
    public void testProject_s_01() {
        testIndexSelection("(distinct (project (?s) (bgp (?s ?p ?o))))", "SPO");
    }

    @Test
    public void testProject_p_01() {
        testIndexSelection("(distinct (project (?p) (bgp (?s ?p ?o))))", "POS");
    }

    @Test
    public void testProject_o_01() {
        testIndexSelection("(distinct (project (?o) (bgp (?s ?p ?o))))", "OSP");
    }

    @Test
    public void testProject_sp_01() {
        testIndexSelection("(distinct (project (?s ?p) (bgp (?s ?p ?o))))", "SPO");
    }

    @Test
    public void testProject_so_01() {
        testIndexSelection("(distinct (project (?s ?o) (bgp (?s ?p ?o))))", "OSP");
    }

    @Test
    public void testProject_po_01() {
        testIndexSelection("(distinct (project (?p ?o) (bgp (?s ?p ?o))))", "POS");
    }

    @Test
    public void testProject_spo_01() {
        testIndexSelection("(distinct (project (?s ?p ?o) (bgp (?s ?p ?o))))", "SPO");
    }

    @Test
    public void testNoDistinct_s_01() {
        testIndexSelection("(project (?s) (bgp (?s ?p ?o)))", "SPO");
    }

    @Test
    public void testNoDistinct_p_01() {
        testIndexSelection("(project (?p) (bgp (?s ?p ?o)))", "POS");
    }

    @Test
    public void testAllConstants_01() {
        testIndexSelection("(distinct (project (?s) (bgp (:s :p :o))))", "SPO");
    }

    @Test
    public void testMixedConstants_sp_01() {
        testIndexSelection("(distinct (project (?p) (bgp (:s ?p :o))))", "OSP");
    }

    @Test
    public void testMixedConstants_so_01() {
        testIndexSelection("(distinct (project (?s ?o) (bgp (?s :p ?o))))", "POS");
    }

    @Test
    public void testConstant_first_01() {
        testIndexSelection("(distinct (project (?p ?o) (bgp (:s ?p ?o))))", "SPO");
    }

    private void testIndexSelection(String opStr, String expectedIndexName) {
        DatasetGraph testDsg = TDBInternal.getDatasetGraphTDB(TDB2Factory.createDataset());
        DatasetGraph referenceDsg = SSE.parseDatasetGraph(TRIPLE_DATASET);
        Txn.executeWrite(testDsg, () -> testDsg.addAll(referenceDsg));

        ExecutionContext execCxt = ExecutionContext.create(testDsg);
        Op op = SSE.parseOp(opStr);
        PatternQuery pq = Objects.requireNonNull(PatternQuery.createOrNull$(op));

        SkipScanCandidate candidate = OpExecutorTDB2SkipScan.findBestCandidate(pq, execCxt);
        assertNotNull(candidate);
        assertNotNull(candidate.index());

        String actualIndexName = candidate.index().getName();
        assertTrue(actualIndexName.startsWith(expectedIndexName),
                "Expected index starting with " + expectedIndexName + " but got " + actualIndexName);
    }
}
