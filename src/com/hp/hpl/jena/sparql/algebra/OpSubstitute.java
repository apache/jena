/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.algebra;

import java.util.Iterator;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.ARQInternalErrorException;
import com.hp.hpl.jena.sparql.algebra.op.*;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.expr.ExprList;

public class OpSubstitute extends TransformCopy
{
    private Binding binding ;
    
    public static Op substitute(Op op, Binding b)
    {
        return Transformer.transform(new OpSubstitute(b), op) ;
    }
    
    public OpSubstitute(Binding binding) 
    {
        super(TransformCopy.COPY_ALWAYS) ;
        this.binding = binding ;
    }

    //@Override
    public Op transform(OpBGP bgp)
    {
        BasicPattern triples = new BasicPattern() ;
        for ( Iterator iter = bgp.getPattern().iterator() ; iter.hasNext() ; )
        {
            Triple triple = (Triple)iter.next() ;
            Node s = substitute(binding, triple.getSubject()) ;
            Node p = substitute(binding, triple.getPredicate()) ;
            Node o = substitute(binding, triple.getObject()) ;
            Triple t = new Triple(s, p, o) ;
            triples.add(t) ;
        }
        return new OpBGP(triples) ;
    }
    
    //@Override
    public Op transform(OpQuadPattern quadPattern)
    {
        Node gNode = quadPattern.getGraphNode() ;
        Node g = substitute(binding, gNode) ;
        
        BasicPattern triples = new BasicPattern() ;
        for ( Iterator iter = quadPattern.getQuads().iterator() ; iter.hasNext() ; )
        {
            Quad quad = (Quad)iter.next() ;
            if ( ! quad.getGraph().equals(gNode) )
                throw new ARQInternalErrorException("Internal error: quads block is not uniform over the graph node") ;
            Node s = substitute(binding, quad.getSubject()) ;
            Node p = substitute(binding, quad.getPredicate()) ;
            Node o = substitute(binding, quad.getObject()) ;
            Triple t = new Triple(s, p, o) ;
            triples.add(t) ;
        }
        
        return new OpQuadPattern(g, triples) ;
    }
    
    //@Override
    public Op transform(OpFilter filter, Op op)
    {
        ExprList exprs = filter.getExprs().copySubstitute(binding, true) ;
        return OpFilter.filter(exprs, op) ; 
    }

    public Op transform(OpGraph op, Op sub)
    {
        Node n = substitute(binding, op.getNode()) ;
        return new OpGraph(n, sub) ;
    }
    
    public Op transform(OpService op, Op sub)
    {
        Node n = substitute(binding, op.getService()) ;
        return new OpService(n, sub) ;
    }
    
    private static Node substitute(Binding b, Node n)
    {
        if ( ! Var.isVar(n) )
            return n ;
        Var v = Var.alloc(n) ;
        Node x = b.get(v) ;
        if ( x == null )
            return n ;
        return x ;
    }
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