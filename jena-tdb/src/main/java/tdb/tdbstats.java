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

package tdb;

import java.util.Iterator ;

import org.apache.jena.atlas.lib.Tuple ;
import org.apache.jena.atlas.logging.Log ;
import tdb.cmdline.CmdTDB ;
import tdb.cmdline.CmdTDBGraph ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.tdb.solver.SolverLib ;
import com.hp.hpl.jena.tdb.solver.stats.Stats ;
import com.hp.hpl.jena.tdb.solver.stats.StatsCollectorNodeId ;
import com.hp.hpl.jena.tdb.solver.stats.StatsResults ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.store.NodeId ;
import com.hp.hpl.jena.tdb.store.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.store.nodetupletable.NodeTupleTable ;

public class tdbstats extends CmdTDBGraph
{
    // tdbconfig?
    static public void main(String... argv)
    { 
        CmdTDB.init() ;
        new tdbstats(argv).mainRun() ;
    }

    protected tdbstats(String[] argv)
    {
        super(argv) ;
    }
    
    @Override
    protected String getSummary()
    {
        return null ;
    }
    
    public static StatsResults stats(DatasetGraphTDB dsg, Node gn)
    {
        NodeTable nt = dsg.getTripleTable().getNodeTupleTable().getNodeTable() ;
        StatsCollectorNodeId stats = new StatsCollectorNodeId(nt) ;
        
        if ( gn == null )
        {
            Iterator<Tuple<NodeId>> iter = dsg.getTripleTable().getNodeTupleTable().findAll() ;
            for ( ; iter.hasNext(); )
            {
                Tuple<NodeId> t = iter.next() ;
                stats.record(null, t.get(0), t.get(1), t.get(2)) ;
            }
        } else {
            // If the union graph, then we need to scan all quads but with uniqueness.
            boolean unionGraph = Quad.isUnionGraph(gn) ;
            NodeId gnid = null ;
            if ( ! unionGraph )
            {
                gnid = nt.getNodeIdForNode(gn) ;
                if ( NodeId.isDoesNotExist(gnid) )
                Log.warn(tdbstats.class, "No such graph: "+gn) ;
            }
                
            NodeTupleTable ntt = dsg.getQuadTable().getNodeTupleTable() ;
            Iterator<Tuple<NodeId>> iter = unionGraph
                ? SolverLib.unionGraph(ntt)
                : ntt.find(gnid, null, null, null) ;
            for ( ; iter.hasNext(); )
            {
                Tuple<NodeId> t = iter.next() ;
                stats.record(t.get(0), t.get(1), t.get(2), t.get(3)) ;
            }
        }
        return stats.results() ;
    }

    @Override
    protected void exec()
    {
        DatasetGraphTDB dsg = getDatasetGraphTDB() ;
        Node gn = getGraphName() ;
        StatsResults results = stats(dsg, gn) ;
        Stats.write(System.out, results) ;
    }
}
