/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.algebra;

import java.util.Stack ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP ;
import com.hp.hpl.jena.sparql.algebra.op.OpDistinct ;
import com.hp.hpl.jena.sparql.algebra.op.OpGraph ;
import com.hp.hpl.jena.sparql.algebra.op.OpQuadPattern ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.core.Var ;

/** Convert query in algrebra form so that the default graph of the query is teh union of named graphs */  
public class TransformUnionQuery extends TransformCopy
{
    public static Op transform(Op op)
    {
        TransformUnionQuery t = new TransformUnionQuery() ;
        Op op2 = Transformer.transform(t, op, new Pusher(t.currentGraph), new Popper(t.currentGraph)) ;
        return op2 ;
    }

    // ** SEE AlgebraQuad : Pusher and Popper :share.

    // General (unquadified) rewrite to make the default graph the
    
    //Deque in Java 6.
    Stack<Node> currentGraph = new Stack<Node>() ;

    public TransformUnionQuery()
    {
        currentGraph.push(Quad.defaultGraphNodeGenerated) ;
    }

    @Override
    public Op transform(OpQuadPattern quadPattern)
    {
        if ( quadPattern.isDefaultGraph() || quadPattern.isUnionGraph() )
        {
            OpBGP opBGP = new OpBGP(quadPattern.getBasicPattern()) ;
            return union(opBGP) ;
        }
        
        // Leave alone.
        // if ( quadPattern.isExplicitDefaultGraph() ) {}
        return super.transform(quadPattern) ;
    }

    @Override
    public Op transform(OpBGP opBGP)
    {
        Node current = currentGraph.peek();
        if ( current == Quad.defaultGraphNodeGenerated || current == Quad.unionGraph )
            return union(opBGP) ;
//        // Turn into a quad pattern.
//        // Not perfect - see AlgebraQuad 
//        return new OpQuadPattern(currentGraph.peek(), opBGP.getPattern()) ;
        // BGP over a named graph.
        // Leave as-is.
        return super.transform(opBGP) ;
    }

    private Op union(OpBGP opBGP)
    {
        // By using the unbinding Var.ANON, the distinct works.
        // Else, we get duplicates from projection out of the graph var. 
        Var v = Var.ANON ; //varAlloc.allocVar() ; 
        Op op = new OpGraph(v, opBGP) ;
        op = OpDistinct.create(op) ;
        return op ;
    }
    
    @Override
    public Op transform(OpGraph opGraph, Op x)
    {
        // Remove any Quad.unionGraph - OpBGPs will be rewritten.
        return super.transform(opGraph, x) ;
    }

    static class Pusher extends OpVisitorBase
    {
        private Stack<Node> stack ;
        Pusher(Stack<Node> stack) { this.stack = stack ; }
        @Override
        public void visit(OpGraph opGraph) 
        {
            stack.push(opGraph.getNode()) ;
        }
    }

    static class Popper extends OpVisitorBase
    {
        private Stack<Node> stack ;
        Popper(Stack<Node> stack) { this.stack = stack ; }
        @Override
        public void visit(OpGraph opGraph) 
        {
            Node n = stack.pop() ;
            if ( ! opGraph.getNode().equals(n))
                throw new ARQInternalErrorException() ;
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