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

import org.apache.jena.atlas.lib.InternalErrorException ;
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

/** Apply the {@link Transform}, {@link ExprTransform}
 *  Works in conjunction with {@link WalkerVisitor}.
 */
public class ApplyTransformVisitor implements OpVisitorByTypeAndExpr, ExprVisitor {
    private final Transform     opTransform ;
    private final ExprTransform exprTransform ;

    protected final boolean     visitService ;

    private final Deque<Op>     opStack   = new ArrayDeque<>() ;
    private final Deque<Expr>   exprStack = new ArrayDeque<>() ;

    private final OpVisitor     beforeVisitor ;
    private final OpVisitor     afterVisitor ;

    public ApplyTransformVisitor(Transform opTransform, ExprTransform exprTransform,
                                 boolean visitService,
                                 OpVisitor before, OpVisitor after) {
        this.opTransform = opTransform ;
        this.exprTransform = exprTransform ;
        this.beforeVisitor = before ;
        this.afterVisitor = after ;
        this.visitService = visitService ;
    }

    public /*package*/ final Op opResult() {
        return pop(opStack) ;
    }

    /*package*/ final Expr exprResult() {
        return pop(exprStack) ;
    }

    // These three could be calls within WalkerVisitor followed by "collect".
    protected Expr transform(Expr expr) {
        int x1 = opStack.size() ;
        int x2 = exprStack.size() ;
        try {
            return Walker.transform(expr, this, beforeVisitor, afterVisitor) ;
        } finally {
            int y1 = opStack.size() ;
            int y2 = exprStack.size() ;
            if ( x1 != y1 )
                Log.error(ApplyTransformVisitor.class, "Misaligned opStack") ;
            if ( x2 != y2 )
                Log.error(ApplyTransformVisitor.class, "Misaligned exprStack") ;
        }
    }

    protected ExprList transform(ExprList exprList) {
//        if ( exprList == null || exprTransform == null )
//            return exprList ;
        ExprList exprList2 = new ExprList() ;
        exprList.forEach( e->exprList2.add(transform(e)) );
        return exprList2 ;
    }

    protected List<SortCondition> transform(List<SortCondition> conditions) {
        List<SortCondition> conditions2 = new ArrayList<>() ;
        boolean changed = false ;

        for ( SortCondition sc : conditions ) {
            Expr e = sc.getExpression() ;
            Expr e2 = transform(e) ;
            conditions2.add(new SortCondition(e2, sc.getDirection())) ;
            if ( e != e2 )
                changed = true ;
        }
        if ( changed )
            return conditions2 ;
        else
            return conditions ;
  }

