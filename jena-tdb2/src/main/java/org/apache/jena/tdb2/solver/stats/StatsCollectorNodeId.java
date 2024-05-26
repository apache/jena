/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.tdb2.solver.stats;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.graph.NodeConst;
import org.apache.jena.tdb2.store.NodeId;
import org.apache.jena.tdb2.store.nodetable.NodeTable;

/** Statistics collector, aggregates based on NodeId */
public class StatsCollectorNodeId extends StatsCollectorBase<NodeId> {
    private NodeTable nodeTable;

    public StatsCollectorNodeId(NodeTable nodeTable) {
        super(findRDFType(nodeTable));
        this.nodeTable = nodeTable;
    }

    private static NodeId findRDFType(NodeTable nodeTable) {
        // It may not exist.
        NodeId nodeId = nodeTable.getNodeIdForNode(NodeConst.nodeRDFType);
        if ( NodeId.isDoesNotExist(nodeId) )
            return null;
        return nodeId;
    }

    @Override
    protected Map<Node, Long> convert(Map<NodeId, Long> stats) {
        // Predicate -> Count
        Map<Node, Long> statsNodes = new HashMap<>(1000);
        for ( NodeId p : stats.keySet() ) {
            Node n = nodeTable.getNodeForNodeId(p);
            statsNodes.put(n, stats.get(p));
        }
        return statsNodes;
    }
}
