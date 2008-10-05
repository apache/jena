/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import java.util.Stack;

import com.hp.hpl.jena.graph.Node;

import com.hp.hpl.jena.sparql.algebra.*;
import com.hp.hpl.jena.sparql.algebra.op.*;
import com.hp.hpl.jena.sparql.core.Quad;

public class Quadization extends TransformCopy
{
    // Transform to a quad form:
    //   BGPs go to quad patterns
    //   Paths (complex) go to (graph (path ...)) [later: quad paths] 
    //   Drop OpGraph 

    // Done as a before/after pair to run the stack of graph
    // nodes and a transform to do the conversion. 
    
    // And paths
    
    private Quadization() { }
    public static Op quadize(Op op)
    {
        final Stack stack = new Stack() ;
        stack.push(Quad.defaultGraphNode) ; // Starting condition
        
        OpVisitor before = new Pusher(stack) ;
        OpVisitor after = new Popper(stack) ;
        
        TransformQuadGraph qg = new TransformQuadGraph(stack) ;
        return Transformer.transform(qg, op, before, after) ;
    }
    
    private static class Pusher extends OpVisitorBase
    {
        Stack stack ;
        Pusher(Stack stack) { this.stack = stack ; }
        public void visit(OpGraph opGraph)
        {
            stack.push(opGraph.getNode()) ;
        }
    }
    
    private static class Popper extends OpVisitorBase
    {
        Stack stack ;
        Popper(Stack stack) { this.stack = stack ; }
        public void visit(OpGraph opGraph)
        {
            Node n = (Node)stack.pop() ;
        }
    }

    private static class TransformQuadGraph extends TransformCopy
    {
        private Stack tracker ;

        public TransformQuadGraph(Stack tracker) { this.tracker = tracker ; }
        private Node getNode() { return (Node)tracker.peek() ; }

        public Op transform(OpGraph opGraph, Op op)
        {
            
            boolean noPattern = false ;
            
            if ( OpBGP.isBGP(op) )
            {
                if ( ((OpBGP)op).getPattern().isEmpty() )
                    noPattern = true ;
            }
            else if ( op instanceof OpTable )
            {
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
            
            // Drop (graph...) because inside nodes have been converted
            // to quads.  Or 
            return op ;
        }
        
        public Op transform(OpPath opPath)
        {
            // Put the (graph) back round it
            // ?? inc default graph node.
            return new OpGraph(getNode() , opPath) ;
            // Does not get removed by transform above. 
        }
        
        public Op transform(OpBGP opBGP)
        {
            return new OpQuadPattern(getNode(), opBGP.getPattern()) ;
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
