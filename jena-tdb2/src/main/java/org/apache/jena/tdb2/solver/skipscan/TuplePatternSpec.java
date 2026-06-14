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

import java.util.Arrays;
import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;

/**
 * projs maps slot to the varMap.
 */
record TuplePatternSpec(
        Node[] tuple,
        List<Var> projection, // JoinKey is a list with unique elements.
        VarMap[] varMaps,
        int[] projs,        // array of projected indices - get the var using tuple[projs[i]]
        EqualityLink[] equalityLinks,
        int neededSlotsMask) { // Omits the indices of unconstrained, undistinguished var.

    public Var getVar(int varId) {
        return varMaps[varId].var();
    }

    /** Get the index of the variable or -1 if absent. */
    public int getIdxVar(Var var) {
        for (VarMap e : varMaps) {
            if (e.var().equals(var)) {
                return e.idx();
            }
        }
        return -1;
    }

    @Override
    public String toString() {
        return "IndexQuerySpec [tuple=" + Arrays.toString(tuple) + ", varMaps=" + Arrays.toString(varMaps) + ", projs="
                + Arrays.toString(projs) + ", equalityLinks="
                + Arrays.toString(equalityLinks) + ", neededSlots=" + Integer.toBinaryString(neededSlotsMask) + "]";
    }

    public static TuplePatternSpec create(Node[] quad, List<Var> projection) {
        // Helper arrays are created with maximum possible size here.
        int n = quad.length;

        // varMap will only holds vars that are present in the tuple.
        VarMap[] varMaps = new VarMap[n];
        int numVarMaps = 0;

        EqualityLink[] equalityLinks = null;
        int numEqualityLinks = 0;

        int[] projs = new int[n];
        int numProjs = 0;

        // Needed slots: those that are projected or participate in an equality condition with a constant or another column.
        int[] neededSlots = new int[n];
        int numNeededSlots = 0;

        for (int i = 0; i < n; ++i) {
            Node node = quad[i];
            if (node.isVariable()) {
                boolean alreadySeen = false;
                for (int j = 0; j < i; ++j) {
                    Node prevNode = quad[j];
                    if (node.equals(prevNode)) {
                        if (equalityLinks == null) {
                            equalityLinks = new EqualityLink[n];
                        }
                        equalityLinks[numEqualityLinks++] = new EqualityLink(i, j);

                        // Add i as needed
                        neededSlots[numNeededSlots++] = i;
                        alreadySeen = true;

                        // If the var was not projected then we now need to declare it as needed!
                        if (!projection.contains(node)) {
                            int i2 = indexOf(neededSlots, j, 0, numNeededSlots);
                            if (i2 == -1) {
                                neededSlots[numNeededSlots++] = j;
                            }
                        }
                    }
                }

                if (!alreadySeen) {
                    Var v = Var.alloc(node);
                    varMaps[numVarMaps] = new VarMap(i, v);

                    if (projection.contains(v)) {
                        projs[numProjs++] = i;
                        neededSlots[numNeededSlots++] = i;
                    }

                    ++numVarMaps;
                }
            } else {
                // Mark the slot as needed because it needs to be post-filtered by a constant.
                neededSlots[numNeededSlots++] = i;
            }
        }

        int neededSlotMask = 0;
        for (int i = 0; i < numNeededSlots; ++i) {
            neededSlotMask |= 1 << neededSlots[i];
        }

        TuplePatternSpec result = new TuplePatternSpec(
                quad,
                projection,
                Arrays.copyOf(varMaps, numVarMaps),
                Arrays.copyOf(projs, numProjs),
                equalityLinks == null ? null : Arrays.copyOf(equalityLinks, numEqualityLinks),
                neededSlotMask);
        return result;
    }

    /** Find an item in a certain sub-range. */
    private static int indexOf(int[] arr, int valueToFind, int startIndex, int endIndex) {
        for (int i = startIndex; i < endIndex; ++i) {
            if (arr[i] == valueToFind) {
                return i;
            }
        }
        return -1;
    }
}
