/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.expr;

import java.util.ArrayList ;
import java.util.EmptyStackException ;
import java.util.List ;
import java.util.Stack ;

import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.Transformer ;
import org.openjena.atlas.logging.Log ;

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
        private final Stack<Expr> stack = new Stack<Expr>() ;
        
        final Expr result()
        { 
            if ( stack.size() != 1 )
                Log.warn(this, "Stack is not aligned") ;
            return stack.peek() ; 
        }

        ApplyExprTransformVisitor(ExprTransform transform)
        { this.transform = transform ; }

        public void startVisit()    {}
        public void finishVisit()   {}

        
        public void visit(ExprFunction0 func)
        {
            Expr e = func.apply(transform) ;
            push(stack, e) ;
        }
        
        public void visit(ExprFunction1 func)
        {
            Expr e1 = pop(stack) ;
            Expr e = func.apply(transform, e1) ;
            push(stack, e) ;
        }

        public void visit(ExprFunction2 func)
        {
            Expr e2 = pop(stack) ;
            Expr e1 = pop(stack) ;
            Expr e = func.apply(transform, e1, e2) ;
            push(stack, e) ;
        }

        public void visit(ExprFunction3 func)
        {
            Expr e3 = pop(stack) ;
            Expr e2 = pop(stack) ;
            Expr e1 = pop(stack) ;
            Expr e = func.apply(transform, e1, e2, e3) ;
            push(stack, e) ;
        }

        public void visit(ExprFunctionN func)
        {
            ExprList x = process(func.getArgs()) ;
            Expr e = func.apply(transform, x) ;
            push(stack, e) ;
        }
        
        private ExprList process(List<Expr> exprList)
        {
            int N = exprList.size() ;
            List<Expr> x = new ArrayList<Expr>(N) ;
            for ( int i = 0 ; i < N ; i++ )
            {
                Expr e2 = pop(stack) ;
                // Add in reverse.
                x.add(0, e2) ;
            }
            return new ExprList(x) ;
        }
        
        public void visit(ExprFunctionOp funcOp)
        {
            ExprList x = null ;
            if ( funcOp.getArgs() != null )
                x = process(funcOp.getArgs()) ;
            Op op = funcOp.getGraphPattern() ;
            if ( transform instanceof ExprTransformOp )
            {
                ExprTransformOp t = (ExprTransformOp)transform ;
                op = Transformer.transform(t.getTransform(), op) ; 
            }
            
            Expr e = funcOp.apply(transform, x, op) ;
            push(stack, e) ;

        }

        public void visit(NodeValue nv)
        {
            Expr e = nv.apply(transform) ;
            push(stack, e) ;
        }

        public void visit(ExprVar var)
        {
            Expr e = var.apply(transform) ;
            push(stack, e) ;
        }
        
        public void visit(ExprAggregator eAgg)
        {
            Expr e = eAgg.apply(transform) ;
            push(stack, e) ;
        }
        
        private static void push(Stack<Expr> stack, Expr value)
        {
            stack.push(value) ;
        }
        
        private static Expr pop(Stack<Expr> stack)
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

/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */