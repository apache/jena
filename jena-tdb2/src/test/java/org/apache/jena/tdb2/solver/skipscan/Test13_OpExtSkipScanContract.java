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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.util.NodeIsomorphismMap;
import org.apache.jena.sparql.util.Symbol;
import org.apache.jena.tdb2.sys.SystemTDB;
import org.junit.jupiter.api.Test;

/**
 * Finding 13 - {@code OpExtSkipScan:89}.
 *
 * <pre>{@code
 * @Override public int hashCode() { return 31 * patternQuery.hashCode(); }
 * @Override public boolean equalTo(Op other, NodeIsomorphismMap labelMap) {
 *     return other instanceof OpExtSkipScan o && this.effectiveOp().equalTo(o.effectiveOp(), labelMap);
 * }
 * }</pre>
 * <p>{@code hashCode} goes through {@code PatternQuery.hashCode()}, which is array-identity
 * based (finding 8), while {@code equalTo} compares the effective op. Two structurally identical
 * nodes are therefore equal with different hash codes, which breaks any hash-based use of
 * {@code Op}.</p>
 *
 * <p>Also: the executor is stashed under a bare {@code Symbol.create("opExecutor")} rather than
 * a TDB-namespaced symbol.</p>
 */
public class Test13_OpExtSkipScanContract {

    private static final Node p = NodeFactory.createURI("http://example/p");

    private static OpExtSkipScan op() {
        Node[] tuple = new Node[] {Var.alloc("s"), p, Var.alloc("o")};
        return new OpExtSkipScan(new PatternQuery(true, List.of(Var.alloc("s")), tuple));
    }

    /** Equal ops must have equal hash codes. */
    @Test
    public void equalOpsHaveEqualHashCodes() {
        OpExtSkipScan a = op();
        OpExtSkipScan b = op();
        assertTrue(a.equalTo(b, new NodeIsomorphismMap()),
                   "Two structurally identical OpExtSkipScan nodes are not equal");
        assertEquals(a.hashCode(), b.hashCode(),
                     "Two equal OpExtSkipScan nodes have different hash codes");
    }

    /** Context keys must be namespaced; {@code "opExecutor"} is a bare, collidable name. */
    @Test
    public void opExecutorSymbolIsNamespaced() {
        Symbol symbol = OpExtSkipScan.symOpExecutor;
        assertTrue(symbol.getSymbol().startsWith(SystemTDB.symbolNamespace),
                   () -> "The OpExecutor is stored under an un-namespaced context symbol: "
                       + symbol.getSymbol());
    }
}
