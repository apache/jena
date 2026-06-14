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

package org.apache.jena.tdb2.solver.index;

import java.util.Comparator;

import org.apache.jena.graph.Node;

record IndexMatch(
    boolean canDoDirectDistinct,
    ValueFilter<Node>[] residualConditions,
    int numResidualConditions,
    int keyLen,
    int valLen)
{
    int numNeededSlots() {
        return keyLen + valLen;
    }

    public static final Comparator<IndexMatch> COMPARATOR = Comparator.nullsLast(Comparator
        .comparingInt(IndexMatch::numResidualConditions)
        .thenComparing(Comparator.comparing(IndexMatch::canDoDirectDistinct).reversed())
        .thenComparing(IndexMatch::numNeededSlots));
}
