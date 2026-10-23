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

import org.junit.jupiter.api.Test;

import org.apache.jena.tdb2.store.DatasetGraphTDB;

/**
 * Finding 2 - {@code OpExecutorTDB2SkipScan:451}.
 *
 * <p>Result rows are built with {@code new BindingNodeId()}, with no parent binding, so every
 * variable carried by the input binding is dropped:</p>
 * <pre>{@code
 * bindingIdIt = Iter.map(recordIt, record -> {
 *     BindingNodeId b = new BindingNodeId();     // no parent
 *     ...
 * });
 * }</pre>
 * <p>The fail-over path executes {@code opExecutor.executeOp(op, <the binding>)} and so does
 * keep them, which makes the two paths disagree.</p>
 *
 * <p>These queries put the skip scan on the right of a sequence/join, so it is evaluated with a
 * non-empty input binding.</p>
 */
public class Test02_InputBindingDropped {

    private static final String DATA = """
        :a  :q :b .
        :s1 :p :o1 .
        :s1 :p :o2 .
        """;

    /** {@code ?x} and {@code ?y} come from the input binding and must survive. */
    @Test
    public void joinWithDistinctSubSelect() {
        DatasetGraphTDB dsg = tdb2(DATA);
        assertSkipScanAgrees(dsg, """
            SELECT * { ?x :q ?y { SELECT DISTINCT ?p { ?s ?p ?o } } }
            """);
    }

    /** The same, with the sub-select naming two variables. */
    @Test
    public void joinWithDistinctSubSelect_twoVars() {
        DatasetGraphTDB dsg = tdb2(DATA);
        assertSkipScanAgrees(dsg, """
            SELECT * { ?x :q ?y { SELECT DISTINCT ?s ?p { ?s ?p ?o } } }
            """);
    }
}
