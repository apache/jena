/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.riot.pipeline.inf;

import java.util.List ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.vocabulary.RDF ;


/** Apply a fixed set of inference rules to a stream of triples.
 *  This is inference on the A-Box (the data) with respect to a fixed T-Box
 *  (the vocabulary, ontology).
 *  <ul>
 *  <li>rdfs:subClassOf (transitive)</li>
 *  <li>rdfs:subPropertyOf (transitive)</li>
 *  <li>rdfs:domain</li>
 *  <li>rdfs:range</li>
 *  </ul>
 *  
 *  Usage: call process(Node, Node, Node), outptus to derive(Node, Node, Node).
 */


abstract class InferenceProcessorRDFS implements Processor
{
    // Calculates hierarchies (subclass, subproperty) from a model.
    // Assumes that model has no metavocabulary (use an inferencer on the model first if necessary).
    
    // Todo:
    //   rdfs:member
    //   list:member ???
    
    // Todo:
    //   rdfs:member
    //   list:member ???
    
    // Work in NodeID space?  But Node id caching solves most of the problems.
    
    // Expanded hierarchy:
    // If C < C1 < C2 then C2 is in the list for C 

    
    static final Node rdfType = RDF.type.asNode() ;
    private final InferenceSetupRDFS state ;
    
    public InferenceProcessorRDFS(InferenceSetupRDFS state)
    {
        this.state = state ;
    }

    public void process(Node s, Node p, Node o)
    {
        subClass(s,p,o) ;
        subProperty(s,p,o) ;

        // domain() and range() also go through subClass processing. 
        domain(s,p,o) ;
        range(s,p,o) ;
    }

    public abstract void derive(Node s, Node p, Node o) ;

    /*
     * [rdfs8:  (?a rdfs:subClassOf ?b), (?b rdfs:subClassOf ?c) -> (?a rdfs:subClassOf ?c)] 
     * [rdfs9:  (?x rdfs:subClassOf ?y), (?a rdf:type ?x) -> (?a rdf:type ?y)] 
     */
    final private void subClass(Node s, Node p, Node o)
    {
        if ( p.equals(rdfType) )
        {
            List<Node> x = state.transClasses.get(o) ;
            if ( x != null )
                for ( Node c : x )
                    derive(s,p,c) ;
        }
    }

    // Rule extracts from Jena's RDFS rules etc/rdfs.rules 
    
    /*
     * [rdfs5a: (?a rdfs:subPropertyOf ?b), (?b rdfs:subPropertyOf ?c) -> (?a rdfs:subPropertyOf ?c)] 
     * [rdfs6:  (?a ?p ?b), (?p rdfs:subPropertyOf ?q) -> (?a ?q ?b)] 
     */
    private void subProperty(Node s, Node p, Node o)
    {
        List<Node> x = state.transProperties.get(p) ;
        if ( x != null )
        {
            for ( Node p2 : x )
                derive(s, p2, o) ;
        }
    }

    /*
     * [rdfs2:  (?p rdfs:domain ?c) -> [(?x rdf:type ?c) <- (?x ?p ?y)] ]
     * [rdfs9:  (?x rdfs:subClassOf ?y), (?a rdf:type ?x) -> (?a rdf:type ?y)]  
     */
    final private void domain(Node s, Node p, Node o)
    {
        List<Node> x = state.domainList.get(p) ;
        if ( x != null )
        {
            for ( Node c : x )
            {
                derive(s, rdfType, c) ;
                subClass(s, rdfType, c) ;
            }
        }
    }

    /*
     * [rdfs3:  (?p rdfs:range ?c)  -> [(?y rdf:type ?c) <- (?x ?p ?y)] ]
     * [rdfs9:  (?x rdfs:subClassOf ?y), (?a rdf:type ?x) -> (?a rdf:type ?y)]  
     */ 
    final private void range(Node s, Node p, Node o)
    {
        // Mask out literal subjects
        if ( o.isLiteral() )
            return ;
        // Range
        List<Node> x = state.rangeList.get(p) ;
        if ( x != null )
        {
            for ( Node c : x )
            {
                derive(o, rdfType, c) ;
                subClass(o, rdfType, c) ;
            }
        }
    }
}
/*
 * (c) Copyright 2010 Talis Systems Ltd.
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