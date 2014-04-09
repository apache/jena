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
import java.util.regex.Pattern ;

import com.hp.hpl.jena.sparql.expr.nodevalue.XSDFuncOp ;
import com.hp.hpl.jena.sparql.sse.Tags ;

public class E_StrReplace extends ExprFunctionN
{
    private static final String symbol = Tags.tagReplace ;
    private Pattern pattern = null ;

    public E_StrReplace(Expr expr1, Expr expr2, Expr expr3, Expr expr4)
    {
        super(symbol, expr1, expr2, expr3, expr4) ;
        
        
        if ( isString(expr2) && (expr4 == null || isString(expr4) ) )
        {
            int flags = 0 ;
            if ( expr4 != null && expr4.isConstant() && expr4.getConstant().isString() )
                flags = RegexJava.makeMask(expr4.getConstant().getString()) ;
            pattern = Pattern.compile(expr2.getConstant().getString(), flags) ;
        }
    }

    private static boolean isString(Expr expr) { return expr.isConstant() && expr.getConstant().isString() ; }
    
    @Override
    public NodeValue eval(List<NodeValue> args)
    {
        if ( pattern != null )
            return XSDFuncOp.strReplace(args.get(0), pattern, args.get(2)) ;

        if ( args.size() == 3 )
            return XSDFuncOp.strReplace(args.get(0), args.get(1), args.get(2)) ;
        return XSDFuncOp.strReplace(args.get(0), args.get(1), args.get(2), args.get(3)) ;
    }

    @Override
    public Expr copy(ExprList newArgs)
    {
        if ( newArgs.size() == 3 )
            return new E_StrReplace(newArgs.get(0), newArgs.get(1), newArgs.get(2), null) ;
        return new E_StrReplace(newArgs.get(0), newArgs.get(1), newArgs.get(2), newArgs.get(3)) ;
    }
    
    
    
//    @Override
//    public NodeValue eval(NodeValue x, NodeValue y, NodeValue z)
//    {
//        if ( pattern == null )
//            return XSDFuncOp.strReplace(x, y, z) ;
//        return XSDFuncOp.strReplace(x, pattern, z) ;
//    }
//
//    @Override
//    public Expr copy(Expr arg1, Expr arg2, Expr arg3)
//    {
//        return new E_StrReplace(arg1, arg2, arg3) ;   
//    }
}
