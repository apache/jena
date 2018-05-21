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

package org.apache.jena.tdb2.store.nodetable ;

import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.atlas.lib.Pair ;
import org.apache.jena.graph.Node ;
import org.apache.jena.tdb2.store.NodeId;

public class NodeTableWrapper implements NodeTable {
    protected final NodeTable nodeTable ;

    @Override
    public final NodeTable wrapped() {
        return nodeTable ;
    }

    protected NodeTableWrapper(NodeTable nodeTable) {
        this.nodeTable = nodeTable ;
    }

    @Override
    public NodeId getAllocateNodeId(Node node) {
        return nodeTable.getAllocateNodeId(node) ;
    }

    @Override
    public NodeId getNodeIdForNode(Node node) {
        return nodeTable.getNodeIdForNode(node) ;
    }

    @Override
    public Node getNodeForNodeId(NodeId id) {
        return nodeTable.getNodeForNodeId(id) ;
    }

    @Override
    public boolean containsNode(Node node) {
        return nodeTable.containsNode(node) ;
    }

    @Override
    public boolean containsNodeId(NodeId nodeId) {
        return nodeTable.containsNodeId(nodeId) ;
    }

    @Override
    public List<NodeId> bulkNodeToNodeId(List<Node> nodes, boolean withAllocation) {
        return nodeTable.bulkNodeToNodeId(nodes, withAllocation) ;
    }

    @Override
    public List<Node> bulkNodeIdToNode(List<NodeId> nodeIds) {
        return nodeTable.bulkNodeIdToNode(nodeIds) ;
    }

    @Override
    public Iterator<Pair<NodeId, Node>> all() {
        return nodeTable.all() ;
    }

    @Override
    public boolean isEmpty() {
        return nodeTable.isEmpty() ;
    }

    @Override
    public void sync() {
        nodeTable.sync() ;
    }

    @Override
    public void close() {
        nodeTable.close() ;
    }
}
