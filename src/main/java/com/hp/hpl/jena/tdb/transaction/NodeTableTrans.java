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

package com.hp.hpl.jena.tdb.transaction;

import java.util.Iterator ;

import org.openjena.atlas.lib.Pair ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.tdb.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.store.NodeId ;

public class NodeTableTrans implements NodeTable
{
    private final NodeTable base ;
    private final NodeTable other ;
    private boolean inTransaction ;
    private long otherAllocOffset ;     // -2 means "undiscovered"

    public  NodeTableTrans(Transaction txn, NodeTable base, NodeTable other)
    {
        // The other object file must use the same allocation policy.
        this.base = base ;
        this.other = other ;
        inTransaction = false ;
        this.otherAllocOffset = -2 ;
    }
    
    @Override
    public NodeId getAllocateNodeId(Node node)
    {
        return null ;
    }

    @Override
    public NodeId getNodeIdForNode(Node node)
    {
        return null ;
    }

    @Override
    public Node getNodeForNodeId(NodeId id)
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
    { 
        //base.sync() ;
        other.sync();
    }

    @Override
    public void close()
    {}

}

