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


/** A function of two arguments */
 
public abstract class ExprFunction2 extends ExprFunction
{
    protected final Expr expr1 ;
    protected final Expr expr2 ;

    protected ExprFunction2(Expr expr1, Expr expr2, String fName) { this(expr1, expr2, fName, null) ; }
    
    protected ExprFunction2(Expr expr1, Expr expr2, String fName, String opSign)
    {
        super(fName, opSign) ;
        this.expr1 = expr1 ;
        this.expr2 = expr2 ;
    }
    
    public Expr getArg1() { return expr1 ; }
    public Expr getArg2() { return expr2 ; }
    
    @Override
    public Expr getArg(int i)
    {
        if ( i == 1 )
            return expr1 ; 
        if ( i == 2 )
            return expr2 ; 
        return null ;
    }
    
    @Override
    public int numArgs() { return 2 ; }
    
    // ---- Evaluation
    
    @Override
    public int hashCode()
    {
        return getFunctionSymbol().hashCode() ^
                Lib.hashCodeObject(expr1) ^
                Lib.hashCodeObject(expr2) ;
    }

    @Override
    final public NodeValue eval(Binding binding, FunctionEnv env)
    {
        NodeValue s = evalSpecial(binding, env) ;
        if ( s != null )
            return s ;
        
        NodeValue x = eval(binding, env, expr1) ;
        NodeValue y = eval(binding, env, expr2) ;
        return eval(x, y, env) ;
    }
    
    /** Special form evaluation (example, don't eval the arguments first) */
    protected NodeValue evalSpecial(Binding binding, FunctionEnv env) { return null ; } 
    
    public NodeValue eval(NodeValue x, NodeValue y, FunctionEnv env) { return eval(x,y) ; }

    public abstract NodeValue eval(NodeValue x, NodeValue y) ; 

    @Override
    final public Expr copySubstitute(Binding binding)
    {
        Expr e1 = (expr1 == null ? null : expr1.copySubstitute(binding)) ;
        Expr e2 = (expr2 == null ? null : expr2.copySubstitute(binding)) ;
        return copy(e1, e2) ;
    }
    

    @Override
    final public Expr applyNodeTransform(NodeTransform transform)
    {
        Expr e1 = (expr1 == null ? null : expr1.applyNodeTransform(transform)) ;
        Expr e2 = (expr2 == null ? null : expr2.applyNodeTransform(transform)) ;
        return copy(e1, e2) ;
    }


    public abstract Expr copy(Expr arg1, Expr arg2) ;

    @Override
    public void visit(ExprVisitor visitor) { visitor.visit(this) ; }
    public Expr apply(ExprTransform transform, Expr arg1, Expr arg2) { return transform.transform(this, arg1, arg2) ; }

}
