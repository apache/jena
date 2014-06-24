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

package com.hp.hpl.jena.sparql.expr;

import java.util.* ;

import org.apache.jena.atlas.logging.Log ;

import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.Transformer ;

public class ExprTransformer
{
    private static ExprTransformer singleton = new ExprTransformer();
    
    /** Get the current transformer of expressions */
    public static ExprTransformer get() { return singleton; }
    
    /** Transform an expression */
    public static Expr transform(ExprTransform transform, Expr expr)
    { return get().transformation(transform, expr) ; }

    /** Transform an expression list */
    public static ExprList transform(ExprTransform transform, ExprList exprList)
    { return get().transformation(transform, exprList) ; }
    
    private Expr transformation(ExprTransform transform, Expr expr)
    {
        ApplyExprTransformVisitor v = new ApplyExprTransformVisitor(transform) ;
        return transformation(v, expr) ;
    }

    private ExprList transformation(ExprTransform transform, ExprList exprList)
    {
        ApplyExprTransformVisitor v = new ApplyExprTransformVisitor(transform) ;
        ExprList exprList2 = new ExprList() ;
        for ( Expr expr : exprList )
        {
            Expr expr2 = transformation(v, expr) ;
            exprList2.add(expr2) ;
        }
        return exprList2 ;
    }
    
    private Expr transformation(ApplyExprTransformVisitor applyVisitor, Expr expr)
    {
        ExprWalker.walk(applyVisitor, expr) ;
        return applyVisitor.result() ;
    }
    
    public static
    class ApplyExprTransformVisitor implements ExprVisitor
    {
        private ExprTransform transform ;
        private final Deque<Expr> stack = new ArrayDeque<>() ;
        
        final Expr result()
        { 
            if ( stack.size() != 1 )
                Log.warn(this, "Stack is not aligned") ;
            return stack.peek() ; 
        }

        ApplyExprTransformVisitor(ExprTransform transform)
        { this.transform = transform ; }

        @Override
        public void startVisit()    {}
        @Override
        public void finishVisit()   {}

        
        @Override
        public void visit(ExprFunction0 func)
        {
            Expr e = func.apply(transform) ;
            push(stack, e) ;
        }
        
        @Override
        public void visit(ExprFunction1 func)
        {
            Expr e1 = pop(stack) ;
            Expr e = func.apply(transform, e1) ;
            push(stack, e) ;
        }

        @Override
        public void visit(ExprFunction2 func)
        {
            Expr e2 = pop(stack) ;
            Expr e1 = pop(stack) ;
            Expr e = func.apply(transform, e1, e2) ;
            push(stack, e) ;
        }

        @Override
        public void visit(ExprFunction3 func)
        {
            Expr e3 = pop(stack) ;
            Expr e2 = pop(stack) ;
            Expr e1 = pop(stack) ;
            Expr e = func.apply(transform, e1, e2, e3) ;
            push(stack, e) ;
        }

        @Override
        public void visit(ExprFunctionN func)
        {
            ExprList x = process(func.getArgs()) ;
            Expr e = func.apply(transform, x) ;
            push(stack, e) ;
        }
        
        private ExprList process(List<Expr> exprList)
        {
            int N = exprList.size() ;
            List<Expr> x = new ArrayList<>(N) ;
            for ( Expr anExprList : exprList )
            {
                Expr e2 = pop( stack );
                // Add in reverse.
                x.add( 0, e2 );
            }
            return new ExprList(x) ;
        }
        
        @Override
        public void visit(ExprFunctionOp funcOp)
        {
            ExprList x = null ;
            if ( funcOp.getArgs() != null )
                x = process(funcOp.getArgs()) ;
            Op op = funcOp.getGraphPattern() ;
            // Caution: the expression can have a pattern inside it.
            // See also: ExprTransformApplyTransform which does much the same in a different way.
            if ( transform instanceof ExprTransformOp )
            {
                ExprTransformOp t = (ExprTransformOp)transform ;
                op = Transformer.transform(t.getTransform(), op) ; 
            }
            
            Expr e = funcOp.apply(transform, x, op) ;
            push(stack, e) ;

        }

        @Override
        public void visit(NodeValue nv)
        {
            Expr e = nv.apply(transform) ;
            push(stack, e) ;
        }

        @Override
        public void visit(ExprVar var)
        {
            Expr e = var.apply(transform) ;
            push(stack, e) ;
        }
        
        @Override
        public void visit(ExprAggregator eAgg)
        {
            Expr e = eAgg.apply(transform) ;
            push(stack, e) ;
        }
        
        private static void push(Deque<Expr> stack, Expr value)
        {
            stack.push(value) ;
        }
        
        private static Expr pop(Deque<Expr> stack)
        {
            try {
            Expr e = stack.pop();
            if ( e == NodeValue.nvNothing )
                e = null ;
            return e ;
            } catch ( EmptyStackException ex)
            {
                System.err.println("Empty stack") ;
                return null ;
            }
        }
    }
    
    
}
