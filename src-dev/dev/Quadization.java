/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import java.util.Stack;

import com.hp.hpl.jena.graph.Node;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpVisitorBase;
import com.hp.hpl.jena.sparql.algebra.TransformCopy;
import com.hp.hpl.jena.sparql.algebra.Transformer;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpGraph;
import com.hp.hpl.jena.sparql.algebra.op.OpPath;
import com.hp.hpl.jena.sparql.algebra.op.OpQuadPattern;
import com.hp.hpl.jena.sparql.core.Quad;

public class Quadization extends TransformCopy
{
    // A pair - a visitor to track the current node in
    // A transformation to convert BGPs to Quads
    // and reset the node on the way out.
    // And paths
    
    private Quadization() { }
    public static Op quadize(Op op)
    {
        // Wire the (pre)vistor to the transformer 
        RecordGraph rg = new RecordGraph() ;
        QuadGraph qg = new QuadGraph(rg) ;
        return Transformer.transform(qg, op, rg) ;
    }
    
    static class RecordGraph extends OpVisitorBase
    {
        Stack stack = new Stack() ;
        RecordGraph() { stack.push(Quad.defaultGraphNode) ; }

        public void visit(OpGraph opGraph)
        {
            stack.push(opGraph.getNode()) ;
        }
        
        public void unvisit(OpGraph opGraph)
        {
            stack.pop() ;
        }
        
        Node getNode() { return (Node)stack.peek(); }
    }
    
    static class QuadGraph extends TransformCopy
    {
        private RecordGraph tracker ;

        public QuadGraph(RecordGraph tracker) { this.tracker = tracker ; }
        
        public Op transform(OpGraph opGraph, Op subOp)
        {
            // On the way out, we reset the currentGraph
            // and return the transformed subtree
            tracker.unvisit(opGraph) ;
            return subOp ;
        }
        
        public Op transform(OpPath opPath)
        {
            // Put the graph back round it
            // ?? inc default graph node.
            Node gn = tracker.getNode() ;
            if ( gn.equals(Quad.defaultGraphNode ) )
                return opPath ; 
            
            return new OpGraph(gn , opPath) ;
        }
        
        public Op transform(OpBGP opBGP)
        {
            return new OpQuadPattern(tracker.getNode(), opBGP.getPattern()) ;
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