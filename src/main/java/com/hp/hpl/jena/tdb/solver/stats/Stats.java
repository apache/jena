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

package com.hp.hpl.jena.tdb.solver.stats;

import static com.hp.hpl.jena.sparql.sse.Item.addPair ;
import static com.hp.hpl.jena.sparql.sse.Item.createTagged ;

import java.io.FileOutputStream ;
import java.io.IOException ;
import java.io.OutputStream ;
import java.util.HashMap ;
import java.util.Iterator ;
import java.util.Map ;
import java.util.Map.Entry ;

import org.openjena.atlas.lib.Tuple ;
import org.openjena.atlas.logging.Log ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.engine.optimizer.StatsMatcher ;
import com.hp.hpl.jena.sparql.sse.Item ;
import com.hp.hpl.jena.sparql.sse.ItemList ;
import com.hp.hpl.jena.sparql.sse.ItemWriter ;
import com.hp.hpl.jena.sparql.util.NodeFactory ;
import com.hp.hpl.jena.sparql.util.Utils ;
import com.hp.hpl.jena.tdb.index.TupleIndex ;
import com.hp.hpl.jena.tdb.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB ;
import com.hp.hpl.jena.tdb.store.GraphTDB ;
import com.hp.hpl.jena.tdb.store.NodeId ;
import com.hp.hpl.jena.tdb.sys.Names ;

public class Stats
{
    static Item ZERO = Item.createNode(NodeFactory.intToNode(0)) ;

    /** Write statistics */
    static public void write(String filename, StatsCollector stats)
    {
        write(filename, stats.getPredicates(), stats.getCount()) ;
    }
    
    /** Write statistics */
    static public void write(OutputStream output, StatsCollector stats)
    {
        write(output, stats.getPredicates(), stats.getCount()) ;
    }
    
    /** Write statistics */
    static public void write(DatasetGraphTDB dsg, StatsCollectorNodeId statsById)
    {
        long statsTotal = statsById.getCount() ;
        Map<Node, Integer> stats = statsById.asNodeStats(dsg.getTripleTable().getNodeTupleTable().getNodeTable()) ;
        Item item = format(stats, statsTotal) ; 
        String filename = dsg.getLocation().getPath(Names.optStats) ;
        write(filename, stats, statsTotal) ;
    }

    static private void write(String filename, Map<Node, Integer> stats, long statsTotal)
    {
        // Write out the stats
        try {
            OutputStream statsOut = new FileOutputStream(filename) ;
            write(statsOut, stats, statsTotal) ;
            statsOut.close() ;
        } catch (IOException ex)
        { Log.warn(Stats.class, "Problem when writing stats file", ex) ; }
    }
    
    static private void write(OutputStream output, Map<Node, Integer> stats, long statsTotal)
    {
        Item item = format(stats, statsTotal) ;
        ItemWriter.write(output, item) ;
    }
    

    /** Gather statistics, any graph */
    public static StatsCollector gather(Graph graph)
    {
        StatsCollector stats = new StatsCollector() ;
    
        Iterator<Triple> iter = graph.find(Node.ANY, Node.ANY, Node.ANY) ;
        for ( ; iter.hasNext() ; )
        {
            Triple t = iter.next();
            stats.record(null, t.getSubject(), t.getPredicate(), t.getObject()) ;
        }
        
        return stats ;
    }

    /** Gather statistics - faster for TDB */
    public static StatsCollector gatherTDB(GraphTDB graph)
    {
        long count = 0 ;
        Map<NodeId, Integer> predicateIds = new HashMap<NodeId, Integer>(1000) ;
        
        TupleIndex index = graph.getNodeTupleTable().getTupleTable().getIndex(0) ;
        if ( ! index.getLabel().equals("SPO->SPO") &&
             ! index.getLabel().equals("GSPO->GSPO") )
            Log.warn(StatsCollector.class, "May not be the right index: "+index.getLabel()) ;
        boolean quads = (index.getTupleLength()==4)  ;
        
        Iterator<Tuple<NodeId>> iter = graph.getNodeTupleTable().findAll() ;
        StatsCollectorNodeId collector = new StatsCollectorNodeId() ;
        
        for ( ; iter.hasNext() ; )
        {
            Tuple<NodeId> tuple = iter.next(); 
            count++ ;
            if ( quads )
                collector.record(tuple.get(0), tuple.get(1), tuple.get(2), tuple.get(3)) ;
            else
                collector.record(null, tuple.get(0), tuple.get(1), tuple.get(2)) ;
        }
        
        Map<Node, Integer> predicates = collector.asNodeStats(graph.getNodeTupleTable().getNodeTable()) ;
        return new StatsCollector(count, predicates) ;
    }

    private static Item statsOutput(NodeTable nodeTable, Map<NodeId, Integer> predicateIds, long total)
    {
        Map<Node, Integer> predicates = new HashMap<Node, Integer>(1000) ;
        for ( NodeId p : predicateIds.keySet() )
        {
            Node n = nodeTable.getNodeForNodeId(p) ;
            
            // Skip these - they just clog things up!
            if ( n.getURI().startsWith("http://www.w3.org/1999/02/22-rdf-syntax-ns#_") )
                continue ;
            
            predicates.put(n, predicateIds.get(p)) ;
        }
        
        return format(predicates, total) ;
    }

    public static Item format(StatsCollector stats)
    {
        return format(stats.getPredicates(), stats.getCount()) ;
    }
    
    /*
     *             
            // Skip these - they just clog things up!
            if ( n.getURI().startsWith("http://www.w3.org/1999/02/22-rdf-syntax-ns#_") )
                continue ;

     */
    
    private static Item format(Map<Node, Integer> predicates, long count)
    {
        Item stats = Item.createList() ;
        ItemList statsList = stats.getList() ;
        statsList.add("stats") ;

//        System.out.printf("Triples  %d\n", count) ;
//        System.out.println("NodeIds") ;
//        for ( NodeId p : predicateIds.keySet() )
//            System.out.printf("%s : %d\n",p, predicateIds.get(p) ) ;

//        System.out.println("Nodes") ;
        
        Item meta = createTagged(StatsMatcher.META) ;
        addPair(meta.getList(), "timestamp", NodeFactory.nowAsDateTime()) ;
        addPair(meta.getList(), "run@",  Utils.nowAsString()) ;
        if ( count >= 0 )
            addPair(meta.getList(), StatsMatcher.COUNT, NodeFactory.intToNode((int)count)) ;
        statsList.add(meta) ;
        
        for ( Entry<Node, Integer> entry : predicates.entrySet() )
        {
            Node node = entry.getKey() ;
            // Skip these - they just clog things up!
            if ( node.getURI().startsWith("http://www.w3.org/1999/02/22-rdf-syntax-ns#_") )
                continue ;
            addPair(statsList, node, NodeFactory.intToNode(entry.getValue())) ;
        }
        
        // Add a default rule.
        addPair(statsList, StatsMatcher.OTHER, ZERO) ;
        
        return stats ;
    }
}
