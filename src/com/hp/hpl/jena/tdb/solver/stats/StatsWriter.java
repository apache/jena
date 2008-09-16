/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.solver.stats;

import static com.hp.hpl.jena.sparql.sse.Item.addPair;
import static com.hp.hpl.jena.sparql.sse.Item.createTagged;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import lib.Tuple;

import com.hp.hpl.jena.graph.Node;

import com.hp.hpl.jena.sparql.sse.Item;
import com.hp.hpl.jena.sparql.sse.ItemList;
import com.hp.hpl.jena.sparql.util.NodeFactory;
import com.hp.hpl.jena.sparql.util.Utils;

import com.hp.hpl.jena.tdb.index.TripleIndex;
import com.hp.hpl.jena.tdb.pgraph.GraphTDB;
import com.hp.hpl.jena.tdb.pgraph.NodeId;

public class StatsWriter
{
    public static Item gather(GraphTDB graph)
    {
        long count = 0 ;
        Map<NodeId, Integer> predicateIds = new HashMap<NodeId, Integer>(10000) ;
        Map<Node, Integer> predicates = new HashMap<Node, Integer>(10000) ;
        
        Item stats = Item.createList() ;
        ItemList statsList = stats.getList() ;
        statsList.add("stats") ;
        
        TripleIndex index = graph.getIndexSPO() ;
        Iterator<Tuple<NodeId>> iter = index.all() ;
        for ( ; iter.hasNext() ; )
        {
            Tuple<NodeId> tuple = iter.next(); 
            count++ ;
            NodeId nodeId = tuple.get(1) ;      // Predciate slot
            Integer n = predicateIds.get(nodeId) ;
            if ( n == null )
                predicateIds.put(nodeId,1) ;
            else
                predicateIds.put(nodeId, n+1) ;
        }
        
//        System.out.printf("Triples  %d\n", count) ;
//        System.out.println("NodeIds") ;
//        for ( NodeId p : predicateIds.keySet() )
//            System.out.printf("%s : %d\n",p, predicateIds.get(p) ) ;

//        System.out.println("Nodes") ;
        
        Item meta = createTagged("meta") ;
        addPair(meta.getList(), "timestamp", NodeFactory.nowAsDateTime()) ;
        addPair(meta.getList(), "run@",  Utils.nowAsString()) ;
        addPair(meta.getList(), "count", NodeFactory.intToNode((int)count)) ;
        statsList.add(meta) ;
        
        for ( NodeId p : predicateIds.keySet() )
        {
            Node n = graph.getNodeTable().retrieveNodeByNodeId(p) ;
            
            // Skip these - they just clog things up!
            if ( n.getURI().startsWith("http://www.w3.org/1999/02/22-rdf-syntax-ns#_") )
                continue ;
            
            predicates.put(n, predicateIds.get(p)) ;
            
            addPair(statsList, n, NodeFactory.intToNode(predicateIds.get(p))) ;
//            System.out.printf("%s : %d\n",n, predicateIds.get(p) ) ;
        }
        return stats ;
    }
}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */