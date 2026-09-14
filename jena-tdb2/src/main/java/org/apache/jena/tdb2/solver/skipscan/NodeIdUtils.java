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

import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.atlas.lib.tuple.TupleFactory;
import org.apache.jena.tdb2.store.NodeId;
import org.apache.jena.tdb2.store.NodeIdFactory;
import org.apache.jena.tdb2.store.NodeIdType;

class NodeIdUtils {
    /** Minimum value for a NodeId. Used as lower bound in range retrievals. */
    private static final NodeId NodeIdMin = NodeIdFactory.createPtr(0);

    /** Maximum value for a NodeId. Used as upper bound in range retrievals. */
    private static final NodeId NodeIdMax = NodeId.createRaw(NodeIdType.SPECIAL, Long.MAX_VALUE);

    public static Tuple<NodeId> anyToMin(Tuple<NodeId> tuple) {
        int n = tuple.len();
        NodeId[] arr = new NodeId[n];
        for (int i = 0; i < n; ++i) {
            NodeId x = tuple.get(i);
            arr[i] = NodeId.isAny(x) ? NodeIdMin : x;
        }
        return TupleFactory.create(arr);
    }

    public static Tuple<NodeId> anyToMax(Tuple<NodeId> tuple) {
        int n = tuple.len();
        NodeId[] arr = new NodeId[n];
        for (int i = 0; i < n; ++i) {
            NodeId x = tuple.get(i);
            arr[i] = NodeId.isAny(x) ? NodeIdMax : x;
        }
        return TupleFactory.create(arr);
    }
}
