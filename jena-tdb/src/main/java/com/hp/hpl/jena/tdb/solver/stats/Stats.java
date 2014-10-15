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

import static com.hp.hpl.jena.sparql.sse.Item.addPair ;
import static com.hp.hpl.jena.sparql.sse.Item.createTagged ;

import java.io.BufferedOutputStream ;
import java.io.FileOutputStream ;
import java.io.IOException ;
import java.io.OutputStream ;
import java.util.Iterator ;
import java.util.Map ;
import java.util.Map.Entry ;

import org.apache.jena.atlas.logging.Log ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.engine.optimizer.StatsMatcher ;
import com.hp.hpl.jena.sparql.graph.NodeConst ;
import com.hp.hpl.jena.sparql.sse.Item ;
import com.hp.hpl.jena.sparql.sse.ItemList ;
import com.hp.hpl.jena.sparql.sse.ItemWriter ;
import com.hp.hpl.jena.sparql.util.NodeFactoryExtra ;
import com.hp.hpl.jena.sparql.util.Utils ;

public class Stats
{
    static Item ZERO = Item.createNode(NodeFactoryExtra.intToNode(0)) ;

    /** Write statistics */
    static public void write(String filename, StatsResults stats)
    {
        write(filename, stats.getPredicates(), stats.getTypes(), stats.getCount()) ;
    }
    
    /** Write statistics */
    static public void write(OutputStream output, StatsResults stats)
    {
        write(output, stats.getPredicates(), stats.getTypes(), stats.getCount()) ;
    }
    
    static private void write(String filename, Map<Node, Integer> predicateStats, Map<Node, Integer> typeStats, long statsTotal)
    {
        // Write out the stats
        try (OutputStream statsOut = new BufferedOutputStream(new FileOutputStream(filename))) {
            write(statsOut, predicateStats, typeStats, statsTotal) ;
        } catch (IOException ex)
        { Log.warn(Stats.class, "Problem when writing stats file", ex) ; }
    }
    
    static private void write(OutputStream output, Map<Node, Integer> predicateStats, Map<Node, Integer> typeStats, long statsTotal)
    {
        Item item = format(predicateStats, typeStats, statsTotal) ;
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

    public static Item format(StatsResults stats)
    {
        return format(stats.getPredicates(), stats.getTypes(), stats.getCount()) ;
    }
    
    private static Item format(Map<Node, Integer> predicates, Map<Node, Integer> types, long count)
    {
        Item stats = Item.createList() ;
        ItemList statsList = stats.getList() ;
        statsList.add("stats") ;

        Item meta = createTagged(StatsMatcher.META) ;
        addPair(meta.getList(), "timestamp", NodeFactoryExtra.nowAsDateTime()) ;
        addPair(meta.getList(), "run@",  Utils.nowAsString()) ;
        if ( count >= 0 )
            addPair(meta.getList(), StatsMatcher.COUNT, NodeFactoryExtra.intToNode((int)count)) ;
        statsList.add(meta) ;
        
        for ( Entry<Node, Integer> entry : types.entrySet() )
        {
            Node type = entry.getKey() ;
            addTypeTriple(statsList, type, NodeFactoryExtra.intToNode(entry.getValue()) ) ;
        }
        
        for ( Entry<Node, Integer> entry : predicates.entrySet() )
        {
            Node node = entry.getKey() ;
            // Skip these - they just clog things up!
            if ( node.getURI().startsWith("http://www.w3.org/1999/02/22-rdf-syntax-ns#_") )
                continue ;
            addPair(statsList, node, NodeFactoryExtra.intToNode(entry.getValue())) ;
        }
        
        // Add a default rule.
        addPair(statsList, StatsMatcher.OTHER, ZERO) ;
        
        return stats ;
    }

    private static void addTypeTriple(ItemList statsList, Node type, Node intCount)
    {
        ItemList triple = new ItemList() ;
        triple.add("VAR") ;
        triple.add(NodeConst.nodeRDFType) ;
        triple.add(type) ;
        addPair(statsList, Item.createList(triple), Item.createNode(intCount)) ;
    }
}
