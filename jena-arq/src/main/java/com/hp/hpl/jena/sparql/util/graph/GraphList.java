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

package com.hp.hpl.jena.sparql.util.graph;

import java.util.ArrayList ;
import java.util.Collection ;
import java.util.HashSet ;
import java.util.Iterator ;
import java.util.List ;
import java.util.Set ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;
import com.hp.hpl.jena.sparql.vocabulary.ListPFunction ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.iterator.IteratorConcat ;
import org.apache.jena.atlas.iterator.Transform ;
import org.apache.jena.atlas.logging.Log ;

import com.hp.hpl.jena.util.iterator.NiceIterator ;
import com.hp.hpl.jena.vocabulary.RDF ;

// Support function for RDF collections (graph level)

public class GraphList
{
    // ----------------------
    /** Starting at a list element, find the heads of lists it is in */
    public static List<Node> listFromMember(GNode gnode)
    {
        List<Node> x = new ArrayList<>() ;
        listFromMember(gnode, x) ;
        return x ;
    }
    
    public static void listFromMember(GNode gnode, final Collection<Node> acc)
    {
        // Get the list nodes for this value.
        Set<GNode> lists = findCellsWithMember(gnode) ;
        
        for ( GNode gn : lists )
        {
            // For each, Reverse to the head
            while( gn != null )
            {
                GNode gn2 = previous(gn) ;
                if ( gn2 == null )
                {
                    acc.add(gn.node) ;
                    // Finish inner loop
                    break ;
                }
                gn = gn2 ;
            }
        }
    }
    
    private static Set<GNode> findCellsWithMember(GNode gnode)
    {
        Set<GNode> x = new HashSet<>() ;
        
        Iterator<Triple> iter = gnode.findable.find(Node.ANY, CAR, gnode.node) ;
        for ( ; iter.hasNext() ; )
        {
            Triple t = iter.next() ;
            x.add(new GNode(gnode, t.getSubject())) ;
        }
        NiceIterator.close(iter) ;
        return x ;         
    }

    private static GNode previous(GNode gnode)
    {
        // reverse 
        Node n = getNodeReverse(gnode, CDR) ;
        if ( n == null )
            return null ;
        return new GNode(gnode, n) ;
    }


    private static Node getNodeReverse(GNode gnode, Node arc)
    {
        Triple t = getTripleReverse(gnode, arc) ;
        if ( t == null )
            return null ;
        return t.getSubject() ;
    }

    private static Triple getTripleReverse(GNode gnode, Node arc)
    {
        Iterator<Triple> iter = gnode.findable.find(Node.ANY, arc, gnode.node) ;
        if ( ! iter.hasNext() )
            return null ;
        Triple t = iter.next() ;
        if ( iter.hasNext() )
            Log.warn(GraphList.class, "Unusual list: two arcs with same property ("+arc+")") ;
        NiceIterator.close(iter) ;
        return t ;    
    }

    // ---------------------------------------------
    
    /** Calculate ?s list:member ?o as if it were a graph.find */
    public static Iterator<Triple> listMember(Graph graph, final Node s, Node o) {
        if ( isAny(s) ) {
            Set<Node> x = findAllLists(graph) ;
            IteratorConcat<Triple> iterConcat = new IteratorConcat<>() ;
            for ( Node s2 : x ) {
                Iterator<Triple> iter = listMember(graph, s2, o) ;
                if ( iter.hasNext() )
                    iterConcat.add(iter); 
            }
            return iterConcat ;
        }
        GNode gn = new GNode(graph, s) ;
        if ( ! isAny(o) ) {
            if ( contains(gn, o) )
                return Iter.singleton(Triple.create(s, ListPFunction.nListMember, o)) ;
            else
                return Iter.nullIterator() ;
        }
        List<Node> x = members(gn) ;
        Transform<Node, Triple> transform = new Transform<Node, Triple>() {
            @Override
            public Triple convert(Node obj) {
                return Triple.create(s, ListPFunction.nListMember, obj) ;
            }
        } ;
        return Iter.map(x.iterator(), transform) ;
    }

    private static boolean isAny(Node x) {
        return x == null || Node.ANY.equals(x) ; 
    }
    
    public static List<Node> members(GNode gnode)
    {
        List<Node> x = new ArrayList<>() ;
        members(gnode, x) ;
        return x ;
    }
    
    public static void members(GNode gnode, final Collection<Node> acc)
    {
        if ( ! isListNode(gnode) )
            return ;
        
        while( ! listEnd(gnode) )
        {
            Node n = car(gnode) ;
            if ( n != null )
                acc.add(n) ;
            gnode = next(gnode) ;
        }
    }
    
    public static int length(GNode gnode)
    {
        if ( ! isListNode(gnode) )
            return -1 ;
        
        int len = 0 ;
        while ( ! listEnd(gnode) )
        {
            len++ ;
            gnode = next(gnode) ;
        }
        return len ;
    }
    
    public static int occurs(GNode gnode, Node item)
    { return indexes(gnode, item).size() ; }
    
    public static boolean contains(GNode gnode, Node item)
    { return index(gnode, item) >= 0 ; } 
    
    public static Node get(GNode gnode, int idx)
    {
//        if ( idx == 0 )
//            return car(gnode) ;
//        Node n = next(gnode) ;
//        return get(graph, n, idx-1) ;
        
        if ( ! isListNode(gnode) )
            return null ;
        
        while ( ! listEnd(gnode) )
        {
            if ( idx == 0 )
                return car(gnode) ;
            gnode = next(gnode) ;
            idx -- ;
        }
        return null ;
    }
    
