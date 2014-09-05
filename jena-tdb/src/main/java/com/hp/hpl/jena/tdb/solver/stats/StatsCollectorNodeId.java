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

package com.hp.hpl.jena.tdb.solver.stats;

import java.util.HashMap ;
import java.util.Map ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.graph.NodeConst ;
import com.hp.hpl.jena.tdb.store.NodeId ;
import com.hp.hpl.jena.tdb.store.nodetable.NodeTable ;

/** Statistics collector, aggregates based on NodeId */
public class StatsCollectorNodeId extends StatsCollectorBase<NodeId>
{
    private NodeTable nodeTable ;
    
    public StatsCollectorNodeId(NodeTable nodeTable)
    {
        super(findRDFType(nodeTable)) ;
        this.nodeTable = nodeTable ;
    }
    
    private static NodeId findRDFType(NodeTable nodeTable2)
    {
        return nodeTable2.getAllocateNodeId(NodeConst.nodeRDFType) ;
    }

    @Override
    protected Map<Node, Integer> convert(Map<NodeId, Integer> stats)
    {
        Map<Node, Integer> statsNodes = new HashMap<>(1000) ;
        for ( NodeId p : stats.keySet() )
        {
            Node n = nodeTable.getNodeForNodeId(p) ;
            statsNodes.put(n, stats.get(p)) ;
        }
        return statsNodes ;
    }
}
