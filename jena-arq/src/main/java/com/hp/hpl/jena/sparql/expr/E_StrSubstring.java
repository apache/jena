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

import com.hp.hpl.jena.sparql.expr.nodevalue.XSDFuncOp ;
import com.hp.hpl.jena.sparql.sse.Tags ;

public class E_StrSubstring extends ExprFunctionN
{
    private static final String symbol = Tags.tagSubstr ;

    public E_StrSubstring(Expr expr1, Expr expr2, Expr expr3)
    {
        super(symbol, expr1, expr2, expr3) ;
    }
    
    @Override
    public NodeValue eval(List<NodeValue> args)
    { 
        if ( args.size() == 2 )
            return XSDFuncOp.substring(args.get(0), args.get(1)) ;
        
        //return NodeFunctions.substring(args.get(0), args.get(1), args.get(2)) ;
        return XSDFuncOp.substring(args.get(0), args.get(1), args.get(2)) ;
    }

    @Override
    public Expr copy(ExprList newArgs)
    {
        if ( newArgs.size() == 2 )
            return new E_StrSubstring(newArgs.get(0), newArgs.get(1), null) ; 
        return new E_StrSubstring(newArgs.get(0), newArgs.get(1), newArgs.get(2)) ;   
    }
}
