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

package com.hp.hpl.jena.tdb.solver;

import java.util.Iterator ;

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.lib.Tuple ;
import org.junit.Test ;

import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.tdb.TDBFactory ;
import com.hp.hpl.jena.tdb.solver.stats.StatsCollectorNodeId ;
import com.hp.hpl.jena.tdb.solver.stats.StatsResults ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.store.NodeId ;
import com.hp.hpl.jena.tdb.store.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.store.nodetupletable.NodeTupleTable ;
import com.hp.hpl.jena.tdb.sys.TDBInternal ;

public class TestStats extends BaseTest
{
    static DatasetGraphTDB dsg      = TDBInternal.getBaseDatasetGraphTDB(TDBFactory.createDatasetGraph()) ;
    static NodeTupleTable quads     = dsg.getQuadTable().getNodeTupleTable() ;
    static NodeTupleTable triples   = dsg.getTripleTable().getNodeTupleTable() ;
    static NodeTable nt             = quads.getNodeTable() ;
    
    static Quad q1 = SSE.parseQuad("(<g1> <s> <p> 1)") ; 
    static Quad q2 = SSE.parseQuad("(<g2> <s> <p> 2)") ; 
    static Quad q3 = SSE.parseQuad("(<g2> <s> <p> 9)") ; 
    static Quad q4 = SSE.parseQuad("(_    <s> <p> 1)") ; 
    static {
        dsg.add(q1) ;
        dsg.add(q2) ;
        dsg.add(q3) ;
        dsg.add(q4) ;
    }

    
    private StatsResults statsForGraph(NodeId gid)
    {
        Iterator<Tuple<NodeId>> iter = quads.find(gid, null, null, null) ;
        
        StatsCollectorNodeId stats = new StatsCollectorNodeId(nt) ;
        for ( ; iter.hasNext(); )
        {
            Tuple<NodeId> t = iter.next() ;
            stats.record(t.get(0), t.get(1), t.get(2), t.get(3)) ;
        }
        
        return stats.results() ;
    }
    
    private StatsResults statsForDftGraph()
    {
        Iterator<Tuple<NodeId>> iter = triples.findAll() ;
        
        StatsCollectorNodeId stats = new StatsCollectorNodeId(nt) ;
        for ( ; iter.hasNext(); )
        {
            Tuple<NodeId> t = iter.next() ;
            stats.record(null, t.get(0), t.get(1), t.get(2)) ;
        }
        
        return stats.results() ;
    }

    @Test public void stats_01() { 
        StatsResults r = statsForDftGraph() ;
        assertEquals(1, r.getCount()) ; 
        assertEquals(1, r.getPredicates().keySet().size()) ;
    }

    @Test public void stats_02() { 
        NodeId gid = nt.getNodeIdForNode(NodeFactory.createURI("g1")) ;
        StatsResults r = statsForGraph(gid) ;
        assertEquals(1, r.getCount()) ; 
        assertEquals(1, r.getPredicates().keySet().size()) ;
    }
    
    @Test public void stats_03() { 
        NodeId gid = nt.getNodeIdForNode(NodeFactory.createURI("g2")) ;
        StatsResults r = statsForGraph(gid) ;
        assertEquals(2, r.getCount()) ; 
        assertEquals(1, r.getPredicates().keySet().size()) ;
    }
    
    @Test public void stats_04() { 
        StatsResults r = statsForGraph(null) ;
        assertEquals(3, r.getCount()) ; 
        assertEquals(1, r.getPredicates().keySet().size()) ;
    }

}


