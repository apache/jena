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

package org.apache.jena.sparql.util;

import java.util.List;

import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;

/**
 * Isomorphism utilities, based in order lists. See {@link IsoMatcher} for
 * isomorphism for un-ordered collections. In this class, "isomorphism" is based on the
 * policy in {@link NodeIsomorphismMap}, which is blank node isomorphism unless
 * {@link NodeIsomorphismMap#makeIsomorphic} is overridden.
 */
public class Iso {

    /* 
     * Are lists of triples isomorphic? 
     * The mapping policy {@link NodeIsomorphismMap} is mutated. 
     */
    public static boolean isomorphicTriples(List<Triple> triples1, List<Triple> triples2, NodeIsomorphismMap isoMap) {
        if ( triples1.size() != triples2.size() )
            return false ;
        for ( int i = 0 ; i < triples1.size() ; i++ ) {
            Triple t1 = triples1.get(i) ;
            Triple t2 = triples2.get(i) ;
            if ( ! tripleIso(t1, t2, isoMap))
                return false ;
        }
        return true ;
    }
    
    /* 
     * Are lists of quads isomorphic? 
     * The mapping policy {@link NodeIsomorphismMap} is mutated. 
     */
    public static boolean isomorphicQuads(List<Quad> quads1, List<Quad> quads2, NodeIsomorphismMap isoMap) {
        if ( quads1.size() != quads2.size() )
            return false ;
        for ( int i = 0 ; i < quads1.size() ; i++ ) {
            Quad q1 = quads1.get(i) ;
            Quad q2 = quads2.get(i) ;
            if ( ! quadIso(q1, q2, isoMap))
                return false ;
        }
        return true ;
    }

    /* 
     * Are lists of nodes isomorphic? 
     * The mapping policy {@link NodeIsomorphismMap} is mutated. 
     */
    public static boolean isomorphicNodes(List<Node> nodes1, List<Node> nodes2, NodeIsomorphismMap isoMap) {
        if ( nodes1.size() != nodes2.size() )
            return false ;
        for ( int i = 0 ; i < nodes1.size() ; i++ ) {
            Node n1 = nodes1.get(i) ;
            Node n2 = nodes2.get(i) ;
            if ( ! nodeIso(n1, n2, isoMap))
                return false ;
        }
        return true ;
    }

    /* 
     * Are two triple paths isomorphic? 
     * The mapping policy {@link NodeIsomorphismMap} is mutated. 
     */
    public static boolean triplePathIso(TriplePath tp1, TriplePath tp2, NodeIsomorphismMap isoMap)
    {
        if ( tp1.isTriple() ^ tp2.isTriple() ) 
            return false ;
    
        if ( tp1.isTriple() )
            return Iso.tripleIso(tp1.asTriple(), tp2.asTriple(), isoMap) ;
        else
            return Iso.nodeIso(tp1.getSubject(), tp2.getSubject(), isoMap) && 
                   Iso.nodeIso(tp1.getObject(), tp2.getObject(), isoMap) &&
                   tp1.getPath().equalTo(tp2.getPath(), isoMap) ;
    }

    /* 
     * Are two triples isomorphic? 
     * The mapping policy {@link NodeIsomorphismMap} is mutated. 
     */
    public static boolean tripleIso(Triple t1, Triple t2, NodeIsomorphismMap labelMap)
    {
        Node s1 = t1.getSubject() ;
        Node p1 = t1.getPredicate() ;
        Node o1 = t1.getObject() ;
        
        Node s2 = t2.getSubject() ;
        Node p2 = t2.getPredicate() ;
        Node o2 = t2.getObject() ;
        
        if ( ! nodeIso(s1, s2, labelMap) )
            return false ;
        if ( ! nodeIso(p1, p2, labelMap) )
            return false ;
        if ( ! nodeIso(o1, o2, labelMap) )
            return false ;
    
        return true ;
    }

    /* 
     * Are two quads isomorphic? 
     * The mapping policy {@link NodeIsomorphismMap} is mutated. 
     */
    public static boolean quadIso(Quad t1, Quad t2, NodeIsomorphismMap labelMap)
    {
        Node g1 = t1.getGraph() ;
        Node s1 = t1.getSubject() ;
        Node p1 = t1.getPredicate() ;
        Node o1 = t1.getObject() ;
        
        Node g2 = t2.getGraph() ;
        Node s2 = t2.getSubject() ;
        Node p2 = t2.getPredicate() ;
        Node o2 = t2.getObject() ;
        
        if ( ! nodeIso(g1, g2, labelMap) )
            return false ;
        if ( ! nodeIso(s1, s2, labelMap) )
            return false ;
        if ( ! nodeIso(p1, p2, labelMap) )
            return false ;
        if ( ! nodeIso(o1, o2, labelMap) )
            return false ;
    
        return true ;
    }

    /* 
     * Are two nodes isomorphic? 
     * The mapping policy {@link NodeIsomorphismMap} is mutated. 
     */
    public static boolean nodeIso(Node n1, Node n2, NodeIsomorphismMap isoMap)
    {
        if ( isoMap != null ) { 
            if ( n1.isBlank() && n2.isBlank() )
                return isoMap.makeIsomorphic(n1, n2) ;
            if ( Var.isBlankNodeVar(n1) && Var.isBlankNodeVar(n2) )
                return isoMap.makeIsomorphic(n1, n2) ;
    }
        return n1.equals(n2) ;
    }

    /** Interface for choosing the pairs of node that can be map[ped for isomorphism. */ 
    public interface Mappable {
        boolean mappable(Node n1, Node n2);
    }

    /** Blank nodes are mappable in {@link IsoAlg} */
    public static Iso.Mappable mappableBlankNodes = (n1, n2) -> (n1.isBlank() && n2.isBlank());
    
    /** Blank nodes and variables are mappable in {@link IsoAlg} */
    public static Iso.Mappable mappableBlankNodesVariables = (n1, n2) -> {
        if ( n1.isBlank() && n2.isBlank() ) return true;
        if ( n1.isVariable() && n2.isVariable() ) return true;
        return false;
    };
}

