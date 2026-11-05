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

import static org.apache.jena.tdb2.solver.skipscan_3995.SkipScanTestLib.assertSkipScanAgrees;
import static org.apache.jena.tdb2.solver.skipscan_3995.SkipScanTestLib.tdb2;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.tdb2.store.DatasetGraphTDB;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Finding 5 - {@code TuplePatternSpec:93}.
 *
 * <p>{@code equalityLinks} and {@code neededSlots} are sized {@code n}, the tuple length:</p>
 * <pre>{@code
 * EqualityLink[] equalityLinks = null;   // ... new EqualityLink[n]
 * int[] neededSlots = new int[n];
 * }</pre>
 * <p>but the nested i/j loop appends one entry per pair of equal slots - up to n(n-1)/2 - so a
 * variable occurring three or more times overflows both arrays.</p>
 */
public class Test05_EqualityLinkArrayOverflow {

    private static final String DATA = """
        :same :same :same .
        :s1   :p    :o1 .
        :same { :same :same :same }
        """;

    /** Three occurrences of one variable in a triple pattern. */
    @Test
    public void distinct_tripleWithOneVarInEverySlot() {
        DatasetGraphTDB dsg = tdb2(DATA);
        assertSkipScanAgrees(dsg, "SELECT DISTINCT ?s { ?s ?s ?s }");
    }

    /** Four occurrences, in a quad pattern. */
    @Test
    @Disabled("Skip scan not (yet) expected to be used for effectively: SELECT * { SELECT DISTINCT ?s { ?s [] [] } FILTER EXISTS { ?s ?s ?s } }")
    public void distinct_quadWithOneVarInEverySlot() {
        DatasetGraphTDB dsg = tdb2(DATA);
        assertSkipScanAgrees(dsg, "SELECT DISTINCT ?s { GRAPH ?s { ?s ?s ?s } }");
    }

    /** The same, directly at {@code TuplePatternSpec.create}. */
    @Test
    public void tuplePatternSpec_repeatedVariable() {
        Var s = Var.alloc("s");
        Node[] triple = new Node[] {s, s, s};
        assertDoesNotThrow(() -> TuplePatternSpec.create(triple, List.of(s)),
                           "TuplePatternSpec.create overflows its helper arrays for (?s ?s ?s)");

        Node[] quad = new Node[] {s, s, s, s};
        assertDoesNotThrow(() -> TuplePatternSpec.create(quad, List.of(s)),
                           "TuplePatternSpec.create overflows its helper arrays for (?s ?s ?s ?s)");
    }
}
