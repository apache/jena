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

import java.util.Collection ;
import java.util.Iterator ;
import java.util.SortedMap ;
import java.util.TreeMap ;
import java.util.regex.Matcher ;
import java.util.regex.Pattern ;

import org.apache.jena.atlas.logging.Log ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.util.iterator.ExtendedIterator ;
import com.hp.hpl.jena.util.iterator.Filter ;
import com.hp.hpl.jena.vocabulary.RDF ;
import com.hp.hpl.jena.vocabulary.RDFS ;

public class GraphContainerUtils
{
    private static final Node RDFtype = RDF.type.asNode() ;
    private static final Node BAG = RDF.Bag.asNode() ;
    private static final Node ALT = RDF.Alt.asNode() ;
    private static final Node SEQ = RDF.Seq.asNode() ;
    private static final String membershipPattern$ = RDF.getURI()+"_(\\d+)" ;
    private static final Pattern membershipPattern = Pattern.compile(membershipPattern$) ;
    private static final int NOT_FOUND = -9999 ;

    public static Collection<Node> containerMembers(Graph graph, Node container)
    { return containerMembers(graph, container, null) ; }

    public static Collection<Node> containerMembers(Graph graph, Node container, Node containerType)
    {
        if ( ! isContainer(graph, container, containerType) )
            return null ;

        ExtendedIterator<Triple> iter = graph.find(container, Node.ANY, Node.ANY) ;

        SortedMap<Integer, Node> triples = new TreeMap<>(order) ;
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
    
    private static Node RDFSmember = RDFS.member.asNode() ;
    private static Filter<Triple> filterRDFSmember = new Filter<Triple>() {
        @Override
        public boolean accept(Triple triple) {
            Node p = triple.getPredicate() ;
            if ( ! triple.getPredicate().isURI() )
                return false ;
            if (RDFSmember.equals(p) )
                return true ;
            String u = triple.getPredicate().getURI() ;
            return membershipPattern.matcher(u).matches() ;
        } } ; 

    /** Calculate graph.find(?, rdfs:member, ?) */ 
    public static Iterator<Triple> rdfsMember(Graph graph, Node s, Node o) {
        ExtendedIterator<Triple> iter = graph.find(s, Node.ANY, o) ;
        return iter.filterKeep(filterRDFSmember)  ;
    }
    
    private static int countContainerMember(Graph graph, Node container, Node containerType, Node member, boolean stopEarly)
    {
        if ( graph == null )
        {
            Log.warn(GraphContainerUtils.class, "containerMember called with null graph") ;
            return 0 ;
        }
        
        if ( container == null )
        {
            Log.warn(GraphContainerUtils.class, "containerMember called with null list") ;
            return 0 ;
        }
        if ( member == null )
        {
            Log.warn(GraphContainerUtils.class, "containerMember called with null member") ;
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
                 
                if ( membershipPattern.matcher(u).matches() ) {
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
    
    private static int getIndex(Triple triple)
    {
        String u = triple.getPredicate().getURI() ;
        // Must be _nnn.
        Matcher m = membershipPattern.matcher(u);
        if ( ! m.find() )
            return NOT_FOUND ; 
        String index = m.group(1) ;
        return Integer.parseInt(index) ;
    }
    
    static ContainerOrder order = new ContainerOrder() ;
    static private class ContainerOrder implements java.util.Comparator<Integer>
    {
        @Override
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
