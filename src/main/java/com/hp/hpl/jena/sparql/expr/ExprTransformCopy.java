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

import java.util.List ;

import com.hp.hpl.jena.sparql.algebra.Op ;

public class ExprTransformCopy implements ExprTransform
{
    public static final boolean COPY_ALWAYS         = true ;
    public static final boolean COPY_ONLY_ON_CHANGE = false ;
    private boolean alwaysCopy = false ;
    
    public ExprTransformCopy()                          { this(COPY_ONLY_ON_CHANGE) ; }
    public ExprTransformCopy(boolean alwaysDuplicate)   { this.alwaysCopy = alwaysDuplicate ; }
    
    @Override
    public Expr transform(ExprFunction0 func)                   
    { return xform(func) ; }

    @Override
    public Expr transform(ExprFunction1 func, Expr expr1)                   
    { return xform(func, expr1) ; }
    
    @Override
    public Expr transform(ExprFunction2 func, Expr expr1, Expr expr2)
    { return xform(func, expr1, expr2) ; }
    
    @Override
    public Expr transform(ExprFunction3 func, Expr expr1, Expr expr2, Expr expr3)
    { return xform(func, expr1, expr2, expr3) ; }
    
    @Override
    public Expr transform(ExprFunctionN func, ExprList args)
    { return xform(func, args) ; }

    @Override
    public Expr transform(ExprFunctionOp funcOp, ExprList args, Op opArg)
    { return xform(funcOp, args, opArg) ; }
    
    @Override
    public Expr transform(NodeValue nv)     
    { return xform(nv) ; }
    
    @Override
    public Expr transform(ExprVar exprVar)       
    { return xform(exprVar) ; }

    @Override
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
