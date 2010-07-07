/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.expr;

import java.util.Stack ;

import com.hp.hpl.jena.sparql.util.ALog ;

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
                ALog.warn(this, "Stack is not aligned") ;
            return stack.pop() ; 
        }

        ApplyExprTransformVisitor(ExprTransform transform)
        { this.transform = transform ; }

        public void startVisit()    {}
        public void finishVisit()   {}

        // Needs all subcases :-(
        public void visit(ExprFunction func)
        {
//            
//            for ( int i = 0 ; i < func.numArgs() ; i++ )
//            {
//                Expr expr = stack.pop() ;
//            }
//            new ExprFunction()
//            stack.push(expr2) ;
        }

        public void visit(ExprFunctionOp funcOp)
        {
//            funcOp.apply(transform) ;
        }

        public void visit(NodeValue nv)
        {
//            nv.apply(transform) ;
        }

        public void visit(ExprVar var)
        {
//            var.apply(transform) ;
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