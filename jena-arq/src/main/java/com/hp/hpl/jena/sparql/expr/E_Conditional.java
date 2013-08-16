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

import com.hp.hpl.jena.sparql.ARQInternalErrorException ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.function.FunctionEnv ;
import com.hp.hpl.jena.sparql.sse.Tags;

/** IF(expr, expr, expr) */ 

public class E_Conditional extends ExprFunction3
{
    private static final String functionName = Tags.tagIf ;
    
    private final Expr condition ;
    private final Expr thenExpr ;
    private final Expr elseExpr ;
    
    public E_Conditional(Expr condition, Expr thenExpr, Expr elseExpr)
    {
        // Don't let the parent eval the theEpxr or ifExpr.
        super(condition, thenExpr, elseExpr, functionName) ;
        // Better names,
        this.condition = condition ;
        this.thenExpr = thenExpr ;
        this.elseExpr = elseExpr ;
    }

    @Override
    public Expr copy(Expr arg1, Expr arg2, Expr arg3)
    {
        return new E_Conditional(arg1, arg2, arg3) ;
    }

    /** Special form evaluation (example, don't eval the arguments first) */
    @Override
    protected NodeValue evalSpecial(Binding binding, FunctionEnv env)
    {
        NodeValue nv = condition.eval(binding, env) ;
        if ( condition.isSatisfied(binding, env) )
            return thenExpr.eval(binding, env) ;
        else
            return elseExpr.eval(binding, env) ;
    }
    
    @Override
    public NodeValue eval(NodeValue x, NodeValue y, NodeValue z)
    {
        throw new ARQInternalErrorException() ;
    }
}
