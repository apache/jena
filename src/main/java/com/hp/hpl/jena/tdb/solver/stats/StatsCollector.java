/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.solver.stats;

import static com.hp.hpl.jena.sparql.sse.Item.addPair ;
import static com.hp.hpl.jena.sparql.sse.Item.createTagged ;

import java.util.HashMap ;
import java.util.Iterator ;
import java.util.Map ;
import java.util.Map.Entry ;

import org.openjena.atlas.lib.MapUtils ;
import org.openjena.atlas.lib.Tuple ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.engine.optimizer.StatsMatcher ;
import com.hp.hpl.jena.sparql.sse.Item ;
import com.hp.hpl.jena.sparql.sse.ItemList ;
import com.hp.hpl.jena.sparql.util.NodeFactory ;
import com.hp.hpl.jena.sparql.util.Utils ;
import com.hp.hpl.jena.tdb.nodetable.NodeTable ;
import com.hp.hpl.jena.tdb.store.GraphTDB ;
import com.hp.hpl.jena.tdb.store.NodeId ;

public class StatsCollector
{
    private static Item ZERO = Item.createNode(NodeFactory.intToNode(0)) ;
    
    private long count = 0 ;
    private Map<NodeId, Integer> predicateIds = new HashMap<NodeId, Integer>(10000) ;
    
    public StatsCollector()    { }

    //@Override
    public void send(NodeId g, NodeId s, NodeId p, NodeId o)
    {
        count++ ;
        MapUtils.increment(predicateIds, p) ;
    }

    public long getCount()
    {
        return count ;
    }

    public Map<NodeId, Integer> getPredicateIds()
    {
        return predicateIds ;
    }

    
//    public static class StatsGraph_OLD extends GraphBase
//    {
//        // Connect to StatsWriter.
//        
//        long count = 0 ;
//        Map<Node, Integer> predicates = new HashMap<Node, Integer>(10000) ;
//        
//        @Override
//        protected ExtendedIterator<Triple> graphBaseFind(TripleMatch m)
//        {
//            return null ;
//        }
//
//        @Override
//        public void performAdd( Triple t ) 
//        { 
//            // Raw.
//            count++ ;
//            
//            Node p = t.getPredicate() ;
//            Integer n = predicates.get(p) ;
//            if ( n == null )
//                predicates.put(p,1) ;
//            else
//                predicates.put(p, n+1) ;
//        }
//        
//        public void printStats()
//        {
//            StatsCollector.format(predicates, count) ;
////            System.out.printf("Triples: %d\n",count) ;
////            for ( Node p : predicates.keySet() )
////                System.out.printf("%s : %d\n",p, predicates.get(p) ) ;
//        }
//    }
    
    /** Gather statistics, any graph */
    public static Item gather(Graph graph)
    {
        Map<Node, Integer> predicates = new HashMap<Node, Integer>(1000) ;
        long count = 0 ;
        Iterator<Triple> iter = graph.find(Node.ANY, Node.ANY, Node.ANY) ;
        for ( ; iter.hasNext() ; )
        {
            Triple t = iter.next();
            count++ ;
            Node p = t.getPredicate() ;
            Integer num = predicates.get(p) ;
            if ( num == null )
                predicates.put(p,1) ;
            else
                predicates.put(p, num+1) ;
        }
        
        return format(predicates, count) ;
    }
    
    /** Gather statistics - faster for TDB */
    public static Item gatherTDB(GraphTDB graph)
    {
        long count = 0 ;
        Map<NodeId, Integer> predicateIds = new HashMap<NodeId, Integer>(1000) ;
        
//        TupleIndex index = graph.getNodeTupleTable().getTupleTable().getIndex(0) ;
//        if ( ! index.getLabel().equals("SPO->SPO") &&
//             ! index.getLabel().equals("GSPO->GSPO") )
//            Log.warn(StatsCollector.class, "May not be the right index: "+index.getLabel()) ;
//        boolean quads = (index.getTupleLength()==4)  ;
        
        int len = graph.getNodeTupleTable().getTupleTable().getTupleLen() ;
        
        Iterator<Tuple<NodeId>> iter = graph.getNodeTupleTable().findAll() ;
        boolean quads = (len==4)  ;
        final int idx = (quads ? 2 : 1) ;
        
        for ( ; iter.hasNext() ; )
        {
            Tuple<NodeId> tuple = iter.next(); 
            count++ ;
            MapUtils.increment(predicateIds, tuple.get(idx)) ;
        }
        
        return statsOutput(graph.getNodeTupleTable().getNodeTable(), predicateIds, count) ;
    }
        
    public static Item statsOutput(NodeTable nodeTable, Map<NodeId, Integer> predicateIds, long total)
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
     
    public static Item format(Map<Node, Integer> predicates, long count)
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
            addPair(statsList, entry.getKey(), NodeFactory.intToNode(entry.getValue())) ;
        
        // Add a default rule.
        addPair(statsList, StatsMatcher.OTHER, ZERO) ;
        
        return stats ;
    }
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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