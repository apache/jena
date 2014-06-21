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

import java.util.Collection ;
import java.util.Set ;

import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.expr.nodevalue.XSDFuncOp ;
import com.hp.hpl.jena.sparql.function.FunctionEnv ;
import com.hp.hpl.jena.sparql.graph.NodeTransform ;
import com.hp.hpl.jena.sparql.util.ExprUtils ;


/** A node that is a constraint expression that can be evaluated
 * An Expr is already a Constraint - ExprNode is the base implementation
 * of all Expr classes that provides the Constraint machinary. */
 
public abstract class ExprNode implements Expr
{
    @Override
    public boolean isSatisfied(Binding binding, FunctionEnv funcEnv)
    {
        try {
            NodeValue v = eval(binding, funcEnv) ;
            boolean b = XSDFuncOp.booleanEffectiveValue(v) ;
            return b ;
        }
        catch (ExprEvalException ex)
        { 
            return false ;
        }
    }

    public boolean isExpr() { return true ; }
    public final Expr getExpr()   { return this ; }
    
    // --- interface Constraint
    
    @Override
    public abstract NodeValue eval(Binding binding, FunctionEnv env) ; 
    
    @Override
    public Set<Var> getVarsMentioned() { return ExprVars.getVarsMentioned(this) ; }
    @Override
    public void varsMentioned(Collection<Var> acc) { ExprVars.varsMentioned(acc, this) ; }

    public Set<String> getVarNamesMentioned() { return ExprVars.getVarNamesMentioned(this) ; }
    public void varNamesMentioned(Collection<String> acc) { ExprVars.varNamesMentioned(acc, this) ; }

    @Override
    public abstract int     hashCode() ;
    @Override
    public abstract boolean equals(Object other) ;
    
    protected static NodeValue eval(Binding binding, FunctionEnv funcEnv, Expr expr)
    {   
        if ( expr == null ) return null ;
        return expr.eval(binding, funcEnv) ;
    }
    
    @Override
    final public Expr deepCopy()                     
    { return copySubstitute(null) ; }
    
    @Override
    public abstract Expr copySubstitute(Binding binding) ;
    
    @Override
    public abstract Expr applyNodeTransform(NodeTransform transform) ;

        
    // ---- Default implementations
    @Override
    public boolean isVariable()        { return false ; }
    @Override
    public String getVarName()         { return null ; } //throw new ExprException("Expr.getVarName called on non-variable") ; }
    @Override
    public ExprVar getExprVar()        { return null ; } //throw new ExprException("Expr.getVar called on non-variable") ; }
    @Override
    public Var asVar()                 { return null ; } //throw new ExprException("Expr.getVar called on non-variable") ; }
    
    @Override
    public boolean isConstant()        { return false ; }
    @Override
    public NodeValue getConstant()     { return null ; } // throw new ExprException("Expr.getConstant called on non-constant") ; }
    
    @Override
    public boolean isFunction()        { return false ; }
    @Override
    public ExprFunction getFunction()  { return null ; }
    
    public boolean isGraphPattern()    { return false ; }
    public Op getGraphPattern()        { return null ; }
    
    // ---- 
    
    @Override
    public String toString()
    {
        return ExprUtils.fmtSPARQL(this) ; 
    }
}
