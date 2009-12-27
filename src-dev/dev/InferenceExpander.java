/*
 * (c) Copyright 2009 Talis Information Ltd.
 * All rights reserved.
 * [See end of file]
 */

package dev;

import java.util.List ;
import java.util.Map ;

import atlas.lib.Sink ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.vocabulary.RDF ;

final class InferenceExpander implements Sink<Triple>
{
    // Assumes rdf:type is not a superproperty. 
    
    // Expanded hierarchy:
    // If C < C1 < C2 then C2 is in the list for C 
    final private Map<Node, List<Node>> transClasses ;
    final private Map<Node, List<Node>> transProperties;
    final private Sink<Triple> output ;
    final private Map<Node, List<Node>> domainList ;
    final private Map<Node, List<Node>> rangeList ;  
    
    static final Node rdfType = RDF.type.asNode() ;
    
    public InferenceExpander(Sink<Triple> output,
                             Map<Node, List<Node>> transClasses,
                             Map<Node, List<Node>> transProperties,
                             Map<Node, List<Node>> domainList,
                             Map<Node, List<Node>> rangeList)
    {
        this.output = output ;
        this.transClasses = transClasses ;
        this.transProperties = transProperties ;
        this.domainList = domainList ;
        this.rangeList = rangeList ;
    }
    
    public void send(Triple triple)
    {
        System.out.println();
        output.send(triple) ;
        Node s = triple.getSubject() ;
        Node p = triple.getPredicate() ;
        Node o = triple.getObject() ;

        subClass(s,p,o) ;
        subProperty(s,p,o) ;

        domain(s,p,o) ;
        
        // Beware of literal subjects.
        range(s,p,o) ;
    }

    /*
    [rdfs2:  (?p rdfs:domain ?c) -> [(?x rdf:type ?c) <- (?x ?p ?y)] ] 
     [rdfs3:  (?p rdfs:range ?c)  -> [(?y rdf:type ?c) <- (?x ?p ?y)] ] 
    */
    
    final private void domain(Node s, Node p, Node o)
    {
        List<Node> x = domainList.get(p) ;
        if ( x != null )
        {
            for ( Node c : x )
            {
                output.send(new Triple(s,rdfType,c)) ;
                subClass(s, rdfType, c) ;
            }
        }
    }

    final private void range(Node s, Node p, Node o)
    {
        // Range
        List<Node> x = rangeList.get(p) ;
        if ( x != null )
        {
            for ( Node c : x )
            {
                output.send(new Triple(o,rdfType,c)) ;
                subClass(o, rdfType, c) ;
            }
        }
    }

    final private void subClass(Node s, Node p, Node o)
    {
        if ( p.equals(rdfType) )
        {
            List<Node> x = transClasses.get(o) ;
            if ( x != null )
                for ( Node c : x )
                    output.send(new Triple(s,p,c)) ;
        }
    }
    
    private void subProperty(Node s, Node p, Node o)
    {
        List<Node> x = transProperties.get(p) ;
        if ( x != null )
        {
            for ( Node p2 : x )
                output.send(new Triple(s,p2,o)) ;
        }
    }
    
    public void flush()
    { output.flush(); }

    public void close()
    { output.close(); }
    
}
/*
 * (c) Copyright 2009 Talis Information Ltd.
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