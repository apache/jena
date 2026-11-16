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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.jena.atlas.lib.tuple.TupleMap;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb2.store.DatasetGraphTDB;
import org.apache.jena.tdb2.store.NodeId;
import org.apache.jena.tdb2.store.nodetable.NodeTable;
import org.apache.jena.tdb2.store.tupletable.TupleIndex;
import org.junit.jupiter.api.Test;

/**
 * Finding 10 - {@code OpExecutorTDB2SkipScan:582}.
 *
 * <pre>{@code
 * int slot = tupleMap.unmapIdx(condition.idx());
 * NodeId nodeId = nodeTable.getNodeIdForNode(condition.value());
 * if ( nodeId == null )
 *     return null;
 * }</pre>
 * <p>Two problems. First, {@code condition.idx()} is already an index-space column - unlike
 * {@code projs} and {@code equalityLinks}, which hold tuple slots, the {@code j} stored by
 * {@code computeIndexMatch} is the index position - and the filter reads it back with
 * {@code extractNodeId(recordKey, idx)}, also index-space. Remapping it points the filter at a
 * different column. Second, {@code getNodeIdForNode} returns {@code NodeId.NodeDoesNotExist},
 * never {@code null}, so an unknown constant yields a filter that matches nothing instead of
 * short-circuiting to an empty result.</p>
 *
 * <p>Latent while finding 7 keeps {@code residualConditions} unreachable; live as soon as it is
 * fixed.</p>
 */
public class Test10_RemapValueConditions {

    private static final String DATA = """
        :s1 :p :o1 .
        :s1 :p :o2 .
        """;

    /** The column of a residual condition is already in index space: it must not be remapped. */
    @Test
    public void columnIsNotRemapped() {
        DatasetGraphTDB dsg = tdb2(DATA);
        NodeTable nodeTable = tripleTable(dsg).getNodeTable();
        Node o1 = NodeFactory.createURI("http://example/o1");

        Txn.executeRead(dsg, () -> {
            for ( TupleIndex index : tripleTable(dsg).getTupleTable().getIndexes() ) {
                TupleMap tupleMap = index.getMapping();
                for ( int column = 0; column < tupleMap.length(); column++ ) {
                    ValueFilter<NodeId>[] mapped = OpExecutorTDB2SkipScan.remapValueConditions(
                        new int[] {column}, new Node[] {o1, o1, o1}, nodeTable);
                    assertNotNull(mapped);
                    final int expected = column;
                    assertEquals(expected, mapped[0].idx(),
                                 () -> "Residual condition column " + expected + " of index " + index.getName()
                                     + " was remapped; the filter would read a different column");
                }
            }
        });
    }

    /** "Returns null if any condition could not be mapped" - an absent term has no NodeId. */
    @Test
    public void unknownConstantGivesNull() {
        DatasetGraphTDB dsg = tdb2(DATA);
        NodeTable nodeTable = tripleTable(dsg).getNodeTable();
        Node absent = NodeFactory.createURI("http://example/not-in-the-database");

        Txn.executeRead(dsg, () -> {
            ValueFilter<NodeId>[] mapped = OpExecutorTDB2SkipScan.remapValueConditions(
                new int[] {0}, new Node[] {absent}, nodeTable);
            assertNull(mapped,
                       "remapValueConditions must return null for a constant with no NodeId; "
                     + "getNodeIdForNode returns NodeId.NodeDoesNotExist, never null");
        });
    }
}
