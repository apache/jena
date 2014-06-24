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

package com.hp.hpl.jena.sparql.algebra;

import java.util.ArrayDeque ;
import java.util.Deque ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP ;
import com.hp.hpl.jena.sparql.algebra.op.OpDistinct ;
import com.hp.hpl.jena.sparql.algebra.op.OpGraph ;
import com.hp.hpl.jena.sparql.algebra.op.OpQuadPattern ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.core.Var ;

/** Convert query in algrebra form so that the default graph of the query is the union of named graphs */  
public class TransformUnionQuery extends TransformCopy
{
    public static Op transform(Op op)
    {
        TransformUnionQuery t = new TransformUnionQuery() ;
        Op op2 = Transformer.transform(t, op, new Pusher(t.currentGraph), new Popper(t.currentGraph)) ;
        return op2 ;
    }

    // ** SEE AlgebraQuad : Pusher and Popper :share.
    Deque<Node> currentGraph = new ArrayDeque<>() ;

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
        private Deque<Node> stack ;
        Pusher(Deque<Node> stack) { this.stack = stack ; }
        @Override
        public void visit(OpGraph opGraph) 
        {
            stack.push(opGraph.getNode()) ;
        }
    }

    static class Popper extends OpVisitorBase
    {
        private Deque<Node> stack ;
        Popper(Deque<Node> stack) { this.stack = stack ; }
        @Override
        public void visit(OpGraph opGraph) 
        {
            Node n = stack.pop() ;
            if ( ! opGraph.getNode().equals(n))
                throw new ARQInternalErrorException() ;
        }
    }
}
