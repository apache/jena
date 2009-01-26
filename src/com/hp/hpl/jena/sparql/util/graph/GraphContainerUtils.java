/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.util.graph;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.util.ALog;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDF;


public class GraphContainerUtils
{
    private static final Node RDFtype = RDF.type.asNode() ;
    private static final Node BAG = RDF.Bag.asNode() ;
    private static final Node ALT = RDF.Alt.asNode() ;
    private static final Node SEQ = RDF.Seq.asNode() ;
    private static final String membershipPattern = RDF.getURI()+"_(\\d+)" ;
    private static final int NOT_FOUND = -9999 ;

    public static Collection<Node> containerMembers(Graph graph, Node container)
    { return containerMembers(graph, container, null) ; }

    public static Collection<Node> containerMembers(Graph graph, Node container, Node containerType)
    {
        if ( ! isContainer(graph, container, containerType) )
            return null ;

        ExtendedIterator<Triple> iter = graph.find(container, Node.ANY, Node.ANY) ;

        SortedMap<Integer, Node> triples = new TreeMap<Integer, Node>(order) ;
        try {
            for ( ; iter.hasNext() ; )
            {
                Triple t = iter.next() ;
                int index = getIndex(t) ;
                if ( index == NOT_FOUND )
                    continue ;
                // Insert 
                triples.put(new Integer(index), t.getObject()) ;
            }
        } finally { iter.close() ; }
        return triples.values() ;
    }
            
            
//            List x = new ArrayList() ;
//            try {
//              for ( ; iter.hasNext() ; )
//              {
//                  // Sort triples in a sorted set.
//                  // Then extract.
//                  Triple t = (Triple)iter.next() ;
//                  String p = t.getPredicate().getURI() ;
//    
//                  if ( p.matches(membershipPattern) )
//                      x.add(t.getObject()) ;
//              }
//          } finally { iter.close() ; }
//          return x ;
//      }

    public static boolean isContainerMember(Graph graph, Node container, Node containerType, Node member)
    {
        return countContainerMember(graph, container, containerType, member, true) != 0 ;
    }

    public static int countContainerMember(Graph graph, Node container, Node containerType, Node member)
    {
        return countContainerMember(graph, container, containerType, member, false) ;
    }

    private static int countContainerMember(Graph graph, Node container, Node containerType, Node member, boolean stopEarly)
    {
        if ( graph == null )
        {
            ALog.warn(GraphContainerUtils.class, "containerMember called with null graph") ;
            return 0 ;
        }
        
        if ( container == null )
        {
            ALog.warn(GraphContainerUtils.class, "containerMember called with null list") ;
            return 0 ;
        }
        if ( member == null )
        {
            ALog.warn(GraphContainerUtils.class, "containerMember called with null member") ;
            return 0 ;
        }
        
        if ( ! isContainer(graph, container, containerType) )
            return 0 ;
        
        int count = 0 ;
        ExtendedIterator<Triple> iter = graph.find(container, Node.ANY, member) ;
        try {
            for ( ; iter.hasNext() ; )
            {
                Triple t = iter.next() ;
                Node p = t.getPredicate() ;
                String u = p.getURI() ;
                
                if ( u.matches(membershipPattern) )
                {
                    count ++ ;
                    if ( stopEarly )
                        return count ;
                }
            }
        } finally { iter.close() ; }
        return count ;
    }

//    public static boolean isContainer(Graph graph, Node container)
//    { return isContainer(graph, container, null) ; }

    public static boolean isContainer(Graph graph, Node container, Node containerType)
    {
//        if ( container.isLiteral() )
//            return false ;
        
        if ( containerType == null )
            return  isContainer(graph, container, BAG) ||
                    isContainer(graph, container, ALT) ||
                    isContainer(graph, container, SEQ) ;
        
        return graph.contains(container, RDFtype, containerType) ; 
    }
    
    static Pattern pattern = Pattern.compile(membershipPattern);
    private static int getIndex(Triple triple)
    {
        String u = triple.getPredicate().getURI() ;
        // Must be _nnn.
        Matcher m = pattern.matcher(u);
        if ( ! m.find() )
            return NOT_FOUND ; 
        String index = m.group(1) ;
        return Integer.parseInt(index) ;
    }
    
    static ContainerOrder order = new ContainerOrder() ;
    static private class ContainerOrder implements java.util.Comparator<Integer>
    {
        public int compare(Integer i1, Integer i2)
        {
            int index1 = i1.intValue() ;
            int index2 = i2.intValue() ;
            
            if ( index1 < index2 ) return Expr.CMP_LESS ; 
            if ( index1 > index2 ) return Expr.CMP_GREATER ;
            return Expr.CMP_EQUAL ; 
        }
    }

}

/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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