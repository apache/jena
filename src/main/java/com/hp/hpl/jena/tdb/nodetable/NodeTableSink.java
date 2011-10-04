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

package com.hp.hpl.jena.tdb.nodetable;

import java.util.Iterator ;

import org.openjena.atlas.iterator.Iter ;
import org.openjena.atlas.lib.Pair ;


import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.tdb.TDBException ;
import com.hp.hpl.jena.tdb.store.NodeId ;

/** Sink node table */
public class NodeTableSink implements NodeTable
{
    long id = 0 ;
    @Override
    public void close()
    {}

    @Override
    public NodeId getNodeIdForNode(Node node)
    {
        throw new TDBException("NodeTableSink.nodeIdForNode") ;
    }

    @Override
    public Node getNodeForNodeId(NodeId id)
    {
        throw new TDBException("NodeTableSink.retrieveNodeByNodeId") ;
    }

    @Override
    public NodeId getAllocateNodeId(Node node)
    {
        NodeId nid = NodeId.create(id) ;
        id++ ;
        return nid ;
    }

    @Override
    public NodeId allocOffset()
    {
        return NodeId.create(id) ;
    }
    
    @Override
    public Iterator<Pair<NodeId, Node>> all()
    {
        return Iter.nullIterator() ;
    }
    
    @Override
    public void sync() { }

    @Override
    public boolean isEmpty()
    {
        return false ;
    } 
}
