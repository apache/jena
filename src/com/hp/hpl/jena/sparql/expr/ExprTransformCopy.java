/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.expr;

import java.util.List ;

import com.hp.hpl.jena.sparql.algebra.Op ;

public class ExprTransformCopy implements ExprTransform
{
    public static final boolean COPY_ALWAYS         = true ;
    public static final boolean COPY_ONLY_ON_CHANGE = false ;
    private boolean alwaysCopy = false ;
    
    public ExprTransformCopy()                          { this(COPY_ONLY_ON_CHANGE) ; }
    public ExprTransformCopy(boolean alwaysDuplicate)   { this.alwaysCopy = alwaysDuplicate ; }
    
    public Expr transform(ExprFunction0 func)                   
    { return xform(func) ; }

    public Expr transform(ExprFunction1 func, Expr expr1)                   
    { return xform(func, expr1) ; }
    
    public Expr transform(ExprFunction2 func, Expr expr1, Expr expr2)
    { return xform(func, expr1, expr2) ; }
    
    public Expr transform(ExprFunction3 func, Expr expr1, Expr expr2, Expr expr3)
    { return xform(func, expr1, expr2, expr3) ; }
    
    public Expr transform(ExprFunctionN func, ExprList args)
    { return xform(func, args) ; }

    public Expr transform(ExprFunctionOp funcOp, ExprList args, Op opArg)
    { return xform(funcOp, args, opArg) ; }
    
    public Expr transform(NodeValue nv)     
    { return xform(nv) ; }
    
    public Expr transform(ExprVar exprVar)       
    { return xform(exprVar) ; }

    public Expr transform(ExprAggregator eAgg)       
    { return xform(eAgg) ; }

    private Expr xform(ExprFunction0 func)
    {
        if ( !alwaysCopy )
            return func ;
        return func.copy() ;
    }

    private Expr xform(ExprFunction1 func, Expr expr1)
    {
        if ( !alwaysCopy && expr1 == func.getArg() )
            return func ;
        return func.copy(expr1) ;
    }
    
    private Expr xform(ExprFunction2 func, Expr expr1, Expr expr2)
    {
        if ( !alwaysCopy && 
                expr1 == func.getArg1() &&
                expr2 == func.getArg2() )
            return func ;
        return func.copy(expr1, expr2) ;
    }
    
    private Expr xform(ExprFunction3 func, Expr expr1, Expr expr2, Expr expr3)
    {
        if ( !alwaysCopy && 
                expr1 == func.getArg1() &&
                expr2 == func.getArg2() &&
                expr3 == func.getArg3() )
        return func ;
    return func.copy(expr1, expr2, expr3) ;
    }
    
    private Expr xform(ExprFunctionN func, ExprList args)
    {
        if ( ! alwaysCopy && equals1(func.getArgs(), args.getList()) )
            return func ;
        return func.copy(args) ;
    }
    
    private boolean equals1(List<Expr> list1, List<Expr> list2)
    {
        if ( list1 == null && list2 == null )
            return true ;
        if ( list1 == null )
            return false ;
        if ( list2 == null )
            return false ;
        
        if ( list1.size() != list2.size() )
            return false ;
        for ( int i = 0 ; i < list1.size() ; i++ )
        {
            if ( list1.get(i) != list2.get(i) )
                return false ;
        }
        return true ;
    }
    
    private Expr xform(ExprFunctionOp funcOp, ExprList args, Op opArg)
    {
        if ( ! alwaysCopy && equals1(funcOp.getArgs(), args.getList()) && funcOp.getGraphPattern() == opArg )
            return funcOp ;
        return funcOp.copy(args, opArg) ;
    }
    
    private Expr xform(NodeValue nv)
    {
        return nv ;
    }
    
    private Expr xform(ExprVar exprVar)
    {
        return exprVar ;
    }
    
    private Expr xform(ExprAggregator eAgg)
    {
        if ( ! alwaysCopy )
            return eAgg ;
        
        return eAgg.copy(eAgg.getVar()) ;
    }

}

/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * (c) Copyright 2010 Epimorphics Ltd.
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