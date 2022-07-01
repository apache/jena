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

import java.util.Iterator ;

import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.algebra.OpVisitor ;
import org.apache.jena.sparql.algebra.OpVisitorBase ;
import org.apache.jena.sparql.algebra.op.* ;
import org.apache.jena.sparql.core.VarExprList ;
import org.apache.jena.sparql.expr.* ;

/** Walk algebra and expressions */
public class WalkerVisitor implements OpVisitorByTypeAndExpr, ExprVisitorFunction {
    protected final ExprVisitor exprVisitor ;
    protected final OpVisitor   opVisitor ;
    protected int               opDepthLimit      = Integer.MAX_VALUE ;
    protected int               exprDepthLimit    = Integer.MAX_VALUE ;

    protected int               opDepth      = 0 ;
    protected int               exprDepth    = 0 ;

    private final OpVisitor     beforeVisitor ;
    private final OpVisitor     afterVisitor ;

    /**
     * A walker. If a visitor is null, then don't walk in. For
     * "no action but keep walking inwards", use {@link OpVisitorBase} and
     * {@link ExprVisitorBase}.
     *
     * @see OpVisitorBase
     * @see ExprVisitorBase
     */
    public WalkerVisitor(OpVisitor opVisitor, ExprVisitor exprVisitor, OpVisitor before, OpVisitor after) {
        this.opVisitor = opVisitor ;
        this.exprVisitor = exprVisitor ;
        if ( opDepthLimit < 0 )
            opDepthLimit = Integer.MAX_VALUE ;
        if ( exprDepth < 0 )
            exprDepthLimit = Integer.MAX_VALUE ;
        opDepth = 0 ;
        exprDepth = 0 ;
        beforeVisitor = before ;
        afterVisitor = after ;
    }

    protected final void before(Op op) {
        if ( beforeVisitor != null )
            op.visit(beforeVisitor) ;
    }

    protected final void after(Op op) {
        if ( afterVisitor != null )
            op.visit(afterVisitor) ;
    }

    public void walk(Op op) {
        if ( op == null )
            return ;
        if ( opDepth == opDepthLimit )
            // No deeper.
            return ;
        opDepth++ ;
        try { op.visit(this); }
        finally { opDepth-- ; }
    }

    public void walk(Expr expr) {
        if ( expr == null )
            return ;
        if ( exprDepth == exprDepthLimit )
            return ;
        exprDepth++ ;
        try { expr.visit(this) ; }
        finally { exprDepth-- ; }
    }

    public void walk(ExprList exprList) {
        if ( exprList == null )
            return ;
        exprList.forEach(e->walk(e));
    }

    public void walk(VarExprList varExprList) {
        if ( varExprList == null )
            return ;
        // retains order.
        varExprList.forEachVarExpr((v,e) -> {
            Expr expr = (e!=null) ? e : Expr.NONE ;
            walk(expr) ;
        });
    }

    // ---- Mode swapping between op and expr. visit=>walk
    @Override
    public void visitExpr(ExprList exprList) {
        if ( exprVisitor != null )
            walk(exprList) ;
    }

    @Override
    public void visitVarExpr(VarExprList varExprList) {
        if ( exprVisitor != null )
            walk(varExprList);
    }

    // ----

    public void visitOp(Op op) {
        before(op) ;
        if ( opVisitor != null )
            op.visit(this);
        after(op) ;
    }

    @Override
    public void visit0(Op0 op) {
        before(op) ;
        if ( opVisitor != null )
            op.visit(opVisitor) ;
        after(op) ;
    }

    @Override
    public void visit1(Op1 op) {
        before(op) ;
        visit1$(op) ;
        after(op) ;
    }

    // Can be called via different routes.
    private void visit1$(Op1 op) {
        if ( op.getSubOp() != null )
            op.getSubOp().visit(this) ;
        if ( opVisitor != null )
            op.visit(opVisitor) ;
    }

    @Override
    public void visit2(Op2 op) {
        before(op) ;
        if ( op.getLeft() != null )
            op.getLeft().visit(this) ;
        if ( op.getRight() != null )
            op.getRight().visit(this) ;
        if ( opVisitor != null )
            op.visit(opVisitor) ;
        after(op) ;
    }

