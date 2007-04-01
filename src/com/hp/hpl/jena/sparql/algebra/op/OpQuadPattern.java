/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.algebra.op;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpVisitor;
import com.hp.hpl.jena.sparql.algebra.Transform;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Quad;

public class OpQuadPattern extends Op0
{
    Node graphNode ;
    BasicPattern triples ;
    List quads = null ;
    
    // A QuadPattern is a block of quads with the same graph arg.
    // i.e. a BasicGraphPattern.  This gets the blank node coping right.
    
    // Quads are for a specific quad store.
    
    // Later, we may introduce OpQuadBlock for this and OpQuadPattern becomes
    // a sequence of such blocks.
    
    public OpQuadPattern(Node quadNode, BasicPattern triples)
    { 
        this.graphNode = quadNode ;
        this.triples = triples ;
    }
    
    public List getQuads()
    {
        if ( quads == null )
        {
            quads = new ArrayList() ;
            for (Iterator iter = triples.iterator() ; iter.hasNext() ; )
            {
                Triple t = (Triple)iter.next() ;
                quads.add(new Quad(graphNode, t)) ;
            } 
        }
        return quads ; 
    }
    
    public Node getGraphNode()              { return graphNode ; } 
    public BasicPattern getBasicPattern()   { return triples ; }
    
    public String getName()                 { return "quadpattern" ; }
    public Op apply(Transform transform)    { return transform.transform(this) ; } 
    public void visit(OpVisitor opVisitor)  { opVisitor.visit(this) ; }
    public Op copy()                        { return new OpQuadPattern(graphNode, triples) ; }
}

/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
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