    // Interact with WalkerVisitor.

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
        VarExprList varExpr2 = collect(varExpr) ;
        OpAssign opAssign2 = opAssign ;
        if ( varExpr != varExpr2 )
            opAssign2 = OpAssign.create(opAssign.getSubOp(), varExpr2) ;
        visit1(opAssign2) ;
    }

    @Override
    public void visit(OpExtend opExtend) {
        VarExprList varExpr = opExtend.getVarExprList() ;
        VarExprList varExpr2 = collect(varExpr) ;
        OpExtend opExtend2 = opExtend ;
        if ( varExpr != varExpr2 )
            opExtend2 = OpExtend.create(opExtend.getSubOp(), varExpr2) ;
        visit1(opExtend2) ;
    }

    @Override
    public void visit(OpUnfold opUnfold) {
        final Expr e2 = pop(exprStack);
        if ( ! opUnfold.getExpr().equals(e2) )
            throw new IllegalArgumentException("Expression on stack differs from expression of UNFOLD.");

        visit1(opUnfold) ;
    }

    // Special test cases for collectors.

    // Careful about order.
    private VarExprList collect(VarExprList varExprList) {
        if ( varExprList == null )
            return varExprList ;
      List<Var> vars = varExprList.getVars() ;
      VarExprList varExpr2 = new VarExprList() ;

      List<Expr> x = collect(vars.size()) ;

      boolean changed = false ;
      for ( int i = 0 ; i < vars.size() ; i++ ) {
          Var v = vars.get(i) ;
          Expr e2 = x.get(i) ;
          Expr e = varExpr2.getExpr(v) ;
          if ( e != e2 )
              changed = true ;
          if ( e2 == null )
              varExpr2.add(v) ;
          else {
              varExpr2.add(v, e2) ;
          }
      }
      return changed ? varExpr2 : varExprList ;
    }

    private ExprList collect(ExprList exprList) {
        if ( exprList == null )
            return null ;
        List<Expr> x = collect(exprList.size()) ;
        boolean changed = false ;
        for ( int i = 0 ; i < x.size() ; i++ ) {
            if ( x.get(i) != exprList.get(i) ) {
                changed = true ;
                break ;
            }
        }
        if ( ! changed )
            return exprList ;
        return new ExprList(x) ;
    }

    private ExprList collect(List<Expr> exprList) {
        if ( exprList == null )
            return null ;
        return new ExprList(collect(exprList.size())) ;
    }

    // collect and return in the original order (take account of stack reversal).
    private List<Expr> collect(int N) {
        // Check for "same"/unchanged
        List<Expr> x = new ArrayList<>(N) ;
        for ( int i = N-1 ; i >= 0 ; i-- ) {
            Expr e2 = pop(exprStack) ;
            if ( e2 == Expr.NONE )
                e2 = null ;
            x.add(0, e2) ;
        }
        return x ;
    }

    @Override
    public void visit(OpGroup opGroup) {
        boolean changed = false ;

        VarExprList varExpr = opGroup.getGroupVars() ;
        VarExprList varExpr2 = collect(varExpr) ;
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
            opGroup2 = OpGroup.create(opGroup.getSubOp(), varExpr2, aggs2) ;
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

    private void dump(String label) {
        System.out.println(label) ;
        String x = opStack.toString().replace('\n', ' ').replaceAll("  +", " ") ;
        String y = exprStack.toString().replace('\n', ' ').replaceAll("  +", " ") ;
        System.out.println("    O:"+x);
        System.out.println("    E:"+y);
    }

    @Override
    public void visit(OpFilter opFilter) {
        Op subOp = null ;
        if ( opFilter.getSubOp() != null )
            subOp = pop(opStack) ;
        ExprList ex = opFilter.getExprs() ;
        if ( ex == null || ex.isEmpty() ) {
            // No expressions.
            // Doesn't normally happen but this code is safe in these cases.
            push(opStack, opFilter.apply(opTransform, subOp)) ;
            return ;
        }
        // ex2 is the same length as ex.
        ExprList ex2 = collect(ex) ;
        OpFilter f = opFilter ;
        if ( ex != ex2 || opFilter.getSubOp() != subOp ) {
            f = OpFilter.filterAlways(ex2, subOp) ;
            // If we removed a layer of filter, then subOp needs changing
            // else f-subOp is subOp anyway.
            subOp = f.getSubOp() ;
        }
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
        ExprList exprs2 = collect(exprs) ;
        OpLeftJoin x = op ;
        if ( exprs != exprs2 )
            x = OpLeftJoin.createLeftJoin(left, right, exprs2) ;
        Op opX = x.apply(opTransform, left, right) ;
        push(opStack, opX) ;
    }

    @Override
    public void visit(OpService op) {
        if ( ! visitService ) {
            // No visit - no transform - push input.
            push(opStack, op) ;
            return ;
        }
        OpVisitorByTypeAndExpr.super.visit(op);
    }

    @Override
    public void visitExt(OpExt op) {
        push(opStack, opTransform.transform(op)) ;
    }

    @Override
    public void visitExpr(ExprList exprs) {
        throw new InternalErrorException("Didn't expect as call to ApplyTransformVisit.visitExpr") ;
    }

    @Override
    public void visitVarExpr(VarExprList exprVarExprList)  {
        throw new InternalErrorException("Didn't expect as call to ApplyTransformVisit.visitVarExpr") ;
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
        ExprList x = collect(func.getArgs()) ;
        Expr e = func.apply(exprTransform, x) ;
        push(exprStack, e) ;
    }

    @Override
    public void visit(ExprFunctionOp funcOp) {
        ExprList x = null ;
        if ( funcOp.getArgs() != null )
            x = collect(funcOp.getArgs()) ;
        Op op = pop(opStack) ;
        Expr e = funcOp.apply(exprTransform, x, op) ;
        push(exprStack, e) ;
    }

    @Override
    public void visit(ExprTripleTerm tripleTerm) {
        //Expr e = tripleTerm.apply(exprTransform) ;
        Expr e = tripleTerm;
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

    @Override
    public void visit(ExprNone e) {
        push(exprStack, e) ;
    }

    private <T> void push(Deque<T> stack, T value) {
        if ( value == null )
            Log.warn(ApplyTransformVisitor.class, "Pushing null onto the "+stackLabel(stack)+" stack") ;
        stack.push(value) ;
    }

    private <T> T pop(Deque<T> stack) {
        try {
            T v = stack.pop() ;
            if ( v ==  null )
                Log.warn(ApplyTransformVisitor.class, "Pop null from the "+stackLabel(stack)+" stack") ;
            return v ;
        }
        catch (NoSuchElementException ex) {
            if ( true ) throw new RuntimeException() ;
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
