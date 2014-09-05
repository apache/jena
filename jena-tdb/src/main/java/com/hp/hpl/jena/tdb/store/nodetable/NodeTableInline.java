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


import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.tdb.store.NodeId ;
import com.hp.hpl.jena.tdb.store.nodetable.NodeTable ;

/** NodeTable wrapper to handle inline node ids.
 * If a node can be made inline, then the underlying table never sees it.
 * If an inline Nodeid is seen, it is decoded and returned without
 * the underlying table being called. 
 */

public class NodeTableInline extends NodeTableWrapper
{
    // Stack order: Inline > Cache > Actual
    
    public static NodeTable create(NodeTable nodeTable)
    {
        return new NodeTableInline(nodeTable) ;
    }
    
    private NodeTableInline(NodeTable nodeTable)
    {
        super(nodeTable) ;
    }
    
    @Override
    public final NodeId getAllocateNodeId(Node node)
    {
        NodeId nid = NodeId.inline(node) ;
        if ( nid != null ) return nid ;
        return super.getAllocateNodeId(node) ;
    }

    @Override
    public final NodeId getNodeIdForNode(Node node)
    {
        NodeId nid = NodeId.inline(node) ;
        if ( nid != null ) return nid ;
        return super.getNodeIdForNode(node) ;
    }
    @Override
    public final Node getNodeForNodeId(NodeId id)
    {
        Node n = NodeId.extract(id) ;
        if ( n != null )
            return n ;
        return super.getNodeForNodeId(id) ;
    }
    
    @Override
    public String toString() { return "Inline("+nodeTable.toString()+")" ; }
}
