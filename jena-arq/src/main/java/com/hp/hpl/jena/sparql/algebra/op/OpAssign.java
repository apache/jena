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

package com.hp.hpl.jena.sparql.algebra.op ;

import org.apache.jena.atlas.lib.Lib ;

import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.OpVisitor ;
import com.hp.hpl.jena.sparql.algebra.Transform ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.core.VarExprList ;
import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.sse.Tags ;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap ;

public class OpAssign extends OpExtendAssign {
    // These factory operations compress nested assignments if possible.
    // Not possible if it's the reassignment of something already assigned.

    /** Create an OpAssign or add to an existing one.
     * This coperation collapses what woudl otherwise be stacks
     * of OpExtend.
     */ 
    static public Op assign(Op op, Var var, Expr expr) {
        if ( !(op instanceof OpAssign) )
            return create(op, var, expr) ;

        OpAssign opAssign = (OpAssign)op ;
        if ( opAssign.assignments.contains(var) )
            // Same variable :
            // Layer one assignment over the top of another
            return create(op, var, expr) ;

        opAssign.add(var, expr) ;
        return opAssign ;
    }

    /** Create an v or add to an existing one.
     * This operation collapses what would otherwise be stacks
     * of OpAssign.
     */ 
    static public Op assign(Op op, VarExprList exprs) {
        if ( !(op instanceof OpAssign) )
            return create(op, exprs) ;

        OpAssign opAssign = (OpAssign)op ;
        for (Var var : exprs.getVars()) {
            if ( opAssign.assignments.contains(var) )
                return create(op, exprs) ;
        }

        opAssign.assignments.addAll(exprs) ;
        return opAssign ;
    }

    /** Make a OpAssign - this does not aggregate (assign .. (assign ...)) */
    public static OpAssign create(Op op, VarExprList exprs) {
        return new OpAssign(op, exprs) ;
    }

    /** Make a OpAssign - this does not aggregate (assign .. (assign ...)) */
    static private Op create(Op op, Var var, Expr expr) {
        return new OpAssign(op, new VarExprList(var, expr)) ;
    }

    private OpAssign(Op subOp) {
        super(subOp) ;
    }

    private OpAssign(Op subOp, VarExprList exprs) {
        super(subOp, exprs) ;
    }

    @Override
    public String getName() {
        return Tags.tagAssign ;
    }

    @Override
    public void visit(OpVisitor opVisitor) {
        opVisitor.visit(this) ;
    }

    @Override
    public Op1 copy(Op subOp) {
        OpAssign op = new OpAssign(subOp, new VarExprList(getVarExprList())) ;
        return op ;
    }
    
    @Override
    public boolean equalTo(Op other, NodeIsomorphismMap labelMap) {
        if ( !(other instanceof OpAssign) )
            return false ;
        OpAssign assign = (OpAssign)other ;

        if ( !Lib.equal(assignments, assign.assignments) )
            return false ;
        return getSubOp().equalTo(assign.getSubOp(), labelMap) ;
    }

    @Override
    public Op apply(Transform transform, Op subOp) {
        return transform.transform(this, subOp) ;
    }

    @Override
    public OpExtendAssign copy(Op subOp, VarExprList varExprList) {
        return new OpAssign(subOp, varExprList) ;
    }
}
