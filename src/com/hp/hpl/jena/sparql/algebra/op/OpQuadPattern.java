/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.algebra.op;

import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpVisitor;
import com.hp.hpl.jena.sparql.algebra.Transform;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.QuadPattern ;
import com.hp.hpl.jena.sparql.sse.Tags;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap;

public class OpQuadPattern extends Op0
{
    public static boolean isQuadPattern(Op op)
    {
        return (op instanceof OpQuadPattern ) ;
    }
    
    private Node graphNode ;
    private BasicPattern triples ;
    
    private QuadPattern quads = null ;
    
    // A QuadPattern is a block of quads with the same graph arg.
    // i.e. a BasicGraphPattern.  This gets the blank node scoping right.
    
    // Quads are for a specific quad store.
    
    // Later, we may introduce OpQuadBlock for this and OpQuadPattern becomes
    // a sequence of such blocks.
    
    public OpQuadPattern(Node quadNode, BasicPattern triples)
    { 
        this.graphNode = quadNode ;
        this.triples = triples ;
    }
    
    private void initQuads()
    {
        if ( quads == null )
        {
            quads = new QuadPattern() ;
            for (Triple t : triples )
                quads.add(new Quad(graphNode, t)) ;
        }
    }
    
    public QuadPattern getPattern()
    {
        initQuads() ;
        return quads ;
    } 
    
    @Deprecated
    public List<Quad> getQuads()
    {
        initQuads() ;
        return quads.getList() ;
    }
    
    public Node getGraphNode()              { return graphNode ; } 
    public BasicPattern getBasicPattern()   { return triples ; }
    public boolean isEmpty()                { return triples.size() == 0 ; }
    
    public boolean isDefaultGraph()         { return Quad.isQuadDefaultGraphNode(graphNode) ; }
    
    public String getName()                 { return Tags.tagQuadPattern ; }
    @Override
    public Op apply(Transform transform)    { return transform.transform(this) ; } 
    public void visit(OpVisitor opVisitor)  { opVisitor.visit(this) ; }
    @Override
    public Op copy()                        { return new OpQuadPattern(graphNode, triples) ; }

    @Override
    public int hashCode()
    { return graphNode.hashCode() ^ triples.hashCode() ; }

    @Override
    public boolean equalTo(Op other, NodeIsomorphismMap labelMap)
    {
        if ( ! ( other instanceof OpQuadPattern ) ) return false ;
        OpQuadPattern opQuad = (OpQuadPattern)other ;
        if ( ! graphNode.equals(opQuad.graphNode) )
            return false ;
        return triples.equiv(opQuad.triples, labelMap) ;
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