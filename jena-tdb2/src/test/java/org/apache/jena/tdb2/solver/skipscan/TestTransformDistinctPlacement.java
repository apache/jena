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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Transform;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.sse.SSE;

/**
  * Tests for TransformDistinctPlacement - the transform that pushes DISTINCT operations
  * down through the algebra tree when it's safe to do so based on variable coverage.
  * <p>
  * The transform pushes DISTINCT through stable filters and extends when the
  * variables mentioned in those operations are fully covered by the sub-operator.
  * If expressions are unstable (non-deterministic),
  * the DISTINCT is not pushed and remains at the current level.
  * <p>
  * Note: When a variable mentioned in a filter/extend is not present in the subOp,
  * the variable is undefined. Pushing DISTINCT is still safe in this case because
  * the undefined variable's evaluation doesn't depend on subOp cardinality - the
  * result will be the same (undefined/false) regardless of duplicate removal.
  */
public class TestTransformDistinctPlacement
{
    private Transform transform = new TransformDistinctPlacement();

    // ---- Filters ----

    @Test
    public void pushThroughFilter_stable_fullCoverage_01() {
        // Should push DISTINCT through filter when all vars in filter are covered by subOp
        // ?s, ?p, ?o are all provided by bgp, so filter can be pushed down
        String input = "(distinct (filter (= ?o 1) (bgp (?s ?p ?o))))";
        String expected = "(filter (= ?o 1) (distinct (bgp (?s ?p ?o))))";
        testTransform(input, expected);
    }

    @Test
    public void pushThroughFilter_stable_fullCoverage_02() {
        // Multiple conditions, all vars covered
        String input = "(distinct (filter (&& (= ?s <s1>) (= ?o 1)) (bgp (?s ?p ?o))))";
        String expected = "(filter (&& (= ?s <s1>) (= ?o 1)) (distinct (bgp (?s ?p ?o))))";
        testTransform(input, expected);
    }

    @Test
    public void pushThroughFilter_stable_function_01() {
        // STR() is a stable function (deterministic) - should push DISTINCT
        String input = "(distinct (filter (str(?o)) (bgp (?s ?p ?o))))";
        String expected = "(filter (str(?o)) (distinct (bgp (?s ?p ?o))))";
        testTransform(input, expected);
    }

    @Test
    public void pushThroughFilter_partialCoverage_01() {
        // Filter mentions ?x which is NOT in subOp - DISTINCT should still be pushed
        // because ?x being undefined doesn't depend on subOp cardinality
        String input = "(distinct (filter (= ?x 1) (bgp (?s ?p ?o))))";
        String expected = "(filter (= ?x 1) (distinct (bgp (?s ?p ?o))))";
        testTransform(input, expected);
    }

    // ---- Extends (BIND/ASSIGN) ----

    @Test
    public void pushThroughExtend_stable_fullCoverage_01() {
        // Should push DISTINCT through stable extend when all expression vars are covered
        // ?o is provided by bgp, so the extend can be pushed down
        String input = "(distinct (extend ((?x ?o)) (bgp (?s ?p ?o))))";
        String expected = "(extend ((?x ?o)) (distinct (bgp (?s ?p ?o))))";
        testTransform(input, expected);
    }

    @Test
    public void pushThroughExtend_stable_fullCoverage_02() {
        // Multiple assignments, all vars covered by subOp
        String input = "(distinct (extend ((?x ?o) (?y ?p)) (bgp (?s ?p ?o))))";
        String expected = "(extend ((?x ?o) (?y ?p)) (distinct (bgp (?s ?p ?o))))";
        testTransform(input, expected);
    }

    @Test
    public void pushThroughExtend_stable_function_01() {
        // LANG() is a stable function (deterministic) - should push DISTINCT
        String input = "(distinct (extend ((?x (lang ?o))) (bgp (?s ?p ?o))))";
        String expected = "(extend ((?x (lang ?o))) (distinct (bgp (?s ?p ?o))))";
        testTransform(input, expected);
    }

    @Test
    public void pushThroughExtend_partialCoverage_01() {
        // Extend mentions ?z which is NOT in subOp - DISTINCT should be pushed
        // because ?z being undefined doesn't depend on subOp cardinality
        String input = "(distinct (extend ((?x ?z)) (bgp (?s ?p ?o))))";
        String expected = "(extend ((?x ?z)) (distinct (bgp (?s ?p ?o))))";
        testTransform(input, expected);
    }

    // ---- Coverage analysis ----

    @Test
    public void coverage_full_01() {
        // All variables in filter are covered by the bgp - should push
        // ?s, ?p, ?o all in subOp
        String input = "(distinct (filter (= ?s ?p) (bgp (?s ?p ?o))))";
        String expected = "(filter (= ?s ?p) (distinct (bgp (?s ?p ?o))))";
        testTransform(input, expected);
    }

