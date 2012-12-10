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

package com.hp.hpl.jena.graph;

import java.util.Iterator ;
import java.util.List ;
import java.util.Set ;

import com.hp.hpl.jena.graph.impl.GraphWithPerform ;
import com.hp.hpl.jena.util.IteratorCollection ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;
import com.hp.hpl.jena.util.iterator.Map1 ;
import com.hp.hpl.jena.util.iterator.WrappedIterator ;

/**
    An ad-hoc collection of useful code for graphs
 */
public class GraphUtil
{
    /**
     * Only static methods here - the class cannot be instantiated.
     */
    private GraphUtil()
    {}
    
    private static Map1<Triple, Node> mapSubject   = new Map1<Triple, Node>() {
        @Override
        public Node map1(Triple triple)
        {
            return triple.getSubject() ;
        }
    } ;
    private static Map1<Triple, Node> mapPredicate = new Map1<Triple, Node>() {
        @Override
        public Node map1(Triple triple)
        {
            return triple.getPredicate() ;
        }
    } ;
    private static Map1<Triple, Node> mapObject    = new Map1<Triple, Node>() {
        @Override
        public Node map1(Triple triple)
        {
            return triple.getObject() ;
        }
    } ;

    /** Return an iterator over the unique subjects with predciate p and object o.
     * p and o can be wildcards (Node.ANY)
     * @param g Graph  
     * @param p Predicate - may be Node.ANY
     * @param o Object  - may be Node.ANY
     * @return  ExtendedIterator
     */
    public static ExtendedIterator<Node> listSubjects(Graph g, Node p, Node o)
    {
        // Restore a minimal QueryHandler?
        ExtendedIterator<Triple> iter = g.find(Node.ANY, p, o) ;
        Set<Node> nodes = iter.mapWith(mapSubject).toSet() ;
        return WrappedIterator.createNoRemove(nodes.iterator()) ;
    }
    
    /** Return an iterator over the unique predicate between s and o.
     * s and o can be wildcards (Node.ANY)
     * @param g Graph  
     * @param s Subject - may be Node.ANY
     * @param o Object  - may be Node.ANY
     * @return  ExtendedIterator
     */
    public static ExtendedIterator<Node> listPredicates(Graph g, Node s, Node o)
    {
        ExtendedIterator<Triple> iter = g.find(s,Node.ANY, o) ;
        Set<Node> nodes = iter.mapWith(mapPredicate).toSet() ;
        return WrappedIterator.createNoRemove(nodes.iterator()) ;
    }
    
    /** Return an iterator over the unique objects with a given subject and object.
     * s and p can be wildcards (Node.ANY)
     * @param g Graph  
     * @param s Subject - may be Node.ANY
     * @param p Predicate  - may be Node.ANY
     * @return  ExtendedIterator
     */
    public static ExtendedIterator<Node> listObjects(Graph g, Node s, Node p)
    {
        ExtendedIterator<Triple> iter = g.find(s, p, Node.ANY) ;
        Set<Node> nodes = iter.mapWith(mapObject).toSet() ;
        return WrappedIterator.createNoRemove(nodes.iterator()) ;
    }
    
    /** Does the graph use the node anywhere as a subject, predciate or object? */
    public static boolean containsNode(Graph graph, Node node)
    {
        return
            graph.contains(node, Node.ANY, Node.ANY) ||
            graph.contains(Node.ANY, Node.ANY, node) ||
            graph.contains(Node.ANY, node, Node.ANY) ;
    }
    
    /* Control how events are dealt with in bulk */
    private static final boolean OldStyle = true ; 
    
    /**
     * Answer an iterator covering all the triples in the specified graph.
     * 
     * @param g
     *            the graph from which to extract triples
     * @return an iterator over all the graph's triples
     */
    public static ExtendedIterator<Triple> findAll(Graph g)
    {
        return g.find(Triple.ANY) ;
    }
    
    public static void add(Graph graph, Triple[] triples)
    {
        if ( OldStyle && graph instanceof GraphWithPerform )
        {
            GraphWithPerform g = (GraphWithPerform)graph ;
            for (Triple t : triples )
                g.performAdd(t) ;
            graph.getEventManager().notifyAddArray(graph, triples) ;
        }
        else
        {
            for (Triple t : triples )
                graph.add(t) ; 
        }
    }
        
    public static void add(Graph graph, List<Triple> triples)
    {
        if ( OldStyle && graph instanceof GraphWithPerform )
        {
            GraphWithPerform g = (GraphWithPerform)graph ;
            for (Triple t : triples)
                g.performAdd(t) ;
            graph.getEventManager().notifyAddList(graph, triples) ;
        } else
        {
            for (Triple t : triples)
                graph.add(t) ;
        }
    }
        
