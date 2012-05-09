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

package org.apache.jena.tdb.store.bulkloader3;

import org.openjena.atlas.data.DataBag;
import org.openjena.atlas.lib.Sink;
import org.openjena.atlas.lib.Tuple;

import tdb.tdbloader3;


import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.tdb.nodetable.NodeTable;
import com.hp.hpl.jena.tdb.nodetable.NodeTupleTable;
import com.hp.hpl.jena.tdb.solver.stats.StatsCollectorNodeId;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB;
import com.hp.hpl.jena.tdb.store.NodeId;

public class NodeTableBuilder implements Sink<Quad>
{
    private NodeTable nodeTable ;
    private DataBag<Tuple<Long>> outputTriples ;
    private DataBag<Tuple<Long>> outputQuads ;
    private ProgressLogger monitor ;
    private StatsCollectorNodeId stats ;

    public NodeTableBuilder(DatasetGraphTDB dsg, ProgressLogger monitor, DataBag<Tuple<Long>> outputTriples, DataBag<Tuple<Long>> outputQuads)
    {
        this.monitor = monitor ;
        NodeTupleTable ntt = dsg.getTripleTable().getNodeTupleTable() ; 
        this.nodeTable = ntt.getNodeTable() ;
        this.outputTriples = outputTriples ; 
        this.outputQuads = outputQuads ; 
        this.stats = new StatsCollectorNodeId() ;
    }
    
    @Override
    public void send(Quad quad)
    {
        Node s = quad.getSubject() ;
        Node p = quad.getPredicate() ;
        Node o = quad.getObject() ;
        Node g = null ;
        // Union graph?!
        if ( ! quad.isTriple() && ! quad.isDefaultGraph() )
            g = quad.getGraph() ;
        
        NodeId sId = nodeTable.getAllocateNodeId(s) ; 
        NodeId pId = nodeTable.getAllocateNodeId(p) ;
        NodeId oId = nodeTable.getAllocateNodeId(o) ;
        
        if ( g != null )
        {
            NodeId gId = nodeTable.getAllocateNodeId(g) ;
            outputQuads.send(Tuple.create(gId.getId(), sId.getId(), pId.getId(), oId.getId())) ;
            if ( !tdbloader3.no_stats ) stats.record(gId, sId, pId, oId) ;
        }
        else
        {
            outputTriples.send(Tuple.create(sId.getId(), pId.getId(), oId.getId())) ;
            if ( !tdbloader3.no_stats ) stats.record(null, sId, pId, oId) ;
        }
        monitor.tick() ;
    }

    @Override
    public void flush()
    {
        outputTriples.flush() ;
        outputQuads.flush() ;
        nodeTable.sync() ;
    }

    @Override
    public void close() { 
        flush() ;
    }
    
    public StatsCollectorNodeId getCollector() { return stats ; }
}