    @Test
    public void coverage_partial_01() {
        // Partial coverage: filter mentions ?s, ?p (in subOp) and ?x (not in subOp)
        // DISTINCT should be pushed because ?x being undefined doesn't depend on subOp cardinality
        String input = "(distinct (filter (&& (= ?s ?p) (= ?x 1)) (bgp (?s ?p ?o))))";
        String expected = "(filter (&& (= ?s ?p) (= ?x 1)) (distinct (bgp (?s ?p ?o))))";
        testTransform(input, expected);
    }

    // ---- Nested structures ----

    @Test
    public void nested_transforms_01() {
        // Multiple transforms in sequence - both filters should be pushed
        String input = "(distinct (filter (= ?s <s1>) (filter (= ?o 1) (bgp (?s ?p ?o)))))";
        String expected = "(filter (= ?s <s1>) (filter (= ?o 1) (distinct (bgp (?s ?p ?o)))))";
        testTransform(input, expected);
    }

    @Test
    public void nested_transforms_02() {
        // Mix of filter and extend
        String input = "(distinct (filter (= ?s <s1>) (extend ((?x ?o)) (bgp (?s ?p ?o)))))";
        String expected = "(filter (= ?s <s1>) (extend ((?x ?o)) (distinct (bgp (?s ?p ?o)))))";
        testTransform(input, expected);
    }

    @Test
    public void nested_distinct_01() {
        // Nested distinct operations
        String input = "(distinct (distinct (bgp (?s ?p ?o))))";
        String expected = "(distinct (distinct (bgp (?s ?p ?o))))";
        testTransform(input, expected);
    }

    // ---- Complex expressions ----

    @Test
    public void complexFilter_functionCall_01() {
        // Filter with stable function STR() that depends on subOp vars
        // STR() is deterministic so DISTINCT should be pushed
        String input = "(distinct (filter (str(?o)) (bgp (?s ?p ?o))))";
        String expected = "(filter (str(?o)) (distinct (bgp (?s ?p ?o))))";
        testTransform(input, expected);
    }

    @Test
    public void complexFilter_multipleOps_01() {
        // Complex filter with stable function STR() and equality
        // Both are stable so DISTINCT should be pushed
        String input = "(distinct (filter (&& (str(?o)) (= ?s ?p)) (bgp (?s ?p ?o))))";
        String expected = "(filter (&& (str(?o)) (= ?s ?p)) (distinct (bgp (?s ?p ?o))))";
        testTransform(input, expected);
    }

    @Test
    public void complexExtend_function_01() {
        // Extend with stable function LANG()
        // ?o is covered and LANG() is deterministic so DISTINCT should be pushed
        String input = "(distinct (extend ((?x (lang ?o))) (bgp (?s ?p ?o))))";
        String expected = "(extend ((?x (lang ?o))) (distinct (bgp (?s ?p ?o))))";
        testTransform(input, expected);
    }

    // ---- Edge cases ----

    @Test
    public void simpleBGP_01() {
        // Simple BGP with distinct - no filter/extend to push through
        String input = "(distinct (bgp (?s ?p ?o)))";
        String expected = "(distinct (bgp (?s ?p ?o)))";
        testTransform(input, expected);
    }

    @Test
    public void noSubOp_01() {
        // DISTINCT with table unit - no subOp to push through
        String input = "(distinct (table unit))";
        String expected = "(distinct (table unit))";
        testTransform(input, expected);
    }

    @Test
    public void unionScenario_01() {
        // DISTINCT over union - should not push through union
        String input = "(distinct (union (bgp (?s ?p ?o)) (bgp (?s ?p1 ?o1))))";
        String expected = "(distinct (union (bgp (?s ?p ?o)) (bgp (?s ?p1 ?o1))))";
        testTransform(input, expected);
    }

    @Test
    public void joinScenario_01() {
        // DISTINCT over join with filter on one side
        String input = "(distinct (filter (= ?o 1) (join (bgp (?s ?p ?o)) (bgp (?s ?p2 ?o2)))))";
        String expected = "(filter (= ?o 1) (distinct (join (bgp (?s ?p ?o)) (bgp (?s ?p2 ?o2)))))";
        testTransform(input, expected);
    }

    // ---- Integration scenarios ----

    @Test
    public void integration_filter_extend_01() {
        // Combined filter and extend scenario
        String input = "(distinct (filter (= ?p <p1>) (extend ((?x ?o)) (bgp (?s ?p ?o)))))";
        String expected = "(filter (= ?p <p1>) (extend ((?x ?o)) (distinct (bgp (?s ?p ?o)))))";
        testTransform(input, expected);
    }

    @Test
    public void integration_project_filter_01() {
        // Project with filter inside distinct
        String input = "(distinct (project (?s) (filter (= ?p <p1>) (bgp (?s ?p ?o)))))";
        // The filter mentions ?p which is not in the project vars (?s only)
        // So it should not be pushed
        String expected = "(distinct (project (?s) (filter (= ?p <p1>) (bgp (?s ?p ?o)))))";
        testTransform(input, expected);
    }

