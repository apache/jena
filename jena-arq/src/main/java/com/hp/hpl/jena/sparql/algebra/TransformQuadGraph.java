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

import java.util.Deque ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.algebra.AlgebraQuad.QuadSlot ;
import com.hp.hpl.jena.sparql.algebra.op.* ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.expr.ExprVar ;

/**
 * Transform that rewrites an algebra into quad form
 *
 */
public class TransformQuadGraph extends TransformCopy
{
    private Deque<QuadSlot> tracker ;
    private OpVisitor beforeVisitor ;
    private OpVisitor afterVisitor ;

    public TransformQuadGraph(Deque<QuadSlot> tracker, OpVisitor before, OpVisitor after) {
        this.tracker = tracker ;
        this.beforeVisitor = before ;
        this.afterVisitor = after ;
    }
    
    private Node getNode() { return tracker.peek().rewriteGraphName ; }

    @Override
    public Op transform(OpGraph opGraph, Op op) {
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
        
        QuadSlot qSlot = tracker.peek() ;
        Node actualName= qSlot.actualGraphName ;
        Node rewriteName= qSlot.rewriteGraphName ; 
        
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
        
        if ( actualName != rewriteName )
            op = OpAssign.assign(op, Var.alloc(actualName), new ExprVar(rewriteName)) ;

        // Drop (graph...) because inside nodes
        // have been converted to quads.
        return op ;
    }
    
    @Override
    public Op transform(OpPropFunc opPropFunc, Op subOp) {
        if ( opPropFunc.getSubOp() != subOp )
            opPropFunc = new OpPropFunc(opPropFunc.getProperty(), opPropFunc.getSubjectArgs(), opPropFunc.getObjectArgs(), subOp) ;
        // Put the (graph) back round it so the property function works on the named graph.
        return new OpGraph(getNode() , opPropFunc) ;
    }
    
    @Override
    public Op transform(OpPath opPath) {
        // Put the (graph) back round it
        // ?? inc default graph node.
        return new OpGraph(getNode() , opPath) ;
        // Does not get removed by transform above because this is
        // not the OpGraph that gets walked by the transform.  
    }
    
    @Override
    public Op transform(OpBGP opBGP) { 
        return new OpQuadPattern(getNode(), opBGP.getPattern()) ;
    }
    
    @Override
    public Op transform(OpExt opExt) {
        return opExt.apply(this, beforeVisitor, afterVisitor) ;
    }

}