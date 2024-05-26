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

package org.apache.jena.tdb2.store.nodetable;

import java.util.Iterator;
import java.util.List;

import org.apache.jena.atlas.lib.Closeable;
import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.atlas.lib.Sync;
import org.apache.jena.graph.Node;
import org.apache.jena.tdb2.store.NodeId;

/** Node table - conceptually a two way mapping of Node{@literal <->}NodeId
 *  where Nodes can be stored and a NodeId allocated
 *  @see NodeId
 */

public interface NodeTable extends Sync, Closeable
{
    /** Store the node in the node table (if not already present) and return the allocated Id. */
    public NodeId getAllocateNodeId(Node node);

    /** Look up node and return the NodeId - return NodeId.NodeDoesNotExist if not found */
    public NodeId getNodeIdForNode(Node node);

    /** Look up node id and return the Node - return null if not found */
    public Node getNodeForNodeId(NodeId id);

    /** Test whether the node table contains an entry for node */
    public boolean containsNode(Node node);

    /** Test whether the node table contains an entry for node */
    public boolean containsNodeId(NodeId nodeId);

    /** Bulk mapping from {@code Node} to {@code NodeId}, with allocation
     * if the {@code withAllocation} is true.
     * The returned list aligns with the input list.
     */
    public List<NodeId> bulkNodeToNodeId(List<Node> nodes, boolean withAllocation);

    /** Bulk mapping from {@code NodeId} to {@code Node} */
    public List<Node> bulkNodeIdToNode(List<NodeId> nodeIds);

    /** Bulk lookup
    public List<NodeId> getAllocateNodeIdBulk(List<Node> nodes);

    /** Iterate over all nodes (not necessarily fast).  Does not include inlined NodeIds */
    public Iterator<Pair<NodeId, Node>> all();

    /** Anything there? */
    public boolean isEmpty();

    /** Return a NodeTable if this instance wraps another, else return null */
    public NodeTable wrapped();

    /** Return the base NodeTable, the end of the wrapped chain */
    default public NodeTable baseNodeTable() {
        NodeTable nt = this;
        NodeTable nt2 = null;
        while ( (nt2 = nt.wrapped()) != null ) {
            nt = nt2;
        }
        return nt;
    }
}
