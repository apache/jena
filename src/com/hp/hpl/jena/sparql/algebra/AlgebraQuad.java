/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.algebra;

import java.util.Stack;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpDatasetNames;
import com.hp.hpl.jena.sparql.algebra.op.OpGraph;
import com.hp.hpl.jena.sparql.algebra.op.OpPath;
import com.hp.hpl.jena.sparql.algebra.op.OpQuadPattern;
import com.hp.hpl.jena.sparql.algebra.op.OpTable;
import com.hp.hpl.jena.sparql.core.Quad;

/*8 COnvert an algebra expression into a quad form */
public class AlgebraQuad extends TransformCopy
{
    // Transform to a quad form:
    //   + BGPs go to quad patterns
    //   + Drop (previous) OpGraph 
    //   + Paths (complex - simple ones are flatten elsewhere) go to (graph (path ...)) [later: quad paths] 

    // Done as a before/after pair to run the stack of graph
    // nodes and a transform to do the conversion. 
    
    private AlgebraQuad() { }

    public static Op quadize(Op op)
    {
        final Stack<Node> stack = new Stack<Node>() ;
        stack.push(Quad.defaultGraphNodeGenerated) ;             // Starting condition
        
        OpVisitor before = new Pusher(stack) ;
        OpVisitor after = new Popper(stack) ;
        
        TransformQuadGraph qg = new TransformQuadGraph(stack) ;
        return Transformer.transform(qg, op, before, after) ;
    }
    
    private static class Pusher extends OpVisitorBase
    {
        Stack<Node> stack ;
        Pusher(Stack<Node> stack) { this.stack = stack ; }
        @Override
        public void visit(OpGraph opGraph)
        {
            stack.push(opGraph.getNode()) ;
        }
    }
    
    private static class Popper extends OpVisitorBase
    {
        Stack<Node> stack ;
        Popper(Stack<Node> stack) { this.stack = stack ; }
        @Override
        public void visit(OpGraph opGraph)
        {
            Node n = stack.pop() ;
        }
    }

    private static class TransformQuadGraph extends TransformCopy
    {
        private Stack<Node> tracker ;

        public TransformQuadGraph(Stack<Node> tracker) { this.tracker = tracker ; }
        private Node getNode() { return tracker.peek() ; }

        @Override
        public Op transform(OpGraph opGraph, Op op)
        {
            boolean noPattern = false ;
            
            if ( OpBGP.isBGP(op) )
            {
                // Empty BGP
                if ( ((OpBGP)op).getPattern().isEmpty() )
                    noPattern = true ;
            }
            else if ( op instanceof OpTable )
            {
                // Empty BGP compiled to a unit table
                if ( ((OpTable)op).isJoinIdentity() )
                    noPattern = true ;
            }
            
            if ( noPattern )
            {
                // The case of something like:
                // GRAPH ?g {} or GRAPH <v> {}
                // which are ways of accessing the names in the dataset.
                return new OpDatasetNames(opGraph.getNode()) ;
            }
            
            // Drop (graph...) because inside nodes
            // have been converted to quads.
            return op ;
        }
        
        @Override
        public Op transform(OpPath opPath)
        {
            // Put the (graph) back round it
            // ?? inc default graph node.
            return new OpGraph(getNode() , opPath) ;
            // Does not get removed by transform above because this is
            // not the OpGraph that gets walked by the transform.  
        }
        
        @Override
        public Op transform(OpBGP opBGP)
        {
            return new OpQuadPattern(getNode(), opBGP.getPattern()) ;
        }
    }    
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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
