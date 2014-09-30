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

package com.hp.hpl.jena.sparql.core;

import static org.apache.jena.atlas.lib.Lib.equal ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.graph.Triple ;

public class Quad
{
    // Create QuadNames? GraphNames?
    
    /** Name of the default graph as used by parsers and in quad form of algebra. 
     *  Not for access to the default graph by name - use Quad.defaultGraphIRI.
     */ 
    public static final Node defaultGraphNodeGenerated     =  NodeFactory.createURI("urn:x-arq:DefaultGraphNode") ;
    
    // These are convenience constants for other systems to give special
    // interpretation to these "named" graphs.  
    
    /** Name of the default for explict use in GRAPH */
    public static final Node defaultGraphIRI        =  NodeFactory.createURI("urn:x-arq:DefaultGraph") ;

    /** Name of the merge of all named graphs (use this for the graph of all named graphs) */
    public static final Node unionGraph           =  NodeFactory.createURI("urn:x-arq:UnionGraph") ;

    /** Name of the non-graph when a quad is really a triple - also parsing of triples formats 
     *  (and the default graph when parsing N-Quads or TriG) 
     *  Not for access to the default graph by name - use Quad.defaultGraphIRI.
     */
    public static final Node tripleInQuad           =  null ;
    
    private final Node graph, subject, predicate, object ;
    
    public Quad(Node graph, Triple triple)
    {
        this(graph, triple.getSubject(), triple.getPredicate(), triple.getObject()) ;
    }
    
    public Quad(Node g, Node s, Node p, Node o)
    {
        // Null means it's a triple really.
        //if ( g == null ) throw new UnsupportedOperationException("Quad: graph cannot be null");
        if ( s == null ) throw new UnsupportedOperationException("Quad: subject cannot be null");
        if ( p == null ) throw new UnsupportedOperationException("Quad: predicate cannot be null");
        if ( o == null ) throw new UnsupportedOperationException("Quad: object cannot be null");
        this.graph = g ;
        this.subject = s ;
        this.predicate = p ;
        this.object = o ;
    }
    
    public static Quad create(Node g, Node s, Node p, Node o)   { return new Quad(g,s,p,o) ; }
    public static Quad create(Node g, Triple t)                 { return new Quad(g,t) ; }

    public final Node getGraph()      { return graph ; }
    public final Node getSubject()    { return subject ; }
    public final Node getPredicate()  { return predicate ; }
    public final Node getObject()     { return object ; }

    /** Get as a triple - useful because quads often come in blocks for the same graph */  
    public Triple asTriple()
    { 
        return new Triple(subject, predicate, object) ;
    }
    
    public boolean isConcrete()
    {
        return subject.isConcrete() && predicate.isConcrete() && object.isConcrete() && graph.isConcrete() ;
    }
    
    /** Test whether this is a quad for the default graph (not the default graphs by explicit name) */
    public static boolean isDefaultGraphGenerated(Node node)
    {
        // The node used by the quad generator for the default graph 
        // Not the named graph that refers to the default graph.
        return defaultGraphNodeGenerated.equals(node) ;
    }
    
    /** Default, concrete graph (either generated or explicitly named) -- not triple-in-quad*/
    public static boolean isDefaultGraphExplicit(Node node)
    {
        return defaultGraphIRI.equals(node) ; 
    }
    
    /** Default, concrete graph (either generated or explicitly named) -- not triple-in-quad*/
    public static boolean isDefaultGraph(Node node)
    {
        return isDefaultGraphGenerated(node) || isDefaultGraphExplicit(node) ; 
    }

    /** Default, concrete graph (either generated or explicitly named) -- not triple-in-quad*/
    public static boolean isUnionGraph(Node node)
    {
        return unionGraph.equals(node) ; 
    }


    /** Default, concrete graph via generated URI (not explciitly named) */
    public boolean isDefaultGraphExplicit()
    { return isDefaultGraphExplicit(getGraph()) ; }
    
    /** Default graph, explicitly named (not generated) */
    public boolean isDefaultGraphGenerated()
    {
        return  isDefaultGraphGenerated(getGraph()) ;
    }

    /** Default, concrete graph (either generated or explicitly named) */
    public boolean isDefaultGraph()
    {
        return  isDefaultGraph(getGraph()) ;
    }
    
    /** node used for the RDF merge of named graphs */
//    public static boolean isQuadUnionGraph(Node node)
//    {
//        return node.equals(unionGraph) ;
//    }
//    
    public boolean isUnionGraph()           { return isUnionGraph(graph) ; }

    /** Is it really a triple? */  
    public boolean isTriple()               { return equal(graph, tripleInQuad) ; } 

    /** Is this quad a legal data quad (legal data triple, IRI for graph) */   
    public boolean isLegalAsData()
    {
        Node sNode = getSubject() ;
        Node pNode = getPredicate() ;
        Node oNode = getObject() ;
        Node gNode = getGraph() ;
        
        if ( sNode.isLiteral() || sNode.isVariable() )
            return false ;
        
        if ( ! pNode.isURI() )  // Not variable, literal or blank.
            return false ;

        if ( oNode.isVariable() )
            return false ;
        
        if ( gNode != null ) {
            if ( ! gNode.isURI() && ! gNode.isBlank() )
                return false ;
        }
        
        return true ;
    }
    
    @Override
    public int hashCode() 
    { 
        int x = 
               (subject.hashCode() >> 1) ^ 
               predicate.hashCode() ^ 
               (object.hashCode() << 1);
        if ( graph != null )
            x ^= (graph.hashCode()>>2) ;
        else
            x++ ;
        return x ;
    }
    
    
    
    @Override
    public boolean equals(Object other) 
    { 
        if ( this == other ) return true ;

        if ( ! ( other instanceof Quad) )
            return false ;
        Quad quad = (Quad)other ;
        
        if ( ! equal(graph, quad.graph) ) return false ;
        if ( ! subject.equals(quad.subject) ) return false ;
        if ( ! predicate.equals(quad.predicate) ) return false ;
        if ( ! object.equals(quad.object) ) return false ;
        return true ;
    }
    
    public boolean matches(Node g, Node s, Node p, Node o)
    {
        return nodeMatches(getGraph(), g) && nodeMatches(getSubject(), s) &&
               nodeMatches(getPredicate(), p) && nodeMatches(getObject(), o) ;
    }

    private static boolean nodeMatches(Node thisNode, Node otherNode)
    {
        // otheNode may be Node.ANY, and this works out.
        return otherNode.matches(thisNode) ;
    }

    @Override
    public String toString()
    {
        String str = (graph==null)?"_":graph.toString() ;
        return "["+str+" "+subject.toString()+" "+predicate.toString()+" "+object.toString()+"]" ;
    }
}
