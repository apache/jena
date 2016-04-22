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

import java.util.* ;

import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.query.SortCondition ;
import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.algebra.OpVisitor ;
import org.apache.jena.sparql.algebra.Transform ;
import org.apache.jena.sparql.algebra.op.* ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.core.VarExprList ;
import org.apache.jena.sparql.expr.* ;
import org.apache.jena.sparql.expr.aggregate.Aggregator ;

public class ApplyTransformVisitor implements OpVisitorByTypeAndExpr, ExprVisitor {
    private final Transform     opTransform ;
    private final ExprTransform exprTransform ;

    protected boolean           visitService = true ;
    
    private final Deque<Op>     opStack   = new ArrayDeque<>() ;
    private final Deque<Expr>   exprStack = new ArrayDeque<>() ;
    
    private final OpVisitor     beforeVisitor ;
    private final OpVisitor     afterVisitor ;

    public ApplyTransformVisitor(Transform opTransform, ExprTransform exprTransform, OpVisitor before, OpVisitor after) {
        this.opTransform = opTransform ;
        this.exprTransform = exprTransform ;
        this.beforeVisitor = before ;
        this.afterVisitor = after ;
    }

    /*package*/ final Op opResult() {
        return pop(opStack) ;
    }

    /*package*/ final Expr exprResult() {
        return pop(exprStack) ;
    }

    protected Op transform(Op op) {
        // reuse this ApplyTransformVisitor? with stack checking?
        return Walker.transform(op, this, beforeVisitor, afterVisitor) ;
    }
    
    protected Expr transform(Expr expr) {
        // reuse this ApplyTransformVisitor? with stack checking?
        return Walker.transform(expr, this, beforeVisitor, afterVisitor) ;
    }
    
    protected ExprList transform(ExprList exprList) {
//        if ( exprList == null || exprTransform == null )
//            return exprList ;
        ExprList exprList2 = new ExprList() ;
        exprList.forEach( e->exprList2.add(transform(e)) );
        return exprList2 ;
    }

    @Override
    public void visit(OpOrder opOrder) {
        List<SortCondition> conditions = opOrder.getConditions() ;
        List<SortCondition> conditions2 = new ArrayList<>() ;
        boolean changed = false ;

        for ( SortCondition sc : conditions ) {
            Expr e = sc.getExpression() ;
            Expr e2 = transform(e) ;
            conditions2.add(new SortCondition(e2, sc.getDirection())) ;
            if ( e != e2 )
                changed = true ;
        }
        OpOrder x = opOrder ;
        if ( changed )
            x = new OpOrder(opOrder.getSubOp(), conditions2) ;
        visit1(x) ;
    }

    @Override
    public void visit(OpAssign opAssign) {
        VarExprList varExpr = opAssign.getVarExprList() ;
        VarExprList varExpr2 = process(varExpr) ;
        OpAssign opAssign2 = opAssign ;
        if ( varExpr != varExpr2 )
            opAssign2 = OpAssign.create(opAssign.getSubOp(), varExpr2) ;
        visit1(opAssign2) ;
    }

    @Override
    public void visit(OpExtend opExtend) {
        VarExprList varExpr = opExtend.getVarExprList() ;
        VarExprList varExpr2 = process(varExpr) ;
        OpExtend opExtend2 = opExtend ;
        if ( varExpr != varExpr2 )
            opExtend2 = OpExtend.create(opExtend.getSubOp(), varExpr2) ;
        visit1(opExtend2) ;
    }

    private VarExprList process(VarExprList varExprList) {
        if ( varExprList == null )
            return varExprList ;
        List<Var> vars = varExprList.getVars() ;
        VarExprList varExpr2 = new VarExprList() ;
        boolean changed = false ;
        for ( Var v : vars ) {
            Expr e = varExprList.getExpr(v) ;
            Expr e2 = e ;
            if ( e != null )
                e2 = transform(e) ;
            if ( e2 == null )
                varExpr2.add(v) ;
            else
                varExpr2.add(v, e2) ;
            if ( e != e2 )
                changed = true ;
        }
        if ( !changed )
            return varExprList ;
        return varExpr2 ;
    }

