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

import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.function.FunctionEnv ;

public abstract class E_OneOfBase extends ExprFunctionN
{   
    /* This operation stores it's arguments as a single list.
     * The first element of the array is the expression being tested,
     * the rest are the items to be used to test against.
     * There are cached copies of the LHS (car) and RHS (cdr).
     */
    
    // Cached values.
    protected final Expr expr ;
    protected final ExprList possibleValues ;
    
    protected E_OneOfBase(String name, Expr expr, ExprList args)
    {
        super(name, fixup(expr, args)) ;
        this.expr = expr ;
        this.possibleValues = args ;
    }
    
    // All ArgList, first arg is the expression.
    protected E_OneOfBase(String name, ExprList args)
    {
        super(name, args) ;
        this.expr = args.get(0) ;
        this.possibleValues = args.tail(1) ;
    }
    
    private static ExprList fixup(Expr expr2, ExprList args)
    {
        ExprList allArgs = new ExprList(expr2) ;
        allArgs.addAll(args) ;
        return allArgs ;
    }

    public Expr getLHS()        { return expr ; }
    public ExprList getRHS()    { return possibleValues; }

    
//    public Expr getLHS() { return expr ; }
//    public ExprList getRHS() { return possibleValues ; }

    protected boolean evalOneOf(Binding binding, FunctionEnv env)
    {
        // Special form.
        // Like ( expr = expr1 ) || ( expr = expr2 ) || ...

        NodeValue nv = expr.eval(binding, env) ;
        ExprEvalException error = null ;
        for ( Expr inExpr : possibleValues )
        {
            try {
                NodeValue maybe = inExpr.eval(binding, env) ;
                if ( NodeValue.sameAs(nv, maybe) )
                    return true ;
            } catch (ExprEvalException ex)
            {
                error = ex ;
            }
        }
        if ( error != null )
            throw error ;
        return false ;
    }
    
    protected boolean evalNotOneOf(Binding binding, FunctionEnv env)
    {
        return ! evalOneOf(binding, env) ;
    }
}
