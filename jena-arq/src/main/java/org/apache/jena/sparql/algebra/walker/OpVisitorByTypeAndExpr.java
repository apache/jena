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

package org.apache.jena.sparql.algebra.walker;

import java.util.List ;

import org.apache.jena.query.SortCondition ;
import org.apache.jena.sparql.algebra.OpVisitor ;
import org.apache.jena.sparql.algebra.op.* ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.core.VarExprList ;
import org.apache.jena.sparql.expr.ExprAggregator ;
import org.apache.jena.sparql.expr.ExprList ;

/** A visitor helper that maps all visits to a few general ones.
 *  Includes visiting expressions, sort conditions etc
 */
public interface OpVisitorByTypeAndExpr extends OpVisitor
{
    public void visit0(Op0 op) ;

    public void visit1(Op1 op) ;

    public void visit2(Op2 op) ;

    public void visitN(OpN op) ;

    public default void visitExt(OpExt op) {
        op.effectiveOp().visit(this);
    }

    public void visitExpr(ExprList exprs) ;
    public void visitVarExpr(VarExprList exprs) ;
    public default void visitAssignVar(Var var) {}

    // Currently, we assume these are handled by the visitor/transformer.
    public default void visitSortConditions(List<SortCondition> list)       {}
    public default void visitAggregators(List<ExprAggregator> aggregators)  {}

    public default void visitModifer(OpModifier opMod) {
        visit1(opMod);
    }

    @Override
    public default void visit(OpBGP opBGP) {
        visit0(opBGP);
    }

    @Override
    public default void visit(OpQuadPattern quadPattern) {
        visit0(quadPattern);
    }

    @Override
    public default void visit(OpQuadBlock quadBlock) {
        visit0(quadBlock);
    }

    @Override
    public default void visit(OpTriple opTriple) {
        visit0(opTriple);
    }

    @Override
    public default void visit(OpQuad opQuad) {
        visit0(opQuad);
    }

    @Override
    public default void visit(OpPath opPath) {
        visit0(opPath);
    }

    @Override
    public default void visit(OpProcedure opProcedure) {
        visit1(opProcedure);
    }

    @Override
    public default void visit(OpPropFunc opPropFunc) {
        visit1(opPropFunc);
    }

    @Override
    public default void visit(OpJoin opJoin) {
        visit2(opJoin);
    }

    @Override
    public default void visit(OpSequence opSequence) {
        visitN(opSequence);
    }

    @Override
    public default void visit(OpDisjunction opDisjunction) {
        visitN(opDisjunction);
    }

    @Override
    public default void visit(OpLeftJoin opLeftJoin) {
        visitExpr(opLeftJoin.getExprs());
        visit2(opLeftJoin);
    }

    @Override
    public default void visit(OpDiff opDiff) {
        visit2(opDiff);
    }

    @Override
    public default void visit(OpMinus opMinus) {
        visit2(opMinus);
    }

    @Override
    public default void visit(OpUnion opUnion) {
        visit2(opUnion);
    }

    @Override
    public default void visit(OpConditional opCond) {
        visit2(opCond);
    }

    @Override
    public default void visit(OpFilter opFilter) {
        visitExpr(opFilter.getExprs());
        visit1(opFilter);
    }

    @Override
    public default void visit(OpGraph opGraph) {
        visit1(opGraph);
    }

    @Override
    public default void visit(OpService opService) {
        visit1(opService);
    }

    @Override
    public default void visit(OpDatasetNames dsNames) {
        visit0(dsNames);
    }

    @Override
    public default void visit(OpTable opUnit) {
        visit0(opUnit);
    }

    @Override
    public default void visit(OpExt opExt) {
        visitExt(opExt);
    }

    @Override
    public default void visit(OpNull opNull) {
        visit0(opNull);
    }

    @Override
    public default void visit(OpLabel opLabel) {
        visit1(opLabel);
    }

    @Override
    public default void visit(OpOrder opOrder) {
        visitSortConditions(opOrder.getConditions()) ;
        visitModifer(opOrder);
    }

    @Override
    public default void visit(OpGroup opGroup) {
        visitVarExpr(opGroup.getGroupVars()) ;
        visitAggregators(opGroup.getAggregators()) ;
        visit1(opGroup);
    }

    @Override
    public default void visit(OpTopN opTop) {
        visitSortConditions(opTop.getConditions()) ;
        visit1(opTop);
    }

    @Override
    public default void visit(OpAssign opAssign) {
        visitVarExpr(opAssign.getVarExprList()) ;
        visit1(opAssign);
    }

    @Override
    public default void visit(OpExtend opExtend) {
        visitVarExpr(opExtend.getVarExprList()) ;
        visit1(opExtend);
    }

    @Override
    public default void visit(OpList opList) {
        visitModifer(opList);
    }

    @Override
    public default void visit(OpProject opProject) {
        visitModifer(opProject);
    }

    @Override
    public default void visit(OpReduced opReduced) {
        visitModifer(opReduced);
    }

    @Override
    public default void visit(OpDistinct opDistinct) {
        visitModifer(opDistinct);
    }

    @Override
    public default void visit(OpSlice opSlice) {
        visitModifer(opSlice);
    }
}