    public static int index(GNode gnode, Node value)
    {
        if ( ! isListNode(gnode) )
            return -1 ;
        
        int idx = 0 ;
        while ( ! listEnd(gnode) )
        {
            Node v = car(gnode) ;
            if ( v != null && v.equals(value) )
                return idx ;
            gnode = next(gnode) ;
            idx++ ;
        }
        return -1 ;
    }
    
    public static List<Integer> indexes(GNode gnode, Node value)
    {
        List<Integer> x = new ArrayList<>() ;
        
        if ( ! isListNode(gnode) )
            return x ;
        
        int idx = 0 ;
        while ( ! listEnd(gnode) )
        {
            Node v = car(gnode) ;
            if ( v != null && v.equals(value) )
                x.add(new Integer(idx)) ;
            gnode = next(gnode) ;
            idx++ ;
        }
        return x ;
    }
    
    public static void triples(GNode gnode, Collection<Triple> acc)
    {
        if ( listEnd(gnode) )
            return ;
        
        Triple t = null ;
        t = getTriple(gnode, CAR) ;
        if ( t != null )
            acc.add(t) ;
        t = getTriple(gnode, CDR) ;
        if ( t != null )
            acc.add(t) ;
    }

    public static List<Triple> allTriples(GNode gnode)
    {
        List<Triple> x = new ArrayList<>() ;
        allTriples(gnode, x) ;
        return x ;
    }
    
    public static void allTriples(GNode gnode, Collection<Triple> acc)
    {
        if ( ! isListNode(gnode) )
          return ;
        
        while( ! listEnd(gnode) )
        {
            triples(gnode, acc) ;
            gnode = next(gnode) ;
        }
    }
    
    /** Expensive operation to find all the likely looking list heads in a model */
    public static Set<Node> findAllLists(Graph graph)
    {
        // All except rdf:nil.
        
        Set<Node> acc = new HashSet<>() ;
        // A list head is a node with a rdf:rest from it, not but rdf:rest to it.
        Iterator<Triple> iter = graph.find(Node.ANY, CDR, Node.ANY) ;
        try {
            for ( ; iter.hasNext() ; )
            {
                Triple t = iter.next();
                Node node = t.getSubject() ;
                if ( ! graph.contains(Node.ANY, CDR, node) )
                    acc.add(node) ;
            }
        } finally { NiceIterator.close(iter) ; }
        
        
        // Find any rdf:nil lists (which are not pure tails)
        iter = graph.find(Node.ANY, Node.ANY, NIL) ;
        try {
            for ( ; iter.hasNext() ; )
            {
                Triple t = iter.next();
                if ( ! t.getPredicate().equals(CDR) )
                {
                    acc.add(NIL) ;
                    break ;
                }
            }
        } finally { NiceIterator.close(iter) ; }
        
        
        if ( graph.contains(NIL, Node.ANY, Node.ANY) )
            acc.add(NIL) ;
        
        return acc ;
    }
    
    /** Convert a list of nodes into triples, placing them in BPG, returning the head of the list*/
    public static Node listToTriples(List<Node> list, BasicPattern bgp)
    {
        // List ...
        if ( list.size() == 0 )
            return RDF.Nodes.nil ;
        
        Node head = NodeFactory.createAnon() ;
        Node n = head ;
        for ( Node elt : list )
        {
            // Cell:
            Node consCell = NodeFactory.createAnon() ;
            // Last cell to this one.
            Triple t = new Triple(n, RDF.Nodes.rest, consCell) ;
            Triple t1 = new Triple(consCell, RDF.Nodes.first, elt) ;
            n = consCell ;
            bgp.add(t) ;
            bgp.add(t1) ;
        }
        // Finish list.
        Triple t = new Triple(n, RDF.Nodes.rest, RDF.Nodes.nil) ;
        bgp.add(t) ;
        return head ;
    }
    
    private static final Node CAR = RDF.first.asNode() ;
    private static final Node CDR = RDF.rest.asNode() ;
    private static final Node NIL = RDF.nil.asNode() ;

    private static GNode next(GNode gnode) { return new GNode(gnode, cdr(gnode)) ; }

    private static Node value(GNode gnode) { return car(gnode) ; }

    public static boolean isListNode (GNode gnode)
    { return gnode.node.equals(NIL) || isCons(gnode) ; }

    private static boolean isCons (GNode gnode)
    { return gnode.findable.contains(gnode.node, CDR, null) ; }
    
    private static boolean listEnd (GNode gnode)
    { return gnode.node == null || gnode.node.equals(NIL) ; }
    
    private static Node car(GNode gnode)     { return getNode(gnode, CAR) ; } 
    private static Node cdr(GNode gnode)     { return getNode(gnode, CDR) ; }
    private static Node getNode(GNode gnode, Node arc)
    {
        if ( listEnd(gnode) )
            return null ;
        Triple t = getTriple(gnode, arc) ;
        if ( t == null )
            return null ;
        return t.getObject() ;
    }

    private static Triple getTriple(GNode gnode, Node arc)
    {
        if ( listEnd(gnode) )
            return null ;
        
        Iterator<Triple> iter = gnode.findable.find(gnode.node, arc, Node.ANY) ;
        if ( ! iter.hasNext() )
            return null ;
        Triple t = iter.next() ;
        if ( iter.hasNext() )
            Log.warn(GraphList.class, "Unusual list: two arcs with same property ("+arc+")") ;
        NiceIterator.close(iter) ;
        return t ;         
    }
}
