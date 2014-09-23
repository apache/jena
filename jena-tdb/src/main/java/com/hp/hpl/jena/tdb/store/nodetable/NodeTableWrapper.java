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

package com.hp.hpl.jena.tdb.store.nodetable;

import java.util.Iterator ;

import org.apache.jena.atlas.lib.Pair ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.tdb.store.NodeId ;
import com.hp.hpl.jena.tdb.store.nodetable.NodeTable ;

public class NodeTableWrapper implements NodeTable
{
    protected final NodeTable nodeTable ;
    public final NodeTable wrapped() { return nodeTable ; } 
    
    protected NodeTableWrapper(NodeTable nodeTable)
    {
        this.nodeTable = nodeTable ;
    }
    
    @Override
    public NodeId getAllocateNodeId(Node node)
    {
        return nodeTable.getAllocateNodeId(node) ;
    }

    @Override
    public NodeId getNodeIdForNode(Node node)
    {
        return nodeTable.getNodeIdForNode(node) ;
    }

    @Override
    public Node getNodeForNodeId(NodeId id)
    {
        return nodeTable.getNodeForNodeId(id) ;
    }
    
    @Override
    public boolean containsNode(Node node) {
        return nodeTable.containsNode(node) ;
    }

    @Override
    public boolean containsNodeId(NodeId nodeId) {
        return nodeTable.containsNodeId(nodeId) ;    }

    @Override
    public NodeId allocOffset()
    {
        return nodeTable.allocOffset() ;
    }

    @Override
    public Iterator<Pair<NodeId, Node>> all()
    {
        return nodeTable.all();
    }

    @Override
    public boolean isEmpty()    { return nodeTable.isEmpty() ; }
    
    @Override
    public void sync() { nodeTable.sync() ; } 

    @Override
    public void close()
    { nodeTable.close() ; }
}
