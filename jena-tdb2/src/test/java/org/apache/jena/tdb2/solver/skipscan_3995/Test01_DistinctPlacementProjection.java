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

package org.apache.jena.tdb2.solver.skipscan_3995;

import static org.apache.jena.tdb2.solver.skipscan_3995.SkipScanTestLib.assertSkipScanAgrees;
import static org.apache.jena.tdb2.solver.skipscan_3995.SkipScanTestLib.tdb2;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.Test;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVars;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.tdb2.solver.skipscan.TransformDistinctPlacement;
import org.apache.jena.tdb2.store.DatasetGraphTDB;

/**
 * Finding 1 - {@code TransformDistinctPlacement:136}.
 *
 * <p>After pushing DISTINCT below an {@code OpExtend}, the re-applied projection is built from
 * the extend-defined variables alone:</p>
 * <pre>{@code
 * List<Var> extendVars = opExtend.getVarExprList().getVars();
 * if ( !projectVars.containsAll(extendVars) )
 *     result = new OpProject(result, new ArrayList<>(extendVars));
 * }</pre>
 * <p>so every pattern variable - the DISTINCT and GROUP BY variables included - is projected
 * away. The transform must preserve the variables visible from the operator it replaces.</p>
 */
public class Test01_DistinctPlacementProjection {

    private static final String DATA = """
        :s1 :p  :o1 .
        :s1 :p  :o2 .
        :s2 :p2 :o3 .
        """;

    /**
     * The transform replaces the OpDistinct, so the result must make the same variables visible.
     * Here that is {@code ?o}: the re-applied projection keeps only {@code ?z} instead.
     */
    @Test
    public void visibleVarsArePreserved_extend() {
        Op input = SSE.parseOp("(distinct (project (?o) (extend ((?z ?o)) (bgp (?s :p ?o)))))");
        Op output = Transformer.transform(new TransformDistinctPlacement(), input);

        Set<Var> before = OpVars.visibleVars(input);
        Set<Var> after = OpVars.visibleVars(output);
        assertEquals(before, after,
                     () -> "Pushing DISTINCT over the extend changed the visible variables."
                         + "\n  Input:  " + input
                         + "\n  Output: " + output);
    }

    /** {@code SELECT DISTINCT ?o { ?s :p ?o BIND(?o AS ?z) }} must still bind {@code ?o}. */
    @Test
    public void distinctOverBind() {
        DatasetGraphTDB dsg = tdb2(DATA);
        assertSkipScanAgrees(dsg, "SELECT DISTINCT ?o { ?s :p ?o BIND(?o AS ?z) }");
    }

    /** The same defect reached through the OpGroup rewrite: the GROUP BY variable is lost. */
    @Test
    public void groupByOverBind() {
        DatasetGraphTDB dsg = tdb2(DATA);
        assertSkipScanAgrees(dsg, """
            SELECT ?p (MIN(?o) AS ?m) { ?s ?p ?o BIND(LANG(?o) AS ?l) } GROUP BY ?p
            """);
    }
}
