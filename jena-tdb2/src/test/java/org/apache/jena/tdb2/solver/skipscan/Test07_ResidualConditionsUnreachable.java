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

import static org.apache.jena.tdb2.solver.skipscan_3995.SkipScanTestLib.tdb2;
import static org.apache.jena.tdb2.solver.skipscan_3995.SkipScanTestLib.tripleTable;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.apache.jena.atlas.lib.tuple.TupleMap;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.tdb2.solver.skipscan_3995.SkipScanTestLib;
import org.apache.jena.tdb2.store.DatasetGraphTDB;
import org.apache.jena.tdb2.store.tupletable.TupleIndex;
import org.junit.jupiter.api.Test;

// Residual condidions should
// SELECT DISTINCT ?p { :s ?p :foo } should use either:
// (SPO with recheck slot[2]=:foo) or
// OPS (with recheck slot[2]=:s)

/**
 * Finding 7 - {@code OpExecutorTDB2SkipScan:252}.
 *
 * <p>In {@code computeIndexMatch}, the branch that records a constant as a residual condition
 * never clears that slot's bit:</p>
 * <pre>{@code
 * } else {
 *     if ( residualConditions == null )
 *         residualConditions = new ValueFilter[idxLen - j];
 *     residualConditions[numResidualConditions++] = new ValueFilter<>(j, node);
 *     // uncoveredSlotsMask is not updated
 * }
 * }</pre>
 * <p>so {@code uncoveredSlotsMask != 0} still holds at the end and the index is rejected. Any
 * index with a constant after the first variable is therefore unusable, and
 * {@code residualConditions} can never be non-empty - which makes
 * {@code remapValueConditions}, the {@code Iter.filter} over the records, and
 * {@code IndexMatch.numResidualConditions} in the plan comparator unreachable.</p>
 */
public class Test07_ResidualConditionsUnreachable {

    private static final String DATA = """
        :s1 :p  :o1 .
        :s1 :p  :o2 .
        :s2 :p2 :o3 .
        """;

    private static final Node p = NodeFactory.createURI("http://example/p");

    /**
     * Fix-agnostic: residual conditions are a feature of the design, so some pattern and index
     * must produce them. None does.
     */
    @Test
    public void residualConditionsAreReachable() {
        DatasetGraphTDB dsg = tdb2(DATA);
        // Var s = Var.alloc("s");
        Var p = Var.alloc("p");

        List<Node[]> patterns = List.<Node[]>of(new Node[] {
            NodeFactory.createURI("http://example/s1"), p, NodeFactory.createURI("http://example/o1")});
        List<List<Var>> projections = List.of(List.of(p));

        boolean seen = false;
        for ( Node[] pattern : patterns ) {
            for ( List<Var> projection : projections ) {
                TuplePatternSpec spec = TuplePatternSpec.create(pattern, projection);
                for ( TupleIndex index : tripleTable(dsg).getTupleTable().getIndexes() ) {
                    TupleMap tupleMap = index.getMapping();
                    IndexMatch match = OpExecutorTDB2SkipScan.computeIndexMatch(tupleMap, spec);
                    if ( match != null && match.numResidualConditions() > 0 )
                        seen = true;
                }
            }
        }
        assertTrue(seen, "No pattern/index combination ever produces a residual condition, so the "
                       + "whole residual-condition code path is unreachable");
    }

    /**
     * The concrete case: for {@code (?s :p ?o)} projecting {@code ?s}, SPO offers an (s,p) prefix
     * with a filter on the predicate column, but the index is rejected outright.
     */
    @Test
    public void spoIsUsableForAConstantPredicate() {
        DatasetGraphTDB dsg = tdb2(DATA);
        Var s = Var.alloc("s");
        Var o = Var.alloc("o");

        TuplePatternSpec spec = TuplePatternSpec.create(new Node[] {s, p, o}, List.of(s));
        TupleMap spo = SkipScanTestLib.tripleIndex(dsg, "SPO").getMapping();

        assertNotNull(OpExecutorTDB2SkipScan.computeIndexMatch(spo, spec),
                      "SPO is unusable for (?s :p ?o) projecting ?s, although the constant predicate "
                    + "could be applied as a residual condition");
    }
}