    private ExprList process(ExprList exprList) {
        if ( exprList == null )
            return null ;
        ExprList exprList2 = new ExprList() ;
        boolean changed = false ;
        for ( Expr e : exprList ) {
            Expr e2 = process(e) ;
            exprList2.add(e2) ;
            if ( e != e2 )
                changed = true ;
        }
        if ( !changed )
            return exprList ;
        return exprList2 ;
    }

    private Expr process(Expr expr) {
        Expr e = expr ;
        Expr e2 = e ;
        if ( e != null )
            e2 = transform(e) ;
        if ( e == e2 )
            return expr ;
        return e2 ;
    }

    @Override
    public void visit(OpGroup opGroup) {
        boolean changed = false ;

        VarExprList varExpr = opGroup.getGroupVars() ;
        VarExprList varExpr2 = process(varExpr) ;
        if ( varExpr != varExpr2 )
            changed = true ;

        List<ExprAggregator> aggs = opGroup.getAggregators() ;
        List<ExprAggregator> aggs2 = aggs ;

        // And the aggregators...
        aggs2 = new ArrayList<>() ;
        for ( ExprAggregator agg : aggs ) {
            Aggregator aggregator = agg.getAggregator() ;
            Var v = agg.getVar() ;

            // Variable associated with the aggregate
            Expr eVar = agg.getAggVar() ;   // Not .getExprVar()
            Expr eVar2 = transform(eVar) ;
            if ( eVar != eVar2 )
                changed = true ;

            // The Aggregator expression
            ExprList e = aggregator.getExprList() ;
            ExprList e2 = e ;
            if ( e != null )
                // Null means "no relevant expression" e.g. COUNT(*)
                e2 = transform(e) ;
            if ( e != e2 )
                changed = true ;
            Aggregator a2 = aggregator.copy(e2) ;
            aggs2.add(new ExprAggregator(eVar2.asVar(), a2)) ;
        }

        OpGroup opGroup2 = opGroup ;
        if ( changed )
            opGroup2 = new OpGroup(opGroup.getSubOp(), varExpr2, aggs2) ;
        visit1(opGroup2) ;
    }

    @Override
    public void visit0(Op0 op) {
        push(opStack, op.apply(opTransform)) ;
    }

    @Override
    public void visit1(Op1 op) {
        Op subOp = null ;
        if ( op.getSubOp() != null )
            subOp = pop(opStack) ;
        push(opStack, op.apply(opTransform, subOp)) ;
    }

    @Override
    public void visit2(Op2 op) {
        Op left = null ;
        Op right = null ;

        // Must do right-left because the pushes onto the stack were left-right.
        if ( op.getRight() != null )
            right = pop(opStack) ;
        if ( op.getLeft() != null )
            left = pop(opStack) ;
        Op opX = op.apply(opTransform, left, right) ;
        push(opStack, opX) ;
    }

    @Override
    public void visitN(OpN op) {
        List<Op> x = new ArrayList<>(op.size()) ;

        for ( Iterator<Op> iter = op.iterator() ; iter.hasNext() ; ) {
            Op sub = iter.next() ;
            Op r = pop(opStack) ;
            // Skip nulls.
            if ( r != null )
                // Add in reverse.
                x.add(0, r) ;
        }
        Op opX = op.apply(opTransform, x) ;
        push(opStack, opX) ;
    }

    @Override
    public void visit(OpFilter opFilter) {
        Op subOp = null ;
        if ( opFilter.getSubOp() != null )
            subOp = pop(opStack) ;
        boolean changed = (opFilter.getSubOp() != subOp) ;

        ExprList ex = opFilter.getExprs() ;
        ExprList ex2 = process(ex) ;
        OpFilter f = opFilter ;
        if ( ex != ex2 )
            f = (OpFilter)OpFilter.filter(ex2, subOp) ;
        push(opStack, f.apply(opTransform, subOp)) ;
    }

    @Override
    public void visit(OpLeftJoin op) {
        Op left = null ;
        Op right = null ;

        // Must do right-left because the pushes onto the stack were left-right.
        if ( op.getRight() != null )
            right = pop(opStack) ;
        if ( op.getLeft() != null )
            left = pop(opStack) ;

        ExprList exprs = op.getExprs() ;
        ExprList exprs2 = process(exprs) ;
        OpLeftJoin x = op ;
        if ( exprs != exprs2 )
            x = OpLeftJoin.createLeftJoin(left, right, exprs2) ;
        Op opX = x.apply(opTransform, left, right) ;
        push(opStack, opX) ;
    }
    
