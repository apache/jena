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
    // A pair - a visitor to track the current node and
    // transformation to convert BGPs to Quads.
    // The transform resets the node on the way out.
    // At the moment, it's "before visitor" 
    // Could have an "after visitor as well"
    // This is "visitor" for the non-recusive indirect to an op by type. 
    
    // And paths
    
    private Quadization() { }
    public static Op quadize(Op op)
    {
        // Wire the (pre)vistor to the transformer 
        RecordGraph rg = new RecordGraph() ;
        QuadGraph qg = new QuadGraph(rg) ;
        return Transformer.transform(qg, op, rg) ;
    }
    
    // **** Use BeforeAfterVisitor
    
    // Could build an an Op=>Node map (or just OpBGP=>Node map)
    static class RecordGraph extends OpVisitorBase
    {
        Stack stack = new Stack() ;
        RecordGraph() { stack.push(Quad.defaultGraphNode) ; }

        public void visit(OpGraph opGraph)
        {
            stack.push(opGraph.getNode()) ;
        }
        
        //**** after visitor action.
        public void unvisit(OpGraph opGraph)
        {
            stack.pop() ;
        }
        
        Node getNode() { return (Node)stack.peek(); }
    }
    
    
    
    // Transforms are a bottom-up rewrite. 
    // We want a top-down tracking of the graph node.
    
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
//            if ( gn.equals(Quad.defaultGraphNode ) )
//                return opPath ; 
            
            return new OpGraph(gn , opPath) ;
        }
        
        public Op transform(OpBGP opBGP)
        {
            return new OpQuadPattern(tracker.getNode(), opBGP.getPattern()) ;
        }
    }

    // Applies a visitor before and visitor after a code visit.
    // Can do as just an "apply, one, apply two" but double nesting but that's confusing.
    static class BeforeAfterVisitor implements OpVisitor
    {
        OpVisitor beforeVisitor = null ;
        OpVisitor afterVisitor = null ;
        OpVisitor mainVisitor = null ;
        
        public BeforeAfterVisitor(OpVisitor mainVisitor ,
                                  OpVisitor beforeVisitor, 
                                  OpVisitor afterVisitor) 
        {
            this.mainVisitor = mainVisitor ;
            this.beforeVisitor = beforeVisitor ;
            this.afterVisitor = afterVisitor ;
        }
        
        private void before(Op op)
        { 
            if ( beforeVisitor != null )
                op.visit(beforeVisitor) ;
        }

        private void after(Op op)
        {
            if ( afterVisitor != null )
                op.visit(afterVisitor) ;
        }

        public void visit(OpBGP opBGP)
        { 
            before(opBGP) ; mainVisitor.visit(opBGP) ; after(opBGP) ;
        }
        
        public void visit(OpQuadPattern quadPattern)
        {
			before(quadPattern) ; mainVisitor.visit(quadPattern) ; after(quadPattern) ;
		}
        
        public void visit(OpTriple opTriple)
        {
			before(opTriple) ; mainVisitor.visit(opTriple) ; after(opTriple) ;
		}
        
        public void visit(OpPath opPath)
        {
			before(opPath) ; mainVisitor.visit(opPath) ; after(opPath) ;
		}
        
        public void visit(OpTable opTable)
        {
			before(opTable) ; mainVisitor.visit(opTable) ; after(opTable) ;
		}
        public void visit(OpNull opNull)
        {
			before(opNull) ; mainVisitor.visit(opNull) ; after(opNull) ;
		}
        
        public void visit(OpProcedure opProc)
        {
			before(opProc) ; mainVisitor.visit(opProc) ; after(opProc) ;
		}
        public void visit(OpPropFunc opPropFunc)
        {
			before(opPropFunc) ; mainVisitor.visit(opPropFunc) ; after(opPropFunc) ;
		}
        
        public void visit(OpFilter opFilter)
        {
			before(opFilter) ; mainVisitor.visit(opFilter) ; after(opFilter) ;
		}
        public void visit(OpGraph opGraph)
        {
			before(opGraph) ; mainVisitor.visit(opGraph) ; after(opGraph) ;
		}
        
        public void visit(OpService opService)
        {
			before(opService) ; mainVisitor.visit(opService) ; after(opService) ;
		}
        public void visit(OpDatasetNames dsNames)
        {
			before(dsNames) ; mainVisitor.visit(dsNames) ; after(dsNames) ;
		}
        
        public void visit(OpLabel opLabel)
        {
			before(opLabel) ; mainVisitor.visit(opLabel) ; after(opLabel) ;
		}
        public void visit(OpJoin opJoin)
        {
			before(opJoin) ; mainVisitor.visit(opJoin) ; after(opJoin) ;
		}
        
        public void visit(OpSequence opSequence)
        {
			before(opSequence) ; mainVisitor.visit(opSequence) ; after(opSequence) ;
		}
        
        public void visit(OpLeftJoin opLeftJoin)
        {
			before(opLeftJoin) ; mainVisitor.visit(opLeftJoin) ; after(opLeftJoin) ;
		}
        public void visit(OpDiff opDiff)
        {
			before(opDiff) ; mainVisitor.visit(opDiff) ; after(opDiff) ;
		}
        
        public void visit(OpUnion opUnion)
        {
			before(opUnion) ; mainVisitor.visit(opUnion) ; after(opUnion) ;
		}
        
        public void visit(OpConditional opCondition)
        {
			before(opCondition) ; mainVisitor.visit(opCondition) ; after(opCondition) ;
		}
        public void visit(OpExt opExt)
        {
			before(opExt) ; mainVisitor.visit(opExt) ; after(opExt) ;
		}
        
        public void visit(OpList opList)
        {
			before(opList) ; mainVisitor.visit(opList) ; after(opList) ;
		}
        public void visit(OpOrder opOrder)
        {
			before(opOrder) ; mainVisitor.visit(opOrder) ; after(opOrder) ;
		}
        
        public void visit(OpProject opProject)
        {
			before(opProject) ; mainVisitor.visit(opProject) ; after(opProject) ;
		}
        
        public void visit(OpReduced opReduced)
        {
			before(opReduced) ; mainVisitor.visit(opReduced) ; after(opReduced) ;
		}
        
        public void visit(OpDistinct opDistinct)
        {
			before(opDistinct) ; mainVisitor.visit(opDistinct) ; after(opDistinct) ;
		}
        public void visit(OpSlice opSlice)
        {
			before(opSlice) ; mainVisitor.visit(opSlice) ; after(opSlice) ;
		}
        
        public void visit(OpAssign opAssign)
        {
			before(opAssign) ; mainVisitor.visit(opAssign) ; after(opAssign) ;
		}
        
        public void visit(OpGroupAgg opGroupAgg)
        {
			before(opGroupAgg) ; mainVisitor.visit(opGroupAgg) ; after(opGroupAgg) ;
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
