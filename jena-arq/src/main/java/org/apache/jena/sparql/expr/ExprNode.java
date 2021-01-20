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

package org.apache.jena.sparql.expr;

import java.util.Set ;

import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.expr.nodevalue.XSDFuncOp ;
import org.apache.jena.sparql.function.FunctionEnv ;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.sse.writers.WriterExpr ;

/** 
 * A node that is a constraint expression that can be evaluated
 * An {@link Expr} is already a Constraint - ExprNode is the base implementation
 * of all {@link Expr} classes that provides the Constraint machinery.
 */
 
public abstract class ExprNode implements Expr
{
    @Override
    public boolean isSatisfied(Binding binding, FunctionEnv funcEnv) {
        try {
            NodeValue v = eval(binding, funcEnv) ;
            boolean b = XSDFuncOp.booleanEffectiveValue(v) ;
            return b ;
        }
        catch (ExprEvalException ex) { 
            return false ;
        }
    }

    public boolean isExpr()     { return true ; }
    public final Expr getExpr() { return this ; }
    
    // --- interface Constraint
    
    @Override
    public abstract NodeValue eval(Binding binding, FunctionEnv env) ; 
    
    @Override
    public final Set<Var> getVarsMentioned()                    { return ExprVars.getVarsMentioned(this) ; }

    @Override
    public abstract int hashCode() ;
    @Override
    public final boolean equals(Object other) {
        if ( other == null ) return false ;
        if ( this == other ) return true ;
        if ( ! ( other instanceof Expr ) ) return false ;
        return equals((Expr)other, false) ;
    }
    
    @Override
    public final boolean equalsBySyntax(Expr other) {
        if ( other == null ) return false ;
        if ( this == other ) return true ;
        return equals(other, true) ;
    }
    
    @Override
    public abstract boolean equals(Expr other, boolean bySyntax) ;
    
    protected static NodeValue eval(Binding binding, FunctionEnv funcEnv, Expr expr) {   
        if ( expr == null ) return null ;
        return expr.eval(binding, funcEnv) ;
    }
    
    @Override
    final public Expr deepCopy()        { return copySubstitute(null) ; }
    
    @Override
    public abstract Expr copySubstitute(Binding binding) ;
    
    @Override
    public abstract Expr applyNodeTransform(NodeTransform transform) ;
        
    // ---- Default implementations
    @Override
    public boolean isVariable()         { return false ; }
    @Override
    public String getVarName()          { return null ; } //throw new ExprException("Expr.getVarName called on non-variable") ; }
    @Override
    public ExprVar getExprVar()         { return null ; } //throw new ExprException("Expr.getVar called on non-variable") ; }
    @Override
    public Var asVar()                  { return null ; } //throw new ExprException("Expr.getVar called on non-variable") ; }
    
    @Override
    public boolean isConstant()         { return false ; }
    @Override
    public NodeValue getConstant()      { return null ; } // throw new ExprException("Expr.getConstant called on non-constant") ; }
    
    @Override
    public boolean isFunction()         { return false ; }
    @Override
    public ExprFunction getFunction()   { return null ; }

    public boolean isGraphPattern()     { return false ; }
    public Op getGraphPattern()         { return null ; }
    @Override
    public String toString()            { return WriterExpr.asString(this) ; } 
}
