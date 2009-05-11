/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.core;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

public class Quad
{
    /** Name of the default graph as used in quad form of algebra 
     * (not for access the default graph by name - use Quad.defaultGraphIRI)
     */ 
    public static final Node defaultGraphNodeGenerated     =  Node.createURI("urn:x-arq:DefaultGraphNode") ;
    
    // These are convenience constants for other systems to give special
    // interpretation to these "named" graphs.  
    
    /** Name of the default for explict use in GRAPH */
    public static final Node defaultGraphIRI      =  Node.createURI("urn:x-arq:DefaultGraph") ;
    
    /** Name of the merge of all named graphs (use this for the graph of all named graphs) */
    public static final Node unionGraph           =  Node.createURI("urn:x-arq:UnionGraph") ;

    private final Node graph, subject, predicate, object ;
    
    public Quad(Node graph, Triple triple)
    {
        this(graph, triple.getSubject(), triple.getPredicate(), triple.getObject()) ;
    }
    
    public Quad(Node g, Node s, Node p, Node o)
    {
        if ( g == null ) throw new UnsupportedOperationException("Quad: graph cannot be null");
        if ( s == null ) throw new UnsupportedOperationException("Quad: subject cannot be null");
        if ( p == null ) throw new UnsupportedOperationException("Quad: predicate cannot be null");
        if ( o == null ) throw new UnsupportedOperationException("Quad: object cannot be null");
        this.graph = g ;
        this.subject = s ;
        this.predicate = p ;
        this.object = o ;
    }

    public Node getGraph()      { return graph ; }
    public Node getSubject()    { return subject ; }
    public Node getPredicate()  { return predicate ; }
    public Node getObject()     { return object ; }
    public Triple getTriple()   { return new Triple(subject, predicate, object) ; }
    
    /** node used by the quad generator for the default graph */
    public static boolean isQuadDefaultGraphNode(Node node)
    {
        // The node used by the quad generator for the default graph 
        // Not the named graph 
        return node.equals(defaultGraphNodeGenerated) ;
    }
    
    /** Default graph (generated or explicitly named) */
    public static boolean isDefaultGraph(Node node)
    {
        return node.equals(defaultGraphNodeGenerated) || node.equals(defaultGraphIRI) ; 
    }
    
    /** node used for the RDF merge of named graphs */
    public static boolean isQuadUnionGraph(Node node)
    {
        return node.equals(unionGraph) ;
    }

    
    public boolean isDefaultGraph()         { return isQuadDefaultGraphNode(graph) ; }
    public boolean isDefaultGraphIRI()      { return graph.equals(defaultGraphIRI) ; }
    public boolean isUnionGraph()           { return isQuadUnionGraph(graph) ; }
    
    @Override
    public int hashCode() 
    { 
        return (graph.hashCode()>>2) ^
               (subject.hashCode() >> 1) ^ 
               predicate.hashCode() ^ 
               (object.hashCode() << 1);
    }
    
    @Override
    public boolean equals(Object other) 
    { 
        if ( this == other ) return true ;

        if ( ! ( other instanceof Quad) )
            return false ;
        Quad quad = (Quad)other ;
        
        if ( ! graph.equals(quad.graph) ) return false ;
        if ( ! subject.equals(quad.subject) ) return false ;
        if ( ! predicate.equals(quad.predicate) ) return false ;
        if ( ! object.equals(quad.object) ) return false ;
        return true ;
    }
    
    @Override
    public String toString()
    {
        return "["+graph.toString()+" "+subject.toString()+" "+predicate.toString()+" "+object.toString()+"]" ;
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