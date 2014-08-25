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
import java.util.Collection ;
import java.util.Deque ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.ARQConstants ;
import com.hp.hpl.jena.sparql.algebra.op.* ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.core.VarAlloc ;

/** 
 * Helper class for converting an algebra expression into a quad form 
 * */
public class AlgebraQuad
{
    // Transform to a quad form:
    //   + BGPs go to quad patterns
    //   + Drop (previous) OpGraph 
    //   + Paths (complex - simple ones are flatten elsewhere) go to (graph (path ...)) [later: quad paths] 

    // Done as a before/after pair to run the stack of graph nodes for rewrite.
    // Need to be careful of use of a variable in GRAPH ?g { .. } and then use ?g inside the pattern.
    
    private AlgebraQuad() { }

    public static Op quadize(Op op)
    {
        final Deque<QuadSlot> stack = new ArrayDeque<>() ;
        QuadSlot qSlot = new QuadSlot(Quad.defaultGraphNodeGenerated, Quad.defaultGraphNodeGenerated) ;  
        stack.push(qSlot) ;             // Starting condition
        
        OpVisitor before = new Pusher(stack) ;
        OpVisitor after = new Popper(stack) ;
        
        TransformQuadGraph qg = new TransformQuadGraph(stack, before, after) ;
        return Transformer.transformSkipService(qg, op, before, after) ;
    }
    
    /** This is the record of the transformation.
     *  The rewriteGraphName is the node to put in the graph slot of the quad.
     *  The actualGraphName is the node used in SPARQL.
     *  If they are the same (by ==), the quadrewrite is OK as is.
     *  If they are different (and that means they are variables)
     *  an assign is done after the execution of the graph pattern block. 
     */
    static class QuadSlot
    {   // Oh scala, where art thou!
        final Node actualGraphName ;
        final Node rewriteGraphName ;
        QuadSlot(Node actualGraphName, Node rewriteGraphName)
        {
            this.actualGraphName = actualGraphName ;
            this.rewriteGraphName = rewriteGraphName ;
        }
        @Override public String toString() { return "actualGraphName="+actualGraphName+" rewriteGraphName="+rewriteGraphName ; } 
    }
    
    private static class Pusher extends OpVisitorBase
    {
        Deque<QuadSlot> stack ;
        VarAlloc varAlloc = new VarAlloc(ARQConstants.allocVarQuad) ;
        Pusher(Deque<QuadSlot> stack) { this.stack = stack ; }
        @Override
        public void visit(OpGraph opGraph)
        {
            // Name in SPARQL
            Node gn = opGraph.getNode() ;
            // Name in rewrite
            Node gnQuad = gn ;
            
            if ( Var.isVar(gn) )
            {
                Collection<Var> vars = OpVars.mentionedVars(opGraph.getSubOp()) ;
                if ( vars.contains(gn) )
                    gnQuad = varAlloc.allocVar() ;
            }
            stack.push(new QuadSlot(gn, gnQuad)) ;
        }
    }
    
    private static class Popper extends OpVisitorBase
    {
        Deque<QuadSlot> stack ;
        Popper(Deque<QuadSlot> stack) { this.stack = stack ; }
        @Override
        public void visit(OpGraph opGraph)
        {
            // The final work is done in the main vistor, 
            // which is called after the subnode has been 
            // rewritten.
            stack.pop() ;
        }
    }    
}
