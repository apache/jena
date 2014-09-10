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

/**
 * This is the operation in stadard SPARQL 1.1 OpAssign is specifically in
 * support of LET.
 */
public class OpExtend extends OpExtendAssign {
    // There factory operations compress nested assignments if possible.
    // Not possible if it's the reassignment of something already assigned.

    /** Create an OpExtend or add to an existing one.
     * This coperation collapses what woudl otherwise be stacks
     * of OpExtend.
     */ 
    static public Op extend(Op op, Var var, Expr expr) {
        if ( !(op instanceof OpExtend) )
            return create(op, var, expr) ;

        OpExtend opExtend = (OpExtend)op ;
        if ( opExtend.assignments.contains(var) )
            return create(op, var, expr) ;

        opExtend.assignments.add(var, expr) ;
        return opExtend ;
    }

    /** Create an OpExtend or add to an existing one.
     * This operation collapses what would otherwise be stacks
     * of OpExtend.
     */ 
    static public Op extend(Op op, VarExprList exprs) {
        if ( !(op instanceof OpExtend) )
            return create(op, exprs) ;

        OpExtend opExtend = (OpExtend)op ;
        for (Var var : exprs.getVars()) {
            if ( opExtend.assignments.contains(var) )
                return create(op, exprs) ;
        }

        opExtend.assignments.addAll(exprs) ;
        return opExtend ;
    }

    /** Make a OpExtend - this does not aggregate (extend .. (extend ...)) */
    public static OpExtend create(Op op, VarExprList exprs) {
        return new OpExtend(op, exprs) ;
    }

    /** Make a OpExtend - this does not aggregate (extend .. (extend ...)) */
    public static Op create(Op op, Var var, Expr expr) {
        VarExprList x = new VarExprList() ;
        x.add(var, expr) ;
        return new OpExtend(op, x) ;
    }

    private OpExtend(Op subOp) {
        super(subOp) ;
    }

    private OpExtend(Op subOp, VarExprList exprs) {
        super(subOp, exprs) ;
    }

    @Override
    public String getName() {
        return Tags.tagExtend ;
    }

    @Override
    public void visit(OpVisitor opVisitor) {
        opVisitor.visit(this) ;
    }

    @Override
    public Op1 copy(Op subOp) {
        OpExtend op = new OpExtend(subOp, new VarExprList(getVarExprList())) ;
        return op ;
    }

    @Override
    public boolean equalTo(Op other, NodeIsomorphismMap labelMap) {
        if ( !(other instanceof OpExtend) )
            return false ;
        OpExtend assign = (OpExtend)other ;

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
        return new OpExtend(subOp, varExprList) ;
    }
}