    @Override
    public void visitN(OpN op) {
        before(op) ;
        for (Iterator<Op> iter = op.iterator(); iter.hasNext();) {
            Op sub = iter.next() ;
            sub.visit(this) ;
        }
        if ( opVisitor != null )
            op.visit(opVisitor) ;
        after(op) ;
    }

    @Override
    public void visitExt(OpExt op) {
        before(op) ;
        if ( opVisitor != null )
            op.visit(opVisitor) ;
        after(op) ;
    }

    @Override
    public void visit(OpOrder opOrder) {
        // XXX Why not this?
        // ApplyTransformVisitor handles the parts of OpOrder.
//        before(opOrder) ;
//        visitSortConditions(opOrder.getConditions()) ;
//        visitModifer(opOrder);
//        visit1$(opOrder);
//        after(opOrder) ;
        visit1(opOrder) ;
    }

    @Override
    public void visit(OpAssign opAssign) {
        before(opAssign) ;
        VarExprList varExpr = opAssign.getVarExprList() ;
        visitVarExpr(varExpr);
        visit1$(opAssign) ;
        after(opAssign) ;
    }

    @Override
    public void visit(OpExtend opExtend) {
        before(opExtend) ;
        VarExprList varExpr = opExtend.getVarExprList() ;
        visitVarExpr(varExpr);
        visit1$(opExtend) ;
        after(opExtend) ;
    }

    // Transforming to quads needs the graph node handled before doing the sub-algebra ops
    // so it has to be done as before/after by the Walker. By the time visit(OpGraph) is called,
    // the sub-tree has already been visited.

//    @Override
//    public void visit(OpGraph op) {
//        pushGraph(op.getNode()) ;
//        OpVisitorByTypeAndExpr.super.visit(op) ;
//        popGraph() ;
//    }
//
//    private Deque<Node> stack = new ArrayDeque<>() ;
//
//    public Node getCurrentGraph() { return stack.peek() ; }
//
//    private void pushGraph(Node node) {
//        stack.push(node) ;
//    }
//
//    private void popGraph() {
//        stack.pop() ;
//    }

    @Override
    public void visit(ExprFunction0 func) { visitExprFunction(func) ; }
    @Override
    public void visit(ExprFunction1 func) { visitExprFunction(func) ; }
    @Override
    public void visit(ExprFunction2 func) { visitExprFunction(func) ; }
    @Override
    public void visit(ExprFunction3 func) { visitExprFunction(func) ; }
    @Override
    public void visit(ExprFunctionN func) { visitExprFunction(func) ; }

    @Override
    public void visitExprFunction(ExprFunction func) {
        for ( int i = 1 ; i <= func.numArgs() ; i++ ) {
            Expr expr = func.getArg(i) ;
            if ( expr == null )
                // Put a dummy in, e.g. to keep the transform stack aligned.
                Expr.NONE.visit(this) ;
            else
                expr.visit(this) ;
        }
        if ( exprVisitor != null )
            func.visit(exprVisitor) ;
    }

    @Override
    public void visit(ExprFunctionOp funcOp) {
        walk(funcOp.getGraphPattern());
        if ( exprVisitor != null )
            funcOp.visit(exprVisitor) ;
    }

    @Override
    public void visit(NodeValue nv) {
        if ( exprVisitor != null )
            nv.visit(exprVisitor) ;
    }

    @Override
    public void visit(ExprTripleTerm exTripleTerm) {
        if ( exprVisitor != null )
            exTripleTerm.visit(exprVisitor) ;
    }

    @Override
    public void visit(ExprVar v) {
        if ( exprVisitor != null )
            v.visit(exprVisitor) ;
    }

    @Override
    public void visit(ExprNone none) {
        if ( exprVisitor != null )
            none.visit(exprVisitor) ;
    }

    @Override
    public void visit(ExprAggregator eAgg) {
        // This is the assignment variable of the aggregation
        // not a normal variable of an expression.
        visitAssignVar(eAgg.getAggVar().asVar()) ;
        if ( exprVisitor != null )
            eAgg.visit(exprVisitor) ;
    }
}