    @Test
    public void leftJoinScenario_01() {
        // DISTINCT over left join
        String input = "(distinct (leftjoin (bgp (?s ?p ?o)) (bgp (?s ?p2 ?o2))))";
        String expected = "(distinct (leftjoin (bgp (?s ?p ?o)) (bgp (?s ?p2 ?o2))))";
        testTransform(input, expected);
    }

    // ---- Unstable functions (should NOT push DISTINCT) ----

    @Test
    public void pushThroughFilter_unstable_rand_01() {
        // RAND() is unstable - should NOT push DISTINCT
        String input = "(distinct (filter (rand) (bgp (?s ?p ?o))))";
        String expected = "(distinct (filter (rand) (bgp (?s ?p ?o))))";
        testTransform(input, expected);
    }

    @Test
    public void pushThroughExtend_unstable_rand_01() {
        // RAND() is unstable - should NOT push DISTINCT
        String input = "(distinct (extend ((?x (rand))) (bgp (?s ?p ?o))))";
        String expected = "(distinct (extend ((?x (rand))) (bgp (?s ?p ?o))))";
        testTransform(input, expected);
    }

    @Test
    public void pushThroughFilter_unstable_uuid_01() {
        // UUID() is unstable - should NOT push DISTINCT
        String input = "(distinct (filter (uuid) (bgp (?s ?p ?o))))";
        String expected = "(distinct (filter (uuid) (bgp (?s ?p ?o))))";
        testTransform(input, expected);
    }

    @Test
    public void pushThroughExtend_unstable_uuid_01() {
        // UUID() is unstable - should NOT push DISTINCT
        String input = "(distinct (extend ((?x (uuid))) (bgp (?s ?p ?o))))";
        String expected = "(distinct (extend ((?x (uuid))) (bgp (?s ?p ?o))))";
        testTransform(input, expected);
    }

    @Test
    public void pushThroughFilter_unstable_bnode_01() {
        // BNODE() is unstable - should NOT push DISTINCT
        String input = "(distinct (filter (bnode) (bgp (?s ?p ?o))))";
        String expected = "(distinct (filter (bnode) (bgp (?s ?p ?o))))";
        testTransform(input, expected);
    }

    @Test
    public void pushThroughExtend_unstable_bnode_01() {
        // BNODE() is unstable - should NOT push DISTINCT
        String input = "(distinct (extend ((?x (bnode))) (bgp (?s ?p ?o))))";
        String expected = "(distinct (extend ((?x (bnode))) (bgp (?s ?p ?o))))";
        testTransform(input, expected);
    }

    @Test
    public void pushThroughFilter_mixed_stable_unstable_01() {
        // Mix of stable (str) and unstable (rand) - should NOT push DISTINCT
        String input = "(distinct (filter (&& (str(?o)) (rand)) (bgp (?s ?p ?o))))";
        String expected = "(distinct (filter (&& (str(?o)) (rand)) (bgp (?s ?p ?o))))";
        testTransform(input, expected);
    }

    @Test
    public void pushThroughExtend_mixed_stable_unstable_01() {
        // Mix of stable (lang) and unstable (uuid) - should NOT push DISTINCT
        String input = "(distinct (extend ((?x (&& (lang ?o) (uuid)))) (bgp (?s ?p ?o))))";
        String expected = "(distinct (extend ((?x (&& (lang ?o) (uuid)))) (bgp (?s ?p ?o))))";
        testTransform(input, expected);
    }

    @Test
    public void pushThroughProject_01() {
        String input = "(distinct (project (?s ?p) (project (?p) (bgp (?s ?p ?o)))))";
        String expected = "(distinct (project (?p) (bgp (?s ?p ?o))))";
        testTransform(input, expected);
    }

    @Test
    public void pushThroughProject_02() {
        String input = "(distinct (project (?s ?p) (filter (bound(?p)) (project (?p) (bgp (?s ?p ?o))))))";
        String expected = "(filter (bound(?p)) (distinct (project (?p) (bgp (?s ?p ?o)))))";
        testTransform(input, expected);
    }

    @Test
    public void pushThroughProject_02b() {
        String input = "(distinct (project (?s ?p) (filter (bound(?s)) (project (?p) (bgp (?s ?p ?o))))))";
        String expected = "(filter (bound(?s)) (distinct (project (?p) (bgp (?s ?p ?o)))))";
        testTransform(input, expected);
    }

    @Test
    public void pushThroughProject_03() {
        String input = "(distinct (project (?s ?p) (filter (bound(?s)) (project (?p ?x) (bgp (?s ?p ?o))))))";
        String expected = "(filter (bound(?s)) (distinct (project (?p) (bgp (?s ?p ?o)))))";
        testTransform(input, expected);
    }

    private void testTransform(String input, String expected) {
        Op inputOp = SSE.parseOp(input);
        Op expectedOp = SSE.parseOp(expected);
        Op result = Transformer.transform(transform, inputOp);
        assertEquals(expectedOp, result, "Transform result should match expected structure");
    }
}