    @Override
    public void visit(OpService op) {
        if ( ! visitService ) {
            // No visit - push input.
            push(opStack, op) ;
            return ;
        }
        // op.getService()
        OpVisitorByTypeAndExpr.super.visit(op);
    }

    @Override
    public void visitExt(OpExt op) {
        push(opStack, opTransform.transform(op)) ;
    }
    
    @Override
    public void visitExpr(ExprList exprs) { 
        System.err.println("visitExpr(ExprList)") ;
        if ( exprs != null && exprTransform != null ) {
            
        }
    }
    
    @Override
    public void visitExpr(VarExprList exprVarExprList)  {
        System.err.println("visitExpr(ExprList)") ;
        if ( exprVarExprList != null && exprTransform != null ) {
            
        }
    }
    
    @Override
    public void visit(ExprFunction0 func) {
        Expr e = func.apply(exprTransform) ;
        push(exprStack, e) ;
    }

    @Override
    public void visit(ExprFunction1 func) {
        Expr e1 = pop(exprStack) ;
        Expr e = func.apply(exprTransform, e1) ;
        push(exprStack, e) ;
    }

    @Override
    public void visit(ExprFunction2 func) {
        Expr e2 = pop(exprStack) ;
        Expr e1 = pop(exprStack) ;
        Expr e = func.apply(exprTransform, e1, e2) ;
        push(exprStack, e) ;
    }

    @Override
    public void visit(ExprFunction3 func) {
        Expr e3 = pop(exprStack) ;
        Expr e2 = pop(exprStack) ;
        Expr e1 = pop(exprStack) ;
        Expr e = func.apply(exprTransform, e1, e2, e3) ;
        push(exprStack, e) ;
    }

    @Override
    public void visit(ExprFunctionN func) {
        ExprList x = process(func.getArgs()) ;
        Expr e = func.apply(exprTransform, x) ;
        push(exprStack, e) ;
    }

    private ExprList process(List<Expr> exprList) {
        if ( exprList == null )
            return null ;
        int N = exprList.size() ;
        List<Expr> x = new ArrayList<>(N) ;
        for ( Expr anExprList : exprList ) {
            Expr e2 = pop(exprStack) ;
            // Add in reverse.
            x.add(0, e2) ;
        }
        return new ExprList(x) ;
    }

    @Override
    public void visit(ExprFunctionOp funcOp) {
        ExprList x = null ;
//        Op op = transform(funcOp.getGraphPattern()) ;
        if ( funcOp.getArgs() != null )
            x = process(funcOp.getArgs()) ;
        Expr e = funcOp.apply(exprTransform, x, funcOp.getGraphPattern()) ;
        push(exprStack, e) ;
    }

    @Override
    public void visit(NodeValue nv) {
        Expr e = nv.apply(exprTransform) ;
        push(exprStack, e) ;
    }

    @Override
    public void visit(ExprVar var) {
        Expr e = var.apply(exprTransform) ;
        push(exprStack, e) ;
    }

    @Override
    public void visit(ExprAggregator eAgg) {
        Expr e = eAgg.apply(exprTransform) ;
        push(exprStack, e) ;
    }

    private <T> void push(Deque<T> stack, T value) {
        stack.push(value) ;
    }

    private <T> T pop(Deque<T> stack) {
        try {
            T v = stack.pop() ;
            if ( v ==  null )
                Log.warn(ApplyTransformVisitor.class, "Pop null from "+stackLabel(stack)+" stack") ;
            return v ;
        }
        catch (EmptyStackException ex) {
            Log.warn(ApplyTransformVisitor.class, "Empty "+stackLabel(stack)+" stack") ;
            return null ;
        }
    }
    
    private String stackLabel(Deque<?> stack) {
        if ( stack == opStack ) return "Op" ;
        if ( stack == exprStack ) return "Expr" ;
        return "<other>" ;
    }
}
