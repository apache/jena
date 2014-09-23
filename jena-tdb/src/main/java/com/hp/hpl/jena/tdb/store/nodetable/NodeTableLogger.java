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
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.tdb.store.NodeId ;

public class NodeTableLogger implements NodeTable
{
    private static Logger defaultLogger = LoggerFactory.getLogger(NodeTable.class) ; 
    private final Logger log ;
    private final String label  ;

    private final NodeTable nodeTable ;
    
    public NodeTableLogger(String label, NodeTable nodeTable)
    {
        this.nodeTable = nodeTable ;
        this.label = label ;
        log = defaultLogger ;
    }
    
    @Override
    public NodeId getAllocateNodeId(Node node)
    {
        //info("getAllocateNodeId("+node+") =>") ;
        NodeId nId = nodeTable.getAllocateNodeId(node) ;
        info("getAllocateNodeId("+node+") => "+nId) ;
        return nId ;
    }

    @Override
    public NodeId getNodeIdForNode(Node node)
    {
        //info("getNodeIdForNode("+node+") =>") ;
        NodeId nId = nodeTable.getNodeIdForNode(node) ;
        info("getNodeIdForNode("+node+") => "+nId) ;
        return nId ;
    }

    @Override
    public Node getNodeForNodeId(NodeId id)
    {
        //info("getNodeForNodeId("+id+") =>") ;
        Node n = nodeTable.getNodeForNodeId(id) ;
        info("getNodeForNodeId("+id+") => "+n) ;
        return n ;
    }
    
    @Override
    public boolean containsNode(Node node) {
        //info("containsNodeId("+id+") =>") ;
        boolean b = nodeTable.containsNode(node) ;
        info("containsNode("+node+") => "+b) ;
        return b ;
    }

    @Override
    public boolean containsNodeId(NodeId id) {
        //info("containsNodeId("+id+") =>") ;
        boolean b = nodeTable.containsNodeId(id) ;
        info("containsNodeId("+id+") => "+b) ;
        return b ;
    }

    
    @Override
    public NodeId allocOffset()
    {
        NodeId nodeId = nodeTable.allocOffset() ;
        info("allocOffset() => "+nodeId) ;
        return nodeId ;
    }
    
    @Override
    public Iterator<Pair<NodeId, Node>> all()
    {
        info("all()") ;
        return nodeTable.all();
    }

    @Override
    public boolean isEmpty()
    {
        boolean b = nodeTable.isEmpty() ; 
        info("isEmpty() => "+b) ;
        return b ;
    }

    @Override
    public void sync()
    {
        info("sync()") ;
        nodeTable.sync() ; 
    } 

    @Override
    public void close()
    {
        info("close()") ;
        nodeTable.close() ;
    }

    private void info(String string)
    {
        if ( label != null )
            string = label+": "+string ;
        log.info(string) ; 
    }
}
