/**
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

import java.util.Collection ;
import java.util.Stack ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.ARQConstants ;
import com.hp.hpl.jena.sparql.algebra.op.OpAssign ;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP ;
import com.hp.hpl.jena.sparql.algebra.op.OpDatasetNames ;
import com.hp.hpl.jena.sparql.algebra.op.OpGraph ;
import com.hp.hpl.jena.sparql.algebra.op.OpPath ;
import com.hp.hpl.jena.sparql.algebra.op.OpPropFunc ;
import com.hp.hpl.jena.sparql.algebra.op.OpQuadPattern ;
import com.hp.hpl.jena.sparql.algebra.op.OpTable ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.Rename ;
import com.hp.hpl.jena.sparql.expr.ExprVar ;

/** Convert an algebra expression into a quad form */
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
        return Transformer.transformSkipService(qg, op, before, after) ;
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
            // ?? Could just leave the (graph) in place always - just rewrite BGPs. 
            boolean noPattern = false ;
            
            /* One case to consider is when the pattern for the GRAPH
             * statement includes uses the variable inside the GRAPH clause. 
             * In this case, we must rename away the inner variable
             * to allow stream execution via index joins, 
             * and then put back the value via an assign.
             * (This is what QueryIterGraph does using a streaming join
             * for triples)
             */

            // Note: op is already quads by this point.
            // Must test scoping by the subOp of GRAPH
            
            Node gn = getNode() ;
            if ( Var.isVar(gn) )
            {
                Collection<Var> vars = OpVars.allVars(opGraph.getSubOp()) ;
                if ( vars.contains(gn) )
                {
                    Var gVar = Var.alloc(gn) ;
                    Var var = Rename.chooseVarName(gVar, vars, ARQConstants.allocVarQuad) ;
                    op = Rename.renameNode(op, gn, var) ;
                    op = OpAssign.assign(op, gVar, new ExprVar(var)) ;
                }
            }
            
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
        public Op transform(OpPropFunc opPropFunc, Op subOp)
        {
            if ( opPropFunc.getSubOp() != subOp )
                opPropFunc = new OpPropFunc(opPropFunc.getProperty(), opPropFunc.getSubjectArgs(), opPropFunc.getObjectArgs(), subOp) ;
            // Put the (graph) back round it so the property function works on the named graph.
            return new OpGraph(getNode() , opPropFunc) ;
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
