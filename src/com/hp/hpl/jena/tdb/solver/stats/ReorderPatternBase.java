/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.solver.stats;

import static iterator.Iter.map;
import static iterator.Iter.toList;
import iterator.Transform;

import java.util.List;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Var;

import com.hp.hpl.jena.tdb.solver.ReorderPattern;

/** Machinary */
public abstract class ReorderPatternBase implements ReorderPattern
{
    public final BasicPattern reorder(Graph graph, BasicPattern pattern)
    {
        if (pattern.size() == 0 )
            return pattern ;
        @SuppressWarnings("unchecked")
        List<Triple> triples = (List<Triple>)pattern.getList() ;
        BasicPattern bgp = new BasicPattern() ;

        List<PatternTriple> components = toList(map(triples, new Transform<Triple, PatternTriple>(){
            @Override
            public PatternTriple convert(Triple triple)
            {
                return new PatternTriple(triple) ;
            }})) ;

        reorder(graph, triples, components, bgp) ;
        return bgp ;
    }

    protected abstract void reorder(Graph graph, List<Triple> triples, List<PatternTriple> components, BasicPattern bgp) ;
    
    /** Update components to note any variables from triple */
    protected final void update(Triple triple, List<PatternTriple> components)
    {
        for ( PatternTriple elt : components )
            if ( elt != null ) update(triple, elt) ;
    }

    
    private void update(Triple triple, PatternTriple tuple)
    {
        update(triple.getSubject(), tuple) ;
        update(triple.getPredicate(), tuple) ;
        update(triple.getObject(), tuple) ;
    }

    private void update(Node node, PatternTriple elt)
    {
        
        if ( Var.isVar(node) )
        {
            if ( elt.subject.equals(node) )
                elt.subject = PatternElements.TERM ;
            if ( elt.predicate.equals(node) )
                elt.predicate = PatternElements.TERM ;
            if ( elt.object.equals(node) )
                elt.object = PatternElements.TERM ;
        }
           
    }
}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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