    public static void add(Graph graph, Iterator<Triple> it)
    {
        // Materialize to avoid ConcurrentModificationException.
        List<Triple> s = IteratorCollection.iteratorToList(it) ;
        if ( OldStyle && graph instanceof GraphWithPerform )
        {
            GraphWithPerform g = (GraphWithPerform)graph ;
            for (Triple t : s)
                g.performAdd(t) ;
            graph.getEventManager().notifyAddIterator(graph, s) ;
        } 
        else
        {
            for (Triple t : s)
                graph.add(t) ;
        }
    }
    
    /** Add triples into the destination (arg 1) from the source (arg 2)*/
    public static void addInto(Graph dstGraph, Graph srcGraph )
    {
        addIteratorWorker(dstGraph, GraphUtil.findAll( srcGraph ));  
        dstGraph.getEventManager().notifyAddGraph( dstGraph, srcGraph );
    }
    
    private static void addIteratorWorker( Graph graph, Iterator<Triple> it )
    { 
        List<Triple> s = IteratorCollection.iteratorToList( it );
        if ( OldStyle && graph instanceof GraphWithPerform )
        {
            GraphWithPerform g = (GraphWithPerform)graph ;
            for (Triple t : s )
                g.performAdd(t) ;
        }
        else
        {
            for (Triple t : s )
                graph.add(t) ;
        }
    }

    public static void delete(Graph graph, Triple[] triples)
    {
        if ( OldStyle && graph instanceof GraphWithPerform )
        {
            GraphWithPerform g = (GraphWithPerform)graph ;
            for (Triple t : triples )
                g.performDelete(t) ;
            graph.getEventManager().notifyDeleteArray(graph, triples) ;
        }
        else
        {
            for (Triple t : triples )
                graph.delete(t) ; 
        }
    }
    
    public static void delete(Graph graph, List<Triple> triples)
    {
        if ( OldStyle && graph instanceof GraphWithPerform )
        {
            GraphWithPerform g = (GraphWithPerform)graph ;
            for (Triple t : triples )
                g.performDelete(t) ;
            graph.getEventManager().notifyDeleteList(graph, triples) ;
        }
        else
        {
            for (Triple t : triples )
                graph.delete(t) ; 
        }
    }
    
    public static void delete(Graph graph, Iterator<Triple> it)
    {
        // Materialize to avoid ConcurrentModificationException.
        List<Triple> s = IteratorCollection.iteratorToList(it) ;
        if ( OldStyle && graph instanceof GraphWithPerform )
        {
            GraphWithPerform g = (GraphWithPerform)graph ;
            for (Triple t : s)
                g.performDelete(t) ;
            graph.getEventManager().notifyDeleteIterator(graph, s) ;
        } 
        else
        {
            for (Triple t : s)
                graph.delete(t) ;
        }
    }
    
    /** Delete triples the destination (arg 1) as given in the source (arg 2) */
    public static void deleteFrom(Graph dstGraph, Graph srcGraph)
    {
        deleteIteratorWorker(dstGraph, GraphUtil.findAll( srcGraph ));  
        dstGraph.getEventManager().notifyDeleteGraph( dstGraph, srcGraph );
    }
    
    private static void deleteIteratorWorker( Graph graph, Iterator<Triple> it )
    { 
        List<Triple> s = IteratorCollection.iteratorToList( it );
        if ( OldStyle && graph instanceof GraphWithPerform )
        {
            GraphWithPerform g = (GraphWithPerform)graph ;
            for (Triple t : s )
                g.performDelete(t) ;
        }
        else
        {
            for (Triple t : s )
                graph.delete(t) ;
        }
    }
    
    private static final int sliceSize = 1000 ;
    /** A safe and cautious remve() function.
     *  To avoid any possible ConcurrentModificationExceptions,
     *  it finds batches of triples, deletes them and tries again until
     *  no change occurs. 
     */
    public static void remove(Graph g, Node s, Node p, Node o)
    {
        // Beware of ConcurrentModificationExceptions.
        // Delete in batches.
        // That way, there is no active iterator when a delete 
        // from the indexes happens.
        
        Triple[] array = new Triple[sliceSize] ;
        
        while (true)
        {
            // Convert/cache s,p,o?
            // The Node Cache will catch these so don't worry unduely. 
            ExtendedIterator<Triple> iter = g.find(s,p,o) ;
            
            // Get a slice
            int len = 0 ;
            for ( ; len < sliceSize ; len++ )
            {
                if ( !iter.hasNext() ) break ;
                array[len] = iter.next() ;
            }
            
            // Delete them.
            for ( int i = 0 ; i < len ; i++ )
            {
                g.delete(array[i]) ;
                array[i] = null ;
            }
            // Finished?
            if ( len < sliceSize )
                break ;
        }
    }
}
