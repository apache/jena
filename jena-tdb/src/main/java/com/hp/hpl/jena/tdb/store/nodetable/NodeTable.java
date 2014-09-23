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

import org.apache.jena.atlas.lib.Closeable ;
import org.apache.jena.atlas.lib.Pair ;
import org.apache.jena.atlas.lib.Sync ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.tdb.store.NodeId ;

/** Node table - conceptually a two way mapping of Node<->NodeId 
 *  where Nodes can be staored and a NodeId allocated
 *  @see NodeId
 */ 

public interface NodeTable extends Sync, Closeable
{
    /** Store the node in the node table (if not already present) and return the allocated Id. */
    public NodeId getAllocateNodeId(Node node) ;
    
    /** Look up node and return the NodeId - return NodeId.NodeDoesNotExist if not found */
    public NodeId getNodeIdForNode(Node node) ;
    
    /** Look up node id and return the Node - return null if not found */
    public Node getNodeForNodeId(NodeId id) ;
    
    /** Test whether the node table contains an entry for node */
    public boolean containsNode(Node node) ;

    /** Test whether the node table contains an entry for node */
    public boolean containsNodeId(NodeId nodeId) ;

    /** Iterate over all nodes (not necessarily fast).  Does not include inlined NodeIds */
    public Iterator<Pair<NodeId, Node>> all() ;
    
    /** The offset needed to predicate allocation difference between peristent tables - internal function */  
    public NodeId allocOffset() ;
    
    /** Anything there? */  
    public boolean isEmpty() ; 

}
