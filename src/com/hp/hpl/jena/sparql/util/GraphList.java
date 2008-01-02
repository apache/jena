/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.util;

import java.util.*;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.util.iterator.NiceIterator;
import com.hp.hpl.jena.vocabulary.RDF;

// Support function for RDF collections (graph level)

public class GraphList
{
    public static GNode next(GNode gnode) { return new GNode(gnode, cdr(gnode)) ; }
    
    public static Node value(GNode gnode) { return car(gnode) ; }
    
    public static List members(GNode gnode)
    {
        List x = new ArrayList() ;
        members(gnode, x) ;
        return x ;
    }
    
    public static void members(GNode gnode, final Collection acc)
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
    
    public static List indexes(GNode gnode, Node value)
    {
        
        List x = new ArrayList() ;
        
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
    
    public static void triples(GNode gnode, Collection acc)
    {
//        if ( ! isListNode(gnode) )
//            return ;
//        
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

    public static List allTriples(GNode gnode)
    {
        List x = new ArrayList() ;
        allTriples(gnode, x) ;
        return x ;
    }
    
    public static void allTriples(GNode gnode, Collection acc)
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
    public static Set findAllLists(Graph graph)
    {
        // All except rdf:nil.
        
        Set acc = new HashSet() ;
        // A list head is a node with a rdf:rest from it, not but rdf:rest to it.
        Iterator iter = graph.find(Node.ANY, CDR, Node.ANY) ;
        try {
            for ( ; iter.hasNext() ; )
            {
                Triple t = (Triple)iter.next();
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
                Triple t = (Triple)iter.next();
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
    
    
//  private static interface Action { public void action(GNode gnode) ; }
//
//  private static void apply(Action action, GNode gnode)
//      {
//          // Java does not require (or provide) tail recursion removal 
//  //        if ( nil_p(node) )
//  //            return ;
//  //        action.action(gnode) ;
//  //        node = next(gnode) ;
//  //        apply(action, gnode) ;
//          while ( ! nil_p(node) )
//          {
//              action.action(gnode) ;
//              node = cdr(gnode) ;
//          }
//      }
    
    private static final Node CAR = RDF.first.asNode() ;
    private static final Node CDR = RDF.rest.asNode() ;
    private static final Node NIL = RDF.nil.asNode() ;

    public static boolean isListNode (GNode gnode)
    { return gnode.node.equals(NIL) || isCons(gnode) ; }

    public static boolean isCons (GNode gnode)
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
        
        Iterator iter = gnode.findable.find(gnode.node, arc, Node.ANY) ;
        if ( ! iter.hasNext() )
            return null ;
        Triple t = (Triple)iter.next() ;
        if ( iter.hasNext() )
            ALog.warn(GraphList.class, "Unusual list: two arc with same property ("+arc+")") ;
        NiceIterator.close(iter) ;
        return t ;         
    }
}

/*
 * (c) Copyright 2007, 2008 Hewlett-Packard Development Company, LP
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