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

import org.apache.jena.atlas.lib.Lib ;

import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.function.FunctionEnv ;
import com.hp.hpl.jena.sparql.graph.NodeTransform ;

/** A function that has a single argument */
 
public abstract class ExprFunction1 extends ExprFunction
{
    protected final Expr expr ;

    protected ExprFunction1(Expr expr, String fName) { this(expr, fName, null) ; }
    
    protected ExprFunction1(Expr expr, String fName, String opSign)
    {
        super(fName, opSign) ;
        this.expr = expr ;
    }

    public Expr getArg() { return expr ; }

    @Override
    public Expr getArg(int i)
    {
        if ( i == 1 )
            return expr ; 
        return null ;
    }
    
    @Override
    public int hashCode()
    {
        return getFunctionSymbol().hashCode() ^ Lib.hashCodeObject(expr) ;
    }

    @Override
    public int numArgs() { return 1 ; }
    
    // ---- Evaluation
    
    @Override
    final public NodeValue eval(Binding binding, FunctionEnv env)
    {
        NodeValue s = evalSpecial(binding, env) ;
        if ( s != null )
            return s ;
        
        NodeValue x = eval(binding, env, expr) ;
        return eval(x, env) ;
    }
    
    // Ideally, we would only have the FunctionEnv form but that break compatibility. 
    public NodeValue eval(NodeValue v, FunctionEnv env) { return eval(v) ; }
    public abstract NodeValue eval(NodeValue v) ;
    
    // Allow special cases.
    protected NodeValue evalSpecial(Binding binding, FunctionEnv env) { return null ; } 
    
    @Override
    final public Expr copySubstitute(Binding binding)
    {
        Expr e = (expr == null ? null : expr.copySubstitute(binding)) ;
        return copy(e) ;
    }

    @Override
    final public Expr applyNodeTransform(NodeTransform transform)
    {
        Expr e = (expr == null ? null : expr.applyNodeTransform(transform)) ;
        return copy(e) ;
    }
    
    public abstract Expr copy(Expr expr) ;
    
    @Override
    public void visit(ExprVisitor visitor) { visitor.visit(this) ; }
    public Expr apply(ExprTransform transform, Expr sub) { return transform.transform(this, sub) ; }
}
