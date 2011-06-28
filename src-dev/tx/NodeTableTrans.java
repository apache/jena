/**
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

package tx;

import java.util.Iterator ;
import java.util.Map ;

import org.openjena.atlas.lib.Pair ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.tdb.base.objectfile.ObjectFile ;
import com.hp.hpl.jena.tdb.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.store.NodeId ;

public class NodeTableTrans implements NodeTable
{
    private final NodeTable other ;
    private final long offset ;
    private final ObjectFile journal ;
    
    private Map<Node, NodeId> node2NodeId ;
    private Map<NodeId, Node> nodeId2Node ;

    public NodeTableTrans(NodeTable sub, long offset, ObjectFile journal)
    {
        this.other = sub ;
        this.offset = offset ;
        this.journal = journal ;
    }

    @Override
    public NodeId getAllocateNodeId(Node node)
    {
        NodeId nodeId = getNodeIdForNode(node) ;
        if ( ! NodeId.doesNotExist(nodeId) )
            return nodeId ;
        // add to journal
        nodeId = allocate(node) ;
        // Convert 
        long x = nodeId.getId() ;
        nodeId = new NodeId(x+offset) ;
        node2NodeId.put(node, nodeId) ;
        nodeId2Node.put(nodeId, node) ;
        
        return nodeId ;
    }
    
    @Override
    public NodeId getNodeIdForNode(Node node)
    {
        NodeId nodeId = node2NodeId.get(node) ;
        if ( nodeId != null )
            return nodeId ;
        nodeId = other.getNodeIdForNode(node) ;
        return nodeId ;
    }

    @Override
    public Node getNodeForNodeId(NodeId id)
    {
        Node node = nodeId2Node.get(id) ;
        if ( node != null )
            return node ;
        node = other.getNodeForNodeId(id) ;
        return node ;
    }

    private NodeId allocate(Node node)
    {
        return null ;
    }
    
    @Override
    public Iterator<Pair<NodeId, Node>> all()
    {
        return null ;
    }

    @Override
    public void sync()
    {}

    @Override
    public void close()
    {}

}

